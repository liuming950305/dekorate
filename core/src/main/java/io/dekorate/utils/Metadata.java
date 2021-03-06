/**
 * Copyright 2018 The original authors.
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
package io.dekorate.utils;

import io.dekorate.deps.kubernetes.api.builder.Builder;
import io.dekorate.deps.kubernetes.api.builder.Predicate;

import io.dekorate.deps.kubernetes.api.builder.VisitableBuilder;
import io.dekorate.deps.kubernetes.api.model.HasMetadata;
import io.dekorate.deps.kubernetes.api.model.ObjectMeta;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

public class Metadata {

  public static Optional<ObjectMeta> getMetadata(Builder builder) {
    try {
      Method method = builder.getClass().getMethod("buildMetadata");
      Object o = method.invoke(builder);
      if (o instanceof ObjectMeta) {
        return Optional.of((ObjectMeta)o);
      }
    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
      //ignore
    }
    return Optional.empty();
  }

  /**
   * Create a {@link Predicate} that checks that a resource builder doesn't match the name and kind.
   * @param HasMetadata The specified resource.
   * @return The predicate.
   */
  public static Predicate<VisitableBuilder<? extends HasMetadata, ?>> matching(HasMetadata candidate) {
    return matching(candidate.getApiVersion(), candidate.getKind(), candidate.getMetadata().getName());
  }

  /**
   * Create a {@link Predicate} that checks that a resource builder doesn't match the name and kind.
   * @param kind The specified kind.
   * @param name The specified name.
   * @return The predicate.
   */
  public static Predicate<VisitableBuilder<? extends HasMetadata, ?>> matching(String apiVersion, String kind, String name) {
    return new Predicate<VisitableBuilder<? extends HasMetadata, ?>>() {
      @Override
      public Boolean apply(VisitableBuilder<? extends HasMetadata, ?> builder) {
        HasMetadata item = builder.build();
        ObjectMeta metadata = item.getMetadata();
        return apiVersion.equals(item.getApiVersion()) &&
          kind != null && kind.equals(item.getKind()) &&
          name != null && name.equals(metadata.getName());
      }
    };
  }
}
