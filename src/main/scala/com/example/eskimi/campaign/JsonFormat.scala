package com.example.eskimi.campaign
import com.example.eskimi.campaign.CampaignRepository._
import spray.json._

object JsonFormat {
 import DefaultJsonProtocol._
 implicit val geoFormat = jsonFormat1(Geo)
 implicit val deviceFormat = jsonFormat2(Device)
 implicit val userFormat = jsonFormat2(User)
 implicit val siteFormat = jsonFormat2(Site)
 implicit val impressionFormat = jsonFormat8(Impression)
 implicit val bidRequestFormat = jsonFormat5(BidRequest)


 implicit val bannerFormat = jsonFormat4(Banner)
 implicit val bidResponseFormat = jsonFormat5(BidResponse)
}