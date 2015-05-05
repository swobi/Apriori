package associationrule;

import java.text.DecimalFormat;

/**
 * 	@author Nick Kao
 * 	@date 2013-11-15
 *	An Itemset consist of an array of item(s) and the support count of this Itemset.
 *	The size of the item array is the maximum number of items possible, which is 
 *	determined by the number of the selected columns.
 *	An Itemset's level is the number of item(s) that it actually contains.  
 */
public class Itemset {
	
	private Object item_arr[];
	private int sup_count=0;
	
	/**
	 * Constructor of Itemset, assign the size of item_arr,
	 * and initialize all its elements to "nil"
	 * @param size	The size of the Itemset
	 */
	public Itemset(int size) {
		item_arr = new Object[size];
		for(int i=0; i<item_arr.length; i++) {
			item_arr[i] = "nil";
		}
	}
	
	/**
	 * @return	The itemset array
	 */
	public Object[] getArray() {
		return item_arr;
	}
	
	/**
	 * @param item	The new value of item
	 * @param index	The index of the item to be set
	 */
	public void setItem(Object item, int index) {
		item_arr[index] = item;
	}
	
	/**
	 * @param arr	The new array of items
	 */
	public void setItemArr(Object[] arr) {
		if (arr.length == item_arr.length) {
			for (int i=0; i<item_arr.length; i++) {
				item_arr[i] = arr[i];
			}
		} else {
			throw new IllegalArgumentException("The length of the new item array "
					+ "does not match the existing item array.");
		}
	}
	
	/**
	 * @param count	The support count to be set
	 */
	public void setSupportCount(int count) {
		sup_count = count;
	}
	
	/**
	 * @param index	The index of the item to be retrieved
	 * @return	The item at the specified index
	 */
	public Object getItem(int index) {
		return item_arr[index];
	}
	
	/**
	 * Increase the support count of the Itemset by 1
	 */
	public void increaseSupportCount() {
		sup_count++;
	}
	
	/**
	 * @return	The support count of the Itemset
	 */
	public int getSupportCount() {
		return sup_count;
	}
	
	/**
	 * @param total_rows	The total number of rows
	 * @return	The relative support percentage
	 */
	public double getRelativeSupport(int total_rows) {
		return (double)sup_count/(double)total_rows;
	}
	
	/**
	 * @param total_rows	The total number of rows
	 * @return	The relative support percentage in formatted String.
	 */
	public String getSupportRelativeFormatted(int total_rows) {
		double frequence = ((double)sup_count) / ((double) total_rows);
		DecimalFormat format = new DecimalFormat();
		format.setMinimumFractionDigits(0); 
		format.setMaximumFractionDigits(5); 
		return format.format(frequence);
	}
	
	/**
	 * @return	The level (the number of item(s) it contains) of the Itemset
	 */
	public int getLevel() {
		int count=0;
		for(int i=0; i<item_arr.length; i++) {
			if (item_arr[i] != "nil") {
				count++;
			}
		}
		return count;
	}
	
	/**
	 * @return	Index of the only item if Itemset's level is 1, -1 otherwise.
	 */
	public int getLevel1Column() {
		if (getLevel()==1) {
			for(int i=0; i<item_arr.length; i++) {
				if (item_arr[i] != "nil") {
					return i;
				}
			}
		}
		return -1;
	}
	
	/**
	 * Print content of the Itemset to console
	 */
	public void print() {
		for(int i=0; i<item_arr.length; i++) {
			System.out.print(item_arr[i] + "\t");
		}
		System.out.println(sup_count);
	}
	
	/**
	 * @param selectColumn	An array of String that contains the name of the selected columns
	 * @return	The content of the Itemset with its corresponding column names
	 */
	public String toString(String[] selectColumn) {
		StringBuilder sb = new StringBuilder();
		for(int i=0; i<item_arr.length; i++) {
			if (!item_arr[i].equals("nil")) {
				sb.append(selectColumn[i] + "=" + item_arr[i] + ",");
			}
		}
		String str = sb.toString();
		str = str.substring(0, str.length()-1);	//eliminate extra comma
		return str;
	}
	
	/**
	 * @param itemset	Another Itemset to compare with
	 * @return 	true if every item in the item_arr match the ones in the given itemset
	 * 			false otherwise 
	 */
	public boolean equalsIgnoreSupportCount(Itemset itemset) {
		Object[] arr = itemset.getArray();
		for (int j=0; j<item_arr.length; j++) {
			if (!item_arr[j].equals(arr[j])) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public boolean equals(Object o) {
		if (o == this) {
			return true;
		}
		if (!(o instanceof Itemset)) {
			return false;
		}
		Itemset itemset = (Itemset)o;
		if (itemset.getSupportCount() != sup_count) {
			return false;
		}
		Object[] arr = itemset.getArray();
		for (int j=0; j<item_arr.length; j++) {
			if (!item_arr[j].equals(arr[j])) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		int result = 17;
		result = result * 31 + item_arr.hashCode();
		result = result * 31 + (int) (sup_count ^ (sup_count >>> 32));
        return result;
    }
	
}
