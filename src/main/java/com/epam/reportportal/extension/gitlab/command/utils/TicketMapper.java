package com.epam.reportportal.extension.gitlab.command.utils;

import com.epam.reportportal.extension.gitlab.rest.client.model.IssueExtended;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;

public class TicketMapper {
    public static Ticket toTicket(IssueExtended input) {
        Ticket ticket = new Ticket();
        ticket.setId(input.getId().toString());
        ticket.setSummary(input.getDescription());
        ticket.setStatus(input.getState().toString());
        ticket.setTicketUrl(input.getWebUrl());
        return ticket;
    }
}
