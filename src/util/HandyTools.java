package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class HandyTools {

	public static String nicify(boolean[] a) {
		return Arrays.toString(a).replace("true", "1").replace("false","0");
	}

	/**
	 * @param min inclusif
	 * @param max exclusif 
	 * @return Un entier aléatoire entre les bornes [min,max)
	 */
	public static int randInt(int min, int max) {
		return ThreadLocalRandom.current().nextInt(min, max);
	}

	/**
	 * @param min inclusif
	 * @param max exclusif 
	 * @return Un double aléatoire entre les bornes [min,max)
	 */
	public static double randDouble(double min, double max) {
		return min + ThreadLocalRandom.current().nextDouble() * (max - min);
	}
	
	/**
	 * @return True si le tirage est inférieur ou égal à la probabilité donnée en paramètre
	 */
	public static boolean randProb(double prob) {
		return (randDouble(0, 1) <= prob) ? true : false;
	}
	
	/**
	 * Utilisé pour servir de clé pour mettre en cache les valeurs de chemins calculés selon deux vertex
	 */
	public static String concatIdsToString(int a, int b) {
		return a+"_"+b ; 
	}

	/**
	 * Utilisé pour retrouver la clé selon deux vertex
	 */
	public static int[] extractIdsFromString(String s) {
		int[] pair = new int[2];
		String[] split = s.split("_");
		pair[0] = Integer.parseInt(split[0]);
		pair[1] = Integer.parseInt(split[1]);
		return pair;
	}
	
	public static double[] copyFromIntArray(int[] source) {
	    double[] dest = new double[source.length];
	    for(int i=0; i<source.length; i++) {
	        dest[i] = source[i];
	    }
		return dest;
	}

	public static int getInstanceOptimal(String filename) {
		String instance = filename.split("/")[1];
		String number = filename.split("/|\\.")[2].substring(1);

		int opt = 0;

		try {
			File 			file 	 = new File("instances/optimal");
			BufferedReader 	br		 = new BufferedReader(new FileReader(file));
			String 			readLine = "";
			
			while((readLine = br.readLine()) != null) {
				if(readLine.startsWith(instance+number)) { 
					opt	= Integer.parseInt(readLine.split(" ")[1]);
				}
			}
			br.close();
		}catch(IOException e) { e.printStackTrace(); }
		return opt;
	}
	
	 
	public static String getInstance(String letter, int number) {
		return "instances/" + letter.toUpperCase() + "/" + letter.toLowerCase() + ((number < 10 ) ? "0" + Integer.toString(number) : Integer.toString(number)) + ".stp";
	}
}

