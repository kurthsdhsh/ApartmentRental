package model;

public class Tenant {
    private int tenantId;
    private String firstName, lastName, contactNo, email, address;
    private String currentUnit = "-";      // display only (JOIN)

    public Tenant() { }
    public Tenant(int tenantId, String firstName, String lastName, String contactNo, String email, String address) {
        this.tenantId = tenantId; this.firstName = firstName; this.lastName = lastName;
        this.contactNo = contactNo; this.email = email; this.address = address;
    }
    public int getTenantId() { return tenantId; }
    public void setTenantId(int v) { tenantId = v; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String v) { firstName = v; }
    public String getLastName() { return lastName; }
    public void setLastName(String v) { lastName = v; }
    public String getContactNo() { return contactNo; }
    public void setContactNo(String v) { contactNo = v; }
    public String getEmail() { return email; }
    public void setEmail(String v) { email = v; }
    public String getAddress() { return address; }
    public void setAddress(String v) { address = v; }
    public String getCurrentUnit() { return currentUnit; }
    public void setCurrentUnit(String v) { currentUnit = v; }
    public String getFullName() { return firstName + " " + lastName; }
}
