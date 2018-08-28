package com.cahill.image

import java.io.{ByteArrayInputStream, File}

import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics
import org.apache.spark.SparkContext
import org.apache.spark.sql.SparkSession
import org.openimaj.image.pixel.{ConnectedComponent, Pixel}
import org.openimaj.image.segmentation.{FelzenszwalbHuttenlocherSegmenter, SegmentationUtilities}
import org.openimaj.image.{DisplayUtilities, FImage, ImageUtilities, MBFImage}

import scala.collection.mutable.ArrayBuffer

//https://towardsdatascience.com/detecting-vehicles-using-machine-learning-and-computer-vision-e319ee149e10
object ImageDetection {

  private val MASTER_URL:String = "masterURL"
  private val PATHTODATA:String = "pathToData"
  private val PATHTOTRAINING_SEGMENTS:String = "pathToTrainingSegments"

  //length to breadth Ratio
  val lengthToBreadthOilRatio = 5/6.0
  val lengthToBreadthContainerRatio = 6/8.0

  //TODO 1: Divide images between has ship and no ship
  //oldTODO 2: template match ship images
  //oldTODO 3: Narrow down to closest possible ship (stil need to research)
  //TODO 2: segment find each image
  //TODO 3: bounding box statistically significant segments

  //Should be only one image. They have to be always in pairs (imageID =>RLE pixels).
// The if the shape of the image is 768x768, we have 589824 pixels in a single line.
// Every time you find an object, you get the first pixel as a index and the next number is the length of pixels until
//  find a background. Then this means: 369226 3 => 3 pixels starting from 369226 and ending on 369229.
// Then this means: 369992 5 => 5 pixels starting from 369992 and ending on 369997. And so on.
// Try to understand the explanation from this very good kernel: https://www.kaggle.com/stkbailey/teaching-notebook-for-total-imaging-newbies/notebook
  def main (args: Array[String]): Unit = {

    val encoding = "264661 17 265429 33 266197 33 266965 33 267733 33 268501 33 269269 33 270037 33 270805 33 271573 33 272341 33 273109 33 273877 33 274645 33 275413 33 276181 33 276949 33 277716 34 278484 34 279252 33 280020 33 280788 33 281556 33 282324 33 283092 33 283860 33 284628 33 285396 33 286164 33 286932 33 287700 33 288468 33 289236 33 290004 33 290772 33 291540 33 292308 33 293076 33 293844 33 294612 33 295380 33 296148 33 296916 33 297684 33 298452 33 299220 33 299988 33 300756 33 301524 33 302292 33 303060 33 303827 34 304595 34 305363 33 306131 33 306899 33 307667 33 308435 33 309203 33 309971 33 310739 33 311507 33 312275 33 313043 33 313811 33 314579 33 315347 33 316115 33 316883 33 317651 33 318419 33 319187 33 319955 33 320723 33 321491 33 322259 33 323027 33 323795 33 324563 33 325331 33 326099 33 326867 33 327635 33 328403 33 329171 33 329938 34 330706 34 331474 33 332242 33 333010 33 333778 33 334546 33 335314 33 336082 33 336850 33 337618 33 338386 33 339154 33 339922 33 340690 33 341458 33 342226 33 343003 24 343787 8"

    val fImage = ImageUtilities.readF(new File("C:\\Users\\ph9qum\\Desktop\\000155de5.jpg"))
//    val fImage = ImageUtilities.readF(new File("C:\\Users\\ph9qum\\Desktop\\00a7c6bed.jpg")) //No Ship
//    val fImage = ImageUtilities.readF(new File("C:\\Users\\ph9qum\\Desktop\\2d9c5aef4.jpg"))
    val mask = getMask(fImage.height, fImage.width, encoding)



//    printDoubleArray(createDoublePixelArray(mask,fImage.getHeight(), fImage.getWidth))

//    val hog = new HOG(new FlexibleHOGStrategy(50, 50, 2500))
//    val hog = new HOG(new SimpleBlockStrategy(50))
    val topLeftBottomRight = getTopLeftBottomRight(createDoublePixelArray(mask, fImage.getHeight(), fImage.getWidth()), fImage.getHeight(), fImage.getWidth())

    val maskDouble = createDoublePixelArray(mask, fImage.height, fImage.width)
//    DisplayUtilities.display(new FImage(maskDouble))
    DisplayUtilities.display(fImage)

  // TODO Use below
    val segmentor = new FelzenszwalbHuttenlocherSegmenter[FImage]()

    val segments = segmentor.segment(fImage)
    val segmentList = segments.toArray(new Array[ConnectedComponent](segments.size())).toList
    //Largest ConnectedComponent is the background; TODO needed?
    val cleanedSegmentList = segmentList.sortBy(cc => cc.calculateArea()).slice(0, segmentList.size - 1)
    val stats = new DescriptiveStatistics()

    cleanedSegmentList.foreach(cc => stats.addValue(cc.calculateArea().toDouble))

  //Also test 1 - value for rotations
  //use .5 allowance
    val filteredSegmentList = cleanedSegmentList
      .filter(cc => {
        val ratio = cc.calculateOrientatedBoundingBoxAspectRatio()
        val oneMinusRation = 1 - ratio
        ~=(ratio, lengthToBreadthOilRatio, 0.04) || ~=(ratio, lengthToBreadthContainerRatio, 0.04) || ~=(oneMinusRation, lengthToBreadthContainerRatio, 0.04) || ~=(oneMinusRation, lengthToBreadthOilRatio, 0.04)
      })
    .filter(cc => stats.getQuadraticMean < cc.calculateArea())

    filteredSegmentList.foreach(cc => println(cc.toString))
    filteredSegmentList.foreach(cc => println(encodeConnectedComponent(cc, fImage.height, fImage.width)))


    val segImage = SegmentationUtilities.renderSegments(new MBFImage(fImage), segments)
    DisplayUtilities.display(segImage)

    System.out.println("hello")






  }

