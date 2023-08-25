package br.upe.jol.experiments;
import java.applet.Applet;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.net.URL;

class Node {
	int	x;
	int	y;
	int	set;
	int	first;
	int	next;
	int	w;
	int	h;
	String	name;
}

class Edge {
	int	rndd_plus;	/* initial vertex of this edge */
	int	rndd_minus;	/* terminal vertex of this edge */
	int	len;		/* length */
	int	select;
	String	name;
}

public class Kruskal extends Applet implements MouseListener {
	int	n,m;
	int	num,den;
	int	u, usel, step;
	Node	v[] = new Node[100];
	Edge	e[] = new Edge[200];
	int	idx[] = new int[200];

	int findNode(String name) {
		for (int i=0; i<n; i++)
			if (v[i].name.equals(name))
				return i;
		return -1;
	}

	void input_graph(InputStream is) throws IOException {
		int x,y,l;
		String	s;

		Reader r = new BufferedReader(new InputStreamReader(is));
		StreamTokenizer st = new StreamTokenizer(r);
		st.commentChar('#');
		st.nextToken();	n = (int)st.nval;
		st.nextToken();	m = (int)st.nval;
		st.nextToken();	s = st.sval;
		for (int i = 0; i<n; i++) {
			Node node = new Node();
			st.nextToken();	node.name = st.sval;
			st.nextToken();	node.x = (int)st.nval;
			st.nextToken();	node.y = (int)st.nval;
			v[i] = node;
		}
		for (int i = 0; i<m; i++) {
			Edge edge = new Edge();
			st.nextToken();	edge.name = st.sval;
			switch (st.nextToken()) {
			case StreamTokenizer.TT_NUMBER:
				edge.rndd_plus = (int)st.nval;
				break;
			case StreamTokenizer.TT_WORD:
				edge.rndd_plus = findNode(st.sval);
				break;
			default:
				break;
			}
			switch (st.nextToken()) {
			case StreamTokenizer.TT_NUMBER:
				edge.rndd_minus = (int)st.nval;
				break;
			case StreamTokenizer.TT_WORD:
				edge.rndd_minus = findNode(st.sval);
				break;
			default:
				break;
			}
			st.nextToken();	edge.len = (int)st.nval;
			e[i] = edge;
		}
		for (int i=0; i<m; i++)
			e[i].select = -1;
		den = 0;
		num = 128;
		for (int i=0; i<m; i++)
			if (e[i].len>den)
				den = e[i].len;
		step1();
		Krsub p = (Krsub)getAppletContext().getApplet("krsub");
		if (p!=null)
			p.set(1,n,m,num,den,v,e,idx);
		step = 2;
	}

	void swap(int i, int j) {
		int	k = idx[i];

		idx[i] = idx[j];
		idx[j] = k;
	}

	int partition(int left, int right) {
		int	pivot = e[idx[(int)((left+right)/2)]].len;

		while (left<=right) {
			while (e[idx[left]].len < pivot)
				left++;
			while (e[idx[right]].len > pivot)
				right--;
			if (left <= right)
				swap(left++,right--);
		}
		return left;
	}

	void qsort(int left, int right) {
		int	i;

		if (left >= right)
			return;
		i = partition(left,right);
		qsort(left,i-1);
		qsort(i,right);
	}

	void step1() {		/* initialize */
		for (int i=0; i<m; i++)
			idx[i] = i;
		for (int i=0; i<m; i++)
			e[i].select = -1;
		qsort(0,m-1);
		for (int i=0; i<m; i++)
			e[i].select = -1;
		for (int i=0; i<n; i++) {
			v[i].set = i;
			v[i].first = i;
			v[i].next = -1;
		}
		usel = u = 0;
	}

	void step2() {		/* select the shortest edge */
		e[idx[u]].select = 1; /* pick up the edge */
	}

	void step3() {		/* check the loop */
		int	vl = e[idx[u]].rndd_plus;
		int	vr = e[idx[u]].rndd_minus;
		int	i,j,k;

		if (v[vl].set == v[vr].set) {
			e[idx[u++]].select = -2; /* de-select the edge */
			return;
		}
		usel ++;
		e[idx[u++]].select = 2; /* select the edge */
		for (i = vl; v[i].next>=0; i = v[i].next)
			;
		v[i].next = v[vr].first;
		j = v[vl].first;
		k = v[vl].set;
		for (i = v[vr].first; i>=0; i = v[i].next) {
			v[i].first = j;
			v[i].set = k;
		}
	}

