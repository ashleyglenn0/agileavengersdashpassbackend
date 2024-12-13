package agileavengers.southwest_dashpass.controllers;

import agileavengers.southwest_dashpass.models.Reservation;
import agileavengers.southwest_dashpass.services.QRCodeService;
import agileavengers.southwest_dashpass.services.ReservationService;
import com.google.zxing.WriterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@Controller
public class QRCodeController {

    @Autowired
    private QRCodeService qrCodeService;
    @Autowired
    private ReservationService reservationService;

    @GetMapping("/qr-code/{customerId}/{reservationId}")
    public ResponseEntity<byte[]> getQRCode(@PathVariable Long customerId, @PathVariable Long reservationId)
            throws WriterException, IOException {

        // Verify that the reservation belongs to the customer
        Reservation reservation = reservationService.findById(reservationId);
        if (reservation.getCustomer().getId() != customerId) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // Return 403 if unauthorized
        }

        // Determine the QR code content based on DashPass status
        String qrText = reservation.hasDashPass() ?
                "Reservation with DashPass - ID: " + reservationId :
                "Reservation Only - ID: " + reservationId;

        // Generate the QR code
        byte[] qrImage = qrCodeService.generateQRCode(qrText, 200, 200);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"qr_code.png\"")
                .contentType(MediaType.IMAGE_PNG)
                .body(qrImage);
    }

}

