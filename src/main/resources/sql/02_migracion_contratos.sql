-- =================================================================================
-- Script de Migracion: Crear primer Contrato Laboral desde Personal
-- =================================================================================
-- Este script toma los datos existentes en la tabla Personal (valor hora, porcentajes)
-- y crea un registro en ContratoLaboral para cada empleado activo.
-- =================================================================================

INSERT INTO contrato_laboral (
    id, 
    version,
    empleado_id, 
    fechaVigenciaDesde, 
    fechaVigenciaHasta, 
    vigente,
    
    -- Valores economicos migrados
    valorHoraAjustado,
    sueldoMensualAcordado, -- Se deja en 0 o null, ya que no tenemos el dato
    porcentajeHoraExtra,
    porcentajeHoraEspecial,
    
    -- Valores por defecto
    tipoContrato,
    modalidadTrabajo,
    motivoFinalizacion,
    observaciones,
    puesto,
    
    fechaModificacion
)
SELECT 
    UUID(), -- Generar ID unico
    0, -- Version JPA
    p.id, -- ID Empleado
    COALESCE(p.inicioActividades, CURRENT_DATE), -- Vigencia desde inicio activ. o hoy
    NULL, -- Hasta indefinido
    1, -- Vigente = true
    
    p.valorHora, -- Migramos el valor hora actual como "Ajustado" para mantener el valor exacto
    NULL, -- No calculamos sueldo mensual invertido para evitar errores de redondeo
    p.porcentajeHoraExtra,
    p.porcentajeHoraEspecial,
    
    'TIEMPO_COMPLETO', -- Default
    'PRESENCIAL', -- Default
    NULL,
    'Migracion automatica desde datos de empleado', -- Observacion
    p.puesto, -- Copiamos el puesto actual
    
    NOW() -- Fecha modificacion
FROM 
    personal p
WHERE 
    p.eliminado = 0 -- Solo empleados no eliminados
    AND p.valorHora IS NOT NULL; -- Solo si tienen valor hora definido
