package com.partners.weather.battery;

import com.partners.weather.common.Battery;

public class BatteryUtil {
	private static final double[] LithiumArray=new double[]{6.00,6.90,7.36,7.48,7.58,7.64,7.74,7.84,7.96,8.12,8.40};
	private static final double[] LeadcellArray=new double[]{11.10,11.60,11.80,12.00,12.20,12.40,12.50,12.80};
	private static final double[] LeadcellPercent=new double[]{0,10,20,40,60,70,80,99};
	public static double getBatteryPercent(Battery battery,double voltage) {
		double batterypercent=0;
		int index;
		switch (battery) {
		case leadcell:		
			index=getRangeIndex(LeadcellArray,voltage);
			if(voltage<LeadcellArray[index]){
				if(index>0){
					index--;
				}
			}
			batterypercent=LeadcellPercent[index];
			break;
		case lithium:	
			index=getRangeIndex(LithiumArray,voltage);
			if(voltage<LithiumArray[index]){
				if(index>0){
					index--;
				}
			}
			batterypercent=(LithiumArray[index]*10);
			break;
		default:
			batterypercent=voltage<12?0:100;
			break;
		}
		return batterypercent;
	}
	private static int getRangeIndex(double[] batteryArray,double voltage) {
		int low=0,middle=0;
		int hight=batteryArray.length-1;
		while (low<=hight) {
			middle=(low+hight)/2;
			if(voltage==batteryArray[middle]){
				return middle;
			}
			if(voltage<batteryArray[middle]){
				hight=middle-1;
			}else{
				low=middle+1;
			}
		}
		return middle;
	}
}
