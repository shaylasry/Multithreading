package bgu.spl.mics.application.passiveObjects;

/**
 * Passive data-object representing a forest creature summoned when HanSolo and C3PO receive AttackEvents.
 * You must not alter any of the given public methods of this class.
 * <p>
 * You may add fields and methods to this class as you see fit (including public methods).
 */
public class Ewok {
	int serialNumber;
	boolean available;

	public Ewok(int serialNumber){
	    this.serialNumber = serialNumber;
	    this.available = true;
    }
  
    /**
     * Acquires an Ewok
     */
    public synchronized void acquire() {
        if (this.available) {
            this.available = false;
        }
        else {                      //use available filed to check if AttackHandler can tak this Ewok
            try {
                this.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * release an Ewok
     */
    public synchronized void release()
    {
        if (!this.available) {
            this.available = true;
            this.notifyAll();         //notify all Thread who are waiting for this Ewok key
        }
    }
    public int getSerialNumber() {
        return this.serialNumber;
    }
    public boolean getAvailable(){
        return this.available;
    }

}
