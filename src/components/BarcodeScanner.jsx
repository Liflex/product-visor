import React, { useState, useEffect, useRef } from 'react';

/**
 * Barcode Scanner Component for specialized barcode scanners (like VMC BSX Lm)
 * Works with keyboard-based scanners that emulate keyboard input
 */
const BarcodeScanner = ({ onScan, onClose, isOpen }) => {
  const [scannedCode, setScannedCode] = useState('');
  const [isListening, setIsListening] = useState(false);
  const [error, setError] = useState('');
  const inputRef = useRef(null);

  /**
   * Start listening for barcode input when modal opens
   */
  useEffect(() => {
    if (isOpen) {
      setIsListening(true);
      setError('');
      setScannedCode('');
      
      // Focus on input after a short delay to ensure modal is rendered
      setTimeout(() => {
        if (inputRef.current) {
          inputRef.current.focus();
        }
      }, 100);
    } else {
      setIsListening(false);
    }
  }, [isOpen]);

  /**
   * Handle barcode input from scanner
   */
  const handleBarcodeInput = (e) => {
    const value = e.target.value;
    setScannedCode(value);
    
    console.log('🔍 Modal Scanner Input:', {
      value: value,
      length: value.length,
      timestamp: Date.now(),
      event: {
        key: e.nativeEvent?.key,
        keyCode: e.nativeEvent?.keyCode,
        code: e.nativeEvent?.code,
        which: e.nativeEvent?.which,
        charCode: e.nativeEvent?.charCode,
        isComposing: e.nativeEvent?.isComposing,
        repeat: e.nativeEvent?.repeat,
        ctrlKey: e.nativeEvent?.ctrlKey,
        altKey: e.nativeEvent?.altKey,
        shiftKey: e.nativeEvent?.shiftKey,
        metaKey: e.nativeEvent?.metaKey,
        location: e.nativeEvent?.location
      }
    });
    
    // Most barcode scanners send Enter key after the code
    // We'll handle this in onKeyDown
  };

  /**
   * Handle key press events
   */
  const handleKeyDown = (e) => {
    console.log('🔍 Modal Key Down:', {
      key: e.key,
      keyCode: e.keyCode,
      code: e.code,
      which: e.which,
      charCode: e.charCode,
      isComposing: e.isComposing,
      repeat: e.repeat,
      ctrlKey: e.ctrlKey,
      altKey: e.altKey,
      shiftKey: e.shiftKey,
      metaKey: e.metaKey,
      location: e.location,
      timestamp: Date.now(),
      scannedCode: scannedCode
    });
    
    if (e.key === 'Enter' && scannedCode.trim()) {
      e.preventDefault();
      console.log('✅ Modal Scanner - Processing barcode:', scannedCode.trim());
      onScan(scannedCode.trim());
      setScannedCode('');
    }
  };

  /**
   * Handle manual submit
   */
  const handleManualSubmit = () => {
    if (scannedCode.trim()) {
      onScan(scannedCode.trim());
      setScannedCode('');
    }
  };

  /**
   * Handle manual input change
   */
  const handleManualInput = (e) => {
    const value = e.target.value;
    setScannedCode(value);
    
    console.log('🔍 Modal Manual Input:', {
      value: value,
      length: value.length,
      timestamp: Date.now(),
      event: {
        key: e.nativeEvent?.key,
        keyCode: e.nativeEvent?.keyCode,
        code: e.nativeEvent?.code,
        which: e.nativeEvent?.which,
        charCode: e.nativeEvent?.charCode,
        isComposing: e.nativeEvent?.isComposing,
        repeat: e.nativeEvent?.repeat,
        ctrlKey: e.nativeEvent?.ctrlKey,
        altKey: e.nativeEvent?.altKey,
        shiftKey: e.nativeEvent?.shiftKey,
        metaKey: e.nativeEvent?.metaKey,
        location: e.nativeEvent?.location
      }
    });
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-75 flex items-center justify-center z-50">
      <div className="bg-gray-800 rounded-lg p-6 max-w-md w-full mx-4">
        <div className="flex justify-between items-center mb-4">
          <h3 className="text-xl font-semibold text-white">Scan Barcode</h3>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-white transition-colors"
          >
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        {/* Scanner Status */}
        <div className="mb-4 p-3 bg-blue-900 border border-blue-700 rounded-md">
          <div className="flex items-center space-x-2">
            <div className={`w-3 h-3 rounded-full ${isListening ? 'bg-green-500 animate-pulse' : 'bg-red-500'}`}></div>
            <p className="text-blue-300 text-sm">
              {isListening ? 'Scanner ready - Point scanner at barcode' : 'Scanner not ready'}
            </p>
          </div>
        </div>

        {/* Hidden input for scanner */}
        <input
          ref={inputRef}
          type="text"
          value={scannedCode}
          onChange={handleBarcodeInput}
          onKeyDown={handleKeyDown}
          className="absolute opacity-0 pointer-events-none"
          autoFocus
        />

        {/* Manual Input */}
        <div className="mb-4">
          <label className="block text-sm font-medium text-gray-300 mb-2">
            Scanned barcode or manual input:
          </label>
          <div className="flex space-x-2">
            <input
              type="text"
              value={scannedCode}
              onChange={handleManualInput}
              onKeyDown={handleKeyDown}
              placeholder="Barcode will appear here when scanned..."
              className="flex-1 px-3 py-2 bg-gray-700 border border-gray-600 rounded-md text-white placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-indigo-500"
            />
            <button
              onClick={handleManualSubmit}
              disabled={!scannedCode.trim()}
              className="px-4 py-2 bg-indigo-600 text-white rounded-md hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
            >
              Add
            </button>
          </div>
        </div>

        {/* Error Message */}
        {error && (
          <div className="mb-4 p-3 bg-red-900 border border-red-700 rounded-md">
            <p className="text-red-300 text-sm">{error}</p>
          </div>
        )}

        {/* Action Buttons */}
        <div className="flex space-x-3">
          <button
            onClick={onClose}
            className="flex-1 px-4 py-2 border border-gray-600 text-gray-300 rounded-md hover:bg-gray-700 transition-colors"
          >
            Cancel
          </button>
          {scannedCode && (
            <button
              onClick={() => {
                onScan(scannedCode);
                setScannedCode('');
              }}
              className="flex-1 px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 transition-colors"
            >
              Use Code
            </button>
          )}
        </div>

        {/* Instructions */}
        <div className="mt-4 p-3 bg-gray-700 rounded-md">
          <p className="text-gray-300 text-sm">
            <strong>Instructions for VMC BSX Lm Scanner:</strong>
          </p>
          <ul className="text-gray-400 text-xs mt-1 space-y-1">
            <li>• Point the scanner at the barcode</li>
            <li>• Pull the trigger to scan</li>
            <li>• The barcode will automatically appear in the field</li>
            <li>• Press Enter or click "Use Code" to confirm</li>
            <li>• Or manually type the barcode and press Enter</li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default BarcodeScanner; 