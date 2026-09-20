import data.Data;
import tree.RegressionTree;
import java.io.FileNotFoundException;

class MainTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws FileNotFoundException{
		Data trainingSet= new Data("prova.dat");
		
		RegressionTree tree=new RegressionTree(trainingSet);
		
		tree.printRules();
		
		tree.printTree();
		
	}

}
