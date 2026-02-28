package zoomInSolution

import java.io.*

object ReadFiles {
  def print_timestamp(time_start: Long, text: String = ""): (Long, Long) = {
    val time_now = System.currentTimeMillis()
    val time_diff = time_now - time_start
    println(s"time-it: $text took ${time_diff}ms.")
    (time_now, time_diff)
  }

  /** reads [layer-prefix]_nodes.csv - files
   * @return nodes: Array[Int] - corresponding to the solution_ids (with respect to layer0) occurring in this layer
   *
   */
  def load_nodes(file_name: String): Array[Int] = {
    val solution_id_file = new File(file_name)
    val solution_id_BR = new BufferedReader(new FileReader(solution_id_file))
    val node_data = solution_id_BR.readLine().split(";").map(x => x.toInt)
    solution_id_BR.close()
    node_data
  }

  /** reads [layer-prefix]_solution_ids.txt - files
   *
   * @return SolutionIDs: Vector[Array[String]] - corresponding to the solution_ids (with respect to layer0) occurring in this layer
   *
   */
  def load_data(file_name: String, size: Int): Vector[Array[String]] = {
    val solution_id_file = new File(file_name)
    val solution_id_BR = new BufferedReader(new FileReader(solution_id_file))
    val data = Vector.fill(size)(solution_id_BR.readLine().split("-"))
    solution_id_BR.close()
    data
  }
}
