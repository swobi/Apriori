package associationrule;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

public class JsonTool {
	
	private static HashMap<String,String> hm = new HashMap<String,String>();

	public static void main(String[] args) {
		putHashMap();
		
		JSONObject JO = null;
		try {
			JO = new JSONObject(new JSONTokener(new FileReader("json/input.json")));
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		JSONArray inputArr = JO.getJSONArray("input");
		
		Writer writer = null;
		try {
		    writer = new BufferedWriter(new OutputStreamWriter(
		          new FileOutputStream("json/tool_output.json"), "utf-8"));
		    
		    writer.write("{\n\t\"input\":[\n");
		    for (int i=0; i<inputArr.length()-1; i++) {
		    	writer.write("\t{\n\t\t\"dbcolumn\": \"");
		    	JSONObject obj = (JSONObject)inputArr.get(i);
		    	String dbcolumn = obj.getString("dbcolumn");
		    	writer.write(dbcolumn);
		    	writer.write("\",\n\t\t\"datatype\": \"");
		    	writer.write(hm.get(dbcolumn) + "\"\n\t},\n");
		    }
		    writer.write("\t{\n\t\t\"dbcolumn\": \"");
	    	JSONObject obj = (JSONObject)inputArr.get(inputArr.length()-1);
	    	String dbcolumn = obj.getString("dbcolumn");
	    	writer.write(dbcolumn);
	    	writer.write("\",\n\t\t\"datatype\": \"");
	    	writer.write(hm.get(dbcolumn) + "\"\n\t}\n\t]\n}");
		} catch (IOException ex) {
		  // report
		} finally {
		   try {
			   writer.close();
		   } catch (Exception ex) { }
		}
	}
	
	public static void putHashMap() {
		hm.put("G_ACTF_", "continuous");	//hm.put("G_ACTF_", "discrete");
		hm.put("G_ACTS_", "continuous");
		hm.put("G_ALAM_", "text");
		hm.put("G_AUMO_", "discrete");
		hm.put("G_CONS_", "discrete");
		hm.put("G_CUTT_", "discrete");
		hm.put("G_CYCT_", "discrete");
		hm.put("G_ELCT_", "discrete");	//hm.put("G_ELCT_", "continuous");
		hm.put("G_EXEP_", "text");		//hm.put("G_EXEP_", "discrete");
		hm.put("G_FERP_", "continuous");
		hm.put("G_GCOD_", "text");
		hm.put("G_MODA_", "text");
		hm.put("G_NONG_F", "discrete");
		hm.put("G_NONG_T", "discrete");
		hm.put("G_OPRT_", "discrete");
		hm.put("G_PARA_6750_6750_N", "text");
		hm.put("G_PARA_6751_6751_N", "text");
		hm.put("G_PARA_6752_6752_N", "text");
		hm.put("G_PARA_6753_6753_N", "text");
		hm.put("G_PARA_6754_6754_N", "text");
		hm.put("G_PARA_6712_6712_N", "text");
		hm.put("G_PICH_0_1023", "text");
		hm.put("G_PMCY_X_0_0", "discrete");		//hm.put("G_PMCY_X_0_0", "text");
		hm.put("G_PMCY_Y_0_0", "discrete");		//hm.put("G_PMCY_Y_0_0", "text");
		hm.put("G_POSA_", "text");
		hm.put("G_POSM_T", "text");
		hm.put("G_POSR_T", "text");
		hm.put("G_POSA_T", "text");
		hm.put("G_POSD_T", "text");
		hm.put("G_PRGM_", "text");
		hm.put("G_PRGR_", "text");
		hm.put("G_SEQN_", "text");
		hm.put("G_SPMC_", "continuous");
		hm.put("G_SPMS_", "continuous");
		hm.put("G_SRAC_", "text");
		hm.put("G_SRMC_", "text");
		hm.put("G_SRMS_", "text");
		hm.put("G_SPSD_", "continuous");
		hm.put("G_TOFS_1_32", "text");
		hm.put("G_ZOFS_0_6", "text");
	}
}
