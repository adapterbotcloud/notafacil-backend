package br.com.notafacil.job;

import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
public class JobStatusHolder {
    private volatile LocalDateTime ultimaExecucao;
    private volatile int protocolosPendentes;

    public LocalDateTime getUltimaExecucao() { return ultimaExecucao; }
    public void setUltimaExecucao(LocalDateTime t) { this.ultimaExecucao = t; }
    public int getProtocolosPendentes() { return protocolosPendentes; }
    public void setProtocolosPendentes(int n) { this.protocolosPendentes = n; }
}
