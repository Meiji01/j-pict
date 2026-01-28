# PICT API Documentation

## Overview

The PICT (Pairwise Independent Combinatorial Testing) API provides a programmatic interface for generating combinatorial test cases. This C/C++ API allows you to create models with parameters, define constraints through exclusions, specify required combinations via seeds, and generate optimized test suites.

---

## Table of Contents

1. [Data Types](#data-types)
2. [Return Codes](#return-codes)
3. [Constants](#constants)
4. [Task Management](#task-management)
5. [Model Management](#model-management)
6. [Parameter Management](#parameter-management)
7. [Constraint Management](#constraint-management)
8. [Generation and Results](#generation-and-results)
9. [Complete Usage Example](#complete-usage-example)

---

## Data Types

### PICT_HANDLE
```cpp
typedef void * PICT_HANDLE;
```
Opaque handle used for tasks, models, and parameters.

### PICT_VALUE
```cpp
typedef size_t PICT_VALUE;
```
Represents a zero-based index to a parameter value.

### PICT_RESULT_ROW
```cpp
typedef PICT_VALUE * PICT_RESULT_ROW;
```
Pointer to an array of values representing one test case.

### PICT_RET_CODE
```cpp
typedef unsigned int PICT_RET_CODE;
```
Return code from API functions.

### PICT_EXCLUSION_ITEM
```cpp
typedef struct _PICT_EXCLUSION_ITEM
{
    PICT_HANDLE Parameter;   // Handle to the parameter
    PICT_VALUE  ValueIndex;  // Zero-based index of the value
} PICT_EXCLUSION_ITEM;
```
Represents one parameter-value pair in an exclusion constraint.

### PICT_SEED_ITEM
```cpp
typedef struct _PICT_SEED_ITEM
{
    PICT_HANDLE Parameter;   // Handle to the parameter
    PICT_VALUE  ValueIndex;  // Zero-based index of the value
} PICT_SEED_ITEM;
```
Represents one parameter-value pair in a seed (required combination).

---

## Return Codes

| Code | Value | Description |
|------|-------|-------------|
| `PICT_SUCCESS` | 0x00000000 | Operation completed successfully |
| `PICT_OUT_OF_MEMORY` | 0xc0000001 | Insufficient memory to complete operation |
| `PICT_GENERATION_ERROR` | 0xc0000002 | Internal engine error during generation |

---

## Constants

| Constant | Value | Description |
|----------|-------|-------------|
| `PICT_PAIRWISE_GENERATION` | 2 | Default order for pairwise testing |
| `PICT_DEFAULT_RANDOM_SEED` | 0 | Default random seed value |

---

## Task Management

### PictCreateTask

**Description:** Allocates a new task. All operations happen in the context of a task.

**Signature:**
```cpp
PICT_HANDLE API_SPEC PictCreateTask();
```

**Parameters:**
- None

**Returns:**
- `Non-nullptr`: Task handle (allocation succeeded)
- `nullptr`: Allocation failed

**Sample Usage:**
```cpp
PICT_HANDLE task = PictCreateTask();
if (task == nullptr) {
    std::cerr << "Failed to create task" << std::endl;
    return;
}
```

---

### PictSetRootModel

**Description:** Associates a model tree with a task. The root model becomes the primary model for generation.

**Signature:**
```cpp
void API_SPEC PictSetRootModel(
    IN const PICT_HANDLE task,
    IN const PICT_HANDLE model
);
```

**Parameters:**
- `task`: Valid handle to a task
- `model`: Valid handle to the root model

**Returns:**
- Nothing (void)

**Sample Usage:**
```cpp
PICT_HANDLE task = PictCreateTask();
PICT_HANDLE model = PictCreateModel();

PictSetRootModel(task, model);
```

---

### PictDeleteTask

**Description:** Deallocates a task and frees all associated resources.

**Signature:**
```cpp
void API_SPEC PictDeleteTask(
    IN const PICT_HANDLE task
);
```

**Parameters:**
- `task`: Valid handle to a task

**Returns:**
- Nothing (void)

**Sample Usage:**
```cpp
PICT_HANDLE task = PictCreateTask();
// ... use the task ...
PictDeleteTask(task);
```

---

## Model Management

### PictCreateModel

**Description:** Allocates a new model. Models contain parameters and can form hierarchies.

**Signature:**
```cpp
PICT_HANDLE API_SPEC PictCreateModel(
    IN OPT unsigned int randomSeed = PICT_DEFAULT_RANDOM_SEED
);
```

**Parameters:**
- `randomSeed` (optional): Seed for randomizing the generation engine. Default is `PICT_DEFAULT_RANDOM_SEED` (0)

**Returns:**
- `Non-nullptr`: Model handle (allocation succeeded)
- `nullptr`: Allocation failed

**Sample Usage:**
```cpp
// Create model with default random seed
PICT_HANDLE model1 = PictCreateModel();

// Create model with custom random seed for reproducibility
PICT_HANDLE model2 = PictCreateModel(12345);

if (model1 == nullptr || model2 == nullptr) {
    std::cerr << "Failed to create model" << std::endl;
}
```

---

### PictAttachChildModel

**Description:** Creates a parent-child relationship between two models, forming a model hierarchy for complex testing scenarios.

**Signature:**
```cpp
PICT_RET_CODE API_SPEC PictAttachChildModel(
    IN     const PICT_HANDLE modelParent,
    IN     const PICT_HANDLE modelChild,
    IN OPT unsigned int      order = PICT_PAIRWISE_GENERATION
);
```

**Parameters:**
- `modelParent`: Handle to the parent model
- `modelChild`: Handle to the child model to attach
- `order` (optional): Order of combinations for the submodel. Default is `PICT_PAIRWISE_GENERATION` (2)

**Returns:**
- `PICT_SUCCESS`: Successfully attached
- `PICT_OUT_OF_MEMORY`: Insufficient memory

**Sample Usage:**
```cpp
PICT_HANDLE parentModel = PictCreateModel();
PICT_HANDLE childModel = PictCreateModel();

// Attach child with default pairwise order
PICT_RET_CODE ret = PictAttachChildModel(parentModel, childModel);
if (ret != PICT_SUCCESS) {
    std::cerr << "Failed to attach child model" << std::endl;
}

// Attach with 3-way coverage
PICT_HANDLE childModel2 = PictCreateModel();
ret = PictAttachChildModel(parentModel, childModel2, 3);
```

---

### PictDeleteModel

**Description:** Deallocates a model, all its parameters, and recursively all attached submodels.

**Signature:**
```cpp
void API_SPEC PictDeleteModel(
    IN const PICT_HANDLE model
);
```

**Parameters:**
- `model`: Valid handle to a model

**Returns:**
- Nothing (void)

**Sample Usage:**
```cpp
PICT_HANDLE model = PictCreateModel();
// ... use the model ...
PictDeleteModel(model);  // Also deletes all child models
```

---

## Parameter Management

### PictAddParameter

**Description:** Adds a parameter with a specified number of values to a model.

**Signature:**
```cpp
PICT_HANDLE API_SPEC PictAddParameter(
    IN     const PICT_HANDLE model,
    IN     size_t            valueCount,
    IN OPT unsigned int      order          = PICT_PAIRWISE_GENERATION,
    IN OPT unsigned int      valueWeights[] = nullptr
);
```

**Parameters:**
- `model`: Model to add the parameter to
- `valueCount`: Number of values this parameter has (must be > 0)
- `order` (optional): Order of combinations for this parameter. Default is `PICT_PAIRWISE_GENERATION` (2)
- `valueWeights` (optional): Array of weights (size must equal `valueCount`). Higher weights make values more likely to be selected. Default is `nullptr` (equal weights)

**Returns:**
- `Non-nullptr`: Parameter handle (allocation succeeded)
- `nullptr`: Allocation failed

**Sample Usage:**
```cpp
PICT_HANDLE model = PictCreateModel();

// Simple parameter with 4 values, pairwise coverage
PICT_HANDLE param1 = PictAddParameter(model, 4);

// Parameter with custom weights (prefer second value)
unsigned int weights[] = {1, 3, 1, 1};
PICT_HANDLE param2 = PictAddParameter(model, 4, PICT_PAIRWISE_GENERATION, weights);

// Parameter with 3-way coverage
PICT_HANDLE param3 = PictAddParameter(model, 5, 3);

if (param1 == nullptr) {
    std::cerr << "Failed to add parameter" << std::endl;
}
```

---

### PictGetTotalParameterCount

**Description:** Returns the total number of parameters across all models in a task.

**Signature:**
```cpp
size_t API_SPEC PictGetTotalParameterCount(
    IN const PICT_HANDLE task
);
```

**Parameters:**
- `task`: Valid handle to a task

**Returns:**
- Total count of parameters in all models attached to the task

**Sample Usage:**
```cpp
PICT_HANDLE task = PictCreateTask();
PICT_HANDLE model = PictCreateModel();
PictSetRootModel(task, model);

PictAddParameter(model, 3);
PictAddParameter(model, 4);
PictAddParameter(model, 2);

size_t totalParams = PictGetTotalParameterCount(task);
// totalParams will be 3
std::cout << "Total parameters: " << totalParams << std::endl;
```

---

## Constraint Management

### PictAddExclusion

**Description:** Adds an exclusion constraint to prevent specific parameter-value combinations from appearing in the output.

**Signature:**
```cpp
PICT_RET_CODE API_SPEC PictAddExclusion(
    IN const PICT_HANDLE         task,
    IN const PICT_EXCLUSION_ITEM exclusionItems[],
    IN       size_t              exclusionItemCount
);
```

**Parameters:**
- `task`: Valid handle to a task
- `exclusionItems`: Array of parameter-value pairs defining the forbidden combination
- `exclusionItemCount`: Number of items in the array

**Returns:**
- `PICT_SUCCESS`: Exclusion added successfully
- `PICT_OUT_OF_MEMORY`: Insufficient memory

**Sample Usage:**
```cpp
PICT_HANDLE task = PictCreateTask();
PICT_HANDLE model = PictCreateModel();
PictSetRootModel(task, model);

PICT_HANDLE param1 = PictAddParameter(model, 4);
PICT_HANDLE param2 = PictAddParameter(model, 3);
PICT_HANDLE param3 = PictAddParameter(model, 5);

// Exclude combination: param1=value[0] AND param2=value[0]
// (first value of param1 cannot occur with first value of param2)
PICT_EXCLUSION_ITEM exclusion[2];
exclusion[0].Parameter = param1;
exclusion[0].ValueIndex = 0;  // First value (0-based)
exclusion[1].Parameter = param2;
exclusion[1].ValueIndex = 0;  // First value (0-based)

PICT_RET_CODE ret = PictAddExclusion(task, exclusion, 2);
if (ret != PICT_SUCCESS) {
    std::cerr << "Failed to add exclusion" << std::endl;
}

// Exclude combination: param2=value[2] AND param3=value[4]
PICT_EXCLUSION_ITEM exclusion2[2];
exclusion2[0].Parameter = param2;
exclusion2[0].ValueIndex = 2;  // Third value
exclusion2[1].Parameter = param3;
exclusion2[1].ValueIndex = 4;  // Fifth value

ret = PictAddExclusion(task, exclusion2, 2);
```

---

### PictAddSeed

**Description:** Adds a seed to ensure a specific parameter-value combination appears in the output (unless it violates exclusions).

**Signature:**
```cpp
PICT_RET_CODE API_SPEC PictAddSeed(
    IN const PICT_HANDLE     task,
    IN const PICT_SEED_ITEM  seedItems[],
    IN       size_t          seedItemCount
);
```

**Parameters:**
- `task`: Valid handle to a task
- `seedItems`: Array of parameter-value pairs defining the required combination
- `seedItemCount`: Number of items in the array

**Returns:**
- `PICT_SUCCESS`: Seed added successfully
- `PICT_OUT_OF_MEMORY`: Insufficient memory

**Sample Usage:**
```cpp
PICT_HANDLE task = PictCreateTask();
PICT_HANDLE model = PictCreateModel();
PictSetRootModel(task, model);

PICT_HANDLE param1 = PictAddParameter(model, 4);
PICT_HANDLE param2 = PictAddParameter(model, 3);
PICT_HANDLE param3 = PictAddParameter(model, 5);

// Seed: ensure param1=value[1], param2=value[1], param3=value[1] appears
PICT_SEED_ITEM seed[3];
seed[0].Parameter = param1;
seed[0].ValueIndex = 1;  // Second value (0-based)
seed[1].Parameter = param2;
seed[1].ValueIndex = 1;  // Second value
seed[2].Parameter = param3;
seed[2].ValueIndex = 1;  // Second value

PICT_RET_CODE ret = PictAddSeed(task, seed, 3);
if (ret != PICT_SUCCESS) {
    std::cerr << "Failed to add seed" << std::endl;
}

// Partial seed: only specify some parameters
PICT_SEED_ITEM partialSeed[2];
partialSeed[0].Parameter = param1;
partialSeed[0].ValueIndex = 3;  // Fourth value
partialSeed[1].Parameter = param3;
partialSeed[1].ValueIndex = 0;  // First value

ret = PictAddSeed(task, partialSeed, 2);
```

---

## Generation and Results

### PictGenerate

**Description:** Generates the test cases based on the configured model, parameters, exclusions, and seeds.

**Signature:**
```cpp
PICT_RET_CODE API_SPEC PictGenerate(
    IN const PICT_HANDLE task
);
```

**Parameters:**
- `task`: Valid handle to a task with a fully configured model

**Returns:**
- `PICT_SUCCESS`: Generation completed successfully
- `PICT_OUT_OF_MEMORY`: Insufficient memory
- `PICT_GENERATION_ERROR`: Internal engine error

**Sample Usage:**
```cpp
PICT_HANDLE task = PictCreateTask();
PICT_HANDLE model = PictCreateModel();
PictSetRootModel(task, model);

// Add parameters, exclusions, seeds...
PictAddParameter(model, 3);
PictAddParameter(model, 4);

// Generate test cases
PICT_RET_CODE ret = PictGenerate(task);
if (ret != PICT_SUCCESS) {
    if (ret == PICT_OUT_OF_MEMORY) {
        std::cerr << "Out of memory during generation" << std::endl;
    } else if (ret == PICT_GENERATION_ERROR) {
        std::cerr << "Internal generation error" << std::endl;
    }
    return;
}

std::cout << "Generation successful!" << std::endl;
```

---

### PictAllocateResultBuffer

**Description:** Allocates a buffer large enough to hold one result row (one test case).

**Signature:**
```cpp
PICT_RESULT_ROW API_SPEC PictAllocateResultBuffer(
    IN const PICT_HANDLE task
);
```

**Parameters:**
- `task`: Valid handle to a task (after generation)

**Returns:**
- `Non-nullptr`: Pointer to allocated buffer
- `nullptr`: Allocation failed

**Sample Usage:**
```cpp
PICT_HANDLE task = PictCreateTask();
// ... configure and generate ...
PictGenerate(task);

PICT_RESULT_ROW row = PictAllocateResultBuffer(task);
if (row == nullptr) {
    std::cerr << "Failed to allocate result buffer" << std::endl;
    return;
}

// Use the buffer to fetch results...

// Remember to free when done
PictFreeResultBuffer(row);
```

---

### PictFreeResultBuffer

**Description:** Frees a buffer previously allocated by `PictAllocateResultBuffer`.

**Signature:**
```cpp
void API_SPEC PictFreeResultBuffer(
    IN const PICT_RESULT_ROW resultRow
);
```

**Parameters:**
- `resultRow`: Buffer returned by `PictAllocateResultBuffer`

**Returns:**
- Nothing (void)

**Sample Usage:**
```cpp
PICT_RESULT_ROW row = PictAllocateResultBuffer(task);
// ... use the buffer ...
PictFreeResultBuffer(row);
```

---

### PictResetResultFetching

**Description:** Resets the result retrieval to start from the beginning. Call this before fetching results or to restart iteration.

**Signature:**
```cpp
void API_SPEC PictResetResultFetching(
    IN const PICT_HANDLE task
);
```

**Parameters:**
- `task`: Valid handle to a task (after generation)

**Returns:**
- Nothing (void)

**Sample Usage:**
```cpp
PictGenerate(task);
PICT_RESULT_ROW row = PictAllocateResultBuffer(task);

// First pass through results
PictResetResultFetching(task);
while (PictGetNextResultRow(task, row)) {
    // Process each row
}

// Second pass through the same results
PictResetResultFetching(task);
while (PictGetNextResultRow(task, row)) {
    // Process each row again
}

PictFreeResultBuffer(row);
```

---

### PictGetNextResultRow

**Description:** Fetches the next result row and advances the internal pointer. Call repeatedly to retrieve all test cases.

**Signature:**
```cpp
size_t API_SPEC PictGetNextResultRow(
    IN  const PICT_HANDLE     task,
    OUT       PICT_RESULT_ROW resultRow
);
```

**Parameters:**
- `task`: Valid handle to a task (after generation)
- `resultRow`: Buffer to fill with the next row's values

**Returns:**
- Number of rows remaining **before** advancing the pointer
- `0` when no more rows are available

**Important:** The return value indicates how many rows remain **when entering the function**. When it returns 0, there are no more rows to fetch.

**Sample Usage:**
```cpp
PICT_HANDLE task = PictCreateTask();
PICT_HANDLE model = PictCreateModel();
PictSetRootModel(task, model);

PICT_HANDLE param1 = PictAddParameter(model, 3);
PICT_HANDLE param2 = PictAddParameter(model, 4);

PictGenerate(task);

size_t paramCount = PictGetTotalParameterCount(task);
PICT_RESULT_ROW row = PictAllocateResultBuffer(task);

PictResetResultFetching(task);

std::cout << "Test Cases:" << std::endl;
size_t testCaseNumber = 1;

while (PictGetNextResultRow(task, row)) {
    std::cout << "Test " << testCaseNumber++ << ": ";
    for (size_t i = 0; i < paramCount; ++i) {
        std::cout << row[i] << " ";
    }
    std::cout << std::endl;
}

PictFreeResultBuffer(row);
```

---

## Complete Usage Example

This comprehensive example demonstrates a typical workflow using the PICT API:

```cpp
#include <iostream>
#include "pictapi.h"

int main() {
    //
    // Step 1: Create a task
    //
    PICT_HANDLE task = PictCreateTask();
    if (task == nullptr) {
        std::cerr << "Failed to create task" << std::endl;
        return 1;
    }

    //
    // Step 2: Create a model with a random seed for reproducibility
    //
    PICT_HANDLE model = PictCreateModel(42);
    if (model == nullptr) {
        std::cerr << "Failed to create model" << std::endl;
        PictDeleteTask(task);
        return 1;
    }

    //
    // Step 3: Associate the model with the task
    //
    PictSetRootModel(task, model);

    //
    // Step 4: Add parameters
    //
    // Browser: 3 values (Chrome, Firefox, Edge)
    PICT_HANDLE browserParam = PictAddParameter(model, 3, PICT_PAIRWISE_GENERATION);
    
    // OS: 4 values (Windows, Linux, macOS, Android)
    PICT_HANDLE osParam = PictAddParameter(model, 4, PICT_PAIRWISE_GENERATION);
    
    // Screen Resolution: 5 values with weights
    unsigned int resWeights[] = {2, 3, 2, 1, 1};  // Prefer 1920x1080
    PICT_HANDLE resParam = PictAddParameter(model, 5, PICT_PAIRWISE_GENERATION, resWeights);
    
    // Connection: 2 values (WiFi, Ethernet)
    PICT_HANDLE connParam = PictAddParameter(model, 2, PICT_PAIRWISE_GENERATION);

    if (!browserParam || !osParam || !resParam || !connParam) {
        std::cerr << "Failed to add parameters" << std::endl;
        goto cleanup;
    }

    //
    // Step 5: Add exclusions (Edge browser not available on Linux)
    //
    PICT_EXCLUSION_ITEM exclusion1[2];
    exclusion1[0].Parameter = browserParam;
    exclusion1[0].ValueIndex = 2;  // Edge (assuming index 2)
    exclusion1[1].Parameter = osParam;
    exclusion1[1].ValueIndex = 1;  // Linux (assuming index 1)

    PICT_RET_CODE ret = PictAddExclusion(task, exclusion1, 2);
    if (ret != PICT_SUCCESS) {
        std::cerr << "Failed to add exclusion" << std::endl;
        goto cleanup;
    }

    //
    // Step 6: Add a seed (ensure we test Chrome on Windows)
    //
    PICT_SEED_ITEM seed1[2];
    seed1[0].Parameter = browserParam;
    seed1[0].ValueIndex = 0;  // Chrome
    seed1[1].Parameter = osParam;
    seed1[1].ValueIndex = 0;  // Windows

    ret = PictAddSeed(task, seed1, 2);
    if (ret != PICT_SUCCESS) {
        std::cerr << "Failed to add seed" << std::endl;
        goto cleanup;
    }

    //
    // Step 7: Generate test cases
    //
    std::cout << "Generating test cases..." << std::endl;
    ret = PictGenerate(task);
    if (ret != PICT_SUCCESS) {
        std::cerr << "Generation failed with code: " << ret << std::endl;
        goto cleanup;
    }

    //
    // Step 8: Allocate result buffer
    //
    PICT_RESULT_ROW row = PictAllocateResultBuffer(task);
    if (row == nullptr) {
        std::cerr << "Failed to allocate result buffer" << std::endl;
        goto cleanup;
    }

    //
    // Step 9: Retrieve and display results
    //
    size_t paramCount = PictGetTotalParameterCount(task);
    std::cout << "\nGenerated Test Cases:" << std::endl;
    std::cout << "Browser\tOS\tResolution\tConnection" << std::endl;
    std::cout << "-------\t--\t----------\t----------" << std::endl;

    PictResetResultFetching(task);
    
    size_t testNumber = 1;
    while (PictGetNextResultRow(task, row)) {
        std::cout << "Test " << testNumber++ << ":\t";
        for (size_t i = 0; i < paramCount; ++i) {
            std::cout << row[i] << "\t";
        }
        std::cout << std::endl;
    }

    //
    // Step 10: Clean up result buffer
    //
    PictFreeResultBuffer(row);

cleanup:
    //
    // Step 11: Clean up model and task
    //
    if (model != nullptr) {
        PictDeleteModel(model);  // This also deletes all parameters
    }
    
    if (task != nullptr) {
        PictDeleteTask(task);
    }

    return 0;
}
```

### Expected Output

```
Generating test cases...

Generated Test Cases:
Browser	OS	Resolution	Connection
-------	--	----------	----------
Test 1:	0	0	1	0
Test 2:	1	1	2	1
Test 3:	2	2	3	0
Test 4:	0	3	4	1
Test 5:	1	0	0	0
...
```

### Interpreting Results

Each row represents one test case. The values are zero-based indices:
- `Browser`: 0=Chrome, 1=Firefox, 2=Edge
- `OS`: 0=Windows, 1=Linux, 2=macOS, 3=Android
- `Resolution`: 0=value0, 1=value1, etc.
- `Connection`: 0=WiFi, 1=Ethernet

---

## Advanced Usage Patterns

### Using Submodels

For complex scenarios with hierarchical parameters:

```cpp
PICT_HANDLE task = PictCreateTask();

// Parent model: Operating System
PICT_HANDLE osModel = PictCreateModel();
PICT_HANDLE osParam = PictAddParameter(osModel, 3);  // Win, Linux, Mac

// Child model: Browser (varies by OS)
PICT_HANDLE browserModel = PictCreateModel();
PICT_HANDLE browserParam = PictAddParameter(browserModel, 4);  // Chrome, FF, Edge, Safari

// Attach browser model as child of OS model
PictAttachChildModel(osModel, browserModel, PICT_PAIRWISE_GENERATION);

// Set root model and generate
PictSetRootModel(task, osModel);
PictGenerate(task);

// Cleanup (deleting osModel also deletes browserModel)
PictDeleteModel(osModel);
PictDeleteTask(task);
```

### Error Handling Pattern

```cpp
#define CHECK_RESULT(ret) \
    if (ret == PICT_OUT_OF_MEMORY) { \
        std::cerr << "Out of memory" << std::endl; \
        goto cleanup; \
    } else if (ret == PICT_GENERATION_ERROR) { \
        std::cerr << "Generation error" << std::endl; \
        goto cleanup; \
    }

PICT_RET_CODE ret = PictAddExclusion(task, items, count);
CHECK_RESULT(ret);

ret = PictGenerate(task);
CHECK_RESULT(ret);
```

---

## Best Practices

1. **Always check return values**: Handle `nullptr` returns and error codes appropriately
2. **Clean up resources**: Always call `PictDeleteTask` and `PictDeleteModel` to prevent memory leaks
3. **Parameter order matters**: The order you add parameters determines the order of values in result rows
4. **Use seeds wisely**: Seeds guarantee specific combinations appear but may increase result set size
5. **Validate indices**: Ensure value indices in exclusions/seeds don't exceed parameter value counts
6. **Reset before fetching**: Always call `PictResetResultFetching` before iterating through results
7. **Use weights strategically**: Higher weights bias selection toward certain values without forcing them
8. **Test exclusions**: Verify your exclusions don't over-constrain the model (making generation impossible)

---

## Common Pitfalls

1. **Forgetting to set root model**: Always call `PictSetRootModel` before generation
2. **Invalid value indices**: Using value index >= valueCount causes undefined behavior
3. **Generating before setup**: Call `PictGenerate` only after all configuration is complete
4. **Memory leaks**: Forgetting to delete models and tasks
5. **Reusing buffers incorrectly**: Don't free a result buffer while still iterating
6. **Off-by-one errors**: Remember indices are zero-based

---

## API Summary Table

| Function | Purpose | Returns Handle/Code |
|----------|---------|---------------------|
| `PictCreateTask` | Create task | Handle |
| `PictSetRootModel` | Attach model to task | void |
| `PictDeleteTask` | Free task | void |
| `PictCreateModel` | Create model | Handle |
| `PictAttachChildModel` | Create model hierarchy | Return code |
| `PictDeleteModel` | Free model (and children) | void |
| `PictAddParameter` | Add parameter to model | Handle |
| `PictGetTotalParameterCount` | Get parameter count | size_t |
| `PictAddExclusion` | Add exclusion constraint | Return code |
| `PictAddSeed` | Add required combination | Return code |
| `PictGenerate` | Generate test cases | Return code |
| `PictAllocateResultBuffer` | Allocate result buffer | Pointer |
| `PictFreeResultBuffer` | Free result buffer | void |
| `PictResetResultFetching` | Reset result iteration | void |
| `PictGetNextResultRow` | Fetch next test case | size_t (rows remaining) |

---

## Additional Resources

- **API Header**: `api/pictapi.h` - Complete API declarations with detailed comments
- **Sample Code**: `api-usage/pictapi-sample.cpp` - Working example demonstrating API usage
- **Test Suite**: `test/` directory - Extensive test cases showing various scenarios
- **Documentation**: `doc/pict.md` - Command-line tool documentation

For more information about combinatorial testing methodology and PICT's algorithms, see the main README.md file in the repository root.
