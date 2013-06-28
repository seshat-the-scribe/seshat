package seshat.processor

import akka.actor.{ActorLogging, Cancellable, ActorRef, Actor}
import concurrent.duration._
import scala.concurrent.ExecutionContext

/** Encapsulates Asking for events behaviour
 * Created by f on 6/22/13.
 */
trait AskAgainProtocol extends Actor with ActorLogging  {

  protected val maxRetries = 500

  private var retries = 0

  // FIXME configure a dispatcher
  implicit val exCtx: ExecutionContext

  private lazy val rnd = new java.util.Random
  rnd.setSeed(new java.util.Date().getTime)

  private[this] var timer: Cancellable = _

  /** Reset the retries counter.
    *
    * Calling this resets the counter so asking will be fast again.
    *
    */
  protected def resetRetries() {
    retries = 0
    if( timer != null && !timer.isCancelled){
      timer.cancel()
    }
  }

  /** Schedule delivery of a `what` to `who`.
    *
    * if retries <= maxRetries then rnd(maxRetries/2)+maxRetries
    * if retries >  maxRetries then rnd(ceil(retries/2)+1)
    *
    * @param who a ref to the target actor
    * @param what the message
    *
    */
  protected def scheduleAsk(who: ActorRef, what: Any) {

    if( retries <= maxRetries ) retries = retries + 1

    log.debug(s"Retries: $retries  ")

    if( timer != null && !timer.isCancelled ){
      timer.cancel()
    }

    val wait =
      if (retries > maxRetries)
        rnd.nextInt(maxRetries/4)+maxRetries
      else
        rnd.nextInt(retries*2)+retries

    log.debug(s"Wait time: $wait")

    timer =
      context.system.scheduler
        .scheduleOnce(wait millis, who, what)

  }

}
