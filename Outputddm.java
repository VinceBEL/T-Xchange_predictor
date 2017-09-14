package main;

import java.io.IOException;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.LoginException;

import org.json.JSONException;
import org.json.JSONObject;

import smile.Network;

import net.ser1.stomp.*;





public class Outputddm {

	/**
	 * 
	 * @param net
	 * @param nodename
	 * @return the most likely number of items read on next dilemma
	 */
	public static int getmaxstate(Network net,String nodename){
		double[] Probs=net.getNodeValue(nodename);
		
		double probmax=Probs[0];
		int indexprobmax=0;
		for(int i=1;i<Probs.length;i++){
			if(Probs[i]>probmax){
				probmax=Probs[i];
				indexprobmax=i;
			}
		}
		return indexprobmax;
	}
	
	/**
	 * 
	 * @param net
	 * @param nodename
	 * @return the probability (in %) of next prediction to be true 
	 */
	public static int getprobmaxstate(Network net, String nodename){
		double[] Probs=net.getNodeValue(nodename);
		
		double probmax=Probs[0];
		for(int i=1;i<Probs.length;i++){
			if(Probs[i]>probmax){
				probmax=Probs[i];
			}
		}
		return (int)(probmax*100);
	}
	
	/**
	 * 
	 * @param i
	 * @return the string/JSON message changing "prediction" value by i in DDM
	 */
	public static String messageprediction(int i){
		return "{\n"+
            	"	\"busMessageType\": \"changeIndicatorValue\",\n"+
            	"	\"indicatorRef\": \"prediction\",\n"+
            	"	\"value\": "+i+",\n"+
            		"	\"operator\": \"set\"\n"+
            	"}";
	}
	
	/**
	 * 
	 * @return the string/JSON message changing "prediction_change" value to 1
	 */
	public static String messagepredictionchange(){
		return "{\n"+
            	"	\"busMessageType\": \"changeIndicatorValue\",\n"+
            	"	\"indicatorRef\": \"prediction_change\",\n"+
            	"	\"value\": "+"1"+",\n"+
            		"	\"operator\": \"set\"\n"+
            	"}";
	}
	
	/**
	 * 
	 * @param net
	 * @return String containing predictions for all the dilemmas (including stated ones)
	 */
	public static String allpredictions(Network net){
		String res="";
		for(int i=1;i<9;i++){
			String currentnodename="V_IIRead_Dilemma"+i;
			res+=getmaxstate(net,currentnodename)+ " , ";
		}
		return res;
	}
	
	/**
	 * 
	 * @param net
	 * @return String containing the highest probabilities for all dilemmas (including stated ones, at 100)
	 */
	public static String allprobmax(Network net){
		String res="";
		for(int i=1;i<9;i++){
			String currentnodename="V_IIRead_Dilemma"+i;
			res+=getprobmaxstate(net,currentnodename)+ " , ";
		}
		return res;
	}
	
	/**
	 * @param args
	 * @throws IOException 
	 * @throws LoginException 
	 */
public static void main(String[] args) throws LoginException, IOException, JSONException{
		
		String user   = "admin";
        String pass   = "password";
        //String broker = "tcp://0.0.0.0:61613";
        String topic  = "/topic/itemread";
        
        final Client c = new Client( "localhost", 61613, user, pass );
        
        
        final List<Integer> list = new ArrayList<>();
        int nombre=1;
        list.add(nombre);
        
        //loading the network to work on
        final Network net = new Network();
		net.readFile("C:/Users/Belloeilvja/genie/NaziBBQ_v2_AdvisesNumberBayesian.xdsl");
		net.updateBeliefs();
		
        //setting up the subscriber and the Listener
        c.subscribe( topic, new Listener() {
            public void message( Map header, String body ) {
            	
				JSONObject jsonObj;
				
				try {
					
					//receive and parse the ddm json
					jsonObj = new JSONObject(body);
					int input=jsonObj.getInt("value");
					System.out.println("received : " + input);
					
					//update beliefs with input
					String state="State"+input;
					String toevidence="V_IIRead_Dilemma" + list.get(0);
					net.setEvidence(toevidence, state);
					net.updateBeliefs();
					
					//reading next dilemma prediction properties
					String currentnodename="V_IIRead_Dilemma"+(list.get(0)+1);
					int indexprobmax=getmaxstate(net,currentnodename);
					int probmaxint=getprobmaxstate(net,currentnodename);
					
					System.out.println("for dilemma number " +list.get(0));
					
					//incrementing current dilemma number
					list.set(0, list.get(0)+1);
    		
					//sending messages to the broker
					c.send("/topic/ddm", messageprediction(indexprobmax));
					
					System.out.println("sent : "+indexprobmax);
					
					c.send("/topic/ddm", messagepredictionchange());
					
					System.out.println(allpredictions(net));
					System.out.println(allprobmax(net));
					
					//if no predictions left to make, disconnect prediction server
					if (list.get(0)==8){
						c.disconnect();
					}
					
				} catch (JSONException e) {
					e.printStackTrace();
				}
				
				
				
            }
          } );

	}
}
