/**
 * Reusable Input Field Component
 * Provides consistent styling and validation display
 */

import React from 'react';

/**
 * Input field component with validation
 * @param {Object} props - Component props
 * @param {string} props.label - Field label
 * @param {string} props.type - Input type
 * @param {string} props.placeholder - Input placeholder
 * @param {boolean} props.required - Whether field is required
 * @param {string} props.error - Error message
 * @param {boolean} props.hasError - Whether field has error
 * @param {Function} props.onChange - Change handler
 * @param {Function} props.onBlur - Blur handler
 * @param {string} props.value - Input value
 * @param {Object} props.inputProps - Additional input props
 * @returns {JSX.Element} Input field component
 */
const InputField = ({
  label,
  type = 'text',
  placeholder,
  required = false,
  error,
  hasError = false,
  onChange,
  onBlur,
  value,
  inputProps = {},
  ...rest
}) => {
  const inputId = `input-${Math.random().toString(36).substr(2, 9)}`;
  
  const baseInputClasses = `
    mt-1 block w-full px-3 py-2 border rounded-md shadow-sm 
    focus:outline-none focus:ring-2 focus:ring-offset-2 sm:text-sm
    transition-colors duration-200
  `;
  
  const inputClasses = hasError
    ? `${baseInputClasses} border-red-500 bg-red-50 text-red-900 focus:ring-red-500 focus:border-red-500`
    : `${baseInputClasses} border-gray-700 bg-gray-700 text-white focus:ring-indigo-500 focus:border-indigo-500`;

  return (
    <div className="mb-4">
      {label && (
        <label htmlFor={inputId} className="block text-sm font-medium text-gray-300">
          {label}
          {required && <span className="text-red-500 ml-1">*</span>}
        </label>
      )}
      
      <input
        id={inputId}
        type={type}
        placeholder={placeholder}
        value={value}
        onChange={onChange}
        onBlur={onBlur}
        className={inputClasses}
        aria-invalid={hasError}
        aria-describedby={hasError ? `${inputId}-error` : undefined}
        {...inputProps}
        {...rest}
      />
      
      {hasError && error && (
        <p id={`${inputId}-error`} className="mt-1 text-sm text-red-500">
          {error}
        </p>
      )}
    </div>
  );
};

export default InputField; 