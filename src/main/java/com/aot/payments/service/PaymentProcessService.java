package com.aot.payments.service;

import com.aot.payments.model.Address;
import com.aot.payments.model.Invoice;
import com.aot.payments.model.InvoiceStatus;
import com.aot.payments.model.Payment;
import com.aot.payments.properties.PaymentConstants;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

import static com.aot.payments.dto.LineToObjectConverter.convertToInvoiceObject;
import static com.aot.payments.topic.PaymentInfoSender.sendToInvoice;

public class PaymentProcessService {

    public void showOptions() throws IOException {
        boolean repeatLoop = true;
        do {
            System.out.println("Select the Option: \n1.List All Invoices \n2.Search With Invoice Id\n");
            Scanner input = new Scanner(System.in);
            switch (Integer.parseInt(input.nextLine())) {
                case 1:
                    checkForAllInvoices();
                    break;
                case 2:
                    Scanner input1 = new Scanner(System.in);
                    System.out.println("Enter the Invoice Number:");
                    checkForSpecificInvoice(input1.nextLine());
                    break;
                default:
                    Scanner input2 = new Scanner(System.in);
                    System.out.println("Do you want to exit(y/n):");
                    if ("y".equals(input2.nextLine().toLowerCase()))
                        repeatLoop = false;
                    break;
            }



        } while (repeatLoop == true);
    }

    private void checkForAllInvoices() {
        FileUtils.listFiles(new File(PaymentConstants.paymentInboundPath),
                new String[]{"txt"}, false).forEach(file -> {
                    try {
                        String fileName = file.getName();
                        String line = FileUtils.readFileToString(file, "UTF-8");
                        Invoice invoice = convertToInvoiceObject(line, new Invoice());
                        displayToUser(fileName, invoice);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        );
    }

    private void checkForSpecificInvoice(String invoiceNumber) throws IOException {
        File file = FileUtils.getFile(PaymentConstants.paymentInboundPath, invoiceNumber + ".txt");
        if(file.exists()) {
            String fileName = file.getName();
            String line = FileUtils.readFileToString(file, "UTF-8");
            System.out.println(line);
            Invoice invoice = convertToInvoiceObject(line, new Invoice());
            displayToUser(fileName, invoice);
        } else {
            System.out.println("File Does not exist for the invoice number!");
        }
    }


    private void displayToUser(String fileName, Invoice invoice) {
        Address address = invoice.getAddress();
        System.out.println("File: " + fileName);
        System.out.println("Invoice Number:" + invoice.getInvoiceNumber());
        System.out.println("Invoice Date:" + invoice.getInvoiceDate());
        System.out.println("Item Code:" + invoice.getItemCode());
        System.out.println("Item Name:" + invoice.getItemName());
        System.out.println("Quantity:" + invoice.getQuantity());
        System.out.println("Cost:" + invoice.getCost());
        System.out.println("Address Line 1:" + address.getAddressLine1());
        System.out.println("Address Line 2:" + address.getAddressLine2());
        System.out.println("City:" + address.getCity());
        System.out.println("State:" + address.getState());
        System.out.println("Zip:" + address.getZip());
        System.out.println("Country:" + address.getCountry());
        System.out.println("Status:" + invoice.getStatus());
        System.out.println("Full Name:" + invoice.getFullName());
        System.out.println("Company:" + invoice.getCompany());
        System.out.println("Email:" + invoice.getEmail());
        System.out.println("Phone Number:" + invoice.getPhone());
        payForInvoices(invoice);
    }

    private void payForInvoices(Invoice invoice) {
        Payment payment = new Payment();
        payment.setInvoiceNumber(invoice.getInvoiceNumber());
        System.out.println("Total Amount:" + invoice.getCost());
        Scanner scanPaidAmount = new Scanner(System.in);
        System.out.println("Amount paid:");
        payment.setPaidAmount(scanPaidAmount.nextLine());
        Scanner scanPaidDate = new Scanner(System.in);
        System.out.println("Paid Date:");
        payment.setPaidDate(scanPaidDate.nextLine());
        Scanner scanPaymentMode = new Scanner(System.in);
        System.out.println("Payment Mode(CC/DC/Cheque/DD):");
        payment.setPaymentMode(scanPaymentMode.nextLine());
        Scanner scanPaidAccount = new Scanner(System.in);
        System.out.println("Payment Account:");
        payment.setPaidAccount(scanPaidAccount.nextLine());
        Scanner scanPaymentNote = new Scanner(System.in);
        System.out.println("Payment Note:");
        payment.setPaymentNote(scanPaymentNote.nextLine());
        if(Integer.parseInt(invoice.getCost()) - Integer.parseInt(payment.getPaidAmount())>0) {
            payment.setStatus(InvoiceStatus.PARTIAL.getStatus());
            payment.setBalanceAmount(String.valueOf(Integer.parseInt(invoice.getCost()) - Integer.parseInt(payment.getPaidAmount())));
        } else if(Integer.parseInt(invoice.getCost()) - Integer.parseInt(payment.getPaidAmount()) == 0) {
            payment.setStatus(InvoiceStatus.COMPLETED.getStatus());
            payment.setBalanceAmount("0");
        } else {
            payment = new Payment();
            System.out.println("Not a valid payment.");
        }

        if(payment.getStatus()!=null){
            try {
                sendToInvoice(payment.getStatus(), payment);
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }
}
