package edu.bsu.petriNet.editor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import edu.bsu.petriNet.controller.IController;
import edu.bsu.petriNet.model.AbstractPlace;
import edu.bsu.petriNet.model.AbstractTransition;
import edu.bsu.petriNet.model.GraphElement;
import edu.bsu.petriNet.util.PropertiesLoader;

public class GTransition implements GElement {

	private AbstractTransition abstractTransition;
	private int W = 25;
	private int H = 50;
	
	private final int SELECTION_PADDING = 4;
	
	public GTransition(AbstractTransition p){
		this.abstractTransition = p;
	}
	
	public void draw(Graphics2D g, Map<Integer,GElement> elements, ElementSelection selection){
		if(this.abstractTransition.isFirable()){
			g.setColor(Color.RED);
			g.fillRect(abstractTransition.getX()-W/2, abstractTransition.getY()-H/2, W, H);
		}
		g.setColor(Color.BLACK);
		g.setStroke(new BasicStroke(CanvasPanel.LINE_THICKNESS));
		g.drawRect(abstractTransition.getX()-W/2, abstractTransition.getY()-H/2, W, H);
		g.drawString(""+abstractTransition.getName(), abstractTransition.getX()-W/2-abstractTransition.getName().length()/2*5, abstractTransition.getY()-H/2-12);
		
		// draw selection indicator
		if (selection.contains(this)) {
			g.setStroke(ElementSelection.SELECTION_STROKE);
			g.drawRect(abstractTransition.getX()-W/2-SELECTION_PADDING,
					abstractTransition.getY()-H/2-SELECTION_PADDING,
					W+SELECTION_PADDING*2, H+SELECTION_PADDING*2);
		}
	}

	@Override
	public GraphElement getAbstractElement() {
		return abstractTransition;
	}

	@Override
	public Boolean containsPoint(Point p) {
		return p.distance(new Point(abstractTransition.getX(), abstractTransition.getY())) < 10;
	}
	
	public Boolean withinRectangle(int startX, int startY, int endX, int endY) {
		int x = abstractTransition.getX();
		int y = abstractTransition.getY();
		return x >= startX && y >= startY && x <= endX && y <= endY;
	}
	
	public Point getUpperLeftVisualCorner() {
		return new Point(abstractTransition.getX()-W/2,abstractTransition.getY()-H/2);
	}
	
	public Boolean isArc() {
		return false;
	}
	public Boolean isPlace() {
		return false;
	}
	public Boolean isTransition() {
		return true;
	}

	public GraphElement getAbstractCopy(int translateX, int translateY) {
		return new AbstractTransition(abstractTransition.getX()+translateX,abstractTransition.getY()+translateY,abstractTransition.getName());
	}
	
	@Override
	public GPoint getExitPoint(Vector vector) {
		Vector v = new Vector(abstractTransition.getX(), abstractTransition.getY());
		Double angle = vector.angle();
		//Right
		if(angle < Math.atan2(H/2.0, W/2.0) && angle > Math.atan2(-H/2.0, W/2.0)){
			return Geometry.intersection(v, vector, v.add(new Vector(W/2.0, -H/2.0)), new Vector(0,1));
		}
		//Top
		if(angle < Math.atan2(H/2.0, -W/2.0) && angle > Math.atan2(H/2.0, W/2.0)){
			return Geometry.intersection(v, vector, v.add(new Vector(-W/2.0, H/2.0)), new Vector(1,0));
		}
		//Bottom
		if(angle < Math.atan2(-H/2.0, W/2.0) && angle > Math.atan2(-H/2.0, -W/2.0)){
			return Geometry.intersection(v, vector, v.add(new Vector(-W/2.0, -H/2.0)), new Vector(1,0));
		}
		//Left
		return Geometry.intersection(v, vector, v.add(new Vector(-W/2.0, -H/2.0)), new Vector(0,1));
	}

	@Override
	public void editDialog(JFrame frame, final IController controller,MouseEvent ev) {
		final JDialog dialog = new JDialog(frame,"Click a button", true);
		dialog.setLocation(ev.getX(), ev.getY()+Integer.parseInt(PropertiesLoader.getProperties("config").getProperty("rcMenuTopPadding")));
		JTextField nameField = new JTextField(this.abstractTransition.getName());
		nameField.setPreferredSize(new Dimension(100,35));
		JPanel contentPane = new JPanel();
		contentPane.add(new JLabel("Name:"));
		contentPane.add(nameField);
		JButton deleteButton = new JButton("Delete");
		deleteButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent arg0) {
				controller.delete(abstractTransition.getID());
				dialog.dispose();
			}
		});
		contentPane.add(deleteButton);
		dialog.setContentPane(contentPane);
		dialog.pack();
		dialog.setVisible(true);
		abstractTransition.setName(nameField.getText());
		controller.setName(abstractTransition, true);
		
	}
	
}
