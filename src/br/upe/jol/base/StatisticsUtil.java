package br.upe.jol.base;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Vector;


public class StatisticsUtil {
	
	private static StatisticsUtil instance;
	
	public static StatisticsUtil getInstance(){
		if(instance == null){
			instance = new StatisticsUtil();
		}
		
		return instance;
	}

	public StatisticsUtil() {
		super();
	}
	
	public double getAverage(double[] values){
		double sum = 0;
		for(double value : values){
			sum += value;
		}
		
		return (sum/(double) values.length);
	}
	
	
	public double getMedian(double[] values){
		Arrays.sort(values);
		if(values.length % 2 != 0){
			return values[(values.length-1)/2];
		}else {
			double value1 = values[values.length/2];
			double value2 = values[(values.length/2)-1];
			
			return (value1+value2)/2;
		}
	}
	
    /**
	   * Calculates the median of a vector considering the positions indicated by
	   * the parameters first and last
	   * @param vector
	   * @param first index of first position to consider in the vector
	   * @param last index of last position to consider in the vector
	   * @return The median
	   */
	  public static Double getMedian(Vector vector, int first, int last) {
	    double median = 0.0;

	    int size = last - first + 1;
	    // System.out.println("size: " + size + "first: " + first + " last:  " + last) ;

	    if (size % 2 != 0) {
	      median = (Double) vector.elementAt(first + size / 2);
	    } else {
	      median = ((Double) vector.elementAt(first + size / 2 - 1) +
	              (Double) vector.elementAt(first + size / 2)) / 2.0;
	    }

	    return median;
	  } // calculatemedian
	
	public double getMedian(List<Double> values){
		Collections.sort(values);
		if(values.size() % 2 != 0){
			return values.get((values.size()-1)/2);
		}else {
			double value1 = values.get(values.size()/2);
			double value2 = values.get((values.size()/2)-1);
			
			return (value1+value2)/2;
		}
	}
	
	public double getStandardDeviation(double[] values){
		double average = this.getAverage(values);
		double[] averageDeviation = new double[values.length];
		double ret = 0;
		
		for(int i=0; i<values.length; i++){
			averageDeviation[i] = (values[i]-average)*(values[i]-average);
		}
		
		for(int i=0; i<averageDeviation.length; i++){
			ret += averageDeviation[i];
		}
		
		return Math.sqrt(ret);
	}
	
	public double[] getQuartis(double[] values){
		Arrays.sort(values);
		double[] ret = new double[3];
		int index = Math.round((values.length)/4);
		
		ret[0] = values[index];
		ret[1] = this.getMedian(values);
		ret[2] = values[3*index];
		
		return ret;
	}
	
	 /**
	   * Calculates the interquartile range (IQR) of a vector of Doubles
	   * @param vector
	   * @return The IQR
	   */
	  public static Double calculateIQR(Vector vector) {
	    double q3 = 0.0;
	    double q1 = 0.0;

	    if (vector.size() > 1) { // == 1 implies IQR = 0
	      if (vector.size() % 2 != 0) {
	        q3 = getMedian(vector, vector.size() / 2 + 1, vector.size() - 1);
	        q1 = getMedian(vector, 0, vector.size() / 2 - 1);
	        //System.out.println("Q1: [" + 0 + ", " + (vector.size()/2 - 1) + "] = " + q1) ;
	        //System.out.println("Q3: [" + (vector.size()/2+1) + ", " + (vector.size()-1) + "]= " + q3) ;
	      } else {
	        q3 = getMedian(vector, vector.size() / 2, vector.size() - 1);
	        q1 = getMedian(vector, 0, vector.size() / 2 - 1);
	        //System.out.println("Q1: [" + 0 + ", " + (vector.size()/2 - 1) + "] = " + q1) ;
	        //System.out.println("Q3: [" + (vector.size()/2) + ", " + (vector.size()-1) + "]= " + q3) ;
	      } // else
	    } // if

	    return q3 - q1;
	  } // calculateIQR
}
