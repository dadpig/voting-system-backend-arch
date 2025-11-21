
package com.voting

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import com.voting.api.VotingRoutes
import com.voting.repository.InMemoryVotingRepository
import com.voting.service.VotingService
import scala.concurrent.ExecutionContextExecutor
import scala.util.{Failure, Success}

object Main {
  def main(args: Array[String]): Unit = {
    implicit val system: ActorSystem[Nothing] = ActorSystem(Behaviors.empty, "voting-system")
    implicit val executionContext: ExecutionContextExecutor = system.executionContext

    val repository = new InMemoryVotingRepository()
    val service = new VotingService(repository)
    val routes = new VotingRoutes(service).routes

    val bindingFuture = Http().newServerAt("0.0.0.0", 8080).bind(routes)

    bindingFuture.onComplete {
      case Success(binding) =>
        val address = binding.localAddress
        system.log.info(s"Server online at http://${address.getHostString}:${address.getPort}/")
      case Failure(ex) =>
        system.log.error("Failed to bind HTTP endpoint, terminating system", ex)
        system.terminate()
    }
  }
}