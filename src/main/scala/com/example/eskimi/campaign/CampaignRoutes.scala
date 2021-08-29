package com.example.eskimi.campaign

import akka.actor.typed.{ActorRef, ActorSystem}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{as, complete, concat, entity, onSuccess, pathEnd, pathPrefix, post}
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import com.example.JsonFormats

import scala.concurrent.Future
import scala.concurrent.duration._

class CampaignRoutes(campaignRepository: ActorRef[CampaignRepository.Command])(implicit system: ActorSystem[_]) {
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import com.example.eskimi.campaign.JsonFormat._
  import akka.actor.typed.scaladsl.AskPattern.{Askable, schedulerFromActorSystem}

  // asking someone requires a timeout and a scheduler, if the timeout hits without response
  // the ask is failed with a TimeoutException
  implicit val timeout: Timeout = 3.seconds
  lazy val theCampaignRoutes: Route =
    pathPrefix("campaigns") {
      concat(
        pathEnd {
          concat(
            post {
              entity(as[CampaignRepository.BidRequest]) { bidRequest =>
                val operationPerformed: Future[CampaignRepository.Response] =
                  campaignRepository.ask(CampaignRepository.SearchCampaigns(bidRequest, _))
                onSuccess(operationPerformed) {
                  case CampaignRepository.Found(bidResponse) =>  complete(StatusCodes.OK, bidResponse)
                  case CampaignRepository.NoContent => complete(StatusCodes.NoContent)
                }
              }
            }
          )
        }
      )
    }
}
