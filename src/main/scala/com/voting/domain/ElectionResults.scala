package com.voting.domain

case class ElectionResults(
                            electionId: String,
                            totalVotes: Long,
                            results: List[CandidateResult]
                          )

case class CandidateResult(
                            candidateId: String,
                            candidateName: String,
                            votes: Long
                          )