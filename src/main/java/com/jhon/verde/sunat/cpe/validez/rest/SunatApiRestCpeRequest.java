package com.jhon.verde.sunat.cpe.validez.rest;

import com.jhon.verde.sunat.cpe.validez.dto.CpeRequest;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SunatApiRestCpeRequest {

    private String numRuc;
    private String codComp;
    private String numeroSerie;
    private String numero;
    private String fechaEmision;
    private Double monto;

    public SunatApiRestCpeRequest(CpeRequest cpeRequest){
        numRuc = cpeRequest.getComprobante().getRuc();
        codComp = cpeRequest.getComprobante().getTipoCpe();
        numeroSerie = cpeRequest.getComprobante().getSerie();
        numero = cpeRequest.getComprobante().getCorrelativo();
        fechaEmision = cpeRequest.getFechaEmision();
        monto = Double.valueOf(cpeRequest.getImporte());
    }

}
