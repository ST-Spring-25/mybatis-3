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
package org.apache.ibatis.io;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class DefaultVFSTest {

  private DefaultVFS vfs;

  @TempDir
  Path tempDir;

  @BeforeEach
  void setUp() {
    vfs = new DefaultVFS();
  }

  /**
   * Test Case 1: Equivalence Partition Testing Tests jar file detection by dividing the input into valid and invalid
   * cases. The first test is a valid case, and tests if the vfs can detect jar files correctly. The second test is an
   * invalid case, and tests if the vfs can detect non-jar files correctly.
   **/
  @Test
  void testJarFileDetection() throws Exception {
    // Test valid jar file detection
    Path jarPath = tempDir.resolve("test.jar");
    try (JarOutputStream jos = new JarOutputStream(Files.newOutputStream(jarPath))) {
      JarEntry entry = new JarEntry("test.txt");
      jos.putNextEntry(entry);
      jos.write("test".getBytes(StandardCharsets.UTF_8));
      jos.closeEntry();
    }
    URL jarURL = jarPath.toUri().toURL();
    assertTrue(vfs.isJar(jarURL), "Should detect valid jar file");

    // Test invalid jar file detection
    Path txtPath = tempDir.resolve("notjar.txt");
    Files.writeString(txtPath, "Not a Jar");
    URL txtUrl = txtPath.toUri().toURL();
    assertFalse(vfs.isJar(txtUrl), "Should not detect non-jar file");
  }
}
