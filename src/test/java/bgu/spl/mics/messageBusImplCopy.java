package bgu.spl.mics;

public class messageBusImplCopy  implements MessageBus{

    private static messageBusImplCopy instance = null;

    protected messageBusImplCopy() {
    }

    public static messageBusImplCopy getInstance() {
        if(instance == null) {
            instance = new messageBusImplCopy();
        }
        return instance;
    }

    @Override
    public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
    }

    @Override
    public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {

    }

    @Override @SuppressWarnings("unchecked")
    public <T> void complete(Event<T> e, T result) {
    }

    @Override
    public void sendBroadcast(Broadcast b) {

    }


    @Override
    public <T> Future<T> sendEvent(Event<T> e) {

        return null;
    }

    @Override
    public void register(MicroService m) {

    }

    @Override
    public void unregister(MicroService m) {

    }

    @Override
    public Message awaitMessage(MicroService m) throws InterruptedException {

        return null;
    }
}


