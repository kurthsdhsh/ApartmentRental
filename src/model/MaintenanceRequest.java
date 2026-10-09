package model;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Data only. tenantId == 0 means "no tenant" (saved as NULL). unitNo / tenantName are filled by JOIN queries. */
public class MaintenanceRequest {
    private int requestId;
    private int unitId;
    private int tenantId;
    private String description;
    private String priority = "Medium";
    private LocalDate dateReported = LocalDate.now();
    private String status = "Open";
    private LocalDate dateResolved;          // null until resolved
    private BigDecimal repairCost = BigDecimal.ZERO;
    // display-only (from JOIN)
    private String unitNo;
    private String tenantName;

    public MaintenanceRequest() { }

    public MaintenanceRequest(int requestId, int unitId, int tenantId, String description, String priority,
                              LocalDate dateReported, String status, LocalDate dateResolved, BigDecimal repairCost) {
        this.requestId = requestId; this.unitId = unitId; this.tenantId = tenantId;
        this.description = description; this.priority = priority; this.dateReported = dateReported;
        this.status = status; this.dateResolved = dateResolved; this.repairCost = repairCost;
    }

    public int getRequestId() { return requestId; }
    public void setRequestId(int requestId) { this.requestId = requestId; }
    public int getUnitId() { return unitId; }
    public void setUnitId(int unitId) { this.unitId = unitId; }
    public int getTenantId() { return tenantId; }
    public void setTenantId(int tenantId) { this.tenantId = tenantId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public LocalDate getDateReported() { return dateReported; }
    public void setDateReported(LocalDate dateReported) { this.dateReported = dateReported; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDate getDateResolved() { return dateResolved; }
    public void setDateResolved(LocalDate dateResolved) { this.dateResolved = dateResolved; }
    public BigDecimal getRepairCost() { return repairCost; }
    public void setRepairCost(BigDecimal repairCost) { this.repairCost = repairCost; }
    public String getUnitNo() { return unitNo; }
    public void setUnitNo(String unitNo) { this.unitNo = unitNo; }
    public String getTenantName() { return tenantName; }
    public void setTenantName(String tenantName) { this.tenantName = tenantName; }
}
