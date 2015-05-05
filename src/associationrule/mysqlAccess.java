package associationrule;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 	@author Nick Kao
 * 	@date 2013-11-15
 *	A mysqlAccess allows access to a database connection. User can use a 
 *	mysqlAccess instance to perform query, insert rows, create/drop tables, etc.
 */
public class mysqlAccess {
	private Connection conDB = null;
	private String Import_table=null;
	private int run_id = -1;
	
	/**
	 * Constructor of mysqlAccess, takes the input information and create
	 * a database connection.
	 * @param Host	The host ip of the mysqlAccess
	 * @param User	The username of the mysqlAccess
	 * @param Password	The user password of the mysqlAccess
	 * @param Database	The database name of the mysqlAccess
	 * @param import_table	The import table in which the raw data is saved
	 * @throws SQLException
	 */
	public mysqlAccess(String Host, String User,String Password,String Database,String import_table) throws SQLException{
		Import_table=import_table;
		
		String url = Host + Database;

		try {
			//create a database connection from the given information
			Class.forName("com.mysql.jdbc.Driver");
			conDB = DriverManager.getConnection(url, User, Password);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @param query	The complete SQL command of the query
	 * @return	The ResultSet obtained by the query
	 * @throws SQLException
	 */
	public ResultSet sql(String query) throws SQLException{
		System.out.println(query);
		Statement stmt = conDB.createStatement();
			
		try {
			stmt.executeQuery(query);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ResultSet rs = stmt.getResultSet();
		return rs;
	}
	
	/**
	 * Given a table name and the input columns, create a table that has the
	 * same columns in TEXT datatype.
	 * @param tableName	The name of the table to be created
	 * @param input	A JSONArray that consists the input columns
	 * @throws SQLException
	 */
	public void createTable(String tableName, JSONArray input) throws SQLException{
		Statement stmt = conDB.createStatement();
		
		StringBuilder sql = new StringBuilder();
		sql.append("CREATE TABLE IF NOT EXISTS " + tableName + "(");
		for (int i=0; i<input.length()-1; i++) {
			JSONObject obj = input.getJSONObject(i);
			sql.append(obj.getString("dbcolumn") + " TEXT" + ",");
		}
		JSONObject obj = input.getJSONObject(input.length()-1);
		sql.append(obj.getString("dbcolumn") + " TEXT);");
		String str = sql.toString();
		System.out.println(str);
		
		try {
			stmt.executeUpdate(str);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @param tableName	Name of the table to be inserted into
	 * @param values	An array of String that contains the value to be inserted
	 * @throws SQLException
	 */
	public void insertInto(String tableName, String[] values) throws SQLException{
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO " + tableName + " VALUES (");
		for (int i=0; i<values.length-1; i++) {
			sql.append("?,");
		}
		sql.append("?)");
		
		PreparedStatement preparedStatement = conDB.prepareStatement(sql.toString());
		for (int i=0; i<values.length; i++) {
			preparedStatement.setString(i+1, values[i]);
		}
		
		try {
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Insert a batch of values to the given table in a single SQL command.
	 * @param tableName	Name of the table to be inserted into
	 * @param valuesArr	An ArrayList that contains arrays of Object, where each array
	 * 					contains values to be inserted 
	 * @throws SQLException
	 */
	public void insertValues(String tableName, ArrayList<Object[]> valuesArr) throws SQLException{
		StringBuilder query = new StringBuilder();
		query.append("INSERT INTO " + tableName + " VALUES ");
		
		for (Object[] values : valuesArr) {
			query.append("(");
			for (int i=0; i<values.length-1; i++) {
				query.append("'" + values[i] + "',");
			}
			query.append("'" + values[values.length-1] + "'),");
		}
		String sql = query.toString();
		sql = sql.substring(0, sql.length()-1);
		PreparedStatement preparedStatement = conDB.prepareStatement(sql);
		
		try {
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @param tableName		Name of the table to be inserted into
	 * @param config_log	An array that contains the values to be inserted
	 * @return	The run_id of the config_log
	 * @throws SQLException
	 */
	public int insertConfigLog(String tableName, Object[] config_log) throws SQLException {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO " + tableName);
		sql.append(" (start_time,end_time,input_db_ip,input_db_account,input_db_password,");
		sql.append("input_db_tablename,output_db_ip,output_db_account,output_db_password,");
		sql.append("min_support,min_confidence,raw_record_count,filtered_record_count)");
		sql.append(" VALUES ('"+(String)config_log[0]+"','"+(String)config_log[1]+"','"+
				(String)config_log[2]+"','"+(String)config_log[3]+"','"+
				(String)config_log[4]+"','"+(String)config_log[5]+"','"+
				(String)config_log[6]+"','"+(String)config_log[7]+"','"+
				(String)config_log[8]+"',"+(Double)config_log[9]+","+
				(Double)config_log[10]+","+(Integer)config_log[11]+","+(Integer)config_log[12]+")");
		PreparedStatement preparedStatement = 
				conDB.prepareStatement(sql.toString(), Statement.RETURN_GENERATED_KEYS);
		System.out.println(sql.toString());
		
		try {
			preparedStatement.executeUpdate();
			ResultSet rs = preparedStatement.getGeneratedKeys();
			rs.next();
			run_id = rs.getInt(1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return run_id;
	}
	
	/**
	 * @param tableName	Name of the table to be inserted into
	 * @param run_id	The referenced run_id
	 * @param input	A JSONArray that contains the attribute information to be inserted
	 * @throws SQLException
	 */
	public void insertAttribute(String tableName, int run_id, JSONArray input) throws SQLException {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO " + tableName + " VALUES ");
		for(int i=0; i<input.length()-1; i++) {
			JSONObject obj = (JSONObject)input.get(i);
			sql.append("("+run_id+","+(i+1)+",'"+obj.getString("dbcolumn")+"','");
			sql.append(obj.getString("datatype")+"'),");
		}
		JSONObject obj = (JSONObject)input.get(input.length()-1);
		sql.append("("+run_id+","+input.length()+",'"+obj.getString("dbcolumn")+"','");
		sql.append(obj.getString("datatype")+"')");
		
		PreparedStatement preparedStatement = conDB.prepareStatement(sql.toString());
		System.out.println(sql.toString());
		try {
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @param tableName	Name of the table to be inserted into
	 * @param run_id	The referenced run_id
	 * @param filter	A JSONArray that contains the filter information to be inserted
	 * @throws SQLException
	 */
	public void insertFilter(String tableName, int run_id, JSONArray filter) throws SQLException {
		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO " + tableName + " VALUES ");
		for(int i=0; i<filter.length()-1; i++) {
			JSONObject obj = (JSONObject)filter.get(i);
			sql.append("("+run_id+","+(i+1)+",'"+obj.getString("dbcolumn")+"','");
			sql.append(obj.getString("type")+"','"+obj.getString("criteria")+"'),");
		}
		JSONObject obj = (JSONObject)filter.get(filter.length()-1);
		sql.append("("+run_id+","+filter.length()+",'"+obj.getString("dbcolumn")+"','");
		sql.append(obj.getString("type")+"','"+obj.getString("criteria")+"')");
		
		PreparedStatement preparedStatement = conDB.prepareStatement(sql.toString());
		System.out.println(sql.toString());
		try {
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	/**
//	 * @param tableName	Name of the table to be inserted into
//	 * @param rulesForInsert	An ArrayList of arrays, where each array contains
//	 * 							values to be inserted
//	 * @param selectColumn	An array of String that contains the name of the input columns
//	 * @param filtered_record_count	The size of the filtered record
//	 * @throws SQLException
//	 */
//	public void insertRules(String tableName, ArrayList<Object[]> rulesForInsert, 
//			String[] selectColumn, int filtered_record_count) throws SQLException{
//		StringBuilder query = new StringBuilder();
//		query.append("INSERT INTO " + tableName + " VALUES ");
//		
//		for (Object[] ruleArr : rulesForInsert) {
//			Itemset ante = (Itemset)ruleArr[2];
//			Itemset cons = (Itemset)ruleArr[3];
//			query.append("("+ruleArr[0]+","+ruleArr[1]+",'");	//run_id, rule_id
//			query.append(ante.toString(selectColumn)+"',");
//			query.append(ante.getSupportRelativeFormatted(filtered_record_count)+",'");
//			query.append(cons.toString(selectColumn)+"',");
//			query.append(cons.getSupportRelativeFormatted(filtered_record_count)+",");
//			query.append(ruleArr[4] + "),");	//rule_confidence
//		}
//		String sql = query.toString();
//		sql = sql.substring(0, sql.length()-1);	//eliminate extra comma
//		System.out.println(sql);
//		PreparedStatement preparedStatement = conDB.prepareStatement(sql);
//		
//		try {
//			preparedStatement.executeUpdate();
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	/**
	 * @param tableName	Name of the table to be inserted into
	 * @param rulesForInsert	An ArrayList of arrays, where each array contains
	 * 							values to be inserted
	 * @param selectColumn	An array of String that contains the name of the input columns
	 * @param filtered_record_count	The size of the filtered record
	 * @throws SQLException
	 */
	public void insertRules(String tableName, ArrayList<Object[]> rulesForInsert, 
			String[] selectColumn, int filtered_record_count) throws SQLException{
		StringBuilder query = new StringBuilder();
		query.append("INSERT INTO " + tableName + " VALUES ");
		
		for (Object[] ruleArr : rulesForInsert) {
			Itemset ante = (Itemset)ruleArr[2];
			Itemset cons = (Itemset)ruleArr[3];
			int maxLevel = ante.getLevel();
			if (cons.getLevel() > maxLevel) {
				maxLevel = cons.getLevel();
			}
			
			int lastAnteIndex=0, lastConsIndex=0;
			for (int i=1; i<=maxLevel; i++) { //i represents the seq_no
				query.append("("+ruleArr[0]+","+ruleArr[1]+","+i+",'");	//run_id, rule_id, seq_no
				if (i <= ante.getLevel()) {
					while (ante.getItem(lastAnteIndex).toString().equals("nil")) {
						lastAnteIndex++;
					}
					query.append(selectColumn[lastAnteIndex]+"','"+
							ante.getItem(lastAnteIndex).toString()+"',");
				} else {
					query.append("null','null',");
				}
				query.append(ante.getRelativeSupport(filtered_record_count)+",");
				
				if (i <= cons.getLevel()) {
					while (cons.getItem(lastConsIndex).toString().equals("nil")) {
						lastConsIndex++;
					}
					query.append(selectColumn[lastConsIndex]+"','"+
							ante.getItem(lastConsIndex).toString()+"',");
				} else {
					query.append("null','null',");
				}
				query.append(cons.getRelativeSupport(filtered_record_count)+",");
				query.append(ruleArr[4]+"),");
			}
		}
		String sql = query.toString();
		sql = sql.substring(0, sql.length()-1);	//eliminate extra comma
		System.out.println(sql);
		PreparedStatement preparedStatement = conDB.prepareStatement(sql);
		
		try {
			preparedStatement.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Drop the given table
	 * @param tableName	Name of the table to be dropped
	 * @throws SQLException
	 */
	public void dropTable(String tableName) throws SQLException {
		Statement stmt = conDB.createStatement();
		String sql = "DROP TABLE " + tableName;
		System.out.println(sql);
		
		try {
			stmt.executeUpdate(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	/**
//	 * Initialization that creates multiple tables to form the output schema.
//	 * The tables would be created only if they don't already exist.
//	 * @throws SQLException
//	 */
//	public void createOutputSchema() throws SQLException {
//		Statement stmt = conDB.createStatement();
//		
//		StringBuilder config_log = new StringBuilder();
//		config_log.append("CREATE TABLE IF NOT EXISTS config_log (");
//		config_log.append("run_id INT NOT NULL AUTO_INCREMENT,"); 
//		config_log.append("start_time DATETIME NOT NULL,end_time DATETIME NOT NULL,");
//		config_log.append("input_db_ip TEXT,input_db_account TEXT,input_db_password TEXT,");
//		config_log.append("input_db_tablename TEXT,output_db_ip TEXT,output_db_account TEXT,");
//		config_log.append("output_db_password TEXT,min_support DOUBLE NOT NULL,");
//		config_log.append("min_confidence DOUBLE NOT NULL,raw_record_count INT NOT NULL,");
//		config_log.append("filtered_record_count INT NOT NULL,PRIMARY KEY (run_id)");
//		config_log.append(") ENGINE=INNODB;");
//		
//		StringBuilder attr = new StringBuilder();
//		attr.append("CREATE TABLE IF NOT EXISTS attribute (run_id INT NOT NULL,"); 
//		attr.append("attr_id INT NOT NULL,column_name TEXT NOT NULL,");
//		attr.append("column_type TEXT NOT NULL,PRIMARY KEY (run_id,attr_id),");
//		attr.append("FOREIGN KEY (run_id) REFERENCES config_log(run_id) ");
//        attr.append("ON DELETE CASCADE) ENGINE=INNODB;");
//		
//        StringBuilder filter = new StringBuilder();
//        filter.append("CREATE TABLE IF NOT EXISTS filter (run_id INT NOT NULL,"); 
//        filter.append("filter_id INT NOT NULL,column_name TEXT NOT NULL,");
//        filter.append("comparison TEXT NOT NULL,threshold TEXT NOT NULL,");
//        filter.append("PRIMARY KEY (run_id,filter_id),FOREIGN KEY (run_id) "); 
//        filter.append("REFERENCES config_log(run_id) ON DELETE CASCADE) ENGINE=INNODB;");
//        
//        StringBuilder rule = new StringBuilder();
//        rule.append("CREATE TABLE IF NOT EXISTS rule (run_id INT NOT NULL,");
//        rule.append("rule_id INT NOT NULL,lhs TEXT NOT NULL,");
//        rule.append("lhs_support_ratio DOUBLE NOT NULL,rhs TEXT NOT NULL,");
//        rule.append("rhs_support_ratio DOUBLE NOT NULL,rule_confidence DOUBLE NOT NULL,");
//        rule.append("PRIMARY KEY (run_id,rule_id),FOREIGN KEY (run_id) ");
//        rule.append("REFERENCES config_log(run_id) ON DELETE CASCADE) ENGINE=INNODB;");
//        
//		try {
//			stmt.executeUpdate(config_log.toString());
//			stmt.executeUpdate(attr.toString());
//			stmt.executeUpdate(filter.toString());
//			stmt.executeUpdate(rule.toString());
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	/**
	 * Initialization that creates multiple tables to form the output schema.
	 * The tables would be created only if they don't already exist.
	 * @throws SQLException
	 */
	public void createOutputSchema() throws SQLException {
		Statement stmt = conDB.createStatement();
		
		StringBuilder config_log = new StringBuilder();
		config_log.append("CREATE TABLE IF NOT EXISTS config_log (");
		config_log.append("run_id INT NOT NULL AUTO_INCREMENT,"); 
		config_log.append("start_time DATETIME NOT NULL,end_time DATETIME NOT NULL,");
		config_log.append("input_db_ip TEXT,input_db_account TEXT,input_db_password TEXT,");
		config_log.append("input_db_tablename TEXT,output_db_ip TEXT,output_db_account TEXT,");
		config_log.append("output_db_password TEXT,min_support DOUBLE NOT NULL,");
		config_log.append("min_confidence DOUBLE NOT NULL,raw_record_count INT NOT NULL,");
		config_log.append("filtered_record_count INT NOT NULL,PRIMARY KEY (run_id)");
		config_log.append(") ENGINE=INNODB;");
		
		StringBuilder attr = new StringBuilder();
		attr.append("CREATE TABLE IF NOT EXISTS attribute (run_id INT NOT NULL,"); 
		attr.append("attr_id INT NOT NULL,column_name TEXT NOT NULL,");
		attr.append("column_type TEXT NOT NULL,PRIMARY KEY (run_id,attr_id),");
		attr.append("FOREIGN KEY (run_id) REFERENCES config_log(run_id) ");
        attr.append("ON DELETE CASCADE) ENGINE=INNODB;");
		
        StringBuilder filter = new StringBuilder();
        filter.append("CREATE TABLE IF NOT EXISTS filter (run_id INT NOT NULL,"); 
        filter.append("filter_id INT NOT NULL,column_name TEXT NOT NULL,");
        filter.append("comparison TEXT NOT NULL,threshold TEXT NOT NULL,");
        filter.append("PRIMARY KEY (run_id,filter_id),FOREIGN KEY (run_id) "); 
        filter.append("REFERENCES config_log(run_id) ON DELETE CASCADE) ENGINE=INNODB;");
        
        StringBuilder rule = new StringBuilder();
        rule.append("CREATE TABLE IF NOT EXISTS rule (run_id INT NOT NULL,");
        rule.append("rule_id INT NOT NULL, seq_no INT NOT NULL, lhs TEXT NOT NULL,");
        rule.append("lhs_val TEXT NOT NULL, lhs_support_ratio DOUBLE NOT NULL,rhs TEXT NOT NULL,");
        rule.append("rhs_val TEXT NOT NULL, rhs_support_ratio DOUBLE NOT NULL,");
        rule.append("rule_confidence DOUBLE NOT NULL,");
        rule.append("PRIMARY KEY (run_id,rule_id,seq_no),FOREIGN KEY (run_id) ");
        rule.append("REFERENCES config_log(run_id) ON DELETE CASCADE) ENGINE=INNODB;");
        
		try {
			stmt.executeUpdate(config_log.toString());
			stmt.executeUpdate(attr.toString());
			stmt.executeUpdate(filter.toString());
			stmt.executeUpdate(rule.toString());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
//	/**
//	 * Given a run_id, print the rules related to this run_id to the console
//	 * @param id	The run_id related to the rules to be printed
//	 * @throws SQLException
//	 */
//	public void printRulesFromDB(int id) throws SQLException {
//		Statement stmt = conDB.createStatement();
//		StringBuilder sql = new StringBuilder();
//		
//		sql.append("SELECT * FROM rule WHERE run_id=" + id);
//		try {
//			stmt.executeQuery(sql.toString());
//			ResultSet rs = stmt.getResultSet();
//			System.out.print("\nPrinting rules for run_id:" + run_id);
//			System.out.println(" from database...");
//			while (rs.next()) {
////				System.out.print(rs.getInt(2)+". ( "+rs.getString(3)+") ===> ( ");
////				System.out.println(rs.getString(5)+") conf: "+rs.getDouble(7));
//				System.out.print(rs.getInt(2)+". ( "+rs.getString(3)+" , ");
//				System.out.print(rs.getString(4) + ") ===> ( "+rs.getString(5));
//				System.out.println(" , " + rs.getString(6) + ") conf: "+rs.getDouble(7));
//			}
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
	
	/**
	 * Given a run_id, print the rules related to this run_id to the console
	 * @param id	The run_id related to the rules to be printed
	 * @throws SQLException
	 */
	public void printRulesFromDB(int id) throws SQLException {
		Statement stmt = conDB.createStatement();
		StringBuilder sql = new StringBuilder();
		
		sql.append("SELECT * FROM rule WHERE run_id=" + id);
		try {
			stmt.executeQuery(sql.toString());
			ResultSet rs = stmt.getResultSet();
			System.out.print("\nPrinting rules for run_id:" + run_id);
			System.out.println(" from database...");
			
			StringBuilder lhs_str = new StringBuilder();
			StringBuilder rhs_str = new StringBuilder();
			int rule_id=0;
			double lhs_sup=0, rhs_sup=0, rule_conf=0;
			while (rs.next()) {
				if (rs.getInt(2) != rule_id) {
					System.out.print(rule_id+". ( "+lhs_str.substring(0, lhs_str.length()-1)+" , ");
					System.out.print(lhs_sup + ") ===> ( "+rhs_str.substring(0, rhs_str.length()-1));
					System.out.println(" , " + rhs_sup + ") conf: "+rule_conf);
					rule_id = rs.getInt(2);
					lhs_sup = rs.getDouble(6);
					rhs_sup = rs.getDouble(9);
					rule_conf = rs.getDouble(10);
					lhs_str.delete(0, lhs_str.length());
					rhs_str.delete(0, rhs_str.length());
				} else {
					if (!rs.getString(4).equals("null")) {
						lhs_str.append(rs.getString(4)+"="+rs.getString(5)+",");
					}
					if (!rs.getString(7).equals("null")) {
						rhs_str.append(rs.getString(7)+"="+rs.getString(8)+",");
					}
				}
			}
			System.out.print(rule_id+". ( "+lhs_str.substring(0, lhs_str.length()-1)+" , ");
			System.out.print(lhs_sup + ") ===> ( "+rhs_str.substring(0, rhs_str.length()-1));
			System.out.println(" , " + rhs_sup + ") conf: "+rule_conf);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * @return	The name of the import table
	 */
	public String getImportTable() {
		return Import_table;
	}
	
	/**
	 * @return	The run_id of this execution, if insertConfigLog hasn't been
	 * 			called this will return -1
	 */
	public int getRunId() {
		return run_id;
	}
	
	/**
	 * @return	The size of the import table
	 * @throws SQLException
	 */
	public int getImportTableRecordCount() throws SQLException{
		Statement stmt = conDB.createStatement();
		String sql = "SELECT COUNT(*) FROM " + Import_table;
			
		try {
			stmt.executeQuery(sql);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ResultSet rs = stmt.getResultSet();
		rs.next();
		return rs.getInt(1);
	}

}

