package agileavengers.southwest_dashpass.services;

import agileavengers.southwest_dashpass.models.Bag;
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


}
