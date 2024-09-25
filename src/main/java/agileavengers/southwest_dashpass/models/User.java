package agileavengers.southwest_dashpass.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //Generate ID
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    private String password;
    private UserType userType;

    public User(String fname, String lname, String mail, String pword) {
        this.firstName = fname;
        this.lastName = lname;
        this.email = mail;
        this.password = pword;
    }
    public User() {
        this.firstName = "";
        this.lastName = "";
        this.email = "";
        this.password = "";
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    protected void setUserType(UserType customer) {
    }
}
