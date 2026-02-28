package SolutionFinder

import SolutionFinder.ImmutablePolyomino.Polyomino_2d

import java.awt.image.BufferedImage
import java.io.{File, IOException}
import javax.imageio.ImageIO
import scala.collection.mutable

class CreateImage(val ImgWidth: Int, val ImgHeight: Int, val ImgPath: String, val ColorMap: Array[Int]) {

  def create(Map: mutable.HashMap[(Int, Int), Int], TilingID: String): Unit = {
    val BI = new BufferedImage(ImgWidth, ImgHeight, BufferedImage.TYPE_INT_RGB)
    //all pixels are of value 0x000000 by default
    //TODO: set all pixels transparent
    BI.setRGB(0,0,ImgWidth,ImgHeight,Array.fill(ImgWidth*ImgHeight)(0x00ffffff),0,1)
    // color in pixels
    for
      (x,y) <- Map.keys
    do
      val color = Map((x,y))
      val rgb = ColorMap(color)
      BI.setRGB(x, y, rgb)

    //store in file
    try ImageIO.write(BI, "png", new File(ImgPath + "/" + TilingID + ".png"))
    catch {
      case e: IOException =>
        // TODO Auto-generated catch block
        e.printStackTrace()
    }
  }

  def create_reference_image(limited_permutations: Array[IndexedSeq[Polyomino_2d]]): Unit = {
    //1. get image dimensions - y = sum(max(brick_width, brick_height) : for all bricks)
    val spacing = 2
    var ImgHeight = spacing
    var ImgWidth = 0
    for permutation<-limited_permutations
      do {
        val brick_size = permutation(0).get_size()
        ImgHeight += spacing + brick_size
        ImgWidth = ImgWidth.max(permutation.size * (brick_size+spacing )+ 2*spacing + brick_size + 1)
      }
    //2. set all pixels white
    val BI = new BufferedImage(ImgWidth, ImgHeight, BufferedImage.TYPE_INT_RGB)
    BI.setRGB(0,0,ImgWidth,ImgHeight,Array.fill(ImgWidth*ImgHeight)(0xffffff),0,1)

    //3. make rows
    var starty = spacing-1
    for i <- limited_permutations.indices
      do {
        val shifted_first = limited_permutations(i)(0).get_shift_nonnegative_coordinates().shift(spacing-1, starty)
        fill_brick(BI,shifted_first,ColorMap(i+1))
        val brick_size = limited_permutations(i)(0).get_size()
        var startx = 2*spacing + shifted_first.get_size() +1
        for j <- limited_permutations(i).indices
          do {
            val limited_offset = limited_permutations(i)(j).get_shift_nonnegative_coordinates().shift(startx, starty)
            fill_brick(BI,limited_offset,ColorMap(i+1))
            val (dx,dy) = limited_permutations(i)(j).get_shift_for_nonnegative_coordinates()
            BI.setRGB(startx+dx, starty+dy, black_but_good_contrast_to(ensure_contrast_to_white(ColorMap(i+1))))
            startx += spacing + brick_size
          }
        starty += limited_permutations(i)(0).get_size() + spacing
      }

    // write image
    try ImageIO.write(BI, "png", new File(ImgPath  + "/_Reference.png"))
    catch {
      case e: IOException =>
        // TODO Auto-generated catch block
        e.printStackTrace()
    }


  }

  private def fill_brick(BI: BufferedImage, brick: Polyomino_2d, brick_color: Int): Unit = {
    val coordinates = brick.coordinates
    val color = ensure_contrast_to_white(brick_color)
    for (x,y) <- coordinates
      do BI.setRGB(x,y,color)
  }

  private def ensure_contrast_to_white(original_color: Int, threshold: Int = 0xaa): Int = {
    val b = original_color % 0x100
    val g = (original_color >>> 8) % 0x100
    val r = (original_color >>> 16) % 0x100
    if r>threshold & g>threshold & b>threshold then 0xffffff - original_color
    else original_color
  }

  private def black_but_good_contrast_to(original_color: Int, threshold: Int = 0x81): Int = {
    val b = original_color % 0x100
    val g = (original_color >>> 8) % 0x100
    val r = (original_color >>> 16) % 0x100
    if r < threshold & g < threshold & b < threshold then {
      0xcfcf00
    }
    else 0x000000
  }
}