import jade.core.*; 
import jade.core.behaviours.*;
import jade.domain.*;
import jade.domain.FIPAAgentManagement.*;
import jade.lang.acl.*;

public class Participant extends Agent {
    protected void setup() {
        System.out.println(getLocalName() + " READY");	
	try{
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            ServiceDescription sd = new ServiceDescription();
            sd.setName("vypracování projektu z AGS");
            sd.setType("vypracovani-projektu-z-AGS");
            sd.addOntologies("ags");
            sd.addLanguages(FIPANames.ContentLanguage.FIPA_SL);
            //sd.addProperties(new Property("country", "Italy"));
            dfd.addServices(sd);
            DFService.register(this, dfd);	
            	
        }
        catch (FIPAException fe){
            fe.printStackTrace();
        }
        addBehaviour(new RecieveMsgBehaviour());
    } 
}

class RecieveMsgBehaviour extends CyclicBehaviour {
    protected AID lastone;
    public void action(){
        ACLMessage msg = myAgent.receive();
        if (msg == null){ 
            block();
        }
        else{ 
            if( msg.getPerformative() == ACLMessage.CFP){
                Integer todo =  (int) (Math.random()*10) % 10;
                if( todo < 5){ // udela
                    ACLMessage reply =  new ACLMessage(ACLMessage.PROPOSE);
                    reply.addReceiver(msg.getSender());
                    Integer price = (int) (Math.random()*10000);
                    reply.setContent(price.toString());
                    myAgent.send(reply);
                    System.out.println(myAgent.getAID().getName() +" moje nabidka je " + price);
                } else if(todo == 6 || todo == 7) { // neudela
                    ACLMessage reply =  new ACLMessage(ACLMessage.REFUSE);
                    reply.addReceiver(msg.getSender()); 
                    System.out.println(myAgent.getAID().getName() +" toto delat nebudu ");
                    myAgent.send(reply);
                } else if (todo == 8){ // udela ale oznami to pozde
                        lastone = msg.getSender();
                        myAgent.addBehaviour(new WakerBehaviour(myAgent, Integer.parseInt(msg.getContent()) + 200) {
                        @Override
                        protected void onWake() {
                            ACLMessage reply =  new ACLMessage(ACLMessage.PROPOSE);
                            reply.addReceiver(lastone);
                            Integer price = (int) (Math.random()*10000);
                            reply.setContent(price.toString());
                            myAgent.send(reply);
                            
                            System.out.println(myAgent.getAID().getName() +" moje nabidka je " + price + " ale asi uz pozde");
                        }
                    });
                } else { 
                    
                    System.out.println(myAgent.getAID().getName() +" na to nema ani smysl odpovidat ");
                    // neodpovi
                }
            } else if(msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL){
                lastone = msg.getSender();
                System.out.println(myAgent.getAID().getName() +msg.getSender().getName() +" prijal moji nabidku");
                myAgent.addBehaviour(new WakerBehaviour(myAgent, 5000) {
                    @Override
                    protected void onWake() {
                     //   if(Math.random()%2 == 0){
                            ACLMessage done = new ACLMessage(ACLMessage.INFORM);
                            done.addReceiver(lastone);
                            done.setContent("Mam to hotovo");
                            myAgent.send(done);
                       /* } else {
                            ACLMessage done = new ACLMessage(ACLMessage.FAILURE);
                            done.addReceiver(lastone);
                            done.setContent("Nepodarilo se");
                            myAgent.send(done);
                        }*/
                    }
                    
                });
            } else if(msg.getPerformative() == ACLMessage.REJECT_PROPOSAL){
                System.out.println(myAgent.getAID().getName() +" moje nabidka byla odmitnuta ");
            }
            //System.out.println("Reciver agent " + myAgent.getAID().getName() + " replied to msg.");  
    }
    };
    
}