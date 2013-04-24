import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class ModelTree { 
	private final List<Feature> features;
	private final List<Data> trainingData;
	//private final List<Data> testData;
	private Node root;
	private LinearRegression linearRegression;
	private FastVector continuousAttributes;
	
	private int trainingCorrect;
	private double trainingAccuracy;
	private int testCorrect;
	private double testAccuracy;
	
	// debug stuff
	protected static final boolean DEBUG = ModelTreeTest.DEBUG;
	private static final String HR = "---------------------------";
	private List<Double> deviations;
	
	/*
	 * Creates a ModelTree object and calculates the accuracy on
	 * the training and test data.
	 * @param features list of features
	 * @param trainingData list of training data
	 * @param testData list of test data
	 */
	public ModelTree(List<Feature> features, List<Data> trainingData) throws Exception {
		this.features = new ArrayList<Feature>(features); // we mess with the features array
		this.trainingData = trainingData;
		
		// initialize our leaf-node linear classifier
		// each leaf node is just re-trained on the same classifier
		linearRegression = new LinearRegression();
		// set up our continuous feature vector
		continuousAttributes = new FastVector(2);
		continuousAttributes.addElement(new Attribute("rating"));
		for (int i = 1; i <= trainingData.get(0).getContinuousSize(); i++) {
			continuousAttributes.addElement(new Attribute("attr" + i));
		}
		
		// initialize debugging stuff
		deviations = new ArrayList<Double>();
		
		// create our tree
		root = M5(this.trainingData, this.features);
		
		// see how we did
		
		/*
		trainingCorrect = numberCorrect(trainingData);
		trainingAccuracy = (double)trainingCorrect / trainingData.size();
		testCorrect = numberCorrect(testData);
		testAccuracy = (double)testCorrect / testData.size();
		*/
	}
	
	public Node M5(List<Data> examples, List<Feature> features) throws Exception {
		return M5(examples, features, 0);
	}
	
	public Node M5(List<Data> examples, List<Feature> features, int depth) throws Exception {
		Node parent = new Node();
		
		if (DEBUG) {
			System.out.println(padLevel(depth) + "M5 with " + examples.size() + " examples and " + features.size() + " features");	
		}
		
		Feature chosen = null;
		double min = Double.POSITIVE_INFINITY;
		List<List<Data>> subsets = null;
		for (Feature feature : features) {
			List<List<Data>> sub = classifyByFeatureValue(examples, feature);
			
			// find total count (including duplicates)
			double count = 0;
			for (List<Data> subset : sub) {
				count += subset.size();
			}
			
			// find standard deviation and adjust for size of subset
			double temp = 0;
			for (List<Data> subset : sub) {
				// only compute standard deviation if subset has more than one example
				if (subset.size() > 1) {
					double stddev = standardDeviation(subset);
					temp += stddev * Math.sqrt(1 - subset.size() / count); // punish small subsets
				}
			}
			
			// if this feature is better than previous features, update accordingly
			if (temp < min) {
				min = temp;
				chosen = feature;
				subsets = sub;
			}
		}
		
		parent.setFeature(chosen);
		if (DEBUG) {
			System.out.println(padLevel(depth) + "chose feature " + chosen.getName() + " with dev = " + min);
			deviations.add(min);
		}
		
		// we now know chosen is the feature we are going to use
		LinearEquation equation = null;
		double average = Double.NaN;
		for (int i = 0; i < subsets.size(); i++) {
			//System.out.println(padLevel(depth + 1) + chosen.getValue(i) + " size is " + subsets.get(i).size());
			int size = subsets.get(i).size();
			if (size <= 2 || size < ModelTreeTest.MIN_SUBSET_SIZE || min < ModelTreeTest.MIN_DEVIATION || features.size() == 1) {
				// run examples through perceptron and create leaf node
				if (equation == null) {
					Node child = new Node(examples, true);
					parent.addChild(chosen.getValue(i), child);
					equation = child.getEquation();
					average = child.getOutputAvg();
				} else {
					parent.addChild(chosen.getValue(i), new Node(examples, equation, average));
				}
				
			} else {
				// only remove the feature for our recursion
				List<Feature> smaller = new ArrayList<Feature>(features);
				smaller.remove(chosen);
				parent.addChild( chosen.getValue(i), M5(subsets.get(i), smaller, depth + 1) );
			}
		}
		
		return parent;
	}
	
	/*
	 * Compute sample standard deviation of set of example outputs.
	 * Assume that examples contains at least two data elements.
	 * @param examples set of data examples
	 * @return sample standard deviation
	 */
	public double standardDeviation(List<Data> examples) {
		// compute average
		double average = 0;
		for (int i = 0; i < examples.size(); i++) {
			average += examples.get(i).getOutput();
		}
		average /= examples.size();
		
		double result = 0;
		for (int i = 0; i < examples.size(); i++) {
			double temp = examples.get(i).getOutput() - average;
			result += temp * temp;
		}
		result /= examples.size() - 1;
		return Math.sqrt(result);
	}
	
	/*
	 * Returns a list of leaf-node deviations. Used for tweaking
	 * the pruning factors.
	 */
	public List<Double> getDebugDeviations() {
		return deviations;
	}
	
	/*
	 * Classify set by the values of a specified feature
	 * @param set set of data examples
	 * @param feature feature to classify examples by
	 * @return list of subsets classified by feature value
	 */
	private List<List<Data>> classifyByFeatureValue(List<Data> set, Feature feature) {
		// get the values for this feature
		List<String> featureValues = feature.getValues();
				
		// initialize our subsets
		List<List<Data>> subsets = new ArrayList<List<Data>>(featureValues.size());
		for (int i = 0; i < featureValues.size(); i++) {
			subsets.add( new ArrayList<Data>() );
		}
				
		// classify the set according to chosen feature value
		for (Data example : set) {
			// iterate through multiple feature values
			for (String value : example.getDiscrete(feature)) {
				subsets.get(featureValues.indexOf(value)).add(example);
			}
		}
		return subsets;
	}
	
	/*
	 * @param example example to test
	 * @param outputPredictions whether or not to output predictions
	 * @return true if tree predicted correctly; false otherwise
	 */
	public double testExample(Data example, boolean outputPredictions) {
		return testExample(this.root, example, outputPredictions);
	}
	
	/*
	 * @param root root node of tree
	 * @param example example to test
	 * @return average squared difference
	 */
	private double testExample(Node root, Data example, boolean outputPredictions) {
		if (!root.isLeaf()) {
			List<String> values = example.getDiscrete(root.getFeature());
			
			// take the average prediction given multiple feature values
			double average = 0;
			for (String value : values) {
				average += testExample(root.getChild(value), example, outputPredictions);
			}
			
			return average / values.size();
		} else {
			//System.out.println(ModelTreeTest.formatArray(example.getContinuousArray()));
			double prediction = root.solve(example.getContinuousArray());
			//double prediction = root.getOutputAvg();
			double result = example.getOutput() - prediction;
			if (outputPredictions) {
				System.out.println(example.getOutput() + "\t" + prediction + "\t" + result);
			}
			//double temp = example.getOutput() - root.getOutputAvg();
			return result * result;
		}
	}
	
	public static double computeAverage(List<Data> examples) {
		double avg = 0;
		for (Data example : examples) {
			avg += example.getOutput();
		}
		return avg / examples.size();
	}
	
	public void printTree() {
		printSubtree(root, 0);
	}
	
	/*
	 * @param root root node of tree
	 * @param level depth of recursion
	 */
	private void printSubtree(Node root, int level) {
		Iterator<Entry<String, Node>> it = root.getChildren().entrySet().iterator();
		while (it.hasNext()) {
			System.out.print(padLevel(level));
			Entry<String, Node> leaf = it.next();
			System.out.print(root.getFeatureName() + "=" + leaf.getKey());
			if (leaf.getValue().isLeaf()) {
				System.out.println(" " + leaf.getValue().getEquation().toString());
			} else {
				System.out.println();
				printSubtree(leaf.getValue(), level + 1);
			}
		}
	}
	
	/*
	 * Helper function that prints some spaces.
	 * @param level depth of recursion
	 */
	private String padLevel(int level) {
		if (level < 1) {
			return "";
		} else {
			String padding = "";
			for (int i = 0; i < level; i++) {
				padding += "    ";
			}
			return padding;
		}
	}
	
	/*
	 * ModelTree node represents a feature or a classification value.
	 * If the node represents a feature, it has n children represented as
	 * a Map<String, Node> where the n key values represent the possible
	 * values for that feature.
	 */
	protected class Node {
		private Feature feature;
		private Map<String, Node> children;
		private List<Data> examples;
		private LinearEquation output;
		private double outputAvg;
		
		public Node() {
			feature = null;
			children = null;
			examples = null;
			output = null;
			outputAvg = Double.NaN;
		}
		
		public Node(List<Data> examples) throws Exception {
			this(examples, false);
		}
		
		public Node(List<Data> examples, boolean leaf) throws Exception {
			this();
			this.examples = examples;
			if (leaf) {
				output = new LinearEquation(perceptron(examples));
				outputAvg = computeAverage(examples);
			}
		}
		
		public Node(List<Data> examples, LinearEquation eq, double avg) {
			this();
			this.examples = examples;
			output = eq;
			outputAvg = avg;
		}
		
		public Feature getFeature() {
			return feature;
		}
		
		public String getFeatureName() {
			return feature.getName();
		}
		
		public Map<String, Node> getChildren() {
			return children;
		}
		
		public Node getChild(String key) {
			return children.get(key);
		}
				
		public double solve(double[] x) {
			return output.solve(x);
		}
		
		public double getOutputAvg() {
			return outputAvg;
		}
		
		public LinearEquation getEquation() {
			return output;
		}
		
		public List<Data> getExamples() {
			return examples;
		}
		
		public void setFeature(Feature feature) {
			this.feature = feature;
		}
		
		public void addChild(String key, Node node) {
			if (children == null) {
				children = new HashMap<String, Node>();
			}
			children.put(key, node);
		}
		
		public int countChildren() {
			if (children == null) {
				return 0;
			} else {
				return children.size();
			}
		}
		
		public boolean isLeaf() {
			if (output == null) {
				return false;
			} else {
				return true;
			}
		}
		
		private double[] perceptron(List<Data> examples) throws Exception {
			Instances dataSet = new Instances("data-set", continuousAttributes, ModelTreeTest.MIN_SUBSET_SIZE);
			dataSet.setClassIndex(0);
			
			for (Data example : examples) {
				double[] attrs = new double[example.getContinuousSize() + 1];
				attrs[0] = example.getOutput();
				System.arraycopy(example.getContinuousArray(), 0, attrs, 1, attrs.length - 1);
				//System.out.println(attrs[0] + "," + attrs[1] + "," + attrs[2] + "," + attrs[3] + "," + attrs[4]);
				dataSet.add(new Instance(1, attrs));
				
				/*
				int count = 0;
				System.out.print(example.getOutput() + ",");
				for (double temp : example.getContinuousArray()) {
					System.out.print(temp);
					count++;
					if (count < example.getContinuousSize()) {
						System.out.print(",");
					} else {
						System.out.println();
					}
				}
				*/
			}
			
			double weights[] = new double[examples.get(0).getContinuousSize() + 1];
			linearRegression.buildClassifier(dataSet);
			double coef[] = linearRegression.coefficients();
			
			// first coefficient is always 0 -- scrap it
			weights[0] = coef[coef.length - 1];
			for (int i = 1; i < coef.length - 1; i++) {
				weights[i] = coef[i]; 
			}
			
			return weights;
		}
	}
}
