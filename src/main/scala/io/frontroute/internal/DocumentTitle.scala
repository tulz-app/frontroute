package io.frontroute.internal

object DocumentTitle {

  def updateTitle(
    title: String,
    updateTitleElement: Boolean = true,
    ignoreEmptyTitle: Boolean = false
  ): Unit = {
    if (title.nonEmpty || !ignoreEmptyTitle) {
      org.scalajs.dom.document.title = title
      if (updateTitleElement) {
        val titleElement = org.scalajs.dom.document.head.querySelector("title")
        titleElement.textContent = title
      }
    }
  }

}
