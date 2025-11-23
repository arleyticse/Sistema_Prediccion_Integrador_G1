const fs = require('fs');
const { execSync } = require('child_process');
const path = require('path');

console.log(' Preparando build de Electron...\n');

// Leer package.json original
const pkg = JSON.parse(fs.readFileSync('package.json', 'utf8'));

// Crear package.json limpio solo con lo necesario para Electron
const cleanPkg = {
  name: pkg.name,
  version: pkg.version,
  description: pkg.description || 'Aplicación de predicción con Angular y PrimeNG',
  author: pkg.author || 'Tu Nombre',
  main: 'main.js',
  dependencies: {},
  devDependencies: {}
};

console.log(' Creando package.json temporal sin dependencias...');

// Backup del package.json original
fs.copyFileSync('package.json', 'package.backup.json');

// Escribir package.json limpio
fs.writeFileSync('package.json', JSON.stringify(cleanPkg, null, 2));

try {
  console.log(' Ejecutando electron-builder...\n');
  
  // Usar la ruta directa al ejecutable local para evitar problemas con npx
  const electronBuilder = path.resolve(__dirname, 'node_modules', '.bin', process.platform === 'win32' ? 'electron-builder.cmd' : 'electron-builder');
  
  execSync(`"${electronBuilder}" --win --x64`, { stdio: 'inherit' });
  console.log('\n Build completado exitosamente!');
  console.log(' El instalador está en: release/');
} catch (error) {
  console.error('\n Error durante el build:', error.message);
} finally {
  // Restaurar package.json original
  console.log('\n Restaurando package.json original...');
  fs.unlinkSync('package.json');
  fs.renameSync('package.backup.json', 'package.json');
  console.log(' package.json restaurado');
}