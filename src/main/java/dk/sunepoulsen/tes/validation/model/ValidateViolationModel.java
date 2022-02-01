package dk.sunepoulsen.tes.validation.model;

import lombok.Data;

@Data
public class ValidateViolationModel {
    private String param;
    private String messageTemplate;
    private String message;
    private String invalidValue;
}
