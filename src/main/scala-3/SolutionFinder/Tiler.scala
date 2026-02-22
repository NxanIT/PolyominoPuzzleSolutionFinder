package SolutionFinder

import scala.collection.mutable

class Tiler (Tiling: mutable.HashMap[(Int, Int), Int],
             bricks: Array[Vector[(Int,Int)]],
             colorMap: Array[Int],
             ProjectPath: String
            ){

  private val ImageCreator = new CreateImage(10, 10, ProjectPath + "/SolutionImages", colorMap)
  private val Categorizer = new SolutionCategories(bricks, ImageCreator)
  private val bricks_with_offset: mutable.HashMap[Int, IndexedSeq[Vector[(Int, Int)]]] = Categorizer.get_all_permutations_as_Hashmap
  private val Topologizer = new WriteSolutions(ProjectPath + "/Solutions")

  //debug variables TODO: remove
  private var LayerStats: List[Long] = List.fill(13)(0.toLong)
  private var solutions_found = 0
  private var pre_sol_found = 0

  def start():Unit = {
    val number_of_bricks = bricks_with_offset.size
    val SolutionID = List.fill(number_of_bricks)((0,0,0))
    new_Layer(Tiling,bricks_with_offset,SolutionID)
    Topologizer.close()
  }

  private def new_Layer(Tiling: mutable.HashMap[(Int, Int), Int],
                        bricks_with_offset: mutable.HashMap[Int, IndexedSeq[Vector[(Int, Int)]]],
                        Solution_ID: List[(Int, Int, Int)]
                       ): Unit = {
    //1. get start position
    val start_coordinates = get_start_position(Tiling)
    //1.25: only for debug - TODO: remove
    val empty_tiling = Tiling.count((k, c) => c == 0)
    val Lay = 12 - bricks_with_offset.size
    //      if(Lay==8) then {
    //        ImageCreator.create(Tiling,s"_pre-8-debug-$pre_sol_found")
    //        pre_sol_found += 1
    //      }
    //      if pre_sol_found>=40 then sys.exit(42)
    LayerStats = LayerStats.updated(Lay, 1.toLong + LayerStats(Lay))



    //1.5: add offset to bricks here instead of twice TODO:

    //2. loop over all bricks in bricks_with_offset and all offsets
    for
      brick_index <- bricks_with_offset.keys
      offset_index <- bricks_with_offset(brick_index).indices
      brick = bricks_with_offset(brick_index)(offset_index)
      if can_be_added(Tiling, brick, start_coordinates)
    do {
      //3. Add brick to new Tiling
      val color = brick_index + 1
      val NewTiling = Tiling.clone()
      add_brick(NewTiling, brick, start_coordinates, color)
      val New_bricks_with_offset = bricks_with_offset.clone()
      New_bricks_with_offset.remove(brick_index)
      val NewSolution_ID = Solution_ID.updated(brick_index, (start_coordinates(0), start_coordinates(1), offset_index))

      //4. Check if Tiling is now complete -> create image; else create new layer
      val count_NewTiling_still_empty = NewTiling.count((k, c) => c == 0)
      if count_NewTiling_still_empty == 0 then {
        //for debug TODO: remove
        //println(s"Solution found in Layer $Lay.") //for debug TODO: remove
        if solutions_found % 100 == 0 then println(s"$solutions_found, $LayerStats")
        solutions_found = solutions_found + 1
        val NewSolution_ID_Str = Categorizer.get_solution_id(NewSolution_ID)
        //for debug TODO: uncomment
        ImageCreator.create(NewTiling,NewSolution_ID_Str)
        Topologizer.append_solution_id(NewSolution_ID_Str)

        /*
        TODO: break for loop
          (be aware that if there are more bricks than needed and if
          one brick is coordinate wise a combination of other bricks,
          then breaking here can lead to loss of solutions)
        */

      } else {

        new_Layer(NewTiling, New_bricks_with_offset, NewSolution_ID)
      }
    }
  }

  private def get_start_position(Tiling: mutable.HashMap[(Int, Int), Int]): (Int, Int) = {
    //TODO: update method for a more efficient algorithm

    val keys_filtered = Tiling.filter((v, color) => color == 0).keys.toVector
    keys_filtered.size match {
      case x if x<25 => least_empty_Neighbors(keys_filtered)
      case _ => max_distance_from_origin(keys_filtered)
    }
  }

  private def least_empty_Neighbors(Tiling_empty: Vector[(Int,Int)]): (Int, Int) = {
    val weight_matrix = Vector((-1,1,1),(0,1,4),(1,1,1),
      (-1,0,4),(1,0,4),
      (-1,-1,1),(0,-1,4),(1,-1,1))
    def score_function(x:Int,y:Int) = {
      val summands = weight_matrix.map((dx,dy,score) => if Tiling_empty.contains((x+dx,y+dy)) then score else 0)
      summands.sum
    }
    val min_score = Tiling_empty.zipWithIndex.minBy((c,_) => score_function(c(0),c(1)))._2
    Tiling_empty(min_score)
  }

  private def max_distance_from_origin(keys_filtered: Vector[(Int,Int)]): (Int, Int) = {
    //TODO: update method for a more efficient algorithm

    val max_index = keys_filtered.zipWithIndex.maxBy(p => p._1(0) + p._1(1))._2
    keys_filtered(max_index)
  }

  private def can_be_added(Tiling: mutable.HashMap[(Int, Int), Int], brick: Vector[(Int, Int)], start: (Int, Int)): Boolean = {
    val (start_x, start_y) = start
    val coordinates_in_tiling = brick.forall(x => Tiling.contains((x(0) + start_x, x(1) + start_y)))
    if (!coordinates_in_tiling) {
      false
    } else {
      brick.forall(x => Tiling((x(0) + start_x, x(1) + start_y)) == 0)
    }
  }

  private def add_brick(Tiling: mutable.HashMap[(Int, Int), Int], brick: Vector[(Int, Int)], start: (Int, Int), color: Int): Unit = {
    val (start_x, start_y) = start
    for coordinate <- brick
      do {
        val shifted_coordinate = (coordinate(0) + start_x, coordinate(1) + start_y)
        Tiling(shifted_coordinate) = color
      }
  }

}