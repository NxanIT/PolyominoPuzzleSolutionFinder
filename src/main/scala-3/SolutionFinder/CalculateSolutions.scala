package SolutionFinder

import java.io.{BufferedReader, File, FileReader}
import scala.collection.mutable


object CalculateSolutions {

//  val bricks = Array(
//    Vector((0, 0), (0, 1), (0, -1), (1, 0), (-1, 0)), //cross orange
//    Vector((0, 0), (0, 1), (0, -1), (1, 1), (1, -1)), // light blue
//    Vector((0, 0), (0, -1), (0, -2), (0, 1), (1, 0)), // purple
//    Vector((0, 0), (0, 1), (0, 2), (0, 3), (1, 0)), // yellow
//    Vector((0, 0), (0, 1), (1, 1), (-1, 0), (-1, -1)), // dark green
//    Vector((0, 0), (0, 1), (0, 2), (1, 2), (1, 3)), // pink
//    Vector((0, 0), (0, 1), (0, 2), (1, 0), (2, 0)), //light green
//    Vector((0, 0), (0, 1), (0, 2), (1, 1), (1, 2)), //grey
//    Vector((0, 0), (0, 1), (0, 2), (0, 3)), // white
//    Vector((0, 0), (0, 1), (1, 0), (1, 1)), // blue
//    Vector((0, 0), (0, 1), (0, 2), (1, 0)), // dark red
//    Vector((0, 0), (0, 1), (1, 0)), // rose
//  )
//  private val colorMap: Array[Int] = Array(0x000000,
//    0xff6600, 0x00ffff, 0xaa14ff,
//    0xffff00, 0x006600, 0xff1450,
//    0x00ff00, 0x808080, 0xffffff,
//    0x0000ff, 0x800000, 0xff6699)
//
//  private val tiling = mutable.HashMap[(Int, Int), Int]()
//  for
//    i <- 0 to 9
//    j <- 0 to 9
//    if i + j <= 9
//  do
//    tiling += (i, j) -> 0
//
//  //printout
//  for i <- bricks.indices
//    do {
//      val brick = bricks(i)
//      println(s"brick $i, \t $brick")
//    }

  def start(ProjectPath: String,
            createImages: Boolean,
            createStatisticalInfo: Boolean): Boolean = {
    val SolutionsDirectory = new File(ProjectPath + "/Solutions")
    SolutionsDirectory.mkdir()
    if(createImages){
      val ImageDirectory = new File(ProjectPath + "/SolutionImages")
      ImageDirectory.mkdir()
    }
    val PD = new readPuzzleData(ProjectPath)
    val tiling = PD.getEmptyTiling()
    val tiling_2d = new mutable.HashMap[(Int, Int), Int]()
    tiling.keySet.map((x,y,z) => tiling_2d.addOne((x,y)->0))

    val bricks = PD.getBricks()
    val bricks_2d = bricks.map(v => v.map((x,y,z)=> (x,y)))

    val colorMap = PD.getColorMap()
    println(colorMap.mkString("Array(", ", ", ")"))

    val Program = new Tiler(tiling_2d,bricks_2d,colorMap, ProjectPath)
    Program.start()
    true
  }

  class readPuzzleData(ProjectPath: String) {
    def getEmptyTiling(): mutable.HashMap[(Int, Int, Int), Int] = {
      val MapBR = new BufferedReader(FileReader(ProjectPath + "/PuzzleData/map.txt"))
      val Tiling = new mutable.HashMap[(Int, Int, Int), Int]()
      var nextLayer = MapBR.readLine()
      while(nextLayer != null){
        val coordinate_start = nextLayer.indexOf("-")
        val z = nextLayer.substring(5,coordinate_start).toInt
        val xy_coordinates: Array[String] = nextLayer.substring(nextLayer.indexOf("-") + 1).split(",")
        for {xy:String <- xy_coordinates}
        do {
          val slash_in_coordinate = xy.indexOf("/")
          val x = xy.substring(1,slash_in_coordinate).toInt
          val y = xy.substring(slash_in_coordinate+1,xy.length-1).toInt
          Tiling.addOne(((x,y,z),0))
        }
        nextLayer = MapBR.readLine()
      }
      Tiling
    }

    def getBricks(): Array[Vector[(Int, Int,Int)]] = {
      def getIntRep(s: String): (Int, Int, Int) = {
        // format of s: (x/y/z)
        val slash1 = s.indexOf("/")
        val slash2 = s.indexOf("/", slash1 + 1)
        val x = s.substring(1, slash1).toInt
        val y = s.substring(slash1 + 1, slash2).toInt
        val z = s.substring(slash2 + 1,s.length-1).toInt
        (x, y, z)
      }
      val BricksFR = new FileReader(ProjectPath + "/PuzzleData/bricks.txt")
      val Lines: Array[String] = BricksFR.readAllAsString().split("\n")
      val Coordinates:Array[Array[String]] = Lines.map(s => s.substring(s.indexOf(":")+1).split(","))
      Coordinates.map(b => b.map(c => getIntRep(c))).map(b => b.toVector)
    }


    def getColorMap(): Array[Int] = {
      val ColorFR = new FileReader(ProjectPath + "/PuzzleData/colors.txt")
      val ColorHexCodes:Array[String] = ColorFR.readAllAsString().split("\n")
      println(ColorHexCodes.mkString("Array(", ", ", ")"))
      Array(0).appendedAll(ColorHexCodes.map(s => Integer.valueOf(s.substring(1),16).toInt))
    }
  }

}
