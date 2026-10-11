package model;

import java.math.BigDecimal;

public class Unit {
    private int unitId, floorNo;
    private String unitNo, unitType, status;
    private BigDecimal monthlyRent;

    public Unit() { }
    public Unit(int unitId, String unitNo, String unitType, int floorNo, BigDecimal monthlyRent, String status) {
        this.unitId = unitId; this.unitNo = unitNo; this.unitType = unitType;
        this.floorNo = floorNo; this.monthlyRent = monthlyRent; this.status = status;
    }
    public int getUnitId() { return unitId; }
    public void setUnitId(int v) { unitId = v; }
    public String getUnitNo() { return unitNo; }
    public void setUnitNo(String v) { unitNo = v; }
    public String getUnitType() { return unitType; }
    public void setUnitType(String v) { unitType = v; }
    public int getFloorNo() { return floorNo; }
    public void setFloorNo(int v) { floorNo = v; }
    public BigDecimal getMonthlyRent() { return monthlyRent; }
    public void setMonthlyRent(BigDecimal v) { monthlyRent = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { status = v; }
}
