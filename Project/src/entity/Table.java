package entity;

public class Table {
    private int id;
    private String status;

    public Table(int id, String status) {
        this.id = id;
        this.status = status;
    }

    public int getId() { return id; }
    public String getStatus() { return status; }
}
