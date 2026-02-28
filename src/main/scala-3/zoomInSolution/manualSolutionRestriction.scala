package zoomInSolution

import java.io.*


object manualSolutionRestriction extends App{
  val path = "C:\\Informatik\\Polyomino\\create-cat\\"
  val parent_path = "C:\\Informatik\\Polyomino"
  val suff_in_for_restr = "restrict"
  val suff_in = "In"
  val suff_out = "Out"
  val raw_data_size = 32288
  val search_for_id = 17701//14415

  //val vector_raw = load_raw_data(path + suff_in_for_restr,raw_data_size)

  val restrictor = RestrictToEquivalenceClass(path)
  val repr = restrictor.restrictToEquivalenceClass("","",search_for_id)
  println(repr)

  def load_raw_data(file_name: String, size: Int): Vector[String] = {
    val solution_id_file = new File(file_name)
    val solution_id_BR = new BufferedReader(new FileReader(solution_id_file))
    val data = Vector.fill(size)(solution_id_BR.readLine())
    solution_id_BR.close()
    data
  }

  def copy_data_to_folder(): Unit = {
    
    val File1 = new File(path + "In\\" + search_for_id + "_solution_ids.txt")
    val file = new File(parent_path+ "\\" + search_for_id + "\\In\\_solution_ids.txt")
    val BR1 = new BufferedReader(new FileReader(File1))
  }
}
