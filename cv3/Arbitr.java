import jade.core.*; 
import jade.core.behaviours.*;
import jade.lang.acl.*;

/**
 *
 * @author nesty
 */
public class Arbitr extends Agent{
    protected int price = 100;
    protected ParallelBehaviour pb;
    protected String Buyers[] = new String[]{"B1","B2","B3","B4","B5"};
    
    protected void setup(){
        addBehaviour(new WakerBehaviour(this, 10000) {
            @Override
            protected void onWake(){
                System.out.println("\nAukce zacina");    
                pb = new ParallelBehaviour(myAgent, ParallelBehaviour.WHEN_ALL);
                pb.addSubBehaviour(new TickerBehaviour(myAgent, 1000) {
                    @Override
                    protected void onTick() {  
                        if(price > 0){
                            ACLMessage msg = new ACLMessage(ACLMessage.INFORM);
                            msg.setContent("" + price--);
                            for(int i = 0; i < Buyers.length; i++){
                                msg.addReceiver(new AID(Buyers[i], AID.ISLOCALNAME));
                            }
                            send(msg);
                        } 
                    }
                });
                pb.addSubBehaviour(new CyclicBehaviour() {
                    @Override
                    public void action() {
                        ACLMessage msg = myAgent.receive();  
                        if (msg == null){ 
                            block();
                        } else {
                            ACLMessage reply = new ACLMessage(ACLMessage.CONFIRM);
                            reply.setContent("Aukce konci, vitezem je " + msg.getSender().getName());
                            for(int i = 0; i < Buyers.length; i++){
                               reply.addReceiver(new AID(Buyers[i], AID.ISLOCALNAME));
                            }
                            reply.addReceiver(myAgent.getAID());
                            send(reply);
                           
                            pb.addSubBehaviour(new OneShotBehaviour() {
                                @Override
                                public void action() {
                                    System.out.println(myAgent.getLocalName() +": Aukce je skoncena");
                                    myAgent.doDelete();
                                }
                            });
                        }
                    }
                });
                addBehaviour(pb);
            }         
        });
    }
}
