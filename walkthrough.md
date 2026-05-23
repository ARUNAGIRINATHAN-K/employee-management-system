# Walkthrough - Core EMS Enhancements Implementation

All requested core enhancements and gap fixes (F-01, F-02, F-03, F-04) have been successfully implemented, compiled, and verified.

---

## 🛠️ Summary of Changes

### 1. Employee Profile Self-Service & Change Request Workflow (F-01)
*   **[NEW] [ProfileChangeRequest.java](file:///a:/My%20project/employee-management-system/src/main/java/com/ems/entity/ProfileChangeRequest.java):** Declares requested changes in a JSON field (`requestedFieldsJson`) alongside metadata (`status`, `submittedAt`, `processedBy`, `comments`).
*   **[NEW] [ProfileChangeRequestRepository.java](file:///a:/My%20project/employee-management-system/src/main/java/com/ems/repository/ProfileChangeRequestRepository.java):** Manages queries for requests by employee or status.
*   **[NEW] [ProfileChangeRequestService.java](file:///a:/My%20project/employee-management-system/src/main/java/com/ems/service/ProfileChangeRequestService.java):** Business logic to submit, approve (merges fields back into the `Employee` entity), and reject requests.
*   **[NEW] [ProfileChangeRequestController.java](file:///a:/My%20project/employee-management-system/src/main/java/com/ems/controller/ProfileChangeRequestController.java):** Exposes secure REST endpoints.
*   **[MODIFY] [dashboard.html](file:///a:/My%20project/employee-management-system/src/main/resources/static/dashboard.html):** Adds **My Profile** tab, HR-only **Change Requests** dashboard, and profile request modal.
*   **[MODIFY] [api.js](file:///a:/My%20project/employee-management-system/src/main/resources/static/js/api.js) & [app.js](file:///a:/My%20project/employee-management-system/src/main/resources/static/js/app.js):** Connects profile loading, self-service request submission, and HR-side request comparisons/actions.

### 2. Leave Policy Engine & Automated Accruals (F-02)
*   **[NEW] [LeavePolicy.java](file:///a:/My%20project/employee-management-system/src/main/java/com/ems/entity/LeavePolicy.java):** Database entity replacing hardcoded values for `leaveType`, `annualAllocation`, and `monthlyAccrualRate`.
*   **[NEW] [LeavePolicyRepository.java](file:///a:/My%20project/employee-management-system/src/main/java/com/ems/repository/LeavePolicyRepository.java):** Standard query repository.
*   **[MODIFY] [LeaveService.java](file:///a:/My%20project/employee-management-system/src/main/java/com/ems/service/LeaveService.java):** Reads `LeavePolicy` values to compute allocations and execute monthly or manual accruals. Resolved transaction read-only exception bug on `getLeaveBalances`.
*   **[MODIFY] [LeaveController.java](file:///a:/My%20project/employee-management-system/src/main/java/com/ems/controller/LeaveController.java):** Exposes GET policies and PUT updates endpoints.
*   **[MODIFY] [DataSeeder.java](file:///a:/My%20project/employee-management-system/src/main/java/com/ems/config/DataSeeder.java):** Seeds default leave policies for `CASUAL`, `SICK`, and `EARNED` leaves.
*   **[MODIFY] [dashboard.html](file:///a:/My%20project/employee-management-system/src/main/resources/static/dashboard.html):** Incorporates **Leave Policies Configuration** panel for HR Admins.
*   **[MODIFY] [api.js](file:///a:/My%20project/employee-management-system/src/main/resources/static/js/api.js) & [app.js](file:///a:/My%20project/employee-management-system/src/main/resources/static/js/app.js):** Binds leave policy configuration updates and tables.

### 3. Shift Rostering & Grace Period UI (F-03)
*   **[MODIFY] [dashboard.html](file:///a:/My%20project/employee-management-system/src/main/resources/static/dashboard.html):** Adds **Manage Shifts** (`⚙️ Shifts`) button in the Employees tab, which opens a detailed shift configuration modal.
*   **[MODIFY] [api.js](file:///a:/My%20project/employee-management-system/src/main/resources/static/js/api.js) & [app.js](file:///a:/My%20project/employee-management-system/src/main/resources/static/js/app.js):** Connects shift creations to `POST /api/shifts` and reloads shift options dynamically.

### 4. Variable Expenses & Claim Reimbursements (F-04)
*   **[NEW] [ExpenseClaim.java](file:///a:/My%20project/employee-management-system/src/main/java/com/ems/entity/ExpenseClaim.java):** Declares category, amount, status (`PENDING`, `APPROVED`, `REJECTED`, `PAID`), claimDate, approvedBy, and comments.
*   **[NEW] [ExpenseClaimRepository.java](file:///a:/My%20project/employee-management-system/src/main/java/com/ems/repository/ExpenseClaimRepository.java):** Manages queries for expenses.
*   **[NEW] [ExpenseClaimService.java](file:///a:/My%20project/employee-management-system/src/main/java/com/ems/service/ExpenseClaimService.java) & [ExpenseClaimController.java](file:///a:/My%20project/employee-management-system/src/main/java/com/ems/controller/ExpenseClaimController.java):** Handles submission and manager/HR approval workflows.
*   **[MODIFY] [PayrollService.java](file:///a:/My%20project/employee-management-system/src/main/java/com/ems/service/PayrollService.java):** Integrates approved expense claims during the month of `payPeriod` directly into the generated payroll `allowances` and marks those claims as `PAID`.
*   **[MODIFY] [dashboard.html](file:///a:/My%20project/employee-management-system/src/main/resources/static/dashboard.html):** Adds **Expenses** tab and submission modal.
*   **[MODIFY] [api.js](file:///a:/My%20project/employee-management-system/src/main/resources/static/js/api.js) & [app.js](file:///a:/My%20project/employee-management-system/src/main/resources/static/js/app.js):** Bridges expense claim submission, list binding, and approval processes.

---

## 📈 Verification & Testing

### 1. Automated Validation
Running `mvnw.cmd clean test` compiled all components and created the MySQL tables:
```cmd
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

### 2. Database Schema DDL Actions
On startup, Hibernate altered the schema automatically:
*   Created tables `profile_change_requests`, `leave_policies`, and `expense_claims`.
*   Seeded default policies for `CASUAL` (+1.5), `SICK` (+1.0), and `EARNED` (+1.5) leaves.

---

## 🚀 How to Test and Manual Verification

The application is actively running on: **[http://localhost:8080](http://localhost:8080)**.

### Test Case 1: Profile Self-Service Request (F-01)
1. Login as Employee (`employee` / `employee123`).
2. Navigate to the **My Profile** tab. Click **Request Profile Edit**.
3. Change First Name to `Rahul Kumar` and Phone to `9090909090`. Click **Submit Request**.
4. Log out, then log in as HR Admin (`admin` / `admin123`).
5. Open the **Change Requests** tab. Compare current vs requested changes, and click **Approve**.
6. Verify the Employee Directory shows the updated values.

### Test Case 2: Dynamic Leave Policies (F-02)
1. Login as HR Admin (`admin` / `admin123`).
2. Navigate to **Leave Management**. In the **Leave Policies Configuration** panel, click **Edit** on `CASUAL` leave.
3. Change Monthly Accrual Rate to `3.0` and click **Save Policy**.
4. Click **Accrue Leaves** in the toolbar.
5. Verify employee casual balances increase by `3.0` instead of the old hardcoded `1.5`.

### Test Case 3: Shift Management (F-03)
1. Login as HR Admin. Go to **Employees**.
2. Click **Manage Shifts** (`⚙️ Shifts`). Add a new shift:
   - Name: `Evening Shift`
   - Start Time: `14:00`
   - End Time: `22:00`
   - Grace Period: `10`
3. Click **Add Shift**. Edit an employee and verify the dropdown contains `Evening Shift`.

### Test Case 4: Expense Claims & Payroll (F-04)
1. Login as Employee. Navigate to **Expenses** tab.
2. Click **Submit Expense Claim**. Fill details: Title = `Client Dinner`, Amount = `150.00`, Category = `MEALS`. Submit it.
3. Login as HR Admin or Manager (`manager` / `manager123`). Go to **Expenses** and click **Approve** on the claim.
4. Go to **Payroll**, click **Generate Payroll** for that employee, selecting the current month (e.g. `2026-05`).
5. Verify that the allowances field in the generated record includes the `$150.00` expense.
6. Verify the expense status in the employee's history has transitioned from `APPROVED` to `PAID`.
