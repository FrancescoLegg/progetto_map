import data.Data;
import data.TrainingDataException;
import tree.RegressionTree;
import tree.UnknownValueException;
import utility.Keyboard;

public class MainTest {

	public static void main(String[] args) {
		System.out.println("Training set:");
		String fileName = Keyboard.readString();

		Data trainingSet;
		try {
			System.out.println("Starting data acquisition phase!");
			trainingSet = new Data(fileName);
		} catch (TrainingDataException e) {
			System.out.println(e);
			return;
		}

		System.out.println("Starting learning phase!");
		RegressionTree tree = new RegressionTree(trainingSet);
		tree.printRules();
		tree.printTree();

		char risp;
		do {
			System.out.println("Starting prediction phase!");
			try {
				Double prediction = tree.predictClass();
				System.out.println(prediction);
			} catch (UnknownValueException e) {
				System.out.println(e);
			}
			System.out.println("Would you repeat ? (y/n)");
			risp = Keyboard.readChar();
		} while (risp == 'y');
	}

}