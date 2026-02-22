package SolutionFinder

import java.io.{BufferedWriter, FileWriter}

class WriteSolutions(path: String) {
  val File_Writer = new FileWriter(path + "/_solution_ids.txt")
  val SolutionIDs_BW = new BufferedWriter(File_Writer)
  def append_solution_id(str: String): Unit = {
    SolutionIDs_BW.write(str + "\n")
  }

  def close(): Unit = {
    SolutionIDs_BW.close()
  }

}
