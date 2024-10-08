package agileavengers.southwest_dashpass.models;

import jakarta.persistence.*;

@Entity
@Table(name="airport")
public class Airport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="airportId")
    private Long airportId;
    @Column(name="airportCode", nullable = false, length = 3)
    private String airportCode;

    public String getAirportName() {
        return airportName;
    }

    public void setAirportName(String airportName) {
        this.airportName = airportName;
    }

    @Column(name="airportName")
    private String airportName;

    public Airport(){
        this.airportCode = "";
    }

    public Airport(String acode){
        this.airportCode = acode;
    }

    public Long getAirportId(){
        return this.airportId;
    }
    public String getAirportCode(){
        return this.airportCode;
    }

    public void setAirportId(Long airportId){
        this.airportId = airportId;
    }

    public void setAirportCode(String airportCode){
        this.airportCode = airportCode;
    }
}
