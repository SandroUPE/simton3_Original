package br.upe.jol.experiments;
/*************************************/
/* Krsub.java                        */
/* Copyright (C) 1997, 1998 K. Ikeda */
/*************************************/

import java.applet.Applet;
import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics;

public class Krsub extends Applet implements Runnable {
	Thread	th;
	int	n=0,m=0;
	int	flag = -1;
	Node	v[];
	Edge	e[];
	int	idx[];
	int	num,den;

	public void set(int flag, int n, int m, int num, int den,
			Node v[], Edge e[], int idx[])  {
		this.flag = flag;
		this.n = n;
		this.m = m;
		this.num = num;
		this.den = den;
		this.v = v;
		this.e = e;
		this.idx = idx;
		setBackground(Color.white);
	}

	public void paintNode(Graphics g, Node n, FontMetrics fm,
				int x, int y, int rl) {
		String s;
		int w = fm.stringWidth(n.name) + 10;
		int h = fm.getHeight() + 4;
		n.w = w;
		n.h = h;

		x += rl*w;
		y -= h/2;

		g.drawRect(x,y,w,h);
		g.drawString(n.name,x+5,y+2+fm.getAscent());
	}

	public void paintEdge(Graphics g, Edge e, FontMetrics fm, int i) {
		Node v1 = v[e.rndd_plus];
		Node v2 = v[e.rndd_minus];
		Color		c;
		int	x0 = 80;
		int	x1 = x0+(e.len*num)/den;
		int	y = (i+1) * (fm.getHeight() + 10);

                if (e.select == -1 )
                        g.setColor(Color.black);
                else if (e.select == -2)
                        g.setColor(Color.lightGray);
                else if (e.select == 1)
                        g.setColor(Color.orange);
                else
                        g.setColor(Color.red);
		g.drawLine(x0,y,x1,y);
		paintNode(g, v1, fm, x0,y,-1);
		paintNode(g, v2, fm, x1,y,0);
	}

	public void paint(Graphics g) {
		FontMetrics	fm = g.getFontMetrics();
		for (int i=0; i<m; i++)
			paintEdge(g,e[idx[i]],fm,i);
	}

	public void update(Graphics g) {
		paint(g);
	}

	public void start() {
		if (th==null) {
			th = new Thread(this);
			th.start();
		}
	}

	public void run() {
		while (true) {
			try {
				th.sleep(100);
			}
			catch (InterruptedException e) {}
			if (flag-- > 0)
				repaint();
		}
	}
}
