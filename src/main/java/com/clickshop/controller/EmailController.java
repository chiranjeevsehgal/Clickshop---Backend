package com.clickshop.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.mailtrap.client.MailtrapClient;
import io.mailtrap.config.MailtrapConfig;
import io.mailtrap.factory.MailtrapClientFactory;
import io.mailtrap.model.request.emails.Address;
import io.mailtrap.model.request.emails.MailtrapMail;

@RestController
@RequestMapping("/email")
public class EmailController {

	@Value("${mailtrap.api.token}")
	private String mailtrapToken;

	@Value("${mailtrap.template.order-confirmation-customer}")
	private String orderTemplateUuidCustomer;

	@Value("${mailtrap.template.order-confirmation-admin}")
	private String orderTemplateUuidAdmin;

	@Value("${mailtrap.from.email}")
	private String fromEmail;

	@Value("${mailtrap.from.name}")
	private String fromName;

	@Value("${mailtrap.admin.email}")
	private String adminEmail;

	// Endpoint to send order notification to admin

	@PostMapping("/demo-email")
	public ResponseEntity<?> sendDemoNotification() {
		try {
			MailtrapClient client = MailtrapClientFactory.createMailtrapClient(
					new MailtrapConfig.Builder()
							.token(mailtrapToken)
							.build());

			// Prepare template variables from the received JSON
			Map<String, Object> variables = new HashMap<>();

			// Extract data from the request body
			variables.put("user_name", "test");
			variables.put("order_id", "1");
			variables.put("delivery_date", "2025-04-17");
			variables.put("order_total", "12000");

			// Create and send the email
			MailtrapMail mail = MailtrapMail.builder()
					.from(new Address(fromEmail, fromName))
					.to(List.of(new Address(adminEmail)))
					.templateUuid(orderTemplateUuidCustomer)
					.templateVariables(variables)
					.build();

			// Send the email
			String response = client.send(mail).toString();

			return ResponseEntity.ok(Map.of(
					"success", true,
					"message", "Order notification sent successfully",
					"details", response));

		} catch (Exception e) {
			System.out.println(e);
			return ResponseEntity.internalServerError().body(Map.of(
					"success", false,
					"message", "Failed to send order notification",
					"error", e.getMessage()));
		}
	}

	@PostMapping("/send-admin-notification")
	public ResponseEntity<?> sendOrderNotification(@RequestBody Map<String, Object> orderData) {
		try {
			MailtrapClient client = MailtrapClientFactory.createMailtrapClient(
					new MailtrapConfig.Builder()
							.token(mailtrapToken)
							.build());

			// Extract customer email from request
			Map<String, Object> user = (Map<String, Object>) orderData.get("user");
			String customerName = user != null ? (String) user.get("name") : "Customer";
			String customerEmail = user != null ? (String) user.get("email") : "";
		    String customerAddress = user != null ? (String) user.get("address") : "";
		    String customerContact = user != null ? (String) user.get("contact") : "";

			// Prepare template variables from the received JSON
			Map<String, Object> variables = new HashMap<>();

		   // Extract data from the request body
		   variables.put("order_id", orderData.get("orderId"));
		   variables.put("user_name", customerName);
		   variables.put("user_email", customerEmail);
		   variables.put("user_contact", customerContact);
		   variables.put("user_address", customerAddress);
		   java.time.LocalDate orderDate = java.time.LocalDate.now();
		   java.time.LocalDate deliveryDate = orderDate.plusDays(3);
		//    variables.put("order_date", orderDate.toString());
		   variables.put("delivery_date", deliveryDate.toString());
		   
		   // Format currency values
		   Double total = toDouble(orderData.get("total"));
		   // Double subtotal = toDouble(orderData.get("subtotal"));
		   // Double shipping = toDouble(orderData.get("shipping"));
		   // Double discount = toDouble(orderData.get("discount"));

		   variables.put("order_total", formatCurrency(total));
		   // variables.put("order_subtotal", formatCurrency(subtotal));
		   // variables.put("shipping_cost", formatCurrency(shipping));
		   // variables.put("discount", formatCurrency(discount));
		   // variables.put("shipping_address", customerAddress);

		   // Add items information if available
		   List<Map<String, Object>> items = (List<Map<String, Object>>) orderData.get("items");
		   if (items != null) {
			   variables.put("items", items);
			   variables.put("items_count", items.size());
		   }

		   // Create and send the email
		   MailtrapMail mail = MailtrapMail.builder()
				   .from(new Address(fromEmail, fromName))
				   .to(List.of(new Address(adminEmail)))
				   .templateUuid(orderTemplateUuidAdmin)
				   .templateVariables(variables)
				   .build();


			// Send the email
			String response = client.send(mail).toString();

			return ResponseEntity.ok(Map.of(
					"success", true,
					"message", "Order notification sent successfully",
					"details", response));

		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(Map.of(
					"success", false,
					"message", "Failed to send order notification",
					"error", e.getMessage()));
		}
	}

