package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.Main;
import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.messages.TerminationBrodcast;
import bgu.spl.mics.application.passiveObjects.Diary;
import bgu.spl.mics.application.passiveObjects.Ewoks;

import java.util.List;


/**
 * C3POMicroservices is in charge of the handling {@link AttackEvents}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link AttackEvents}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class C3POMicroservice extends MicroService {


    public C3POMicroservice() {
        super("C3PO");

    }

    @Override
    protected void initialize() {
        //same way as HanSolo (except Diary updates)
        this.subscribeEvent(AttackEvent.class,
                call -> {
                    Ewoks ewoks = Ewoks.getInstance();
                    List<Integer> serials = call.getAttackImpl().getSerials();
                    serials.sort(Integer::compareTo);
                    for (Integer id : serials) {
                        ewoks.acquireEwok(id);  locking ewoks singleton
                    }
                    Thread.sleep(call.getAttackImpl().getDuration());
                    for (Integer id : serials) {
                        ewoks.releaseEwok(id);
                    }
                    complete(call, true);
                    Diary.getInstance().addTotalAttacks();
                    if (call.getLocation().equals("last")) {
                        Diary.getInstance().setC3POFinish(System.currentTimeMillis());
                    }
                });
        this.subscribeBroadcast(TerminationBrodcast.class, call -> {
            Diary.getInstance().setC3POTerminate(call.getTerminationTime());
            this.terminate();
        });
        Main.start.countDown();
    }

}
