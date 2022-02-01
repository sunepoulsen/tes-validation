package dk.sunepoulsen.tes.validation.exceptions;

import dk.sunepoulsen.tes.validation.model.ValidateViolationModel;
import lombok.Getter;

import java.util.Set;

@Getter
public class ModelValidateException extends RuntimeException {
    private final Set<ValidateViolationModel> violations;

    public ModelValidateException(String message, Set<ValidateViolationModel> violations) {
        super(message);
        this.violations = violations;
    }
}
