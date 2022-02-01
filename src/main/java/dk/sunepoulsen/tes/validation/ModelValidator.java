package dk.sunepoulsen.tes.validation;

import dk.sunepoulsen.tes.validation.exceptions.ModelValidateException;
import dk.sunepoulsen.tes.validation.model.ValidateViolationModel;
import org.hibernate.validator.HibernateValidator;
import org.hibernate.validator.HibernateValidatorConfiguration;
import org.hibernate.validator.cfg.ConstraintMapping;
import org.hibernate.validator.cfg.context.TypeConstraintMappingContext;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Utility class to validate objects against a set a constraints.
 * <p>
 *     Hibernate and Spring have support of validating objects based on
 *     annotations of the properties of the object. Unfortunately they do
 *     not has support different validation constraints based on context.
 *     And we have a need to validate differently depending on the context
 *     when we validate bodies and variables in REST requests.
 * </p>
 * <p>
 *     For instance an object has an id property that is used asa primary key in
 *     the database. This property need to be validated differently based on the
 *     REST operation in a CRUD service. The id must be <code>null</code> in the
 *     POST (create) operation. But is required in the GET (read) operation.
 * </p>
 * <p>
 *     To solve this problem we have come up with this class.
 * </p>
 */
public class ModelValidator {
    /**
     * Validates an object against a set of constraints.
     *
     * @param <T> Object type.
     * @param value The value to validate.
     * @param clazz Object class type. Required by hibernate.
     * @param mappings A Consumer functional interface that adds any constraints that
     *                 value should be validated against.
     *
     * @throws ModelValidateException Thrown if any constraints is violated.
     */
    public static <T> void validate( T value, Class<T> clazz, Consumer<TypeConstraintMappingContext<T>> mappings ) throws ModelValidateException {
        HibernateValidatorConfiguration configuration = Validation
            .byProvider( HibernateValidator.class )
                .configure();

        ConstraintMapping constraintMapping = configuration.createConstraintMapping();

        mappings.accept( constraintMapping.type( clazz ) );

        Validator validator = configuration.addMapping( constraintMapping )
                .buildValidatorFactory()
                .getValidator();

        Set<ConstraintViolation<T>> violations = validator.validate( value );
        if(!violations.isEmpty()) {
            throw new ModelValidateException("Unable to validate object of type " + clazz.getName(), violations.stream()
                .map(ModelValidator::mapConstraintViolation)
                .collect(Collectors.toSet())
            );
        }
    }

    /**
     * Validates an object against its constraint annotations.
     *
     * @param value The value to validate.
     *
     * @throws ModelValidateException Thrown if any constraints is violated.
     */
    public static <T> void validate( T value ) throws ModelValidateException {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        Validator validator = factory.getValidator();

        Set<ConstraintViolation<T>> violations = validator.validate( value );
        if(!violations.isEmpty()) {
            throw new ModelValidateException("Unable to validate value of type " + value.getClass().getName(), violations.stream()
                .map(ModelValidator::mapConstraintViolation)
                .collect(Collectors.toSet())
            );
        }
    }

    private static <T> ValidateViolationModel mapConstraintViolation(ConstraintViolation<T> violation) {
        ValidateViolationModel result = new ValidateViolationModel();
        result.setParam(violation.getPropertyPath().toString());
        result.setMessageTemplate(violation.getMessageTemplate());
        result.setMessage(violation.getMessage());
        if (violation.getInvalidValue() != null) {
            result.setInvalidValue(violation.getInvalidValue().toString());
        }

        return result;
    }
}
