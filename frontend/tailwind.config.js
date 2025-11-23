/** Tailwind CSS configuration with custom palette */
module.exports = {
  darkMode: 'class', // Enable class-based dark mode
  content: [
    './src/**/*.{html,ts}'
  ],
  theme: {
    extend: {
      colors: {
        primary: {
          100: '#0077c2',
          200: '#59a5f5',
          300: '#c8ffff'
        },
        accent: {
          100: '#00bfff',
          200: '#00619a'
        },
        text: {
          100: '#333333',
          200: '#5c5c5c',
          // Dark mode variants
          'dark-100': '#f5f5f5',
          'dark-200': '#cccccc'
        },
        bg: {
          100: '#ffffff',
          200: '#f5f5f5',
          300: '#cccccc',
          // Dark mode variants
          'dark-100': '#0f172a', // slate-900
          'dark-200': '#1e293b', // slate-800
          'dark-300': '#334155'  // slate-700
        }
      }
    }
  },
  plugins: [
    require('tailwindcss-primeui')
  ]
};