  def findSegments(fImage: FImage, display:Boolean):Unit = {
    val segmentor = new FelzenszwalbHuttenlocherSegmenter[FImage]()

    val segments = segmentor.segment(fImage)
    val segmentList = segments.toArray(new Array[ConnectedComponent](segments.size())).toList
    //Largest ConnectedComponent is the background; TODO needed?
    val cleanedSegmentList = segmentList.sortBy(cc => cc.calculateArea()).slice(0, segmentList.size - 1)
    val stats = new DescriptiveStatistics()

    cleanedSegmentList.foreach(cc => stats.addValue(cc.calculateArea().toDouble))

    //Also test 1 - value for rotations
    //use .04 allowance
    val filteredSegmentList = cleanedSegmentList
      .filter(cc => {
        val ratio = cc.calculateOrientatedBoundingBoxAspectRatio()
        val oneMinusRation = 1 - ratio
        ~=(ratio, lengthToBreadthOilRatio, 0.04) || ~=(ratio, lengthToBreadthContainerRatio, 0.04) || ~=(oneMinusRation, lengthToBreadthContainerRatio, 0.04) || ~=(oneMinusRation, lengthToBreadthOilRatio, 0.04)
      })
      .filter(cc => stats.getQuadraticMean < cc.calculateArea())

    filteredSegmentList.foreach(cc => println(cc.toString))
    filteredSegmentList.foreach(cc => println(encodeConnectedComponent(cc, fImage.height, fImage.width)))
    //TODO cc to encoded pixels

    if (display) {
      val segImage = SegmentationUtilities.renderSegments(new MBFImage(fImage), segments)
      DisplayUtilities.display(segImage)
    }
  }

