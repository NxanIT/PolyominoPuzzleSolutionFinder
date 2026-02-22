package SolutionFinder

import scala.collection.mutable.ArrayBuffer

object Polyomino {
  trait tile {
    def shift(delta_x: Int, delta_y: Int): Unit
    def rotate(axis: Int, angle: Int): Unit
    def reflect(axis: Int): Unit
  }

  class Polyomino_2d(val coordinates: ArrayBuffer[(Int,Int)]) extends tile {
    override def clone(): Polyomino_2d = {
      new Polyomino_2d(coordinates.clone())
    }

    override def shift(delta_x: Int, delta_y: Int): Unit = {
      for i <- coordinates.indices
        do {
          val (x, y) = coordinates(i)
          coordinates.update(i, (x + delta_x, y + delta_y))
        }
    }

    def rotate90clockwise(): Unit = {
      for
        i <- coordinates.indices
      do {
        val (x, y) = coordinates(i)
        coordinates.update(i, (y, -x))
      }
    }

    def rotate90counterclockwise(): Unit = {
      for
        i <- coordinates.indices
      do {
        val (x, y) = coordinates(i)
        coordinates.update(i, (-y, x))
      }
    }

    override def rotate(axis: Int, angle: Int): Unit = {
      //axis does not matter since dim=2
      //origin of rotation is (0,0)

      angle match {
        case 0 =>
        case x if x < 0 => rotate(axis, angle + (angle / 360 + 1) * 360)
        case 90 => rotate90clockwise()
        case 180 => {
          rotate90clockwise()
          rotate90clockwise()
        }
        case 270 => rotate90counterclockwise()
        case _ => println("error")
      }
    }

    def reflectx(): Unit = {
      for
        i <- coordinates.indices
      do
        val (x, y) = coordinates(i)
        coordinates.update(i, (x, -y))
    }

    def reflecty(): Unit = {
      for
        i <- coordinates.indices
      do
        val (x, y) = coordinates(i)
        coordinates.update(i, (-x, y))
    }

    override def reflect(axis: Int): Unit = {
      //1 - reflection on x-axis, 2 - reflection on y-axis
      axis match {
        case 0 =>
        case 1 => reflectx()
        case 2 => reflecty()
        case _ => println("error")
      }
    }

    private def get_all_permutations(): IndexedSeq[Polyomino_2d] = {
      val L: Set[Polyomino_2d] = Set()
      for
        i <- coordinates.indices
        r <- 0 to 3
        s <- 0 to 1
      yield {
        val (dx, dy) = coordinates(i)
        val P = clone()
        P.shift(-dx, -dy)
        P.rotate(0, r * 90)
        P.reflect(s)
        P
      }
    }

    def get_permutation_set(): IndexedSeq[Set[(Int, Int)]] = {
      get_all_permutations().map(x => x.coordinates.toSet).distinct
    }

    def get_permutation(): IndexedSeq[Polyomino_2d] = {
      get_all_permutations().distinct
    }

    private def get_limited_permutations(): IndexedSeq[Polyomino_2d] = {
      val L: Set[Polyomino_2d] = Set()
      for
        r <- 0 to 3
        s <- 0 to 1
      yield {
        val P = clone()
        P.rotate(0, r * 90)
        P.reflect(s)
        P
      }
    }

    def get_limited_permutation_set(): IndexedSeq[Set[(Int, Int)]] = {
      get_limited_permutations().map(x => x.coordinates.toSet).distinct
    }
    //TODO: add equals method so .toSet is not longer needed

    override def equals(obj: Any): Boolean = {
      val equals_obj = obj match {
        case x: Polyomino_2d => coordinates.toSet == x.coordinates.toSet
        case _ => false
      }
      equals_obj
    }
  }
}
