package associationrule;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 	@author Nick Kao
 * 	@date 2013-11-15
 *	AlgoApriori handles the input request, generates and stores the filtered dataset
 *	in memory, and create a set of association rules.
 *	User can output the rules to database, or print the rules or statistics to console.
 */
public class AlgoApriori {

	private double min_sup;
	private double min_conf;
	private ArrayList<Object[]> filtered_dataset;
	private Object[] parsedInput;
	private String[] dbConfig;
	private mysqlAccess inputMysql;
	private mysqlAccess outputMysql;
	
	private String[] selectColumn;
	private int filtered_record_count;
	private long startTime, endTime;
	private List<Ruleset> rules;
	private String[] resultTableNames = {"config_Log","attribute","filter","rule","term"};
	
	/**
	 * Constructor of AlgoApriori instance. Initialize the corresponding elements,
	 * and set the minimum support and confidence.
	 * @param p_input	The parsed input that represent the mining configuration
	 * @param db	An array of String that represent the database configuration
	 * @param inputSqlAccess	The mysqlAccess to the input database
	 * @param outputSqlAccess	The mysqlAccess to the output database
	 */
	public AlgoApriori (Object[] p_input, String[] db, mysqlAccess inputSqlAccess, mysqlAccess outputSqlAccess ) {
		this.parsedInput = p_input;
		this.dbConfig = db;
		this.inputMysql = inputSqlAccess;
		this.outputMysql = outputSqlAccess;
		
		JSONObject action = (JSONObject)parsedInput[1];
		this.min_sup = action.getJSONObject("parameter").getDouble("Support_threshold");	
		this.min_conf = action.getJSONObject("parameter").getDouble("Confidence_threshold");
		this.filtered_dataset = new ArrayList<Object[]>();
	}
	
	/**
	 * Run the Apriori algorithm. Record the start time and end time of the execution.
	 * Handles the input request to generate the filtered dataset, generate the 
	 * frequent itemset from the filtered dataset. Finally, generate the association
	 * rules from the frequent itemset.
	 * @throws SQLException
	 */
	public void runAlgorithm() throws SQLException {
		System.out.println("\nRunning Apriori Algorithm...");
		startTime = getTime();
		
		queryHandler();
		List<Itemset> freq_itemset = get_FreqItemset();
		rules = ruleGeneration(freq_itemset);
		
		endTime = getTime();
	}
	
	/**
	 * @return	A list of frequent itemset(s) that consists of a set of Itemset that has
	 * 			support value that meets the threshold
	 * @throws SQLException
	 */
	public List<Itemset> get_FreqItemset() throws SQLException {
		List<Itemset> freq_itemset = new ArrayList<Itemset>();
		int column_count = selectColumn.length;
		
		//get level=1 itemset and calculate its support count
		for (Object[] values : filtered_dataset) {
			for (int i=0; i<column_count; i++) {
				boolean already_exist = false;
				for (Itemset itemset : freq_itemset) {
					if (itemset.getItem(i).equals(values[i])) {
						itemset.increaseSupportCount();
						already_exist = true;
					}
				}
				if (!already_exist) {
					Itemset new_itemset = new Itemset(column_count);
					new_itemset.setItem(values[i], i);
					new_itemset.increaseSupportCount();
					freq_itemset.add(new_itemset);
				}
			}
		}
		
		//filter
		for(int i=0; i<freq_itemset.size(); ) {
			double sup = (double)freq_itemset.get(i).getSupportCount() / filtered_record_count;
			if (sup < min_sup) {
				freq_itemset.remove(i);
			} else {
				i++;
			}
		}
		
		recursive_FreqItemset(freq_itemset, 1);
		return freq_itemset;
	}
	