  def encodeConnectedComponent(cc:ConnectedComponent, height:Int, width:Int): String = {
    //(x,y) -> per pixel
    //(383, 468)
    //Pixel value is (
    val pixels = cc.getPixels.toArray(new Array[Pixel](cc.getPixels.size()))
//    val pixel = pixels(0)
//    val x = pixel.x
//    val y = pixel.y

//    val overCount =

    val maskDoubleArray = new Array[Array[Float]](height)
    0 until width foreach(i => {
      maskDoubleArray(i) = Array.fill[Float](height)(0.0f)
    })

    pixels.foreach(p => maskDoubleArray(p.x)(p.y) = 1.0f)

    DisplayUtilities.display(new FImage(maskDoubleArray.transpose))
//    printDoubleArray(maskDoubleArray)
    val maskArray = getArray(maskDoubleArray)
    val encodingList = new ArrayBuffer[String]()

    var a = 0
    var startRun = false
    var count = 0
    var startIndex = 0
    for(a <- maskArray.indices) {
      if (maskArray(a) == 1.0f) {
        count += 1
        if (startRun) {
          startRun = true
          startIndex = a
        }
      } else {
        if (startRun) {
          encodingList += startIndex.toString
          encodingList += count.toString
          startRun = false
          count = 0
          startIndex = 0
        }
      }
    }

    encodingList.mkString(" ")
  }

  //Flatten double array to single array for Run Time Length Encoding
  def getArray(pixels:Array[Array[Float]]): Array[Float] = pixels.flatten

  //Zero Based
  def getPixelPoint(arrayPoint:Integer, rowCount:Integer, colCount:Integer): (Integer, Integer) = {
    val row = Math.floor(arrayPoint/rowCount).toInt
    (row, arrayPoint - (row*colCount))
  }

  def createDoublePixelArray(pixels:Array[Float], height:Integer, width:Integer): Array[Array[Float]] = {
    val pixelDoubleArray = new Array[Array[Float]](height)

    0 until height foreach(row => {
      val rowArray = new Array[Float](width)
      val startPt = row * width
      val endPt = startPt + (width-1)
      pixelDoubleArray(row) = pixels.slice(startPt, endPt).array
    })


    pixelDoubleArray.transpose
  }

  def getMask(rowCount:Integer, colCount:Integer, encoding:String): Array[Float] = {
    val encodingValues = getEncodingValues(encoding)
    var mask = ArrayBuffer.fill(rowCount * colCount)(0.0F)

    encodingValues.foreach( values => {
      Array.range(values._1, values._1 + values._2).foreach(pt => mask(pt) = 1.0F)
    })

    mask.toArray
  }

  def getTopLeftBottomRight(mask:Array[Array[Float]], height:Integer, width:Integer):(Integer, Integer, Integer, Integer) = {
    val topBottomArray = getArray(mask)
    val top = Math.floor(topBottomArray.indexWhere(pt => pt == 1.0f)/height).intValue()
    val bottom = Math.floor(topBottomArray.lastIndexWhere(pt => pt == 1.0f)/height).intValue()
    val tranposed = getArray(mask.transpose)
    val left = Math.floor(tranposed.indexWhere(pt => pt == 1.0f)/width).intValue()
    val right = Math.floor(tranposed.lastIndexWhere(pt => pt == 1.0f)/width).intValue()
    (top, left, bottom, right)
  }

  def getEncodingValues(encoding:String): Array[(Integer, Integer)] = {
    val splits = encoding.split(" ")

    var encodingValues = new ArrayBuffer[(Integer, Integer)](splits.length/2)
    var a = 0
    for(a <- 0 until splits.length by 2) {
      encodingValues ++= getEncodedArray(splits, a)
    }
    encodingValues.toArray
  }

  def getEncodedArray(splits:Array[String], idx:Integer): Array[(Integer, Integer)]  = {
    if (idx % 2 == 0) Array((splits.apply(idx).toInt, splits.apply(idx + 1).toInt)) else Array()
  }

  def printDoubleArray(doubleArray:Array[Array[Float]]): Unit = {
    doubleArray.filter(a => a.contains(1.0f)).foreach(f => {f.foreach(l => print(l + " ")); println()})
  }

