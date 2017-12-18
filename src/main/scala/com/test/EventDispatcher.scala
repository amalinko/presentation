package com.test

import scala.concurrent.Future

trait Event

class EventDispatcher {
  def dispatch(event: Event): Future[Unit] = ???
}
