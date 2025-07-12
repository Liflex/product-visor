/**
 * Custom hook for form validation
 * Provides real-time validation and error handling for forms
 */

import { useState, useCallback, useMemo } from 'react';
import { validateProductForm } from '../utils/validation.js';

/**
 * Custom hook for form validation
 * @param {Object} initialValues - Initial form values
 * @param {Function} customValidator - Custom validation function
 * @returns {Object} Form validation state and methods
 */
export const useFormValidation = (initialValues = {}, customValidator = null) => {
  const [values, setValues] = useState(initialValues);
  const [errors, setErrors] = useState({});
  const [touched, setTouched] = useState({});
  const [isSubmitting, setIsSubmitting] = useState(false);

  /**
   * Update field value
   */
  const updateValue = useCallback((fieldName, value) => {
    setValues(prev => ({
      ...prev,
      [fieldName]: value
    }));

    // Clear error when user starts typing
    setErrors(prev => {
      if (prev[fieldName]) {
        return {
          ...prev,
          [fieldName]: ''
        };
      }
      return prev;
    });
  }, []);

  /**
   * Mark field as touched
   */
  const markFieldTouched = useCallback((fieldName) => {
    setTouched(prev => ({
      ...prev,
      [fieldName]: true
    }));
  }, []);

  /**
   * Validate single field
   */
  const validateField = useCallback((fieldName, value) => {
    if (customValidator) {
      const currentValue = value !== undefined ? value : values[fieldName];
      const fieldErrors = customValidator({ ...values, [fieldName]: currentValue });
      const fieldError = fieldErrors.errors?.[fieldName] || '';
      
      setErrors(prev => ({
        ...prev,
        [fieldName]: fieldError
      }));
      
      return !fieldError;
    }
    return true;
  }, [values, customValidator]);

  /**
   * Validate all fields
   */
  const validateForm = useCallback((formValues) => {
    if (customValidator) {
      const currentValues = formValues || values;
      const validation = customValidator(currentValues);
      setErrors(validation.errors || {});
      return validation.isValid;
    }
    return true;
  }, [values, customValidator]);

  /**
   * Handle field change with validation
   */
  const handleFieldChange = useCallback((fieldName) => (event) => {
    const value = event.target.type === 'checkbox' 
      ? event.target.checked 
      : event.target.value;
    
    updateValue(fieldName, value);
    
    // Validate field if it's been touched
    if (touched[fieldName]) {
      validateField(fieldName, value);
    }
  }, [updateValue, validateField, touched]);

  /**
   * Handle field blur
   */
  const handleFieldBlur = useCallback((fieldName) => () => {
    markFieldTouched(fieldName);
    validateField(fieldName);
  }, [markFieldTouched, validateField]);

  /**
   * Handle form submission
   */
  const handleSubmit = useCallback(async (onSubmit) => {
    setIsSubmitting(true);
    
    // Mark all fields as touched
    const allFields = Object.keys(values);
    const touchedFields = {};
    allFields.forEach(field => {
      touchedFields[field] = true;
    });
    setTouched(touchedFields);
    
    // Validate form
    const isValid = validateForm();
    
    if (isValid && onSubmit) {
      try {
        await onSubmit(values);
      } catch (error) {
        console.error('Form submission error:', error);
      }
    }
    
    setIsSubmitting(false);
    return isValid;
  }, [values, validateForm]);

  /**
   * Reset form to initial values
   */
  const resetForm = useCallback(() => {
    setValues(initialValues);
    setErrors({});
    setTouched({});
    setIsSubmitting(false);
  }, [initialValues]);

  /**
   * Set form values (for editing)
   */
  const setFormValues = useCallback((newValues) => {
    setValues(newValues);
    setErrors({});
    setTouched({});
  }, []);

  /**
   * Get field props for input components
   */
  const getFieldProps = useCallback((fieldName) => ({
    value: values[fieldName] || '',
    onChange: handleFieldChange(fieldName),
    onBlur: handleFieldBlur(fieldName),
    error: touched[fieldName] ? errors[fieldName] : '',
    hasError: touched[fieldName] && !!errors[fieldName]
  }), [values, handleFieldChange, handleFieldBlur, touched, errors]);

  // Computed values
  const hasErrors = useMemo(() => {
    return Object.values(errors).some(error => !!error);
  }, [errors]);

  const isFormValid = useMemo(() => {
    return !hasErrors;
  }, [hasErrors]);

  const touchedFields = useMemo(() => {
    return Object.keys(touched).filter(field => touched[field]);
  }, [touched]);

  return {
    // Values and state
    values,
    errors,
    touched,
    isSubmitting,
    
    // Computed
    hasErrors,
    isFormValid,
    touchedFields,
    
    // Methods
    updateValue,
    markFieldTouched,
    validateField,
    validateForm,
    handleFieldChange,
    handleFieldBlur,
    handleSubmit,
    resetForm,
    setFormValues,
    getFieldProps
  };
};

/**
 * Specialized hook for product form validation
 * @param {Object} initialValues - Initial form values
 * @param {boolean} isEdit - Whether this is an edit form
 * @returns {Object} Product form validation state and methods
 */
export const useProductFormValidation = (initialValues = {}, isEdit = false) => {
  const validator = useCallback((formData) => {
    return validateProductForm(formData, isEdit);
  }, [isEdit]);

  return useFormValidation(initialValues, validator);
}; 