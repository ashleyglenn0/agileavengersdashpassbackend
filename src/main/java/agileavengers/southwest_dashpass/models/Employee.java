package agileavengers.southwest_dashpass.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "employee")
public class Employee extends User{
    int employeeId = 0;
    String role;
    boolean canSellDashPass;

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    boolean canRedeemDashPass;
    public Employee(String fname, String lname, String uname, String mail, String pword, String role) {
        super(fname, lname, uname, mail, pword);
        this.role = role;
        this.setUserType(UserType.EMPLOYEE);
        this.employeeId = this.employeeId + 1;
    }

}
