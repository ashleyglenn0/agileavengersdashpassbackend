package agileavengers.southwest_dashpass.controllers;

import agileavengers.southwest_dashpass.models.SupportRequest;
import agileavengers.southwest_dashpass.services.SupportRequestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class SupportRequestController {
    @Autowired
    private SupportRequestService supportRequestService;

    @PostMapping("/employee/{employeeId}/supportrequest/{supportRequestId}/close")
    public String closeSupportRequest(@PathVariable Long employeeId, @PathVariable Long supportRequestId, Model model) {
        supportRequestService.updateStatus(supportRequestId, SupportRequest.Status.CLOSED);
        return "redirect:/employee/" + employeeId + "/managerequests";
    }

    @PostMapping("/employee/{employeeId}/supportrequest/{supportRequestId}/escalate")
    public String escalateSupportRequest(@PathVariable Long employeeId, @PathVariable Long supportRequestId, Model model) {
        supportRequestService.updateStatus(supportRequestId, SupportRequest.Status.ESCALATED);
        return "redirect:/employee/" + employeeId + "/managerequests";
    }

}
