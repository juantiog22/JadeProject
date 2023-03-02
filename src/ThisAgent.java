package JadeProject;

import jade.core.Agent;

import java.sql.SQLOutput;
import java.util.HashSet;
import java.util.Random;
import java.util.ArrayList;
import jade.lang.acl.UnreadableException;
import jade.lang.acl.ACLMessage;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.MessageTemplate;

public class ThisAgent extends Agent{

    private ThisAgentGui gui;
    private Schedule schedule = new Schedule();
    private boolean try2schedule = false;
    private final int FINISHTIME = 2000;

    @Override
    protected void setup(){
        gui = new ThisAgentGui(this);
        gui.display();
        System.out.println(schedule.toString());
        System.out.println(getAID().getLocalName() + " is starting service");

        DFAgentDescription template = new DFAgentDescription();
        template.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType("this-agent");
        sd.setName(getAID().getLocalName());

        template.addServices(sd);

        try {
            DFService.register(this, template);//We register the Agent with the description of itsself
        } catch (FIPAException ex) {
            ex.printStackTrace();
        }

        addBehaviour(new ReceiveACancel());
        addBehaviour(new ReceiveAMeeting());
        addBehaviour(new ReceiveAnOk());
    }

    public void haveMeeting(){
        try2schedule= true;
        System.out.println("\n\n"+getAID().getLocalName() + " wants to have a meeting");
        addBehaviour(new StartingMeetingBehaviour());
    }

    @Override
    protected void takeDown() {
        gui.dispose();
        try {
            DFService.deregister(this);
        } catch (FIPAException ex) {
            ex.printStackTrace();
        }

        System.out.println(getAID().getLocalName() + " is shutting down service");//Message of finish of a Agent taken down ;)
    }

    private class StartingMeetingBehaviour extends Behaviour {
        private int step =0;
        private int day, hour;
        private long begin, end;
        private MessageTemplate mt;
        private double newPref;
        private int meetingWithWhom, canceled=0;

        private AID[] agents; // list of found agents

