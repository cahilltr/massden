import org.apache.spark.SparkContext

/**
 * Starting point for Simulated annealing on Spark
 */
object SimulatedAnnealingProcess {

  def main(args: Array[String]): Unit = {

    if (args.length < 4) {
      System.err.println("Usage: SparkStreamingEnron <zkQuorum> <group> <topics> <numThreads>")
      System.exit(1)
    }

    val Array(zkQuorum, group, topics, numThreads) = args

    val sc = new SparkContext()

  }

}
