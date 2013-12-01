package suggestions

import rx.lang.scala._
import rx.lang.scala.subjects._
import scala.concurrent.duration._

object observable {
  val xs: Observable[Int] = Observable(3,2,1).take(2)
                                                  //> xs  : rx.lang.scala.Observable[Int] = rx.lang.scala.Observable$$anon$9@4b52f
                                                  //| 4ce
  val ys: Observable[Int] = Observable(4,5,6).take(2)
                                                  //> ys  : rx.lang.scala.Observable[Int] = rx.lang.scala.Observable$$anon$9@53011
                                                  //| 788
  val zs = xs.merge(ys)                           //> zs  : rx.lang.scala.Observable[Int] = rx.lang.scala.Observable$$anon$9@18a9b
                                                  //| 68c
  val s = zs.subscribe(b => println(b))           //> 3
                                                  //| 2
                                                  //| 4
                                                  //| 5
                                                  //| s  : rx.lang.scala.Subscription = rx.lang.scala.subscriptions.Subscription$$
                                                  //| anon$1@aaf8eaa
  s.unsubscribe()
  
  
//val channel = PublishSubject[Int](5)
//val channel = ReplaySubject[Int]()
  val channel = BehaviorSubject[Int](5)           //> channel  : rx.lang.scala.subjects.BehaviorSubject[Int] = rx.lang.scala.subje
                                                  //| cts.BehaviorSubject@122507c2
//val channel = AsyncSubject[Int]()

  val a = channel.subscribe(x => println("a: " + x))
                                                  //> a: 5
                                                  //| a  : rx.lang.scala.Subscription = rx.lang.scala.subscriptions.Subscription$$
                                                  //| anon$1@76dedd5c
  val b = channel.subscribe(x => println("b: " + x))
                                                  //> b: 5
                                                  //| b  : rx.lang.scala.Subscription = rx.lang.scala.subscriptions.Subscription$$
                                                  //| anon$1@381bfd7
  channel.onNext(42)                              //> b: 42
                                                  //| a: 42
  a.unsubscribe()
  channel.onNext(4711)                            //> b: 4711
  channel.onCompleted()
  val c = channel.subscribe(x => println("c: " + x))
                                                  //> c: 4711
                                                  //| c  : rx.lang.scala.Subscription = rx.lang.scala.subscriptions.Subscription$$
                                                  //| anon$1@2e64e02
  channel.onNext(13)                              //> c: 13
}