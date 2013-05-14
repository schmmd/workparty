package edu.knowitall.workparty

import java.io.File
import scala.io.Source
import java.io.FileNotFoundException
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import org.slf4j.LoggerFactory
import scala.collection.immutable.Queue

object Scheduler extends App {
  val logger = LoggerFactory.getLogger(this.getClass)

  case class Config(nodes: Seq[String] = Seq.empty, work: Seq[String] = Seq.empty)

  val argumentParser = new scopt.immutable.OptionParser[Config]("workparty") {
    def options = Seq(
      arg("work", "file describing work") { (workFilePath, config) =>
        val file = new File(workFilePath)
        import resource._

        val work = for (source <- managed(Source.fromFile(file))) yield {
          source.getLines.toList
        }

        config.copy(work = work.opt.getOrElse(throw new FileNotFoundException))
      },
      arg("nodes", "file describing nodes") { (nodesFilePath, config) =>
        val file = new File(nodesFilePath)
        import resource._

        val nodes = for (source <- managed(Source.fromFile(file))) yield {
          source.getLines.toList
        }

        config.copy(nodes = nodes.opt.getOrElse(throw new FileNotFoundException))
      })
  }

  argumentParser.parse(args, Config()) match {
    case Some(config) => run(config)
    case None =>
  }

  def run(config: Config) = {
    import resource._

    val workQueue = Queue.empty[String] ++ config.work
    val nodes = new ArrayBlockingQueue[String](config.nodes.size)
    config.nodes.foreach(node => nodes.add(node))

    if (config.work.isEmpty) logger.info("Scheduler started with no work")
    if (nodes.isEmpty) logger.info("Scheduler started with no nodes")

    for (work <- workQueue) {
      val node = nodes.poll(2, TimeUnit.DAYS)

      logger.info("Distributing to node '" + node + "': " + work)
      val future = Future {
        import scala.sys.process._
        val ec = Seq("ssh", node, work) !

        if (ec == 0) logger.info(s"Node '$node' completed successfully: $work")
        else logger.warn(s"Node '$node' failed ($ec): $work")

        nodes.synchronized {
          nodes.offer(node)
        }
      }
    }

    logger.info("All work complete.")
  }
}