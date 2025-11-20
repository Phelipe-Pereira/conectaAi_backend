package com.conectaai.adapter.stripe;

import com.conectaai.adapter.gateway.GatewaySubscriptionResponse;
import com.conectaai.adapter.gateway.SubscriptionGatewayAdapter;
import com.conectaai.domain.Customer;
import com.conectaai.enums.Provider;
import com.conectaai.exception.GatewayException;
import com.conectaai.logger.AppLogger;
import com.stripe.exception.StripeException;
import com.stripe.model.Price;
import com.stripe.model.Product;
import com.stripe.model.Subscription;
import com.stripe.param.PriceCreateParams;
import com.stripe.param.ProductCreateParams;
import com.stripe.param.SubscriptionCreateParams;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class StripeSubscriptionAdapter implements SubscriptionGatewayAdapter {

    private static final AppLogger LOGGER = AppLogger.getLogger(StripeSubscriptionAdapter.class);
    private static final String METHOD_CANCEL_SUBSCRIPTION = "cancelSubscription";

    @Override
    public GatewaySubscriptionResponse createSubscription(
            Customer customer,
            BigDecimal amount,
            String currency,
            String interval,
            String paymentMethod,
            String description,
            LocalDateTime startAt,
            LocalDateTime endAt,
            String externalId) {
        LOGGER.info("createSubscription", "Criando assinatura no Stripe: customer={}, amount={}", 
                customer.getExternalId(), amount);

        try {
            Product product = Product.create(ProductCreateParams.builder()
                    .setName(description != null && !description.isBlank() ? description : "Subscription")
                    .putMetadata("external_id", externalId)
                    .build());

            long amountInCents = amount.multiply(new BigDecimal("100")).longValue();
            
            PriceCreateParams.Recurring recurring = PriceCreateParams.Recurring.builder()
                    .setInterval(mapInterval(interval))
                    .build();

            Price price = Price.create(PriceCreateParams.builder()
                    .setProduct(product.getId())
                    .setCurrency(currency.toLowerCase())
                    .setUnitAmount(amountInCents)
                    .setRecurring(recurring)
                    .build());

            SubscriptionCreateParams.Builder paramsBuilder = SubscriptionCreateParams.builder()
                    .setCustomer(customer.getExternalId())
                    .addItem(SubscriptionCreateParams.Item.builder()
                            .setPrice(price.getId())
                            .build());

            if (startAt != null) {
                paramsBuilder.setBillingCycleAnchor(startAt.atZone(ZoneId.systemDefault()).toEpochSecond());
            }

            SubscriptionCreateParams params = paramsBuilder.build();
            Subscription subscription = Subscription.create(params);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("status", subscription.getStatus());
            metadata.put("current_period_start", subscription.getCurrentPeriodStart());
            metadata.put("current_period_end", subscription.getCurrentPeriodEnd());

            return new GatewaySubscriptionResponse(
                    subscription.getId(),
                    subscription.getStatus(),
                    amount,
                    currency,
                    subscription.getCurrentPeriodStart() != null 
                            ? OffsetDateTime.ofInstant(Instant.ofEpochSecond(subscription.getCurrentPeriodStart()), 
                                    ZoneId.systemDefault())
                            : null,
                    subscription.getCurrentPeriodEnd() != null 
                            ? OffsetDateTime.ofInstant(Instant.ofEpochSecond(subscription.getCurrentPeriodEnd()), 
                                    ZoneId.systemDefault())
                            : null,
                    null,
                    metadata
            );
        } catch (StripeException e) {
            LOGGER.error("createSubscription", "Erro ao criar assinatura no Stripe", e);
            throw new GatewayException(Provider.STRIPE, "Erro ao criar assinatura: " + e.getMessage(), e);
        }
    }

    @Override
    public GatewaySubscriptionResponse getSubscription(String providerSubscriptionId) {
        LOGGER.info("getSubscription", "Buscando assinatura no Stripe: id={}", providerSubscriptionId);

        try {
            Subscription subscription = Subscription.retrieve(providerSubscriptionId);

            Map<String, Object> metadata = new HashMap<>();
            metadata.put("status", subscription.getStatus());

            BigDecimal amount = BigDecimal.ZERO;
            if (subscription.getItems() != null && !subscription.getItems().getData().isEmpty()) {
                Price price = subscription.getItems().getData().get(0).getPrice();
                if (price != null) {
                    amount = BigDecimal.valueOf(price.getUnitAmount()).divide(new BigDecimal("100"));
                }
            }

            return new GatewaySubscriptionResponse(
                    subscription.getId(),
                    subscription.getStatus(),
                    amount,
                    subscription.getCurrency().toUpperCase(),
                    subscription.getCurrentPeriodStart() != null 
                            ? OffsetDateTime.ofInstant(Instant.ofEpochSecond(subscription.getCurrentPeriodStart()), 
                                    ZoneId.systemDefault())
                            : null,
                    subscription.getCurrentPeriodEnd() != null 
                            ? OffsetDateTime.ofInstant(Instant.ofEpochSecond(subscription.getCurrentPeriodEnd()), 
                                    ZoneId.systemDefault())
                            : null,
                    subscription.getCanceledAt() != null 
                            ? OffsetDateTime.ofInstant(Instant.ofEpochSecond(subscription.getCanceledAt()), 
                                    ZoneId.systemDefault())
                            : null,
                    metadata
            );
        } catch (StripeException e) {
            LOGGER.error("getSubscription", "Erro ao buscar assinatura no Stripe", e);
            throw new GatewayException(Provider.STRIPE, "Erro ao buscar assinatura: " + e.getMessage(), e);
        }
    }

    @Override
    public void cancelSubscription(String providerSubscriptionId) {
        LOGGER.info(METHOD_CANCEL_SUBSCRIPTION, "Cancelando assinatura no Stripe: id={}", providerSubscriptionId);

        try {
            Subscription subscription = Subscription.retrieve(providerSubscriptionId);
            Subscription canceled = subscription.cancel();
            LOGGER.info(METHOD_CANCEL_SUBSCRIPTION, "Assinatura cancelada: id={}, status={}", 
                    canceled.getId(), canceled.getStatus());
        } catch (StripeException e) {
            LOGGER.error(METHOD_CANCEL_SUBSCRIPTION, "Erro ao cancelar assinatura no Stripe", e);
            throw new GatewayException(Provider.STRIPE, "Erro ao cancelar assinatura: " + e.getMessage(), e);
        }
    }

    @Override
    public List<Object> listSubscriptions(String customer, String customerGroupName, String billingType, String status, String externalReference, Integer offset, Integer limit) {
        throw new IllegalArgumentException("Funcionalidade de listagem de assinaturas não está disponível para Stripe");
    }

    @Override
    public GatewaySubscriptionResponse updateSubscription(String providerSubscriptionId, Customer customer, BigDecimal amount, String currency, String interval, String paymentMethod, String description, LocalDateTime startAt, LocalDateTime endAt, String externalId) {
        throw new IllegalArgumentException("Funcionalidade de atualização de assinatura não está disponível para Stripe");
    }

    private PriceCreateParams.Recurring.Interval mapInterval(String interval) {
        return switch (interval.toUpperCase()) {
            case "DAILY" -> PriceCreateParams.Recurring.Interval.DAY;
            case "WEEKLY" -> PriceCreateParams.Recurring.Interval.WEEK;
            case "BIWEEKLY" -> PriceCreateParams.Recurring.Interval.WEEK;
            case "MONTHLY" -> PriceCreateParams.Recurring.Interval.MONTH;
            case "QUARTERLY" -> PriceCreateParams.Recurring.Interval.MONTH;
            case "SEMIANNUALLY" -> PriceCreateParams.Recurring.Interval.MONTH;
            case "YEARLY" -> PriceCreateParams.Recurring.Interval.YEAR;
            default -> PriceCreateParams.Recurring.Interval.MONTH;
        };
    }
}

