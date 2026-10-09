//    KrOS POS
//    Copyright (c) 2019-2023 KriolOS
//    
//
//     
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with KrOS POS.  If not, see <http://www.gnu.org/licenses/>.

package com.openbravo.pos.ticket;

import com.openbravo.format.Formats;
import com.openbravo.pos.domain.utils.AmountCalculatorUtil;

/**
 *
 * @author JG uniCenta
 */
public class TicketTaxInfo {
    
    private TaxInfo tax;
    
    private double subtotal;
    private double taxtotal;
            

    public TicketTaxInfo(TaxInfo tax) {
        this.tax = tax;
        
        subtotal = 0.0;
        taxtotal = 0.0;
    }

    public TaxInfo getTaxInfo() {
        return tax;
    }

    public void add(double dValue) {
        subtotal += dValue;
        taxtotal = AmountCalculatorUtil.calcTaxAmount(subtotal, tax);
    }
    
    public String getTaxName() {       
        return tax.getName();
    }

    public double getSubTotal() {    
        return subtotal;
    }

    public double getTax() {       
        return taxtotal;
    }

    public double getTotal() {         
        return subtotal + taxtotal;
    }

    public String printTaxName() {
        return Formats.STRING.formatValue(getTaxName());
    }
    
    public String printSubTotal() {
        return Formats.CURRENCY.formatValue(getSubTotal());
    }

    public String printTax() {
        return Formats.CURRENCY.formatValue(getTax());
    }    

    public String printTotal() {
        return Formats.CURRENCY.formatValue(getTotal());
    }    
}
