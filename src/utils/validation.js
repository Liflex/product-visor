/**
 * Validation utilities for Product Visor application
 * Provides validation functions for form fields and data types
 */

import { API_CONFIG } from '../config/api-config.js';

/**
 * Validation result object
 * @typedef {Object} ValidationResult
 * @property {boolean} isValid - Whether the value is valid
 * @property {string} message - Error message if invalid
 */

/**
 * Check if value is empty (null, undefined, empty string, or whitespace only)
 * @param {*} value - Value to check
 * @returns {boolean} True if empty
 */
export const isEmpty = (value) => {
  if (value === null || value === undefined) return true;
  if (typeof value === 'string') return value.trim().length === 0;
  if (Array.isArray(value)) return value.length === 0;
  return false;
};

/**
 * Validate required field
 * @param {*} value - Value to validate
 * @param {string} fieldName - Name of the field for error message
 * @returns {ValidationResult} Validation result
 */
export const validateRequired = (value, fieldName = 'Field') => {
  const isValid = !isEmpty(value);
  return {
    isValid,
    message: isValid ? '' : `${fieldName} is required`
  };
};

/**
 * Validate string field
 * @param {string} value - String value to validate
 * @param {Object} options - Validation options
 * @param {number} options.minLength - Minimum length
 * @param {number} options.maxLength - Maximum length
 * @param {boolean} options.required - Whether field is required
 * @param {string} options.fieldName - Field name for error messages
 * @returns {ValidationResult} Validation result
 */
export const validateString = (value, options = {}) => {
  const {
    minLength = 1,
    maxLength = 255,
    required = false,
    fieldName = 'String field'
  } = options;

  // Check required
  if (required) {
    const requiredResult = validateRequired(value, fieldName);
    if (!requiredResult.isValid) return requiredResult;
  }

  // If not required and empty, it's valid
  if (!required && isEmpty(value)) {
    return { isValid: true, message: '' };
  }

  // Validate length
  const stringValue = String(value).trim();
  
  if (stringValue.length < minLength) {
    return {
      isValid: false,
      message: `${fieldName} must be at least ${minLength} characters long`
    };
  }

  if (stringValue.length > maxLength) {
    return {
      isValid: false,
      message: `${fieldName} must not exceed ${maxLength} characters`
    };
  }

  return { isValid: true, message: '' };
};

/**
 * Validate numeric field (double)
 * @param {*} value - Numeric value to validate
 * @param {Object} options - Validation options
 * @param {number} options.min - Minimum value
 * @param {number} options.max - Maximum value
 * @param {boolean} options.required - Whether field is required
 * @param {string} options.fieldName - Field name for error messages
 * @returns {ValidationResult} Validation result
 */
export const validateNumber = (value, options = {}) => {
  const {
    min = Number.MIN_SAFE_INTEGER,
    max = Number.MAX_SAFE_INTEGER,
    required = false,
    fieldName = 'Numeric field'
  } = options;

  // Check required
  if (required) {
    const requiredResult = validateRequired(value, fieldName);
    if (!requiredResult.isValid) return requiredResult;
  }

  // If not required and empty, it's valid
  if (!required && isEmpty(value)) {
    return { isValid: true, message: '' };
  }

  // Convert to number
  const numValue = Number(value);

  // Check if it's a valid number
  if (isNaN(numValue)) {
    return {
      isValid: false,
      message: `${fieldName} must be a valid number`
    };
  }

  // Check range
  if (numValue < min) {
    return {
      isValid: false,
      message: `${fieldName} must be at least ${min}`
    };
  }

  if (numValue > max) {
    return {
      isValid: false,
      message: `${fieldName} must not exceed ${max}`
    };
  }

  return { isValid: true, message: '' };
};

/**
 * Validate integer field
 * @param {*} value - Integer value to validate
 * @param {Object} options - Validation options
 * @param {number} options.min - Minimum value
 * @param {number} options.max - Maximum value
 * @param {boolean} options.required - Whether field is required
 * @param {string} options.fieldName - Field name for error messages
 * @returns {ValidationResult} Validation result
 */
