package bgu.spl.mics.application.messages;
import bgu.spl.mics.Event;
import bgu.spl.mics.application.passiveObjects.Attack;

import java.util.List;

public class AttackEvent implements Event<Boolean> {
    private Attack attackImpl;
    private String location; //use this filed to know the attack event location in Leia SendEvents sequence

    public AttackEvent(String location,List<Integer> serials,int duration) {
        this.location = location;
        this.attackImpl = new Attack(serials,duration);
    }
    public String getLocation() {
        return location;
    }

    public Attack getAttackImpl() {
        return attackImpl;
    }

}
