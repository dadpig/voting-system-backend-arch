package com.voting.service

import com.voting.domain.{Candidate, Election, ElectionResults, User, Vote, VoteRequest, VoteResponse}
import com.voting.repository.VotingRepository

import java.time.Instant
import java.util.UUID


class VotingService(repository: VotingRepository) {

  def castVote(request: VoteRequest): Either[String, VoteResponse] = {
    for {
      _ <- validateUser(request.userId)
      _ <- validateCandidate(request.candidateId, request.electionId)
      _ <- checkIfAlreadyVoted(request.userId, request.electionId)
      vote <- createAndSaveVote(request)
    } yield VoteResponse(
      voteId = vote.id,
      message = "Vote successfully recorded",
      timestamp = vote.timestamp
    )
  }

  def getResults(electionId: String): Either[String, ElectionResults] = {
    repository.findElection(electionId) match {
      case Some(_) => Right(repository.getElectionResults(electionId))
      case None => Left("Election not found")
    }
  }

  def getUserVote(userId: String, electionId: String): Either[String, Vote] = {
    repository.findVoteByUserAndElection(userId, electionId) match {
      case Some(vote) => Right(vote)
      case None => Left("No vote found for this user in this election")
    }
  }

  private def validateUser(userId: String): Either[String, User] = {
    repository.findUser(userId).toRight("User not found")
  }



  private def validateCandidate(candidateId: String, electionId: String): Either[String, Candidate] = {
    for {
      candidate <- repository.findCandidate(candidateId).toRight("Candidate not found")
      election <- repository.findElection(electionId).toRight("Election not found")
      _ <- Either.cond(
        election.candidates.contains(candidate),
        candidate,
        "Candidate not part of this election"
      )
    } yield candidate
  }

  private def checkIfAlreadyVoted(userId: String, electionId: String): Either[String, Unit] = {
    repository.findVoteByUserAndElection(userId, electionId) match {
      case Some(_) => Left("User has already voted in this election")
      case None => Right(())
    }
  }

  private def createAndSaveVote(request: VoteRequest): Either[String, Vote] = {
    val vote = Vote(
      id = UUID.randomUUID().toString,
      userId = request.userId,
      candidateId = request.candidateId,
      electionId = request.electionId,
      timestamp = Instant.now()
    )
    repository.saveVote(vote)
  }
}
