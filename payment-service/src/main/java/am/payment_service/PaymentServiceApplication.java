package am.payment_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/*
# Stripe API Keys (Replace with your actual keys)
# You can find these in your Stripe Dashboard: Developers -> API keys
stripe.secret.key=sk_test_YOUR_STRIPE_SECRET_KEY
stripe.publishable.key=pk_test_YOUR_STRIPE_PUBLISHABLE_KEY
stripe.webhook.secret=whsec_YOUR_STRIPE_WEBHOOK_SECRET # Only needed if you implement webhooks
*/
@SpringBootApplication
public class PaymentServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(PaymentServiceApplication.class, args);
  }
}
