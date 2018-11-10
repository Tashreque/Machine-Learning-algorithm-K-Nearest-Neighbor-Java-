import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

public class classify {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		ArrayList<String> putToExamples = new ArrayList<String>();
		ArrayList<String> nExamples = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(args[0]));
			String str = "";
			while((str = br.readLine()) != null) {
				putToExamples.add(str.trim());
			}
			br.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
		
		ArrayList<String> testFiles = new ArrayList<String>();
		ArrayList<String> nTests = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(new FileReader(args[1]));
			String str = "";
			while((str = br.readLine()) != null) {
				testFiles.add(str.trim());
			}
			br.close();
		}catch(IOException e) {
			e.printStackTrace();
		}
		
		classify cl = new classify();
		String []attributes = putToExamples.get(0).split("\\s+");
		if(args[2].equals("knn")) {
			nExamples = cl.normalize(putToExamples, attributes.length-1);
			nTests = cl.normalize(testFiles, attributes.length-1);
			//System.out.println(nExamples);
			ArrayList<String> labels = cl.classifier(nExamples, nTests, Integer.parseInt(args[3]), attributes.length-1);
			cl.Accuracy(labels, attributes.length-1, testFiles);
		}
		else {
			System.out.println("Wrong classifier!");
		}
		
		//System.out.println(labels);

	}
	
	public ArrayList<String> classifier(ArrayList<String> examples, ArrayList<String> tests, int k, int l){
		
		
		ArrayList<Double> Distances = new ArrayList<Double>();
		ArrayList<String> testLabels = new ArrayList<String>(); 
		HashMap<Double, Integer> hmap = new HashMap<Double, Integer>();
		//double [][]mapper = new double[examples.size()][1];
		for(int i=0; i<tests.size(); i++) {
			for(int j=0; j<examples.size(); j++) {
				double d = getEuclideanDistance(tests.get(i), examples.get(j), l);
				Distances.add(d);
				hmap.put(d, j);
			}
			
			Collections.sort(Distances);
			
			testLabels.add(decideLabel(Distances, hmap, k, examples));
			Distances.clear();
			hmap.clear();
		}
		return testLabels;
	}
	
	
	public ArrayList<String> normalize(ArrayList<String> examples, int l) {
		ArrayList<String> normalized = new ArrayList<String>();
		ArrayList<Double> attributeValues = new ArrayList<Double>();
		double[]minimum = new double[l];
		double[]maximum = new double[l];
		
		for(int i=0; i<l; i++) {
			attributeValues = SelectColumn(examples, i);
			minimum[i] = min(attributeValues);
			maximum[i] = max(attributeValues);
		}
		String str = "";
		for(int i=0; i<examples.size(); i++) {
			String []tokens = examples.get(i).split("\\s+");
			for(int j=0; j<tokens.length-1; j++) {
				double val = (Double.parseDouble(tokens[j]) - minimum[j])/(maximum[j] - minimum[j]);
				//System.out.println(val);
				if(Double.isNaN(val)) {
					val = 0;
				}
				str = str + val +" ";
			}
			normalized.add(str +tokens[tokens.length-1]);
			str = "";
		}
		return normalized;
	}
	
    public ArrayList<Double> SelectColumn(ArrayList<String> examples, int a){
		
		ArrayList<Double> columnValues = new ArrayList<Double>();
		for(int i=0; i<examples.size(); i++) {
			String []tokens = examples.get(i).split("\\s+");
			columnValues.add(Double.parseDouble(tokens[a]));
		}
		return columnValues;
	}
    
    public double max(ArrayList<Double> array) {
		double maxValue = array.get(0);
		for(int i=1; i<array.size(); i++) {
			if(array.get(i) > maxValue) {
				maxValue = array.get(i);
			}
		}
		return maxValue;
	}
	
	public double min(ArrayList<Double> array) {
		double minValue = array.get(0);
		for(int i=1; i<array.size(); i++) {
			if(array.get(i) < minValue) {
				minValue = array.get(i);
			}
		}
		return minValue;
	}
	
	public double getEuclideanDistance(String tests, String examples, int l) {
		
		double distance = 0;
		String []tTest = tests.split("\\s+");
		String []tExample = examples.split("\\s+");
		for(int i=0; i<l; i++) {
			
			distance = distance + Math.pow((Double.parseDouble(tTest[i]) - Double.parseDouble(tExample[i])), 2);
		}
		//System.out.println(Math.sqrt(distance));
		return Math.sqrt(distance);
	}
	
	public String decideLabel(ArrayList<Double> distances, HashMap<Double, Integer> hmap, int k, ArrayList<String> examples) {

		//String[]tokens = examples.get(0).split("\\s+");
		//ArrayList<Integer> frequencies = new ArrayList<Integer>();
		ArrayList<Integer> labels = new ArrayList<Integer>();
		ArrayList<Double> shortestDistances = new ArrayList<Double>();
		ArrayList<Integer> uniqueLabels = new ArrayList<Integer>();
		//int b = 0;
		/*b = hmap.get(distances.get(0));
		//System.out.println(b);
		label = Integer.parseInt(tokens[tokens.length-1]);
		classLabels.add(label);*/
		
		for(int i=0; i<k; i++) {
			shortestDistances.add(distances.get(i));
			int row = hmap.get(distances.get(i));
			String []tokens = examples.get(row).split("\\s+");
			labels.add(Integer.parseInt(tokens[tokens.length-1]));
		}
		
		for(int i=0; i<labels.size(); i++) {           //for root
			if(!uniqueLabels.contains(labels.get(i))) {
				uniqueLabels.add(labels.get(i));
			}
		}
		int []classes = new int[uniqueLabels.size()];
		for(int i=0; i<classes.length; i++) {
			classes[i] = 0;
		}
		for(int i=0; i<uniqueLabels.size(); i++) {
			for(int j=0; j<labels.size(); j++) {
				if(labels.get(j).equals(uniqueLabels.get(i))) {
					classes[i] = classes[i] + 1;
				}
			}
		}
		
		int max = classes[0];
		for(int i=1; i<classes.length; i++) {
			if(classes[i] > max) {
				max = classes[i];
			}
		}
		int id = 0;
		double distance = 0;
		for(int i=0; i<k; i++) {
			if(max == labels.get(i)) {
				distance = shortestDistances.get(i);
				id = hmap.get(distance);
				break;
			}
		}
		String results = max+" "+id+" "+distance;
		return results;
	}
	
	public void Accuracy(ArrayList<String> results, int l, ArrayList<String> testFile) {
		int accuracy=0;
		int totalAccuracy = 0;
		for(int i=0; i<testFile.size(); i++) {
			
			String []tokens = testFile.get(i).split("\\s+");
			String []tokens1 = results.get(i).split("\\s+");
			if(tokens1[0].equals(tokens[l])) {
				accuracy = 1;
				totalAccuracy = totalAccuracy + 1;
			}
			else {
				accuracy = 0;
			}
			System.out.println("ID="+i+" "+"predicted="+tokens1[0]+" "+"true="+Integer.parseInt(tokens[l])+" "+"nn="+tokens1[1]+" "+"distance="+tokens1[2]+" "+"accuracy="+accuracy);
			System.out.println("classification accuracy="+totalAccuracy);
		}
	}

}
