/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        // 温暖治愈色系
        warm: {
          50:  '#FFF8F0',
          100: '#FFE8D0',
          200: '#FFD4A8',
          300: '#FFBA70',
          400: '#FF9E40',
          500: '#FF7F1A',
          600: '#E0680A',
          700: '#B84E00',
          800: '#8F3B00',
          900: '#6B2B00',
        },
        dusk: {
          50:  '#F5F0EB',
          100: '#E8DED3',
          200: '#D4C3B0',
          300: '#BAA38A',
          400: '#A08464',
          500: '#876B4A',
          600: '#6E5538',
          700: '#554028',
          800: '#3D2D1B',
          900: '#261B10',
        }
      },
      fontFamily: {
        sans: ['"Noto Sans SC"', 'system-ui', 'sans-serif'],
        serif: ['"Noto Serif SC"', 'Georgia', 'serif'],
      },
      borderRadius: {
        'xl': '1rem',
        '2xl': '1.5rem',
      }
    },
  },
  plugins: [],
}
