/**
 * Utility function to extract error message from API errors
 */
export const getErrorMessage = (err: any): string => {
  if (!err) {
    return 'An unexpected error occurred'
  }

  // If response.data exists
  if (err.response?.data) {
    const data = err.response.data
    
    // If it's already a string, return it
    if (typeof data === 'string') {
      return data
    }
    
    // If it's an object with a message property
    if (data.message) {
      return data.message
    }
    
    // If it's an object with an error property
    if (data.error) {
      return data.error
    }
    
    // If it's an object, try to stringify (fallback)
    return JSON.stringify(data)
  }
  
  // If there's a message property directly
  if (err.message) {
    return err.message
  }
  
  // Fallback
  return 'An unexpected error occurred'
}




