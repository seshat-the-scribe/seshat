package seshat.processor

import akka.actor.{ActorRef, Actor}
import concurrent.duration._
import akka.dispatch.MessageDispatcher
import scala.concurrent.ExecutionContext

/** Encapsulates Asking for event behaviour
 * Created by f on 6/22/13.
 */
trait AskAgainProtocol extends Actor  {

  private var retries = 0

  // FIXME configure a dispatcher
  implicit val exCtx: ExecutionContext

  private lazy val rnd = new java.util.Random
  rnd.setSeed(new java.util.Date().getTime)

  protected def askAgainHandler: Receive = {
    case Processor.Common.AskAgain(who, what) => who ! what
  }

  protected def resetRetries() { retries = 0 }

  /** Schedule delivery of a `Msg.AskAgain` to `self`.
    *
    * if retries <= 10 then rnd(retries*10)+100
    * if retries >  10 then rnd(100)+100
    *
    * @param who the ref to the target actor
    * @param what the message
    *
    */
  protected def scheduleAsk(who: ActorRef, what: Any) {

    retries = retries + 1

    val wait =
      if (retries > 10)
        rnd.nextInt(100)+100
      else
        rnd.nextInt(retries*10)+100

    context.system.scheduler
      .scheduleOnce(
        wait millis,
        self,
        Processor.Common.AskAgain(who, what)
      )

  }


}
