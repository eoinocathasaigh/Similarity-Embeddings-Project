package ie.atu.sw;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.*;

/**
 * MapAssignment
 * @author Eoin
 * @version Java 17
 * This is a console or command line-based program 
 * that creates a HashMap that reads in words and their respective embeddings
 * from a user specified text file. Furthermore, the program searches for similarities in the words 
 * based on a user’s input and prints those similarities
 * and their respective scores to a user specified output file.
 * 
*/
public class Runner {
	private static String WORD_EMBEDDINGS_FILE = ""; // Assuming the file is in the project directory
	public static String WORD_PRINT_FILE = "output.txt"; //Default word printing file
	
	public static void main(String[] args) throws Exception {
		//You should put the following code into a menu or Menu class
		System.out.println(ConsoleColour.WHITE);
		System.out.println("************************************************************");
		System.out.println("*     ATU - Dept. of Computer Science & Applied Physics    *");
		System.out.println("*                                                          *");
		System.out.println("*          Similarity Search with Word Embeddings          *");
		System.out.println("*                                                          *");
		System.out.println("************************************************************");
		System.out.println("(1) Specify Embedding File");
		System.out.println("(2) Specify an Output File (default: ./output.txt)");
		System.out.println("(3) Search and Write");
		System.out.println("(4) Add a new word to the text file");
		System.out.println("(5) Does word exist?");
		System.out.println("(-1) Quit");
		
		//Menu for the user to interact with
		System.out.print(ConsoleColour.BLACK_BOLD_BRIGHT);
		System.out.print("Select Option [1-?]>");
		System.out.println();
		
		Map<String, double[]> wordEmbeddingsMap = new HashMap<>();
		//loadWordEmbeddings(wordEmbeddingsMap);
        
		//Reading in the user input and calling the appropriate methods & executing the appropriate code
		Scanner keyboard = new Scanner(System.in);
		int userChoice = keyboard.nextInt();
		
		keyboard.nextLine();
		
		System.out.print(ConsoleColour.YELLOW);	//Change the colour of the console text
		int size = 100;							//The size of the meter. 100 equates to 100%
		for (int i =0 ; i < size ; i++) {		//The loop equates to a sequence of processing steps
			printProgress(i + 1, size); 		//After each (some) steps, update the progress meter
			Thread.sleep(10);					//Slows things down so the animation is visible 
		}
		
		//Loop to trap the user until they manually terminate the program
		while (userChoice != -1)
		{
			if(userChoice == 1)//User wants to change the file we're loading the words from
			{
				loadWordEmbeddings(wordEmbeddingsMap);
			}
			else if(userChoice == 2)//User wants to change the file the words are being printed to
			{
				//Specifying the output file to print things to
				//Running time for this method would be O(1) as its not dependent on any iterations of a loop
				String outFile;
				System.out.println("\nPlease enter the name of the file you'd like to print to(Please include the .txt):");
				outFile = keyboard.nextLine();
				//That new specified file becomes the output file
				WORD_PRINT_FILE = outFile;
				
				System.out.println("\nPrinting file successfully changed!\n");
			}
			else if(userChoice == 3)//Searching for a word in the file, finding similarities based on their vectors and printing the top n to a file
			{
				System.out.print("\nEnter a query word you would like to search for in the file: ");
		        String queryWord = keyboard.nextLine().trim();

		        // Check if the query word exists in the map
		        if (!wordEmbeddingsMap.containsKey(queryWord)) {
		            System.out.println("Query word \"" + queryWord + "\" not found in the word embeddings map.");
		            return;
		        }
		        //Asking the user how many of the words they would like to print to the file
		        System.out.print("Enter the number of top similar words to print (enter 'all' to print all similar words): ");
		        String topNInput = keyboard.nextLine().trim().toLowerCase();

		        int topN;
		        if (topNInput.equals("all")) {
		            topN = wordEmbeddingsMap.size() - 1; // Exclude the query word itself
		        } else {
		            try {
		                topN = Integer.parseInt(topNInput);
		            } catch (NumberFormatException e) {
		                System.out.println("Invalid input. Please enter a positive integer or 'all'.");
		                return;
		            }
		        }

		        // Priority queue to store the top n most similar words based on their similarity scores
		        PriorityQueue<Map.Entry<String, Double>> topNWords = new PriorityQueue<>(Comparator.comparingDouble(Map.Entry::getValue));

		        // Get the embedding vector for the query word
		        double[] queryEmbedding = wordEmbeddingsMap.get(queryWord);

		        // Calculate similarity scores for all words in the map using dot product
		        for (Map.Entry<String, double[]> entry : wordEmbeddingsMap.entrySet()) {
		            String word = entry.getKey();
		            if (!word.equals(queryWord)) {
		                double[] embedding = entry.getValue();
		                double similarityScore = calculateDotProductSimilarity(queryEmbedding, embedding);
		                topNWords.offer(Map.entry(word, similarityScore));

		                // Keep only the top n most similar words in the priority queue
		                if (topNWords.size() > topN) {
		                    topNWords.poll(); // Remove the least similar word
		                }
		            }
		        }

		        //Writing the specified amount of words to the text file 
		        try (BufferedWriter writer = new BufferedWriter(new FileWriter(WORD_PRINT_FILE))) {
		            writer.write("Top " + topN + " most similar words for query word \"" + queryWord + "\":\n");
		            while (!topNWords.isEmpty()) {
		                Map.Entry<String, Double> entry = topNWords.poll();
		                writer.write(entry.getKey() + ": " + entry.getValue() + "\n");
		            }
		            System.out.println("Top " + topN + " most similar words for query word \"" + queryWord + "\" written to file: " + WORD_PRINT_FILE);
		        } catch (IOException e) {
		            System.out.println("Error writing to file: " + e.getMessage());
		        }
			}
			else if(userChoice == 4)//Entering a new word to the word embedding file
			{
				//I believe the running time for this would be O(1) due to the known number of iterations in the loop
				try (BufferedWriter writer = new BufferedWriter(new FileWriter(WORD_EMBEDDINGS_FILE, true))) {
		            System.out.print("Enter the new word: "); //Prompting the user for the word
		            String word = keyboard.nextLine().trim();
		            writer.write(word);

		            //Adding the embedding values and ensuring that there's 50 of them
		            for (int i = 0; i < 50; i++) {
		                System.out.print("Enter embedding value " + (i + 1) + ": ");
		                double value = keyboard.nextDouble();
		                writer.write(", " + value);
		            }

		            writer.newLine();
		            System.out.println("Word and embedding added successfully to the file: " + WORD_EMBEDDINGS_FILE);
		        } catch (IOException e) {
		            System.out.println("Error writing to file: " + e.getMessage());
		        }
				
				loadWordEmbeddings(wordEmbeddingsMap);//LOADING THE WORDS AGAIN ONCE THE FILE HAS BEEN UPDATED
			}
			else if(userChoice == 5)
			{
				//Running time of this would also be O(n) where n is the number of entries in the map
				
				System.out.println("\nWelcome to the existance checking system");
				System.out.println("Here you can check to see if a word you're searching for exists in our file!");
				System.out.println("If the word does exist, Happy Days!\nIf it doesnt exist? Theres no need to worry, you can add it to the file with one of our other functions!");
				
				//Prompting the user for the word they wish to search for
				System.out.println("Please enter the word you wish to search for: ");
				String searchWord = keyboard.next();
				
				//checking the word exists in our file
				boolean doesExist = wordEmbeddingsMap.containsKey(searchWord);
				
				//Printing the appropriate message to the console depending on if the word exists or not
				if(doesExist == true)
				{
					System.out.println("Happy Days, the word you're looking for does exist!");
				}
				else
				{
					System.out.println("Oh dear, it seems the word you searched for doesnt exist \nTry searching again and if you want to add it you can from the main menu!");
				}
			}
			
			//Looping the menu again to keep the loop going until the user decides to stop
			System.out.println("(1) Specify Embedding File");
			System.out.println("(2) Specify an Output File (default: ./output.txt)");
			System.out.println("(3) Search and Write");
			System.out.println("(4) Add a new word to the text file");
			System.out.println("(5) Does word exist?");
			System.out.println("(-1) Quit");
			System.out.print(ConsoleColour.BLACK_BOLD_BRIGHT);
			System.out.print("Select Option [1-?]>");
			System.out.println();
			userChoice = keyboard.nextInt();
			keyboard.nextLine();
			System.out.print(ConsoleColour.YELLOW);
			size = 100;
			for (int i =0 ; i < size ; i++) {
				printProgress(i + 1, size);
				Thread.sleep(10); 
			}
		}
		
		//GIVING THE APPROPRIATE MESSAGE FOR THE PROGRAM ENDING
		System.out.println("\nPROGRAM TERMINATED, THANK YOU FOR JOINING US TODAY!\n");
	}
	
