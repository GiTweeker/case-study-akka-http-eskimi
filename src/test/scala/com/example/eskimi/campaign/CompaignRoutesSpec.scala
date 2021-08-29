package com.example.eskimi.campaign

//#campaign-routes-spec
//#test-top
import akka.actor.testkit.typed.scaladsl.ActorTestKit
import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model._
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.example.eskimi.campaign.CampaignRepository._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

//#set-up
class CompaignRoutesSpec extends AnyWordSpec with Matchers with ScalaFutures with ScalatestRouteTest {
  //#test-top

  // the Akka HTTP route testkit does not yet support a typed actor system (https://github.com/akka/akka-http/issues/2036)
  // so we have to adapt for now
  lazy val testKit = ActorTestKit()
  implicit def typedSystem = testKit.system
  override def createActorSystem(): akka.actor.ActorSystem =
    testKit.system.classicSystem

  // Here we need to implement all the abstract members of CampaignRoutes.
  // We use the real CampaignRegistryActor to test it while we hit the Routes,
  // but we could "mock" it by implementing it in-place or by using a TestProbe
  // created with testKit.createTestProbe()
  val campaignRegistry = testKit.spawn(CampaignRepository(CampaignRepository.activeCampaigns))

  lazy val routes = new CampaignRoutes(campaignRegistry).theCampaignRoutes

  // use the json formats to marshal and unmarshall objects in the test
  import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
  import com.example.eskimi.campaign.JsonFormat._
  //#set-up



    //#testing-post
    "Should return No Content when search match returns empty (POST /campaigns)" in {

      //id: String, imp: Option[List[Impression]], site: Site, user: Option[User], device: Option[Device]
      //	"id": "1",
      //  	"wmin": 50,
      //  	"wmax": 300,
      //  	"hmin": 100,
      //  	"hmax": 300,
      //  	"h": 250,
      //  	"w": 300,
      //  	"bidFloor": 3.121232
      val bidRequest = BidRequest("24",Some(
        List(Impression(
        "2",
          Option(700),Option(1000),Option(800),
          Option(200),Option(400),Option(399),
          Option(8d)
      ))),
        Site("7006a522ce0f4bbbbaa6b3c38cafaa0f","note.fake.id"),
        Option(CampaignRepository.User("JohnDoe",Option(CampaignRepository.Geo(Option("NG"))))),
        Some(Device(
          "840579f4b408831516ebd02f6e1c31b42",
          Option(Geo(
            Option("NG")
          ))
        ))


      )


      val bidRequestEntity = Marshal(bidRequest).to[MessageEntity].futureValue // futureValue is from ScalaFutures

      // using the RequestBuilding DSL:
      val request = Post("/campaigns").withEntity(bidRequestEntity)

      request ~> routes ~> check {
        status should ===(StatusCodes.NoContent)

      }
    }
    //#testing-post ( query for campaign)


  //#actual-test

  //#set-up
}
//#set-up
//#campaign-routes-spec
