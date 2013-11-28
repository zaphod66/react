import rx.lang.scala.Observable
import scala.concurrent.duration._

object observable {
  val xs: Observable[Int] = Observable(3,2,1).take(2)
                                                  //> xs  : rx.lang.scala.Observable[Int] = rx.lang.scala.Observable$$anon$9@62bc1
                                                  //| e7e
  val ys: Observable[Int] = Observable(4,5,6).take(3)
                                                  //> ys  : rx.lang.scala.Observable[Int] = rx.lang.scala.Observable$$anon$9@38f3c
                                                  //| ab6
  val zs = xs.merge(ys)                           //> zs  : rx.lang.scala.Observable[Int] = rx.lang.scala.Observable$$anon$9@86b5f
                                                  //| 7b
  val s = zs.subscribe(b => println(b))           //> 3
                                                  //| 2
                                                  //| 4
                                                  //| 5
                                                  //| 6
                                                  //| s  : rx.lang.scala.Subscription = rx.lang.scala.subscriptions.Subscription$$
                                                  //| anon$1@44770739
  s.unsubscribe()
  
  val ticks = Observable.interval(1 seconds)      //> ticks  : rx.lang.scala.Observable[Long] = rx.lang.scala.Observable$$anon$9@5
                                                  //| 9b30ac0
  val evens = ticks.filter(s⇒s%2==0)              //> evens  : rx.lang.scala.Observable[Long] = rx.lang.scala.Observable$$anon$9@6
                                                  //| 765acfc
  val bufs: Observable[Seq[Long]] = ticks.buffer(2,1)
                                                  //> bufs  : rx.lang.scala.Observable[Seq[Long]] = rx.lang.scala.Observable$$anon
                                                  //| $9@2f86c83b
  val t = bufs.subscribe(b⇒println(b))            //> t  : rx.lang.scala.Subscription = rx.lang.scala.subscriptions.Subscription$$
                                                  //| anon$1@29dbf9c3
  t.unsubscribe()
}