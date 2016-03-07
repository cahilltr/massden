package SparkMLLib

import org.apache.spark.sql.Row
import org.apache.spark.sql.functions._
import org.apache.spark.{SparkContext, SparkConf}

/**
  * Created by cahillt on 2/26/16.
  */
object MarchMadnessPredicitions {

  def main (args: Array[String]): Unit = {

    val conf = new SparkConf().setAppName("March_Madness_Predictions").setMaster("local[*]")
    val sc = new SparkContext(conf)

    val sqlContext = new org.apache.spark.sql.SQLContext(sc)

    val teamsDF = sqlContext.read.format("com.databricks.spark.csv").option("header", "true").option("inferSchema", "true").load("file:////Users/cahillt/Downloads/mm/Teams.csv")
    val seasonsDF = sqlContext.read.format("com.databricks.spark.csv").option("header", "true").option("inferSchema", "true").load("file:////Users/cahillt/Downloads/mm/Seasons.csv")
    val regularSeasonDetailedResultsDF = sqlContext.read.format("com.databricks.spark.csv").option("header", "true").option("inferSchema", "true").load("file:////Users/cahillt/Downloads/mm/RegularSeasonDetailedResults.csv")
    val tourneyDetailedResultsDF = sqlContext.read.format("com.databricks.spark.csv").option("header", "true").option("inferSchema", "true").load("file:////Users/cahillt/Downloads/mm/TourneyDetailedResults.csv")
    val tourneySlotsDF = sqlContext.read.format("com.databricks.spark.csv").option("header", "true").option("inferSchema", "true").load("file:////Users/cahillt/Downloads/mm/TourneySlots.csv")


     val regSeasonWin = regularSeasonDetailedResultsDF.select(col("Season"),col("Daynum"),col("Wteam") as "team", col("Wscore") as "score",
       col("Lteam") as "oppTeam",col("Lscore") as "oppScore",col("Wloc") as "loc", col("Numot"),col("Wfgm") as "fgm",
       col("Wfga") as "fga",col("Wfgm3") as "fgm3",col("Wfga3") as "fga3",col("Wftm") as "ftm",col("Wfta") as "fta",
       col("Wor") as "or",col("Wdr") as "dr",col("Wast") as "ast",col("Wto") as "to",col("Wstl") as "stl",
       col("Wblk") as "blk",col("Wpf") as "pf", col("Lfgm") as "oppfgm",col("Lfga") as "oppfga",col("Lfgm3") as "oppfgm3"
       ,col("Lfga3") as "oppfga3",col("Lftm") as "oppftm",col("Lfta") as "oppfta",col("Lor") as "oppor",col("Ldr") as "oppdr"
       ,col("Last") as "oppast",col("Lto") as "oppto",col("Lstl") as "oppstl",col("Lblk") as "oppblk", col("Lpf") as "opppf")

    val regSeasonLose = regularSeasonDetailedResultsDF.select(col("Season"),col("Daynum"),col("Lteam") as "team", col("Lscore") as "score",
      col("Wteam") as "oppTeam",col("Wscore") as "oppScore",col("Wloc") as "loc", col("Numot"),col("Lfgm") as "fgm",
      col("Lfga") as "fga",col("Lfgm3") as "fgm3",col("Lfga3") as "fga3",col("Lftm") as "ftm",col("Lfta") as "fta",
      col("Lor") as "or",col("Ldr") as "dr",col("Last") as "ast",col("Lto") as "to",col("Lstl") as "stl",
      col("Lblk") as "blk",col("Lpf") as "pf", col("Wfgm") as "oppfgm",col("Wfga") as "oppfga",col("Wfgm3") as "oppfgm3"
      ,col("Wfga3") as "oppfga3",col("Wftm") as "oppftm",col("Wfta") as "oppfta",col("Wor") as "oppor",col("Wdr") as "oppdr"
      ,col("Wast") as "oppast",col("Wto") as "oppto",col("Wstl") as "oppstl",col("Wblk") as "oppblk", col("Wpf") as "opppf")

    //joined team to wTeam or Lteam
    val joined = teamsDF
      .join(regSeasonWin, teamsDF("Team_Id") === regSeasonWin("team"))
        .unionAll(
          teamsDF.join(regSeasonLose,teamsDF("Team_Id") === regSeasonLose("team")))

    val teamsAgg = joined.
      groupBy("team", "Season")
      .agg(sum("score") as "score", sum("oppScore") as "oppScore", sum("Numot") as "numot", sum("fgm") as "fgm",
        sum("oppfgm") as "oppfgm", sum("fga") as "fga", sum("oppfga") as "oppfga", sum("fgm3") as "fgm3",
        sum("oppfgm3") as "oppfgm3", sum("fga3") as "fga3", sum("oppfga3") as "oppfga3", sum("ftm") as "ftm",
        sum("oppftm") as "oppftm", sum("fta") as "fta", sum("oppfta") as "oppfta", sum("or") as "or", sum("oppor") as "oppor",
        sum("dr") as "dr", sum("oppdr") as "oppdr", sum("ast") as "ast", sum("oppast") as "oppast", sum("to") as "to",
        sum("oppto") as "oppto", sum("stl") as "stl", sum("oppstl") as "oppstl", sum("blk") as "blk", sum("oppblk") as "oppblk",
        sum("pf") as "pf", sum("opppf") as "opppf")

    println(teamsDF.count())

    //regularSeasonDetailedResultsDF schema
    //Season,Daynum,Wteam,Wscore,Lteam,Lscore,Wloc,Numot,Wfgm,Wfga,Wfgm3,Wfga3,Wftm,Wfta,Wor,Wdr,Wast,Wto,Wstl,Wblk,Wpf,
    // Lfgm,Lfga,Lfgm3,Lfga3,Lftm,Lfta,Lor,Ldr,Last,Lto,Lstl,Lblk,Lpf
    //Shooting Efficency - eFG% = (FG + 0.5 x 3FG) / FGA
    val shootingEfficiency = udf((fgm: Int, fgm3: Int, fga: Int, fga3: Int) => (fgm + (0.5 * fgm3))/ (fga + fga3))

    //Turnover rate - TO% = turnovers/possessions
    val turnoverRate = udf((to:Int, possessions:Int) => to/possessions)

    //Offensive rebouding - OReb% = offensive rebounds / (offensive rebounds + opponent’s defensive rebounds)
    val offRebounding = udf((offRebounds:Int, oppDefRebounds:Int) => offRebounds/ (offRebounds + oppDefRebounds))

    //free throw conversion - FTM/FGA
    val freeThrowConversion = udf((ftm: Int, fga: Int) => ftm/fga)

    val teamsPrep = teamsAgg
      .withColumn("shootingEfficiency", shootingEfficiency(col("fgm"), col("fgm3"), col("fga"), col("fga3")))
      .withColumn("offRebounding", offRebounding(col("or"), col("oppdr")))
      .withColumn("freeThrowConversion", freeThrowConversion(col("ftm"), col("fta")))

    println(teamsPrep.count())


    val getTourneyParings =

  }

  def compareTeams(team1:Array[Float], team2:Array[Float], weights:Array[Float]) : Boolean => {


    false
  }
}
