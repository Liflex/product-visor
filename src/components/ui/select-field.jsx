/**
 * Reusable Select Field Component
 * Provides consistent styling and validation display for select inputs
 */

import React from 'react';

/**
 * Select field component with validation
 * @param {Object} props - Component props
 * @param {string} props.label - Field label
 * @param {Array} props.options - Array of options {value, label}
 * @param {string} props.placeholder - Placeholder text
 * @param {boolean} props.required - Whether field is required
 * @param {string} props.error - Error message
 * @param {boolean} props.hasError - Whether field has error
 * @param {Function} props.onChange - Change handler
 * @param {Function} props.onBlur - Blur handler
 * @param {string} props.value - Selected value
 * @returns {JSX.Element} Select field component
 */
const SelectField = ({
  label,
  options = [],
  placeholder = 'Select an option',
  required = false,
  error,
  hasError = false,
  onChange,
  onBlur,
  value,
  ...rest
}) => {
  const selectId = `select-${Math.random().toString(36).substr(2, 9)}`;
  
  const baseSelectClasses = `
    mt-1 block w-full pl-3 pr-10 py-2 text-base border rounded-md 
    focus:outline-none focus:ring-2 focus:ring-offset-2 sm:text-sm
    transition-colors duration-200
  `;
  
  const selectClasses = hasError
    ? `${baseSelectClasses} border-red-500 bg-red-50 text-red-900 focus:ring-red-500 focus:border-red-500`
    : `${baseSelectClasses} border-gray-700 bg-gray-700 text-white focus:ring-indigo-500 focus:border-indigo-500`;

  return (
    <div className="mb-4">
      {label && (
        <label htmlFor={selectId} className="block text-sm font-medium text-gray-300">
          {label}
          {required && <span className="text-red-500 ml-1">*</span>}
        </label>
      )}
      
      <select
        id={selectId}
        value={value}
        onChange={onChange}
        onBlur={onBlur}
        className={selectClasses}
        aria-invalid={hasError}
        aria-describedby={hasError ? `${selectId}-error` : undefined}
        {...rest}
      >
        <option value="">{placeholder}</option>
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
      
      {hasError && error && (
        <p id={`${selectId}-error`} className="mt-1 text-sm text-red-500">
          {error}
        </p>
      )}
    </div>
  );
};

export default SelectField; 