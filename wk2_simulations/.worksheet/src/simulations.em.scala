package simulations

object em {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(64); 
  val em = new EpidemySimulator;System.out.println("""em  : simulations.EpidemySimulator = """ + $show(em ))}
}
