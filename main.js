const { app, BrowserWindow } = require('electron');
const path = require('path');
const url = require('url');

let win;

function createWindow() {
  win = new BrowserWindow({
    width: 1200,
    height: 800,
    webPreferences: {
      nodeIntegration: false,
      contextIsolation: true,
      enableRemoteModule: false,
    }
  });

  // En desarrollo, carga desde el servidor local
  if (process.env.NODE_ENV === 'development') {
    win.loadURL('http://localhost:4200');
    win.webContents.openDevTools();
  } else {
    // IMPORTANTE: Ruta actualizada para Angular 17+
    win.loadURL(url.format({
      pathname: path.join(__dirname, 'dist/angular-prime-prediccion/browser/index.html'),
      protocol: 'file:',
      slashes: true
    }));
  }

  win.on('closed', () => {
    win = null;
  });
}

app.on('ready', createWindow);

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