  //https://alvinalexander.com/scala/how-to-compare-floating-point-numbers-in-scala-float-double
  def ~=(x: Double, y: Double, precision: Double) = {
    if ((x - y).abs < precision) true else false
  }

  //TODO: is Spark even needed?
  def run(masterURL:String, pathToFolder:String, pathToTrainingSegs:String): Unit = {
    val spark = SparkSession
      .builder()
      .master(masterURL)
      .appName("Image Detection")
      .getOrCreate()
    val sc: SparkContext = spark.sparkContext




    val images = sc.wholeTextFiles(pathToFolder)
    val fImages = images.map(f => ImageUtilities.readF(new ByteArrayInputStream(f._2.getBytes)))
    //    val imageHOGS = fImages.map(f => new HOG(new FlexibleHOGStrategy()))


    //    val hog = new HOG(new FlexibleHOGStrategy())

    sc.stop()
  }
//  //    val optMap = args.map(s => {
//  //      val split = s.split("=")
//  //      (split(0), split(1))
//  //    }).toMap
//  //
//  //    val masterURL = if (optMap.contains(MASTER_URL)) optMap.get(MASTER_URL).toString else "local"
//  //    val pathToFolder = if (optMap.contains(PATHTODATA)) optMap.get(PATHTODATA).toString else "C:\\Users\\ph9qum\\Desktop\\defaultdata\\"
//  //    val pathToTrainingSegs = if (optMap.contains(PATHTOTRAINING_SEGMENTS)) optMap.get(PATHTOTRAINING_SEGMENTS).toString else "C:\\Users\\ph9qum\\Desktop\\train_ship_sgementations.csv"
//  //
//  //    run(masterURL, pathToFolder, pathToTrainingSegs)
//
//
//  val encoding = "264661 17 265429 33 266197 33 266965 33 267733 33 268501 33 269269 33 270037 33 270805 33 271573 33 272341 33 273109 33 273877 33 274645 33 275413 33 276181 33 276949 33 277716 34 278484 34 279252 33 280020 33 280788 33 281556 33 282324 33 283092 33 283860 33 284628 33 285396 33 286164 33 286932 33 287700 33 288468 33 289236 33 290004 33 290772 33 291540 33 292308 33 293076 33 293844 33 294612 33 295380 33 296148 33 296916 33 297684 33 298452 33 299220 33 299988 33 300756 33 301524 33 302292 33 303060 33 303827 34 304595 34 305363 33 306131 33 306899 33 307667 33 308435 33 309203 33 309971 33 310739 33 311507 33 312275 33 313043 33 313811 33 314579 33 315347 33 316115 33 316883 33 317651 33 318419 33 319187 33 319955 33 320723 33 321491 33 322259 33 323027 33 323795 33 324563 33 325331 33 326099 33 326867 33 327635 33 328403 33 329171 33 329938 34 330706 34 331474 33 332242 33 333010 33 333778 33 334546 33 335314 33 336082 33 336850 33 337618 33 338386 33 339154 33 339922 33 340690 33 341458 33 342226 33 343003 24 343787 8"
//
//  val fImage = ImageUtilities.readF(new File("C:\\Users\\ph9qum\\Desktop\\000155de5.jpg"))
//  //    val fImage = ImageUtilities.readF(new File("C:\\Users\\ph9qum\\Desktop\\00a7c6bed.jpg")) //No Ship
//  //    val fImage = ImageUtilities.readF(new File("C:\\Users\\ph9qum\\Desktop\\2d9c5aef4.jpg"))
//  val mask = getMask(fImage.height, fImage.width, encoding)
//
//
//
//  //    printDoubleArray(createDoublePixelArray(mask,fImage.getHeight(), fImage.getWidth))
//
//  //    val hog = new HOG(new FlexibleHOGStrategy(50, 50, 2500))
//  //    val hog = new HOG(new SimpleBlockStrategy(50))
//  val topLeftBottomRight = getTopLeftBottomRight(createDoublePixelArray(mask, fImage.getHeight(), fImage.getWidth()), fImage.getHeight(), fImage.getWidth())
//  //    hog.analyseImage(fImage)
//  //    val histogram = hog.getFeatureVector(new Rectangle(new Point2dImpl(topLeftBottomRight._1.floatValue(), topLeftBottomRight._2.floatValue()), new Point2dImpl(topLeftBottomRight._3.floatValue(), topLeftBottomRight._4.floatValue())))
//
//
//  //    val hogDetector = new HOGDetector(GetHOGClassifier.getHOGClassifier(fImage.height, fImage.width))
//  //    val rectangle = hogDetector.detect(fImage)
//
//  //    val engine = new DoGSIFTEngine()
//  //    val features = engine.findFeatures(fImage)
//  //    val featuresArray = features.asDataArray(new Array[Keypoint](features.vecLength()))
//  //    val featuresArray:Array[Keypoint] = features.toArray(new Array[Keypoint](features.vecLength()))
//
//  val maskDouble = createDoublePixelArray(mask, fImage.height, fImage.width)
//  //    DisplayUtilities.display(new FImage(maskDouble))
//  DisplayUtilities.display(fImage)
//
//  //    val featuresToMask = featuresArray.filter(kp => maskDouble.apply(kp.x.intValue()).apply(kp.y.intValue()) == 1.0F)
//
//  //LocalFeatureMatcher<Keypoint> matcher = new BasicMatcher<Keypoint>(80);
//  //matcher.setModelFeatures(queryKeypoints);
//  //matcher.findMatches(targetKeypoints);
//  //    val matcher = new BasicMatcher[Keypoint](80)
//  //    matcher.setModelFeatures()
//  //    matcher.findMatches(features)
//
//  //    val pixelSet = new PixelSet(fImage,0.1f)
//  //    val filter = new LocalContrastFilter(pixelSet.pixels)
//  //    val filter = new AverageBoxFilter(15)
//  //    val filter = new AdaptiveLocalThresholdMedian(50)
//  //    filter.processImage(fImage)
//
//  //    fImage.processInplace(new CannyEdgeDetector(.1f))
//  //    fImage.processInplace(new SUSANEdgeDetector())
//  //    DisplayUtilities.display(fImage)
//
//  //  getTopLeftBottomRight
//  //  fImage.ex x, y, w, h
//  /**
//    * y = 344 (top)
//    * x = 466 (left)
//    * w = 501 - 466
//    * h = 447 - 344
//    */
//  //    val template = fImage.extractROI(344,466,447,501)
//  //    val template = fImage.extractROI(466, 344, (501 - 466), (447 - 344))
//  //    val template = fImage.extractROI(new Rectangle(new Point2dImpl(topLeftBottomRight._1.floatValue(), topLeftBottomRight._2.floatValue()), new Point2dImpl(topLeftBottomRight._3.floatValue(), topLeftBottomRight._4.floatValue())))
//  //    val tm = new TemplateMatcher(template,TemplateMatcher.Mode.CORRELATION_COEFFICIENT)
//  //    DisplayUtilities.display(template)
//  //    val testImage = ImageUtilities.readF(new File("C:\\Users\\ph9qum\\Desktop\\00a7c6bed.jpg")) //Contains NO Ship; not considering this case as it'll be separated before here (hopefully)
//  //      fImage.analyseWith(tm)
//  //    DisplayUtilities.display(fImage)
//  //    val image = tm.getResponseMap.normalise()
//  //    DisplayUtilities.display(image)
//
//  //    val filter = new LocalContrastFilter(FilterSupport.createBlockSupport(image.width, image.height))
//  //    val filter = new AverageBoxFilter(5)
//  //    filter.processImage(image)
//  //    DisplayUtilities.display(image)
//
//  //    image.processInplace(new CannyEdgeDetector(.1f))
//  //    image.processInplace(new SUSANEdgeDetector())
//  //    DisplayUtilities.display(image)

}
