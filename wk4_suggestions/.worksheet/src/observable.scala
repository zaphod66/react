import rx.lang.scala.Observable
import scala.concurrent.duration._

object observable {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(141); 
  val xs: Observable[Int] = Observable(3,2,1).take(2);System.out.println("""xs  : rx.lang.scala.Observable[Int] = """ + $show(xs ));$skip(54); 
  val ys: Observable[Int] = Observable(4,5,6).take(3);System.out.println("""ys  : rx.lang.scala.Observable[Int] = """ + $show(ys ));$skip(24); 
  val zs = xs.merge(ys);System.out.println("""zs  : rx.lang.scala.Observable[Int] = """ + $show(zs ));$skip(40); 
  val s = zs.subscribe(b => println(b));System.out.println("""s  : rx.lang.scala.Subscription = """ + $show(s ));$skip(18); 
  s.unsubscribe();$skip(48); 
  
  val ticks = Observable.interval(1 seconds);System.out.println("""ticks  : rx.lang.scala.Observable[Long] = """ + $show(ticks ));$skip(37); 
  val evens = ticks.filter(s⇒s%2==0);System.out.println("""evens  : rx.lang.scala.Observable[Long] = """ + $show(evens ));$skip(54); 
  val bufs: Observable[Seq[Long]] = ticks.buffer(2,1);System.out.println("""bufs  : rx.lang.scala.Observable[Seq[Long]] = """ + $show(bufs ));$skip(39); 
  val t = bufs.subscribe(b⇒println(b));System.out.println("""t  : rx.lang.scala.Subscription = """ + $show(t ));$skip(18); 
  t.unsubscribe()}
}
