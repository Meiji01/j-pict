# Java JNI Usage Sample

This directory contains a minimal example showing how to call the PICT JNI bindings from Java code. The entry point in src/jpict_usage/nativewrapper.java instantiates com.microsoft.pict.PictJni, builds a small combinatorial model from a 2D array of value labels, generates test cases, and prints each row using the human-readable labels instead of numeric indices.

## Prerequisites
- Build the native components so that pictjni.dll (or the platform-equivalent shared library) is available in this folder.
- Install a Java Development Kit (JDK) 11 or newer and ensure javac and java are on your PATH.

## Compile the sample
```powershell
cd jpict_usage
javac -d bin src/com/microsoft/pict/PictJni.java src/jpict_usage/nativewrapper.java
```
The compiled classes are written to bin/ (created automatically by javac).

## Run the sample
```powershell
java -cp bin -Djava.library.path=. jpict_usage.nativewrapper
```
The program prints each generated combination as a comma-separated row of labels (for example, test1, comb2, 300). Modify the paramValues array in nativewrapper.java to experiment with different parameters, value counts, and descriptive names. Extend the flow with methods such as addExclusion, addSeed, or attachChildModel when you need more advanced scenarios.

## Next steps
- Keep the paramValues structure in sync with the number of parameters you add to the model.
- Organize the sample into a build tool (e.g., Gradle or Maven) if you need to integrate PICT generation into larger projects.
