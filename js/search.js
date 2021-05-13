// When the user clicks on the search box, we want to toggle the search dropdown
function displayToggleSearch(e) {
  e.preventDefault();
  e.stopPropagation();

  closeDropdownSearch(e);
  
  if (idx === null) {
    console.log("Building search index...");
    prepareIdxAndDocMap();
    console.log("Search index built.");
  }
  const dropdown = document.querySelector("#search-dropdown-content");
  if (dropdown) {
    if (!dropdown.classList.contains("show")) {
      dropdown.classList.add("show");
    }
    document.addEventListener("click", closeDropdownSearch);
    document.addEventListener("keydown", searchOnKeyDown);
    document.addEventListener("keyup", searchOnKeyUp);
  }
}

//We want to prepare the index only after clicking the search bar
var idx = null
const docMap = new Map()

function prepareIdxAndDocMap() {
  const docs = [  
    {
      "title": "Home",
      "url": "/datadog4s/",
      "content": "Toolkit for monitoring applications written in functional Scala using Datadog. Goal of this project is to make great monitoring as easy as possible. In addition to basic monitoring utilities, we also provide bunch of plug-and-play modules that do monitoring for you. Currently, those are: JVM monitoring Http4s monitoring For the installation guide, please read our install guide. For the documentation, please read our user guide."
    } ,    
    {
      "title": "Install",
      "url": "/datadog4s/install.html",
      "content": "Installation To start monitoring your code, first you need to add this library as a dependency to your project. This project is composed of multiple packages to make it easy for you to pick and choose what you require. Available Packages: dependency name notes \"com.avast.cloud\" %% \"datadog4s\" % \"0.13.0\" all-you-can-eat … all the available packages \"com.avast.cloud\" %% \"datadog4s-api\" % \"0.13.0\" api classes \"com.avast.cloud\" %% \"datadog4s-statsd\" % \"0.13.0\" statsd implementation of api classes \"com.avast.cloud\" %% \"datadog4s-jvm\" % \"0.13.0\" support for monitoring JVM itself \"com.avast.cloud\" %% \"datadog4s-http4s\" % \"0.13.0\" monitoring support for [http4s][http4s] framework For the bare minimum, you need to add datadog4s-api which contains classes defining our API. You also need to add its implementation. Currently, we only support metric delivery using StatsD in package datadog4s which already contains api. We are going to assume you are using sbt. To explore how to use imported libraries, please read on Note on compatibility Datadog4s is currently released for following combinations of scala/cats versions: library 2.12 version 2.13 version 3.0.0-RC3 version cats-core 2.6.1 2.6.1 2.6.1 cats-effect 2.5.0 2.5.0 2.5.0 http4s 0.21.22 0.21.22 0.21.22"
    } ,        
    {
      "title": "User guide",
      "url": "/datadog4s/userguide.html",
      "content": "User guide Creating metric factory Creating metrics Timers Tagging Tagger Extensions Http4s Jvm monitoring Creating metric factory To start creating your metrics, first you need to create a MetricFactory[F[_]]. Currently, the only implementation is in statsd package. MetricFactory is purely functional, so it requires you to provide type constructor which implements cats.effect.Sync. For the simplicity, we will use cats.effect.IO in these examples. To create an instance, we need to provide it with configuration which contains a few basic fields, like address of the StatsD server, prefix etc. For more information see scaladoc of the config class. The instance is wrapped in Resource because of the underlying StatsD client. import java.net.InetSocketAddress import cats.effect._ import com.avast.datadog4s.api._ import com.avast.datadog4s.api.metric._ import com.avast.datadog4s._ val statsDServer = InetSocketAddress.createUnresolved(\"localhost\", 8125) val config = StatsDMetricFactoryConfig(Some(\"my-app-name\"), statsDServer) val factoryResource: Resource[IO, MetricFactory[IO]] = StatsDMetricFactory.make(config) Creating metrics Once you have a metrics factory, creating metrics is straight-forward. Note that all metric operations return side-effecting actions. factoryResource.use { factory =&gt; val count: Count[IO] = factory.count(\"hits\") val histogram: Histogram[IO, Long] = factory.histogram.long(\"my-histogram\") for { _ &lt;- count.inc() // increase count by one _ &lt;- histogram.record(1337, Tag.of(\"username\", \"xyz\")) // record a value to histogram with Tag } yield { () } } Timers In addition to basic datadog metrics, we provide a Timer[F] abstraction which has proved to be very useful is practice. Timers provide you with .time[A](fa: F[A]): F[A] method, which will measure how long it took to run provided fa. In addition, it tags the metric with success:true or success:false and exception:&lt;&lt;throwable class name&gt;&gt; in case the fa failed. In addition to .time[A] method it also allows for recording of raw values that represent elapsed time or even raw time data. You can see example of such calls below. Optionally, when creating a Timer, you can also set the time units which will be used for reporting. By default, all timers create with microsecond granularity. You can provide your own time unit if you need more or less precision. Histogram vs Distribution There are two versions of timers, one backed by Histogram and one backed by Distribution. You can read scaladoc for more details and links to datadog documentation. Long story short, histogram backed timers are aggregated per datadog agent, while the distributions are computed on datadog server. The implications are that distribution based timers, and it’s buckets (50th, 75th, 95th percentile etc) are more correct and in general it’s the implementation that we’d suggest to use. Example import java.util.concurrent.TimeUnit factoryResource.use { factory =&gt; val timer = factory.timer.distribution(\"request-latency\") timer.time(IO.delay(println(\"success\"))) // tagged with success:true timer.time(IO.raiseError(new NullPointerException(\"error\"))) // tagged with success:false and exception:NullPointerException val nanoTimer = factory.timer.distribution(\"nano-timer\", timeUnit = TimeUnit.NANOSECONDS) nanoTimer.time(IO.delay(println(\"success\"))) // metric will be recorded with 'nanoseconds' precision // timer.record works for all types that implement `ElapsedTime` typeclass, out of the box we provide implementation // for java.time.Duration and scala.concurrent.duration.FiniteDuration import java.time.Duration timer.record(Duration.ofNanos(1000)) import scala.concurrent.duration.FiniteDuration timer.record(FiniteDuration(1000, TimeUnit.MILLISECONDS)) timer.recordTime(1000L, TimeUnit.MILLISECONDS) } Tagging There are two ways to create a Tag instances. One way is using of method of Tag object, like so: import com.avast.datadog4s.api.Tag Tag.of(\"endpoint\", \"admin/login\") // res2: Tag = \"endpoint:admin/login\" This is simple and straight-forward, but in some cases it leaves your code with Tag keys scattered around and forces you to repeat it - making it prone to misspells etc. The better way is to use Tagger. Tagger Tagger[T] is basically a factory interface for creating tags based on provided value of type T - as long as implicit TagValue[T] exists in scope. This instance is used for converting T into String. By using Tagger, you get a single value that you can use in multiple places in your code to create Tags without repeating yourself. Example: import com.avast.datadog4s.api.tag.{TagValue, Tagger} val pathTagger: Tagger[String] = Tagger.make[String](\"path\") // pathTagger: Tagger[String] = com.avast.datadog4s.api.tag.Tagger$$anon$1@5778aee2 assert(Tag.of(\"path\", \"admin/login\") == pathTagger.tag(\"admin/login\")) // tagger also supports taging using custom types using TagValue typeclass case class StatusCode(value: Int) implicit val statusCodeTagValue: TagValue[StatusCode] = TagValue[Int].contramap[StatusCode](sc =&gt; sc.value) // statusCodeTagValue: TagValue[StatusCode] = com.avast.datadog4s.api.tag.TagValue$$anonfun$contramap$2@16f2fbb7 val statusCodeTagger: Tagger[StatusCode] = Tagger.make[StatusCode](\"statusCode\") // statusCodeTagger: Tagger[StatusCode] = com.avast.datadog4s.api.tag.Tagger$$anon$1@53d21d82 assert(Tag.of(\"statusCode\", \"200\") == statusCodeTagger.tag(StatusCode(200))) Extensions Extensions are packages that monitor some functionality for you - without you having to do much. Http4s Http4s package (datadog4s-http4s) provides implementation of MetricsOps that is used by http4s to report both client and server metrics. import com.avast.datadog4s.extension.http4s._ factoryResource.use { metricFactory =&gt; // create metrics factory and use it as you please DatadogMetricsOps.builder[IO](metricFactory).build().flatMap { metricOps =&gt; // setup http4s Metrics middleware here val _ = metricOps IO.unit } } Jvm monitoring JVM monitoring package (datadog4s-jvm) collects a bunch of JVM metrics that we found useful over last 5 or so years running JVM apps in Avast. Those metrics can be found in JvmReporter and are hopefully self-explanatory. We tried to match reported metrics to datadog JVM runtime metrics Usage can not be simpler (unless you want to configure things like collection-frequency etc.). Simply add following to your initialization code. Resource is returned, because a fiber is started in the background and has to be terminated eventually. import com.avast.datadog4s.extension.jvm._ import scala.concurrent.ExecutionContext implicit val ec = ExecutionContext.global // please don't use global EC in production implicit val contextShift = IO.contextShift(ec) implicit val timer = IO.timer(ec) val jvmMonitoring: Resource[IO, Unit] = factoryResource.flatMap { factory =&gt; JvmMonitoring.default[IO](factory) } jvmMonitoring.use { _ =&gt; // your application is in here IO.unit }"
    }    
  ];

  idx = lunr(function () {
    this.ref("title");
    this.field("content");

    docs.forEach(function (doc) {
      this.add(doc);
    }, this);
  });

  docs.forEach(function (doc) {
    docMap.set(doc.title, doc.url);
  });
}

