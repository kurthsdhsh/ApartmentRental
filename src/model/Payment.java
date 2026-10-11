package model;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Payment {
    private int paymentId, contractId, userId;
    private LocalDate paymentDate = LocalDate.now();
    private String billingMonth, paymentMethod = "Cash", status = "Paid", remarks;
    private BigDecimal amount;
    private String tenantName, unitNo, receivedBy;     // display only (JOIN)

    public Payment() { }
    public int getPaymentId() { return paymentId; }
    public void setPaymentId(int v) { paymentId = v; }
    public int getContractId() { return contractId; }
    public void setContractId(int v) { contractId = v; }
    public int getUserId() { return userId; }
    public void setUserId(int v) { userId = v; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate v) { paymentDate = v; }
    public String getBillingMonth() { return billingMonth; }
    public void setBillingMonth(String v) { billingMonth = v; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal v) { amount = v; }
    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String v) { paymentMethod = v; }
    public String getStatus() { return status; }
    public void setStatus(String v) { status = v; }
    public String getRemarks() { return remarks; }
    public void setRemarks(String v) { remarks = v; }
    public String getTenantName() { return tenantName; }
    public void setTenantName(String v) { tenantName = v; }
    public String getUnitNo() { return unitNo; }
    public void setUnitNo(String v) { unitNo = v; }
    public String getReceivedBy() { return receivedBy; }
    public void setReceivedBy(String v) { receivedBy = v; }
}
