package main;

import java.io.IOException;
import java.util.Map;
import java.util.Scanner;

import javax.security.auth.login.LoginException;

import org.json.JSONException;
import org.json.JSONObject;

import net.ser1.stomp.Client;
import net.ser1.stomp.Listener;

public class Subscriber {
	
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
	
	public static String messageprediction(int i, String user){
		return "{\n"+
            	"	\"busMessageType\": \"changeIndicatorValue\",\n"+
            	"	\"indicatorRef\": \"prediction\",\n"+
            	"	\"value\": "+i+",\n"+
            		"	\"operator\": \"set\"\n"+
            	"	\"userid\": " + user +",\n" +
            	"}";
	}
	
	public static String askuserid() {
		System.out.println("What's your user id?");
		Scanner scan = new Scanner(System.in);
		String res = scan.nextLine();
		scan.close();
		return res;
	}

	public static void main(String[] args) throws LoginException, IOException {
		// TODO Auto-generated method stub
		
		String user   = "admin";
        String pass   = "password";
        //String broker = "tcp://0.0.0.0:61613";
        String topic  = "/topic/itemread";
        
        String userid = askuserid();
        
        final Client c = new Client( "localhost", 61613, user, pass );
        
        Ethics et = new Ethics(userid);

        c.subscribe( topic, new Listener() {
        	int time = 0;
			public void message(Map header, String body) {
				
				JSONObject jsonObj;
				
				try {
					jsonObj = new JSONObject(body);
					int input=jsonObj.getInt("value");
					System.out.println("received : " + input);
					
					
					et.updateall(input, time);
					if(time == 3 && et.getfirstchoice() == 1) {
						time++;
					}
					time++;
					
					
					c.send("/topic/ddm", messagepredictionchange());
					c.send("/topic/ddm", messageprediction(0, userid));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
        	
        });
	}

}
