package com.Temur.Paypal_Demo.controller;

import com.Temur.Paypal_Demo.service.PaypalService;
import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.api.payments.RedirectUrls;
import com.paypal.base.rest.PayPalRESTException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequiredArgsConstructor
@Slf4j
public class PaypalController {
    private final PaypalService paypalService;

    @GetMapping("/")
    public String home(){
        return "index";
    }

    @PostMapping("/payment/create")
    public RedirectView createPayment(
            @RequestParam("method") String method,
            @RequestParam("amount") String amount,
            @RequestParam("currency") String currency,
            @RequestParam("description") String description
    ){
        try{
            String cancelUrl = "http://localhost:8080/payment/cancel";
            String successUrl = "http://localhost:8080/payment/success";
            Payment payment=paypalService.createPayment(
                    Double.valueOf(amount),
                    currency,
                    method,
                    "sale",
                    "Payment description",
                    cancelUrl,
                    successUrl
                    );

            for(Links links: payment.getLinks()){
                if(links.getRel().equals("approval_url")){
                    return new RedirectView(links.getHref());
                }
            }
        }catch (PayPalRESTException e){
            log.error("Error occurred:: ",e);
        }
        return new RedirectView("/payment/error");
    }
    @GetMapping("/payment/success")
    public String paymentsuccess(
        @RequestParam("paymentId") String paymentid,
        @RequestParam("payerId") String payerId
    ){
        try{
            Payment payment=paypalService.executePayment(paymentid,payerId);
            if(payment.getState().equals("approved")){
                return "PaymentSuccess";
            }
        }catch (PayPalRESTException e){
            log.error("Error occured::",e);
        }
        return "PaymentSuccess";
    }

    @GetMapping("/payment/cancel")
    public String PaymentCancel(){
        return "paymentCancel";
    }
    @GetMapping("/payment/error")
    public String PaymentError(){
        return "paymentError";
    }
}
