package zoomInSolution

import scala.collection.mutable


object DisjointUnion {

  
  /**
   * from https://stackoverflow.com/questions/17409641/union-find-or-disjoint-set-data-structure-in-scala
   * Union Find implementaion.
   * Find is O(1)
   * Union is O(log(n))
   * Implementation is using a HashTable. Each wrap has a set which maintains the elements in that wrap.
   * When 2 wraps are union, then both the sets are clubbed. O(log(n)) operation
   * A HashMap is also maintained to find the Wrap associated with each node. O(log(n)) operation in mainitaining it.
   *
   * If the input array is null at any index, it is ignored
   */
  class UnionFind[T](all: Array[T]) {
    private val dataStruc = new mutable.HashMap[T, Wrap]
    for (a <- all if (a != null))
      dataStruc.addOne(a -> new Wrap(a))
    
    /**
     * The number of Unions
     */
    private var size = dataStruc.size
    
    
    def get_eq_classes(): Vector[Vector[T]] = {
      val res = dataStruc.map((a,w) => w.set.toVector).toVector
      res
    }
    /**
     * Unions the set containing a and b
     */
    def union(a: T, b: T): Wrap = {
      val first: Wrap = dataStruc(a)
      val second: Wrap = dataStruc(b)
      if (first.contains(b) || second.contains(a))
        first
      else {
        // below is to merge smaller with bigger rather than other way around
        val firstIsBig = (first.set.size > second.set.size)
        val ans = if (firstIsBig) {
          first.set = first.set ++ second.set
          second.set.foreach(a => {
            dataStruc.remove(a)
            dataStruc.addOne(a -> first)
          })
          first
        } else {
          second.set = second.set ++ first.set
          first.set.foreach(a => {
            dataStruc.remove(a)
            dataStruc.addOne(a -> second)
          })
          second
        }
        
        size = size - 1
        ans
      }
    }

    /**
     * true if they are in same set. false if not
     */
    def find(a: T, b: T): Boolean = {
      dataStruc(a).contains(b)
    }

    def sizeUnion: Int = size

    class Wrap(e: T) {
      var set = new mutable.HashSet[T]
      set.add(e)

      def add(elem: T): Boolean = {
        set.add(elem)
      }

      def contains(elem: T): Boolean = set.contains(elem)
    }
  }
}
