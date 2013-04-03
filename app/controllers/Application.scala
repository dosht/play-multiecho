package controllers

import play.api._
import play.api.mvc._
import play.api.libs.json.{ JsValue, JsObject }
import play.api.libs.iteratee.{ Enumerator, Iteratee }
import play.api.libs.concurrent.Promise
import play.api.libs.iteratee.Concurrent

object Application extends Controller {

  val hubEnum = Enumerator.imperative[JsValue]()
  val hub = Concurrent.hub[JsValue](hubEnum)

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def echo(clientId: String) = WebSocket.async[JsValue] { request =>
    println(clientId)
    val out = hub.getPatchCord
    val in = Iteratee.foreach[JsValue](
      x => {
        println(s"message rcv: $clientId")
        x match {
          case message: JsObject => message.keys.foreach(
            k => k match {
              case "message" => hubEnum push message // Store message
              case "clear" => hubEnum push message // delete messages
            })
        }
      })

    Promise.pure((in, out))
  }

}