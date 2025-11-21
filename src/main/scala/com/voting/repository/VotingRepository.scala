package com.voting.repository

import com.voting.domain.*

trait VotingRepository {
  
  def saveVote(vote: Vote): Either[String, Vote]

  def findVoteByUserAndElection(userId: String, electionId: String): Option[Vote]

  def getElectionResults(electionId: String): ElectionResults

  def findElection(electionId: String): Option[Election]

  def findCandidate(candidateId: String): Option[Candidate]

  def findUser(userId: String): Option[User]
}