	/**
	 * Generate frequent itemsets of level+1 and add it to the frequent itemset. 
	 * @param freq_itemset	The currently obtained frequent itemset
	 * @param level	The current level
	 * @throws SQLException
	 */
	private void recursive_FreqItemset(List<Itemset> freq_itemset, int level) throws SQLException {
		int column_count = selectColumn.length;
		if (level < column_count) {
			//generate new permutations of itemsets
			List<Itemset> new_level_perm = itemset_permutations(freq_itemset, level);
			
			//verify the support of new itemsets
			//if support is higher or equal to min_sup, add to freq_itemset
			for (Itemset itemset : new_level_perm) {
				for (Object[] values : filtered_dataset) {
					boolean eq = true;
					for(int j=0; j<column_count; j++) {
						if (!itemset.getItem(j).equals("nil") && 
							!values[j].equals(itemset.getItem(j))) {
							eq = false;
						} 
					}
					if (eq == true) {
						itemset.increaseSupportCount();
					}
				}
				if (((double)itemset.getSupportCount()/(double)filtered_record_count) >= min_sup) {
					freq_itemset.add(itemset);
				}
			}
			level++;
			
			//run recursively until level exceeds column_count
			recursive_FreqItemset(freq_itemset, level);
		}
	}
	
	/**
	 * Generates itemset permutations of level+1
	 * @param freq_itemset	The currently found frequent itemset
	 * @param level	The current highest level
	 * @return	Itemset permutations of level+1
	 */
	private List<Itemset> itemset_permutations(List<Itemset> freq_itemset, int level) {
		List<Itemset> result = new ArrayList<Itemset>();
		List<Itemset> level_1 = new ArrayList<Itemset>();
		List<Itemset> level_top = new ArrayList<Itemset>();
		
		for (Itemset i : freq_itemset) {
			if (i.getLevel() == 1) {
				level_1.add(i);
			}
			if (i.getLevel() == level) {
				level_top.add(i);
			}
		}
		
		for (Itemset i : level_top) {
			Object[] tmp = i.getArray();
			for (Itemset j : level_1) {
				if (tmp[j.getLevel1Column()].equals("nil")) {
					int c = j.getLevel1Column();
					Itemset new_itemset = new Itemset(tmp.length);
					new_itemset.setItemArr(tmp);
					new_itemset.setItem(j.getItem(c), c);
					if (result.isEmpty()) {
						result.add(new_itemset);
					} else {
						boolean already_exist = false;
						for (Itemset k : result) {
							if (k.equalsIgnoreSupportCount(new_itemset)) {
								already_exist = true;
							}
						}
						if (!already_exist) {
							result.add(new_itemset);
						}
					}
				}
				
			}
		}
		return result;
	}
	
	/**
	 * Generate a list of Ruleset from the frequent itemset
	 * @param freq_itemset	The frequent itemset
	 * @return	A list of association rules
	 * @throws SQLException
	 */
	private List<Ruleset> ruleGeneration(List<Itemset> freq_itemset) throws SQLException {
		List<Ruleset> rules = new ArrayList<Ruleset>();
		
		for (Itemset itemset : freq_itemset) {
			if (itemset.getLevel() > 1) {
				Object[] itemsetArr = itemset.getArray();
				int count = 0;
				for (int i=0; i<itemsetArr.length; i++) {
					if(itemsetArr[i] != "nil") {
						count++;
						if (count <= itemset.getLevel()-1) {
							Itemset i1 = new Itemset(itemsetArr.length);
							Itemset i2 = new Itemset(itemsetArr.length);
							for (int j=0; j<itemsetArr.length; j++) {
								if (j<=i) {
									i1.setItem(itemsetArr[j], j);
								} else if (j>i) {
									i2.setItem(itemsetArr[j], j);
								}
							}
							//create rules and add to Ruleset
							Ruleset rule1 = createRule(i1, i2, itemset.getSupportCount());
							if (rule1.getConfidence() >= min_conf)
								rules.add(rule1);
							
							Ruleset rule2 = createRule(i2, i1, itemset.getSupportCount());
							if (rule2.getConfidence() >= min_conf)
								rules.add(rule2);
						}
					}
				}
			}
		}
		return rules;
	}
	
