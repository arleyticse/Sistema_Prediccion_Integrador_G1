const fs = require('fs');
const path = require('path');

console.log('🔧 Ajustando rutas para Electron...');

const indexPath = path.join(__dirname, 'dist', 'angular-prime-prediccion', 'browser', 'index.html');

if (!fs.existsSync(indexPath)) {
  console.error('❌ No se encontró index.html en:', indexPath);
  process.exit(1);
}

let html = fs.readFileSync(indexPath, 'utf8');

// Asegurar que base href sea relativo
if (!html.includes('<base href="./">')) {
  html = html.replace(/<base href="[^"]*">/, '<base href="./">');
}

// Cambiar rutas absolutas a relativas
html = html.replace(/href="\//g, 'href="./');
html = html.replace(/src="\//g, 'src="./');

fs.writeFileSync(indexPath, html);
console.log('Rutas actualizadas correctamente');