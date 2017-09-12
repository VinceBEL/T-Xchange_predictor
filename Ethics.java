package main;

import java.io.IOException;
import java.util.Scanner;

import javax.print.attribute.standard.PresentationDirection;
import javax.security.auth.login.LoginException;

import net.ser1.stomp.Client;
import smile.Network;

public class Ethics {
	
	private Affichage a;
	private Network net;
	private String[] nodenames;
	private String ethicsnode;
	private String profilenode;
	private int[] inputarray;
	private int[] displaypred;
	

	public Ethics(String userid) {
		//loading the network
		Network net = new Network();
		net.readFile("C:/Users/Belloeilvja/genie/1.1. Ethics Score & Final Answer.xdsl");
		net.updateBeliefs();
		this.net=net;
				
		//entering the node names
		String[] nodenames=new String[7];
		nodenames[0]="Answer_Dil41";nodenames[1]="Answer_Dil43";nodenames[2]="Answer_Dil55";nodenames[3]="Answer_Dil51";
		nodenames[4]="Answer_Dil6b";nodenames[5]="Answer_Dil61";nodenames[6]="Answer_Dil62";
		String ethicsnode="In_Game_Ethics_Score";
		String profilenode = "FinalEthicsScore";
		
		this.nodenames=nodenames;
		this.ethicsnode=ethicsnode;
		this.profilenode=profilenode;
		
		//store the inputs
		int[] inputarray = new int[7];
		
		this.inputarray = inputarray;
		
				
		//creating prediction array to display
		int[] displaypred = new int[7];
		
		this.displaypred = displaypred;
				
		//setting up the slide for display
		Affichage a = new Affichage(0, userid);
		this.a=a;
		a.setVisible(true);
	}
	
	public int getfirstchoice() {
		return this.inputarray[0];
	}
	
	public void updateall(int input, int time) {
		inputarray[time] = input;
		displaypred[0] = inputarray[0];
		
		String state="No";
		if(input==1) {
			state="Yes";
		}
		
		net.setEvidence(nodenames[time], state);
		net.updateBeliefs();
		
		int[] predictions=readprediction(net, nodenames);
		
		for (int j = time+1 ; j < 7 ; j++) {
			displaypred[j]=predictions[j];
		}
		
		if(displaypred[time]==inputarray[time]) {
			a.addsquare(1);
		}
		else {
			a.addsquare(0);
		}
		
		if(inputarray[0]==1 && time==3) {
			a.addsquare(-1);
		}
		
		double[] profile=net.getNodeValue(profilenode);
		int indmaxprof=0;
		double probmaxprof=profile[0];
		for(int k=1 ; k<profile.length ; k++) {
			if(profile[k]>probmaxprof) {
				probmaxprof=profile[k];
				indmaxprof=k;
			}
		}
		String maxprof=net.getOutcomeId(profilenode, indmaxprof);
		
		double[] ethic=net.getNodeValue(ethicsnode);
		
		int[] inputdisplay = new int[time+1];
		for(int j = 0 ; j<=time ; j++) {
			inputdisplay[j]=inputarray[j];
		}
		
		a.ethicschangeto((int)ethic[0]);
		a.changeprofile(maxprof);
		//a.lastanswer(predictions[6]);
		a.updateinputs(inputdisplay);
		a.updatepredictions(displaypred);
	}
	
	/**
	 * 
	 * @param net
	 * @param nodenames
	 * @return int array with all likely answers to come AND already stated
	 */
	public static int[] readprediction(Network net,String[] nodenames) {
		int[] predictions = new int[7];
		for(int i=0;i<nodenames.length;i++) {
			double[] outcomes=net.getNodeValue(nodenames[i]);
			if(outcomes[0]>0.5) {
				predictions[i]=1;
			}
		}
		return predictions;
	}

	public static void main(String[] args) throws LoginException, IOException {
		
		
		
		//loading the network
		Network net = new Network();
		net.readFile("C:/Users/Belloeilvja/genie/1.1. Ethics Score & Final Answer.xdsl");
		net.updateBeliefs();
		
		//entering the node names
		String[] nodenames=new String[7];
		nodenames[0]="Answer_Dil41";nodenames[1]="Answer_Dil43";nodenames[2]="Answer_Dil55";nodenames[3]="Answer_Dil51";
		nodenames[4]="Answer_Dil6b";nodenames[5]="Answer_Dil61";nodenames[6]="Answer_Dil62";
		String ethicsnode="In_Game_Ethics_Score";
		String profilenode = "FinalEthicsScore";
		
		//input scanner
		Scanner scan=new Scanner(System.in);
		
		//store the inputs
		int[] inputarray = new int[7];
		
		double[] ethic=net.getNodeValue(ethicsnode);
		double[] profile=net.getNodeValue(profilenode);
		
		//creating prediction array to display
		int[] displaypred = new int[7];
		
		//setting up the slide for display
		Affichage a = new Affichage((int)ethic[0], "this is userid");
		a.setVisible(true);
		
		//main loop
		for (int i=0;i<7;i++) {
			
			//input
			int input=scan.nextInt();
			inputarray[i]=input;
			if(i == 0 ) {
				displaypred[0]=input;
			}
			
			//converting to state (string)
			String state="No";
			if(input==1) {
				state="Yes";
			}
			
			//updating network
			net.setEvidence(nodenames[i], state);
			net.updateBeliefs();
			
			//printing predictions for all dilemmas (even stated ones)
			int[] predictions=readprediction(net, nodenames);
			System.out.println("["+ predictions[0] + " , " + predictions[1] + " , " + predictions[2] + " , "
					+ predictions[3] + " , " +predictions[4] + " , " + predictions[5] + " , " + predictions[6] + "]");
			
			//update the displayed predictions
			for (int j = i+1 ; j < 7 ; j++) {
				displaypred[j]=predictions[j];
			}
			
			//add square red or green 
			if(displaypred[i]==inputarray[i]) {
				a.addsquare(1);
			}
			else {
				a.addsquare(0);
			}
			
			//skipping dilemma #4 if answer is yes at dilemma #1
			if(inputarray[0]==1 && i==3) {
				i++;
				a.addsquare(-1);
			}
			
			//printing prediction for next dilemma
			if(i<6) {
				double[] Outcomes=net.getNodeValue(nodenames[i+1]);
				if(Outcomes[0]>0.5) {
					System.out.println("likely to say Yes next decision; proba= " +(int)(Outcomes[0]*100));
				}
				else {
					System.out.println("likely to say No next decision; proba= " +(int)(Outcomes[1]*100));
				}
			}
			
			//printing Ethics value
			ethic=net.getNodeValue(ethicsnode);
			System.out.println("ethics value " +ethic[0]);
			
			//getting the player's profile
			profile=net.getNodeValue(profilenode);
			int indmaxprof=0;
			double probmaxprof=profile[0];
			for(int k=1 ; k<profile.length ; k++) {
				if(profile[k]>probmaxprof) {
					probmaxprof=profile[k];
					indmaxprof=k;
				}
			}
			String maxprof=net.getOutcomeId(profilenode, indmaxprof);
			
			//setting the input array to display
			int[] inputdisplay = new int[i+1];
			for(int j = 0 ; j<=i ; j++) {
				inputdisplay[j]=inputarray[j];
			}
			
			//displaying the profile
			a.ethicschangeto((int)ethic[0]);
			a.changeprofile(maxprof);
			//a.lastanswer(predictions[6]);
			a.updateinputs(inputdisplay);
			a.updatepredictions(displaypred);
			
			
		}
		
		scan.close();
	}

}
