package SolutionFinder

import SolutionFinder.ImmutablePolyomino.Polyomino_2d

import scala.collection.mutable


class SolutionCategories(bricks: Array[Vector[(Int,Int)]], ImageCreator: CreateImage) {
  //1. get limited permutations of bricks and create reference image
  private val Polyominos = bricks.map(x => Polyomino_2d(x))
  private val limited_Permutations = Polyominos.map(p => p.get_limited_permutation())
  
  ImageCreator.create_reference_image(limited_Permutations)

  //2. get all permutations
  def get_offsets(V: IndexedSeq[Polyomino_2d]): IndexedSeq[(Int,(Int,Int), Vector[(Int,Int)])] = {
    def yield_off = {
      for
        i <- V.indices
        (dx, dy) <- V(i).coordinates
      yield (i, (dx,dy), V(i).shift(-dx, -dy).coordinates)
    }

    yield_off.distinctBy((_,_,x) => x.toSet)
  }
  private val (permutation_indices,permutation_shifts, permutations) = limited_Permutations.map(l => get_offsets(l).unzip3).unzip3

  def get_all_permutations_as_Hashmap: mutable.HashMap[Int, IndexedSeq[Vector[(Int, Int)]]] = {
    val HM = new mutable.HashMap[Int, IndexedSeq[Vector[(Int, Int)]]]()
    for i<- permutations.indices
      do HM.addOne(i -> permutations(i))
    HM
  }


  def get_solution_id(values: List[(Int, Int, Int)]): String = {
    // values contains (start_coordinates(0), start_coordinates(1), offset_index) and index = brick_index
    //assuming that x,y are in the range of [0,9]
    var Str = ""
    var solution_id = new StringBuilder()
    for i<- values.indices
        elem = values(i)
    do {
      val corresponding_shift = permutation_shifts(i)(elem(2))
      val ind_x = elem(0) - corresponding_shift(0)
      val ind_y = elem(1) - corresponding_shift(1)
      val corresponds_to_in_limited = permutation_indices(i)(elem(2))
      val offset_repr = get_ascii_repr(corresponds_to_in_limited)
      Str += s"$ind_x,$ind_y$offset_repr-"
      solution_id.addAll(s"$ind_x,$ind_y$offset_repr-")
    }
    solution_id.deleteCharAt(solution_id.size-1)
    solution_id.toString()

  }

  private def get_ascii_repr(value: Int) = {
    val ascii_val = value match {
      case x if 0<=x & x<26 => (x+97).toByte //lowercase letters
      case x if 25<x & x<52 => (x+65 - 26).toByte //uppercase letters
      case _ => 63.toByte //? symbol
    }
    new String(Array(ascii_val),"ASCII")
  }

}