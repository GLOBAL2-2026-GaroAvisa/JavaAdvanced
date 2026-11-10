package br.com.GaroAvisa.model.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class SimNaoConverter implements AttributeConverter<Boolean, String> {

    @Override
    public String convertToDatabaseColumn(Boolean valor) {
        if (valor == null) {
            return null;
        }
        return valor ? "S" : "N";
    }

    @Override
    public Boolean convertToEntityAttribute(String valor) {
        if (valor == null) {
            return null;
        }
        return "S".equals(valor);
    }
}