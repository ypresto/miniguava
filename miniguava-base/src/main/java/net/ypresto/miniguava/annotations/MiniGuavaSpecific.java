/*
 * Copyright (C) 2016 Yuya Tanaka
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.ypresto.miniguava.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks APIs (public class, method or field) or internal code is added by Mini Guava
 * and is not exists in original Google Guava.
 *
 * Please place this annotation on all non-private things and private classes or methods.
 *
 * @author Yuya Tanaka
 */
@Retention(RetentionPolicy.CLASS)
@Target({
    ElementType.ANNOTATION_TYPE,
    ElementType.CONSTRUCTOR,
    ElementType.FIELD,
    ElementType.METHOD,
    ElementType.TYPE})
@Documented
public @interface MiniGuavaSpecific {
  Reason value() default Reason.NOT_EXISTS;
  String from() default "";

  enum Reason {
    NOT_EXISTS,
    NOT_PUBLIC,
    MOVED,
    COPIED;
  }
}