export const validateInteger = (value, options = {}) => {
  const {
    min = Number.MIN_SAFE_INTEGER,
    max = Number.MAX_SAFE_INTEGER,
    required = false,
    fieldName = 'Integer field'
  } = options;

  // Check required
  if (required) {
    const requiredResult = validateRequired(value, fieldName);
    if (!requiredResult.isValid) return requiredResult;
  }

  // If not required and empty, it's valid
  if (!required && isEmpty(value)) {
    return { isValid: true, message: '' };
  }

  // Convert to number
  const numValue = Number(value);

  // Check if it's a valid number
  if (isNaN(numValue)) {
    return {
      isValid: false,
      message: `${fieldName} должно быть целым числом`
    };
  }

  // Check if it's an integer
  if (!Number.isInteger(numValue)) {
    return {
      isValid: false,
      message: `${fieldName} должно быть целым числом`
    };
  }

  // Check range
  if (numValue < min) {
    return {
      isValid: false,
      message: `${fieldName} должно быть не менее ${min}`
    };
  }

  if (numValue > max) {
    return {
      isValid: false,
      message: `${fieldName} не должно превышать ${max}`
    };
  }

  return { isValid: true, message: '' };
};

/**
 * Validate date field
 * @param {*} value - Date value to validate
 * @param {Object} options - Validation options
 * @param {Date|string} options.minDate - Minimum date
 * @param {Date|string} options.maxDate - Maximum date
 * @param {boolean} options.required - Whether field is required
 * @param {string} options.fieldName - Field name for error messages
 * @returns {ValidationResult} Validation result
 */
export const validateDate = (value, options = {}) => {
  const {
    minDate,
    maxDate,
    required = false,
    fieldName = 'Date field'
  } = options;

  // Check required
  if (required) {
    const requiredResult = validateRequired(value, fieldName);
    if (!requiredResult.isValid) return requiredResult;
  }

  // If not required and empty, it's valid
  if (!required && isEmpty(value)) {
    return { isValid: true, message: '' };
  }

  // Try to parse date
  const dateValue = new Date(value);

  // Check if it's a valid date
  if (isNaN(dateValue.getTime())) {
    return {
      isValid: false,
      message: `${fieldName} must be a valid date`
    };
  }

  // Check minimum date
  if (minDate) {
    const minDateValue = new Date(minDate);
    if (dateValue < minDateValue) {
      return {
        isValid: false,
        message: `${fieldName} must be after ${minDateValue.toLocaleDateString()}`
      };
    }
  }

  // Check maximum date
  if (maxDate) {
    const maxDateValue = new Date(maxDate);
    if (dateValue > maxDateValue) {
      return {
        isValid: false,
        message: `${fieldName} must be before ${maxDateValue.toLocaleDateString()}`
      };
    }
  }

  return { isValid: true, message: '' };
};

/**
 * Validate file (image) field
 * @param {File} file - File to validate
 * @param {Object} options - Validation options
 * @param {boolean} options.required - Whether file is required
 * @param {string} options.fieldName - Field name for error messages
 * @returns {ValidationResult} Validation result
 */
export const validateFile = (file, options = {}) => {
  const {
    required = false,
    fieldName = 'File'
  } = options;

  // Check required
  if (required && !file) {
    return {
      isValid: false,
      message: `${fieldName} is required`
    };
  }

  // If not required and no file, it's valid
  if (!required && !file) {
    return { isValid: true, message: '' };
  }

  // Validate file size
  if (file.size > API_CONFIG.IMAGE.MAX_SIZE) {
    const maxSizeMB = API_CONFIG.IMAGE.MAX_SIZE / (1024 * 1024);
    return {
      isValid: false,
      message: `${fieldName} size must not exceed ${maxSizeMB}MB`
    };
  }

  // Validate file type
  if (!API_CONFIG.IMAGE.ALLOWED_TYPES.includes(file.type)) {
    return {
      isValid: false,
      message: `${fieldName} must be one of: ${API_CONFIG.IMAGE.ALLOWED_TYPES.join(', ')}`
    };
  }

  return { isValid: true, message: '' };
};

/**
 * Validate attribute value based on attribute definition
 * @param {*} value - Value to validate
 * @param {Object} attribute - Attribute definition
 * @param {string} attribute.type - Attribute type (string, double, integer, date)
 * @param {boolean} attribute.required - Whether attribute is required
 * @param {string} attribute.nameRus - Russian name for error messages
 * @returns {ValidationResult} Validation result
 */
