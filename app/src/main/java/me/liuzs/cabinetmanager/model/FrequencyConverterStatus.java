package me.liuzs.cabinetmanager.model;

public class FrequencyConverterStatus {
    public enum StatusType {
        Clockwise(1), Counterclockwise(2), Stop(3), Fault(4), PowerOff(5);
        private final int id;

        StatusType(int id) {
            this.id = id;
        }

        public int getValue() {
            return id;
        }
    }

    public StatusType statusType = StatusType.PowerOff;

    public float RotatingSpeed;

}
