package agileavengers.southwest_dashpass.models;

import java.util.List;

public class FlightSearchResponse {
    private List<Flight> outboundFlights;
    private List<Flight> returnFlights; // Empty for one-way trips

    // Getters and setters

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
}
