import cats.{Eval, MonadError}
import cats.effect.Sync
import io.micrometer.core.instrument.{MeterRegistry, Tags}
import io.micrometer.core.instrument.simple.SimpleMeterRegistry

class Main {

  import com.timgroup.statsd.NonBlockingStatsDClient
cli.recordExecutionTime()
  val cli = new NonBlockingStatsDClient(
    "my.prefix",
    "statsd-host",
    8125,
    Array[String]("tag:value"): _*
  )
  
  
  Sync[Eval]
}
