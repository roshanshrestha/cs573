package edu.bsu.petriNet.controller;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;

import edu.bsu.petriNet.editor.BasicGraphEditorPanel;
import edu.bsu.petriNet.helper.BranchedHistoryProvider;
import edu.bsu.petriNet.helper.HistoryProvider;
import edu.bsu.petriNet.model.AbstractArc;
import edu.bsu.petriNet.model.AbstractGraphNode;
import edu.bsu.petriNet.model.AbstractPlace;
import edu.bsu.petriNet.model.AbstractTransition;
import edu.bsu.petriNet.model.GraphNode;
import edu.bsu.petriNet.model.PetriNet;
import edu.bsu.petriNet.model.XmlInputOutput;

public class BaseController implements IController {
	private ChangeDispatch dispatch;
	private PetriNet petrinet;
	private Random random;
	private BranchedHistoryProvider history;
	private boolean inUndoBlock;

	public BaseController(){
		this.dispatch = new ChangeDispatch();
		(new Thread(this.dispatch)).start();
		this.petrinet = new PetriNet();
		this.history = new BranchedHistoryProvider();
		this.random = new Random();
		this.newNet();
	}

	@Override
	public Boolean newNet() {
		synchronized(this.petrinet){
			this.petrinet = new PetriNet();
			this.history.reset();
			this.dispatch.notifyStateListeners();
		}
		return true;
	}
	
	public void beginUndoBlock() throws IllegalStateException {
		if (inUndoBlock) {
			throw new IllegalStateException("Already in undo block");
		} else {
			inUndoBlock = true;
		}
	}
	
	public void endUndoBlock() throws IllegalStateException {
		if (inUndoBlock) {
			inUndoBlock = false;
			dispatch.notifyStateListeners();
		} else {
			throw new IllegalStateException("Not in undo block");
		}
	}

	@Override
	public Integer addTransition(AbstractTransition t) {
		synchronized(this.petrinet){
			Integer id = petrinet.createTransition(t.getName(),t.getX(),t.getY());
			if (!inUndoBlock) this.dispatch.notifyStateListeners();
			return id;
		}
	}

	@Override
	public Integer addPlace(AbstractPlace p) {
		synchronized(this.petrinet){
			Integer id = petrinet.createPlace(p.getName(), p.getTokens(),p.getX(),p.getY());
			if (!inUndoBlock) this.dispatch.notifyStateListeners();
			return id;
		}
	}

	@Override
	public Integer addArc(AbstractArc a) {
		synchronized(this.petrinet){
			Integer id = petrinet.createArc(a.getName(), a.getOrigin(), a.getTarget(), a.getWeight());
			if (!inUndoBlock) this.dispatch.notifyStateListeners();
			return id;
		}
	}

	@Override
	public Boolean delete(Integer ID) {
		synchronized(this.petrinet){
			petrinet.delete(ID);
			if (!inUndoBlock) this.dispatch.notifyStateListeners();
		}
		return null;
	}

	@Override
	public Boolean setArcWeight(AbstractArc a, Boolean notify) {
		synchronized(this.petrinet){
			Boolean r = petrinet.setArcWeight(a.getID(), a.getWeight());
			if(notify && !inUndoBlock){
				this.dispatch.notifyStateListeners();
			}
			return r;
		}
	}

	@Override
	public Boolean setPlaceTokenCount(AbstractPlace p, Boolean notify) {
		synchronized(this.petrinet){
			Boolean r = petrinet.setPlaceTokenNumber(p.getID(), p.getTokens());
			if(notify && !inUndoBlock){
				this.dispatch.notifyStateListeners();
			}
			return r;
		}
	}

	@Override
	public Boolean setName(AbstractGraphNode n, Boolean notify) {
		synchronized(this.petrinet){
			petrinet.setName(n.getID(), n.getName());
			if(notify && !inUndoBlock){
				this.dispatch.notifyStateListeners();
			}
		}
		return null;
	}


	@Override
	public Boolean registerStateListener(IStateListener l) {
		this.dispatch.registerStateListener(l);
		return true;
	}

	@Override
	public Boolean setLocation(AbstractGraphNode n, Boolean notify) {
		synchronized(this.petrinet){
			petrinet.setPosition(n.getID(), n.getX(), n.getY());
			if(notify && !inUndoBlock){
				this.dispatch.notifyStateListeners();
			}
		}
		return null;
	}
	
