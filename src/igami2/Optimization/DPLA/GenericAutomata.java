package igami2.Optimization.DPLA;


public interface GenericAutomata {
	
	boolean hasConverged(double threshold);
	int selectAction();
	void doLearning(int response1, boolean print);
	void printP();
	int convergedTo();

}
