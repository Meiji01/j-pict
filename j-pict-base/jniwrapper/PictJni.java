package com.microsoft.pict;

/**
 * Java Native Interface wrapper for PICT (Pairwise Independent Combinatorial Testing) API.
 * 
 * This class provides Java bindings for the PICT C++ library, enabling combinatorial
 * test case generation from Java applications.
 * 
 * Usage example:
 * <pre>
 * PictJni pict = new PictJni();
 * 
 * // Create task and model
 * long task = pict.createTask();
 * long model = pict.createModel(0);
 * 
 * // Add parameters (e.g., 3 parameters with 2, 3, and 4 values)
 * long param1 = pict.addParameter(model, 2, 2, null);
 * long param2 = pict.addParameter(model, 3, 2, null);
 * long param3 = pict.addParameter(model, 4, 2, null);
 * 
 * // Set the root model and generate
 * pict.setRootModel(task, model);
 * int result = pict.generate(task);
 * 
 * if (result == PictJni.PICT_SUCCESS) {
 *     // Fetch results
 *     long paramCount = pict.getTotalParameterCount(task);
 *     long resultBuffer = pict.allocateResultBuffer(task);
 *     pict.resetResultFetching(task);
 *     
 *     long[] row = new long[(int)paramCount];
 *     while (pict.getNextResultRow(task, resultBuffer, row) > 0) {
 *         // Process row (contains value indices for each parameter)
 *         for (long value : row) {
 *             System.out.print(value + " ");
 *         }
 *         System.out.println();
 *     }
 *     
 *     pict.freeResultBuffer(resultBuffer);
 * }
 * 
 * // Cleanup
 * pict.deleteModel(model);
 * pict.deleteTask(task);
 * </pre>
 */
public class PictJni {
    
    // Return codes
    public static final int PICT_SUCCESS = 0x00000000;
    public static final int PICT_OUT_OF_MEMORY = 0xc0000001;
    public static final int PICT_GENERATION_ERROR = 0xc0000002;
    
    // Defaults
    public static final int PICT_PAIRWISE_GENERATION = 2;
    public static final int PICT_DEFAULT_RANDOM_SEED = 0;
    
    static {
        // Load the native library
        System.loadLibrary("pictjni");
    }
    
    // Task Management
    
    /**
     * Creates a new PICT task.
     * @return Handle to the task, or 0 if allocation failed
     */
    public native long createTask();
    
    /**
     * Deletes a task and frees its resources.
     * @param task Handle to the task to delete
     */
    public native void deleteTask(long task);
    
    // Model Management
    
    /**
     * Creates a new PICT model with an optional random seed.
     * @param randomSeed Seed for randomizing the generation algorithm (use PICT_DEFAULT_RANDOM_SEED for default)
     * @return Handle to the model, or 0 if allocation failed
     */
    public native long createModel(int randomSeed);
    
    /**
     * Deletes a model and all its parameters and child models.
     * @param model Handle to the model to delete
     */
    public native void deleteModel(long model);
    
    /**
     * Associates a model with a task as the root model.
     * @param task Handle to the task
     * @param model Handle to the model to set as root
     */
    public native void setRootModel(long task, long model);
    
    // Parameter Management
    
    /**
     * Adds a parameter to a model.
     * @param model Handle to the model
     * @param valueCount Number of values this parameter has
     * @param order Order of combinations to generate (e.g., 2 for pairwise)
     * @param valueWeights Optional array of weights for each value (null for equal weights)
     * @return Handle to the parameter, or 0 if allocation failed
     */
    public native long addParameter(long model, long valueCount, int order, int[] valueWeights);
    
    /**
     * Attaches a child model to a parent model to create a hierarchy.
     * @param modelParent Handle to the parent model
     * @param modelChild Handle to the child model
     * @param order Order of combinations to generate with the submodel
     * @return PICT_SUCCESS or an error code
     */
    public native int attachChildModel(long modelParent, long modelChild, int order);
    
    // Constraints
    
    /**
     * Adds an exclusion constraint to the task.
     * Exclusions define parameter-value combinations that should not appear in the output.
     * @param task Handle to the task
     * @param parameters Array of parameter handles
     * @param valueIndices Array of value indices (0-based) corresponding to the parameters
     * @return PICT_SUCCESS or an error code
     */
    public native int addExclusion(long task, long[] parameters, long[] valueIndices);
    
    /**
     * Adds a seed constraint to the task.
     * Seeds define parameter-value combinations that must appear in the output.
     * @param task Handle to the task
     * @param parameters Array of parameter handles
     * @param valueIndices Array of value indices (0-based) corresponding to the parameters
     * @return PICT_SUCCESS or an error code
     */
    public native int addSeed(long task, long[] parameters, long[] valueIndices);
    
    // Generation
    
    /**
     * Generates the combinatorial test cases.
     * Call this after the model is fully configured.
     * @param task Handle to the task
     * @return PICT_SUCCESS, PICT_OUT_OF_MEMORY, or PICT_GENERATION_ERROR
     */
    public native int generate(long task);
    
    // Result Handling
    
    /**
     * Allocates a buffer for holding one result row.
     * Must be freed with freeResultBuffer when done.
     * @param task Handle to the task
     * @return Handle to the buffer, or 0 if allocation failed
     */
    public native long allocateResultBuffer(long task);
    
    /**
     * Frees a result buffer allocated by allocateResultBuffer.
     * @param resultRow Handle to the result buffer
     */
    public native void freeResultBuffer(long resultRow);
    
    /**
     * Resets result fetching to start from the beginning of the result set.
     * @param task Handle to the task
     */
    public native void resetResultFetching(long task);
    
    /**
     * Fetches the next result row.
     * @param task Handle to the task
     * @param resultRow Handle to the result buffer
     * @param output Array to receive the value indices for each parameter
     * @return Number of rows remaining BEFORE this fetch (0 when no more rows)
     */
    public native long getNextResultRow(long task, long resultRow, long[] output);
    
    /**
     * Gets the total number of parameters in the task.
     * @param task Handle to the task
     * @return Total parameter count
     */
    public native long getTotalParameterCount(long task);
}
