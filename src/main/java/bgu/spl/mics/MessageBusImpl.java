package bgu.spl.mics;


import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


/**
 * The {@link MessageBusImpl class is the implementation of the MessageBus interface.
 * Write your implementation here!
 * Only private fields and methods can be added to this class.
 */
public class MessageBusImpl implements MessageBus {
	 private static MessageBusImpl instance = null;
	 private ConcurrentHashMap <Class<? extends Event>, ConcurrentLinkedQueue<MicroService>> EventQueue;
	 private ConcurrentHashMap <Class<? extends Broadcast>, ConcurrentLinkedQueue<MicroService>> BroadcastQueue;
	 private ConcurrentHashMap <MicroService,ConcurrentLinkedQueue<Class<? extends Event>>> MsEvents;
	 private ConcurrentHashMap <MicroService,ConcurrentLinkedQueue<Class<? extends Broadcast>>> MsBroadcast;
	 private ConcurrentHashMap <MicroService, LinkedList<Message>> actionQueue;
	 private ConcurrentHashMap <Class<? extends Event>, int[]> robinCount;
	 private ConcurrentHashMap<Event,Future> futureQueue;




	 private MessageBusImpl() {
	 	//use two hashmaps for subscribe to Event and subscribe to Broadcast
	 	EventQueue = new ConcurrentHashMap<Class<? extends Event>, ConcurrentLinkedQueue<MicroService>>();
	 	BroadcastQueue = new ConcurrentHashMap<Class<? extends Broadcast>, ConcurrentLinkedQueue<MicroService>>();
	 	//use two hashmaps that keeps Events and Broadcasts subscription lists for each MS;
	 	MsEvents = new ConcurrentHashMap<MicroService,ConcurrentLinkedQueue<Class<? extends Event>>>();
	 	MsBroadcast = new ConcurrentHashMap<MicroService, ConcurrentLinkedQueue<Class<? extends Broadcast>>>();
	 	//robinCount keeps track of rubinManner track for each Event
	 	robinCount = new ConcurrentHashMap<Class<? extends Event>,int[]>();
	 	//actionQueue keeps the message queue for each MS
	 	actionQueue = new ConcurrentHashMap<MicroService,LinkedList<Message>>();
	 	//we use futureQueue to keep the futures for each event so we can resolve each one by call complete method
	 	futureQueue = new ConcurrentHashMap<Event,Future>();


	}

		private static class SingletonHolder {
			private static MessageBusImpl instance = new MessageBusImpl();
		}
		public static MessageBusImpl getInstance() {
			return SingletonHolder.instance;
		}


	@Override
	public <T> void subscribeEvent(Class<? extends Event<T>> type, MicroService m) {
		//we use synchronized to avoid the scenario of two threads try to add new Event type at the same time to EventQueue
	 	synchronized (this.EventQueue) {
			if (this.EventQueue.containsKey(type)) {
				this.robinCount.get(type)[1]++;
				this.EventQueue.get(type).add(m);
				this.MsEvents.get(m).add(type);
			} else {
				this.robinCount.put(type, new int[2]);
				this.robinCount.get(type)[1]++;
				this.EventQueue.put(type, new ConcurrentLinkedQueue<MicroService>());
				this.EventQueue.get(type).add(m);
				this.MsEvents.get(m).add(type);
			}
		}

	}

	@Override
	public void subscribeBroadcast(Class<? extends Broadcast> type, MicroService m) {
		//we use synchronized to avoid the scenario of two threads try to add
		//new Broadcast type at the same time to BroadcastQueue
	 	synchronized (this.BroadcastQueue) {
			if (this.BroadcastQueue.containsKey(type)) {
				this.BroadcastQueue.get(type).add(m);
				this.MsBroadcast.get(m).add(type);
			} else {
				this.BroadcastQueue.put(type, new ConcurrentLinkedQueue<>());
				this.BroadcastQueue.get(type).add(m);
				this.MsBroadcast.get(m).add(type);
			}
		}
    }

	@Override @SuppressWarnings("unchecked")
	public <T> void complete(Event<T> e, T result) {
	 	if(this.futureQueue.get(e)!= null){
			this.futureQueue.get(e).resolve(result);
			this.futureQueue.remove(e,this.futureQueue.get(e));
	 	}
	 }

