package util;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.stream.IntStream;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;

import genetic.SimpleGeneticAlgorithm;
import localResearch.LocalResearchAlgorithm;

public class Rapport {

	private SimpleGeneticAlgorithm sga;
	private LocalResearchAlgorithm lra;
	 
	private String name;
	private boolean display;
	private int opt;	
	private int sol;
	private double ecartSolOpt;	// Retourne l'écart (en pourcentages) de la solution avec l'optimal
	
	public Rapport(SimpleGeneticAlgorithm sga, String name, boolean display) {
		this.sga 	= sga;
		sol			= sga.getBestValue();
		opt 		= sga.getOpt();
		ecartSolOpt = calculEcart();
		this.name	= name;
		this.display = display;
	}
	
	public Rapport(LocalResearchAlgorithm lra, String name, boolean display) {
		this.lra	= lra;
		sol			= lra.getBestSolution().getKey();
		opt			= lra.getOpt();
		ecartSolOpt = calculEcart();
		this.name	= name;
		this.display = display;
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String getSGARapport() {
		int[] y = sga.getOptimalValueLog().stream().mapToInt(Integer::intValue).toArray();
		int[] x = IntStream.range(0, y.length).toArray();
	 
		// Affichage de l'évolution de la fitness au cours des évolutions
	    XYChart chart = QuickChart.getChart("Evolution de la fitness selon les générations - Temps = " + sga.getExecutionDuration() + " ms", "Generation", "Fitness", "y(x)", HandyTools.copyFromIntArray(x), HandyTools.copyFromIntArray(y));
	    if (this.display) new SwingWrapper(chart).displayChart();	
	    try {
			BitmapEncoder.saveBitmap(chart, "./Charts/"+name, BitmapFormat.JPG);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	    DecimalFormat numberFormat = new DecimalFormat("#.00");

	    // Affichages des différentes informations
	    if (this.display) {
			System.out.println(" ===== Rapport =====");
			System.out.println(" Instance       : " + sga.getInstance());
			System.out.println(" Exec. Duration : " + sga.getExecutionDuration() + " ms ");
			System.out.println(" Pop. Duration  : " + sga.getPopGenDuration() + " ms ");
			System.out.println(" Valeur Sol     : " + sol);
			System.out.println(" Valeur Opt     : " + opt);
			System.out.println(" Valeur Ecart   : " + numberFormat.format(ecartSolOpt) + "%");
	    }

		return sol + " & " + numberFormat.format(ecartSolOpt) + "\\% & " + sga.getExecutionDuration() + " ms ";
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public String getLRARapport() {
	    
	    DecimalFormat numberFormat = new DecimalFormat("#.00");

	    // Affichages des différentes informations
	    if (this.display) {
			System.out.println(" ===== Rapport =====");
			System.out.println(" Instance       : " + lra.getInstance());
			System.out.println(" Exec. Duration : " + lra.getExecutionDuration() + " ms ");
			System.out.println(" Research count : " + lra.getResearchCount());
			System.out.println(" Valeur Sol     : " + sol);
			System.out.println(" Valeur Opt     : " + opt);
			System.out.println(" Valeur Ecart   : " + numberFormat.format(ecartSolOpt) + "%");
	    }

		int[] y = lra.getOptimalValueLog().stream().mapToInt(Integer::intValue).toArray();
		int[] x = IntStream.range(0, y.length).toArray();
	 
		// Affichage de l'évolution de la fitness au cours des évolutions
	    XYChart chart = QuickChart.getChart("Evolution des valeurs selon les différentes recherches - Temps = " + lra.getExecutionDuration() + " ms", "Generation", "Fitness", "y(x)", HandyTools.copyFromIntArray(x), HandyTools.copyFromIntArray(y));
	    if (this.display) new SwingWrapper(chart).displayChart();	
	    try {
			BitmapEncoder.saveBitmap(chart, "./Charts/"+name, BitmapFormat.JPG);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return sol + " & " + numberFormat.format(ecartSolOpt) + "\\% & " + lra.getExecutionDuration() + " ms ";
	}
	
	private double calculEcart() {
		return (Math.abs(sol-opt)*1.0)/opt*100;
	}
	
}
