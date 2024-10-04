package agileavengers.southwest_dashpass.models;

import jakarta.persistence.*;

@Entity
@Table(name="airport")
public class Airport {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="airportId")
    private Long airportId;
    @Column(name="airportCode")
    private String airportCode;

    public Airport(){
        this.airportId = 0000000001L;
        this.airportCode = "";
    }

    public Airport(Long id, String acode){
        this.airportId = id;
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