export const validateAttributeValue = (value, attribute) => {
  const { type, required, nameRus } = attribute;
  const fieldName = nameRus || 'Attribute';

  switch (type) {
    case 'string':
      return validateString(value, { required, fieldName });
    case 'double':
      // Allow negative values for double fields (like diopters)
      return validateNumber(value, { required, fieldName });
    case 'integer':
      return validateInteger(value, { required, fieldName, min: 0 });
    case 'date':
      return validateDate(value, { required, fieldName });
    default:
      return { isValid: true, message: '' };
  }
};

/**
 * Validate multiple attribute values
 * @param {Array} values - Array of values to validate
 * @param {Object} attribute - Attribute definition
 * @returns {ValidationResult} Validation result
 */
export const validateMultipleAttributeValues = (values, attribute) => {
  const { required, nameRus } = attribute;
  const fieldName = nameRus || 'Attribute';

  // Ensure values is an array
  const valuesArray = Array.isArray(values) ? values : [values].filter(v => v !== undefined);

  // Check if at least one value is provided for required attributes
  if (required && (!valuesArray || valuesArray.length === 0 || valuesArray.every(v => isEmpty(v)))) {
    return {
      isValid: false,
      message: `At least one ${fieldName} value is required`
    };
  }

  // Validate each non-empty value
  for (let i = 0; i < valuesArray.length; i++) {
    const value = valuesArray[i];
    if (!isEmpty(value)) {
      const result = validateAttributeValue(value, { ...attribute, required: false });
      if (!result.isValid) {
        return {
          isValid: false,
          message: `${fieldName} (item ${i + 1}): ${result.message}`
        };
      }
    }
  }

  return { isValid: true, message: '' };
};

/**
 * Validate entire product form
 * @param {Object} formData - Form data to validate
 * @param {string} formData.name - Product name
 * @param {Object} formData.category - Selected category
 * @param {Object} formData.dynamicFields - Dynamic attribute values
 * @param {File} formData.imageFile - Image file
 * @param {boolean} isEdit - Whether this is an edit form (image not required)
 * @returns {Object} Validation results for each field
 */
export const validateProductForm = (formData, isEdit = false) => {
  const { name, quantity, category, dynamicFields, imageFile } = formData;
  const errors = {};
  
  console.log('🔍 Validating form data:', formData);
  console.log('🔍 Dynamic fields for validation:', dynamicFields);

  // Validate product name
  const nameResult = validateString(name, {
    required: true,
    minLength: 1,
    maxLength: 255,
    fieldName: 'Product name'
  });
  if (!nameResult.isValid) {
    errors.name = nameResult.message;
  }

  // Validate quantity
  const quantityResult = validateInteger(quantity, {
    required: false,
    min: 0,
    max: 999999,
    fieldName: 'Quantity'
  });
  if (!quantityResult.isValid) {
    errors.quantity = quantityResult.message;
  }

  // Validate category
  if (!category) {
    errors.category = 'Category is required';
  }

  // Validate image (required for new products, optional for edits)
  const imageResult = validateFile(imageFile, {
    required: !isEdit,
    fieldName: 'Product image'
  });
  if (!imageResult.isValid) {
    errors.image = imageResult.message;
  }

  // Validate dynamic attributes
  if (category && category.attributes && dynamicFields) {
    console.log('🔍 Validating attributes for category:', category.name);
    category.attributes.forEach(attribute => {
      const fieldName = attribute.name;
      const fieldValue = dynamicFields[fieldName];
      
      console.log(`🔍 Validating attribute ${fieldName}:`, {
        value: fieldValue,
        required: attribute.required,
        type: attribute.type
      });

      // Skip validation if field value is undefined (not yet loaded)
      if (fieldValue === undefined) {
        console.log(`🔍 Skipping ${fieldName} - undefined`);
        return;
      }

      let validationResult;
      if (attribute.multiple) {
        validationResult = validateMultipleAttributeValues(fieldValue, attribute);
      } else {
        validationResult = validateAttributeValue(fieldValue, attribute);
      }

      if (!validationResult.isValid) {
        console.log(`🔍 Validation failed for ${fieldName}:`, validationResult.message);
        errors[fieldName] = validationResult.message;
      } else {
        console.log(`🔍 Validation passed for ${fieldName}`);
      }
    });
  }

  console.log('🔍 Final validation result:', {
    isValid: Object.keys(errors).length === 0,
    errors
  });
  
  return {
    isValid: Object.keys(errors).length === 0,
    errors
  };
}; 