	// Endpoint to send order confirmation to customer
	@PostMapping("/send-customer-confirmation")
	public ResponseEntity<?> sendCustomerConfirmation(@RequestBody Map<String, Object> orderData) {
		try {
			MailtrapClient client = MailtrapClientFactory.createMailtrapClient(
					new MailtrapConfig.Builder()
							.token(mailtrapToken)
							.build());

			// Extract customer email from request
			 Map<String, Object> user = (Map<String, Object>) orderData.get("user");
			 String customerName = user != null ? (String) user.get("name") : "Customer";
			 String customerEmail = user != null ? (String) user.get("email") : "";
			//  String customerAddress = user != null ? (String) user.get("address") : "";
			//  String customerContact = user != null ? (String) user.get("contact") : "";
 
			 // Prepare template variables from the received JSON
			 Map<String, Object> variables = new HashMap<>();

            // Extract data from the request body
            variables.put("order_id", orderData.get("orderId"));
            variables.put("user_name", customerName);
            // variables.put("customer_email", customerEmail);
            // variables.put("customer_contact", customerContact);
			java.time.LocalDate orderDate = java.time.LocalDate.now();
            java.time.LocalDate deliveryDate = orderDate.plusDays(3);
            variables.put("order_date", orderDate.toString());
            variables.put("delivery_date", deliveryDate.toString());
            
            // Format currency values
            Double total = toDouble(orderData.get("total"));
            // Double subtotal = toDouble(orderData.get("subtotal"));
            // Double shipping = toDouble(orderData.get("shipping"));
            // Double discount = toDouble(orderData.get("discount"));

			variables.put("order_total", formatCurrency(total));
            // variables.put("order_subtotal", formatCurrency(subtotal));
            // variables.put("shipping_cost", formatCurrency(shipping));
            // variables.put("discount", formatCurrency(discount));
            // variables.put("shipping_address", customerAddress);

			// Add items information if available
            List<Map<String, Object>> items = (List<Map<String, Object>>) orderData.get("items");
            if (items != null) {
                variables.put("items", items);
                variables.put("items_count", items.size());
            }

			// Create and send the email
			MailtrapMail mail = MailtrapMail.builder()
					.from(new Address(fromEmail, fromName))
					.to(List.of(new Address(customerEmail)))
					.templateUuid(orderTemplateUuidCustomer)
					.templateVariables(variables)
					.build();

			// Send the email
			String response = client.send(mail).toString();

			return ResponseEntity.ok(Map.of(
					"success", true,
					"message", "Order confirmation sent successfully",
					"details", response));

		} catch (Exception e) {
			return ResponseEntity.internalServerError().body(Map.of(
					"success", false,
					"message", "Failed to send order confirmation",
					"error", e.getMessage()));
		}
	}

	private Double toDouble(Object value) {
        if (value == null) return 0.0;
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        try {
            return Double.parseDouble(value.toString());
        } catch (Exception e) {
            return 0.0;
        }
    }
    
    /**
     * Helper method to format currency
     */
    private String formatCurrency(Double amount) {
        
        return String.format("%.2f", amount);
    }
}