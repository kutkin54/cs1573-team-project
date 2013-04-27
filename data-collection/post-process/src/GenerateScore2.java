import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.HashMap;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/*
 * Class GenerateScore2:
 *
 * Places all actors, writers, and directors from two files,
 * a training and test file,
 * into several HashMaps, the key being the name of the said actor/director/writer
 * and the value being an ArrayList of rating values times some coefficient
 * to mark the position in the film (lead actors get more weight than the
 * fifth actor mentioned).
 *
 * These maps then used to generate a total actor, writer, and director
 * score for each film, which replaces the list of actors, writers, and
 * directors, respectively, for that example.
 */

public class GenerateScore2 {

	HashMap<String, ArrayList<Double>> trainActors;
	HashMap<String, ArrayList<Double>> trainWriters;
	HashMap<String, ArrayList<Double>> trainDirectors;

	HashMap<String, ArrayList<Double>> testActors;
	HashMap<String, ArrayList<Double>> testWriters;
	HashMap<String, ArrayList<Double>> testDirectors;

	File training;
	File testing;
	
	double average = 0.0;
	String attributes = "";

	public GenerateScore2(String inputTraining, String _attributes) {
		// reads from two text files
		training = new File(inputTraining);
		attributes = _attributes;
		/*
		testing = new File(inputTesting);

		trainActors = new HashMap<String, ArrayList<Double>>();
		trainWriters = new HashMap<String, ArrayList<Double>>();
		trainDirectors = new HashMap<String, ArrayList<Double>>();

		testActors = new HashMap<String, ArrayList<Double>>();
		testWriters = new HashMap<String, ArrayList<Double>>();
		testDirectors = new HashMap<String, ArrayList<Double>>();
		*/
	}

	private void addToMap(HashMap<String, ArrayList<Double>> m, String str,
			Double rating) {
		ArrayList<String> list = new ArrayList<String>(Arrays.asList(str
				.split(",")));
		for (int i = 0; i < list.size(); i++) {
			String key = list.get(i);
			ArrayList<Double> values = m.get(key);
			if (values == null) {
				values = new ArrayList<Double>();
				m.put(key, values);
			}
			Double v = 1.0;
			switch (i) {
			case 0: {
				v += 10 * rating;
				break;
			}
			case 1: {
				v += 5 * rating;
				break;
			}
			case 2: {
				v += 1 * rating;
				break;
			}
			case 3: {
				v += 0.5 * rating;
				break;
			}
			case 4: {
				v += 0.3 * rating;
				break;
			}
			}
			values.add(v);
		}

	}

	private void buildHashMaps() {
		
		int count = 0;
		try {
			Scanner scanner = new Scanner(training);
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				ArrayList<String> example = new ArrayList<String>(
						Arrays.asList(line.split("\t")));
				if (example.get(example.size()-1) != null) {
					Double rating = Double.parseDouble(example.get(example.size()-1));
					addToMap(trainActors, example.get(1), rating);
					addToMap(trainDirectors, example.get(2), rating);
					addToMap(trainWriters, example.get(3), rating);
					average += rating;
				}
				count++;
			}
			average /= count;
			scanner.close();

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public void CreateCSV(String outputTraining) {
		//buildHashMaps();

		// assume files don't exist
		File train_data = new File(outputTraining);
		//File test_data = new File(outputTesting);

		try {
			train_data.createNewFile();
			Scanner scanner = new Scanner(training);
			FileWriter train_fw = new FileWriter(train_data.getAbsoluteFile());
			BufferedWriter train_bw = new BufferedWriter(train_fw);
			train_bw.write("rating, ");
			train_bw.write(attributes);
			train_bw.write("\n");
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();

				ArrayList<String> example = new ArrayList<String>(
						Arrays.asList(line.split("\t")));
				/*
				Double a = calcScore(example.get(4), trainActors);
				Double d = calcScore(example.get(5), trainDirectors);
				Double w = calcScore(example.get(6), trainWriters);
				*/
				String outputline = example.get(2) + ",";// + a + "," + d + "," + w + ",";
				for(int i=7; i<example.size()-1; i++){
					outputline += example.get(i)+",";
				}
				outputline += example.get(example.size()-1);
				outputline += "\n";
				train_bw.write(outputline);
			}
			scanner.close();
			train_bw.close();
			train_bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		/*
		// then testing
		try {
			test_data.createNewFile();
			FileWriter test_fw = new FileWriter(test_data.getAbsoluteFile());
			BufferedWriter test_bw = new BufferedWriter(test_fw);
			Scanner scanner = new Scanner(testing);
			test_bw.write("class, \n");
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				ArrayList<String> example = new ArrayList<String>(
						Arrays.asList(line.split("\t")));
				/*
				Double a = calcScore(example.get(1), trainActors);
				Double d = calcScore(example.get(2), trainDirectors);
				Double w = calcScore(example.get(3), trainWriters);
				
				String outputline = example.get(0);// + "," + a + "," + d + "," + w + ",";
				for(int i=4; i<example.size()-1; i++){
					outputline += example.get(i)+",";
				}
				outputline += example.get(example.size()-1);
				outputline += "\n";
				test_bw.write(outputline);
			}
			scanner.close();
			test_bw.close();
			test_bw.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
	}
	
	

	private Double calcScore(String a, HashMap<String, ArrayList<Double>> m) {
		Double rating = 0.0;
		if (a == null) {
			return rating;
		}
		ArrayList<String> list = new ArrayList<String>(Arrays.asList(a
				.split(",")));
		for (int i = 0; i < list.size(); i++) {
			ArrayList<Double> values = m.get(list.get(i));
			if (values == null) {
				rating +=average;
			} else {
				double total = 0;
				for (int j = 0; j < values.size(); j++) {
					total += values.get(j);
				}
				total /= values.size();
				rating += total;
			}
		}
		return rating;
	}

	public float Normalize(float var, float max, float min) {
		float answer = var - min;
		answer /= (max - min);
		return answer;
	}

}