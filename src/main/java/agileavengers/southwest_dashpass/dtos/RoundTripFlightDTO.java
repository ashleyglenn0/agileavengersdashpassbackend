package agileavengers.southwest_dashpass.dtos;

import agileavengers.southwest_dashpass.models.Flight;

import java.util.ArrayList;
import java.util.List;

public class RoundTripFlightDTO {

    private String tripId;
    private List<Flight> outboundFlights;
    private List<Flight> returnFlights;

    // Constructor that initializes lists to avoid NullPointerException
    public RoundTripFlightDTO(String tripId) {
        this.tripId = tripId;
        this.outboundFlights = new ArrayList<>();
        this.returnFlights = new ArrayList<>();
    }

    // Getters and Setters
    public String getTripId() {
        return tripId;
    }

    public void setTripId(String tripId) {
        this.tripId = tripId;
    }

    public List<Flight> getOutboundFlights() {
        return outboundFlights;
    }

    public void setOutboundFlights(List<Flight> outboundFlights) {
        this.outboundFlights = outboundFlights;
    }

    public List<Flight> getReturnFlights() {
        return returnFlights;
    }

    public void setReturnFlights(List<Flight> returnFlights) {
        this.returnFlights = returnFlights;
    }

    // Method to add flights to the appropriate list based on direction
    public void addFlight(Flight flight) {
        if ("OUTBOUND".equalsIgnoreCase(flight.getDirection())) {
            this.outboundFlights.add(flight);
        } else if ("RETURN".equalsIgnoreCase(flight.getDirection())) {
            this.returnFlights.add(flight);
        }
    }
}