	void step4() {
		for (; u<m; u++)
			e[idx[u]].select = -2;
	}

	public void init() {
		String mdname = getParameter("inputfile");
		try {
			InputStream is;

			is = new URL(getDocumentBase(),mdname).openStream();
			input_graph(is);
			try {
				if (is != null)
					is.close();
				} catch(Exception e) {
			}
		} catch (FileNotFoundException e) {
			System.err.println("File not found.");
		} catch (IOException e) {
			System.err.println("Cannot access file.");
		}
		setBackground(Color.white);
		addMouseListener(this);
	}

	public void paintNode(Graphics g, Node n, FontMetrics fm) {
		String s;
		int x = n.x;
		int y = n.y;
		int w = fm.stringWidth(n.name) + 10;
		int h = fm.getHeight() + 4;
		n.w = w;
		n.h = h;

		g.setColor(Color.black);
		g.drawRect(x-w/2,y-h/2,w,h);

		g.setColor(getBackground());
		g.fillRect(x-w/2+1,y-h/2+1,w-1,h-1);

		g.setColor(Color.black);
		g.drawString(n.name,x-(w-10)/2,(y-(h-4)/2)+fm.getAscent());
	}

	int [] xy(int a, int b, int w, int h) {
		int	x[] = new int[2];

		if (Math.abs(w*b)>=Math.abs(h*a)) {
			x[0] = ((b>=0)?1:-1)*a*h/b/2;
			x[1] = ((b>=0)?1:-1)*h/2;
		} else {
			x[0] = ((a>=0)?1:-1)*w/2;
			x[1] = ((a>=0)?1:-1)*b*w/a/2;
		}
		return x;
	}

	void drawEdge(Graphics g,int x1,int y1,int x2,int y2) {
		g.drawLine(x1,y1,x2,y2);
	}

	public void paintEdge(Graphics g, Edge e, FontMetrics fm) {
		Node v1 = v[e.rndd_plus];
		Node v2 = v[e.rndd_minus];

		int a = v1.x-v2.x;
		int b = v1.y-v2.y;

		int x1[] = xy(-a,-b,v1.w,v1.h);
		int x2[] = xy(a,b,v2.w,v2.h);

		if (e.select == -1 )
			g.setColor(Color.black);
		else if (e.select == -2)
			g.setColor(Color.lightGray);
		else if (e.select == 1)
			g.setColor(Color.orange);
		else
			g.setColor(Color.red);
		drawEdge(g,v1.x+x1[0],v1.y+x1[1],v2.x+x2[0],v2.y+x2[1]);

		int w = fm.stringWidth("" + e.len);
		int h = fm.getHeight();

		g.setColor(getBackground());
		g.fillRect((v1.x+v2.x-w)/2,(v1.y+v2.y-h)/2,w,h);

		if (e.select == -1 )
			g.setColor(Color.black);
		else if (e.select == -2)
			g.setColor(Color.lightGray);
		else if (e.select == 1)
			g.setColor(Color.orange);
		else
			g.setColor(Color.red);
		g.drawString("" + e.len,(v1.x+v2.x-w)/2,(v1.y+v2.y-h)/2+fm.getAscent());
	}

	public void paint(Graphics g) {
		FontMetrics fm = g.getFontMetrics();
		for (int i=0; i<n; i++)
			paintNode(g,v[i],fm);
		for (int i=0; i<m; i++)
			paintEdge(g,e[i],fm);
		Krsub p = (Krsub)getAppletContext().getApplet("krsub");
		if (p!=null)
			p.set(1,n,m,num,den,v,e,idx);
	}

	public void update(Graphics g) {
		paint(g);
	}

	public void mousePressed(MouseEvent e) {
		if (step == 1) {
			step1();
			step = 2;
		} else if (usel >= n-1) {
			step4();
			step = 1;
		} else {
			if (step == 3) {
				step3();
				step = 2;
			} else {
				step2();
				step = 3;
			}
		}
		repaint();
	}
	public void mouseClicked(MouseEvent event) {}
	public void mouseReleased(MouseEvent event) {}
	public void mouseEntered(MouseEvent event) {}
	public void mouseExited(MouseEvent event) {}
}