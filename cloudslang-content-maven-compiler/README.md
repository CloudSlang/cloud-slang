CloudSlang Maven Compiler
=========================

To use the CloudSlang maven compiler, you need to make the artifact available in the classpath when the Compiler Plugin runs.
This is possible by adding the dependency when declaring the plugin in your project's pom.xml.
The example below shows how to use the CloudSlang compiler:


```shell
<project>
   [...]
   <build>
    [...]
    <plugins>
      [...]
      <plugin>
        <artifactId>maven-compiler-plugin</artifactId>
        <version>3.5.1</version>
        <configuration>
          <compilerId>cloudslang</compilerId>
        </configuration>
        <dependencies>
          <dependency>
            <groupId>io.cloudslang.lang</groupId>
            <artifactId>cloudslang-content-maven-compiler</artifactId>
            <version><any_version></version>
          </dependency>
        </dependencies>
      </plugin>
      [...]
    <plugins>
    [...]
   <build>
   [...]
</project>
```