	/**
	 * Create an association rule out of the given antecedent and consequent itemset.
	 * Scans the filtered_dataset to calculate the correct confidence.
	 * @param ante	The antecedent itemset
	 * @param cons	The consequent itemset
	 * @param sup_count	The number of rows where antecedent and consequent both exist
	 * @return 	A Ruleset that represent an association rule
	 * @throws SQLException
	 */
	private Ruleset createRule(Itemset ante, Itemset cons, int sup_count)
			throws SQLException {
		Object[] anteArr = ante.getArray();
		Object[] consArr = cons.getArray();
		ante.setSupportCount(0);
		cons.setSupportCount(0);
		
		for (Object[] values : filtered_dataset) {
			boolean match_ante = true;
			boolean match_cons = true;
			for (int i=0; i<anteArr.length; i++) {
				if (!values[i].equals(anteArr[i]) && anteArr[i] != "nil") {
					match_ante = false;
				}
				if (!values[i].equals(consArr[i]) && consArr[i] != "nil") {
					match_cons = false;
				}
			}
			if (match_ante) {
				ante.increaseSupportCount();
			}
			if (match_cons) {
				cons.increaseSupportCount();
			}
		}
		Ruleset rule = new Ruleset(ante, cons, sup_count, (double)sup_count/ante.getSupportCount());
		return rule;	
	}
	
	/**
	 * Handles the input request, transform the request into SQL command, 
	 * performs number filters, and perform discretization on continuous attributes.
	 * The resulted filtered dataset is saved in the ArrayList: filtered_dataset
	 * @throws SQLException
	 */
	private void queryHandler() throws SQLException {
		StringBuilder query = new StringBuilder();
		boolean existNumFilter = false;
		boolean existContinuous = false;
		List<MinMax> minmaxArr = null;
		JSONArray input = (JSONArray)parsedInput[0];
		selectColumn = new String[input.length()];
		String[] datatypeColumn = new String[input.length()];
		
		//SELECT handler
		for(int i=0; i<input.length(); i++) {
			JSONObject obj = input.getJSONObject(i);
			selectColumn[i] = obj.getString("dbcolumn");
			datatypeColumn[i] = obj.getString("datatype");
			//check if there are any continuous attribute
			//if true, create an ArrayList of MinMax to hold its min/max values
			if(obj.getString("datatype").equalsIgnoreCase("continuous")) {
				existContinuous = true;
				minmaxArr = new ArrayList<MinMax>();
				for (int j=0; j<input.length(); j++) {
					minmaxArr.add(new MinMax());
				}
			}
		}
		
		//check whether the input has at least two select columns,
		//and generate the SELECT clause for the query
		if (selectColumn.length > 1) {
			query.append("SELECT " + selectColumn[0]);
			for (int i=1; i<selectColumn.length; i++) {
				query.append(", " + selectColumn[i]);
			}
		} else {
			throw new IllegalStateException("It takes at least two selected columns to generate "
					+ "association rules. The input only contains: " + selectColumn.length + ".");
		}
		
		//FROM handler
		query.append(" FROM " + inputMysql.getImportTable());
		
		//WHERE handler
		JSONArray filter = new JSONArray();
		//if the input has 3 keySets, there are filters given
		if (parsedInput.length == 3) {
			 filter = (JSONArray)parsedInput[2];
			int textFilterCount = 0; 
			//for each filter, if the filter datatype is text, add it to the WHERE clause
			for (int i=0; i<filter.length(); i++) {
				JSONObject obj = filter.getJSONObject(i);
				if (obj.getString("datatype").equalsIgnoreCase("text")) {
					if (textFilterCount == 0) {
						textFilterCount++;
						query.append(" WHERE ");
					} else {
						query.append(" AND ");
					}
					query.append(obj.getString("dbcolumn"));
					query.append(obj.getString("type"));
					query.append("'" + obj.getString("criteria") + "'");
				} else {
					//if there exist filter(s) with a datatype other than text
					//then there exists a number filter.
					existNumFilter = true;
				}
			}
		}
		
		//perform query
		ResultSet rs = inputMysql.sql(query.toString());
		rs.last();
		int row_count = rs.getRow();
		int[] discard_row = new int[row_count];
		rs.beforeFirst();
		row_count=0;
		
		while(rs.next()) {
			//prepare for number filters, mark the rows that need to be discarded
			if (existNumFilter) {
				for (int i=0; i<filter.length(); i++) {
					JSONObject f = filter.getJSONObject(i);
					for (int j=0; j<selectColumn.length; j++) {
						if (selectColumn[j].equals(f.getString("dbcolumn")) && !f.getString("datatype").equals("text")) {
							//mark this row as to be discarded, if the value does not meet the criteria
							//if the value is "B", which is missing value, we keep it
							//since we cannot determine whether an unknown value meets the criteria or not
							if (rs.getString(j+1).equals("B")) {
								//discard_row[row_count] = 1;
							} else if (!compare(f.getString("type"), 
									Double.parseDouble(rs.getString(j+1)), 
									Double.parseDouble(f.getString("criteria")))) {
								discard_row[row_count] = 1;	
							}
						}
					}
				}
			}
			//prepare for discretization on continuous attributes
			//calculate the min / max values for each continuous attribute
			//only take into account the rows that are NOT to be discarded
			if(existContinuous && discard_row[row_count] != 1) {
				for (int i=0; i<datatypeColumn.length; i++) {
					if (datatypeColumn[i].equalsIgnoreCase("continuous")) {
						double tmp=0;
						if (!rs.getString(i+1).equals("B")) {
							tmp = Double.parseDouble(rs.getString(i+1));
							if (tmp > minmaxArr.get(i).getMax())
								minmaxArr.get(i).setMax(tmp);
							if (tmp < minmaxArr.get(i).getMin())
								minmaxArr.get(i).setMin(tmp);
						}
					}
				}
			}
			//if there's no continuous attribute, or any number filters
			//just add each row, with the selected attributes, to the ArrayList
			if (!existContinuous && !existNumFilter) {
				String[] values = new String[input.length()];
				for (int i=0; i<input.length(); i++) {
					values[i] = rs.getString(i+1);
				}
				filtered_dataset.add(values);
			}
			row_count++;
		}

		//if there are continuous attributes or number filters
		//then another pass of the resultset is needed to discard some rows
		//discretize some attributes, in order to create a filtered dataset
		if (existContinuous || existNumFilter) {
			rs.beforeFirst();
			row_count = 0;
			double min, max;
			while(rs.next()) {
				//if this row should be kept
				if (discard_row[row_count] == 0) {
					Object[] values = new Object[input.length()];
					//perform discretization on continuous attributes
					if (existContinuous) {
						for (int i=0; i<input.length(); i++) {
							if (datatypeColumn[i].equalsIgnoreCase("continuous") && !rs.getString(i+1).equals("B")) {
								min = minmaxArr.get(i).getMin();
								max = minmaxArr.get(i).getMax();
								StringBuilder discretized = new StringBuilder();
								if (min == max) {
									discretized.append(new DecimalFormat("#.###").format(min));
								} else {
									double tmp = Double.parseDouble(rs.getString(i+1));
									//here we discretize the continuous attribute into 10 parts
									for (double j=min; j<max; j+=(max-min)/10) {
										if (tmp >= j && tmp <= (j + ((max-min)/10))) {
											discretized.append(new DecimalFormat("#.###").format(j) + "-");
											discretized.append(new DecimalFormat("#.###").format(j + ((max-min)/10)));
										}
									}
								}
								values[i] = discretized.toString();
							} else {
								values[i] = rs.getString(i+1);
							}
						}
					} else if (existNumFilter){
						for (int i=0; i<input.length(); i++) {
							values[i] = rs.getString(i+1);
						}
					}
					filtered_dataset.add(values);
				}
				row_count++;
			}
		}
		filtered_record_count = filtered_dataset.size();
	}
	
