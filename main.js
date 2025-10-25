const { app, BrowserWindow } = require('electron');
const path = require('path');

let win;

function createWindow() {
  win = new BrowserWindow({
    width: 1200,
    height: 800,
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
    }
  });

  if (process.env.NODE_ENV === 'development') {
    win.loadURL('http://localhost:4200');
    win.webContents.openDevTools();
  } else {
    // Ruta correcta para producción
    const indexPath = path.join(__dirname, 'dist', 'angular-prime-prediccion', 'browser', 'index.html');
    
    console.log('Loading from:', indexPath);
    
    win.loadFile(indexPath);
    
    // Abrir DevTools temporalmente para debug
    win.webContents.openDevTools();
  }

  win.on('closed', () => {
    win = null;
  });
}

app.whenReady().then(createWindow);

app.on('window-all-closed', () => {
  if (process.platform !== 'darwin') {
    app.quit();
  }
});

app.on('activate', () => {
  if (win === null) {
    createWindow();
  }
});