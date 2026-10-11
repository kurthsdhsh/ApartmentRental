package model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Contract {
    private int contractId, tenantId, unitId;
    private LocalDate startDate, endDate;
    private BigDecimal monthlyRent = BigDecimal.ZERO, deposit = BigDecimal.ZERO;
    private String status = "Active";
    private String tenantName, unitNo;      // display only (JOIN)

    public Contract() { }
    public int getContractId() { return contractId; }
    public void setContractId(int v) { contractId = v; }
    public int getTenantId() { return tenantId; }
    public void setTenantId(int v) { tenantId = v; }
    public int getUnitId() { return unitId; }
    public void setUnitId(int v) { unitId = v; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate v) { startDate = v; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate v) { endDate = v; }
    public BigDecimal getMonthlyRent() { return monthlyRent; }
    public void setMonthlyRent(BigDecimal v) { monthlyRent = v; }
    public BigDecimal getDeposit() { return deposit; }
    public void setDeposit(BigDecimal v) { deposit = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { status = v; }
    public String getTenantName() { return tenantName; }
    public void setTenantName(String v) { tenantName = v; }
    public String getUnitNo() { return unitNo; }
    public void setUnitNo(String v) { unitNo = v; }
}
