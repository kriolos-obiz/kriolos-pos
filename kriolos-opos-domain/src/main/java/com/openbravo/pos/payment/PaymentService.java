package com.openbravo.pos.payment;

/**
 * Service interface for Payment operations.
 * Extracts business logic from UI components (JPaymentSelect).
 */
public interface PaymentService {

    /**
     * Create a new empty payment list.
     * 
     * @return New PaymentInfoList instance
     */
    PaymentInfoList createPaymentList();

    /**
     * Add a payment to the list.
     * 
     * @param list    The list to modify
     * @param payment The payment to add
     */
    void addPayment(PaymentInfoList list, PaymentInfo payment);

    /**
     * Remove the last payment from the list.
     * 
     * @param list The list to modify
     */
    void removeLastPayment(PaymentInfoList list);

    /**
     * Get the total amount of payments in the list.
     * 
     * @param list The payment list
     * @return Total amount
     */
    double getTotal(PaymentInfoList list);

    /**
     * Get the total paid amount (including change/overpayment).
     * 
     * @param list The payment list
     * @return Total paid
     */
    double getPaidTotal(PaymentInfoList list);

    /**
     * Calculate remaining amount to be paid.
     * 
     * @param list             The payment list
     * @param transactionTotal The total expected amount
     * @return Remaining amount (positive) or 0/negative if paid
     */
    double calculateRemaining(PaymentInfoList list, double transactionTotal);

    /**
     * Check if the transaction is fully paid.
     * 
     * @param list             The payment list
     * @param transactionTotal The total expected amount
     * @return true if paid >= total
     */
    boolean isFullyPaid(PaymentInfoList list, double transactionTotal);
}
