package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.models.Bag;
import agileavengers.southwest_dashpass.models.BagStatus;
import agileavengers.southwest_dashpass.repository.BagRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;

@Service
public class BagService {
    @Autowired
    private BagRepository bagRepository;
    public List<Bag> findBagsByReservationId(Long reservationId) {
        return bagRepository.findByReservation_ReservationId(reservationId);
    }

    public List<Bag> findBagsByCustomerId(Long customerId) {
        return bagRepository.findByCustomerId(customerId);
    }

    public void updateBagStatus(Long reservationId, String nextStatus) {
        // Validate the next status
        BagStatus bagStatus;
        try {
            bagStatus = BagStatus.valueOf(nextStatus); // Convert string to BagStatus enum
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid bag status: " + nextStatus);
        }

        // Retrieve bags for the given reservation ID
        List<Bag> bags = bagRepository.findByReservation_ReservationId(reservationId);

        if (bags.isEmpty()) {
            throw new RuntimeException("No bags found for the reservation ID: " + reservationId);
        }

        // Update the status of each bag
        for (Bag bag : bags) {
            bag.setStatus(bagStatus);
        }

        // Save the updated bags
        bagRepository.saveAll(bags);
    }


}