// The onkeypress handler for search functionality
function searchOnKeyDown(e) {
  const keyCode = e.keyCode;
  const parent = e.target.parentElement;
  const isSearchBar = e.target.id === "search-bar";
  const isSearchResult = parent ? parent.id.startsWith("result-") : false;
  const isSearchBarOrResult = isSearchBar || isSearchResult;

  if (keyCode === 40 && isSearchBarOrResult) {
    // On 'down', try to navigate down the search results
    e.preventDefault();
    e.stopPropagation();
    selectDown(e);
  } else if (keyCode === 38 && isSearchBarOrResult) {
    // On 'up', try to navigate up the search results
    e.preventDefault();
    e.stopPropagation();
    selectUp(e);
  } else if (keyCode === 27 && isSearchBarOrResult) {
    // On 'ESC', close the search dropdown
    e.preventDefault();
    e.stopPropagation();
    closeDropdownSearch(e);
  }
}

// Search is only done on key-up so that the search terms are properly propagated
function searchOnKeyUp(e) {
  // Filter out up, down, esc keys
  const keyCode = e.keyCode;
  const cannotBe = [40, 38, 27];
  const isSearchBar = e.target.id === "search-bar";
  const keyIsNotWrong = !cannotBe.includes(keyCode);
  if (isSearchBar && keyIsNotWrong) {
    // Try to run a search
    runSearch(e);
  }
}

