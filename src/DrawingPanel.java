import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.JPanel;
public class DrawingPanel extends JPanel{
	//pole ve kterem se nachazi vsechny uzle site
	private static NetworkNode[] nodes;
	//pole ve kterem se nachazi vsechny trubky
	private static Pipe[] pipes;
	//uchovava sirku site - pocita i s elementy na extremnich souradnic (sirka sveta cele site)
	private static double sirkaSite;
	//uchovava vysku site - pocita i s elementy na extremnich souradnic (vyska sveta cele site)
	private static double vyskaSite;
	//promena uchovava bod (extemni souradnici) elementu site, ktery se nachazi nejvice vlevo	
	private static double minX = Integer.MAX_VALUE;
	//promena uchovava bod (extemni souradnici) elementu site, ktery se nachazi nejvice vpravo	
	private static double maxX = Integer.MIN_VALUE;
	//promena uchovava bod (extemni souradnici) elementu site, ktery se nachazi nejvice nahore	
	private static double minY = Integer.MAX_VALUE;
	//promena uchovava bod (extemni souradnici) elementu site, ktery se nachazi nejvice dole	
	private static double maxY = Integer.MIN_VALUE;
	//atribut ktery udava velikost elementu v pixelech
	private static double glyphSize = 0;
	//atribut ktery obsahuje hodnotu pro prepocet souradnic aby byli zachovany pomery v okne
	private static double scale;
	//instance vodovodni site
	private static WaterNetwork wn;
	//atribut ktery obsahuje pole, ktere urcuje jestli je zacatecni nebo koncovi uzel rezervoarem nebo pohym spojovim bodem
	private static boolean[] cile;
	//list pro zachovani tvaru rezervoaru k testovani jestli uzivatel klikl na rezervoar
	private static ArrayList<Rectangle2D> rezervoary = new ArrayList<>();
	//list pro zachovani tvaru hitboxu pro stredy potrubi
	private static ArrayList<Rectangle2D> stredyPotrubi = new ArrayList<>();
	//pro zachovani objektu hitboxu na kohouty
	private static ArrayList<Ellipse2D> kohouty = new ArrayList<>();
	//atribut obsahuje miru otevrenosti kohoutu pri nastavovani kouhout uzivatelem
	private static float kohoutOtevrenost = -1; 
	//index aktualne nastavovaneho kohoutu
	private static int indexKohoutu = -1;
		
