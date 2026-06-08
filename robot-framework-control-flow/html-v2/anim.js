(function () {
  function esc(s) {
    return String(s)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;');
  }

  function rfHighlight(line) {
    // Very basic RF keyword coloring
    return esc(line)
      .replace(/(\$\{[^}]+\}|@\{[^}]+\})/g, '<span class="rf-var">$1</span>')
      .replace(/\b(IF|ELSE IF|ELSE|FOR|WHILE|BREAK|CONTINUE|END|IN RANGE|IN ENUMERATE|IN ZIP|IN|RETURN)\b/g,
        '<span class="rf-kw">$1</span>')
      .replace(/\b(Log|Set Variable|Evaluate|Should Be Equal|Should Not Be Empty|Create List|Get Length|Get WebElements|Get Text|Get Element Count|Click Element|Input Text|Wait Until Element Is Visible|Run Keyword And Return Status|Sleep|Append To List)\b/g,
        '<span class="rf-builtin">$1</span>')
      .replace(/(#.*)$/, '<span class="rf-comment">$1</span>');
  }

  class Stepper {
    constructor(el) {
      this.el = el;
      const data = JSON.parse(el.dataset.stepper);
      this.codeLines = data.code.split('\n');
      this.steps = data.steps;
      this.idx = 0;
      this.build();
      this.render();
    }

    build() {
      const lines = this.codeLines.map((l, i) =>
        `<div class="sl" data-n="${i + 1}">` +
        `<span class="sn">${i + 1}</span>` +
        `<span class="sc">${rfHighlight(l)}</span>` +
        `</div>`
      ).join('');

      this.el.innerHTML = `
        <div class="st-wrap">
          <div class="st-code"><div class="st-lines">${lines}</div></div>
          <div class="st-right">
            <div class="st-panel">
              <div class="sp-title">Variables</div>
              <div class="sp-body sv-body"></div>
            </div>
            <div class="st-panel">
              <div class="sp-title">Output</div>
              <div class="sp-body so-body"></div>
            </div>
          </div>
        </div>
        <div class="st-note"><span class="st-num"></span><span class="st-msg"></span></div>
        <div class="st-ctrl">
          <button class="st-btn" data-action="reset">↺ Reset</button>
          <button class="st-btn" data-action="prev">← ก่อนหน้า</button>
          <span class="st-prog"></span>
          <button class="st-btn primary" data-action="next">ถัดไป →</button>
        </div>`;

      this.el.querySelector('[data-action="reset"]').onclick = () => { this.idx = 0; this.render(); };
      this.el.querySelector('[data-action="prev"]').onclick = () => { if (this.idx > 0) { this.idx--; this.render(); } };
      this.el.querySelector('[data-action="next"]').onclick = () => { if (this.idx < this.steps.length - 1) { this.idx++; this.render(); } };
    }

    render() {
      const step = this.steps[this.idx];
      const hi = step.hi || [];

      // Code lines
      this.el.querySelectorAll('.sl').forEach(row => {
        const n = +row.dataset.n;
        row.className = 'sl' + (hi.includes(n) ? ' hi' : hi.length ? ' dim' : '');
      });

      // Variables
      const vars = step.vars || {};
      const keys = Object.keys(vars);
      this.el.querySelector('.sv-body').innerHTML = keys.length
        ? keys.map(k => `<div class="sv-row"><span class="sv-k">\${${k}}</span><span class="sv-v">${esc(vars[k])}</span></div>`).join('')
        : '<span class="sp-empty">—</span>';

      // Output
      const out = step.out || [];
      this.el.querySelector('.so-body').innerHTML = out.length
        ? out.map(l => `<div class="so-line">${esc(l)}</div>`).join('')
        : '<span class="sp-empty">—</span>';

      // Note
      this.el.querySelector('.st-num').textContent = `ขั้นที่ ${this.idx + 1}/${this.steps.length}  `;
      this.el.querySelector('.st-msg').textContent = step.note;

      // Progress & buttons
      this.el.querySelector('.st-prog').textContent = `${this.idx + 1} / ${this.steps.length}`;
      this.el.querySelector('[data-action="prev"]').disabled = this.idx === 0;
      this.el.querySelector('[data-action="next"]').disabled = this.idx === this.steps.length - 1;
    }
  }

  document.querySelectorAll('[data-stepper]').forEach(el => new Stepper(el));
})();