	public Boolean translate(Integer id, Integer dx, Integer dy, Boolean notify) {
		synchronized(this.petrinet){
			GraphNode node = petrinet.getGraphNodeById(id);
			if (node != null) {
				petrinet.setPosition(id, node.getX()+dx, node.getY()+dy);
				if(notify && !inUndoBlock){
					this.dispatch.notifyStateListeners();
				}
			}
		}
		return null;
	}

	@Override
	public Boolean save(String filename) {
		synchronized(this.petrinet){
			return XmlInputOutput.printModel(petrinet, filename);
		}
	}

	@Override
	public Boolean load(String filename) {
		synchronized(this.petrinet){
			petrinet = XmlInputOutput.readModel(filename);
			this.dispatch.notifyStateListeners();
		}
		return null;
	}

	@Override
	public Boolean fire(AbstractTransition t) {
		synchronized(this.petrinet){
			Boolean r = petrinet.fire(t.getID());
			if (!inUndoBlock) this.dispatch.notifyStateListeners(true, false);
			return r;
		}
	}

	@Override
	public Boolean simulate(final int n_steps, final int delay_ms) {
		new Thread(){
			public void run(){
				synchronized(BaseController.this.petrinet){
					for(int i = 0; i < n_steps; i++){
						//Get available firable transitions.
						HashMap<Integer,AbstractTransition> firables = new HashMap<>();
						int k = 0;
						for(AbstractTransition t :BaseController.this.petrinet.getAbstractTransitions()){
							if(t.isFirable()){
								firables.put(k++, t);
							}
						}
						//Fire one.
						if(firables.keySet().size() >0){
							int target = BaseController.this.random.nextInt(firables.keySet().size());
							BaseController.this.fire(firables.get(target));
						}
						try {
							Thread.sleep(delay_ms);
						} catch (InterruptedException e) {}
					}
				}
			}
		}.start();
		return true;
	}

	@Override
	public Boolean undo() {
		if(this.history.isUndoPossible()){
			this.history.undo();
			synchronized(this.petrinet){
				this.petrinet = this.history.getCurrentPetriNet();
				this.dispatch.notifyStateListeners(true, true);
			}
			return true;
		}
		return false;
	}
	
	@Override
	public Boolean redo() {
		if(this.history.isRedoPossible()){
			this.history.redo();
			synchronized(this.petrinet){
				this.petrinet = this.history.getCurrentPetriNet();
				this.dispatch.notifyStateListeners(true, true);
			}
			return true;
		}
		return false;
	}
	
	@Override
	public Boolean undoSimulation() {
		this.history.undoBranch();
		synchronized(this.petrinet){
			this.petrinet = this.history.getCurrentPetriNet();
			this.dispatch.notifyStateListeners(false, true);
		}
		return true;
	}
	
	private class ChangeDispatch implements Runnable{
		private ArrayList<IStateListener> stateListeners;
		private LinkedBlockingQueue<PetriNet> queue;
		
		public ChangeDispatch(){
			this.stateListeners = new ArrayList<>();
			this.queue = new LinkedBlockingQueue<>();
		}
		
		public void registerStateListener(IStateListener l){
			this.stateListeners.add(l);
		}
		
		private void notifyStateListeners(){
			notifyStateListeners(false, false);
		}
		
		private void notifyStateListeners(Boolean isSimulation, Boolean isUndoChange){
			// We have two HistoryProviders.  One for M0 changes, One for Simulation changes.
			// For every M0 change we can clear the Simulation change history
			if(!isUndoChange){
				history.checkPoint(petrinet, isSimulation);
			}
			this.queue.add(petrinet.getDeepCopy());
		}
		
		public void run(){
			while(true){
				if(!queue.isEmpty()){
					PetriNet net = queue.poll();
					for(IStateListener l : this.stateListeners){
						StateSet newstate = new StateSet();
						for(AbstractArc arc: net.getAbstractArcs()){
							newstate.addArc(arc);
						}
						for(AbstractTransition trans: net.getAbstractTransitions()){
							newstate.addTransition(trans);
						}
						for(AbstractPlace place: net.getAbstractPlaces()){
							newstate.addPlace(place);
						}
						l.newState(newstate);
					}
				}
			}
		}
	}
	
}
