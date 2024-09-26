package agileavengers.southwest_dashpass.models;

import jakarta.persistence.*;


@Entity
@Inheritance(strategy= InheritanceType.JOINED)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String username;
    private String email;
    private String password;
    private UserType userType;

    public User(String fname, String lname, String uname, String mail, String pword) {
        this.firstName = fname;
        this.lastName = lname;
        this.username = uname;
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public UserType getUserType() {
        return userType;
    }

    protected void setUserType(UserType customer) {
    }
}
