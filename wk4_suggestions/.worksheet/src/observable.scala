import rx.lang.scala._
import rx.lang.scala.subjects._
import scala.concurrent.duration._

object observable {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(164); 
  val xs: Observable[Int] = Observable(3,2,1).take(2);System.out.println("""xs  : rx.lang.scala.Observable[Int] = """ + $show(xs ));$skip(54); 
  val ys: Observable[Int] = Observable(4,5,6).take(2);System.out.println("""ys  : rx.lang.scala.Observable[Int] = """ + $show(ys ));$skip(24); 
  val zs = xs.merge(ys);System.out.println("""zs  : rx.lang.scala.Observable[Int] = """ + $show(zs ));$skip(40); 
  val s = zs.subscribe(b => println(b));System.out.println("""s  : rx.lang.scala.Subscription = """ + $show(s ));$skip(18); 
  s.unsubscribe();$skip(119); 
  
//val channel = PublishSubject[Int](5)
//val channel = ReplaySubject[Int]()
  val channel = BehaviorSubject[Int](5);System.out.println("""channel  : rx.lang.scala.subjects.BehaviorSubject[Int] = """ + $show(channel ));$skip(90); 
//val channel = AsyncSubject[Int]()

  val a = channel.subscribe(x => println("a: " + x));System.out.println("""a  : rx.lang.scala.Subscription = """ + $show(a ));$skip(53); 
  val b = channel.subscribe(x => println("b: " + x));System.out.println("""b  : rx.lang.scala.Subscription = """ + $show(b ));$skip(21); 
  channel.onNext(42);$skip(18); 
  a.unsubscribe();$skip(23); 
  channel.onNext(4711);$skip(24); 
  channel.onCompleted();$skip(53); 
  val c = channel.subscribe(x => println("c: " + x));System.out.println("""c  : rx.lang.scala.Subscription = """ + $show(c ));$skip(21); 
  channel.onNext(13)}
}