// Move the cursor up the search list
function selectUp(e) {
  if (e.target.parentElement.id.startsWith("result-")) {
    const index = parseInt(e.target.parentElement.id.substring(7));
    if (!isNaN(index) && (index > 0)) {
      const nextIndexStr = "result-" + (index - 1);
      const querySel = "li[id$='" + nextIndexStr + "'";
      const nextResult = document.querySelector(querySel);
      if (nextResult) {
        nextResult.firstChild.focus();
      }
    }
  }
}

// Move the cursor down the search list
function selectDown(e) {
  if (e.target.id === "search-bar") {
    const firstResult = document.querySelector("li[id$='result-0']");
    if (firstResult) {
      firstResult.firstChild.focus();
    }
  } else if (e.target.parentElement.id.startsWith("result-")) {
    const index = parseInt(e.target.parentElement.id.substring(7));
    if (!isNaN(index)) {
      const nextIndexStr = "result-" + (index + 1);
      const querySel = "li[id$='" + nextIndexStr + "'";
      const nextResult = document.querySelector(querySel);
      if (nextResult) {
        nextResult.firstChild.focus();
      }
    }
  }
}

// Search for whatever the user has typed so far
function runSearch(e) {
  if (e.target.value === "") {
    // On empty string, remove all search results
    // Otherwise this may show all results as everything is a "match"
    applySearchResults([]);
  } else {
    const tokens = e.target.value.split(" ");
    const moddedTokens = tokens.map(function (token) {
      // "*" + token + "*"
      return token;
    })
    const searchTerm = moddedTokens.join(" ");
    const searchResults = idx.search(searchTerm);
    const mapResults = searchResults.map(function (result) {
      const resultUrl = docMap.get(result.ref);
      return { name: result.ref, url: resultUrl };
    })

    applySearchResults(mapResults);
  }

}

// After a search, modify the search dropdown to contain the search results
function applySearchResults(results) {
  const dropdown = document.querySelector("div[id$='search-dropdown'] > .dropdown-content.show");
  if (dropdown) {
    //Remove each child
    while (dropdown.firstChild) {
      dropdown.removeChild(dropdown.firstChild);
    }

    //Add each result as an element in the list
    results.forEach(function (result, i) {
      const elem = document.createElement("li");
      elem.setAttribute("class", "dropdown-item");
      elem.setAttribute("id", "result-" + i);

      const elemLink = document.createElement("a");
      elemLink.setAttribute("title", result.name);
      elemLink.setAttribute("href", result.url);
      elemLink.setAttribute("class", "dropdown-item-link");

      const elemLinkText = document.createElement("span");
      elemLinkText.setAttribute("class", "dropdown-item-link-text");
      elemLinkText.innerHTML = result.name;

      elemLink.appendChild(elemLinkText);
      elem.appendChild(elemLink);
      dropdown.appendChild(elem);
    });
  }
}

// Close the dropdown if the user clicks (only) outside of it
function closeDropdownSearch(e) {
  // Check if where we're clicking is the search dropdown
  if (e.target.id !== "search-bar") {
    const dropdown = document.querySelector("div[id$='search-dropdown'] > .dropdown-content.show");
    if (dropdown) {
      dropdown.classList.remove("show");
      document.documentElement.removeEventListener("click", closeDropdownSearch);
    }
  }
}
