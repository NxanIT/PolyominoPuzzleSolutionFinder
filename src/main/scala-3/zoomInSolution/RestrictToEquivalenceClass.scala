package zoomInSolution

import zoomInSolution.ReadFiles.{load_data, load_nodes}

import java.io.*

class RestrictToEquivalenceClass(path: String) {
  private val in_path = path + "\\Solutions\\"
  private val out_path = path + "\\GraphData\\"

  def restrictToEquivalenceClass(layer_prefix: String,
                                 layer_suffix: String,
                                 search_for: Int,
                                 flag_search_for_row: Boolean = false): Int = {

    //1. read in all solution-ids
    val layer0size = 0
    val solution_ids_path = in_path + "_solution_ids.txt"
    val solution_ids_data = load_data(solution_ids_path, layer0size)

    //2. read in last created equivalence classes file
    val size = load_nodes(in_path + layer_prefix + "_nodes.csv").length
    val eq_file_path = out_path + layer_prefix + s"_size${size}" + layer_suffix + "_EQ.csv"

    //3. find the equivalence class of the element
    val nodes = get_selected_nodes(eq_file_path, search_for, flag_search_for_row)
    println(s"number of elements in equivalence class of $search_for is ${nodes.length}.")

    //4. write the elements of the equivalence - and - the corresponding solution-ids to files
    val write_filename_initial_segment = layer_prefix match {
      case "" => in_path
      case _ => in_path + layer_prefix + "-"
    }
    write_solution_id_file(solution_ids_data, nodes, write_filename_initial_segment)
    write_nodes_file(nodes, write_filename_initial_segment)

    //5. return representative of equivalence class
    nodes(0)
  }

  private def get_selected_nodes(str: String, i: Int, flag_search_row: Boolean): Array[Int] = {
    if (flag_search_row) {
      get_selected_nodes_row_mode(str, i)
    } else {
      get_selected_nodes_nodeId_mode(str, i)
    }
  }

  private def get_selected_nodes_nodeId_mode(eq_file_name: String, search: Int): Array[Int] = {
    println(eq_file_name)
    val eq_file = new File(eq_file_name)
    val eq_BR = new BufferedReader(new FileReader(eq_file))
    eq_BR.readLine() //get rid of table header
    var line = eq_BR.readLine()
    var eq_array = line.substring(line.indexOf(";") + 1).split(";").map(x => x.toInt)
    while (! eq_array.contains(search)) {
      line = eq_BR.readLine()
      eq_array = line.substring(line.indexOf(";") + 1).split(";").map(x => x.toInt)
    }
    eq_BR.close()
    eq_array
  }

  private def get_selected_nodes_row_mode(eq_file_name: String, search: Int): Array[Int] = {
    val eq_file = new File(eq_file_name)
    val eq_BR = new BufferedReader(new FileReader(eq_file))
    Array.range(0, search).map(_ => eq_BR.readLine())
    val line = eq_BR.readLine()
    eq_BR.close()

    val line_without_size = line.substring(line.indexOf(";") + 1)
    line_without_size.split(";").map(x => x.toInt)
  }

  private def get_new_sol_ids(all_data: Vector[Array[String]], nodes: Array[Int]): Unit = {
    all_data.zipWithIndex.filter((_, i) => nodes.contains(i))
  }

  private def write_solution_id_file(all_data: Vector[Array[String]],
                                     eq_class_array: Array[Int],
                                     file_name: String): Unit = {
    val repr = eq_class_array(0)
    val Output_file_solution_ids = new File(file_name + s"${repr}_solution_ids.txt")
    val Output_BW_solution_ids = new BufferedWriter(new FileWriter(Output_file_solution_ids))

    for i <- eq_class_array
      do {
        Output_BW_solution_ids.write(all_data(i).mkString("", "-", "-\n"))
      }
    Output_BW_solution_ids.close()
  }

  private def write_nodes_file(eq_class_array: Array[Int], file_name: String): Unit = {
    val repr = eq_class_array(0)
    val Output_file_nodes = new File(file_name + s"${repr}_nodes.csv")
    val Output_BW_nodes = new BufferedWriter(new FileWriter(Output_file_nodes))
    Output_BW_nodes.write(eq_class_array.mkString("", ";", "\n"))
    Output_BW_nodes.close()
  }
}
