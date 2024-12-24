package modules

import com.google.inject.AbstractModule
import play.api.libs.concurrent.PekkoGuiceSupport
import services.PipelineScheduler

class EagerLoaderModule extends AbstractModule with PekkoGuiceSupport {
  override def configure(): Unit = {
    bind(classOf[PipelineScheduler]).asEagerSingleton()
  }
}
