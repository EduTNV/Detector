package com.projetoA3.detector.dto;

import java.math.BigDecimal;

public class TransacaoDTO {

    private BigDecimal valor;
    private String estabelecimento;
    private Long cartaoId;  
    private double latitude;
    private double longitude;
    private String ipAddress;
    private double latitudeUsuario;
    private double longitudeUsuario;
 
    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public String getEstabelecimento() {
        return estabelecimento;
    }

    public void setEstabelecimento(String estabelecimento) {
        this.estabelecimento = estabelecimento;
    }

    public Long getCartaoId() {
        return cartaoId;
    }

    public void setCartaoId(Long cartaoId) {
        this.cartaoId = cartaoId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public double getLatitudeUsuario() {
        return latitudeUsuario;
    }

    public void setLatitudeUsuario(double latitudeUsuario) {
        this.latitudeUsuario = latitudeUsuario;
    }

    public double getLongitudeUsuario() {
        return longitudeUsuario;
    }

    public void setLongitudeUsuario(double longitudeUsuario) {
        this.longitudeUsuario = longitudeUsuario;
    }

}