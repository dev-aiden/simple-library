package com.aiden.dev.simplelibrary.modules.account.form;

import lombok.Data;

@Data
public class NotificationForm {

    private Boolean bookRentalNotificationByEmail;

    private Boolean bookRentalNotificationByWeb;

    private Boolean bookReturnNotificationByEmail;

    private Boolean bookReturnNotificationByWeb;

    private Boolean bookRentalAvailabilityNotificationByEmail;

    private Boolean bookRentalAvailabilityNotificationByWeb;
}
