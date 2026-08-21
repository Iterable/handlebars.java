package com.github.jknack.handlebars.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import static org.junit.Assert.assertSame;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.junit.Test;

import com.github.jknack.handlebars.Context;
import com.github.jknack.handlebars.Handlebars;
import com.github.jknack.handlebars.ValueResolver;

public class SetAccessibleValueResolverTest {

  /*
   * The following tests require JDK 9 or greater.
   * To keep the tests from failing we use junit assume.
   */

  @Test
  public void testSetAccessibleOnJDK9OrGreater() throws Exception {
    assumeTrue(getJavaVersion() >= 9);
    MethodValueResolver mv = new MethodValueResolver() {

      @Override
      protected boolean isUseSetAccessible(Method m) {
        return true;
      }
    };
    try {
      mv.resolve(Collections.emptyMap(), "doesNotMatter");
      fail("Expect InaccessibleObjectException");
    } catch (/* InaccessibleObjectException */ Exception e) {
    }

    mv = new MethodValueResolver();
    mv.resolve(Collections.emptyMap(), "doesNotMatter");
    Object result = mv.resolve(Collections.emptyMap(), "isEmpty");
    assertEquals(Boolean.TRUE, result);
  }

  @Test
  public void fieldResolverDoesNotOpenJdkInternals() {
    Object result = new FieldValueResolver().resolve(new HashMap<>(), "missing");

    assertSame(ValueResolver.UNRESOLVED, result);
  }

  @Test
  public void privateJdkFieldsAreNotResolved() {
    List<String> groups = new ArrayList<String>(Arrays.asList("a", "b"));

    assertSame(ValueResolver.UNRESOLVED, new FieldValueResolver().resolve(groups, "size"));
  }

  @Test
  public void collectionSizeFallsThroughToPublicMethod() throws Exception {
    List<String> groups = new ArrayList<String>(Arrays.asList("a", "b"));
    Context context = Context.newBuilder(groups)
        .resolver(FieldValueResolver.INSTANCE, MethodValueResolver.INSTANCE)
        .build();

    assertEquals("2", new Handlebars().compileInline("{{this.size}}").apply(context));
  }

  static int getJavaVersion() {
    String version = System.getProperty("java.version");
    if (version.startsWith("1.")) {
      version = version.substring(2, 3);
    } else {
      int dot = version.indexOf(".");
      if (dot != -1) {
        version = version.substring(0, dot);
      }
    }
    try {
      return Integer.parseInt(version);
    } catch (NumberFormatException e) {
      return 8;
    }
  }

}