        @Override
        public void action() {
            if(try2schedule){
                switch(step){
                    case 0:
                        System.out.println("\n\n"+myAgent.getLocalName()+" is trying to start a meeting with anybody");

                        DFAgentDescription template = new DFAgentDescription();
                        ServiceDescription sd = new ServiceDescription();

                        sd.setType("this-agent");
                        template.addServices(sd);
                        try{
                            DFAgentDescription[] result =DFService.search(myAgent, template);

                            System.out.println(getAID().getLocalName() + ": We found this guys to be invited:");
                            agents = new AID[result.length - 1];
                            int help=0;
                            for (int i = 0; i < result.length; ++i)
                            {
                                if (!result[i].getName().equals(getAID().getName())) {
                                    agents[help] = result[i].getName();
                                    System.out.println(agents[help].getLocalName() +" has been found");
                                    help++;
                                }
                            }


                        }catch (FIPAException ex) {
                            ex.printStackTrace();
                        }
                        //seleccionar persona al azar
                        Random claseRandom = new Random();
                        meetingWithWhom= claseRandom.nextInt(agents.length);
                        System.out.println(getAID().getLocalName() + " has invited " +  agents[meetingWithWhom].getLocalName());
                        step =1;
                        break;
                    //Con quien va a haber meeting
                    case 1:

                        System.out.println("\n"+getAID().getLocalName() + ": Now is selecting a meeting time");

                        day= schedule.getArrayList().get(canceled).getDay();
                        hour = schedule.getArrayList().get(canceled).getHour();
                        newPref = schedule.getArrayList().get(canceled).getPreference();

                        if(newPref >= 0.5f){ //we check if there is a space
                            System.out.println(getAID().getLocalName()+" proposed meeting at: "+ schedule.getArrayList().get(canceled).getDayName(day) + " at " +schedule.getArrayList().get(canceled).getHourName(hour) );

                            begin = System.currentTimeMillis();

                            ACLMessage message = new ACLMessage(ACLMessage.CFP);

                            message.addReceiver(agents[meetingWithWhom]);

                            message.setContent(Integer.toString(day) + "," + Integer.toString(hour));
                            message.setConversationId("schedule-meeting");
                            message.setReplyWith("cfp" + System.currentTimeMillis());

                            schedule.timeSelected();//we change the schedule order
                            myAgent.send(message);
                            System.out.println(getAID().getLocalName() + ": Send the invitation");

                            mt = MessageTemplate.and(MessageTemplate.MatchConversationId("schedule-meeting"),
                                    MessageTemplate.MatchInReplyTo(message.getReplyWith()));//matching the template to find before receiving
                            step = 2;
                        }
                        else{
                            System.out.println(getAID().getLocalName() + " has no available timeslot for both of them");
                            step = 100;
                        }


                        break;

                    case 2: // we send the message
                        ACLMessage reply = myAgent.receive(mt);
                        end = System.currentTimeMillis();


                        long timeReceived = end - begin;//we calculate the time to receive the reply

                        if (timeReceived > FINISHTIME) {
                            cancelMessage(Integer.toString(day) + "," + Integer.toString(hour),null);
                            step =100;
                        }
                        else{
                            if(reply!=null){
                                if (reply.getPerformative() == ACLMessage.REJECT_PROPOSAL){
                                    AID rejectAgent = reply.getSender();
                                    System.out.println(getAID().getLocalName() + " meeting rejected with " + reply.getSender().getLocalName() );
                                    canceled++;

                                    schedule.timeSelectedCanceled(day,hour,newPref);
                                    cancelMessage(Integer.toString(day) + "," + Integer.toString(hour),rejectAgent);
                                    step =1;
                                }
                                else{
                                    System.out.println(getAID().getLocalName() + " accepted the meeting with " + reply.getSender().getLocalName() );
                                    canceled=0;
                                    step =3;
                                }
                            }else{
                                block(FINISHTIME);
                            }
                        }
                        break;

                    case 3: //send the confirmation

                        begin = System.currentTimeMillis();

                        ACLMessage confirmationMessage = new ACLMessage(ACLMessage.CONFIRM);

                        confirmationMessage.addReceiver(agents[meetingWithWhom]);


                        confirmationMessage.setContent(Integer.toString(day) + "," + Integer.toString(hour));
                        confirmationMessage.setConversationId("schedule-meeting");
                        confirmationMessage.setReplyWith("confirm" + System.currentTimeMillis());

                        myAgent.send(confirmationMessage);//We send the message

                        System.out.println(getAID().getLocalName() + " has sent the confirmation");

                        mt = MessageTemplate.and(MessageTemplate.MatchConversationId("schedule-meeting"),
                                MessageTemplate.MatchInReplyTo(confirmationMessage.getReplyWith()));

                        step = 4;

                        break;
                    case 4: //recieve the ok


                        ACLMessage ackMessage = myAgent.receive(mt);

                        end = System.currentTimeMillis();
                        timeReceived = end - begin;

                        if (timeReceived > FINISHTIME) {//We checked like before if the people reply that we have a meeting confirmed
                            cancelMessage(Integer.toString(day) + "," + Integer.toString(hour), null);
                            step = 100;//If not we send message of cancelation and we move ot the end
                        } else {
                            if (ackMessage != null) {
                                if (ackMessage.getPerformative() == ACLMessage.AGREE) {//If they agree
                                    AID agent = ackMessage.getSender();

                                    System.out.println(getAID().getLocalName() + ": received the ack message from " + agent.getLocalName());//we say that we received perfect the ACK
                                    System.out.println(getAID().getLocalName() + " has booked a meeting the "+ schedule.getArrayList().get(day).getDayName(day) + " at " +  schedule.getArrayList().get(hour).getHourName(hour));
                                    step = 5;
                                }
                            } else {
                                block(FINISHTIME);
                            }
                        }


                        break;
                    case 5:

                        break;
                    default:
                        System.out.println("An error has happened on agent :"+ getAID().getLocalName());
                        step = 100;
                        break;

                }
            }

        }

