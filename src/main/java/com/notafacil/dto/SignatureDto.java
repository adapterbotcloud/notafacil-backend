package com.notafacil.dto;

public record SignatureDto(
    String signedInfo,
    String signatureValue,
    String keyInfo
) {}