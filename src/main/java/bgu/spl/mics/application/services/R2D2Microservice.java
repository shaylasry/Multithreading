package bgu.spl.mics.application.services;

import bgu.spl.mics.MicroService;
import bgu.spl.mics.application.Main;
import bgu.spl.mics.application.messages.AttackEvent;
import bgu.spl.mics.application.messages.DeactivationEvent;
import bgu.spl.mics.application.messages.TerminationBrodcast;
import bgu.spl.mics.application.passiveObjects.Diary;

/**
 * R2D2Microservices is in charge of the handling {@link DeactivationEvent}.
 * This class may not hold references for objects which it is not responsible for:
 * {@link DeactivationEvent}.
 *
 * You can add private fields and public methods to this class.
 * You MAY change constructor signatures and even add new public constructors.
 */
public class R2D2Microservice extends MicroService {
    private long duration;
    public R2D2Microservice(long duratio) {
        super("R2D2");
        this.duration = duration;
    }

    @Override
    protected void initialize() {
        this.subscribeEvent(DeactivationEvent.class,
                call -> {
                    Thread.sleep(duration);
                    complete(call,true);
                    Diary.getInstance().setR2D2Deactivate(System.currentTimeMillis());
                    });
        this.subscribeBroadcast(TerminationBrodcast.class, call ->{
            Diary.getInstance().setR2D2Terminate(call.getTerminationTime());
            this.terminate();
        });
        Main.start.countDown();
    }
}
