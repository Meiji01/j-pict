package jpict_usage;

import com.microsoft.pict.PictJni;

public class nativewrapper {
	
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


