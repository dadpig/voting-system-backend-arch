package com.voting.api

import com.voting.domain.{Candidate, CandidateResult, Election, ElectionResults, User, Vote, VoteRequest, VoteResponse}

import java.time.Instant


trait JsonProtocol extends SprayJsonSupport with DefaultJsonProtocol {

  implicit val instantFormat: RootJsonFormat[Instant] = new RootJsonFormat[Instant] {
    override def write(obj: Instant): JsValue = JsString(obj.toString)
    override def read(json: JsValue): Instant = json match {
      case JsString(s) => Instant.parse(s)
      case _ => throw DeserializationException("Expected ISO-8601 timestamp")
    }
  }

  implicit val candidateFormat: RootJsonFormat[Candidate] = jsonFormat5(Candidate.apply)
  implicit val userFormat: RootJsonFormat[User] = jsonFormat5(User.apply)

  implicit val electionStatusFormat: RootJsonFormat[ElectionStatus] = new RootJsonFormat[ElectionStatus] {
    override def write(obj: ElectionStatus): JsValue = JsString(obj.toString)
    override def read(json: JsValue): ElectionStatus = json match {
      case JsString("Scheduled") => ElectionStatus.Scheduled
      case JsString("Active") => ElectionStatus.Active
      case JsString("Closed") => ElectionStatus.Closed
      case _ => throw DeserializationException("Invalid election status")
    }
  }

  implicit val electionFormat: RootJsonFormat[Election] = jsonFormat6(Election.apply)
  implicit val voteFormat: RootJsonFormat[Vote] = jsonFormat5(Vote.apply)
  implicit val voteRequestFormat: RootJsonFormat[VoteRequest] = jsonFormat3(VoteRequest.apply)
  implicit val voteResponseFormat: RootJsonFormat[VoteResponse] = jsonFormat3(VoteResponse.apply)
  implicit val candidateResultFormat: RootJsonFormat[CandidateResult] = jsonFormat4(CandidateResult.apply)
  implicit val electionResultsFormat: RootJsonFormat[ElectionResults] = jsonFormat3(ElectionResults.apply)
}