package com.conectaai.service.webhook;

import com.conectaai.enums.Provider;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class WebhookPayloadNormalizer {

    private static final String FIELD_EXTERNAL_REFERENCE = "externalReference";
    private static final String TARGET_TYPE_SUBSCRIPTION = "subscription";
    private static final String TARGET_TYPE_PAYMENT = "payment";

    private final ObjectMapper objectMapper;

    public NormalizedWebhookData normalize(Provider provider, String eventType, String rawPayload) {
        try {
            JsonNode payload = objectMapper.readTree(rawPayload);
            return switch (provider) {
                case STRIPE -> normalizeStripe(eventType, payload, rawPayload);
                case MERCADO_PAGO -> normalizeMercadoPago(eventType, payload, rawPayload);
                case ASAAS -> normalizeAsaas(eventType, payload, rawPayload);
            };
        } catch (Exception e) {
            return createFallback(provider, eventType, rawPayload);
        }
    }

    private NormalizedWebhookData normalizeStripe(String eventType, JsonNode payload, String rawPayload) {
        String targetType = inferTargetTypeStripe(eventType, payload);
        String eventId = extractStripeEventId(payload);
        OffsetDateTime occurredAt = extractTimestamp(payload, "created");
        String providerEntityId = extractProviderEntityIdStripe(targetType, payload);
        String externalId = extractStripeExternalId(payload);
        String finalEventId = eventId != null 
                ? eventId 
                : generateEventId(Provider.STRIPE, eventType, rawPayload);
        OffsetDateTime finalOccurredAt = occurredAt != null ? occurredAt : OffsetDateTime.now();

        return new NormalizedWebhookData(
                targetType,
                eventType,
                finalEventId,
                finalOccurredAt,
                providerEntityId,
                externalId,
                rawPayload
        );
    }

    private String extractStripeEventId(JsonNode payload) {
        String eventId = extractString(payload, "id");
        if (eventId != null) {
            return eventId;
        }
        JsonNode objectIdNode = payload.at("/data/object/id");
        boolean isValidNode = objectIdNode != null && !objectIdNode.isMissingNode();
        return isValidNode ? extractString(objectIdNode, "id") : null;
    }

    private String extractStripeExternalId(JsonNode payload) {
        JsonNode metadataNode = payload.at("/data/object/metadata/external_id");
        boolean isValidMetadata = metadataNode != null && !metadataNode.isMissingNode();
        String externalId = isValidMetadata ? extractString(metadataNode, "external_id") : null;
        if (externalId != null) {
            return externalId;
        }
        JsonNode externalIdNode = payload.at("/data/object/external_id");
        boolean isValidExtId = externalIdNode != null && !externalIdNode.isMissingNode();
        return isValidExtId ? extractString(externalIdNode, "external_id") : null;
    }

    private NormalizedWebhookData normalizeMercadoPago(String eventType, JsonNode payload, String rawPayload) {
        String normalizedEventType = normalizeMercadoPagoEventType(eventType, payload);
        String targetType = inferTargetTypeMercadoPago(normalizedEventType, payload);
        String eventId = extractMercadoPagoEventId(payload);
        OffsetDateTime occurredAt = extractMercadoPagoTimestamp(payload);
        String providerEntityId = extractMercadoPagoProviderEntityId(payload);
        String externalId = extractMercadoPagoExternalId(payload);
        String finalEventId = eventId != null 
                ? eventId 
                : generateEventId(Provider.MERCADO_PAGO, normalizedEventType, rawPayload);
        OffsetDateTime finalOccurredAt = occurredAt != null ? occurredAt : OffsetDateTime.now();

        return new NormalizedWebhookData(
                targetType,
                normalizedEventType,
                finalEventId,
                finalOccurredAt,
                providerEntityId,
                externalId,
                rawPayload
        );
    }

    private String normalizeMercadoPagoEventType(String eventType, JsonNode payload) {
        String normalizedEventType = extractString(payload, "action");
        if (normalizedEventType != null) {
            return normalizedEventType;
        }
        normalizedEventType = extractString(payload, "type");
        return normalizedEventType != null ? normalizedEventType : eventType;
    }

    private String extractMercadoPagoEventId(JsonNode payload) {
        String eventId = extractString(payload, "id");
        return eventId != null ? eventId : extractString(payload, "_id");
    }

    private OffsetDateTime extractMercadoPagoTimestamp(JsonNode payload) {
        OffsetDateTime occurredAt = extractTimestamp(payload, "date_created");
        return occurredAt != null ? occurredAt : extractTimestamp(payload, "created");
    }

    private String extractMercadoPagoProviderEntityId(JsonNode payload) {
        String providerEntityId = extractString(payload, "data/id");
        return providerEntityId != null ? providerEntityId : extractString(payload, "id");
    }

    private String extractMercadoPagoExternalId(JsonNode payload) {
        JsonNode externalRefNode = payload.at("/data/external_reference");
        boolean isValidRef = externalRefNode != null && !externalRefNode.isMissingNode();
        String externalId = isValidRef ? extractString(externalRefNode, "external_reference") : null;
        return externalId != null ? externalId : extractString(payload, "external_reference");
    }

    private NormalizedWebhookData normalizeAsaas(String eventType, JsonNode payload, String rawPayload) {
        String targetType = inferTargetTypeAsaas(eventType, payload);
        String eventId = extractAsaasEventId(payload);
        OffsetDateTime occurredAt = extractAsaasTimestamp(payload);
        String providerEntityId = extractProviderEntityIdAsaas(targetType, payload);
        String externalId = extractAsaasExternalId(payload);
        String finalEventId = eventId != null 
                ? eventId 
                : generateEventId(Provider.ASAAS, eventType, rawPayload);
        OffsetDateTime finalOccurredAt = occurredAt != null ? occurredAt : OffsetDateTime.now();

        return new NormalizedWebhookData(
                targetType,
                eventType,
                finalEventId,
                finalOccurredAt,
                providerEntityId,
                externalId,
                rawPayload
        );
    }

    private String extractAsaasEventId(JsonNode payload) {
        String eventId = extractString(payload, "event/id");
        if (eventId != null) {
            return eventId;
        }
        eventId = extractString(payload, "id");
        return eventId;
    }

    private OffsetDateTime extractAsaasTimestamp(JsonNode payload) {
        OffsetDateTime occurredAt = extractTimestamp(payload, "event/date");
        if (occurredAt != null) {
            return occurredAt;
        }
        occurredAt = extractTimestamp(payload, "dateCreated");
        if (occurredAt != null) {
            return occurredAt;
        }
        return extractTimestamp(payload, "date");
    }

    private String extractAsaasExternalId(JsonNode payload) {
        String externalId = extractAsaasExternalIdFromPayment(payload);
        if (externalId != null) {
            return externalId;
        }
        externalId = extractAsaasExternalIdFromSubscription(payload);
        return externalId != null ? externalId : extractString(payload, FIELD_EXTERNAL_REFERENCE);
    }

    private String extractAsaasExternalIdFromPayment(JsonNode payload) {
        JsonNode paymentExtRefNode = payload.at("/payment/externalReference");
        boolean isValidPaymentRef = paymentExtRefNode != null && !paymentExtRefNode.isMissingNode();
        return isValidPaymentRef 
                ? extractString(paymentExtRefNode, FIELD_EXTERNAL_REFERENCE) : null;
    }

    private String extractAsaasExternalIdFromSubscription(JsonNode payload) {
        JsonNode subExtRefNode = payload.at("/subscription/externalReference");
        boolean isValidSubRef = subExtRefNode != null && !subExtRefNode.isMissingNode();
        return isValidSubRef 
                ? extractString(subExtRefNode, FIELD_EXTERNAL_REFERENCE) : null;
    }

    private String inferTargetTypeStripe(String eventType, JsonNode payload) {
        String e = eventType != null ? eventType.toLowerCase(Locale.ROOT) : "";
        if (e.contains(TARGET_TYPE_SUBSCRIPTION) || e.contains("invoice")) {
            return TARGET_TYPE_SUBSCRIPTION;
        }
        if (e.contains("installment")) {
            return "installment";
        }
        JsonNode object = payload.at("/data/object");
        if (object.has("object")) {
            String objectType = extractString(object, "object");
            if (TARGET_TYPE_SUBSCRIPTION.equals(objectType) || "invoice".equals(objectType)) {
                return TARGET_TYPE_SUBSCRIPTION;
            }
        }
        return TARGET_TYPE_PAYMENT;
    }

    private String inferTargetTypeMercadoPago(String eventType, JsonNode payload) {
        String e = eventType != null ? eventType.toLowerCase(Locale.ROOT) : "";
        if (e.contains(TARGET_TYPE_SUBSCRIPTION) || e.contains("preapproval")) {
            return TARGET_TYPE_SUBSCRIPTION;
        }
        JsonNode data = payload.get("data");
        if (data != null && data.has("type")) {
            String type = extractString(data, "type");
            if (type != null && (type.contains(TARGET_TYPE_SUBSCRIPTION) || type.contains("preapproval"))) {
                return TARGET_TYPE_SUBSCRIPTION;
            }
        }
        return TARGET_TYPE_PAYMENT;
    }

    private String inferTargetTypeAsaas(String eventType, JsonNode payload) {
        String e = eventType != null ? eventType.toUpperCase(Locale.ROOT) : "";
        if (e.contains("SUBSCRIPTION")) {
            return TARGET_TYPE_SUBSCRIPTION;
        }
        if (payload.has(TARGET_TYPE_SUBSCRIPTION) && payload.get(TARGET_TYPE_SUBSCRIPTION).has("id")) {
            return TARGET_TYPE_SUBSCRIPTION;
        }
        if (payload.has(TARGET_TYPE_PAYMENT) && payload.get(TARGET_TYPE_PAYMENT).has("id")) {
            return TARGET_TYPE_PAYMENT;
        }
        return TARGET_TYPE_PAYMENT;
    }

    private String extractProviderEntityIdStripe(String targetType, JsonNode payload) {
        JsonNode object = payload.at("/data/object");
        if (TARGET_TYPE_SUBSCRIPTION.equals(targetType)) {
            String subId = extractString(object, TARGET_TYPE_SUBSCRIPTION);
            if (subId != null) {
                return subId;
            }
            return extractString(object, "id");
        }
        return extractString(object, "id");
    }

    private String extractProviderEntityIdAsaas(String targetType, JsonNode payload) {
        if (TARGET_TYPE_SUBSCRIPTION.equals(targetType)) {
            JsonNode subIdNode = payload.at("/subscription/id");
            boolean isValidSubId = subIdNode != null && !subIdNode.isMissingNode();
            String subId = isValidSubId ? extractString(subIdNode, "id") : null;
            if (subId != null) {
                return subId;
            }
        }
        JsonNode paymentIdNode = payload.at("/payment/id");
        boolean isValidPaymentId = paymentIdNode != null && !paymentIdNode.isMissingNode();
        return isValidPaymentId ? extractString(paymentIdNode, "id") : null;
    }

    private String extractString(JsonNode node, String field) {
        if (node == null || node.isMissingNode()) {
            return null;
        }
        JsonNode fieldNode = node.get(field);
        if (fieldNode != null && fieldNode.isTextual()) {
            return fieldNode.asText();
        }
        if (fieldNode != null && !fieldNode.isNull()) {
            return fieldNode.asText();
        }
        return null;
    }

    private OffsetDateTime extractTimestamp(JsonNode node, String field) {
        if (node == null || node.isMissingNode()) {
            return null;
        }
        JsonNode fieldNode = node.get(field);
        if (fieldNode == null) {
            return null;
        }
        if (fieldNode.isNumber()) {
            long timestamp = fieldNode.asLong();
            if (timestamp > 1000000000000L) {
                return OffsetDateTime.ofInstant(
                        java.time.Instant.ofEpochMilli(timestamp),
                        java.time.ZoneId.systemDefault()
                );
            }
            return OffsetDateTime.ofInstant(
                    java.time.Instant.ofEpochSecond(timestamp),
                    java.time.ZoneId.systemDefault()
            );
        }
        if (fieldNode.isTextual()) {
            String dateStr = fieldNode.asText();
            try {
                return OffsetDateTime.parse(dateStr);
            } catch (Exception e) {
                try {
                    return OffsetDateTime.parse(dateStr, DateTimeFormatter.ISO_DATE_TIME);
                } catch (Exception ex) {
                    try {
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        return java.time.LocalDateTime.parse(dateStr, formatter)
                                .atOffset(java.time.ZoneOffset.of("-03:00"));
                    } catch (Exception ex2) {
                        try {
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                            return java.time.LocalDate.parse(dateStr, formatter)
                                    .atStartOfDay()
                                    .atOffset(java.time.ZoneOffset.of("-03:00"));
                        } catch (Exception ex3) {
                            return null;
                        }
                    }
                }
            }
        }
        return null;
    }

    private String generateEventId(Provider provider, String eventType, String payload) {
        return provider.name() + ":" + eventType + ":" + Integer.toHexString(payload.hashCode());
    }

    private NormalizedWebhookData createFallback(Provider provider, String eventType, String rawPayload) {
        boolean isSubscription = eventType != null 
                && eventType.toLowerCase(Locale.ROOT).contains(TARGET_TYPE_SUBSCRIPTION);
        String targetType = isSubscription ? TARGET_TYPE_SUBSCRIPTION : TARGET_TYPE_PAYMENT;
        return new NormalizedWebhookData(
                targetType,
                eventType != null ? eventType : "unknown",
                generateEventId(provider, eventType != null ? eventType : "unknown", rawPayload),
                OffsetDateTime.now(),
                null,
                null,
                rawPayload
        );
    }

    public record NormalizedWebhookData(
            String targetType,
            String eventType,
            String eventId,
            OffsetDateTime occurredAt,
            String providerEntityId,
            String externalId,
            String rawPayload
    ) {
    }
}

