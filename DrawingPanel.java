import java.awt.Graphics;


import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.awt.Color;
import java.awt.Dimension;

import javax.swing.JPanel;


public class DrawingPanel extends JPanel {
	
	//pole slouzici pro praci s instancemi figurek dale v programu
	private IFigurka[] poleFigurek = new IFigurka[32];
	//identifikator jestli je figurka vybrana k presunu
	private boolean vybranaF = false;
	//x-ova souradnice aktualniho presunu
	private double aktualniBX;
	//y-ova souradnice aktualniho presunu
	private double aktualniBY;
	//promena slouzici pro ulozeni indexu figurky s kterou se aktualne pracuje
	private int indexF = -1;
	//reference s, ktera slouzi pro prisutp k sachovnici dale v programu
	private Sachovnice s;
	//slouzi pro ulozeni predchozi pole
	private Rectangle2D predchoziPole;
	//slouzi pro ulozeni aktualniho pole
	private Rectangle2D aktualniPole;
	//Identifikator toho ze se maji vyznacit mozne tahy figurek
	private boolean vybarvujTahy = false;
	//Identifikator poctu tahu
	private int pocetTahu = 0;
	
	public static boolean mat = false;
	public static boolean novaHra = false;
	
	public DrawingPanel() {
		this.setPreferredSize(new Dimension(800, 600));
		
		vystavSachovnici(800, 600, 600, 800);
		vytvorFigurky();
		
		this.addMouseListener(new MouseListener() {
			
			@Override
			public void mouseReleased(MouseEvent e) { 
				
				if(vybranaF == true) {
					aktualniBX = e.getX();
					aktualniBY = e.getY();
	
					Rectangle2D cilovePole = s.zjistPole(aktualniBX, aktualniBY);
					
					nastavPoziciF(cilovePole);	
					
				}
				DrawingPanel.super.repaint();		
			}
			
			@Override
			public void mousePressed(MouseEvent e) {				
				jeOznacenaF(e.getX(),e.getY());
		
			}	
			@Override
			public void mouseExited(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		this.addMouseMotionListener(new MouseMotionListener() {
			
			@Override
			public void mouseMoved(MouseEvent e) {
				// TODO Auto-generated method stub			
			}
			
			@Override
			public void mouseDragged(MouseEvent e) {
				if(vybranaF == true) {
					//Test sachu
					if(pocetTahu > 0) {
						for (int i = 0; i < poleFigurek.length; i++) {
							if (poleFigurek[i].getVyhozena() == false) {
								poleFigurek[i].ZkontrolujZakrytySach(poleFigurek, i, s);
							}
						}
					}
					
					poleFigurek[indexF].setPresouvaSe(true);	
					poleFigurek[indexF].setVybarvujMozneTahy(true);

					aktualniBX = e.getX();
					aktualniBY = e.getY();
					//nastaveni pozice presunu figurky
					poleFigurek[indexF].setPoziceX(aktualniBX);
					poleFigurek[indexF].setPoziceY(aktualniBY);
					
				DrawingPanel.super.repaint();
				}
			}
		});	
	}
	
	@Override
	public void paint(Graphics g) {
		//long start = System.nanoTime();
		super.paint(g);
		if(novaHra == true) {
			System.out.println("nova hra");
		}
		Graphics2D g2 = (Graphics2D)g;
	
		if(this.getWidth()!= s.getSirka() || this.getHeight() != s.getVyska()) {
			double sirka = this.getWidth();
			double vyska = this.getHeight();
			
			prenastavSachovnici(sirka, vyska);
			
			nastavHraciPoleFigurek();
			
			prenastavPozicePresunu();		
		}
		
		vykresliSachovnici(s.getSachovnice(), Color.WHITE, Color.BLACK, g2);
		
		//zvyrazneni tahu
		if(indexF != -1 && poleFigurek[indexF].getPredchoziPole() != null  ) {
		if(pocetTahu == 1 && vybarvujTahy == true) {
			vybarvujTahy = false;
			predchoziPole = poleFigurek[indexF].getPredchoziPole();
			aktualniPole = poleFigurek[indexF].getPole();
			
		}else if(pocetTahu > 1 && vybarvujTahy == true) {
			vybarvujTahy = false;
			if (s.jsouPoleStejna(poleFigurek[indexF].getPredchoziPole(),poleFigurek[indexF].getPole() )== true ) {
				
			}else {
				predchoziPole = poleFigurek[indexF].getPredchoziPole();
				aktualniPole = poleFigurek[indexF].getPole();
			}
		}
		}
		if(pocetTahu != 0) {
			g2.setColor(Color.BLUE);
			g2.fill(predchoziPole);	
			g2.fill(aktualniPole);
		}
			
		
		//Test sachu
		if(pocetTahu > 0) {
			for (int i = 0; i < poleFigurek.length; i++) {
				if (poleFigurek[i].getVyhozena() == false) {
					poleFigurek[i].ZkontrolujZakrytySach(poleFigurek, i, s);
				}
			}
		}
		
		//Test Matu
		jeMat();
		
		
		//Vyznaceni tahu
		for (int i = 0; i < poleFigurek.length; i++) {
				if(poleFigurek[i].getPresouvaSe() == true) {
				Rectangle2D[] mozneTahy = poleFigurek[i].getMozneTahyF();
					if (poleFigurek[indexF].getVybarvujMozneTahy() == true) {
						poleFigurek[indexF].setVybarvujMozneTahy(false);
						for (int j = 0; j < mozneTahy.length; j++) {
								if( ( s.jsouPoleStejna(poleFigurek[16].getPole(), mozneTahy[j]) || 
									  s.jsouPoleStejna(poleFigurek[17].getPole(), mozneTahy[j]) ) && i != 16 && i!=17) {
									g2.setColor(Color.ORANGE);
									g2.fill(mozneTahy[j]);
									g2.setColor(Color.GREEN);
									g2.draw(mozneTahy[j]);
								}else {
									g2.setColor(Color.RED);
									g2.fill(mozneTahy[j]);
									g2.setColor(Color.GREEN);
									g2.draw(mozneTahy[j]);
								}
						}
					}else if (poleFigurek[indexF].getVybarvujMozneTahy() == false) {
						for (int j = 0; j < mozneTahy.length; j++) {
								Color b = s.zjistiBarvu(mozneTahy[j]);
								g2.setColor(b);
								g2.fill(mozneTahy[j]);	
								
						}
					}
			}
		}
		
		rozradFigurky(this.poleFigurek, Color.LIGHT_GRAY, Color.DARK_GRAY, g2); 
		
		//Test sachu
		if(pocetTahu > 0) {
			for (int i = 0; i < poleFigurek.length; i++) {
				if (poleFigurek[i].getVyhozena() == false) {
					poleFigurek[i].ZkontrolujZakrytySach(poleFigurek, i, s);
				}
			}
		}

			//Brani mimochodem
			for (int i = 0; i < poleFigurek.length; i++) {
				// Testovani jestli protivnik mimochodem vynechal
				if(i <16 && poleFigurek[i] instanceof Pesci) {
					if(i < 16 ) {
						((Pesci)poleFigurek[i]).setPocetTahu(pocetTahu);
					}
					
					if(i < 16 && ( ((Pesci)poleFigurek[i]).getTahBraniMimo() +1) == ((Pesci)poleFigurek[i]).getPocetTahu() ) {
						((Pesci)poleFigurek[i]).setBraniMimochodem(null);
					}
					else if(i < 16 && ((Pesci)poleFigurek[i]).getBraniMimochodem() != null) {
						((Pesci)poleFigurek[i]).setTahBraniMimo(pocetTahu);
					}
				}
				//Ulozeni idnexu poli pro prenastaveni velikosti pri zmene velikosti sachovnice
				if(poleFigurek[i].getVyhozena() == false) {
					poleFigurek[i].vytvorMozneTahy(s, i, poleFigurek);
					poleFigurek[i].ulozIndexyPoleF(s);
					poleFigurek[i].ulozIndexyPoliMoznychTahuF(s);
					if(poleFigurek[i] instanceof Pesci && i<16 && ((Pesci)poleFigurek[i]) != null) {
						((Pesci)poleFigurek[i]).ulozIndexyMimochodemF(s);
					}
					if(indexF != -1) {
						poleFigurek[indexF].ulozIndexyPredchoziP(s);
					}
				}
			}
			
		//System.out.println(System.nanoTime()- start + " ns");
	}
	/**
	 * Metoda vytvori instance jednotlivych figurek a vlozi je do poleFigurek.
	 */
	public void vytvorFigurky() {
		for (int i = 0; i < 32; i++) { 
			if(i <= 15) {
				poleFigurek[i] = new Pesci(s.getSachovnice());
			}
			else if(i > 15 && i <= 17) {
				poleFigurek[i] = new Kralove(s.getSachovnice());
			}
			else if( i > 17 && i <= 19) {
				poleFigurek[i] = new Damy(s.getSachovnice());
			}
			else if(i > 19 && i <= 23) {
				poleFigurek[i] = new Strelci(s.getSachovnice());
			}
			else if(i > 23 && i <= 27) {
				poleFigurek[i] = new Kone(s.getSachovnice());
			}
			else {
				poleFigurek[i] = new Veze(s.getSachovnice());
			}	
		}
	}
	/**
	 * Metoda rozradi figurky pro dalsi zpracovani.
	 * @param poleFigurek - pole predstavujici reference na instance jednotlivych figurek
	 * @param c1 - barva hrace 1
	 * @param c2 - barva hrace 2
	 * @param g2
	 */
	public void rozradFigurky(IFigurka[] poleFigurek, Color c1, Color c2, Graphics2D g2) {
		int poziceS = 2;
		int poziceK = 1;
		int poziceV = 0;
		
		for (int i = 0; i < 32 ; i++) {
			if(i <= 15) {
				//test Promeny
				if(poleFigurek[i] instanceof Pesci == false) {
					int index = (i < 8)? 18:19;
					Color c = (i < 8)? c1:c2;
					otestujFigurku(poleFigurek[i], g2, c, c, index, 0, 0);
				}else {
					nastavPoziciPesce(poleFigurek[i], i, c1, c2, g2);	
				}
				
			}
			else if (i >= 16 && i <= 17) {
				nastavPoziciKrale(poleFigurek[i], i, c1, c2, g2);
			}else if( i >= 18 && i <= 19) {
				nastavPoziciDamy(poleFigurek[i], i, c1, c2, g2);
			}
			else if(i >= 20 && i <= 23) {
				if(i > 21) poziceS = 5;
				nastavPoziciStrelce(poleFigurek[i], i, poziceS, c1, c2, g2);	
			}
			else if(i >= 24 && i <= 27) {
				if(i > 25) poziceK = 6;
				nastavPoziciKone(poleFigurek[i], i, poziceK, c1, c2, g2);
			}
			else {
				if(i > 29) poziceV = 7;
				nastavPoziciVeze(poleFigurek[i], i, poziceV, c1, c2, g2);
			}
		}	
	}
	/**
	 * Pomocna metoda k metode rozradFigurky.
	 * Nastavi zacinajici pozici pesci.
	 * @param f - predana figurka
	 * @param i - pomocny index k nastaveni pozice prislusne figurky
	 * @param c1 - barva hrace 1
	 * @param c2 - barva hrace 2
	 * @param g2
	 */
	private void nastavPoziciPesce(IFigurka f, int i, Color c1, Color c2, Graphics2D g2) {
		double x = 0;
		double y = 0;
		Color c;
		
		if(i < 8) {
			c = c1;
			 x = s.getSachovnice()[6][i].getX();
			 y = s.getSachovnice()[6][i].getY();		 
		}else {
			c = c2;
			 x = s.getSachovnice()[1][i-8].getX();
			 y = s.getSachovnice()[1][i-8].getY();	
		}
		otestujFigurku(f, g2, c, c, i, x, y);
	}
	/**
	 * Pomocna metoda k metode otestujFigurku.
	 * Vykresli pesce na sachovnici.
	 * @param p - predany pesec
	 * @param c - barva pesce
	 * @param g2
	 */
	private void vykresliPesce(Pesci p, Color c, Graphics2D g2){	
		g2.setColor(c);
		p.vytvorP();
		g2.fill(p.getPesec()); 	
	}
	/**
	 * Pomocna metoda k metode rozradFigurky.
	 * Nastavi zacinajici pozici krali. 
	 * @param f - predana figurka
	 * @param i - pomocny index k nastaveni pozice prislusne figurky
	 * @param c1 - barva hrace 1
	 * @param c2 - barva hrace 2
	 * @param g2
	 */
	private void nastavPoziciKrale(IFigurka f, int i, Color c1, Color c2, Graphics2D g2) {
		double x;
		double y;
		Color c3;
		Color c;
		if(i % 2 == 1) {
			c = c2;
			c3 = c1;
			x = s.getSachovnice()[0][i-13].getX();
			y = s.getSachovnice()[0][i-13].getY();
			
		}else {
			c = c1;
			c3 = c2;
			x = s.getSachovnice()[7][i-12].getX();
			y = s.getSachovnice()[7][i-12].getY();
		}
		otestujFigurku(f, g2, c, c3, i, x, y);
	}
	/**
	 * Pomocna metoda k metode otestujFigurku.
	 * Vykresli krale na sachovnici.
	 * @param k - predany kral
	 * @param c - barva krale
	 * @param c3 - barva koruny
	 * @param g2
	 */
	private void vykresliKrale(Kralove k, Color c, Color c3, Graphics2D g2){
		g2.setColor(c);
		k.vytvorK();
		g2.fill(k.getKral());
		g2.setColor(c3);
		k.vytvorKor();
		g2.fill(k.getKoruna());	
	}
	/**
	 * Pomocna metoda k metode rozradFigurky.
	 * Nastavi zacinajici pozici dame.
	 * @param f - predana dama
	 * @param i - pomocny index k nastaveni pozice prislusne figurky
	 * @param c1 - barva hrace 1
	 * @param c2 - barva hrace 2
	 * @param g2
	 */
	private void nastavPoziciDamy(IFigurka f, int i, Color c1, Color c2, Graphics2D g2) {
		double x;
		double y;
		Color c;
		if(i % 2 == 1) {
			c = c2;			
			x = s.getSachovnice()[0][i-16].getX();
			y = s.getSachovnice()[0][i-16].getY();
			
		}else {
			c = c1;
			x = s.getSachovnice()[7][i-15].getX();
			y = s.getSachovnice()[7][i-15].getY();
		}
		otestujFigurku(f, g2, c, c, i, x, y);
	}
	/**
	 * Pomocna metoda k metode otestujFigurku.
	 * Vykresli damu na sachovnici.
	 * @param d - predana dama
	 * @param c - barva damy
	 * @param g2
	 */
	private void vykresliDamy(Damy d, Color c, Graphics2D g2){
		g2.setColor(c);
		d.vytvorD();
		g2.fill(d.getDama()); 
	}
	/**
	 * Pomocna metoda k metode rozradFigurky.
	 * Nastavi zacinajici pozici strelci. 
	 * @param f - predana figurka
	 * @param i - pomocny index k nastaveni pozice prislusne figurky
	 * @param pozice - index, ktery definuje do jakeho pole se ma strelec vykreslit
	 * @param c1 - barva hrace 1
	 * @param c2 - barva hrace 2
	 * @param g2
	 */
	private void nastavPoziciStrelce(IFigurka f, int i, int pozice, Color c1, Color c2, Graphics2D g2) {
		double x;
		double y;
		Color c3;
		Color c;
		if(i % 2 == 1) {
			c = c2;
			c3 = c1;
			x = s.getSachovnice()[0][pozice].getX();
			y = s.getSachovnice()[0][pozice].getY();		
		}else {
			c = c1;
			c3 = c2;
			x = s.getSachovnice()[7][pozice].getX();
			y = s.getSachovnice()[7][pozice].getY();
		}
		otestujFigurku(f, g2, c, c3, i, x, y);
	}
	/**
	 * Pomocna metoda k metode otestujFigurku.
	 * Vykresli strelce na sachovnici.
	 * @param s - predany strelec
	 * @param c - barva strelce
	 * @param c3 - barva krize
	 * @param g2
	 */
	private void vykresliStrelce(Strelci s, Color c, Color c3, Graphics2D g2){
		g2.setColor(c);
		s.vytvorS();
		g2.fill(s.getStrelec()); 
		g2.setColor(c3);
		s.vytvorKrz();
		g2.fill(s.getKriz());
	}
	/**
	 * Pomocna metoda k metode rozradFigurky.
	 * Nastavi zacinajici pozici jezdci. 
	 * @param f - predana figurka
	 * @param i - pomocny index k nastaveni pozice prislusne figurky
	 * @param pozice - index, ktery definuje do jakeho pole se ma jezdec vykreslit
	 * @param c1 - barva hrace 1
	 * @param c2 - barva hrace 2
	 * @param g2
	 */
	private void nastavPoziciKone(IFigurka f, int i, int pozice, Color c1, Color c2, Graphics2D g2) {
		double x;
		double y;
		Color c;
		Color c3;
		if(i % 2 == 1) {
			c = c2;
			c3 = c1;
			x = s.getSachovnice()[0][pozice].getX();
			y = s.getSachovnice()[0][pozice].getY();	
		}else {
			c = c1;
			c3 = c2;
			x = s.getSachovnice()[7][pozice].getX();
			y = s.getSachovnice()[7][pozice].getY();
		}	
		otestujFigurku(f, g2, c, c3, i, x, y);
	}	
	/**
	 * Pomocna metoda k metode otestujFigurku.
	 * Vykresli kone na sachovnici.
	 * @param j - predany kun
	 * @param c - barva kone
	 * @param c3 - barva oka kone
	 * @param g2
	 */
	private void vykresliKone(Kone j, Color c, Color c3, Graphics2D g2){
		g2.setColor(c);
		j.vytvorK();
		g2.fill(j.getKun()); 
		g2.setColor(c3);
		j.vytvorOko();
		g2.fill(j.getOko());
	}
	/**
	 * Pomocna metoda k metode rozradFigurky.
	 * Nastavi zacinajici pozici vezi. 
	 * @param f - predana figurka
	 * @param i - pomocny index k nastaveni pozice prislusne figurky
	 * @param pozice - index, ktery definuje do jakeho pole se ma vez vykreslit
	 * @param c1 - barva hrace 1
	 * @param c2 - barva hrace 2
	 * @param g2
	 */
	private void nastavPoziciVeze(IFigurka f, int i, int pozice, Color c1, Color c2, Graphics2D g2) {
		double x;
		double y;	
		Color c;
		if(i % 2 == 1) {
			c = c2;
			x = s.getSachovnice()[0][pozice].getX();
			y = s.getSachovnice()[0][pozice].getY();		
		}else {
			c = c1;
			x = s.getSachovnice()[7][pozice].getX();
			y = s.getSachovnice()[7][pozice].getY();
		}
		otestujFigurku(f, g2, c, c, i, x, y);
	}
	/**
	 * Pomocna metoda k metode otestujFigurku.
	 * Vykresli vez na sachovnici.
	 * @param v - predana vez
	 * @param c - barva veze
	 * @param g2
	 */
	private void vykresliVez(Veze v, Color c, Graphics2D g2){
		g2.setColor(c);
		v.vytvorV();
		g2.fill(v.getVez()); 
	}
	/**
	 * Metoda otestuje jestli figurka zmenila svoji vychozi pozici.
	 * V pripade, ze figurka neopustila vychozi pozici, tak nastavi figurce predanou vychozi pozici. (x,y)
	 * Pokud figurka pozici opustila, ale v jejim cilovem poli se nachazi jina figurka, tak metoda puvodni
	 * figurku prebarvy barvou pole.
	 * Dale figurku preda metode k vykresleni, pokud figurka nepatri mezi vyhozene
	 * @param f - predana figurka
	 * @param g2
	 * @param c - barva figurky
	 * @param c3 - barva dopnku figurky
	 * @param i - index predstavujici konkretni figurky v poliFigurek
	 * @param x - vychozi pozice x danne figurky
	 * @param y - vychozi pozice y danne figurky
	 */
	private void otestujFigurku(IFigurka f, Graphics2D g2, Color c, Color c3, int i, double x, double y) {
		if (f.getPresouvaSe() == true) {
			f.setPresouvaSe(false);

		}else if ( f.getOpustilZacPoz() == false ){
			f.setPoziceX(x);
			f.setPoziceY(y);
			Rectangle2D pole = s.zjistPole(x, y);
			f.setPole(pole);
		}
		
		
		if( f.getVyhozena() == false) {
			 
			  if(i <= 15) {
				 Pesci p = (Pesci)f;
				 vykresliPesce(p, c, g2);		
			}
			else if (i >= 16 && i <= 17) {
				Kralove k = (Kralove)f;
				vykresliKrale(k, c, c3, g2);
			}else if( i >= 18 && i <= 19) {
				Damy d = (Damy)f;
				vykresliDamy(d, c, g2);
			}
			else if(i >= 20 && i <= 23) {	
				Strelci s = (Strelci)f;
				vykresliStrelce(s, c, c3, g2);
			}
			else if(i >= 24 && i <= 27) {
				Kone j = (Kone)f;
				vykresliKone(j, c, c3, g2);
			}
			else {
				Veze v = (Veze)f;
				vykresliVez(v, c3, g2);
			}
			
		}
	}
	/**
	 * Metoda vytvori instanci sachovnice, na kterou odkaze refernci s.
	 * @param sirka - sirka okna
	 * @param vyska - vyska okna
	 * @param nejmensi - mensi z sirky nebo vysky
	 * @param nejvetsi - vetsi z sirky nebo vysky
	 */
	public void vystavSachovnici(double sirka, double vyska, double nejmensi, double nejvetsi) {
		s = new Sachovnice(sirka, vyska, nejmensi, nejvetsi);
		s.vytvorSachovnici();
	}
	/**
	 * Metoda vykresli ctvercovou sachovnici, která bude na středu okna
	 * a bude zabı́rat maximálnı́ možný prostor tohoto okna. 
	 * @param hraciPole - sachovnice - pole Rectanglu2D, ktere predstavuji jednotliva hraci pole 
	 * @param c1 - prvni barva poli
	 * @param c2 - druha barva poli
	 * @param g2
	 */
	public void vykresliSachovnici(Rectangle2D[][] hraciPole, Color c1, Color c2, Graphics2D g2) {
		for (int i = 1;  i <= hraciPole.length; i++) {
			for (int j = 1; j <= hraciPole.length; j++) {
				g2.setColor(c2);
				if((i%2==1 && j%2==1) || (i%2==0 && j%2==0)) {
					g2.setColor(c1);
				}
				g2.fill(hraciPole[i-1][j-1]);
			}
		}
	}
	/**
	 * Metoda zjisti jestli byla mysi vybrana figurka a nastavi potrebne atributy. 
	 * @param x - x-ova souradnice stisku mysi
	 * @param y - y-ova souradnice stisku mysi
	 */
	public void jeOznacenaF(double x, double y) {
		//inicializace hrace ktery ma tahnout
		int tahneHrac = (pocetTahu % 2 == 0)?1:2;
		for (int i = 0; i < poleFigurek.length; i++) {
			int hrac = (i < 8 || (i > 15 && i % 2 == 0))?1:2;
			if( poleFigurek[i].getVyhozena() == false && poleFigurek[i].jeVybrana(x, y) == true  && 
				( (tahneHrac == 1 && hrac == 1) || (tahneHrac == 2 && hrac == 2)) ) {			
				vybranaF = true;	
				indexF = i;
				poleFigurek[indexF].setVybarvujMozneTahy(true);
				break;
			}else {
				vybranaF = false;
			}
		}
	}
	
	/**
	 * Metoda nastavi pozice danne figurky do pozadovaneho pole.
	 * @param poleX - x-ova souradnice ciloveho pole
	 * @param poleY - y-ova souradnice ciloveho pole
	 */
	public void umistiFigurku(double poleX, double poleY){
		poleFigurek[indexF].setPoziceX(poleX);
		poleFigurek[indexF].setPoziceY(poleY);
	}
	public void nastavPoziciF(Rectangle2D cilovePole){
		
		//Test sachu
			for (int i = 0; i < poleFigurek.length; i++) {
				if (poleFigurek[i].getVyhozena() == false) {
					poleFigurek[i].ZkontrolujZakrytySach(poleFigurek, i, s);
				}
			}
		//zvaliduje jestli je tah v poradku vzhledem k pravidlum sachu a vnitrne natavi figurce atributy
		int indexPlatnehoPole = poleFigurek[indexF].zvalidujTah(cilovePole, s, poleFigurek, indexF);
		
		
		if (indexPlatnehoPole != -1) {
			pocetTahu++;
			vybarvujTahy = true;
			
			for(int i = 0; i < poleFigurek.length; i++) {
				if(poleFigurek[i].getVyhozena()==false) {
					poleFigurek[i].vytvorMozneTahy(s, i, poleFigurek);
					
					poleFigurek[i].ulozIndexyPoliMoznychTahuF(s);	
				}
			}
		}else {
			vybarvujTahy = false;
		}
			poleFigurek[indexF].setPoziceX( poleFigurek[indexF].getPole().getX() );  
			poleFigurek[indexF].setPoziceY(	poleFigurek[indexF].getPole().getY() ); 	
	}
	/**
	 * Nastavi hraciPole jednotlivym figurkam.
	 */
	public void nastavHraciPoleFigurek() {
		for (int i = 0; i < poleFigurek.length; i++) {
			poleFigurek[i].setHraciPole(s.getSachovnice());
		}
	}
	/**
	 * Prenastavi pozice presunu a umisteni jednotlivych figurek.
	 */
	public void prenastavPozicePresunu(){
		for(int i = 0; i<poleFigurek.length;i++) {
			if(poleFigurek[i].getVyhozena()==false) {
				poleFigurek[i].prenastavPoleF(s);
				poleFigurek[i].setPoziceX(poleFigurek[i].getPole().getX());
				poleFigurek[i].setPoziceY(poleFigurek[i].getPole().getY());
				poleFigurek[i].prenastavMozneTahyF(s);
				if (poleFigurek[i] instanceof Pesci && i < 16 && ((Pesci)poleFigurek[i]) != null) {
					((Pesci)poleFigurek[i]).prenastavPoleMimochodemF(s);
					
				}
			}
			
		}	
		if(indexF !=-1 && poleFigurek[indexF].getPredchoziPole() != null) {
			poleFigurek[indexF].prenastavPredchoziP(s);
			predchoziPole = poleFigurek[indexF].getPredchoziPole();
			aktualniPole = poleFigurek[indexF].getPole();
		}
	}
	/**
	 * Prenastavi rozmery sachovnice.
	 * @param sirka - sirka okna
	 * @param vyska - vyska okna
	 */
	public void prenastavSachovnici(double sirka, double vyska){
		s.setSirka(sirka);
		s.setVyska(vyska);
		s.setNejmensi(Math.min(sirka, vyska));
		s.setNejvetsi(Math.max(sirka, vyska));
		s.setSachovnice(s.getSirka(), s.getVyska(), s.getNejmensi(), s.getNejvetsi());
		s.vytvorSachovnici();
	}	
	/**
	 * Metoda testuje jestli doslo k matu.
	 */
	public void jeMat() {
		int hrac = (indexF < 8 || (indexF > 15 && indexF %2 == 0))?1:2;
		int kral = (hrac ==1)?17:16;
		boolean maSeKralHnout = true;
		boolean mohouSeHnoutF = true;
		boolean jeKralPodUtokem = false;
		//kral se nemuze hnout
		if(poleFigurek[kral].getMozneTahyF() != null && poleFigurek[kral].getMozneTahyF().length == 0 ) {
			maSeKralHnout = false;
		}
		//kralovo figurky se nemohou hnout
		for (int i = 0; i < poleFigurek.length; i++) {
			int tym = (i < 8 || (i > 15 && i %2 == 0))?1:2;
			if( (kral == 17 && tym == 2) || (kral == 16 && tym ==1) ) {
				
				if(poleFigurek[i].getMozneTahyF() != null && poleFigurek[i].getVyhozena() == false
				   && poleFigurek[i].getMozneTahyF().length != 0 ) {
					mohouSeHnoutF = true;
					break;
				}else {
					
					mohouSeHnoutF = false;
				}
			}
		}
		//nektera z figurek protihrace na krale utoci neboli kral je v sachu
		if( ((Kralove)poleFigurek[kral]).getSach() == true) {
			jeKralPodUtokem = true;
		}else {
			jeKralPodUtokem = false;
		}
		
		
		if(maSeKralHnout == false && mohouSeHnoutF == false && jeKralPodUtokem == true) {
			System.out.println("Sach mat - GAME OVER");
			hrac = (kral ==16)? 1: 2;
			System.out.println("Prohral hrac: "+hrac);
		}
	}
}
