/*
 * Copyright 2011 WorldWide Conferencing, LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package code.form

import net.liftweb.common.{Empty, Full, Box, Failure}

class FormValue[T](val enteredValue:String, val value: Box[T])(implicit val converter: ValueConverter[T]) {
  def this()(implicit converter: ValueConverter[T]) = this("", Empty)(converter)
  def this(value: Box[T])(implicit converter: ValueConverter[T]) = this("", value)(converter)
  def this(enteredValue: String)(implicit converter: ValueConverter[T]) =
    this(enteredValue, FormValue.calcValue(converter, enteredValue))

  def asString: String =
    value match {
      case Full(v) => converter.asString(v)
      case Empty => enteredValue
      case Failure(_, _, _) => enteredValue
    }

  def valueAsString: Box[String] =
    value match {
      case Full(v) => Full(converter.asString(v))
      case Empty => Empty
      case f:Failure => f
    }

  def isEmpty = value == Empty
}

object FormValue {
  private def calcValue[T](converter: ValueConverter[T], s: String): Box[T] = {
    if (s == null)
      Empty
    else {
      val trimmed = s.trim
      if (trimmed.length == 0)
        Empty
      else
        converter.fromString(s)
    }
  }
}

