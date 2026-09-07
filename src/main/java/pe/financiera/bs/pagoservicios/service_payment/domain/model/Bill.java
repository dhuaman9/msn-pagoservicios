package pe.financiera.bs.pagoservicios.service_payment.domain.model;

import lombok.Builder;
import lombok.Value;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;

@Value
@Builder(toBuilder = true)
public class Bill {
    String id;
    String number;
    BigDecimal amount;
    String amountFormat;
    String currency;
    BigDecimal discount;
    BigDecimal commission;
    LocalDate dueDate;
    String description;

    public Long getDueDateInMillisec() {
        if (dueDate == null) return null;
        try {
            return dueDate.atTime(0, 0, 0).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        } catch (Exception e) {
            return LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
    }
}
