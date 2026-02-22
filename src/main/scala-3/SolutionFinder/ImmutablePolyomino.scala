package SolutionFinder


object ImmutablePolyomino {
  class Polyomino_2d(val coordinates: Vector[(Int, Int)]) {

    def shift(delta_x: Int, delta_y: Int): Polyomino_2d = {
      new Polyomino_2d(coordinates.map((x,y) => (x+delta_x,y+delta_y)))
    }

    private def rotate90clockwise(): Polyomino_2d = {
      new Polyomino_2d(coordinates.map((x, y) => (y, -x)))
    }

    private def rotate90counterclockwise(): Polyomino_2d = {
      new Polyomino_2d(coordinates.map((x, y) => (-y, x)))
    }

    def rotate(axis: Int, angle: Int): Polyomino_2d = {
      //axis does not matter since dim=2
      //origin of rotation is (0,0)

      angle match {
        case 90 => rotate90clockwise()
        case 180 =>
          val intermediate = rotate90clockwise()
          intermediate.rotate90clockwise()
        case 270 => rotate90counterclockwise()
        case x if x < 0 => rotate(axis, angle + (angle / 360 + 1) * 360)
        case _ => this
      }
    }

    private def reflectx(): Polyomino_2d = {
      new Polyomino_2d(coordinates.map((x, y) => (x, -y)))
    }

    private def reflecty(): Polyomino_2d = {
      new Polyomino_2d(coordinates.map((x, y) => (-x, y)))
    }

    def reflect(axis: Int): Polyomino_2d = {
      //1 - reflection on x-axis, 2 - reflection on y-axis
      axis match {
        case 1 => reflectx()
        case 2 => reflecty()
        case _ => this
      }
    }

    def get_size(): Int = {
      val min_y = coordinates.minBy((x,y)=>y)
      val max_y = coordinates.maxBy((x,y)=>y)
      val min_x = coordinates.minBy((x,y)=>x)
      val max_x = coordinates.maxBy((x,y)=>x)
      if max_y(1)-min_y(1) > max_x(0)-min_x(0) then max_y(1)-min_y(1)
      else max_x(0)-min_x(0)
    }

    def get_shift_nonnegative_coordinates(): Polyomino_2d = {
      val (dx,dy) = get_shift_for_nonnegative_coordinates()
      this.shift(dx,dy)
    }

    def get_shift_for_nonnegative_coordinates(): (Int,Int) = {
      val min_x = coordinates.minBy((x, y) => x)
      val min_y = coordinates.minBy((x, y) => y)
      (-min_x(0), -min_y(1))
    }

    private def get_all_permutations(): IndexedSeq[Polyomino_2d] = {
      for
        i <- coordinates.indices
        r <- 0 to 3
        s <- 0 to 1
      yield {
        val (dx, dy) = coordinates(i)
        shift(-dx, -dy).rotate(0, r * 90).reflect(s)
      }
    }

    def get_permutation(): IndexedSeq[Polyomino_2d] = {
      get_all_permutations().distinctBy(x=>x.coordinates.toSet)
    }

    private def get_limited_permutations(): IndexedSeq[Polyomino_2d] = {
      for
        r <- 0 to 3
        s <- 0 to 1
      yield {
        rotate(0, r * 90).reflect(s)
      }
    }

    def get_limited_permutation(): IndexedSeq[Polyomino_2d] = {
      // here another distinctness quality has to be used
      get_limited_permutations().distinctBy(x=>x.get_shift_nonnegative_coordinates().coordinates.toSet)
    }
    //
    //    def is_distinct_modulo_translation(other: Polyomino_2d): Boolean = {
    //      other.get_shift_nonnegative_coordinates().coordinates.toSet == get_shift_nonnegative_coordinates().coordinates.toSet
    //    }

    //TODO: equals does not work, distinctBy used, hence not needed, remove

    override def equals(obj: Any): Boolean = {
      obj match {
        case x: Polyomino_2d => coordinates.toSet == x.coordinates.toSet
        case _ => false
      }
    }

  }
}
