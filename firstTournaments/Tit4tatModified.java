package play;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import gametree.GameNode;
import gametree.GameNodeDoesNotExistException;
import play.exception.InvalidStrategyException;

public class Tit4tatModified  extends Strategy {

	@Override
	public void execute() throws InterruptedException {
		while(!this.isTreeKnown()) {
			System.err.println("Waiting for game tree to become available.");
			Thread.sleep(1000);
		}
		GameNode finalP1 = null;
		GameNode finalP2 = null;
		Set<String> lastMoves = new HashSet<String>();
		int round = 1;
		while(true) {
			PlayStrategy myStrategy = this.getStrategyRequest();
			if(myStrategy == null) //Game was terminated by an outside event
				break;	
			boolean playComplete = false;
						
			while(! playComplete ) {
				lastMoves.clear();
				if(myStrategy.getFinalP1Node() != -1) {
					finalP1 = this.tree.getNodeByIndex(myStrategy.getFinalP1Node());
					GameNode fatherP1 = null;
					if(finalP1 != null) {
						try {
							fatherP1 = finalP1.getAncestor();
						} catch (GameNodeDoesNotExistException e) {
							e.printStackTrace();
						}
						lastMoves.add(finalP1.getLabel());
					}
				}			
				if(myStrategy.getFinalP2Node() != -1) {
					finalP2 = this.tree.getNodeByIndex(myStrategy.getFinalP2Node());
					GameNode fatherP2 = null;
					if(finalP2 != null) {
						try {
							fatherP2 = finalP2.getAncestor();
						} catch (GameNodeDoesNotExistException e) {
							e.printStackTrace();
						}
						lastMoves.add(fatherP2.getLabel());
					}
				}	
				if(finalP1 == null || finalP2 == null) {			
					double[] moves = {new Double(1),new Double(0),new Double(1),new Double(0)};
					applyStrategy(myStrategy, moves);
				}else {	
					if (round == 100) {
						double[] moves = {new Double(0),new Double(1),new Double(0),new Double(1)};
						applyStrategy(myStrategy, moves);
					} else {
						Iterator<String> moves = myStrategy.keyIterator();
						while(moves.hasNext()) {
							String k = moves.next();
							if(lastMoves.contains(k)) {
								myStrategy.put(k, new Double(1));
								System.err.println("Setting " + k + " to prob 1.0");
							} else {
								myStrategy.put(k, new Double(0));
								System.err.println("Setting " + k + " to prob 0.0");
							}						
						}
					}
				}							
				try{
					this.provideStrategy(myStrategy);
					playComplete = true;
					round++;
				} catch (InvalidStrategyException e) {
					System.err.println("Invalid strategy: " + e.getMessage());;
					e.printStackTrace(System.err);
				} 
			}
		}
		
	}
	
	public void applyStrategy(PlayStrategy myStrategy, double[] moves) {
		Iterator<String> keys = myStrategy.keyIterator();
		for(int i = 0; i < moves.length; i++) {
			if(!keys.hasNext()) {
				System.err.println("PANIC: Strategy structure does not match the game.");
				return;
			}
			myStrategy.put(keys.next(), moves[i]);
		}
	}
}
