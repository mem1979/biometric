package com.sta.biometric.dashboard.auxiliares;

	import javax.persistence.*;

import lombok.*;

	@Embeddable
	@Getter @Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public class ItemGraficoPorcentageAgentesDashboard {

	    @Column(length = 50)
	    private String descripcion;

	    private int valor;
	}