	/**
	 * Create the output schema if not already existed. Output the execution
	 * results, including the config_log, attribute, filter, rules,
	 * to the output database.
	 * @throws SQLException
	 */
	public void outputResult() throws SQLException {
		System.out.println("Inserting the execution results to output database");
		outputMysql.createOutputSchema();
		
		int run_id;
		Object[] config_log = new Object[13];
		config_log[0] = getDateTime(startTime);
		config_log[1] = getDateTime(endTime);
		config_log[2] = dbConfig[0]+dbConfig[3];	//input_db_ip
		config_log[3] = dbConfig[1];	//input_db_account
		config_log[4] = dbConfig[2];	//input_db_password
		config_log[5] = dbConfig[4];	//input_db_tablename
		config_log[6] = dbConfig[5]+dbConfig[8];	//output_db_ip
		config_log[7] = dbConfig[6];	//output_db_account
		config_log[8] = dbConfig[7];	//output_db_password
		config_log[9] = min_sup;
		config_log[10] = min_conf;
		config_log[11] = inputMysql.getImportTableRecordCount(); //raw record count
		config_log[12] = filtered_record_count;
		
		//insert the execution information to the corresponding tables
		run_id = outputMysql.insertConfigLog(resultTableNames[0], config_log);
		outputMysql.insertAttribute(resultTableNames[1], run_id, (JSONArray)parsedInput[0]);
		if(parsedInput.length>2) { //if the parsedInput contains filter(s)
			outputMysql.insertFilter(resultTableNames[2], run_id, (JSONArray)parsedInput[2]);
		}
		
		//this is the newer version where all terms are inserted with a single sql command!
		ArrayList<Object[]> rulesForInsert = new ArrayList<Object[]>();
		int rule_id = 0;
		
		for (Ruleset rule : rules) {
			Object[] ruleArr = new Object[5];
			rule_id++;
			Itemset ante = rule.getAntecedent();
			Itemset cons = rule.getConsequent();

			ruleArr[0] = run_id;
			ruleArr[1] = rule_id;
			ruleArr[2] = ante;
			ruleArr[3] = cons;
			ruleArr[4] = rule.getConfidenceFormatted();
			rulesForInsert.add(ruleArr);
		}
		
		if (rules.size() != 0) {
			outputMysql.insertRules(resultTableNames[3], rulesForInsert,
					selectColumn, filtered_record_count);
		}
	}
	
