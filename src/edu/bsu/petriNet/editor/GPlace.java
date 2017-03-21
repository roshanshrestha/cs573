package edu.bsu.petriNet.editor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Map;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.bsu.petriNet.controller.IController;
import edu.bsu.petriNet.model.AbstractGraphNode;
import edu.bsu.petriNet.model.AbstractPlace;
import edu.bsu.petriNet.model.GraphElement;

public class GPlace implements GElement{
	
	AbstractPlace abstractPlace;
	private final Integer RADIUS = 25;
	
	public GPlace(AbstractPlace p){
		this.abstractPlace = p;
	}
	
	public void draw(Graphics2D g, Map<Integer,GElement> elements){
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(CanvasPanel.LINE_THICKNESS));
		g.drawOval(abstractPlace.getX()-RADIUS, abstractPlace.getY()-RADIUS, 2*RADIUS, 2*RADIUS);
		g.drawString(String.valueOf(abstractPlace.getTokens()), abstractPlace.getX(), abstractPlace.getY());
		g.drawString(""+abstractPlace.getName(), abstractPlace.getX()-RADIUS, abstractPlace.getY()-RADIUS);
	}

	@Override
	public GraphElement getAbstractElement() {
		return abstractPlace;
	}

	@Override
	public Boolean containsPoint(Point p) {
		return p.distance(new Point(abstractPlace.getX(), abstractPlace.getY())) < RADIUS;
	}

	@Override
	public GPoint getExitPoint(Vector vector) {
		Vector v = new Vector(abstractPlace.getX(), abstractPlace.getY());
		return new GPoint(v.add(vector.unit().mul(RADIUS)));
	}

	@Override
	public void editDialog(JFrame frame, IController controller) {
		JDialog dialog = new JDialog(frame,"Click a button", true);
		JTextField nameField = new JTextField(this.abstractPlace.getName());
		nameField.setPreferredSize(new Dimension(100,35));
		JTextField tokensField = new JTextField(this.abstractPlace.getTokens());
		tokensField.setPreferredSize(new Dimension(100,35));
		JPanel contentPane = new JPanel();
		contentPane.add(new JLabel("Name:"));
		contentPane.add(nameField);
		contentPane.add(new JLabel("tokens:"));
		contentPane.add(tokensField);
		dialog.setContentPane(contentPane);
		dialog.pack();
		dialog.setVisible(true);
		abstractPlace.setName(nameField.getText());
		try{
			int t = Integer.valueOf(tokensField.getText());
			abstractPlace.setTokens(t);
		}catch(NumberFormatException e){
			
		}
		controller.setName(abstractPlace);
		controller.setPlaceTokenCount(abstractPlace);
		
		
	}

}