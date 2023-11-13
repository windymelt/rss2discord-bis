package io.github.windymelt.rss2discordbis

import scalajs.js.annotation.*
import api.v1._
import com.raquo.laminar.api.L.{*, given}
import org.scalajs.dom
import com.raquo.airstream.ownership.Owner
import com.raquo.airstream.ownership.ManualOwner
import scala.util.Random

package object components {
  case class ToastMessage(message: String, id: Option[Long] = None)
  def Toasts(messages: EventBus[ToastMessage]): Element = {
    val buffer = Var(List.empty[(String, Boolean, Long)])

    implicit val owner: Owner = ManualOwner()
    messages.events.foreach { m =>
      println(s"Toast: $m")
      buffer.update(_ :+ (m.message, true, m.id.getOrElse(Random.nextLong())))
    }

    div(
      cls := "toast-container position-fixed bottom-0 end-0 p-3",
      children <-- buffer.signal.map { messages =>
        messages.map { m =>
          Toast(
            m._1,
            Var(m._2),
            () => {
              buffer.update(_.filterNot(_._3 == m._3))
            }
          )
        }
      }
    )
  }

  private def Toast(
      message: String,
      isShown: Var[Boolean] = Var(true),
      onRemove: () => Unit
  ): Element = {
    def toastCls(b: Boolean) =
      if b then "toast show" else "toast"
    def onClickClose(e: dom.Event): Unit = {
      isShown.set(false)
      onRemove()
    }

    div(
      cls <-- isShown.signal.map(toastCls),
      role := "alert",
      aria.live := "assertive",
      aria.atomic := true,
      div(
        cls := "toast-header",
        // img(
        //   src := "...",
        //   cls := "rounded me-2",
        //   alt := "..."
        // ),
        strong(
          cls := "me-auto",
          "Notice"
        ),
        small(
          "11 mins ago"
        ),
        button(
          tpe := "button",
          cls := "btn-close",
          dataAttr("bs-dismiss") := "toast",
          aria.label := "Close",
          onClick --> onClickClose
        )
      ),
      div(
        cls := "toast-body",
        message
      )
    )
  }
}
