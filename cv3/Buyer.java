import jade.core.*; 
import jade.lang.acl.*;
import jade.core.behaviours.*;
import jade.tools.gui.ACLStatisticsFrame;
/**
 *
 * @author nesty
 */
public class Buyer extends Agent{
    protected int acceptedPrize = (int) (Math.random()*100);
    private ACLMessage msg;
    private static final String STATE_WAIT = "Wait";
    private static final String STATE_BUY = "Buy";
    private static final String STATE_END = "End";
    
    protected void setup(){
        System.out.println(this.getLocalName() +": Maximalni cena - " + acceptedPrize);
                    
        FSMBehaviour fsm = new FSMBehaviour(this);
        
        fsm.registerFirstState(new Reciver(), STATE_WAIT);
        
        fsm.registerState(new Buy(), STATE_BUY);
        
        fsm.registerLastState(new End(), STATE_END);
        
        fsm.registerDefaultTransition(STATE_WAIT, STATE_WAIT);
        fsm.registerTransition(STATE_WAIT, STATE_BUY, 0);
        fsm.registerTransition(STATE_WAIT, STATE_END, 1);
        fsm.registerDefaultTransition(STATE_BUY, STATE_END);
       
        addBehaviour(fsm);
    }


    private class Reciver extends OneShotBehaviour{
        private int exitVal;
        
        public void action() {
            msg = myAgent.receive();
            if(msg == null){
                exitVal = 4;
            } else {
                if(msg.getPerformative() == ACLMessage.INFORM){
                    if(acceptedPrize >= Integer.parseInt(msg.getContent())){
                        exitVal = 0;
                    }
                } else {
                    exitVal = 1;
                }
            }
        }

        public int onEnd(){
            return exitVal;
        }
    }
    
    private class Buy extends OneShotBehaviour{
        private int exitVal;
        
        public void action(){
            ACLMessage acc = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
            acc.addReceiver(new AID("Arbitr", AID.ISLOCALNAME));
            send(acc);
            exitVal = 5;
           
        }
        
        public int onEnd(){
            return exitVal;
        }
    }
    
    private class End extends CyclicBehaviour{
        @Override
        public void action() {
            if(msg != null && msg.getPerformative() != ACLMessage.INFORM){
                System.out.println(myAgent.getLocalName() +": " + msg.getContent());
                myAgent.doDelete();
            } else {
                msg = myAgent.receive();
                if(msg == null){
                    block();
                }
            }
        }   
    }
}    
