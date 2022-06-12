package bgu.spl.mics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;


public class MessageBusImplTest {

    private MessageBusImpl bus = null;
    simpleMicroservice m1;
    simpleMicroservice2 m2;
    private AttackEventExample attack;
    private AttackBroadcast brodAttack;

    @BeforeEach
    public void setUp() {
        bus = MessageBusImpl.getInstance();
        m1 = new simpleMicroservice();
        m2 = new simpleMicroservice2();
        attack = new AttackEventExample();
        brodAttack = new AttackBroadcast();
        bus.register(m1);
        bus.register(m2);
    }
    @AfterEach
    public void clean(){
        bus.unregister(m1);
        bus.unregister(m2);
    }



    //register method called by subscribe methods. so register it self will be check while using thos methodd
    @Test
    public void testSubscribeEvent() {
        bus.register(m2);
        bus.subscribeEvent(attack.getClass(), m2);
        bus.sendEvent(attack);
        Message m = null;
        try {
            m = bus.awaitMessage(m2);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertSame(m, attack);
    }

    @Test
    void testSubscribeBroadcast() {
        bus.subscribeBroadcast(brodAttack.getClass(),m1);
        bus.subscribeBroadcast(brodAttack.getClass(),m2);
        bus.sendBroadcast(brodAttack);
        Message message1 = null;
        Message message2 = null;
        try {
            message1 = bus.awaitMessage(m2);
            message2 = bus.awaitMessage(m1);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertSame(message1, brodAttack);
        assertSame(message1, message2);
    }

    @Test
    public void testComplete() {
        bus.subscribeEvent(attack.getClass(), m2);
        Future <Boolean> f = bus.sendEvent(attack);
        bus.complete(attack,true);
        assertTrue(f.isDone());
    }

    @Test
    public void testSendBroadcast() {
        bus.subscribeBroadcast(brodAttack.getClass(),m1);
        bus.subscribeBroadcast(brodAttack.getClass(),m2);
        bus.sendBroadcast(brodAttack);
        Message message1 = null;
        Message message2 = null;
        try {
            message1 = bus.awaitMessage(m1);
            message2 = bus.awaitMessage(m2);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertSame(message1, brodAttack);
        assertSame(message1, message2);

    }

    @Test
    public void testSendEvent() {
        bus.register(m2);
        bus.subscribeEvent(attack.getClass(), m2);
        bus.sendEvent(attack);
        Message message=null;
        try {
            message= bus.awaitMessage(m2);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertSame(message, attack);

    }

    @Test
    public void testAwaitMessage() {
        bus.subscribeEvent(attack.getClass(), m1);
        bus.subscribeBroadcast(brodAttack.getClass(),m2);
        bus.sendEvent(attack);
        bus.sendBroadcast(brodAttack);
        Message message1=null;
        Message message2=null;
        try {
            message1= bus.awaitMessage(m1);
            message2= bus.awaitMessage(m2);

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        assertSame(message1,attack);
        assertSame(message2,brodAttack);
    }

}