	/* Method to load in the words from out word embedding file so they can be used in the program
	 * Based on my understanding of big o notation I 
	 * would say that the running time of this method would be O(n) time as we are simply cycling through
	 * the number of keys/strings and embeddings/doubles in the file
	*/
	/**
	 * This method loads in the word embeddings from the the
	 * user specified text file to the map so that it can be used throughout our code
	 * @param loadWords
	 * @retrun Map full of the keys and their respective double values
	*/
	private static void loadWordEmbeddings(Map<String, double[]> wordEmbeddingsMap) throws IOException {
	    // Search for the file in the current directory and its parent directories
		Scanner scanner = new Scanner(System.in);
	    System.out.print("\nEnter the path of the word embeddings file (or leave empty for default): ");
	    String filePath = scanner.nextLine().trim();

	    // If no file path is specified, use the default file name
	    if (filePath.isEmpty()) {
	        filePath = "./word-embeddings.txt";
	        System.out.println("Loading from default file");
	        WORD_EMBEDDINGS_FILE = filePath;
	    }

	    File file = new File(filePath);
	    if (!file.exists()) {
	        // Search for the file in the current directory and its parent directories
	        file = searchFileInParentDirectories(new File(System.getProperty("user.dir")), file.getName());
	    }

	    if (file != null && file.exists()) {
	        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
	            String line;
	            while ((line = br.readLine()) != null) {
	                String[] separation = line.split(",");
	                String word = separation[0];

	                double[] embeddings = new double[separation.length - 1];
	                for (int i = 1; i < separation.length; i++) {
	                    embeddings[i - 1] = Double.parseDouble(separation[i]);
	                }

	                wordEmbeddingsMap.put(word, embeddings);
	            }
	            System.out.println("Words loaded successfully from file: " + file.getAbsolutePath());
	        }
	    } else {
	        System.out.println("Word embeddings file not found.");
	    }
	}

	// Method to search for a file in the current directory and its parent directories
	/**
	 * Method to search for a files existence within the current and parent directories
	 * @param current file directory, and the specified file name by the user
	 * @return the correct file, assuming it is found within and no errors have occurred
	 * */
	private static File searchFileInParentDirectories(File directory, String filename) {
	    File file = new File(directory, filename);
	    if (file.exists()) {
	        return file;
	    } else {
	        File parent = directory.getParentFile();
	        if (parent != null) {
	            return searchFileInParentDirectories(parent, filename);
	        } else {
	            return null;
	        }
	    }
	}
	
	/* METHOD TO CALCULATE DOT PRODUCT SIMILARITY, I USED DOT PRODUCT AS I FOUND IT THE EASIEST TO UNDERSTAND
	 * The running time of this method would be linear or O(n)
	 * where n is the length of the embedding arrays
	 */
    /**
     * Method to calculate the dot product similarity value of each key for the comparison
     * @param embeddings of the queried word & embeddings of the word at the current index
     * @return the dot product of both the words
     * */
	private static double calculateDotProductSimilarity(double[] embedding1, double[] embedding2) {
        double dotProduct = 0;
        for (int i = 0; i < embedding1.length; i++) {
            dotProduct += embedding1[i] * embedding2[i];
        }
        return dotProduct;
    }
	
	public static void printProgress(int index, int total) {
		if (index > total) return;	//Out of range
        int size = 50; 				//Must be less than console width
	    char done = '/';			//Change to whatever you like.
	    char todo = '-';			//Change to whatever you like.
	    
	    //Compute basic metrics for the meter
        int complete = (100 * index) / total;
        int completeLen = size * complete / 100;
        
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < size; i++) {
        	sb.append((i < completeLen) ? done : todo);
        }
        
        System.out.print("\r" + sb + "] " + complete + "%");
        
        //Once the meter reaches its max, move to a new line.
        if (done == total) System.out.println("\n");
    }
}