	@Override
	public void sendBroadcast(Broadcast b) {
	 	for (MicroService microService : this.BroadcastQueue.get(b.getClass())) {
	 		synchronized (this.actionQueue.get(microService)) {
	 			this.actionQueue.get(microService).add(b);
	 			this.actionQueue.get(microService).notifyAll();
	 		}
	 	}
	 }


	@Override
	public synchronized <T> Future<T> sendEvent(Event<T> e) {
		Future<T> eventFuture = new Future<>();
		if (this.robinCount.get(e.getClass()) != null) {
			//synchronized robinCount to avoid scenario in which two Threads
			// try to read and update robinCount at the same time
			synchronized (this.robinCount.get(e.getClass())) {
				int[] rubin = this.robinCount.get(e.getClass());
				int keepRubin = rubin[0];
				this.robinCount.get(e.getClass())[0] = (keepRubin + 1) % rubin[1];
				MicroService[] arr = this.EventQueue.get(e.getClass()).toArray(new MicroService[0]);
				if (this.actionQueue.get(arr[keepRubin]) != null) {
					synchronized (this.actionQueue.get(arr[keepRubin])) {
						this.actionQueue.get(arr[keepRubin]).add(e);
						this.futureQueue.put(e, eventFuture);
						//after Event insertion to the MS queue we want to let it know he can stop
						// waiting and try to catch the new message
						this.actionQueue.get(arr[keepRubin]).notifyAll();
					}
				}
				this.robinCount.get(e.getClass()).notifyAll();
			}
			return  eventFuture;
		}
		return null;
	}

	@Override
	public void register(MicroService m) {
	 	//in registration each MS create his queue and his Event and Broadcasts subscribe lists
	 	this.actionQueue.put(m,new LinkedList<Message>());
	 	this.MsEvents.put(m,new ConcurrentLinkedQueue<Class<? extends Event>>());
	 	this.MsBroadcast.put(m,new ConcurrentLinkedQueue<Class<? extends Broadcast> >());
	}

	@Override
	public void unregister(MicroService m) {
	 	//clean all Events related reference
		for (Class<? extends Event> e:this.MsEvents.get(m)){
			this.EventQueue.get(e).remove(m);
			if (this.EventQueue.get(e).isEmpty()){
				this.EventQueue.remove(e);
			}
			this.robinCount.get(e)[1]--;
			int roundRubin[] = this.robinCount.get(e);
			//for this case there are no more Events in Event subscribe queue for this specific event
			//so we can remove it from robinCountHash
			if (roundRubin[0] == roundRubin[1] && roundRubin[0] == 0){ //the next if insure that if roundRubin[0] = 0 , also round rubin[1] = 0
				this.robinCount.remove(e);
			}
			//make sure that no matter what while unregistraion accours Event subscribe queue
			//size wont be smaller than robinCount size
			else if (roundRubin[1] < roundRubin[0]){
				this.robinCount.get(e)[0]--;
			}
			else { //Event subscribe queue is not empty and it size bigger from curr roundRubin cycle so we should do nothing
			}
			while (!MsEvents.get(m).isEmpty()){
				MsEvents.get(m).remove(e);
			}
			MsEvents.remove(m);
		}
		//clean all Broadcast related reference
		for (Class<? extends Broadcast> b:this.MsBroadcast.get(m)){
			this.BroadcastQueue.get(b).remove(m);
			if (this.BroadcastQueue.get(b).isEmpty()){
				this.BroadcastQueue.remove(b);
			}
			while (!MsBroadcast.get(m).isEmpty()){
				MsBroadcast.get(m).remove(b);
			}
			MsBroadcast.remove(m);
		}
		this.actionQueue.remove(m);

	}

	@Override
	public Message awaitMessage(MicroService m) throws InterruptedException {
		synchronized (this.actionQueue.get(m)) {
			while (this.actionQueue.get(m).isEmpty()) {
				try {
					this.actionQueue.get(m).wait();
				} catch (InterruptedException exception) {
				}
			}
			return this.actionQueue.get(m).remove();
		}
	}
}
