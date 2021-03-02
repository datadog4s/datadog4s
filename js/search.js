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
      "content": "Toolkit for monitoring applications written in functional Scala using Datadog. Goal of this project is to make great monitoring as easy as possible. In addition to basic monitoring utilities, we also provide bunch of p¡¡lug-and-play modules that do monitoring for you. Currently those are: JVM monitoring Http4s monitoring Quick start Latest version: To add all packages, add to build.sbt: libraryDependencies += \"com.avast.cloud\" %% \"datadog4s\" % \"0.11.1\" Or pick and choose from the available packages: dependency name notes \"com.avast.cloud\" %% \"datadog4s\" % \"0.11.1\" all-you-can-eat … all the available packages \"com.avast.cloud\" %% \"datadog4s-api\" % \"0.11.1\" api classes \"com.avast.cloud\" %% \"datadog4s-statsd\" % \"0.11.1\" statsd implementation of api classes \"com.avast.cloud\" %% \"datadog4s-jvm\" % \"0.11.1\" support for monitoring JVM itself \"com.avast.cloud\" %% \"datadog4s-http4s\" % \"0.11.1\" monitoring support for [http4s][http4s] framework Comaptibility Datadog4s is currently released for both scala 2.12 and scala 2.13. It is using following versions of libraries: library 2.12 version 2.13 version 3.0.0-M2 version cats-core 2.4.2 2.4.2 2.4.2 cats-effect 2.3.3 2.3.3 2.3.3 http4s 0.21.20 0.21.20 0.21.20 User guide For documentation, please read our user guide."
    } ,        
    {
      "title": "User guide",
      "url": "/datadog4s/userguide.html",
      "content": "User guide User guide Quick start Installation Creating metric factory Creating metrics Timers Tagging Tagger Extensions Http4s Jvm monitoring Quick start Installation To start monitoring your code, first you need to add this library as a dependency to your project. This project is composed of multiple packages to make it easy for you to pick and choose what you require. You need to add datadog4s-api which contains classes defining our API. You also need to add it’s implementation. Currently we only support metric delivery using StatsD in package datadog4s which already contains api. We are going to assume you are using sbt: libraryDependencies += \"com.avast.cloud\" %% \"datadog4s-api\" % \"0.11.1\" Creating metric factory To start creating your metrics, first you need to create a MetricFactory[F[_]]. Currently the only implementation is in statsd package. MetricFactory is purely functional so it requires you to provide type constructor which implements cats.effect.Sync. For the simplicity, we will use cats.effect.IO in these examples. To create an instance, we need to provide it with configuration which contains a few basic fields, like address of the StatsD server, prefix etc. For more information see scaladoc of the config class. The instance is wrapped in Resource because of the underlying StatsD client. import java.net.InetSocketAddress import cats.effect._ import com.avast.datadog4s.api._ import com.avast.datadog4s.api.metric._ import com.avast.datadog4s._ val statsDServer = InetSocketAddress.createUnresolved(\"localhost\", 8125) val config = StatsDMetricFactoryConfig(Some(\"my-app-name\"), statsDServer) val factoryResource: Resource[IO, MetricFactory[IO]] = StatsDMetricFactory.make(config) Creating metrics Once you have a metrics factory, creating metrics is straight forward. factoryResource.use { factory =&gt; val count: Count[IO] = factory.count(\"hits\") count.inc() // increase count by one val histogram: Histogram[IO, Long] = factory.histogram.long(\"my-histogram\") histogram.record(1337, Tag.of(\"username\", \"xyz\")) // record a value to histogram with Tag } Timers Timers are great. And with our API, they are even better. Because we are living in functional code, we expect you to provide us with F[_]: Sync and we will time how long execution takes, and tag it with whether it succeeded (and if it failed, which class of exception was thrown). factoryResource.use { factory =&gt; val timer = factory.timer(\"request-latency\") timer.time(IO.pure(println(\"success\"))) // tagged as success timer.time(IO.raiseError(new Exception(\"error\"))) //tagged as failure } Tagging There are two ways to create a Tag instances. One way is using of method of Tag object, like so: import com.avast.datadog4s.api.Tag Tag.of(\"endpoint\", \"admin/login\") // res2: Tag = \"endpoint:admin/login\" This is simple and straight-forward, but in some cases it leaves your code with Tag keys scattered around in your code and forces you to repeat it - making it prone to misspells etc. The better way is to use Tagger. Tagger Tagger[T] is basically a factory interface for creating tags based on provided value of type T - as long as implicit TagValue[T] exists in scope. This instance is used for converting T into String. By using Tagger, you get a single value that you can use in multiple places in your code to create Tags without repeating yourself. Example: import com.avast.datadog4s.api.tag.{TagValue, Tagger} case class StatusCode(value: Int) implicit val statusCodeTagValue: TagValue[StatusCode] = TagValue[Int].contramap[StatusCode](sc =&gt; sc.value) // statusCodeTagValue: TagValue[StatusCode] = com.avast.datadog4s.api.tag.TagValue$$anonfun$contramap$2@4d6b4a7 val pathTagger: Tagger[String] = Tagger.make[String](\"path\") // pathTagger: Tagger[String] = com.avast.datadog4s.api.tag.Tagger$$anon$1@28b51f5e val statusCodeTagger: Tagger[StatusCode] = Tagger.make[StatusCode](\"statusCode\") // statusCodeTagger: Tagger[StatusCode] = com.avast.datadog4s.api.tag.Tagger$$anon$1@6a1a057d assert(Tag.of(\"path\", \"admin/login\") == pathTagger.tag(\"admin/login\")) assert(Tag.of(\"statusCode\", \"200\") == statusCodeTagger.tag(StatusCode(200))) Extensions Extensions are packages that monitor some functionality for you - without you having to do anything. Http4s Http4s package (datadog4s-http4s) provides implementation of MetricsOps that is used by http4s to report both client and server metrics. import com.avast.datadog4s.extension.http4s._ factoryResource.use { metricFactory =&gt; val _ = DatadogMetricsOps.make[IO](metricFactory) // create metrics factory and use it as you please IO.pure(()) } Jvm monitoring JVM monitoring package (datadog4s-jvm) collects bunch of JVM metrics that we found useful over last 5 or so years running JVM apps in Avast. Those metrics can be found in JvmReporter and are hopefully self explenatory. Usage can not be simpler (unless you want to configure things like collection-frequency etc.). Simply add following to your initialization code. Resource is returned, because Scheduler has to be created which does the actual metric collection. import com.avast.datadog4s.extension.jvm._ import scala.concurrent.ExecutionContext implicit val ec = ExecutionContext.global // please don't use global EC in production implicit val contextShift = IO.contextShift(ec) implicit val timer = IO.timer(ec) val jvmMonitoring: Resource[IO, Unit] = factoryResource.flatMap(factory =&gt; JvmMonitoring.default[IO](factory)) jvmMonitoring.use { _ =&gt; // your application is in here IO.pure(()) }"
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