        private void cancelMessage(String content, AID agentReject) {
            System.out.println(getAID().getLocalName() + ": cancelling message "+ content);
            ACLMessage cancel = new ACLMessage(ACLMessage.CANCEL);//we generate a cancel message


            if (agentReject != null) {
                cancel.addReceiver(agents[meetingWithWhom]);
            }
            cancel.setContent(content);
            cancel.setConversationId("schedule-meeting");
            cancel.setReplyWith("cancel" + System.currentTimeMillis());

            myAgent.send(cancel);

            System.out.println(getAID().getLocalName() + ":sent the cancel message");

        }
        @Override
        public boolean done() {
            if (step == 100 || step == 5) {
                try2schedule = false;
                return true;
            } else {
                return false;
            }
        }
    }
    private class ReceiveAMeeting extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
            ACLMessage message = myAgent.receive(mt);
            if (message != null) {
                AID invited = message.getSender();

                String messageContent = message.getContent();
                String[] time = messageContent.split(",");

                int day, hour;
                day = Integer.parseInt(time[0]);
                hour = Integer.parseInt(time[1]);
                int index = schedule.find(day,hour);
                otherSchedule schedule_aux = schedule.getArrayList().get(index);
                System.out.println(getAID().getLocalName() + ": Has an invitation for a meeting the day: " + schedule.getArrayList().get(day).getDayName(day) + " at " +  schedule.getArrayList().get(hour).getHourName(hour));
                if (schedule_aux.getPreference() >= 0.5f) {
                    System.out.println(getAID().getLocalName() + ": is available for the meeting!");


                    ACLMessage acceptance = message.createReply();
                    acceptance.setContent(messageContent);
                    acceptance.setPerformative(ACLMessage.INFORM);

                    schedule.cancelReceiving(schedule_aux);

                    myAgent.send(acceptance);


                }
                else {
                    System.out.println(getAID().getLocalName() + ": is not available for the meeting");

                    ACLMessage rejection = message.createReply();

                    rejection.setContent(messageContent);
                    rejection.setPerformative(ACLMessage.REJECT_PROPOSAL);

                    myAgent.send(rejection);

                    System.out.println(getAID().getLocalName() + ": message of rejection send! Wainting for different proposals...");
                }
            } else {
                block(FINISHTIME);
            }



        }
    }
    private class ReceiveACancel extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CANCEL);
            ACLMessage message = myAgent.receive(mt);

            if (message != null) {
                AID cancelingAgent = message.getSender();

                String messageContent = message.getContent();
                String[] time = messageContent.split(",");

                int day, hour;
                day = Integer.parseInt(time[0]);
                hour = Integer.parseInt(time[1]);
                int index = schedule.find(day,hour);
                otherSchedule schedule_aux = schedule.getArrayList().get(index);

                System.out.println(getAID().getLocalName() + ": received a cancelation message from " + cancelingAgent.getLocalName()  + " for a meeting the day: " + schedule.getArrayList().get(day).getDayName(day)+" at "+schedule.getArrayList().get(day).getHourName(hour));

                //schedule.cancelReceiving(schedule_aux);

                System.out.println(getAID().getLocalName() + ": meeting the day:" + schedule.getArrayList().get(day).getDayName(day)+" at "+schedule.getArrayList().get(day).getHourName(hour) + " has been cancelled");
            } else {
                block(FINISHTIME);
            }
        }
    }
    private class ReceiveAnOk extends CyclicBehaviour {
        @Override
        public void action() {
            MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CONFIRM);
            ACLMessage message = myAgent.receive(mt);

            if (message != null) {
                ACLMessage ackMessage = message.createReply(); //generate ackMessage replying the confirmation
                String messageContent = message.getContent();	// content day,hour
                String[] time = messageContent.split(",");  //we split the content in [day,hour]

                int day, hour;
                day = Integer.parseInt(time[0]);
                hour = Integer.parseInt(time[1]);

                System.out.println(getAID().getLocalName() + ": received the confirmation for the meeting on " + schedule.getArrayList().get(day).getDayName(day) +" at " + schedule.getArrayList().get(day).getHourName(hour));

                ackMessage.setContent(messageContent);
                ackMessage.setPerformative(ACLMessage.AGREE);

                myAgent.send(ackMessage);

                System.out.println(getAID().getLocalName() + "sent the ack");
            } else {
                block(FINISHTIME);
            }
        }
    }
}