	public DrawingPanel(int size, WaterNetwork wn) {
		this.setPreferredSize(new Dimension(800, 600));
		//nastaveni glyphsize
		glyphSize = size;
		//nastaveni site
		this.wn = wn;
	}
	@Override
	public void paint(Graphics g){
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		try {
			//nacteme rezervoary a trubky do poli
			nactiScenar();
			//inicializace meritka
			initScale(this.getWidth(), this.getHeight(), glyphSize, wn);
			//vykreslime trubky
			drawTrubky(g2);
			//vykreslime rezervoary
			drawRezervoary(g2);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	/**
	 * Metoda naplni pole trubkama a uzlama site
	 * @throws InterruptedException - vyjimka
	 */
	public static void nactiScenar() throws InterruptedException {
		nodes = wn.getAllNetworkNodes();
		pipes = wn.getAllPipes();
	}
	/**
	 * Metoda inicializuje atribut scale, ktery se pote pouziva k prenasobeni souradnic.
	 * @param panelSizeX - sirka okna
	 * @param panelSizeY - vyska okna
	 * @param size - glyphSize
	 * @param network - WaterNetwork instance (vodovodni sit)
	 */
	public static void initScale(int panelSizeX, int panelSizeY, double size, WaterNetwork network) {
		//aktualizace pomocnych promenych pri kazdem prekresleni okna
		minX = Integer.MAX_VALUE;
		maxX = Integer.MIN_VALUE;
		minY = Integer.MAX_VALUE;
		maxY = Integer.MIN_VALUE;
		//jdeme pres vsechny uzle
		for (int i = 0; i < nodes.length; i++) {
			double  xmensi = 0;
			double  xvetsi = 0;
			double  ymensi = 0;
			double  yvetsi = 0;
			//pokud je uzel rezervoar pak musime dopocist jeho krajni body
			if (nodes[i] instanceof Reservoir) {
				 Reservoir r = (Reservoir)nodes[i];
				 xmensi = r.getX()-(size/2.0);
				 xvetsi = r.getX()+(size/2.0);
				 ymensi = r.getY()-(size/2.0);
				 yvetsi = r.getY()+(size/2.0);
				 //zkontrolujeme min a max (extremni souradnice)
				 zkontrolujMinMax(xmensi, ymensi);
				 zkontrolujMinMax(xvetsi, yvetsi);
			}else {
				 zkontrolujMinMax(nodes[i].getX(),nodes[i].getY());
			}	
		}
		 //spocteme sirku sveta
		 sirkaSite = maxX - minX;
		 //spocteme vysku sveta
		 vyskaSite = maxY - minY;
		 double scaleW = panelSizeX/sirkaSite;
		 double scaleH = panelSizeY/vyskaSite;
		 //vybereme mensi ze scalu tak aby nedoslo k naruseni pomeru a rozestaveni bodu v siti
		 scale = Math.min(scaleW, scaleH);		 
	}
	/**
	 * Metoda zkontroluje minima a maxima a pripadne prenastavi
	 * @param hodnotaX
	 * @param hodnotaY
	 */
	private static void zkontrolujMinMax(double hodnotaX, double hodnotaY) {
		if (hodnotaX > maxX) {
			maxX = hodnotaX;
		}
		if (hodnotaY > maxY) {
			maxY = hodnotaY;
		}
		if (hodnotaX < minX) {
			minX = hodnotaX;
		}
		if (hodnotaY < minY) {
			minY = hodnotaY;
		}	
	}
	/**
	 * Metoda vykresli rezervoar o velikosti size kdy jeho stred bude v bode (px,py), a vyplni ho vodou podle parametru fill.		
	 * @param px - x-ova souradnice
	 * @param py - y-ova souradnice
	 * @param g2 - graficky kontext
	 * @param size - pozadovana velikost rezervoaru odvozena od glyphSize
	 * @param fill - urcuje vysku hladiny vody v rezervoaru
	 * @param vycentrovaniX - atribut obsahujici posun v ose x aby se vycentrovali stredy rezervoaru pokud maji stejne x-ove souradnice z tridy WaterNetwork
	 * @param vycentrovaniY - atribut obsahujici posun v ose y aby se vycentrovali stredy rezervoaru pokud maji stejne y-ove souradnice z tridy WaterNetwork
	 */
	public static void drawReservoir(double px, double py, Graphics2D g2, double size, float fill, double vycentrovaniX, double vycentrovaniY) {
		//prescalovani bodu a posunuti bodu do leve horniho rohu
		px = (px-(glyphSize/2)) * scale;
		py = (py-(glyphSize/2)) * scale;
		//vypocteni sirka a vysky rezervoaru a jejich prescalovani
		double sirkaR = glyphSize*scale;
		double vyskaR = glyphSize*scale;
		//epsilon pro porovnavani double
		final double E = 0.0001;
		//dosloby k presazeni glyphSize velikosti prvku, je treba prepocitat souradnice
		if(scale > 1 ) {
			//Urcime o kolik je potreba rezervoar zmensit a o tento kus posuneme souradnice
			double pripocetX = (sirkaR/2) - (size/2);
			double pripocetY = (sirkaR/2) - (size/2);
			//prenastavime pripocty pro souradnice krajich sveta abychom maximalne vyuzili prostor okna
			double[] pripocet = jeNejakaSouradniceNaKrajiSveta(px, py, glyphSize, pripocetX, pripocetY, E);
			pripocetX = pripocet[0];
			pripocetY = pripocet[1];
			//pripocteme k souradnicim
			px = px + pripocetX;
			py = py + pripocetY;
			//vycentrujeme rezervoary ktere maji stejny stred
			px = px + vycentrovaniX;
			py = py + vycentrovaniY;
			//prenastavime vysku a sirku do parametru glyphSize
			sirkaR = size;
			vyskaR = size;
		}else {
			//Urcime o kolik je potreba rezervoar zmensit a o tento kus posuneme souradnice
			double pripocetX = (sirkaR/2) - ((size*scale)/2);
			double pripocetY = (sirkaR/2) - ((size*scale)/2);
			//prenastavime pripocty pro souradnice krajich sveta abychom maximalne vyuzili prostor okna
			double[] pripocet = jeNejakaSouradniceNaKrajiSveta(px, py, glyphSize, pripocetX, pripocetY, E);
			pripocetX = pripocet[0];
			pripocetY = pripocet[1];
			//pripocteme k souradnicim
			px = px + pripocetX;
			py = py + pripocetY;
			//vycentrujeme rezervoary ktere maji stejny stred
			px = px + (vycentrovaniX*scale);
			py = py + (vycentrovaniY*scale);
			//prenastavime vysku a sirku do parametru glyphSize
			sirkaR = size*scale;
			vyskaR = size*scale;
		}	
		//vykresleni rezervoaru o velikosti size * scale pokud scale <=1, jinak do parametru glyphsize
	    Rectangle2D nadrz = new Rectangle2D.Double( (px), (py), sirkaR, vyskaR);
		g2.setColor(Color.WHITE);
		g2.fill(nadrz);
		//pridame objekt nadrze do listu pro testovani jesli uzivatel na nadrz klikl
		rezervoary.add(nadrz);
		//nastaveni barvy pomoci rgb
		Color lightBlue= new Color(115,194,251);
		g2.setColor(lightBlue);
		//voda v rezervoaru
		Path2D voda = new Path2D.Double();
		voda.moveTo(px, py + ( (sirkaR)-(sirkaR*fill) ));
		voda.lineTo(px+(sirkaR),  py + ( (sirkaR)-(sirkaR * fill) ));
		voda.lineTo(px+(sirkaR), py + (sirkaR)); 
		voda.lineTo(px, py + (sirkaR));
		g2.fill(voda);
		g2.setColor(Color.BLACK);
		g2.setStroke(new BasicStroke(3));
		g2.draw(nadrz);
	}
	/**
	 * Metoda vykresli trubku v ni sipku ktera je umistena v pulce, je dlouha 1/4 delky potrubi a je orientovana podle flow. Nekam vedle sipky umisti popisek
	 * rychlosti toku vody. Dale vykresli kohout za zacatecnim bodem trubky. 
	 * @param pxStart - x-ova souradnice pocatecniho bodu trubky
	 * @param pyStart - y-ova souradnice pocatecniho bodu trubky
	 * @param pxEnd - x-ova souradnice koncoveho bodu trubky
	 * @param pyEnd - y-ova souradnice koncoveho bodu trubky
	 * @param size - glyphSize
	 * @param flow - urcuje rychlost a smer vody (minus zacina od konce do zacataku a + znamena opak)
	 * @param open - urcuje otevrenost kohout 1 znamena otevreny 0 zavreny
	 * @param g2 - graficky kontext
	 * @param cile - pole urcujici jestli zacatacni a koncove body jsou rezervoary (true je rezervoarem)
	 * @param vycentrovanaPotrubi - pole poli obsahujic dopocty pro vycentrovani potrubi aby sli do stredu rezervoaru
	 */
	public static void drawPipe(double pxStart, double pyStart, double pxEnd, double pyEnd, double[] size, float flow, float open, Graphics2D g2, boolean[] cile, double[][] vycentrovanaPotrubi) {
		//prescalovani bodu
		if(scale <= 1 ) {			
			//vypocteni sirka a vysky rezervoaru a jejich prescalovani
			double sirkaR = glyphSize*scale;
			double vyskaR = glyphSize*scale;
			//Urcime o kolik byl rezervoar zmensen a o tento kus posuneme souradnice potrubi
			double pripocetX = (sirkaR/2) - ((size[0]*scale)/2);
			double pripocetY = (vyskaR/2) - ((size[0]*scale)/2);
			//epsilon pro porovnavani double
			final double E = 0.0001;
			//pokud neni rezervoar tak bod muzeme dopocist pomoci scale
			if(cile[0] == false) {
				pxStart = (pxStart)*scale;
				pyStart = (pyStart)*scale;
			}else {//pokud je rezervoarem musime dopocist stred rezervoaru
				//levy hroni roh
				pxStart = (pxStart-(glyphSize/2.0))*scale;
				pyStart = (pyStart-(glyphSize/2.0))*scale;
				//pokud nejaka souradnice rezervoaru je na kraji sveta, tak abychom vyuzili cele okno musime souradnici posunout o 2 * pripocet
				double[] pripocet = jeNejakaSouradniceNaKrajiSveta(pxStart, pyStart, glyphSize, pripocetX, pripocetY, E);
				pripocetX = pripocet[0];
				pripocetY = pripocet[1];
				//pripoctem k souradnicim pripocet
				pxStart = pxStart + pripocetX;
				pyStart = pyStart + pripocetY;
				//vycentrujeme
				pxStart = pxStart + (vycentrovanaPotrubi[0][0]*scale);
				pyStart = pyStart + (vycentrovanaPotrubi[0][1]*scale);
				//posuneme se do stredu
				pxStart = pxStart + ((size[0]*scale)/2.0);
				pyStart = pyStart + ((size[0]*scale)/2.0);
			}
			//aktualizujeme pripocty
			pripocetX = (sirkaR/2) - ((size[1]*scale)/2);
			pripocetY = (vyskaR/2) - ((size[1]*scale)/2);
			//pokud neni rezervoar tak bod muzeme dopocist pomoci scale
			if(cile[1] == false) {
				pxEnd = (pxEnd)*scale;
				pyEnd = (pyEnd)*scale;
			}else {//pokud je rezervoarem musime dopocist stred rezervoaru
				//levy hroni roh
				pxEnd = (pxEnd-(glyphSize/2.0))*scale;
				pyEnd = (pyEnd-(glyphSize/2.0))*scale;
				//pokud nejaka souradnice rezervoaru je na kraji sveta, tak abychom vyuzili cele okno musime souradnici posunout o 2 * pripocet
				double[] pripocet = jeNejakaSouradniceNaKrajiSveta(pxEnd, pyEnd, glyphSize, pripocetX, pripocetY, E);
				pripocetX = pripocet[0];
				pripocetY = pripocet[1];
				//pripocteme pripocet k souradnicim
				pxEnd = pxEnd + pripocetX;
				pyEnd = pyEnd + pripocetY;
				//vycentrujeme
				pxEnd = pxEnd + (vycentrovanaPotrubi[1][0]*scale);
				pyEnd = pyEnd + (vycentrovanaPotrubi[1][1]*scale);
				//posuneme se do stredu
				pxEnd = pxEnd + ((size[1]*scale)/2.0);
				pyEnd = pyEnd + ((size[1]*scale)/2.0);
			}
		}else {
			//vypocteni sirka a vysky rezervoaru a jejich prescalovani
			double sirkaR = glyphSize*scale;
			double vyskaR = glyphSize*scale;
			//Urcime o kolik byl rezervoar zmensen a o tento kus posuneme souradnice potrubi
			double pripocetX = (sirkaR/2) - (size[0]/2);
			double pripocetY = (vyskaR/2) - (size[0]/2);
			//epsilon pro porovnavani double
			final double E = 0.0001;
			//prescalovani bodu v pripade kdyby by prvky presahli velikost glyphSize
			if(cile[0] == false) {//pokud neni rezervoar tak bod muzeme dopocist pomoci scale
				pxStart = (pxStart)*scale;
				pyStart = (pyStart)*scale;
			}else {//pokud je rezervoarem musime dopocist stred rezervoaru a velikosti prave glyphSize
				//levy hroni roh
				pxStart = (pxStart-(glyphSize/2.0))*scale;
				pyStart = (pyStart-(glyphSize/2.0))*scale;
				//pokud nejaka souradnice rezervoaru je na kraji sveta, tak abychom vyuzili cele okno musime souradnici posunout o 2 * pripocet
				double[] pripocet = jeNejakaSouradniceNaKrajiSveta(pxStart, pyStart, glyphSize, pripocetX, pripocetY, E);
				pripocetX = pripocet[0];
				pripocetY = pripocet[1];
				//pripoctem k souradnicim pripocet
				pxStart = pxStart + pripocetX;
				pyStart = pyStart + pripocetY;
				//vycentrujeme
				pxStart = pxStart + vycentrovanaPotrubi[0][0];
				pyStart = pyStart + vycentrovanaPotrubi[0][1];
				//posuneme se do stredu
				pxStart = pxStart + (size[0]/2.0);
				pyStart = pyStart + (size[0]/2.0);
			}
			//aktualizujeme pripocty
			pripocetX = (sirkaR/2) - (size[1]/2);
			pripocetY = (vyskaR/2) - (size[1]/2);
			if(cile[1] == false) {
				pxEnd = (pxEnd)*scale;
				pyEnd = (pyEnd)*scale;
			}else {
				//levy hroni roh
				pxEnd = (pxEnd-(glyphSize/2.0))*scale;
				pyEnd = (pyEnd-(glyphSize/2.0))*scale;
				//pokud nejaka souradnice rezervoaru je na kraji sveta, tak abychom vyuzili cele okno musime souradnici posunout o 2 * pripocet
				double[] pripocet = jeNejakaSouradniceNaKrajiSveta(pxEnd, pyEnd, glyphSize, pripocetX, pripocetY, E);
				pripocetX = pripocet[0];
				pripocetY = pripocet[1];
				//pripocteme pripocet k souradnicim
				pxEnd = pxEnd + pripocetX;
				pyEnd = pyEnd + pripocetY;
				//vycentrujeme
				pxEnd = pxEnd + vycentrovanaPotrubi[1][0];
				pyEnd = pyEnd + vycentrovanaPotrubi[1][1];
				//posuneme se do stredu
				pxEnd = pxEnd + (size[1]/2.0);
				pyEnd = pyEnd + (size[1]/2.0);
			}
		}
		
		//prurez trubky je spocitan jako 1 petina glyphSize * scale 
		double prurez = glyphSize*0.2*scale;
		if(scale > 1) {//pro lepsi vykresleni zvolime jiny prurez pokud mame vykresleni elementu do glyphSize
			prurez = glyphSize*0.4;
		}
		//definice a vykresleni trubky trubky
		Path2D trubka = new Path2D.Double();
        trubka.moveTo(pxStart, pyStart);
        trubka.lineTo(pxEnd, pyEnd);
		trubka.closePath();
		Color lightBlue= new Color(115,194,251);
		g2.setColor(lightBlue);
        g2.setStroke(new BasicStroke((int)prurez));
		g2.draw(trubka);
		//definice bodu zacatku a konce potrubi	
		double[] body = nastavKrajniBodyPotrubi(pxStart, pyStart, pxEnd, pyEnd, size, cile);
		double zacatekPotrubiX = body[0];
		double zacatekPotrubiY = body[1];
		double konecPotrubiX = body[2];
		double konecPotrubiY = body[3];
		//ziskani stredu potrubi
		double stredPipeX = zacatekPotrubiX + ((konecPotrubiX - zacatekPotrubiX)/2);
		double stredPipeY = zacatekPotrubiY + ((konecPotrubiY - zacatekPotrubiY)/2);
		//vytvoreni hitboxu pro grafy rychlosti toku vody v potrubi
		Rectangle2D hitBoxStreduPotrubi = new Rectangle2D.Double(stredPipeX-(prurez/2), stredPipeY-(prurez/2), prurez, prurez);
		stredyPotrubi.add(hitBoxStreduPotrubi);
		//vykresli sipku a kohout
		vykresliSipku(stredPipeX, stredPipeY, zacatekPotrubiX, zacatekPotrubiY, konecPotrubiX, konecPotrubiY, g2, prurez, flow, open);
	}
	/**
	 * Metoda vykresli kohout v bode ktery je umisten o polovinu delky (misto mezi zacatkem sipky a zacatkem potrubi) od zacatecniho bodu potrubi
	 * @param px - x-ova souradnice 
	 * @param py - y-ova souradnice
	 * @param size - delka kohoutu
	 * @param open - otevrenost kohout, 1 otevreny, 0 zavreny
	 * @param g2 - graficky kontext
	 */
	private static void drawValve(double px, double py, double size, float open, Graphics2D g2) {
		//vykresleni kohoutu
		Ellipse2D.Double kohout = new Ellipse2D.Double( (px-(size/2.0)), (py-(size/2.0)), size, size);
		//pridame kohou do listu
		kohouty.add(kohout);
		g2.setColor(Color.BLACK);
		g2.draw(kohout);
		g2.setColor(Color.WHITE);
		g2.fill(kohout);
        //nastaveni barvy pro vypln
		g2.setColor(Color.GRAY);
        //obsah kohoutu
        double obsah = Math.PI * ((size/2.0)*(size/2.0));
        //vypln kouhout je zavisla obsahu ktery je vypocitan pomoci parametru open
        obsah = obsah * (1-open);
        //vypocet polomeru vyplne
        double r = Math.sqrt(obsah/Math.PI);
        //umisteni vyplne uzavrenosti do stredu kohoutu a jeji nasledne vykresleni 
        Ellipse2D.Double uzavrenost = new Ellipse2D.Double( (px-r), (py-r), 2*r, 2*r);
        g2.fill(uzavrenost);
	}
	/**
	 * Metoda nacte pocatecni a koncove body trubky do pole a to vrati.
	 * @param i - index trubky v poli trubek
	 * @return body - double[]  
	 */
	public static double[] nactiBodyTrubky(int i) {
		double pxStart = 0;
		double pyStart = 0;
		double pxEnd = 0;
		double pyEnd = 0;
		cile = new boolean[2];
		double[] body = new double[4];
		if (pipes[i].start instanceof Reservoir) {
		  Reservoir r = (Reservoir)pipes[i].start;
		  pxStart = (r.getX()-minX);
		  pyStart = (r.getY()-minY);
		  body[0] = pxStart;
		  body[1] = pyStart;
		  cile[0] = true;
		}else {
		  pxStart = (pipes[i].start.getX()-minX);
		  pyStart = (pipes[i].start.getY()-minY);
		  body[0] = pxStart;
		  body[1] = pyStart;
		  cile[0] = false;
		}
		if(pipes[i].end instanceof Reservoir) {
		  Reservoir r = (Reservoir)pipes[i].end;
		  pxEnd = (r.getX()-minX);
		  pyEnd = (r.getY()-minY);
		  body[2] = pxEnd;
		  body[3] = pyEnd;
		  cile[1] = true;
		}else {
		  pxEnd = (pipes[i].end.getX()-minX);
		  pyEnd = (pipes[i].end.getY()-minY);
		  body[2] = pxEnd;
		  body[3] = pyEnd;
		  cile[1] = false;
		}	
		return body;
	}
	/**
	 * Metoda vykresli vsechny trubky i s sipkami, kohouty a popisky mezi bodami site. Vyuziva pomocnou metodu drawPipe().
	 * @param g2 - graficky kontext
	 */
	public static void drawTrubky(Graphics2D g2) {
		double[] body = new double[4];
	    float flow;
	    float open;
	    //pomocny index pro nastaveni kohoutu uzivatelem
	    int pocet = 0;
	    //inicializace listu pri kazdem prekresleni okna
	    stredyPotrubi = new ArrayList<>();
	    kohouty = new ArrayList<>();
	    //for cyklus zajisti vytvoreni trubek 
		for (int i = 0; i < pipes.length; i++) {
			//pokud se jedna o uzivatelem nastavovany kohout, tak kohout prenastavime
			if(kohoutOtevrenost != -1 && pocet == indexKohoutu ) {
				//nastavime trubce otevrenost kohoutu
				pipes[i].setOpen(kohoutOtevrenost);
				kohoutOtevrenost = -1;
			}
			//nacteme body
			body = nactiBodyTrubky(i);
			//flow udava jak rychle voda tece trubkou
			flow = (float)pipes[i].getFlow();
			//open urcuje jak moc je otevreny kohout 1 uplne 0 zavreny
			open = (float)pipes[i].getOpen();
			//pridani hodnoty rychlosti toku a casu do ArrayListu
			WNVis_SP2024.setRychlostiToku(flow, pocet);
			WNVis_SP2024.setCasyNamerenychToku(wn.currentSimulationTime(), pocet);
			//ziskame z bodu rozmery rezervoaru ktere jsou potreba pro do pocteni kraju potrubi
			double[] rozmeryRezervoaru = najdiRozmeryRezervoaru(body);
			//ziskame jejich vycentrovani
			double[][] vecentrovaniPotrubi = vypocitejVycentrovaniRezervoaru(body); 
			//vykresleni trubky
			drawPipe(body[0], body[1], body[2], body[3], rozmeryRezervoaru, flow, open, g2, cile, vecentrovaniPotrubi);
			//zvysime pocet
			pocet++;
		}
	}
	/**
	 * Metoda vykresli vsechny rezervoary. Vyuziva pomocnou metodu drawReservoir()
	 * @param g2 - graficky kontext
	 */
	public static void drawRezervoary(Graphics2D g2) {
		//pomocna promena pro udrzeni indexu pro identifikaci rezervoaru v listu
		int pocet = 0;
		rezervoary = new ArrayList<>();
		//nacteme si proporcionalni rozmery rezervoaru
		double[][] proporcionalniRomzemryRezervoaru = vypoctiProporcionalniRozmeryRezervoaru();
		//projdem vsechny uzle site
		for (int i = 0; i < nodes.length; i++) {
			//pokud je uzel instanci Rezervoaru
			if (nodes[i] instanceof Reservoir) {
				//pretypovani
				Reservoir r = (Reservoir)nodes[i];
				//vypocet pro vypln rezervoaru vodou 
				double vypln = (r.content/r.capacity);
				//navyseni indexu
				pocet++;
				//pridani hodnoty naplneni a casu do ArrayListu
				WNVis_SP2024.setNaplneniRezervoaru(r.getContent(), pocet-1);
				WNVis_SP2024.setCasNaplneniRezervoaru(wn.currentSimulationTime(), pocet-1);
				//ziskame sirku rezervoaru neboli stranu a
				double sirkaR = proporcionalniRomzemryRezervoaru[i][0];
				//ziskame vycentrovani v ose x
				double vycentrovaniX = proporcionalniRomzemryRezervoaru[i][1];
				//ziskame vycentrovani v ose y
				double vycentrovaniY = proporcionalniRomzemryRezervoaru[i][2];
				//vykresleni 
				drawReservoir( (nodes[i].getX()-minX), (nodes[i].getY()-minY), g2, sirkaR, (float)vypln, vycentrovaniX, vycentrovaniY);
			}
		}
	}
	/**
	 * Metoda vykresli sipku ktera je umistena v pulce, je dlouha 1/4 delky potrubi a je orientovana podle flow. Nekam vedle sipky umisti popisek
	 * rychlosti toku vody. Dale vykresli kohout za zacatecnim bodem trubky.
	 * @param stredPipeX - x-ova souradnice stredu trubky
	 * @param stredPipeY - y-ova souradnice stredu trubky
	 * @param zacatekPotrubiX - x-ova souradnice zacatku trubky
	 * @param zacatekPotrubiY - y-ova souradnice zacatku trubky
	 * @param konecPotrubiX x-ova souradnice konce trubky
	 * @param konecPotrubiY y-ova souradnice konce trubky
	 * @param g2 - graficky kontext
	 * @param prurez - prurez trubky
	 * @param flow - urcuje smer a rychlost vody
	 * @param open - urcuje otevrenost a uzavrenost kohoutu
	 */
	public static void vykresliSipku(double stredPipeX, double stredPipeY, double zacatekPotrubiX, double zacatekPotrubiY, double konecPotrubiX, double konecPotrubiY, Graphics2D g2, double prurez, double flow, float open){
		//definovani bodu sipky v potrubi
		//sipka je dlouha 1/4 potrubi
		double zacatekSipkyX = stredPipeX - ((konecPotrubiX - zacatekPotrubiX)/8);
		double zacatekSipkyY = stredPipeY - ((konecPotrubiY - zacatekPotrubiY)/8);
		double konecSipkyX = stredPipeX + ((konecPotrubiX - zacatekPotrubiX)/8);
		double konecSipkyY = stredPipeY + ((konecPotrubiY - zacatekPotrubiY)/8);	
		//vykresleni tela
		g2.setStroke(new BasicStroke(3));
		g2.setColor(Color.BLACK);
		Path2D sp = new Path2D.Double();
		sp.moveTo(zacatekSipkyX, zacatekSipkyY);
		sp.lineTo(konecSipkyX, konecSipkyY);
		sp.closePath();
		g2.draw(new Line2D.Double(zacatekSipkyX, zacatekSipkyY, konecSipkyX, konecSipkyY));
		//polovicni delka sipky
		double polDelkaSipky = Math.sqrt(((zacatekSipkyX - konecSipkyX)* (zacatekSipkyX - konecSipkyX)) + ((zacatekSipkyY - konecSipkyY)* (zacatekSipkyY - konecSipkyY)))/2;
		//vektor sipky
		double ux = konecSipkyX - zacatekSipkyX;
		double uy = konecSipkyY - zacatekSipkyY;
		//jednotkovy vektor
		double u_len1 = 1 / Math.sqrt(ux * ux + uy*uy); 
		ux *= u_len1;
		uy *= u_len1;
		//pro posun do bodu q a x
		double delka = polDelkaSipky/1.25;
		//body na tele sipce z kterych jdou normalove viktory
		double q_x = konecSipkyX - ux * (delka);
		double q_y = konecSipkyY - uy * (delka);
		double c_x = zacatekSipkyX + ux * (delka);
		double c_y = zacatekSipkyY + uy * (delka);
		//delka od bodu cx, qx do krajnich bodu
		double d = prurez/2;
		//pro lepsi vykresleni sipky kdyz je prurez vetsi nez delka sipky
		if(d > delka ) {
			d = delka;
		}
		//body okraje sipek, vyuziva se normaloveho vektoru
		double d_x = q_x + uy * d;
		double d_y = q_y - ux * d;
		double l_x = q_x - uy * d;
		double l_y = q_y + ux * d;
		//body okraje sipek z druhe strany
		double f_x = c_x + uy * d;
		double f_y = c_y - ux * d;
		double r_x = c_x - uy * d;
		double r_y = c_y + ux * d;
		//promene pro vykresleni smeru sipky
		double konecnyBodX = 0;
		double konecnyBodY = 0;
		double zacatecniBodX = 0;
		double zacatecniBodY = 0;
		double okraj1X = 0;
		double okraj1Y = 0;
		double okraj2X = 0;
		double okraj2Y = 0;
		//pokud je kladne tak tece proud od zacatku do konce
		if (flow >= 0) {
			konecnyBodX = konecSipkyX;
			konecnyBodY = konecSipkyY;
			zacatecniBodX = zacatekSipkyX;
			zacatecniBodY = zacatekSipkyY;
			okraj1X = d_x;
			okraj1Y = d_y;
			okraj2X = l_x;
			okraj2Y = l_y;
		}else {//pokud zaporne tak tece proud od konce do zacatku
			konecnyBodX = zacatekSipkyX;
			konecnyBodY = zacatekSipkyY;
			zacatecniBodX = konecSipkyX;
			zacatecniBodY = konecSipkyY;
			okraj1X = f_x;
			okraj1Y = f_y;
			okraj2X = r_x;
			okraj2Y = r_y;
		}
		//vykresleni smeru sipky
		Path2D tip = new Path2D.Double();
		tip.moveTo(okraj1X , okraj1Y);
		tip.lineTo(konecnyBodX , konecnyBodY);
		tip.lineTo(okraj2X, okraj2Y);
		//spojime tip s telem sipky
		sp.append(tip, true);
		g2.setColor(Color.BLACK);
		g2.setStroke(new BasicStroke(3));
		g2.draw(tip);
		//ziskani bodu ve stredu sipky
		double ox = konecSipkyX - zacatekSipkyX;
		double oy = konecSipkyY - zacatekSipkyY;
		double len2 = Math.sqrt(ox * ox + oy*oy)/2;
		c_x = konecSipkyX - ux * (len2);
		c_y = konecSipkyY - uy * (len2);
		g2.setColor(Color.black);
		d = d * 1.2;
		//ziskani bodu vedle stredu sipky
		f_x = c_x + uy * d;
		f_y = c_y - ux * d;
		//formatovani stringu pro vypis na 3 desitine mista
		String rychlost = String.format("%.3f", flow) + " m^3/s";
		//delka sipky
		double j_x = konecSipkyX - zacatekSipkyX;
		double j_y = konecSipkyY - zacatekSipkyY;
		double len_2 = Math.sqrt( (j_x*j_x) + (j_y*j_y) );
		//nastaveni fontu
		Font f = new Font( "Calibri", Font.BOLD,(int)(len_2*0.2) );
		g2.setFont(f);
		//upravime pozici popisku pokud se protina s sipkou nebo je popisek mimo okno a vykreslime
		upravPoziciRetezceAVykresli(g2, rychlost, f_x, f_y, f, c_x, c_y, ux, uy, d, len_2, sp);
		//definovani bodu pro vykresleni kohoutu ktery bude umisten od zacatku potrubi + polovina delky mezi zacatkem potrubi a zacatkem sipky
		double KohoutX = zacatekPotrubiX + ((zacatekSipkyX - zacatekPotrubiX)/2 );
		double KohoutY = zacatekPotrubiY + ((zacatekSipkyY - zacatekPotrubiY)/2 );
		double delkaKohoutu = glyphSize * 0.7;
		//pro lepsi vykresleni pokud je glyhpSize*0,7 vetsi nez pulka sipky
		if(delkaKohoutu > polDelkaSipky) {
			delkaKohoutu = polDelkaSipky;
		}
		//vykresleni kohoutu
		drawValve(KohoutX, KohoutY, delkaKohoutu, open, g2);
}
	/**
	 * Metoda umisti lepe popisek oproti sipce pokud je potreba
	 * @param g2 - graficky kontext
	 * @param rychlost - String popis s rychlosti
	 * @param f_x - x-ova souradnice popisku
	 * @param f_y - y-ova souradnice popisku
	 * @param f - font pisma popisku
	 * @param c_x - x-ova souradnice stredu sipky
	 * @param c_y - y-ova souradnice stredu sipky
	 * @param ux - x-ova slozka jednotkoveho vektoru
	 * @param uy - y-ova slozka jednotkoveho vektoru
	 * @param d - vzalenost popisku od stredu
	 * @param len_2 - velikost pisma
	 * @param sp - sipka
	 */
	public static void upravPoziciRetezceAVykresli(Graphics2D g2, String rychlost, double f_x, double f_y, Font f, double c_x, double c_y, double ux, double uy, double d, double len_2, Path2D sp){
		//obdelnik obsahujici retezec 
		Rectangle2D rec = new Rectangle2D.Double(f_x, f_y-g2.getFontMetrics(f).getAscent(), g2.getFontMetrics(f).stringWidth(rychlost), g2.getFontMetrics(f).getHeight() ) ;		
		// zkontroluje zda se sipka protina s obdelnikem, paklize ano tak vezme opacny bod aby se retezec neprekryval s sipkou
	    boolean intersects = sp.intersects(rec);
	    //pokud se popisek s rychlosti protina s sipkou nebo je popisek mimo okno vybereme opacny bod 
	    //zvetsime odstup textu o 10% a zmensime velikost fontu o ctvrtinu a opakujeme dokud se protinaji
	    while(intersects || zkontrolujTextMimoOkno(f_x, f_y, g2.getFontMetrics(f).getHeight(), g2.getFontMetrics(f).stringWidth(rychlost)) ) {
	    	//dopocet bodu popisku
	    	f_x = c_x - uy * d;
			f_y = c_y + ux * d;
			//zvetseni zvdalenost popisku od stredu
			d= d*1.1;
			//zmenseni velikosti fontu
			f = new Font( "Calibri", Font.BOLD,(int)((len_2*0.2)*0.75) );
			g2.setFont(f);
			//obdelnik definujici popisek
			rec = new Rectangle2D.Double(f_x, f_y-g2.getFontMetrics(f).getAscent(), g2.getFontMetrics(f).stringWidth(rychlost), g2.getFontMetrics(f).getHeight() ) ;
			//prunik obdelniku a sipky
			intersects = sp.intersects(rec);
			//obratime popisek na druhou stranu sipky
			ux = -ux;
			uy = -uy;
	    }
	    //vykresleni stringu rychlosti
	  	g2.drawString(rychlost, (int)f_x,(int) f_y);
	}
	/**
	 * Metoda dopocita body od kterych zacina a konci potrubi ve vizualizaci v okne. Vyuziva obecnou rovnici primky pro dopocet nezname slozky bodu
	 * @param pxStart - x-ova souradnice pocatecniho bodu trubky
	 * @param pyStart - y-ova souradnice pocatecniho bodu trubky
	 * @param pxEnd - x-ova souradnice koncoveho bodu trubky
	 * @param pyEnd - y-ova souradnice koncoveho bodu trubky
	 * @param size - glyphSize
	 * @param cile - pole urcujici jestli zacatacni a koncove body jsou rezervoary (true je rezervoarem)
	 * @return body - pole obsahujici zacatecni a koncove body potrubi
	 */
	public static double[] nastavKrajniBodyPotrubi(double pxStart, double pyStart, double pxEnd, double pyEnd, double[] size, boolean[] cile){
		double zacatekPotrubiX = 0;
		double zacatekPotrubiY = 0;
		double konecPotrubiX = 0;
		double konecPotrubiY = 0;
		double rozdilX = 0;
		double rozdilY = 0;
		double pripocetStartUzelX = 0;
		double pripocetKonecUzelX = 0;
		double pripocetStartUzelY = 0;
		double pripocetKonecUzelY = 0;
	
		double[] body = new double[4];
		//inicializace velikosti pokud se vykresluji rezervoary s maximem glyphSize
		double velikostStart = (size[0]*scale)/2.0;
		double velikostEnd = (size[1]*scale)/2.0;
		if (scale > 1) {
			velikostStart = size[0]/2.0;
			velikostEnd = size[1]/2.0;
		}
		
		if(cile[0] == true) {//Startovni uzel je rezervoarem tim padem budeme muset dopocita kde zacina potrubi
			pripocetStartUzelX = velikostStart;
			pripocetStartUzelY = velikostStart;
		}else {//je to pouhy uzel
			zacatekPotrubiY = pyStart;
			zacatekPotrubiX = pxStart;
		}
		if(cile[1] == true) {//Koncovy uzel je rezervoarem tim padem budeme muset dopocita kde zacina potrubi
			pripocetKonecUzelX = velikostEnd;
			pripocetKonecUzelY = velikostEnd;
		}else {//je to pouhy uzel
			konecPotrubiY = pyEnd;
			konecPotrubiX = pxEnd;
		}		
		//vypocet obecne rovnice primky kterou budeme potrebovat pro dopocet druhé složky bodu
		//ziskame smerovy vektor 
		double sx = pxEnd - pxStart;
		double sy = pyEnd - pyStart;
		//urcime normalovy vektor
		double nx = sy;
		double ny = -sx;
		//dopocteni c = -ax - by
		double c = -(nx*pxStart) -(ny*pyStart);
		//p: nx*x + ny*y + c = 0
		//abychom rozpoznali ktera strana osa prevazuje a tim padem muzeme rict ze
		//jedna slozka bodu je souradnice prevazujici osy + size/2*scale
		rozdilX = Math.abs(pxEnd - pxStart);
		rozdilY = Math.abs(pyEnd - pyStart); 
		
		//zacatecni uzel je v pravo od koncoveho
		if(pxStart > pxEnd) {
			pripocetStartUzelX = -pripocetStartUzelX;
			if(pyStart <= pyEnd) {//zacatecni uzel je vpravo nad koncovým uzlem nebo ve stejne urovni
				pripocetKonecUzelY = -pripocetKonecUzelY;
				body = dopoctiZacatekAKonecPotrubi(rozdilX, rozdilY, cile, pxEnd, pyEnd, pxStart, pyStart, pripocetStartUzelX, pripocetStartUzelY, pripocetKonecUzelX, pripocetKonecUzelY, ny, c, nx, zacatekPotrubiX, zacatekPotrubiY, konecPotrubiX, konecPotrubiY);
			}else {//zacatecni uzej je vpravo pod koncovým uzlem
				pripocetStartUzelY = -pripocetStartUzelY;
				body = dopoctiZacatekAKonecPotrubi(rozdilX, rozdilY, cile, pxEnd, pyEnd, pxStart, pyStart, pripocetStartUzelX, pripocetStartUzelY, pripocetKonecUzelX, pripocetKonecUzelY, ny, c, nx, zacatekPotrubiX, zacatekPotrubiY, konecPotrubiX, konecPotrubiY);
			}
		}else if(pxStart < pxEnd) {//zacatecni uzel je vlevo od koncoveho
			pripocetKonecUzelX = -pripocetKonecUzelX;
			if(pyStart <= pyEnd) {//zacatecni uzel je vlevo nad koncovým uzlem nebo ve stejne urovni
				pripocetKonecUzelY = -pripocetKonecUzelY;
				body = dopoctiZacatekAKonecPotrubi(rozdilX, rozdilY, cile, pxEnd, pyEnd, pxStart, pyStart, pripocetStartUzelX, pripocetStartUzelY, pripocetKonecUzelX, pripocetKonecUzelY, ny, c, nx, zacatekPotrubiX, zacatekPotrubiY, konecPotrubiX, konecPotrubiY);
			}else {//zacatecni uzej je vlevo pod koncovým uzlem
				pripocetStartUzelY = -pripocetStartUzelY;
				body = dopoctiZacatekAKonecPotrubi(rozdilX, rozdilY, cile, pxEnd, pyEnd, pxStart, pyStart, pripocetStartUzelX, pripocetStartUzelY, pripocetKonecUzelX, pripocetKonecUzelY, ny, c, nx, zacatekPotrubiX, zacatekPotrubiY, konecPotrubiX, konecPotrubiY);
			}
		}else { //zacatecni a koncovi jsou ve stejne urovni
			if(pyStart <= pyEnd) {//zacatecni uzel je nad koncovým uzlem nebo ve stejne urovni
				pripocetKonecUzelX = -pripocetKonecUzelX;
				pripocetKonecUzelY = -pripocetKonecUzelY;
				body = dopoctiZacatekAKonecPotrubi(rozdilX, rozdilY, cile, pxEnd, pyEnd, pxStart, pyStart, pripocetStartUzelX, pripocetStartUzelY, pripocetKonecUzelX, pripocetKonecUzelY, ny, c, nx, zacatekPotrubiX, zacatekPotrubiY, konecPotrubiX, konecPotrubiY);
			}else {//zacatecni uzej je pod koncovým uzlem
				pripocetStartUzelY = -pripocetStartUzelY;
				pripocetKonecUzelX = -pripocetKonecUzelX;
				body = dopoctiZacatekAKonecPotrubi(rozdilX, rozdilY, cile, pxEnd, pyEnd, pxStart, pyStart, pripocetStartUzelX, pripocetStartUzelY, pripocetKonecUzelX, pripocetKonecUzelY, ny, c, nx, zacatekPotrubiX, zacatekPotrubiY, konecPotrubiX, konecPotrubiY);
			}
		}
		return body;
	}
	/**
	 * Metoda zjisti jestli prevazuje x-ova nebo y-ova osa dle, pak zjisti jestli se jedna o rezervoar nebo sitovy bod, pokud rezervoar, tak
	 * dopocte bod a ulozi do pole bodyPotrubi ktere vrati.
	 * @param rozdilX - rozdil v absolutni hodnote mezi x-ovymi slozkami rezervoaru
	 * @param rozdilY - rozdil v absolutni hodnote mezi y-ovymi slozkami rezervoaru
	 * @param cile - boolean[] - identifikuje jestli potrubi je mezi rezervoary (true) nebo uzly (false)
	 * @param pxEnd - x-ova souradnice koncoveho sitoveho uzlu
	 * @param pyEnd - y-ova souradnice koncoveho sitoveho uzlu
	 * @param pxStart -  x-ova souradnice zacatecniho sitoveho uzlu
	 * @param pyStart - y-ova souradnice zacatecniho sitoveho uzlu
	 * @param pripocetStartUzelX - pripocet k start x-ove slozce sitoveho bodu (tim ziskame jednu slozku a nasledne muzeme dopocitat dalsi)
	 * @param pripocetStartUzelY - pripocet k start y-ove slozce sitoveho bodu (tim ziskame jednu slozku a nasledne muzeme dopocitat dalsi)
	 * @param pripocetKonecUzelX - pripocet ke konci x-ove slozce sitoveho bodu (tim ziskame jednu slozku a nasledne muzeme dopocitat dalsi)
	 * @param pripocetKonecUzelY - pripocet ke konci y-ove slozce sitoveho bodu (tim ziskame jednu slozku a nasledne muzeme dopocitat dalsi)
	 * @param ny - y-ova slozka normaloveho vektoru
	 * @param c - konstanta rovnice
	 * @param nx - x-ova slozka normaloveho vektoru
	 * @param zacatekPotrubiX - x-ova slozka zacatku potrubi
	 * @param zacatekPotrubiY - y-ova slozka zacatku potrubi
	 * @param konecPotrubiX - x-ova slozka konce potrubi
	 * @param konecPotrubyY - y-ova slozka konce potrubi
	 * @return bodyPotrubi - double[] pole obsahujici body zacatku a konce potrubi 
	 */
	public static double[] dopoctiZacatekAKonecPotrubi(double rozdilX, double  rozdilY, boolean[] cile, double pxEnd, double pyEnd, double pxStart, double pyStart,
													   double pripocetStartUzelX, double pripocetStartUzelY, double pripocetKonecUzelX, double pripocetKonecUzelY, double ny, double c, double nx,
													   double zacatekPotrubiX, double zacatekPotrubiY, double konecPotrubiX, double konecPotrubyY) {
		double[] bodyPotrubi = new double[4];
		double[] bod = new double[2];
		if(rozdilX < rozdilY){//prevazuje y a tim padem muzeme pricist size/2 * scale
			if(cile[0] == true) {//Startovni uzel je rezervoarem tim padem budeme muset dopocita kde zacina potrubi
				bod = dopoctiBodPomociPrevazujiciSlozky(pyStart, pripocetStartUzelY, true, ny, c, nx);
				//zacatecni bod potrubi
				bodyPotrubi[0] = bod[0];
				bodyPotrubi[1] = bod[1];
			}else {
				bodyPotrubi[0] = zacatekPotrubiX;
				bodyPotrubi[1] = zacatekPotrubiY;
			}
			if(cile[1] == true) {//Koncovy uzel je rezervoarem tim padem budeme muset dopocita kde zacina potrubi
				bod = dopoctiBodPomociPrevazujiciSlozky(pyEnd, pripocetKonecUzelY, true, ny, c, nx);
				//konecny bod potrubi
				bodyPotrubi[2] = bod[0];
				bodyPotrubi[3] = bod[1];
			}else {
				bodyPotrubi[2] = konecPotrubiX;
				bodyPotrubi[3] = konecPotrubyY;
			}
		}else if(rozdilX > rozdilY) {//prevazuje x a tim padem muzeme odecist size/2 * scale
			if(cile[0] == true) {//Startovni uzel je rezervoarem tim padem budeme muset dopocita kde zacina potrubi
				bod = dopoctiBodPomociPrevazujiciSlozky(pxStart, pripocetStartUzelX, false, ny, c, nx);
				//zacatecni bod potrubi
				bodyPotrubi[0] = bod[0];
				bodyPotrubi[1] = bod[1];
			}else {
				bodyPotrubi[0] = zacatekPotrubiX;
				bodyPotrubi[1] = zacatekPotrubiY;
			}
			if(cile[1] == true) {//Koncovy uzel je rezervoarem tim padem budeme muset dopocita kde zacina potrubi
				bod = dopoctiBodPomociPrevazujiciSlozky(pxEnd, pripocetKonecUzelX, false, ny, c, nx);
				//konecny bod potrubi
				bodyPotrubi[2] = bod[0];
				bodyPotrubi[3] = bod[1];
			}else {
				bodyPotrubi[2] = konecPotrubiX;
				bodyPotrubi[3] = konecPotrubyY;
			}
		}else {//prevazuji stejne 
			if(cile[0] == true) {
				bodyPotrubi[0] = pxStart + pripocetStartUzelX;
				bodyPotrubi[1] = pyStart + pripocetStartUzelY;
			}else {
				bodyPotrubi[0] = zacatekPotrubiX;
				bodyPotrubi[1] = zacatekPotrubiY;
			}
			if(cile[1] == true) {
				bodyPotrubi[2] = pxEnd + pripocetKonecUzelX;
				bodyPotrubi[3] = pyEnd + pripocetKonecUzelY;
			}else {
				bodyPotrubi[2] = konecPotrubiX;
				bodyPotrubi[3] = konecPotrubyY;
			}
		}
	return bodyPotrubi;
	}
	/**
	 * Metoda na zaklade osy, ktera prevazuje dopocita prvni slozku bodu potrubi, a nasledne diky rovnici obecne primky dopocte druhou.
	 * To same udela i pro druhy bod. Musi ale platit ze sitovy uzel je rezervoarem.
	 * @param slozkaSitovehoUzlu - y nebo x slozka bodu sitoveho uzlu 
	 * @param pripocet - double - pripocet ke slozce pomoci ktere ziskame bod
	 * @param xovaSlozka - boolean - oznamujici jestli se dopocitava x-ova (true) nebo y-ova (false) slozka
	 * @param ny - y-ova slozka normaloveho vektoru
	 * @param nx - x-ova slozka normaloveho vektoru
	 * @param c - konstanta rovnice
	 * @return bod double[] - vypocitane body, kde prvni je vzdy x-ova slozka bodu
	 */
	public static double[] dopoctiBodPomociPrevazujiciSlozky(double slozkaSitovehoUzlu, double pripocet, boolean xovaSlozka, double ny, double c, double nx){
		double[] bod = new double[2];
		if(xovaSlozka) {//prevazuje x-ova slozka
			bod[1] = slozkaSitovehoUzlu + pripocet;
			//zname jednu slozku vektoru tim padem muzeme dopocist druhou
			bod[0] = (-(ny * bod[1]) -c)/nx;
		}else {//prevazuje y-ova slozka
			bod[0] = slozkaSitovehoUzlu + pripocet;
			//zname jednu slozku vektoru tim padem muzeme dopocist druhou
			bod[1] = (-(nx * bod[0]) -c)/ny;
		}
	return bod;
	}
	/**
	 * Metoda prenastavi glyphSize o predanou hodnotu
	 * @param gl (double) pripocet ke glyphSize
	 */
	public void setGlyphSize(double gl) {
		glyphSize = glyphSize + gl;
		System.out.println(glyphSize);
	}
	/**
	 * Metoda otestuje jestli uzivatel klik na stred potrubi
	 * @param x - x-ova souradnice kliku
	 * @param y - y-ova souradnice kliku
	 * @return (int) - pokud ano tak vrati index potrubi pod kterym se nachazi potrubi v listu stredyPotrubi, jinak vrati -1
	 */
	public int klikUzivatelNaStredPotrubi(double x, double y) {
		for(int i = 0; i < stredyPotrubi.size(); i++) {
			if(stredyPotrubi.get(i).contains(x, y)) {
				return i;
			}
		}
		return -1;
	}
	/**
	 * Metoda otestuje jestli uzivatel klik na rezervoar
	 * @param x - x-ova souradnice kliku
	 * @param y - y-ova souradnice kliku
	 * @return (int) - pokud ano tak vrati index rezervoaru pod kterym se nachazi rezervoar v listu rezervoary, jinak vrati -1
	 */
	public int klikUzivatelNaRezervoar(double x, double y) {
		for(int i = 0; i < rezervoary.size(); i++) {
			if(rezervoary.get(i).contains(x, y)) {
				return i;
			}
		}
		return -1;
	}
	/**
	 * Metoda otestuje jestli uzivatel klik na kohout
	 * @param x - x-ova souradnice kliku
	 * @param y - y-ova souradnice kliku
	 * @return (int) - pokud ano tak vrati index kohoutu pod kterym se nachazi kohout v listu kohouty, jinak vrati -1
	 */
	public int jeUzivatelNaKohoutu(double x, double y) {
		for(int i = 0; i < kohouty.size(); i++) {
			if(kohouty.get(i).contains(x, y)) {
				return i;
			}
		}
		return -1;
	}
	/**
	 * Metoda nastavi potrebne atributy pro nastaveni kohoutu
	 * @param otevrenost - urcuje miru otevrenosti kohoutu
	 * @param indexK - indexKohoutu pod kterym se kohout nachazi v listu kohouty
	 */
	public void setValve (float otevrenost, int indexK) {
		kohoutOtevrenost = otevrenost;
		indexKohoutu = indexK;
	}
	/**
	 * Metoda zkontroluje jestli se text nachazi mimo okno
	 * @param f_x - x-ova souradnice popisku
	 * @param f_y - y-ova souradnice popisku
	 * @param vyskaTextu - reprezentuje vysku textu
	 * @param sirkaTextu - reprezentuje sirku textu
	 * @return true pokud je mimo okno, jinak false
	 */
	public static boolean zkontrolujTextMimoOkno(double f_x, double f_y, double vyskaTextu, double sirkaTextu) {
		//jeslize text je vpravo nebo vlevo mimo okno 
		if( (f_x + sirkaTextu) > (sirkaSite*scale) || (f_x + sirkaTextu) < 0 ) {
			return true;
		}
		//jeslize text je mimo okno v ose y
		if( (f_y + vyskaTextu) > (vyskaSite*scale) || (f_y - vyskaTextu)< 0) {
			return true;
		}
		return false;
	}
	/**
	 * Metoda zjisti jestli se nejaka souradnice rezervoaru nachazi na kraji sveta, 
	 * tim pademe muzmeme pripocet zvetsit 2 krat abychom vyuzili co nejvetsi prostor okna.
	 * @param x - x-ova souradnice rezervoaru (levy roh)
	 * @param y - y-ova souradnice rezervoaru (levy roh)
	 * @param size - velikost rezervoaru
	 * @param pripocetX - pripocet k x-ove souradnici
	 * @param pripocetY - pripocet k y-ove souradnici
	 * @param E - epsilon pro porovnani mezi doubly
	 * @return double[] - kdy prvek na indexu 0 urcuje x a na 1 y
	 */
	public static double[] jeNejakaSouradniceNaKrajiSveta(double x, double y, double size, double pripocetX, double pripocetY, double E) {
		//pokud nejaka souradnice rezervoaru je na kraji sveta, tak abychom vyuzili cele okno musime souradnici posunout o 2 * pripocet
		if(x == 0) {
			pripocetX = 0;
		}
		if(y == 0) {
			pripocetY = 0;
		}
		if(Math.abs( x + (size*scale) - (sirkaSite*scale)) < E) {
			pripocetX = pripocetX*2;
		}
		if( Math.abs(y + (size*scale) - (vyskaSite*scale)) < E) {
			pripocetY = pripocetY*2;
		}
		//vratime pripocty
		return new double[] {pripocetX, pripocetY};
	}
	/**
	 * Metoda vypocte proporcionalni rozmery rezervoaru odvozene od glyphSize tento rozmer ulozi na index 0 dovjrozmerneho pole
	 * pod index urcujici dotycny rezervoar a na dalsi dva nasledujici indexy ulozi posuny pro osu x a y, ktere jsou potreba pokud
	 * maji rezervoary ruznych velikosti stejne x nebo y souradnice
	 * @return double[][] - dvojrozmerne pole obsahujici rozmery rezervoaru a posuny pro vycentrovani stredu, pokud maji rezervoary stejne stredy
	 */
	public static double[][] vypoctiProporcionalniRozmeryRezervoaru(){
		double[][] rezervoaryRozmery = new double[nodes.length][3];
		//v prvním pruchodu najdeme maximum
		double obsahNejvetsihoR = Double.NEGATIVE_INFINITY;
		for(int i = 0; i < nodes.length; i++) {
			if(nodes[i] instanceof Reservoir) {
				Reservoir r = (Reservoir)nodes[i];
				if(r.getCapacity() > obsahNejvetsihoR) {
					obsahNejvetsihoR = r.getCapacity();
				}
			}
		}
		//vypocitame maximalni obsah
		double maxObsah = glyphSize*glyphSize;
		for(int i = 0; i < rezervoaryRozmery.length; i++) {
			if(nodes[i] instanceof Reservoir) {
				Reservoir r = (Reservoir)nodes[i];
				//podelime capacity a tim ziskame meritko
				double proporcionalniRozdil = r.getCapacity()/obsahNejvetsihoR;
				//pomoci meritka ziskame pomerovy obsah z maxObsahu
				double obsahAktualniho = proporcionalniRozdil * maxObsah;
				//z obsahu ziskame stranu a
				double sirka = Math.sqrt(obsahAktualniho);
				//ulozime rozmer pod index rezervoaru
				rezervoaryRozmery[i][0] = sirka;
			}	
		}
		//pro pripad ze se rezervoary ruznych velikosti nachazi ve stejne urovni je treba je vycentorvat
		double posunX = 0;
		double posunY = 0;
		//jdeme pres vsechny rezervoary a zjistime jestli se v siti nachazi rezervoar se stejnymi souradnicem
		//a pokud je rezervoar mensi tak vypocteme posuny pro vycentrovani
		for(int i = 0; i < nodes.length; i++) {
			for(int j = 0; j < nodes.length; j++) {
				//maji stejne x-ove souradnice a kontrolovany rezervoar je mensi nez porovnavany
				if(nodes[i].getX() == nodes[j].getX() && rezervoaryRozmery[i][0] < rezervoaryRozmery[j][0]) {
					//chceme aby porovnavany byl ten nejvetsi podle kterho se budou ostatni posouvat
					if(Math.abs(rezervoaryRozmery[i][0] - rezervoaryRozmery[j][0]) > posunX) {
						//vypocteme rozdil stran
						posunX = Math.abs(rezervoaryRozmery[i][0] - rezervoaryRozmery[j][0]);
						//vydelime 2 
						posunX = posunX/2;
						//ulozime na druhou pozici
						rezervoaryRozmery[i][1] = posunX;
						
					}
				}
				//to same pro y-osu
				if(nodes[i].getY() == nodes[j].getY() && rezervoaryRozmery[i][0] < rezervoaryRozmery[j][0]) {
					if(Math.abs(rezervoaryRozmery[i][0] - rezervoaryRozmery[j][0]) > posunX) {
						posunY = Math.abs(rezervoaryRozmery[i][0] - rezervoaryRozmery[j][0]);
						//cheme posouvat do stredu
						posunY = posunY/2;
						rezervoaryRozmery[i][2] = posunY;
					}
				}
				
			}
			posunX = 0;
			posunY = 0;
		}		
		//vratime rozmeryRezervoaru
		return rezervoaryRozmery;
	}
	/**
	 * Metoda vrati rozmery start a end bodu
	 * @param body - pole obsahujici souradanice start a end
	 * @return - pole rozmeru kdy prvek na 0 je rozmer start a na 1 end
	 */
	public static double[] najdiRozmeryRezervoaru(double[] body) {
		//ziskame si rozmery rezervoaru
		double[][] rozmeryRezervoaru = vypoctiProporcionalniRozmeryRezervoaru();
		double[] rozmery = new double[2];
		//jdeme pres vsechny uzly
		for(int i = 0; i < nodes.length; i++) {
			//pokud jsme nasli start bod vratime jeho rozmer
			if(body[0]-(glyphSize/2) == nodes[i].getX() && body[1]-(glyphSize/2) == nodes[i].getY()) {
				//ulozime rozmer start vrcholu
				rozmery[0] = rozmeryRezervoaru[i][0];
				continue;
			}
			//pokud jsme nasli end bod vratime jeho rozmer
			if(body[2]-(glyphSize/2) == nodes[i].getX() && body[3]-(glyphSize/2) == nodes[i].getY()) {
				//ulozime rozmer end vrcholu
				rozmery[1] = rozmeryRezervoaru[i][0];
				continue;
			}
		}
		return rozmery;
	}
	/**
	 * Metoda vrati posuny pro vycentrovani start a end bodu
	 * @param body - souradnice start a end
	 * @return double[][] - dvojrozmerne pole obsahujic na indexu 0 posuny pro vycentrovani start bodu,
	 * na indexu 1 jsou pak posuvy pro end
	 */
	public static double[][] vypocitejVycentrovaniRezervoaru(double[] body){
		//ziskame si rozmery rezervoaru
		double[][] rozmeryR = vypoctiProporcionalniRozmeryRezervoaru();
		double[][] vycentrovani = new double[2][2];
		for(int i = 0; i < nodes.length; i++) {
			if (nodes[i] instanceof Reservoir) {
				//pokud jsme nasli start bod natavime jeho posuny
				if(nodes[i].getX() == body[0]-(glyphSize/2) && body[1]-(glyphSize/2) == nodes[i].getY()) {
					vycentrovani[0][0] =  rozmeryR[i][1];
					vycentrovani[0][1] = rozmeryR[i][2];
				}
				//pokud jsme nasli end bod natavime jeho posuny
				if(nodes[i].getX() == body[2]-(glyphSize/2) && body[3]-(glyphSize/2) == nodes[i].getY()) {
					vycentrovani[1][0] =  rozmeryR[i][1];
					vycentrovani[1][1] = rozmeryR[i][2];
				}
			}
		}
		return vycentrovani;
	}
	
}
	

