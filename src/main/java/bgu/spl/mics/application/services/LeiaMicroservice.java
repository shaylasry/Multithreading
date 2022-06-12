package bgu.spl.mics.application.services;

import java.util.ArrayList;
import java.util.List;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.Main;
import bgu.spl.mics.application.messages.BombDestroyerEvent;
import bgu.spl.mics.application.messages.TerminationBrodcast;
import bgu.spl.mics.application.passiveObjects.Attack;
import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.passiveObjects.Diary;

/**
 * LeiaMicroservices Initialized with Attack objects, and sends them as  {@link }.
 * This class may not hold references for objects which it is not responsible for:
 * {@link }.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class LeiaMicroservice extends MicroService {
    private Attack[] attacks;
    private Future[] attackFuture;
    Diary d = Diary.getInstance();

    public LeiaMicroservice(Attack[] attacks) {
        super("Leia");
        this.attacks = attacks;
        this.attackFuture = new Future[attacks.length];
    }

    @Override
    protected void initialize() {
        boolean ready=false;
        this.subscribeBroadcast(TerminationBrodcast.class, call -> {
            Diary.getInstance().setLeiaTerminate(call.getTerminationTime());
            this.terminate();
        });
        while (!ready){
            try{
                Main.start.await();
                ready=true;
            }catch (InterruptedException e){}
        }
        for (int i = 0; i < attacks.length; i++) {
            //Leia send Events in roundRubin way,there are only 2 attack handlers so last event sent and the one before
            //will be the last events for each attack handler
            if ((i == attacks.length - 1) || (i == attacks.length - 2)) {
                AttackEvent attack = new AttackEvent("last", attacks[i].getSerials(), attacks[i].getDuration());
                attackFuture[i] = this.sendEvent(attack);
            } else {
                AttackEvent attack = new AttackEvent("", attacks[i].getSerials(), attacks[i].getDuration());
                attackFuture[i] = this.sendEvent(attack);
            }
        }
        for (Future f : attackFuture) {
            f.get();
        }
        sendEvent(new BombDestroyerEvent());
    }
}

