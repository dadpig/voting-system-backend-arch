package com.voting.domain

import java.time.Instant

case class Vote(
                 id: String,
                 userId: String,
                 candidateId: String,
                 electionId: String,
                 timestamp: Instant = Instant.now()
               )


case class VoteRequest(
                        userId: String,
                        candidateId: String,
                        electionId: String
                      )

case class VoteResponse(
                         voteId: String,
                         message: String,
                         timestamp: Instant
                       )