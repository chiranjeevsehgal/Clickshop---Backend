package com.clickshop.service;

import java.util.Map;

import com.clickshop.entity.User;
import com.razorpay.RazorpayException;

public interface PaymentService {
	
	
	public Map<String, Object> createRazorpayOrder(Map<String, Object> orderRequest, User user) throws RazorpayException ;
	public boolean verifyRazorpayPayment(Map<String, String> paymentData) ;
}