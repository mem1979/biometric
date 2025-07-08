

/* --- Polyfill global para Calendar.getEvents() --- */
if (window.Calendar && typeof Calendar.getEvents !== 'function') {
    // Devuelve la dataSource del primer calendario encontrado; suficiente
    Calendar.getEvents = () => {
        const c = document.querySelector('.calendar table')
                 || document.querySelector('.year-calendar');
        return c && c._calendar ? c._calendar.options.dataSource || [] : [];
    };
}

openxava.addEditorInitFunction(() => {

  /* ───── 0. Polyfill global para versiones antiguas ───── */
  if (window.Calendar && !Calendar.prototype.getEvents && Calendar.prototype.getDataSource) {
      Calendar.prototype.getEvents = Calendar.prototype.getDataSource;   // v≤1.3 y v3
  }

  document.querySelectorAll('.ox-year-cal').forEach(container => {

      /* 1. JSON incrustado en el JSP */
      const rawJson = container.dataset.items;
      if (!rawJson) return;

      let raw;
      try { raw = JSON.parse(rawJson); }
      catch (e) { console.error('JSON inválido', e); return; }

      /* 2. Helper: Date local (00:00) */
      function asLocalDate(v) {
          if (v instanceof Date) return new Date(v.getFullYear(), v.getMonth(), v.getDate());
          if (typeof v === 'string') {
              const [y, m, d] = v.split('T')[0].split('-').map(Number);
              return new Date(y, m - 1, d);
          }
          return null;
      }

      /* 3. Mapear filas → eventos Year‑Calendar */
      const events = raw.map(r => {
          const start = asLocalDate(r.fechaInicio || r.startDate || r.start);
          const end   = asLocalDate(r.fechaFin   || r.endDate   || r.end || start);
          return {
              id:        r.id,
              name:      r.descripcion || r.name || '',
              motive:    r.motivo || '',
              startDate: start,
              endDate:   end,
              color:     r.color
          };
      });

      /* 4. Instanciar calendario anual */
      const year = new Date().getFullYear();
      const cal  = new Calendar(container, {
          language: 'es',
          startYear: year,
          minDate:   new Date(year, 0, 1),
          maxDate:   new Date(year, 11, 31),
          dataSource: events,

          mouseOnDay: e => {
              if (e.events.length === 0) return;
              

              let tip = document.getElementById('ox-cal-tooltip');
              if (!tip) {
                  tip = document.createElement('div');
                  tip.id = 'ox-cal-tooltip';
                  tip.className = 'custom-tooltip';
                  document.body.appendChild(tip);
              }

              tip.innerHTML = e.events.map(ev => {
                  const ini = ev.startDate.toLocaleDateString('es-AR');
                  const fin = ev.endDate.toLocaleDateString('es-AR');
                  const rango = ini === fin ? ini : `Del ${ini} al ${fin}`;
                  const motivo = ev.motive ? `<i>${ev.motive}</i><br>` : '';
                  return `<div style="margin-bottom:4px"><b>${ev.name}</b><br>${motivo}${rango}</div>`;
              }).join('');

              const rect = e.element.getBoundingClientRect();
              tip.style.cssText = `top:${rect.top + window.scrollY + 16}px;
                                   left:${rect.left + window.scrollX + 16}px;
                                   opacity:1;`;
          },

          mouseOutDay: () => {
              const tip = document.getElementById('ox-cal-tooltip');
              if (tip) tip.style.opacity = '0';
          }
      });

      container.__calendar = cal;
  });
});

/* Limpieza */
openxava.addEditorDestroyFunction(() => {
    const tip = document.getElementById('ox-cal-tooltip');
    if (tip) tip.remove();
});
