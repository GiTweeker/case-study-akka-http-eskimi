package com.example.eskimi.campaign

import akka.actor.typed.{ActorRef, Behavior}
import akka.actor.typed.scaladsl.Behaviors
import scala.util.Random
object CampaignRepository {
  final case class Item(name: String, id: Long)
  // Trait defining successful and failure responses
  sealed trait Response

  case class Found(bidResponse: BidResponse) extends Response

  case object NoContent extends Response

  case class Campaign(id: Int, country: String, targeting: Targeting, banners: List[Banner], bid: Double)

  // proper type for targetedSiteIds
  case class Targeting(targetedSiteIds: Seq[String])

  case class Banner(id: Int, src: String, width: Int, height: Int)

  final case class BidRequest(id: String, imp: Option[List[Impression]], site: Site, user: Option[User], device: Option[Device])

  case class Impression(id: String, wmin: Option[Int], wmax: Option[Int], w: Option[Int],
                        hmin: Option[Int], hmax: Option[Int], h: Option[Int], bidFloor: Option[Double])

  case class Site(id: String, domain: String)

  case class User(id: String, geo: Option[Geo])

  case class Device(id: String, geo: Option[Geo])

  case class Geo(country: Option[String])


  case class BidResponse(id: String, bidRequestId: String, price: Double, adid: Option[String], banner: Option[Banner]) extends Response

  sealed trait Command

  final case class SearchCampaigns(bidRequest: BidRequest, replyTo: ActorRef[Response]) extends Command

  def getBanner(impressions: Option[List[Impression]], banners: List[Banner] ) : Option[Banner] = {

    /* the width and height of the campaign banner must be within the range (hmin, hmax and wmin , wmax)
      * of the bid request
      * for this purpose.. I am validating only the last impression
      * */
    val impression = impressions.get.last

     Option(banners.filter(b=>{
      b.width.>=(impression.wmin.get) && b.width.<=(impression.wmax.get)  && b.height.>=(impression.hmin.get)  && b.height.<=(impression.hmax.get)
    }).last)

  }
  def getBidResponse( campaigns: Seq[Campaign], bidRequest:BidRequest ) : Option[BidResponse] = {

    val random = new Random
    /*
  * Bid Request Target Site Ids must be contained in the campaign target id
  * and validate country device
  *
  * */

    val bidResponses = campaigns.filter(c=>{
      c.targeting.targetedSiteIds.contains(bidRequest.site.id) && c.country.equalsIgnoreCase(bidRequest.device.get.geo.get.country.get)

    }).map(c=>{

      val banner =  getBanner(bidRequest.imp,c.banners).get


      BidResponse(
        "response1",
        bidRequest.id,
        bidRequest.imp.get.last.bidFloor.get,
        Option(bidRequest.imp.get.last.id),
        Option(banner))
    })
    if(bidResponses.isEmpty){
      return None
    }
    //Get Random Bid Responses of the campaign
    Option(
      bidResponses(random.nextInt(bidResponses.length))
    )


  }

  val activeCampaigns : Seq[Campaign] = Seq(
    Campaign(
      id = 1,
      country = "LT",
      targeting = Targeting(
        targetedSiteIds = Seq("0006a522ce0f4bbbbaa6b3c38cafaa0f")
      ),
      banners = List(
        Banner(
          id = 1,
          src = "https://business.eskimi.com/wp-content/uploads/2020/06/openGraph.jpeg",
          width = 300,
          height = 250
        )
      ),
      bid = 5d
    )
  )


  // This behavior handles all possible incoming messages and keeps the state in the function parameter
  def apply(campaigns: Seq[Campaign] = Seq.empty[Campaign]): Behavior[Command] = Behaviors.receiveMessage {
/*    case SearchCampaigns(bidRequest, replyTo) if getBidResponse(campaigns,bidRequest).isEmpty =>
      replyTo ! NoContent
      Behaviors.same*/
    case SearchCampaigns(bidRequest, replyTo) =>
      val bidResponse  = getBidResponse(campaigns,bidRequest)
      if(bidResponse.isEmpty){
        replyTo ! NoContent
      }else{
        replyTo ! Found(getBidResponse(campaigns,bidRequest).get)
      }

      Behaviors.same

  }
}
