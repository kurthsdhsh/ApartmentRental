package model;

/** Put real IDs inside a JComboBox. */
public class ComboItem {
    private final int id;
    private final String label;

    public ComboItem(int id, String label) { this.id = id; this.label = label; }
    public int getId() { return id; }
    @Override public String toString() { return label; }   // what the combo displays
}
