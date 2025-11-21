package com.voting.domain

case class Election(
                     id: String,
                     description: String,
                     year: Int,
                     candidates: List[Candidate]
                   )