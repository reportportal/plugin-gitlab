package com.epam.reportportal.extension.gitlab.utils;

import com.epam.reportportal.extension.gitlab.dto.IssueDto;
import com.epam.ta.reportportal.ws.model.externalsystem.Ticket;

public class TicketMapper {

  public static Ticket toTicket(IssueDto input) {
    Ticket ticket = new Ticket();
    ticket.setId(input.getIid().toString());
    ticket.setSummary(input.getTitle());
    ticket.setStatus(input.getState());
    ticket.setTicketUrl(input.getWebUrl());
    return ticket;
  }
}
