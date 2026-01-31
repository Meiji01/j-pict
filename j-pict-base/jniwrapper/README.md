# PICT JNI Wrapper

This project provides Java Native Interface (JNI) bindings for the PICT (Pairwise Independent Combinatorial Testing) library, enabling Java applications to generate combinatorial test cases.

## Overview

The JNI wrapper exposes all core PICT API functionality to Java applications:
- Task and model management
- Parameter definition with optional weights
- Hierarchical models (parent-child relationships)
- Exclusion and seed constraints
- Combinatorial test case generation
- Result retrieval

## Prerequisites

### Windows
- Visual Studio 2017 or later
- Java Development Kit (JDK) 8 or later
- Set `JAVA_HOME` environment variable pointing to your JDK installation

### Linux/macOS
- GCC/Clang compiler with C++17 support
- Java Development Kit (JDK) 8 or later
- CMake 3.13 or later

## Building

### Windows (Visual Studio)

1. Ensure `JAVA_HOME` is set:
   ```cmd
   set JAVA_HOME=C:\Program Files\Java\jdk-11.0.0
   ```

2. Build using MSBuild:
   ```cmd
   msbuild pict.sln /p:Configuration=Release
   ```

3. The output will be in `bin\pictjni.dll`

### Cross-Platform (CMake)

1. Ensure `JAVA_HOME` is set:
   ```bash
   export JAVA_HOME=/path/to/jdk
   ```

2. Build using CMake:
   ```bash
   cmake -DCMAKE_BUILD_TYPE=Release -S . -B build
   cmake --build build
   ```

3. The output will be in `build/jniwrapper/libpictjni.so` (Linux) or `libpictjni.dylib` (macOS)

## Usage

### 1. Compile the Java Class

```bash
javac -h . jniwrapper/PictJni.java
```

This generates the JNI header file `com_microsoft_pict_PictJni.h` (already provided).

### 2. Load the Native Library

The `PictJni` class automatically loads the native library:

```java
import com.microsoft.pict.PictJni;

PictJni pict = new PictJni();
```

Ensure the native library (`pictjni.dll` or `libpictjni.so`) is in your `java.library.path`.

### 3. Basic Example

```java
import com.microsoft.pict.PictJni;

public class PictExample {
    public static void main(String[] args) {
        PictJni pict = new PictJni();
        
        // Create task and model
        long task = pict.createTask();
        long model = pict.createModel(PictJni.PICT_DEFAULT_RANDOM_SEED);
        
        // Add 3 parameters with 2, 3, and 4 values respectively
        long param1 = pict.addParameter(model, 2, PictJni.PICT_PAIRWISE_GENERATION, null);
        long param2 = pict.addParameter(model, 3, PictJni.PICT_PAIRWISE_GENERATION, null);
        long param3 = pict.addParameter(model, 4, PictJni.PICT_PAIRWISE_GENERATION, null);
        
        // Set root model and generate
        pict.setRootModel(task, model);
        int result = pict.generate(task);
        
        if (result == PictJni.PICT_SUCCESS) {
            System.out.println("Generation successful!");
            
            // Get results
            long paramCount = pict.getTotalParameterCount(task);
            long resultBuffer = pict.allocateResultBuffer(task);
            pict.resetResultFetching(task);
            
            long[] row = new long[(int)paramCount];
            int rowNum = 0;
            
            while (pict.getNextResultRow(task, resultBuffer, row) > 0) {
                System.out.print("Row " + (++rowNum) + ": ");
                for (int i = 0; i < paramCount; i++) {
                    System.out.print(row[i]);
                    if (i < paramCount - 1) System.out.print(", ");
                }
                System.out.println();
            }
            
            pict.freeResultBuffer(resultBuffer);
        } else {
            System.err.println("Generation failed with code: " + result);
        }
        
        // Cleanup
        pict.deleteModel(model);
        pict.deleteTask(task);
    }
}
```

### 4. Advanced Features

#### Adding Exclusions

Exclude specific parameter-value combinations:

```java
// Exclude: param1=0 AND param2=1
long[] params = {param1, param2};
long[] values = {0, 1};
pict.addExclusion(task, params, values);
```

#### Adding Seeds

Ensure specific combinations appear in the output:

```java
// Ensure: param1=1 AND param3=2 appears
long[] params = {param1, param3};
long[] values = {1, 2};
pict.addSeed(task, params, values);
```

#### Using Weights

Assign weights to parameter values:

```java
// param1 value 0 appears 3x more often than value 1
int[] weights = {3, 1};
long param1 = pict.addParameter(model, 2, PictJni.PICT_PAIRWISE_GENERATION, weights);
```

#### Hierarchical Models

Create parent-child model relationships:

```java
long parentModel = pict.createModel(0);
long childModel = pict.createModel(0);

// Add parameters to both models
long parentParam = pict.addParameter(parentModel, 3, 2, null);
long childParam = pict.addParameter(childModel, 2, 2, null);

// Attach child to parent
pict.attachChildModel(parentModel, childModel, 2);
```

## API Reference

See `PictJni.java` for complete API documentation with method signatures and descriptions.

### Return Codes

- `PICT_SUCCESS` (0x00000000) - Operation succeeded
- `PICT_OUT_OF_MEMORY` (0xc0000001) - Memory allocation failed
- `PICT_GENERATION_ERROR` (0xc0000002) - Internal generation error

### Constants

- `PICT_PAIRWISE_GENERATION` (2) - Generate pairwise (2-way) combinations
- `PICT_DEFAULT_RANDOM_SEED` (0) - Use default random seed

## Files

- `jniwrapper.cpp` - JNI implementation wrapping PICT API
- `com_microsoft_pict_PictJni.h` - JNI header file (generated from Java)
- `PictJni.java` - Java class with native method declarations
- `pictjni.def` - Windows DLL export definitions
- `pictjni.vcxproj` - Visual Studio project file
- `CMakeLists.txt` - CMake build configuration

## Architecture

The JNI wrapper follows these design patterns:

1. **Handle Conversion**: Java `long` values store C++ pointers (PICT_HANDLE)
2. **Memory Management**: Native resources must be explicitly freed (deleteTask, deleteModel, freeResultBuffer)
3. **Array Marshaling**: Java arrays are converted to C++ arrays and released after use
4. **Error Propagation**: PICT error codes are returned directly to Java

## Troubleshooting

### Library Not Found

If you get `UnsatisfiedLinkError`:
- Ensure the native library is in `java.library.path`
- On Windows, add the directory containing `pictjni.dll` to PATH
- On Linux, add to `LD_LIBRARY_PATH`
- On macOS, add to `DYLD_LIBRARY_PATH`

### JAVA_HOME Not Set

Build errors about missing JNI headers:
- Set `JAVA_HOME` to your JDK installation directory
- Ensure it points to a JDK (not JRE)

### Compilation Errors

- Verify you have a C++17-compatible compiler
- Check that PICT API library (`pict.lib` or `libpict.a`) is built first

## License

This wrapper follows the same license as PICT. See LICENSE.TXT in the root directory.
