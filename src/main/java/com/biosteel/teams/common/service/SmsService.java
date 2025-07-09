package com.biosteel.teams.common.service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.model.MessageAttributeValue;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SmsService {

    @Value("${spring.application.name}")
    private String applicationName;

    private final AmazonSNS snsClient;
    private final Map<String, VerificationCode> verificationCodes = new ConcurrentHashMap<>();

    public SmsService(AmazonSNS snsClient) {
        this.snsClient = snsClient;
    }

    @Async
    public void sendVerificationCode(String phoneNumber, String code) {
        try {
            String formattedPhoneNumber = formatPhoneNumber(phoneNumber);
            log.info("Sending verification code to formatted number: {}", formattedPhoneNumber);

            verificationCodes.put(formattedPhoneNumber, new VerificationCode(code));

            Map<String, MessageAttributeValue> smsAttributes = new HashMap<>();
            smsAttributes.put("AWS.SNS.SMS.SMSType", new MessageAttributeValue()
                    .withStringValue("Transactional")
                    .withDataType("String"));

            // TOD: i18n
            PublishRequest publishRequest = new PublishRequest()
                    .withMessage(
                            code + " is your " + applicationName + " code. Do not share it with anyone.")
                    .withPhoneNumber(formattedPhoneNumber)
                    .withMessageAttributes(smsAttributes);

            PublishResult result = snsClient.publish(publishRequest);
            log.debug("Success sms send to: {} message: {}", formattedPhoneNumber, result.getMessageId());

        } catch (Exception e) {
            log.error("Failed to send verification sms to {}", phoneNumber, e);
            throw new RuntimeException("Failed to send verification sms", e);
        }
    }

    @Async
    public void sendInvitationSms(String phoneNumber, String invitationCode, String teamName) {
        try {
            String formattedPhoneNumber = formatPhoneNumber(phoneNumber);
            log.info("Sending invitation SMS to: {}", formattedPhoneNumber);

            Map<String, MessageAttributeValue> smsAttributes = new HashMap<>();
            smsAttributes.put("AWS.SNS.SMS.SMSType", new MessageAttributeValue()
                    .withStringValue("Transactional")
                    .withDataType("String"));

            PublishRequest publishRequest = new PublishRequest()
                    .withMessage(invitationCode + " is your " + teamName + " invitation code for " + applicationName
                            + ". Do not share it with anyone.")
                    .withPhoneNumber(formattedPhoneNumber)
                    .withMessageAttributes(smsAttributes);

            PublishResult result = snsClient.publish(publishRequest);
            log.debug("Success invitation SMS sent to: {} message: {}", formattedPhoneNumber, result.getMessageId());

        } catch (Exception e) {
            log.error("Failed to send invitation SMS to {}", phoneNumber, e);
            throw new RuntimeException("Failed to send invitation SMS", e);
        }
    }

    private String formatPhoneNumber(String phoneNumber) {
        // Remove any non-digit characters
        String cleaned = phoneNumber.replaceAll("[^0-9]", "");

        // If it doesn't start with +1 and it's a 10-digit number, add +1
        if (!phoneNumber.startsWith("+1") && cleaned.length() == 10) {
            return "+1" + cleaned;
        }

        // If it already has +1 or other country code, return original
        return phoneNumber;
    }
}