	/**
	 * Print the execution parameters and statistics to console
	 */
	public void printStatistics() {
		System.out.println("============Apriori Result Statistics============");
		System.out.println("Minimum Support: " + min_sup);
		System.out.println("Minimum Confidence: " + min_conf);
		System.out.println("Size of filtered dataset: " + filtered_record_count + " rows");
		System.out.println("Number of rules generated: " + rules.size());
		System.out.println("Algorithm took: " + (double)(endTime-startTime)/1000 + " second(s)");
		System.out.println("================End of Statistics================");
		System.out.println();
	}
	
	/**
	 * Print the resulted association rule(s) to console.
	 */
	public void printRules() {
		System.out.println("==============Apriori Result Ruleset=============");
		for (int i=0; i<rules.size(); i++) {
			System.out.print((i+1) + ". ");
			rules.get(i).print(selectColumn);
		}
	}
	
	/**
	 * Print the rules that are recorded in the database to the console
	 * @throws SQLException
	 */
	public void printRulesFromDB() throws SQLException {
		outputMysql.printRulesFromDB(outputMysql.getRunId());
	}
	
	/**
	 * @return	The current system time in milliseconds
	 */
	private long getTime() {
		return System.currentTimeMillis();
	}
	
	/**
	 * @param timestamp	The timestamp to be formatted
	 * @return	A String in DateTime format
	 */
	private String getDateTime(long timestamp) {
		java.util.Date dt = new java.util.Date(timestamp);

		java.text.SimpleDateFormat sdf = 
		     new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		return sdf.format(dt);
	}
	
	/**
	 * Determines whether "i op j" is true
	 * @param op	the mathematics operation
	 * @param i 	the front part of the operation
	 * @param j		the rear part of the operation
	 * @return		true if "i op j" is true, false otherwise
	 */
	private boolean compare(String op, double i, double j) {
		if (op.equals("="))
			return (i == j);
		else if (op.equals("!="))
			return (i != j);
		else if (op.equals(">"))
			return (i > j);
		else if (op.equals("<"))
			return (i < j);
		else if (op.equals(">="))
			return (i >= j);
		else if (op.equals("<="))
			return (i<=j);
		else
			return false;
	}
	
	/**
	 * @author Nick Kao
	 * A public class to hold the min/max pair of an attribute
	 */
	public class MinMax {
		private double min;
		private double max;
		
		public MinMax() {
			this.min = Double.MAX_VALUE;
			this.max = -Double.MAX_VALUE;
		}
		public double getMin() {
			return min;
		}
		public double getMax() {
			return max;
		}
		public void setMin(double n) {
			min = n;
		}
		public void setMax(double n) {
			max = n;
		}
	}

}
