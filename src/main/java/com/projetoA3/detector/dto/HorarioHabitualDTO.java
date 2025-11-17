package com.projetoA3.detector.dto;

// Este DTO será usado para receber o horário do frontend
public class HorarioHabitualDTO {

    // Espera uma string no formato "HH:mm" (ex: "08:00")
    private String horarioInicio; 
    
    // Espera uma string no formato "HH:mm" (ex: "22:00")
    private String horarioFim;

    // Getters e Setters
    public String getHorarioInicio() {
        return horarioInicio;
    }

    public void setHorarioInicio(String horarioInicio) {
        this.horarioInicio = horarioInicio;
    }

    public String getHorarioFim() {
        return horarioFim;
    }

    public void setHorarioFim(String horarioFim) {
        this.horarioFim = horarioFim;
    }
}