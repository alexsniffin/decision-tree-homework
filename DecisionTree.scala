import scala.collection.mutable

object WeatherField extends Enumeration {
    type WeatherField = Value
    val OUTLOOK, TEMP, HUMIDITY, WINDY = Value
}

case class WeatherData(outlook: String, temp: String, humidity: String, windy: String, play: Boolean)

object WeatherTreeLogic {
    val fixture = List(
        WeatherData("sunny", "hot", "high", "true", play = false),
        WeatherData("sunny", "hot", "high", "false", play = true),
        WeatherData("overcast", "mild", "high", "false", play = true),
        WeatherData("rainy", "cool", "normal", "false", play = true),
        WeatherData("rainy", "cool", "normal", "true", play = false),
        WeatherData("rainy", "cool", "normal", "true", play = true),
        WeatherData("overcast", "mild", "high", "false", play = false),
        WeatherData("sunny", "cool", "normal", "false", play = true),
        WeatherData("sunny", "mild", "normal", "false", play = true),
        WeatherData("rainy", "mild", "normal", "true", play = true),
        WeatherData("sunny", "mild", "high", "true", play = true),
        WeatherData("overcast", "hot", "normal", "false", play = true),
        WeatherData("overcast", "mild", "high", "true", play = true),
        WeatherData("rainy", "mild", "high", "true", play = false)
    )

    def printTree(weatherData: Seq[WeatherData]): Unit = {

        def calc(weatherData: Seq[WeatherData]): Unit = {
            val ynTuple = weatherData.foldLeft((0.0, 0.0))((x, y) => if (y.play) (x._1 + 1, x._2) else (x._1, x._2 + 1))

            val infoAll = DecisionTree.info(ynTuple._1, ynTuple._2)

            val largestField = getLargestGain(weatherData, infoAll)
            val createSubSets = subsetFields(largestField, weatherData)

            println(s"${largestField.toString} with ${createSubSets.keys.toString()}")

            createSubSets.foreach(x => if (x._2.size > 1) calc(x._2))
        }

        calc(weatherData)
    }

    def subsetFields(field: WeatherField.Value, weatherData: Seq[WeatherData]) = field match {
        case WeatherField.OUTLOOK => weatherData.groupBy(x => x.outlook)
        case WeatherField.TEMP => weatherData.groupBy(x => x.temp)
        case WeatherField.HUMIDITY => weatherData.groupBy(x => x.humidity)
        case WeatherField.WINDY => weatherData.groupBy(x => x.windy)
    }

    def getLargestGain(weatherData: Seq[WeatherData], infoAll: Double): WeatherField.Value = {
        val outlookGain = infoAll - DecisionTree.gain(weatherData.foldLeft[mutable.Map[String, (Double, Double)]](mutable.Map[String, (Double, Double)]())((x, y) => {
            if (x.contains(y.outlook))
                if (y.play) x(y.outlook) = (x(y.outlook)._1 + 1, x(y.outlook)._2)
                else x(y.outlook) = (x(y.outlook)._1, x(y.outlook)._2 + 1)
            else x += (y.outlook -> (0.0, 0.0))
            x
        }).values.toList)

        val humidityGain = infoAll - DecisionTree.gain(weatherData.foldLeft[mutable.Map[String, (Double, Double)]](mutable.Map[String, (Double, Double)]())((x, y) => {
            if (x.contains(y.humidity))
                if (y.play) x(y.humidity) = (x(y.humidity)._1 + 1, x(y.humidity)._2)
                else x(y.humidity) = (x(y.humidity)._1, x(y.humidity)._2 + 1)
            else x += (y.humidity -> (0.0, 0.0))
            x
        }).values.toList)

        val tempGain = infoAll - DecisionTree.gain(weatherData.foldLeft[mutable.Map[String, (Double, Double)]](mutable.Map[String, (Double, Double)]())((x, y) => {
            if (x.contains(y.temp))
                if (y.play) x(y.temp) = (x(y.temp)._1 + 1, x(y.temp)._2)
                else x(y.temp) = (x(y.temp)._1, x(y.temp)._2 + 1)
            else x += (y.temp -> (0.0, 0.0))
            x
        }).values.toList)

        val windyGain = infoAll - DecisionTree.gain(weatherData.foldLeft[mutable.Map[String, (Double, Double)]](mutable.Map[String, (Double, Double)]())((x, y) => {
            if (x.contains(y.windy))
                if (y.play) x(y.windy) = (x(y.windy)._1 + 1, x(y.windy)._2)
                else x(y.windy) = (x(y.windy)._1, x(y.windy)._2 + 1)
            else x += (y.windy -> (0.0, 0.0))
            x
        }).values.toList)

        if (outlookGain < humidityGain && outlookGain < tempGain && outlookGain < windyGain) WeatherField.OUTLOOK
        else if (humidityGain < outlookGain && humidityGain < tempGain && humidityGain < windyGain) WeatherField.HUMIDITY
        else if (tempGain < outlookGain && tempGain < humidityGain && tempGain < windyGain) WeatherField.TEMP
        else WeatherField.WINDY
    }
}

object DecisionTree {
    def log2(x: Double) = Math.log10(x) / Math.log10(2.0)

    def info(yes: Double, no: Double): Double = {
        if (yes == 0) return 1
        else if (no == 0) return 0

        val yesProb = yes / (yes + no)
        val noProb = no / (yes + no)

        val yesLog = -yesProb * log2(yesProb)
        val noLog = -noProb * log2(noProb)

        yesLog + noLog
    }

    def entropy(probabilityDistribution: Seq[(Double, Double)], total: Double): Double =
        probabilityDistribution.foldRight[Double](0)((x, y) => y + (x._1 + x._2) / total * info(x._1, x._2))

    def gain(probabilityDistribution: Seq[(Double, Double)]): Double = {
        val infoAll = probabilityDistribution.foldRight[(Double, Double)]((0, 0))((x, y) => (y._1 + x._1, y._2 + x._2))
        val total = infoAll._1 + infoAll._2

        info(infoAll._1, infoAll._2) - entropy(probabilityDistribution, total)
    }
}
