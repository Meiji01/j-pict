package jpict_usage;

import com.microsoft.pict.PictJni;

public class nativewrapper {
	
	public static void main(String[] args) {
	       PictJni pict = new PictJni();
	        
	        // Create task and model
	        long task = pict.createTask();
	        long model = pict.createModel(PictJni.PICT_DEFAULT_RANDOM_SEED);
	        
	        String[] parameterLabels = new String[] { "Parameter1", "Parameter2", "Parameter3" };
	        String[][] paramValues = {
	        		{ "test1", "test2", "test3" }, // Parameter 1 with 3 values
	        		{ "comb1", "comb2" },          // Parameter 2 with 2 values
	        		{ "100", "300", "500" }        // Parameter 3 with 3 values
	        }; //paramValues should be 2 dimentional array, element 1 is parameter, element 2 is value labels

	        // Add parameters with their labels
	        //dynamic add parameters based on paramValues array
	        
	        for (int i = 0; i < paramValues.length; i++) {
	            pict.addParameter(model, paramValues[i].length, PictJni.PICT_PAIRWISE_GENERATION, null);
	        }
	        
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
	                                
	                //printing of row combination with value labels
	                for (int i = 0; i < paramCount; i++) {
	                    //System.out.print("xrow"+ row[i]);
	                	System.out.print(paramValues[i][(int)row[i]]);
	                    
	                    if (i < paramCount - 1) {
	                    	System.out.print(", ");
	                    }
	                    
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


