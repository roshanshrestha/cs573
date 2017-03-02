package edu.boisestate.cs573.spring2017.pretrinet.model;

import java.util.HashMap;


import edu.boisestate.cs573.spring2017.helpers.IdGenerator;

public class PetriNet
{

	
	private HashMap<Integer,Arc> arcs;
	private HashMap<Integer,Place> places;
	private HashMap<Integer,Transition> transitions;
	
	public PetriNet()
	{
		arcs = new HashMap<>();
		places = new HashMap<>();
		transitions = new HashMap<>();
	}
	
	protected Boolean createPlace(int id, String name, int tokenN){
		
		Place p = new Place(id, name, tokenN);
		places.put(id,p);
		return true;
	}
		
	public Boolean createPlace(String name, int tokenN){
		
		return this.createPlace(IdGenerator.getUniqueIdentifier(), name, tokenN);
	}
	
	protected Boolean createTransition(int id, String name){
		
		Transition t = new Transition(id, name);
		transitions.put(id,t);
		return true;
	}
	
    public Boolean createTransition(String name){
		
		return this.createTransition(IdGenerator.getUniqueIdentifier(), name);
	}
	
    protected Boolean createArc(int id, String name, int sourceId, int targeId, int weight){
		
    	GraphNode source = getGraphNodeById(sourceId);
    	GraphNode target = getGraphNodeById(targeId);
    	
    	if(source != null && target != null){
    		Arc a = new Arc(id, name, source, target);
    		
    		source.addArc(a);
    		target.addArc(a);
    		arcs.put(id,a);
    		
    		return true;
    	}
		return false;
	}
	
    public Boolean createArc(String name, int sourceId, int targeId, int weight){
		
		return this.createArc(IdGenerator.getUniqueIdentifier(), name, sourceId, targeId, weight);
	}
    
   /* private boolean deletePlace(int id)
    {
    	return true;
    }
    
    private boolean deletePlace(int id)
    {
    	return true;
    }
    
    private boolean deletePlace(int id)
    {
    	return true;
    }
    */
    
    public boolean setArcWeight(int id, int weight)
    {
    	//TODO
    	return true;
    }
    
    public boolean setPlaceTokenNumber(int id, int tokenNumber)
    {
    	//TODO
    	return true;
    }
    public boolean setName(int id , String name){
    	//TODO
    	return true;
    }
    
    public boolean setPosition(int id, int x, int y)
    {
    	//TODO
    	return true;
    }
    
    public boolean fire(int transitionId)
    {
    	//TODO
    	return true;
    }
    
    
    public boolean delete(int id)
    {
    	//TODO
    	return true;
    }
    
	public PetriNet getDeepCopy()
	{
		PetriNet newNet = this;
		//TODO
		return newNet;
	}
	public HashMap<Integer,Arc> getArcs()
	{
		return this.arcs;
	}
	
	public HashMap<Integer,Place> getPlaces()
	{
		return this.places;
	}
	
	
	public HashMap<Integer,Transition> getTransitions()
	{
		return this.transitions;
	}
	
	public HashMap<Integer,Transition> getAbstractTransitions()
	{
		//TODO
		return this.transitions;
	}
	
	
	public HashMap<Integer,Transition> getAbstractArcs()
	{
		//TODO
		return this.transitions;
	}
	
	public HashMap<Integer,Transition> getAbstractPlaces()
	{
		//TODO
		return this.transitions;
	}
	
	
	
	public Arc getArcById(int id){	
		return this.arcs.get(id);
	}
	
	public Transition getTransitionById(int id){	
		return this.transitions.get(id);
	}
	
	public Place getPlaceById(int id){	
		return this.places.get(id);
	}
	
	public GraphNode getGraphNodeById(int id){
		
		GraphNode g = null;
		
		g = getTransitionById(id);
		
		if(g != null)
			return g;
		else{
			return getPlaceById(id);
		}
	}
	
	public void updateIdGenerator(){	
		int maxID = 0;
		
		for(Integer id: this.arcs.keySet()){		
			maxID = Math.max(id, maxID);
		}
		
		for(Integer id: this.places.keySet()){		
			maxID = Math.max(id, maxID);
		}
		
		for(Integer id: this.transitions.keySet()){		
			maxID = Math.max(id, maxID);
		}
		
		IdGenerator.updateId(maxID);
	}
}
