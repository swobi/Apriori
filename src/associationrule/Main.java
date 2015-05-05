package associationrule;

import java.sql.SQLException;

import org.json.JSONObject;

/*
 * 此專案用途：成大(己方)與政大合作工具機資料探勘
 * 專案說明：
 * 	Main會先New一個jsonParser的物件，裡面有兩個Method
 * 		1.parseDatabase		,Main用catchInformation存取結果並初始化資料庫連線
 * 		2.parseInformation 	,Main用catchDBarr存取結果
 * 		目前上述兩個方式會去讀JSON檔 (目前採取絕對路徑與固定名稱、可自行調整)
 * 		並將結果存在上述兩個變數中
 * 	接著會對catchInformation做處理,目前欄位有四個(type不用處理),
 * 	目前input&filter已完成
 * 	output尚未完成
 * 	可將input與filter(where)組成mysql語法並讀取資料
 * 程式撰寫人：ISMPmember_林彥廷
 * 最後更新日期：2013/9/18
 * 
 * */

/**
 * 	@author Nick Kao
 * 	@date 2013-11-15
 *	The Main class allows user to assign the mining and database configuration files,
 *	and use the engine to perform association rule mining.
 *
 *	A jsonParser instance is created to parse the input request, and two mysqlAccess
 *	instances are created to allow input and output mysqlAccess.
 *
 *	Depending on the input kernel_type, the program passes the parsed input and 
 *	mysqlAccess to run the Non-SQL or SQL-based Apriori algorithm.  Association rule(s)
 *	will be generated, and results are inserted to the output database and printed to 
 *	the console.
 */
public class Main {

	public static void main(String[] args) throws SQLException   {
		jsonParser Parser = new jsonParser();
		String[] dbConfig = Parser.parseDatabase("json/utilisation/databaseConf.json");
		mysqlAccess inputSqlAccess = new mysqlAccess(dbConfig[0],dbConfig[1],dbConfig[2],dbConfig[3],dbConfig[4]);
		mysqlAccess outputSqlAccess = new mysqlAccess(dbConfig[5],dbConfig[6],dbConfig[7],dbConfig[8],"");
		
		//outputSqlAccess.printRulesFromDB(id);
		
		Object[] parsedInput = Parser.parseInput("json/utilisation/mining_config_5.json");
		
		JSONObject action = (JSONObject)parsedInput[1];
		if (action.getString("kernel_type").equalsIgnoreCase("MEM")) {
			AlgoApriori apriori = new AlgoApriori(parsedInput, dbConfig, inputSqlAccess, outputSqlAccess);
			apriori.runAlgorithm();
			apriori.outputResult();
			apriori.printStatistics();
			apriori.printRules();
			apriori.printRulesFromDB();
		} else if (action.getString("kernel_type").equalsIgnoreCase("SQL")) {
			AlgoSqlApriori sqlApriori = new AlgoSqlApriori(parsedInput, dbConfig, inputSqlAccess, outputSqlAccess);
			sqlApriori.runAlgorithm();
			sqlApriori.outputResult();
			sqlApriori.printStatistics();
			sqlApriori.printRules();
			sqlApriori.printRulesFromDB();
		}
		
	}
	
}
