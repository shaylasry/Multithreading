package bgu.spl.mics.application.services;


import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.Main;
import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.messages.TerminationBrodcast;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.Ewoks;

import java.util.List;

/**
 * HanSoloMicroservices is in charge of the handling {@link AttackEvents}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link AttackEvents}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class HanSoloMicroservice extends MicroService {

    public HanSoloMicroservice() {
        super("Han");
    }


    @Override
    protected void initialize() {
        this.subscribeEvent(AttackEvent.class,
                call -> {
                    Ewoks ewoks = Ewoks.getInstance();
                    List<Integer> serials = call.getAttackImpl().getSerials();
                    serials.sort(Integer::compareTo);   //we sort the Array to get Ewoks by serials number order
                    //for shared Ewoks, first attack handler to reach the first shared Ewok will stop the other attack handler
                    //so acquire the first shared Ewok and all other shared Ewoks after the first one is Thread safe
                    for (Integer id : serials) {
                        ewoks.acquireEwok(id);
                    }
                    //each iteration lock release and notify for specific Ewok. Thread who waits for this specific Ewok can
                    //resume running right away
                    Thread.sleep(call.getAttackImpl().getDuration());
                    for (Integer id : serials) {
                        ewoks.releaseEwok(id);
                    }
                    complete(call,true);
                    Diary.getInstance().addTotalAttacks();
                    if (call.getLocation().equals("last")){ //check if this is HanSolo last event.
                        Diary.getInstance().setHanSoloFinish(System.currentTimeMillis());//if so add finish time to Diary
                    }
                });
        this.subscribeBroadcast(TerminationBrodcast.class,call ->{
            Diary.getInstance().setHanSoloTerminate(call.getTerminationTime());
            this.terminate();
        });
        Main.start.countDown();
    }
}
