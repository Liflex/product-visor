/**
 * Validation tests for integer type
 */

import { validateInteger, validateAttributeValue } from './validation.js';

// Test validateInteger function
console.log('🧪 Testing validateInteger function...');

// Test valid integers
const validTests = [
  { value: '123', expected: true, description: 'Valid positive integer' },
  { value: '0', expected: true, description: 'Valid zero' },
  { value: '42', expected: true, description: 'Valid small integer' },
  { value: '999999', expected: true, description: 'Valid large integer' }
];

validTests.forEach(test => {
  const result = validateInteger(test.value, { fieldName: 'Test Field' });
  const passed = result.isValid === test.expected;
  console.log(`${passed ? '✅' : '❌'} ${test.description}: ${result.isValid ? 'PASS' : 'FAIL'}`);
  if (!passed) {
    console.log(`   Expected: ${test.expected}, Got: ${result.isValid}, Message: ${result.message}`);
  }
});

// Test invalid integers
const invalidTests = [
  { value: '123.45', expected: false, description: 'Decimal number' },
  { value: 'abc', expected: false, description: 'Non-numeric string' },
  { value: '12.34', expected: false, description: 'Decimal with integer part' },
  { value: '0.5', expected: false, description: 'Decimal less than 1' }
];

invalidTests.forEach(test => {
  const result = validateInteger(test.value, { fieldName: 'Test Field' });
  const passed = result.isValid === test.expected;
  console.log(`${passed ? '✅' : '❌'} ${test.description}: ${result.isValid ? 'FAIL' : 'PASS'}`);
  if (!passed) {
    console.log(`   Expected: ${test.expected}, Got: ${result.isValid}, Message: ${result.message}`);
  }
});

// Test validateAttributeValue with integer type
console.log('\n🧪 Testing validateAttributeValue with integer type...');

const integerAttribute = {
  type: 'integer',
  required: true,
  nameRus: 'Количество'
};

const attributeTests = [
  { value: '10', expected: true, description: 'Valid integer attribute' },
  { value: '10.5', expected: false, description: 'Invalid decimal attribute' },
  { value: '', expected: false, description: 'Empty required attribute' },
  { value: 'abc', expected: false, description: 'Invalid string attribute' }
];

attributeTests.forEach(test => {
  const result = validateAttributeValue(test.value, integerAttribute);
  const passed = result.isValid === test.expected;
  console.log(`${passed ? '✅' : '❌'} ${test.description}: ${result.isValid ? 'PASS' : 'FAIL'}`);
  if (!passed) {
    console.log(`   Expected: ${test.expected}, Got: ${result.isValid}, Message: ${result.message}`);
  }
});

console.log('\n✅ Integer validation tests completed!'); 