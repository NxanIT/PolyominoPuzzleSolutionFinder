package zoomInSolution

//> using scala 3.3.7
object project extends App {

  val zoom_in_solution = 260//276//42
  val starting_modulo_leq = 2

  val enforce_load_nodes = true
  val path = "C:\\Informatik\\Polyomino\\" +zoom_in_solution +"\\"
  //val path = "C:\\Informatik\\Polyomino\\Laura\\"//"C:\\Informatik\\Polyomino\\ZoomIn\\"
  val number_of_solutions = 442//100//32288 //number of entries in _solution_ids.txt
  val forth = CreateGraph(path)
  val back = RestrictToEquivalenceClass(path)


  zoom_in()

  def zoom_in(): Unit = {
    var layerPrefix = ""
    var layerSuffix = ""
    for modLeq <- starting_modulo_leq to 1 by -1
    do {
      forth.createGraph(layerPrefix,modLeq)
      layerSuffix = s"_leq${modLeq+1}-${modLeq}"
      val new_eq_class = back.restrictToEquivalenceClass(layerPrefix,layerSuffix,zoom_in_solution)
      layerPrefix = layerPrefix match {
        case "" => new_eq_class.toString
        case _ => layerPrefix + "-" + new_eq_class.toString
      }
    }


  }
}