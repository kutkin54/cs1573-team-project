import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Evaluate {
	private double rms;
	private double normRMS;
	
	public Evaluate(ModelTree tree, List<Data> test) {
		rms = 0;
		normRMS = 0;
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		for (Data example : test) {
			double temp = tree.testExample(example, ModelTreeTest.PREDICTIONS); // testExample returns square of difference
			rms += temp;
			if (temp < min) {
				min = temp;
			}
			if (temp > max) {
				max = temp;
			}
		}
		rms = Math.sqrt(rms / test.size());
		normRMS = rms / (max - min);
		/*
		if (ModelTreeTest.PREDICTIONS) {
			System.out.println(rms + "\t" + normRMS);
			System.exit(0);
		}
		*/
	}
	
	public double getRMS() {
		return rms;
	}
	
	public double getNormRMS() {
		return normRMS;
	}
	
	/*
	 * @param array double array to average
	 * @return average of double array
	 */
	public static double average(List<Float> list) {
		double sum = 0;
		for (int i = 0; i < list.size(); i++) {
			sum += list.get(i);
		}
		return sum / list.size();
	}
	
	/*
	 * @param examples data set of example ratings
	 * @return root-mean-squared deviation
	 */
	public static double rootMeanSquare(List<Float> examples) {
		return rootMeanSquare(examples, false);
	}
	
	/*
	 * Baseline evaluation always predicts average movie rating
	 * @param examples data set of example ratings
	 * @param normalize whether or not to normalize the deviation
	 * @return root-mean-square or normalized root-mean-square deviation
	 */
	public static double rootMeanSquare(List<Float> examples, boolean normalize) {
		double result = 0;
		double min = Double.MAX_VALUE;
		double max = Double.MIN_VALUE;
		double avg = average(examples);
		for (Float example : examples) {
			double temp = avg - example.floatValue();
			temp *= temp;
			result += temp;
			if (normalize) {
				if (temp < min) {
					min = temp;
				}
				if (temp > max) {
					max = temp;
				}	
			}
		}
		result = Math.sqrt(result / examples.size());
		if (normalize) {
			result /= max - min;
		}
		return result;
	}
	
	/*
	 * Splits a list into k distinct subsets in preparation for
	 * k-fold validation.
	 * @param list list to split
	 * @param k number of distinct subsets
	 * @return k distinct subsets
	 */
	public static List<List<Float>> splitList(List<Float> list, int k) {
		List<List<Float>> subsets = new ArrayList<List<Float>>();
		int size = list.size() / k; //5
		for (int i = 0; i < k; i++) {
			int max = (i + 1) * size - 1;
			if (i == k - 1) {
				// our last subset is probably going to bigger
				max = list.size() - 1;
			}
			subsets.add( list.subList(i * size, max) );
		}
		return subsets;
	}
	
	public static void main(String[] args) throws ParseException {
		Scanner scanner = Parse.openFile("../data-collection/datasets/usa/usa_data_5000.txt");
		List<Float> ratings = new ArrayList<Float>();
		
		// bring all of the ratings into memory
		while (scanner.hasNextLine()) {
			String[] example = scanner.nextLine().split("\t");
			ratings.add(Float.parseFloat(example[2]));
		}
		
		//System.out.println(rootMeanSquare(ratings, false));
		//System.exit(0);
		
		List<List<Float>> subsets = splitList(ratings, 10);
		
		// test on each training set of the 10-folds
		for (int test = 0; test < subsets.size(); test++) {
			// create our training set
			List<Float> train = new ArrayList<Float>();
			for (int i = 0; i < subsets.size(); i++) {
				if (i != test) {
					train.addAll(subsets.get(i));
				}
			}
			
			// evaluate our training set
			System.out.println("(" + test + ")\t" + rootMeanSquare(train, false));
		}
	}
}
