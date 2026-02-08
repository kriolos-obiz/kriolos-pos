package com.openbravo.pos.payment;

/**
 * Default implementation of PaymentService.
 */
public class PaymentServiceImpl implements PaymentService {

    @Override
    public PaymentInfoList createPaymentList() {
        return new PaymentInfoList();
    }

    @Override
    public void addPayment(PaymentInfoList list, PaymentInfo payment) {
        if (list != null && payment != null) {
            list.add(payment);
        }
    }

    @Override
    public void removeLastPayment(PaymentInfoList list) {
        if (list != null && !list.isEmpty()) {
            list.removeLast();
        }
    }

    @Override
    public double getTotal(PaymentInfoList list) {
        return list == null ? 0.0 : list.getTotal();
    }

    @Override
    public double getPaidTotal(PaymentInfoList list) {
        return list == null ? 0.0 : list.getPaidTotal();
    }

    @Override
    public double calculateRemaining(PaymentInfoList list, double transactionTotal) {
        double paid = getTotal(list); // Use getTotal (sum of payment amounts) not getPaidTotal (sum of tendered)?
        // JPaymentSelect uses list.getTotal() for remaining calculation.
        // m_dTotal - m_aPaymentInfo.getTotal()
        return transactionTotal - paid;
    }

    @Override
    public boolean isFullyPaid(PaymentInfoList list, double transactionTotal) {
        double remaining = calculateRemaining(list, transactionTotal);
        // Using a small epsilon for double comparison if needed, but existing code
        // likely uses simple comparison
        // In POS, usually we check if remaining <= 0 (or epsilon)
        return remaining <= 0.00001; // Epsilon just in case
    }
}
