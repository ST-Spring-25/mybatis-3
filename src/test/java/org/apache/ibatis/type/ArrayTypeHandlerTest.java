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
package org.apache.ibatis.type;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.sql.*;
import java.time.*;
import java.util.Calendar;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class ArrayTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler<Object> TYPE_HANDLER = new ArrayTypeHandler();

  @Mock
  Array mockArray;

  @Override
  @Test
  public void shouldSetParameter() throws Exception {
    TYPE_HANDLER.setParameter(ps, 1, mockArray, null);
    verify(ps).setArray(1, mockArray);
  }

  @Test
  void shouldSetStringArrayParameter() throws Exception {
    Connection connection = mock(Connection.class);
    when(ps.getConnection()).thenReturn(connection);

    Array array = mock(Array.class);
    when(connection.createArrayOf(anyString(), any(String[].class))).thenReturn(array);

    TYPE_HANDLER.setParameter(ps, 1, new String[] { "Hello World" }, JdbcType.ARRAY);
    verify(ps).setArray(1, array);
    verify(array).free();
  }

  @Test
  void shouldSetNullParameter() throws Exception {
    TYPE_HANDLER.setParameter(ps, 1, null, JdbcType.ARRAY);
    verify(ps).setNull(1, Types.ARRAY);
  }

  @Test
  void shouldFailForNonArrayParameter() {
    assertThrows(TypeException.class, () -> TYPE_HANDLER.setParameter(ps, 1, "unsupported parameter type", null));
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByName() throws Exception {
    when(rs.getArray("column")).thenReturn(mockArray);
    String[] stringArray = { "a", "b" };
    when(mockArray.getArray()).thenReturn(stringArray);
    assertEquals(stringArray, TYPE_HANDLER.getResult(rs, "column"));
    verify(mockArray).free();
  }

  @Override
  @Test
  public void shouldGetResultNullFromResultSetByName() throws Exception {
    when(rs.getArray("column")).thenReturn(null);
    assertNull(TYPE_HANDLER.getResult(rs, "column"));
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByPosition() throws Exception {
    when(rs.getArray(1)).thenReturn(mockArray);
    String[] stringArray = { "a", "b" };
    when(mockArray.getArray()).thenReturn(stringArray);
    assertEquals(stringArray, TYPE_HANDLER.getResult(rs, 1));
    verify(mockArray).free();
  }

  @Override
  @Test
  public void shouldGetResultNullFromResultSetByPosition() throws Exception {
    when(rs.getArray(1)).thenReturn(null);
    assertNull(TYPE_HANDLER.getResult(rs, 1));
  }

  @Override
  @Test
  public void shouldGetResultFromCallableStatement() throws Exception {
    when(cs.getArray(1)).thenReturn(mockArray);
    String[] stringArray = { "a", "b" };
    when(mockArray.getArray()).thenReturn(stringArray);
    assertEquals(stringArray, TYPE_HANDLER.getResult(cs, 1));
    verify(mockArray).free();
  }

  @Override
  @Test
  public void shouldGetResultNullFromCallableStatement() throws Exception {
    when(cs.getArray(1)).thenReturn(null);
    assertNull(TYPE_HANDLER.getResult(cs, 1));
  }

  /**
   * Test Case 8: Tests the ArrayTypeHandler's type resolution mechanism which maps Java types to SQL type names.
   * The test verifies three key scenarios:
   * 1. Standard type mappings defined in STANDARD_MAPPING are correctly resolved
   * 2. Non-standard types default to "JAVA_OBJECT" as expected
   * 3. Array types themselves are handled appropriately within the resolution system
   **/
  static class TestableArrayTypeHandler extends ArrayTypeHandler {
    public String publicResolveTypeName(Class<?> type) {
      return resolveTypeName(type);
    }
  }

  private final TestableArrayTypeHandler handler = new TestableArrayTypeHandler();

  @Test
  void shouldResolveStandardTypes() {
    assertEquals("NUMERIC", handler.publicResolveTypeName(BigDecimal.class));
    assertEquals("BIGINT", handler.publicResolveTypeName(BigInteger.class));
    assertEquals("BOOLEAN", handler.publicResolveTypeName(boolean.class));
    assertEquals("BOOLEAN", handler.publicResolveTypeName(Boolean.class));
    assertEquals("VARBINARY", handler.publicResolveTypeName(byte[].class));
    assertEquals("TINYINT", handler.publicResolveTypeName(byte.class));
    assertEquals("TINYINT", handler.publicResolveTypeName(Byte.class));
    assertEquals("TIMESTAMP", handler.publicResolveTypeName(Calendar.class));
    assertEquals("DATE", handler.publicResolveTypeName(java.sql.Date.class));
    assertEquals("TIMESTAMP", handler.publicResolveTypeName(java.util.Date.class));
    assertEquals("DOUBLE", handler.publicResolveTypeName(double.class));
    assertEquals("DOUBLE", handler.publicResolveTypeName(Double.class));
    assertEquals("REAL", handler.publicResolveTypeName(float.class));
    assertEquals("REAL", handler.publicResolveTypeName(Float.class));
    assertEquals("INTEGER", handler.publicResolveTypeName(int.class));
    assertEquals("INTEGER", handler.publicResolveTypeName(Integer.class));
    assertEquals("DATE", handler.publicResolveTypeName(LocalDate.class));
    assertEquals("TIMESTAMP", handler.publicResolveTypeName(LocalDateTime.class));
    assertEquals("TIME", handler.publicResolveTypeName(LocalTime.class));
    assertEquals("BIGINT", handler.publicResolveTypeName(long.class));
    assertEquals("BIGINT", handler.publicResolveTypeName(Long.class));
    assertEquals("TIMESTAMP_WITH_TIMEZONE", handler.publicResolveTypeName(OffsetDateTime.class));
    assertEquals("TIME_WITH_TIMEZONE", handler.publicResolveTypeName(OffsetTime.class));
    assertEquals("SMALLINT", handler.publicResolveTypeName(Short.class));
    assertEquals("VARCHAR", handler.publicResolveTypeName(String.class));
    assertEquals("TIME", handler.publicResolveTypeName(Time.class));
    assertEquals("TIMESTAMP", handler.publicResolveTypeName(Timestamp.class));
    assertEquals("DATALINK", handler.publicResolveTypeName(URL.class));
  }

  @Test
  void shouldReturnJavaObjectForNonStandardTypes() {
    class CustomClass {
    }

    assertEquals("JAVA_OBJECT", handler.publicResolveTypeName(CustomClass.class));
    assertEquals("JAVA_OBJECT", handler.publicResolveTypeName(StringBuilder.class));
    assertEquals("JAVA_OBJECT", handler.publicResolveTypeName(Object.class));
  }

  @Test
  void shouldHandleArrayTypes() {
    // Array types should also be JAVA_OBJECTs
    assertEquals("JAVA_OBJECT", handler.publicResolveTypeName(String[].class));
    assertEquals("JAVA_OBJECT", handler.publicResolveTypeName(Integer[].class));
    assertEquals("JAVA_OBJECT", handler.publicResolveTypeName(Object[].class));
  }

}
