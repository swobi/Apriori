package associationrule;

import java.io.FileNotFoundException;
import java.io.FileReader;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;


/**
 * 	@author Nick Kao
 * 	@date 2013-11-15
 *	A jsonParser contains methods to parse the mining and database configurations'
 *	JSON files, and also a method to perform validity check to verify if these input
 *	are legal.
 */
public class jsonParser {
	
	/**
	 * @param filename	Name of the mining configuration file
	 * @return	An array of Object that contains the configuration information
	 * @throws JSONException
	 */
	public Object[] parseInput(String filename) throws JSONException{
		JSONObject JO = null;
		try {
			JO = new JSONObject(new JSONTokener(new FileReader(filename)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("JSON input: "+JO);
		
		if (JO.length() > 3) {
			throw new IllegalStateException("JSON - error: the input should contain at most three keySets, "
					+ "while it has: " + JO.length());
		} else if (JO.length() < 2) {
			throw new IllegalStateException("JSON - error: the input should contain at least two keySets, "
					+ "while it has: " + JO.length());
		}

		Object[] dataset = new Object[JO.length()];
		//input  handler
		dataset[0] = JO.getJSONArray("input");			
		//action handler
		dataset[1] = JO.getJSONObject("action");
		//filter handler
		if (JO.length() == 3) {
			dataset[2] = JO.getJSONArray("filter");
		}
		
		//perform input validity check
		validityCheck(dataset);
		return dataset;
	}
	
	/**
	 * @param filename	Name of the database configuration file
	 * @return	An array of String that contains the configuration information
	 * @throws JSONException
	 */
	public String[] parseDatabase(String filename) throws JSONException {
		JSONObject JO = null;
		try {
	
			JO = new JSONObject(new JSONTokener(new FileReader(filename)));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		JSONObject input = JO.getJSONObject("input");
		JSONObject output = JO.getJSONObject("output");
		
		String[] DBconfArr = new String[input.length()+output.length()];
		DBconfArr[0] = input.getString("host");
		DBconfArr[1] = input.getString("user");
		DBconfArr[2] = input.getString("password");
		DBconfArr[3] = input.getString("database");
		DBconfArr[4] = input.getString("import_table");
		DBconfArr[5] = output.getString("host");
		DBconfArr[6] = output.getString("user");
		DBconfArr[7] = output.getString("password");
		DBconfArr[8] = output.getString("database");
		return DBconfArr;
	}
	
	/**
	 * Performs validity check of the inputed JSON configuration files.
	 * Exceptions would be thrown if the user inputed erroneous information.
	 * @param dataset
	 */
	public void validityCheck(Object[] dataset) {
		JSONArray input = (JSONArray)dataset[0];
		String[] selectColumn = new String[input.length()];
		String[] datatypeColumn = new String[input.length()];
		
		//input validity check
		for (int i=0; i<input.length(); i++) {
			selectColumn[i] = input.getJSONObject(i).getString("dbcolumn");
			datatypeColumn[i] = input.getJSONObject(i).getString("datatype");
			if ((datatypeColumn[i].equalsIgnoreCase("text") || 
				datatypeColumn[i].equalsIgnoreCase("discrete") ||
				datatypeColumn[i].equalsIgnoreCase("continuous")) == false) {
				throw new IllegalArgumentException("JSON - input error: dbcolumn=" + selectColumn[i] + 
						",datatype=" + datatypeColumn[i] + 
						" >>> datatype must belong to the following categories: [text,discrete,continuous]");
			}
		}
		
		//action validity check
		JSONObject action = (JSONObject)dataset[1];
		//if the program is expanded to allow more action type, this part needs to be changed
		if (!action.getString("type").equalsIgnoreCase("association")) {
			throw new IllegalArgumentException("JSON - action error: type=" + action.getString("type") + 
					" >>> currently supported action type: association");
		}
		//if the program is expanded to allow more kernel_type, this part needs to be changed
		String kernel_type = action.getString("kernel_type");
		if (!(kernel_type.equalsIgnoreCase("MEM") || 
			kernel_type.equalsIgnoreCase("SQL"))) {
			throw new IllegalArgumentException("JSON - action error: kernel_type=" + kernel_type + 
					" >>> currently supported kernel_type: MEM or SQL");
		}
		JSONObject parameter = action.getJSONObject("parameter");
		double min_sup = parameter.getDouble("Support_threshold");
		if (min_sup < 0 || min_sup > 1) {
			throw new IllegalArgumentException("JSON - action error: Support_threshold=" + min_sup +
					" >>> The Support_threshold must range between 0 and 1.");
		}
		double min_conf = parameter.getDouble("Confidence_threshold");
		if (min_conf < 0 || min_conf > 1) {
			throw new IllegalArgumentException("JSON - action error: Confidence_threshold=" + min_sup +
					" >>> The Confidence_threshold must range between 0 and 1.");
		}
		
		//filter validity check
		if (dataset.length > 3) { // if there are filters
			JSONArray filter = (JSONArray)dataset[2];
			for (int i=0; i<filter.length(); i++) {
				JSONObject f = filter.getJSONObject(i);
				boolean validColumn = false;
				for (int j=0; j<input.length(); j++) {
					if (f.getString("dbcolumn").equals(selectColumn[j])) {
						validColumn = true;
						if (!f.getString("datatype").equalsIgnoreCase(datatypeColumn[j])) {
							throw new IllegalStateException("JSON - filter error: dbcolumn=" + f.getString("dbcolumn") + 
									",datatype=" + f.getString("datatype") + " >>> datatype of filter does not correspond with input.");
						}
					}
				}
				if (!validColumn) {
					throw new IllegalStateException("JSON - filter error: dbcolumn=" + f.getString("dbcolumn") +
							" does not correspond with any of the input dbcolumn");
				}
				String type = f.getString("type");
				if ((type.equals("=") || type.equals("!=") || type.equals(">") ||
					type.equals(">=") || type.equals("<=") || type.equals("<")) == false) {
					throw new IllegalArgumentException("JSON - filter error: dbcolumn=" + f.getString("dbcolumn") + 
							",type=" + type + " >>> type of filter must belong to the following categories: [=,!=,<,>,<=,>=]");
				}
			}
		}
		
	}
	
}
