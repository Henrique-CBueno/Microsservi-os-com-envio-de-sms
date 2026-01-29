package com.henrique.analiseCredito.service.strategy;

import com.henrique.analiseCredito.domain.Proposta;


public interface CalculoPonto {

    int calcular(Proposta proposta);
}
