package Destify;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.border.AbstractBorder;

class IconBorder extends AbstractBorder {
    private final static int RADIUS = 20;
    private static final long serialVersionUID = 1L;
    private Color color;

    IconBorder (Color color) {
    	this.color = color;
    }

    @Override
    public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g.setColor(color);
        g.drawRoundRect(x, y, width -1, height -1, RADIUS, RADIUS);
    }
}