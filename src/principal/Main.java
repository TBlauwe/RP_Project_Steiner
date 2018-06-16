package principal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

import genetic.SimpleGeneticAlgorithm;
import genetic.SimpleGeneticAlgorithm.PopulationGeneration;
import genetic.SimpleGeneticAlgorithm.ReplacementStrategy;
import localResearch.LocalResearchAlgorithm;
import util.HandyTools;
import util.Rapport;

public class Main {

	public static void main(String[] args) {
		ClassLoader.getSystemClassLoader().setDefaultAssertionStatus(false);	// Enable assertions, making the program more robust, but making him run a litte bit slower
		
		SimpleGeneticAlgorithm sga = new SimpleGeneticAlgorithm(HandyTools.getInstance("c", 1),ReplacementStrategy.ELITIST, PopulationGeneration.IMPROVED, true);
		sga.compute();
		new Rapport(sga, "rien", true).getSGARapport();

//		LocalResearchAlgorithm lra = new LocalResearchAlgorithm(HandyTools.getInstance("b", 5), false, true);
//		lra.compute();
//		new Rapport(lra, "rien", true).getLRARapport();

//		computeAllInstance("d");
//		computeAllInstance("e");

//		concatenateFile();
	    
	}
	
	public static void computeAllInstance(String instanceLetter) {
		// Ecriture d'un fichier rapport pour récupérer les valeurs des différentes éxecutions
	    FileWriter fileWriter;
		try {

			for(int i=1; i<=20; i++) {
				String name = instanceLetter + ((i < 10 ) ? "0" + Integer.toString(i) : Integer.toString(i));

				fileWriter = new FileWriter("Rapports/Rapport_" + name);
				PrintWriter printWriter = new PrintWriter(fileWriter, true);
				String instance 		= HandyTools.getInstance(instanceLetter, i);
				String tabLine 			= "\\multicolumn{1}{|l|}{"+instanceLetter+Integer.toString(i)+"} & \\multicolumn{1}{l|}{"+HandyTools.getInstanceOptimal(instance)+"} &";

				// SGA with random population generation
				SimpleGeneticAlgorithm sga = new SimpleGeneticAlgorithm(instance, ReplacementStrategy.ELITIST, PopulationGeneration.RANDOM, false);
				sga.compute();
				tabLine += new Rapport(sga, name + " - SGA with random pop gen", false).getSGARapport();
				System.out.println("	1 finished");

				tabLine += " & ";
				// SGA with improved population generation
				sga = new SimpleGeneticAlgorithm(instance, ReplacementStrategy.ELITIST, PopulationGeneration.IMPROVED, false);
				sga.compute();
				tabLine += new Rapport(sga, name +  " - SGA with improved pop gen", false).getSGARapport();
				System.out.println("	2 finished");
					
				tabLine += " & ";
				// LRA with neighbours limitation (ignore some)
				LocalResearchAlgorithm lra = new LocalResearchAlgorithm(instance, true, false);
				lra.compute();
				tabLine += new Rapport(lra, name + " - LRA with ignore", false).getLRARapport();
				System.out.println("	3 finished");

				tabLine += " & ";
				// LRA without neighbours limitation (ignore none)
				lra = new LocalResearchAlgorithm(instance, false, false);
				lra.compute();
				tabLine += new Rapport(lra, name + " - LRA without ignore", false).getLRARapport();
				System.out.println("	4 finished");
					
				tabLine += "\\\\ \\hline";

				printWriter.println(tabLine);
				printWriter.close();

				System.out.println("instance " + i + " finished");
			}
			System.out.println("done");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void concatenateFile() {
		File folder 			= new File("./Rapports");
		File[] listOfFiles 		= folder.listFiles();
		Arrays.sort(listOfFiles);

		FileWriter fileWriter;
		try {
			fileWriter 				= new FileWriter("Rapport.txt");
			PrintWriter printWriter = new PrintWriter(fileWriter);

			String 	readLine = "";
			for (int i = 0; i < listOfFiles.length; i++) {
				if (listOfFiles[i].isFile()) {
					BufferedReader br;
					br = new BufferedReader(new FileReader(listOfFiles[i]));
					while((readLine = br.readLine()) != null) {
						printWriter.println(readLine);
					}
					br.close();
				 }
			}
			printWriter.close();
		} catch (IOException e1) { e1.printStackTrace(); }
		System.out.println("Done");
	}
}

