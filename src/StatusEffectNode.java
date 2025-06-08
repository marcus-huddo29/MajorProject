// src/StatusEffectNode.java

public class StatusEffectNode {
    String effectName;
    int duration;
    StatusEffectNode next; // This field makes the class recursive

    /**
     * Constructor for a new status effect node.
     * @param name The name of the status effect (e.g., "Poison").
     * @param dur The duration of the effect in turns.
     */
    public StatusEffectNode(String name, int dur) {
        this.effectName = name;
        this.duration = dur;
        this.next = null; // Initially, the next node is null
    }
}