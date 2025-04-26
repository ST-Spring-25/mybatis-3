/*
 *    Copyright 2009-2025 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.apache.ibatis.mapping;

import static com.googlecode.catchexception.apis.BDDCatchException.caughtException;
import static com.googlecode.catchexception.apis.BDDCatchException.when;
import static org.assertj.core.api.BDDAssertions.then;

import java.lang.reflect.Field;
import java.util.Properties;

import org.apache.ibatis.builder.InitializingObject;
import org.apache.ibatis.cache.Cache;
import org.apache.ibatis.cache.CacheException;
import org.apache.ibatis.cache.impl.PerpetualCache;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class CacheBuilderTest {

  @Test
  void initializing() {
    InitializingCache cache = unwrap(new CacheBuilder("test").implementation(InitializingCache.class).build());

    Assertions.assertThat(cache.initialized).isTrue();
  }

  @Test
  void initializingFailure() {
    when(() -> new CacheBuilder("test").implementation(InitializingFailureCache.class).build());
    then(caughtException()).isInstanceOf(CacheException.class).hasMessage(
        "Failed cache initialization for 'test' on 'org.apache.ibatis.mapping.CacheBuilderTest$InitializingFailureCache'");
  }

  @SuppressWarnings("unchecked")
  private <T> T unwrap(Cache cache) {
    Field field;
    try {
      field = cache.getClass().getDeclaredField("delegate");
    } catch (NoSuchFieldException e) {
      throw new IllegalStateException(e);
    }
    try {
      field.setAccessible(true);
      return (T) field.get(cache);
    } catch (IllegalAccessException e) {
      throw new IllegalStateException(e);
    } finally {
      field.setAccessible(false);
    }
  }

  private static class InitializingCache extends PerpetualCache implements InitializingObject {

    private boolean initialized;

    public InitializingCache(String id) {
      super(id);
    }

    @Override
    public void initialize() {
      this.initialized = true;
    }

  }

  private static class InitializingFailureCache extends PerpetualCache implements InitializingObject {

    public InitializingFailureCache(String id) {
      super(id);
    }

    @Override
    public void initialize() {
      throw new IllegalStateException("error");
    }

  }

  /**
   * Test Case 7: Verifies that CacheBuilder correctly converts string properties to various types when setting cache
   * properties, ensuring type conversion handles all supported data types properly.
   */
  @Test
  void propertyTypeConversion() {
    Properties properties = new Properties();
    properties.setProperty("stringProperty", "test");
    properties.setProperty("intProperty", "42");
    properties.setProperty("longProperty", "9223372036854775807");
    properties.setProperty("shortProperty", "32767");
    properties.setProperty("byteProperty", "127");
    properties.setProperty("floatProperty", "8.72");
    properties.setProperty("doubleProperty", "2.113211");
    properties.setProperty("booleanProperty", "true");

    Cache cache = new CacheBuilder("typeTest").implementation(TypedPropertiesCache.class).properties(properties)
        .build();

    TypedPropertiesCache typedCache = unwrap(cache);

    Assertions.assertThat(typedCache.stringProperty).isEqualTo("test");
    Assertions.assertThat(typedCache.intProperty).isEqualTo(42);
    Assertions.assertThat(typedCache.longProperty).isEqualTo(9223372036854775807L);
    Assertions.assertThat(typedCache.shortProperty).isEqualTo((short) 32767);
    Assertions.assertThat(typedCache.byteProperty).isEqualTo((byte) 127);
    Assertions.assertThat(typedCache.floatProperty).isEqualTo(8.72f);
    Assertions.assertThat(typedCache.doubleProperty).isEqualTo(2.113211);
    Assertions.assertThat(typedCache.booleanProperty).isTrue();
  }

  private static class TypedPropertiesCache extends PerpetualCache {
    private String stringProperty;
    private int intProperty;
    private long longProperty;
    private short shortProperty;
    private byte byteProperty;
    private float floatProperty;
    private double doubleProperty;
    private boolean booleanProperty;

    public TypedPropertiesCache(String id) {
      super(id);
    }
  }

}
