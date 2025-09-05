import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.DefaultXYDataset;


public class WNVis_SP2024 {
	//pro vizualizaci okna grafu
	private static JFrame oknoGrafu;
	//pro vizualizaci okna druheho grafu
	private static JFrame oknoGrafu2;
	//pro vizualizaci buttonu
	private static JFrame oknoRadioButtonu;
	//list pro zaznamenani obsahu rezervoaru
	private static ArrayList<ArrayList<Double>> naplneniRezervoaru = new ArrayList<>();
	//list pro zaznamenani casu pri kterem byl content nameren
	private static ArrayList<ArrayList<Double>> casyNamerenychHodnot = new ArrayList<>();
	//list pro zaznamenani rychlosti toku vody
	private static ArrayList<ArrayList<Double>> rychlostiToku = new ArrayList<>();
	//list pro zaznamenani casu pri kterem byla rychlost namerena
	private static ArrayList<ArrayList<Double>> casyNamerenychToku = new ArrayList<>();
	//atribut pro predani indexu kohoutu na ktery uzivatel klikl
	private static int indexKohoutu = -1;

	public static void main(String[] args) throws InterruptedException {
		JFrame okno = new JFrame();
		okno.setTitle("Semetralni Prace: Filip Valtr,  A22B0107P");
		okno.setSize(800, 600);
		
		int glyphSize = 30;
		if (args.length == 0) {
			Scanner sc = new Scanner(System.in);
			System.out.println("Zadejte gylphsize: ");
			glyphSize = sc.nextInt();
		}else {
			glyphSize = Integer.parseInt(args[0]);
		}
		
		WaterNetwork wn = new WaterNetwork(3);
		DrawingPanel dr = new DrawingPanel(glyphSize, wn);
		okno.add(dr, BorderLayout.CENTER); //prida komponentu
		//vytvorime a rozmistime talcitka 
		vytvorARozmistiTlacitka(okno, wn, dr);
		okno.pack(); //udela resize okna dle komponent
		okno.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		okno.setLocationRelativeTo(null); //vycentrovat na obrazovce
		okno.setVisible(true);
		
		Timer tm = new Timer();
		tm.schedule(new TimerTask() {
			@Override
			public void run() {
				wn.updateState();	//updateState of the system 
				rozpoznejUdalostMysi(dr, okno);
				okno.repaint();
			}
		}, 0, 100);//10x za sekundu je naplanovano volani run()	
	}
	/**
	 * Metoda vytvori a rozmisti talcitka v okne 
	 * @param okno - okno ve kterem se vizualizace vykresluje
	 * @param wn - instance trid WaterNetwork
	 * @param dr - instance tridy drawing panel
	 */
	public static void vytvorARozmistiTlacitka(JFrame okno, WaterNetwork wn, DrawingPanel dr) {
		//zalozime hlavni toolbar
	    JPanel toolBar = new JPanel();
	    toolBar.setLayout(new BorderLayout());
	    //umistime toolBar v okne 
	    okno.add(toolBar, BorderLayout.SOUTH);
	    //pouzijeme GridLayout s 2 radky
	    JPanel buttonPanel = new JPanel(new GridLayout(2, 0));
	    //pridame panel pro tlacitka do toolbaru
	    toolBar.add(buttonPanel, BorderLayout.CENTER);
		//zalozime tlacitka
		JButton btnZrychli = new JButton("zrychlit");
		JButton btnZpomal = new JButton("normální běh");
		JButton btnGlyphSize1 = new JButton("zvetsit Glyph size o 1px");
		JButton btnGlyphSize10 = new JButton("zvetsit Glyph size o 10px");
		JButton btnGlyphSizeMinus1 = new JButton("zmensit Glyph size o 1px");
		JButton btnGlyphSizeMinus10 = new JButton("zmensit Glyph size o 10px");
		//pridame tlacitka do panelu
	    buttonPanel.add(btnGlyphSizeMinus1);
	    buttonPanel.add(btnZrychli);
	    buttonPanel.add(btnGlyphSize1);
	    buttonPanel.add(btnGlyphSizeMinus10);
	    buttonPanel.add(btnZpomal);
	    buttonPanel.add(btnGlyphSize10);
		//zalozime radiobuttony
		JRadioButton otevrenyVentil = new JRadioButton("100%");
		JRadioButton z75ProcentOtevren = new JRadioButton("75%");
		JRadioButton poloOtevrenyVentil = new JRadioButton("50%");
		JRadioButton z25ProcentOtevren = new JRadioButton("25%");
		JRadioButton zavrenyVentil = new JRadioButton("0%");
		//seskupi radiobuttony
		ButtonGroup skupinaRadio = new ButtonGroup();
        skupinaRadio.add(otevrenyVentil);
        skupinaRadio.add(z75ProcentOtevren);
        skupinaRadio.add(poloOtevrenyVentil);
        skupinaRadio.add(z25ProcentOtevren);
        skupinaRadio.add(zavrenyVentil);
        /**
         * Listenery udalosti pro radiobuttony
         */
        otevrenyVentil.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent a) {
				//nastavime kohout
				dr.setValve(1f, indexKohoutu);
				dr.repaint();
			}
		});
        poloOtevrenyVentil.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//nastavime kohout
				dr.setValve(0.5f, indexKohoutu);
				dr.repaint();
			}
		});
        zavrenyVentil.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				//nastavime kohout
				dr.setValve(0f, indexKohoutu);
				dr.repaint();
			}
		});
        z25ProcentOtevren.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent a) {
				//nastavime kohout
				dr.setValve(0.25f, indexKohoutu);
				dr.repaint();
			}
		});
        z75ProcentOtevren.addActionListener(new ActionListener() {
			//nastavime kohout
			@Override
			public void actionPerformed(ActionEvent a) {
				dr.setValve(0.75f, indexKohoutu);
				dr.repaint();	
			}
		});
    	//Zalozime okno pro radio buttony a pridame je do okna
		oknoRadioButtonu = new JFrame();
        oknoRadioButtonu.setTitle("míra otevřenosti kohoutu");
        oknoRadioButtonu.setLayout(new FlowLayout());
        oknoRadioButtonu.add(otevrenyVentil);
        oknoRadioButtonu.add(z75ProcentOtevren);
        oknoRadioButtonu.add(poloOtevrenyVentil);
        oknoRadioButtonu.add(z25ProcentOtevren);
        oknoRadioButtonu.add(zavrenyVentil);
        oknoRadioButtonu.pack();
		/**
		 * Action listenery pro stisk na akci tlacitka
		 */
		btnZrychli.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				wn.runFast();
			}
		});
		btnZpomal.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				wn.runNormal();
			}
		});
		btnGlyphSizeMinus10.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dr.setGlyphSize(-10);
				okno.repaint();
			}
		});
		btnGlyphSizeMinus1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dr.setGlyphSize(-1);
				okno.repaint();
			}
		});
		btnGlyphSize10.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dr.setGlyphSize(10);
				okno.repaint();
			}
		});
		btnGlyphSize1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				dr.setGlyphSize(1);
				okno.repaint();
			}
		});
	}
	/**
	 * Metoda obsahuje metody pro rozpoznani interakce uzivatele se siti
	 * @param dr - instance Drawing panelu
	 * @param okno - okno ve kterem se sit vykresluje
	 */
	public static void rozpoznejUdalostMysi(DrawingPanel dr, JFrame okno) {
		dr.addMouseListener(new MouseListener() {
			@Override
			public void mouseReleased(MouseEvent e) { 
				
						
			}
			
			@Override
			public void mousePressed(MouseEvent e) {				
			
		
			}	
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				//okno.repaint();
				//pokud uzivatel klikl na rezervoar tak si ulozime jeho index, jinak -1
				int index = dr.klikUzivatelNaRezervoar(e.getX(),e.getY());
				if( index != -1 ) {//klilk na rezervoar
					//vytahneme si z listu list k prislusneme rezervoaru s namerenymi hodnotami
					ArrayList<Double> kubickeHodnotyNaplneni = naplneniRezervoaru.get(index);
					//vytahneme casy pri kterem byli hodnoty namereny
					ArrayList<Double> casy =  casyNamerenychHodnot.get(index);
					//zobrazime graf
					zobrazGrafRezervoaru(kubickeHodnotyNaplneni, casy);
				}
				//pokud uzivatel klikl na stred potrubi tak si ulozime jeho index, jinak -1
				index = dr.klikUzivatelNaStredPotrubi(e.getX(), e.getY());
				if( index != -1 ) {
					//vytahneme si z listu list k prislusnemu potrubi s namerenymi hodnotami
					ArrayList<Double> rychlostTokuVody = rychlostiToku.get(index);
					//vytahneme casy pri kterem byli hodnoty namereny
					ArrayList<Double> casy =  casyNamerenychToku.get(index);
					//zobrazime graf
					zobrazGrafTokuVodyVPotrubi(rychlostTokuVody, casy);
				}
				//pokud uzivatel klikl na kohout tak si ulozime jeho index, jinak -1
				index = dr.jeUzivatelNaKohoutu(e.getX(), e.getY());
				if(index != -1) {
					// Nastavení pozice okna relativne k pozici stisku mysi
					oknoRadioButtonu.setLocation(e.getXOnScreen(), e.getYOnScreen());
					//nastavime vyobrazeni okna
		            oknoRadioButtonu.setVisible(true);
		            //ulozime si index
		            indexKohoutu = index;
				}else {
					//pokud uzivatel kliknul mimo jaky koliv kohout zavreme okno
					oknoRadioButtonu.dispose();
					//inex kohoutu nastavime na -1
					indexKohoutu = -1;
				}
			}
		});
	}
	/**
	 * Metoda zobrazi graf prislusejici rezervoaru
	 * @param kubickeHodnotyNaplneni - list s postupne namerenym obsahem vody v rezervoaru
	 * @param casyNamerenychHodnot - list s casy kdy byli hodnoty obsahu vody v rezervoaru namereny
	 */
	public static void zobrazGrafRezervoaru(ArrayList<Double> kubickeHodnotyNaplneni, ArrayList<Double> casyNamerenychHodnot) {
		//pokud jeste nebylo zalozeno okno grafu tak ho zalozime
        if (oknoGrafu == null || !oknoGrafu.isVisible()) { 
            oknoGrafu = new JFrame();
            oknoGrafu.setTitle("Grafická reprezentace zaplnění rezervoáru v závislosti na čase");
            oknoGrafu.setSize(800, 600);
            //vytvorime panel pro graf
            ChartPanel panel = new ChartPanel(
                    vytvorSpojnicovyGraf(kubickeHodnotyNaplneni, casyNamerenychHodnot)
            );
            //pridame panel do okna
            oknoGrafu.add(panel);
            oknoGrafu.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            oknoGrafu.setLocationRelativeTo(null);
            oknoGrafu.setVisible(true);
        } else {
            // okno s grafem jiz existuje, aktualizujeme jen jeho obsah
            ChartPanel panel = new ChartPanel(
                    vytvorSpojnicovyGraf(kubickeHodnotyNaplneni, casyNamerenychHodnot)
            );
            oknoGrafu.getContentPane().removeAll();
            oknoGrafu.getContentPane().add(panel, BorderLayout.CENTER);
            oknoGrafu.pack();
            oknoGrafu.repaint();
        }
	}
	/**
	 * Metoda vytvori a vrati spojnicovy graf
	 * @param kubickeHodnotyNaplneni - list s postupne namerenym obsahem vody v rezervoaru
	 * @param casyNamerenychHodnot - list s casy kdy byli hodnoty obsahu vody v rezervoaru namereny
	 * @return - spojnicovy graf
	 */
	public static JFreeChart vytvorSpojnicovyGraf(ArrayList<Double> kubickeHodnotyNaplneni, ArrayList<Double> casyNamerenychHodnot) {
		//vytvorime dataset
		DefaultXYDataset dataset = new DefaultXYDataset();
		double[][] XYbody = new double[2][kubickeHodnotyNaplneni.size()];
		//pridame body do 2 rozmerneho pole
		for (int i = 0; i < kubickeHodnotyNaplneni.size(); i++) {
			XYbody[0][i] = casyNamerenychHodnot.get(i);
			XYbody[1][i] = kubickeHodnotyNaplneni.get(i);
		}
		//pridame serii bodu do datasetu
		dataset.addSeries("Objem rezervoaru / čas", XYbody);
		//vyber spojnicoveho grafu s nastavenim popisku
	    JFreeChart chart = ChartFactory.createXYLineChart("Zachycení zaplnění zvoleného rezervoaru v zavislosti na čase simulace",
	                                                                      "čas naměření hodnoty (v sekundách)",
	                                                                      "objem rezervoaru v kubických jednotkách",
	                                                                       dataset);
	    // ziskani plotu grafu
	    XYPlot plot = (XYPlot) chart.getPlot();
	    //nastaveni barvy grafu
	    Color barva = Color.CYAN;
	    plot.getRenderer().setSeriesPaint(0, barva);
	    //vratime graf
	    return chart;
	}
	/**
	 * Metoda zobrazi graf prislusejicimu potrubi
	 * @param rychlostTokuVody - list s postupne namerenymi rychlostmi toku vody v konkretnim potrubi
	 * @param casy - list s casy kdy byli hodnoty rychlosti vody v potrubi namereny
	 */
	public static void  zobrazGrafTokuVodyVPotrubi(ArrayList<Double> rychlostTokuVody, ArrayList<Double> casy) {
		//pokud jeste nebylo zalozeno okno grafu tak ho zalozime 
		if (oknoGrafu2 == null || !oknoGrafu2.isVisible()) { 
	            oknoGrafu2 = new JFrame();
	            oknoGrafu2.setTitle("Grafická reprezentace rychlosti proudění vody v závislosti na čase");
	            oknoGrafu2.setSize(800, 600);
	            //vytvorime panel pro graf
	            ChartPanel panel = new ChartPanel(
	                    vytvorSpojnicovyGrafPotrubi(rychlostTokuVody, casy)
	            );

	            oknoGrafu2.add(panel);
	            oknoGrafu2.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	            oknoGrafu2.setLocationRelativeTo(null);
	            oknoGrafu2.setVisible(true);
	     } else {
	            // Okno s grafem jiz existuje, aktualizujeme jen jeho obsah
	            ChartPanel panel = new ChartPanel(
	                    vytvorSpojnicovyGrafPotrubi(rychlostTokuVody, casy)
	            );
	            oknoGrafu2.getContentPane().removeAll();
	            oknoGrafu2.getContentPane().add(panel, BorderLayout.CENTER);
	            oknoGrafu2.pack();
	            oknoGrafu2.repaint();
	     }
	}
	/**
	 * 
	 * Metoda vytvori a vrati spojnicovy graf
	 * @param rychlostTokuVody - list s postupne namerenymi rychlostmi vody v potrubi
	 * @param casy - list s casy kdy byli hodnoty obsahu vody v potrubi namereny
	 * @return spojnicovy graf
	 */
	public static JFreeChart  vytvorSpojnicovyGrafPotrubi(ArrayList<Double> rychlostTokuVody, ArrayList<Double> casy) {
		//vytvorime dataset
		DefaultXYDataset dataset = new DefaultXYDataset();
		double[][] XYbody = new double[2][rychlostTokuVody.size()];
		//pridame body do 2 rozmerneho pole
		for (int i = 0; i < rychlostTokuVody.size(); i++) {
			XYbody[0][i] = casy.get(i);
			//u rychlosti je jeste znamenko ktere urcuje smer toku vody. 
			//Samotny smer nas ale nezajima pro musime zaporne hodnoty umocnit.
			if(rychlostTokuVody.get(i) < 0) {
				XYbody[1][i] = Math.abs(rychlostTokuVody.get(i));
			}else {
				XYbody[1][i] = rychlostTokuVody.get(i);
			}
			
		}
		//pridame serii bodu do datasetu
		dataset.addSeries("rycholost toku / čas", XYbody);
		//vyber spojnicoveho grafu s nastavenim popisku
	    JFreeChart chart = ChartFactory.createXYLineChart("Rychlost toku vody v potrubí v zavislosti na čase simulace",
	                                                                      "čas naměření hodnoty (v sekundách)",
	                                                                      "rychlost průtoku vody daným potrubím (v m/s(",
	                                                                       dataset);
	    // ziskani plotu grafu
	    XYPlot plot = (XYPlot) chart.getPlot();
	    //nastaveni barvy grafu
	    Color barva = Color.RED;
	    plot.getRenderer().setSeriesPaint(0, barva);
	    return chart;
	}
	/**
	 * Metoda nastavi do listu pod prislusnym indexem rezervoaru kubickou hodnotu 
	 * @param kubickaJednotka - double - kubicka jednotka obsahu vody v rezervoaru
	 * @param index - index rezervoaru v listu
	 */
	public static void setNaplneniRezervoaru(double kubickaJednotka, int index) {
		if(naplneniRezervoaru.size() == index) {
			naplneniRezervoaru.add(index, new ArrayList<>());
			naplneniRezervoaru.get(index).add(kubickaJednotka);
		}else {
			naplneniRezervoaru.get(index).add(kubickaJednotka);
		}
	}
	/**
	 * Metoda nastavi do listu pod prislusnym indexem rezervoaru casy namerenych hodnot
	 * @param cas - double - casova jednotka
	 * @param index - index rezervoaru v listu
	 */
	public static void setCasNaplneniRezervoaru(double cas, int index) {
		if(casyNamerenychHodnot.size() == index) {
			casyNamerenychHodnot.add(index, new ArrayList<>());
			casyNamerenychHodnot.get(index).add(cas);
		}else {
			casyNamerenychHodnot.get(index).add(cas);
		}
		
	}
	/**
	 * Metoda nastavi do listu pod prislusnym indexem potrubi rychlost toku vody 
	 * @param rychlost - dobule - hodnota urcujici rychlost toku vody
	 * @param index - index potrubi v listu
	 */
	public static void setRychlostiToku(double rychlost, int index) {
		if(rychlostiToku.size() == index) {
			rychlostiToku.add(index, new ArrayList<>());
			rychlostiToku.get(index).add(rychlost);
		}else {
			rychlostiToku.get(index).add(rychlost);
		}
	}
	/**
	 *  Metoda nastavi do listu pod prislusnym indexem potrubi casy namerenych hodnot
	 * @param cas - double - casova jednotka
	 * @param index - index potrubi v listu
	 */
	public static void setCasyNamerenychToku(double cas, int index) {
		if(casyNamerenychToku.size() == index) {
			casyNamerenychToku.add(index, new ArrayList<>());
			casyNamerenychToku.get(index).add(cas);
		}else {
			casyNamerenychToku.get(index).add(cas);
		}
		
	}
}
