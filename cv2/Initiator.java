import jade.core.*; 
import jade.core.behaviours.*;
import jade.lang.acl.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import java.util.ArrayList;
import java.util.Collections;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Initiator extends Agent{
    protected DFAgentDescription[] result;
    protected Integer TIMEOUT = 5000; 
    public ArrayList<Price> prices = new ArrayList<Price>();
    protected void setup() {
        System.out.println(getLocalName() +" READY");
        addBehaviour(new WakerBehaviour(this, 10000) {
            protected void onWake(){
                System.out.println(getLocalName() + " AWAKE");	
                getService("vypracovani-projektu-z-AGS");
                if(result.length > 0){
                    // cfp
                    ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
                    for(int i = 0; i < result.length; i++){
                        cfp.addReceiver(result[i].getName());
                    }
                    cfp.setContent(TIMEOUT.toString());
                    myAgent.send(cfp);
                    mybehav();
                } else {
                    System.out.println(getLocalName() +" nenasel zadneho poskitovatele reseni ulohy");
                    // konec?
                    
                }
            }
        });
        
    } 
    // vrati vsechny agenty nabizejici sluzbu service
    protected void getService(String service) {
        DFAgentDescription dfd = new DFAgentDescription();
        ServiceDescription sd  = new ServiceDescription();
        sd.setType( service );
        dfd.addServices(sd);
        try {
            result =  DFService.search(this, dfd);
            System.out.println("Result len: " + result.length);
        } catch (FIPAException fe) {
            fe.printStackTrace();
        }
    }
    // timeout
    protected void mybehav(){
        final CyclicBehaviour MCB = new CyclicBehaviour(){
            @Override
            public void action(){
                ACLMessage msg = myAgent.receive();  
                if (msg == null){ 
                    block();
            }
            else{ 
                if(msg.getPerformative() == ACLMessage.PROPOSE){
                    System.out.println(myAgent.getAID().getName() +" prijal za cenu: " + msg.getContent());
                    prices.add(new Price(msg.getSender(), Integer.parseInt(msg.getContent())));
                }
                else if(msg.getPerformative() == ACLMessage.REFUSE){
                    System.out.println(myAgent.getAID().getName() +" odmitl ");
                }
                else {}
            
             
            }
        };
        };
        addBehaviour(MCB);
        addBehaviour(new WakerBehaviour(this, TIMEOUT) {
            @Override
            protected void onWake(){
                removeBehaviour(MCB);
                System.out.println(myAgent.getAID().getName() +": Tak udela to nekdo za me? ");
                // seradim ceny
                Collections.sort(prices);
                // odeberu prvni a posledni pokud jich je vice jak 2
                if(prices.size() > 2){
                    ACLMessage rej = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                    rej.addReceiver(prices.get(0).getAgent());
                    System.out.println(prices.get(0).getAgent().getName()+" - vyrazen ");
                    prices.remove(0);
                    rej.addReceiver(prices.get(prices.size()-1).getAgent());
                    System.out.println(prices.get(prices.size()-1).getAgent().getName()+" - vyrazen ");
                    prices.remove(prices.size()-1);
                    ACLMessage acc = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                    acc.addReceiver(prices.get(0).getAgent());
                    send(acc);
                    prices.remove(0);
                    
                    for (Price p:prices) {
                        System.out.println(p.getAgent().getName()+" - odmitnut ");
                        rej.addReceiver(p.getAgent());
                    }
                    send(rej);
                    addBehaviour(new rcv());
                } else {
                    ACLMessage rej = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
                    for (Price p:prices) {
                        System.out.println(p.getAgent().getName()+" - odmitnut ");
                        rej.addReceiver(p.getAgent());
                    }
                    
                    send(rej);
                    System.out.println(myAgent.getAID().getName() +": Tak si to budu muset hold udelat sam :-(");
                }
            };
            });
    }
}


class Price implements Comparable<Object> {
	private AID agent;
	private Integer price;
 
	public Price(AID pAgent, Integer pPrice) {
		this.agent = pAgent;
		this.price = pPrice;
	}
 
	public Integer getPrice() {
		return price;
	}
 
	public AID getAgent() {
		return agent;
	}
 
        @Override
	public int compareTo(Object o) {
		if (o instanceof Price) {
			Price a = (Price)o;
			if (this.price == a.price)
			{
			    return 0;
			}
			else if (this.price > a.price)
			{
			    return 1;
			}
			else
			{
			    return -1;
			}
                }
		return 0;
	}
}

class rcv extends CyclicBehaviour{
  public void action() {
    ACLMessage msg = myAgent.receive();  
    if (msg == null){ 
      block();
    }
    else{ 
      if(msg.getPerformative() == ACLMessage.INFORM){
          System.out.println("Tak a mam to hotovo a udelal to " + msg.getSender().getName() + "a napsal mi: " +msg.getContent());
      } else if(msg.getPerformative() == ACLMessage.FAILURE){
          // neimplementovano
      }
    }
  }
}