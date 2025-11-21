package com.voting.api

import com.voting.domain.VoteRequest
import com.voting.service.VotingService


class VotingRoutes(votingService: VotingService) extends JsonProtocol {

  val routes: Route = pathPrefix("api" / "v1") {
    concat(
      castVoteRoute,
      getResultsRoute,
      getUserVoteRoute,
      healthRoute
    )
  }

  private def castVoteRoute: Route = path("votes") {
    post {
      entity(as[VoteRequest]) { request =>
        votingService.castVote(request) match {
          case Right(response) => complete(StatusCodes.Created, response)
          case Left(error) => complete(StatusCodes.Conflict, Map("error" -> error))
        }
      }
    }
  }

  private def getResultsRoute: Route = path("elections" / Segment / "results") { electionId =>
    get {
      votingService.getResults(electionId) match {
        case Right(results) => complete(StatusCodes.OK, results)
        case Left(error) => complete(StatusCodes.NotFound, Map("error" -> error))
      }
    }
  }

  private def getUserVoteRoute: Route = path("votes" / "me") {
    get {
      parameters("userId", "electionId") { (userId, electionId) =>
        votingService.getUserVote(userId, electionId) match {
          case Right(vote) => complete(StatusCodes.OK, vote)
          case Left(error) => complete(StatusCodes.NotFound, Map("error" -> error))
        }
      }
    }
  }

  private def healthRoute: Route = path("health") {
    get {
      complete(StatusCodes.OK, Map("status" -> "healthy", "service" -> "voting-system"))
    }
  }
}