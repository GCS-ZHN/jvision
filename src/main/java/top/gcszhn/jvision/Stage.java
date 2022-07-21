package top.gcszhn.jvision;

public enum Stage {
    GRAHPIC_INITIALIZATION ("initialization"),
    DATA_LOADING ("loading"),
    GRAHPIC_PAINTING ("painting"),
    GRAHPIC_SERIALIZATION ("serialization");

    private String name;
    private Stage(String name) {
        this.name = name;
    }
    public String toString() {
        return name;
    }
}
