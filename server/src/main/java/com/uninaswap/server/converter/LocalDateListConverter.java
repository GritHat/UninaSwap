// Create a new file: server/src/main/java/com/uninaswap/server/converter/LocalDateListConverter.java

package com.uninaswap.server.converter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Converter
public class LocalDateListConverter implements AttributeConverter<List<LocalDate>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    static {
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public String convertToDatabaseColumn(List<LocalDate> attribute) {
        if (attribute == null || attribute.isEmpty()) {
            return "[]";
        }
        
        try {
            return objectMapper.writeValueAsString(attribute);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error converting List<LocalDate> to JSON", e);
        }
    }

    @Override
    public List<LocalDate> convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty() || "[]".equals(dbData)) {
            return new ArrayList<>();
        }
        
        try {
            return objectMapper.readValue(dbData, new TypeReference<List<LocalDate>>() {});
        } catch (Exception e) {
            throw new RuntimeException("Error converting JSON to List<LocalDate>", e);
        }
    }
}