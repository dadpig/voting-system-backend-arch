package com.voting.repository

import com.voting.domain.{Candidate, CandidateResult, Election, ElectionResults, User, Vote}

import java.time.Instant
import scala.collection.concurrent.TrieMap

class InMemoryVotingRepository extends VotingRepository {
  private val votes = TrieMap[String, Vote]()
  private val elections = TrieMap[String, Election]()
  private val candidates = TrieMap[String, Candidate]()
  private val users = TrieMap[String, User]()
  private val userVotes = TrieMap[(String, String), Vote]() // (userId, electionId) -> Vote

  // Initialize sample data
  initializeSampleData()

  private def initializeSampleData(): Unit = {
    // Create candidates
    val candidatesList = List(
      Candidate("c1", "Someguy 1", "Progressive Party", 10),
      Candidate("c2", "Otherguy 2", "Democratic Union", 20),
      Candidate("c3", "No one special 1", "Social Reform", 30),
      Candidate("c4", "Just another candidate", "Green Alliance", 40),
      Candidate("c5", "Fulano probably will win", "People's Front", 50)
    )
    candidatesList.foreach(c => candidates.put(c.id, c))

    // Create election
    val election = Election(
      id = "2025-national",
      description = "Presidential election",
      2025,
      candidates = candidatesList,
    )
    elections.put(election.id, election)

    // Create sample users
    (1 to 1000).foreach { i =>
      val user = User(
        id = s"user_$i",
        name = s"User $i",
        email = s"user$i@example.com",
        password = s"user_$i".hashCode().toString
      )
      users.put(user.id, user)
    }
  }

  override def saveVote(vote: Vote): Either[String, Vote] = {
    val key = (vote.userId, vote.electionId)

    if (userVotes.contains(key)) {
      Left("User has already voted in this election")
    } else {
      votes.put(vote.id, vote)
      userVotes.put(key, vote)
      Right(vote)
    }
  }

  override def findVoteByUserAndElection(userId: String, electionId: String): Option[Vote] = {
    userVotes.get((userId, electionId))
  }

  override def getElectionResults(electionId: String): ElectionResults = {
    val electionVotes = votes.values.filter(_.electionId == electionId).toList
    val totalVotes = electionVotes.size.toLong

    val voteCounts = electionVotes
      .groupBy(_.candidateId)
      .view
      .mapValues(_.size.toLong)
      .toMap

    val results = candidates.values
      .filter(c => elections.get(electionId).exists(_.candidates.contains(c)))
      .map { candidate =>
        val votes = voteCounts.getOrElse(candidate.id, 0L)
        CandidateResult(candidate.id, candidate.name, votes)
      }
      .toList
      .sortBy(-_.votes)

    ElectionResults(electionId, totalVotes, results)
  }

  override def findElection(electionId: String): Option[Election] = elections.get(electionId)

  override def findCandidate(candidateId: String): Option[Candidate] = candidates.get(candidateId)

  override def findUser(userId: String): Option[User] = users.get(userId)

  
}
