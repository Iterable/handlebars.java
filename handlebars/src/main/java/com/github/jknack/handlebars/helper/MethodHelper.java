/*
 * Handlebars.java: https://github.com/jknack/handlebars.java
 * Apache License Version 2.0 http://www.apache.org/licenses/LICENSE-2.0
 * Copyright (c) 2012 Edgar Espina
 */
package com.github.jknack.handlebars.helper;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.Helper;
import com.github.jknack.handlebars.Options;

/**
 * Wrap a method as Handlebars helper.
 *
 * @author edgar.espina
 * @see Handlebars#registerHelpers(Object)
 * @see Handlebars#registerHelpers(Class)
 */
public class MethodHelper implements Helper<Object> {

  /** No args. */
  private static final Object[] NO_ARGS = new Object[0];

  /** The source or instance object. Might be null. */
  private Object source;

  /** The method to invoke. Required. */
  private Method method;

  /**
   * Creates a new {@link MethodHelper}.
   *
   * @param method The method to invoke. Required.
   * @param source The source or instance object. Might be null.
   */
  public MethodHelper(final Method method, final Object source) {
    this.method = notNull(method, "A helper method is required.");
    this.source = source;
  }

  @Override
  public Object apply(final Object context, final Options options) throws IOException {
    try {
      Class<?>[] paramTypes = method.getParameterTypes();
      Object[] args = NO_ARGS;
      if (paramTypes.length > 0) {
        args = new Object[paramTypes.length];
        args[0] = paramTypes[0] == Options.class ? options : context;
        for (int i = 1; i < args.length; i++) {
          if (paramTypes[i] == Options.class) {
            args[i] = options;
          } else {
            args[i] = options.param(i - 1, null);
          }
        }
      }
      return method.invoke(source, args);
    } catch (ArrayIndexOutOfBoundsException ex) {
      throw new IllegalArgumentException(
          "could not execute helper: "
              + toString(method)
              + ", with the given arguments: "
              + toString(options.params),
          ex);
    } catch (InvocationTargetException ex) {
      throw launderThrowable(ex.getCause());
    } catch (IllegalAccessException ex) {
      throw new IllegalStateException("could not execute helper: " + toString(method), ex);
    }
  }

  /**
   * Describe params.
   *
   * @param params Params to describe.
   * @return ToString of params
   */
  private String toString(final Object[] params) {
    StringBuilder buff = new StringBuilder();
    buff.append("[");
    for (Object param : params) {
      buff.append(param == null ? "null" : param.getClass().getSimpleName()).append(", ");
    }
    if (buff.length() > 1) {
      buff.setLength(buff.length() - 2);
    }
    return buff.append("]").toString();
  }

  /**
   * Describes method.
   *
   * @param method Method to describe.
   * @return ToString of method.
   */
  private String toString(final Method method) {
    return method.getName() + "(" + toString(method.getParameterTypes()) + ")";
  }

  /**
   * Describe types.
   *
   * @param types Types to describe.
   * @return ToString of types.
   */
  private String toString(final Class<?>[] types) {
    StringBuilder buff = new StringBuilder();
    for (Class<?> type : types) {
      buff.append(type.getSimpleName()).append(", ");
    }
    if (buff.length() > 0) {
      buff.setLength(buff.length() - 2);
    }
    return buff.toString();
  }

  /**
   * Return a runtime exception or throw an {@link IOException}.
   *
   * @param cause The invocation cause.
   * @return A runtime exception or throw an {@link IOException}.
   * @throws IOException If the cause is an {@link IOException}.
   */
  private RuntimeException launderThrowable(final Throwable cause) throws IOException {
    if (cause instanceof RuntimeException) {
      return (RuntimeException) cause;
    }
    if (cause instanceof IOException) {
      throw (IOException) cause;
    }
    return new IllegalStateException("could not execute helper: " + method.getName(), cause);
  }
}
