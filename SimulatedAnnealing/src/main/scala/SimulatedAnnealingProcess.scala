import org.apache.spark.SparkContext

/**
 * Starting point for Simulated annealing on Spark
 */
object SimulatedAnnealingProcess {

  //Work based off of Estimating Financial risk with Apache Spark

  def main(args: Array[String]): Unit = {

    if (args.length < 1) {
      System.err.println("Usage: SimulatedAnnealingProcess <PathtoSchoolInfoFile>")
      System.exit(1)
    }

    val Array(schoolInfo) = args

    val sc = new SparkContext()

    //TODO: broadcast schoolInfo
    //TODO: create collection of RDDs
    //NOTE: each RDD will essentially be on its own
    // after the first round to do SA
    //TODO: Figure out SA to begin with
    //TODO: return trials to an rdd
    //TODO: find best scored of each rdd
    //TODO; find best of best.



  }

}
