package zoomInSolution

import zoomInSolution.project.{number_of_solutions, path}

class ZoomInSolution(ProjectPath: String,
                     ZoomIn: String,
                     RestrictTo: String,
                     initialModuloLeq: Int,
                     initialConnectedLeq: Int,
                     decreaseByPerLayer: Int,
                     restrictionLeq: Int) extends Thread{
  
  override def run(): Unit = {
    
  }

  val forth = CreateGraph(ProjectPath)
  val back = RestrictToEquivalenceClass(ProjectPath)
  val zoom_in_solution = 0

  def zoom_in(): Unit = {
    var layerPrefix = ""
    var layerSuffix = ""
    for modLeq <- initialModuloLeq to 1 by -restrictionLeq
      do {
        forth.createGraph(layerPrefix, modLeq)
        layerSuffix = s"_leq${modLeq + 1}-${modLeq}"
        val new_eq_class = back.restrictToEquivalenceClass(layerPrefix, layerSuffix, zoom_in_solution)
        layerPrefix = layerPrefix match {
          case "" => new_eq_class.toString
          case _ => layerPrefix + "-" + new_eq_class.toString
        }
      }
  }
}
