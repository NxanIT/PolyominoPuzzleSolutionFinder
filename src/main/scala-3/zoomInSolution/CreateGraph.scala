package zoomInSolution

import zoomInSolution.ReadFiles.{load_data, load_nodes}

import java.io.{BufferedWriter, File, FileWriter}
import java.lang.System
import scala.collection.mutable
//import cats.collections.DisjointSets

class CreateGraph (path: String){
  private val in_path = path + "\\Solutions\\"
  private val out_path = path + "\\GraphData\\"
  
  def createGraph(layer_prefix: String, modleq: Int): Unit = {
    
    val nodes = load_nodes(in_path + layer_prefix + "_nodes.csv")
    val size = nodes.length

    var time_stamp = System.currentTimeMillis()

    //1. load solution Id's
    val sol_str = load_data(in_path + layer_prefix + "_solution_ids.txt", size)
    time_stamp = print_timestamp(time_stamp, "load data")(0)

    //2. create eq_classes and edges table between single solutions
    val (eq_classes, edges_without_eq) = get_eq_classes(sol_str, nodes, modleq)
    time_stamp = print_timestamp(time_stamp, "get eq-classes and conn comp.")(0)

    //3. create edge table between eq_classes
    val representants_of_elements = eq_classes.map(e => e.min(using Ordering[Int]))
    val edges = get_edges_via_vector(edges_without_eq, representants_of_elements)
    time_stamp = print_timestamp(time_stamp, "get edges table")(0)

    //4. write files
    val file_name_start = out_path + layer_prefix + s"_size${size}_leq${modleq + 1}-${modleq}"
    write_edges_table_to_file(edges, file_name_start)
    write_eq_classes_and_nodes_table_to_file(eq_classes, representants_of_elements, file_name_start)
    print_timestamp(time_stamp, "writing files")
  }

  private def get_eq_classes(solutions_string: Vector[Array[String]],
                             nodes: Array[Int],
                             max_solution_difference: Int = 4): (Vector[Vector[Int]], Vector[(Int, Int)]) = {

    val u = DisjointUnion.UnionFind[Int](nodes)
    val joinLater: mutable.ArrayBuffer[(Int, Int)] = mutable.ArrayBuffer()
    for
      i <- solutions_string.indices
      j <- i + 1 until solutions_string.size

      diff = solutions_string(i).zip(solutions_string(j)).count((a, b) => a != b)

    do {
      if diff <= max_solution_difference then u.union(nodes(i), nodes(j))
      if diff == max_solution_difference + 1 then joinLater.addOne((i, j))
    }
    val eq_classes = u.get_eq_classes()
    (eq_classes, joinLater.toVector)
  }

  private def get_edges_via_vector(edges_without_eq: Vector[(Int, Int)],
                                   eq_classes_vector: Vector[Int]): Vector[(Int, Int)] = {
    val edges: mutable.HashSet[(Int, Int)] = mutable.HashSet()
    for (eq_i, eq_j) <- edges_without_eq.map((i, j) => (eq_classes_vector(i), eq_classes_vector(j)))
      do {
        if eq_i <= eq_j then edges.addOne((eq_i, eq_j))
        else edges.addOne((eq_j, eq_i))
      }
    val eq_classes = eq_classes_vector.toSet
    println(s"number of eq-classes: ${eq_classes.size}")
    for eq_i <- eq_classes
      do edges.addOne((eq_i, eq_i))
    edges.toVector
  }

  private def write_edges_table_to_file(A: Vector[(Int, Int)], file_name: String): Unit = {
    val Output_file = new File(file_name + "_ET.csv")
    val Output_BW = new BufferedWriter(new FileWriter(Output_file))
    Output_BW.write("Source;Target\n")
    for line <- A
      do {
        val line_string = line(0).toString + ";" + line(1).toString

        Output_BW.write(line_string + "\n")
      }
    Output_BW.close()
  }

  private def write_eq_classes_and_nodes_table_to_file(eq_classes_vector: Vector[Vector[Int]],
                                                       representants_of_elements: Vector[Int],
                                                       file_names: String): Unit = {
    val Output_file_EQ = new File(file_names + "_EQ.csv")
    val Output_BW_EQ = new BufferedWriter(new FileWriter(Output_file_EQ))
    Output_BW_EQ.write("#Elem;Elements\n")

    val Output_file_NW = new File(file_names + "_NT.csv")
    val Output_BW_NW = new BufferedWriter(new FileWriter(Output_file_NW))
    Output_BW_NW.write("Id;weight\n")

    val indices_sorted = representants_of_elements.zipWithIndex.
                          distinctBy((x, _) => x).sortBy((x, _) => x).map((_, i) => i)
    for i <- indices_sorted
      do {
        val line = eq_classes_vector(i).sortBy(x => x)
        val linesize = line.size
        val linestr = line.toString()
        val opening_bracket = linestr.indexOf("(")
        val closing_bracket = linestr.lastIndexOf(")")

        Output_BW_EQ.write(linesize.toString + ";" +
                            linestr.substring(opening_bracket + 1, closing_bracket).replace(", ", ";") + "\n")
        Output_BW_NW.write(s"${representants_of_elements(i)};${linesize}\n")
      }
    Output_BW_EQ.close()
    Output_BW_NW.close()
  }

  private def print_timestamp(time_start: Long, text: String = ""): (Long, Long) = {
    val time_now = System.currentTimeMillis()
    val time_diff = time_now - time_start
    println(s"time-it: $text took ${time_diff}ms.")
    (time_now, time_diff)
  }
}
