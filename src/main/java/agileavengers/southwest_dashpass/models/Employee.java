package agileavengers.southwest_dashpass.models;

public class Employee extends User{
    int employeeId;
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
    }

}
