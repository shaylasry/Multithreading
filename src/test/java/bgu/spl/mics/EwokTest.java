package bgu.spl.mics;

import bgu.spl.mics.application.passiveObjects.Ewok;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.TimeUnit;


import static org.junit.jupiter.api.Assertions.*;

public class EwokTest {

    private Ewok ewok;

    @BeforeEach
    public void setUp(){
        ewok = new Ewok(2);
    }

    @Test
    public void testAcquire()
    {
        assertTrue(ewok.getAvailable());
        ewok.acquire();
        assertFalse(ewok.getAvailable());
    }

    @Test
    public void testRelease(){
        ewok.acquire();
        assertFalse(ewok.getAvailable());
        ewok.release();
        assertTrue(ewok.getAvailable());
    }


}

