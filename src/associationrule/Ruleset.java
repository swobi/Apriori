package associationrule;

import java.text.DecimalFormat;

/**
 * 	@author Nick Kao
 * 	@date 2013-11-15
 *	A Ruleset has two Itemset, namely antecedent and consequent that represent
 *	the left-hand-side and right-hand-side of a rule, and also the support count 
 *	and	confidence of the rule.
 */
public class Ruleset {

	private Itemset antecedent;
	private Itemset consequent;
	private int sup_count;
	private double conf;
	
	/**
	 * Constructor of Ruleset. Initialize the corresponding elements.
	 * @param ante
	 * @param cons
	 * @param s
	 * @param c
	 */
	public Ruleset (Itemset ante, Itemset cons, int s, double c) {
		this.antecedent = ante;
		this.consequent = cons;
		this.sup_count = s;
		this.conf = c;
	}
	
	/**
	 * @return	The support count of the rule
	 */
	public int getSupportCount() {
		return sup_count;
	}
	
	/**
	 * @param total_rows	Total number of rows
	 * @return	The relative support percentage
	 */
	public double getRelativeSupport(int total_rows) {
		return (double)sup_count/total_rows;
	}
	
	/**
	 * @return	The confidence of the rule
	 */
	public double getConfidence() {
		return conf;
	}
	
	/**
	 * @return	Formatted confidence, with minimum=0 and maximum=5
	 * 			fraction digits
	 */
	public String getConfidenceFormatted() {
		DecimalFormat format = new DecimalFormat();
		format.setMinimumFractionDigits(0); 
		format.setMaximumFractionDigits(5); 
		return format.format(conf);
	}
	
	/**
	 * @return	The antecedent Itemset of the Ruleset
	 */
	public Itemset getAntecedent() {
		return antecedent;
	}
	
	/**
	 * @return	The consequent Itemset of the Ruleset
	 */
	public Itemset getConsequent() {
		return consequent;
	}
	
	/**
	 * Print the Ruleset to console.
	 * @param column_names	An array of String that contains the names 
	 * 						of the selected columns
	 */
	public void print(String[] column_names) {
		Object[] anteArr = antecedent.getArray();
		Object[] consArr = consequent.getArray();
		//print antecedent
		System.out.print("( ");
		for (int i=0; i<anteArr.length; i++) {
			if (anteArr[i] != "nil") {
				System.out.print(column_names[i] + "=" + anteArr[i] + " ");
			}
		}
		System.out.print(", " + antecedent.getSupportCount() + ") ===> ( ");
		//print consequent
		for (int i=0; i<consArr.length; i++) {
			if (consArr[i] != "nil") {
				System.out.print(column_names[i] + "=" + consArr[i] + " ");
			}
		}
		//System.out.print(", " + consequent.getSupportCount() + ") ");
		//System.out.println("sup:" + sup_count + " conf:" + getConfidenceFormatted());
		
		//print statistics
		System.out.println(", " + sup_count + ") conf:" + getConfidenceFormatted());
		
	}
	
}
