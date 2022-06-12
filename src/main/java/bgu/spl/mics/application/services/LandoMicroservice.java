package bgu.spl.mics.application.services;

import bgu.spl.mics.Future;
import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.Main;
import bgu.spl.mics.application.messages.BombDestroyerEvent;
import bgu.spl.mics.application.messages.DeactivationEvent;
import bgu.spl.mics.application.messages.TerminationBrodcast;
import bgu.spl.mics.application.passiveObjects.Diary;

/**
 * LandoMicroservice
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class LandoMicroservice  extends MicroService {

    private Future deactivated;
    long duration;


    public LandoMicroservice(long duration) {
        super("Lando");
        deactivated=new Future();
        this.duration=duration;
    }

    @Override
    protected void initialize() {
        this.subscribeEvent(BombDestroyerEvent.class, call ->  {
            //Lando will be the one who sends DeactivationEvent to R2d2. when R2d2 will finish he notify Lando
            deactivated=sendEvent(new DeactivationEvent());
            synchronized (this.deactivated) {
                while (!deactivated.isDone()) {
                    try {
                        this.deactivated.wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
            //after deactivation Lando can now start callBack for BombDestroyerEvent
            Thread.sleep(duration);
            this.complete(call,true);
            //after Termination Lando will send BroadCast to all other MS to Terminate with Termination time
            this.sendBroadcast(new TerminationBrodcast(System.currentTimeMillis()));
        });
        this.subscribeBroadcast(TerminationBrodcast.class, call ->{
            Diary.getInstance().setLandoTerminate(call.getTerminationTime());
            this.terminate();
        });
        Main.start.countDown();
    }
}
