package me.liuzs.cabinetmanager.model;

public class FrequencyConverterStatus {
    public enum Status {
        Clockwise(1), Counterclockwise(2), Stop(3), Fault(4), PowerOff(5);
        private final int id;

        Status(int id) {
            this.id = id;
        }

        public int getValue() {
            return id;
        }
    }

    public Status status = Status.PowerOff;

    public float rotatingSpeed;

    public float frequency;

    public float targetFrequency;

    public Exception e;

}
