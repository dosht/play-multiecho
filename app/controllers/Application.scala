package controllers

import scala.collection.mutable
import play.api.libs.concurrent.Promise
import play.api.libs.iteratee.Concurrent
import play.api.libs.iteratee.Iteratee
import play.api.libs.json.JsValue
import play.api.mvc.Action
import play.api.mvc.Controller
import play.api.mvc.WebSocket
import scala.collection.mutable.{ Map => MutableMap }
import play.api.libs.iteratee.Enumerator

object Application extends Controller {
  /**
   * Create an enumerator and channel for broadcasting input to many iteratees.
   *
   * This is intended for imperative style push input feeding into iteratees.  For example:
   *
   * {{{
   * val (chatEnumerator, chatChannel) = Concurrent.broadcast[String]
   * val chatClient1 = Iteratee.foreach[String](m => println("Client 1: " + m))
   * val chatClient2 = Iteratee.foreach[String](m => println("Client 2: " + m))
   * chatEnumerator |>>> chatClient1
   * chatEnumerator |>>> chatClient2
   *
   * chatChannel.push(Message("Hello world!"))
   * }}}
   */
  //  case class Message(message: String)
  //  def echo2(clientId: String) = WebSocket.async[String] {
  //    request =>
  //      val (chatEnumerator, chatChannel) = Concurrent.broadcast[String]
  //      val chatClient1 = Iteratee.foreach[String](m => println("Client 1: " + m))
  //      chatEnumerator |>>> chatClient1
  //      chatChannel.push(Message("Hello world!"))
  //  }
  //  

  val rooms: MutableMap[String, Enumerator[JsValue]] = MutableMap.empty
  val chatChannels: MutableMap[String, Concurrent.Channel[JsValue]] = MutableMap.empty
  val roomsChannels: MutableMap[String, Seq[Concurrent.Channel[JsValue]]] = MutableMap.empty

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def echo(clientId: String) = WebSocket.async[JsValue] { request =>
    lazy val (out, chatChannel) = Concurrent.broadcast[JsValue]
    out run Iteratee.foreach { println }
    out >>> out
    chatChannels put (clientId, chatChannel)
    val in = Iteratee.foreach[JsValue](
      msg => {
        //    	chatChannel push msg
        chatChannels foreach { case (k, channel) => channel push msg }
      })

    Promise.pure((in, out))
  }

}