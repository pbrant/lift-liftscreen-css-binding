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

import net.liftweb.util.FieldError
import net.liftweb.http.AbstractScreen
import net.liftweb.common.{Full, Empty, Box, Failure}

trait ScreenValidations {
  self: AbstractScreen =>

  def valConversion[T](): FormValue[T] => List[FieldError] = formValue =>
    formValue.value match {
      case Failure(message, _, _) => message
      case _ => Nil
    }

  def valRequiredIfOtherDefined[T](otherField: Field{type ValueType=FormValue[T]}): FormValue[_] => List[FieldError] =
    formValue =>
      if (formValue.value == Empty && otherField.get.value != Empty)
        "This field is required"
      else
        Nil

  def valMinLen(minLen: Int): FormValue[String] => List[FieldError] = formValue =>
    formValue.value match {
      case Full(s) if (s.length < minLen) => "This field must be at least %s characters long" format (minLen)
      case _ => Nil
    }

  val WebUrlPattern = """([a-zA-Z0-9\-\.\/]+)""".r

  def valIsWebUrl: FormValue[String] => List[FieldError] = formValue =>
    formValue.value match {
      case Full(WebUrlPattern(x)) => Nil
      case _ => "This field must be a web address (example: google.com)"
    }

  def valRequired[T](): FormValue[T] => List[FieldError] = formValue =>
    if (formValue.isEmpty)
      "This field is required"
    else
      Nil

  def valCountyRequired(): Int => List[FieldError] = formValue =>
    if (formValue == 0)
      "This field is required"
    else
      Nil
}
