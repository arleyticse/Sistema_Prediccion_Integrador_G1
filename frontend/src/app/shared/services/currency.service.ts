import { Injectable } from '@angular/core';

export interface CurrencyConfig {
  symbol: string;
  code: string;
  locale: string;
  decimals: number;
  position: 'before' | 'after';
}

@Injectable({
  providedIn: 'root'
})
export class CurrencyService {
  private config: CurrencyConfig = {
    symbol: 'S/.',
    code: 'PEN',
    locale: 'es-PE',
    decimals: 2,
    position: 'before'
  };

  getConfig(): CurrencyConfig {
    return { ...this.config };
  }

  setConfig(config: Partial<CurrencyConfig>): void {
    this.config = { ...this.config, ...config };
  }

  format(value: number | string | null | undefined): string {
    const numValue = typeof value === 'string' ? parseFloat(value) : (value || 0);
    
    if (isNaN(numValue)) {
      return `${this.config.symbol} 0.00`;
    }

    const formatted = numValue.toFixed(this.config.decimals);
    
    return this.config.position === 'before' 
      ? `${this.config.symbol} ${formatted}`
      : `${formatted} ${this.config.symbol}`;
  }

  getSymbol(): string {
    return this.config.symbol;
  }

  getCode(): string {
    return this.config.code;
  }
}
