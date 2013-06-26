package seshat.processor

import akka.actor.{ActorRef, Actor}
import concurrent.duration._
import akka.dispatch.MessageDispatcher
import scala.concurrent.ExecutionContext

/** Encapsulates Asking for events behaviour
 * Created by f on 6/22/13.
 */
trait AskAgainProtocol extends Actor  {

  private var retries = 0

  // FIXME configure a dispatcher
  implicit val exCtx: ExecutionContext

  private lazy val rnd = new java.util.Random
  rnd.setSeed(new java.util.Date().getTime)


  protected def resetRetries() { retries = 0 }

  /** Schedule delivery of a `Msg.AskAgain` to `who`.
    *
    * if retries <= 10 then rnd(retries*100)+100
    * if retries >  10 then rnd(100)+1000
    *
    * @param who the ref to the target actor
    * @param what the message
    *
    */
  protected def reScheduleAsk(who: ActorRef, what: Any) {

    retries = retries + 1

    val wait =
      if (retries > 1000)
        rnd.nextInt(100)+1000
      else
        rnd.nextInt(retries*100)+100

    context.system.scheduler
      .scheduleOnce(wait millis, who, what)

  }


}
