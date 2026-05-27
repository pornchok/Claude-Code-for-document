/* ============================================================
   AI for Beginners — Animation Components
   Version: 1.0 | 2026-05-28
   All components auto-init on DOMContentLoaded.
   ============================================================ */

(function () {
  'use strict';

  /* ─────────────────────────────────────────────
     1. SCROLL FADE-UP (applies to .fade-up elements)
  ───────────────────────────────────────────── */
  function initFadeUp() {
    const els = document.querySelectorAll('.fade-up');
    if (!els.length) return;
    const io = new IntersectionObserver((entries) => {
      entries.forEach(e => { if (e.isIntersecting) { e.target.classList.add('visible'); io.unobserve(e.target); } });
    }, { threshold: 0.12 });
    els.forEach(el => io.observe(el));
  }

  /* ─────────────────────────────────────────────
     2. TIMELINE ANIMATION  [data-anim="timeline"]
     data-items = JSON array of { year, event }
  ───────────────────────────────────────────── */
  class TimelineAnim {
    constructor(el) {
      const items = JSON.parse(el.dataset.items || '[]');
      const track = document.createElement('div');
      track.className = 'timeline-track';
      items.forEach((it, i) => {
        const div = document.createElement('div');
        div.className = 'timeline-item';
        div.style.transitionDelay = (i * 0.12) + 's';
        div.innerHTML = `<div class="timeline-dot"></div>
          <div class="timeline-year">${it.year}</div>
          <div class="timeline-event">${it.event}</div>`;
        track.appendChild(div);
      });
      el.classList.add('anim-timeline');
      el.appendChild(track);

      const io = new IntersectionObserver((entries) => {
        if (entries[0].isIntersecting) {
          el.querySelectorAll('.timeline-item').forEach(d => d.classList.add('visible'));
          io.disconnect();
        }
      }, { threshold: 0.1 });
      io.observe(el);
    }
  }

  /* ─────────────────────────────────────────────
     3. TAXONOMY DIAGRAM  [data-anim="taxonomy"]
     Animated nested circles: AI ⊃ ML ⊃ DL ⊃ LLM
  ───────────────────────────────────────────── */
  class TaxonomyDiagram {
    constructor(el) {
      el.classList.add('anim-taxonomy');
      const svg = `<svg class="taxonomy-svg" viewBox="0 0 480 340" xmlns="http://www.w3.org/2000/svg">
        <ellipse class="tax-circle tax-ai"  cx="240" cy="170" rx="220" ry="155" data-tax="0"/>
        <ellipse class="tax-circle tax-ml"  cx="240" cy="185" rx="155" ry="110" data-tax="1"/>
        <ellipse class="tax-circle tax-dl"  cx="240" cy="195" rx="100" ry="72"  data-tax="2"/>
        <ellipse class="tax-circle tax-llm" cx="240" cy="200" rx="55"  ry="38"  data-tax="3"/>
        <text class="tax-label" x="80"  y="42"  fill="#4338ca" data-tax="0">🤖 AI (Artificial Intelligence)</text>
        <text class="tax-label" x="120" y="100" fill="#6d28d9" data-tax="1">🧠 Machine Learning</text>
        <text class="tax-label" x="165" y="158" fill="#7c3aed" data-tax="2">🔬 Deep Learning</text>
        <text class="tax-label" x="196" y="208" fill="#8b5cf6" font-size="11" data-tax="3">💬 LLM</text>
      </svg>`;
      el.innerHTML = svg;

      const circles = el.querySelectorAll('.tax-circle, .tax-label');
      const io = new IntersectionObserver((entries) => {
        if (entries[0].isIntersecting) {
          [0,1,2,3].forEach(i => {
            setTimeout(() => {
              el.querySelectorAll(`[data-tax="${i}"]`).forEach(c => c.classList.add('visible'));
            }, i * 400);
          });
          io.disconnect();
        }
      }, { threshold: 0.2 });
      io.observe(el);
    }
  }

  /* ─────────────────────────────────────────────
     4. LLM STEPPER  [data-anim="llm-stepper"]
     Step-through LLM pipeline with descriptions
     data-steps = JSON array of { icon, name, title, desc }
  ───────────────────────────────────────────── */
  class LLMStepper {
    constructor(el) {
      el.classList.add('anim-llm-stepper');
      const steps = JSON.parse(el.dataset.steps || '[]');
      this.steps = steps;
      this.current = 0;

      // Build pipeline row
      let pipelineHTML = '';
      steps.forEach((s, i) => {
        if (i > 0) pipelineHTML += `<div class="llm-arrow" data-arrow="${i}">→</div>`;
        pipelineHTML += `<div class="llm-stage${i===0?' active':''}" data-stage="${i}">
          <div class="llm-stage-icon">${s.icon}</div>
          <div class="llm-stage-name">${s.name}</div>
        </div>`;
      });

      el.innerHTML = `
        <div class="llm-pipeline">${pipelineHTML}</div>
        <div class="llm-panel">
          <div class="llm-panel-title">${steps[0].title}</div>
          <div class="llm-panel-desc">${steps[0].desc}</div>
        </div>
        <div class="llm-controls">
          <button class="llm-btn" data-action="prev">← ก่อนหน้า</button>
          <button class="llm-btn primary" data-action="next">ถัดไป →</button>
          <span class="llm-progress">1 / ${steps.length}</span>
        </div>`;

      el.addEventListener('click', e => {
        const action = e.target.closest('[data-action]')?.dataset.action;
        if (action === 'next' && this.current < steps.length - 1) this.go(this.current + 1);
        if (action === 'prev' && this.current > 0) this.go(this.current - 1);
      });
    }

    go(idx) {
      this.current = idx;
      this.el = this.el || this.steps; // keep ref
      const container = document.querySelectorAll('[data-anim="llm-stepper"]')[0];
      const allStages = container.querySelectorAll('.llm-stage');
      const allArrows = container.querySelectorAll('.llm-arrow');
      allStages.forEach((s,i) => s.classList.toggle('active', i === idx));
      allArrows.forEach((a,i) => a.classList.toggle('active', i < idx));
      container.querySelector('.llm-panel-title').textContent = this.steps[idx].title;
      container.querySelector('.llm-panel-desc').textContent = this.steps[idx].desc;
      container.querySelector('.llm-progress').textContent = `${idx+1} / ${this.steps.length}`;
    }
  }

  // Multi-instance support
  const llmStepperInstances = [];
  function initLLMSteppers() {
    document.querySelectorAll('[data-anim="llm-stepper"]').forEach((el, elIdx) => {
      const steps = JSON.parse(el.dataset.steps || '[]');
      let current = 0;

      let pipelineHTML = '';
      steps.forEach((s, i) => {
        if (i > 0) pipelineHTML += `<div class="llm-arrow" data-arrow="${i}">→</div>`;
        pipelineHTML += `<div class="llm-stage${i===0?' active':''}" data-stage="${i}">
          <div class="llm-stage-icon">${s.icon}</div>
          <div class="llm-stage-name">${s.name}</div>
        </div>`;
      });

      el.innerHTML = `
        <div class="llm-pipeline">${pipelineHTML}</div>
        <div class="llm-panel">
          <div class="llm-panel-title">${steps[0].title}</div>
          <div class="llm-panel-desc">${steps[0].desc}</div>
        </div>
        <div class="llm-controls">
          <button class="llm-btn" data-action="prev">← ก่อนหน้า</button>
          <button class="llm-btn primary" data-action="next">ถัดไป →</button>
          <span class="llm-progress">1 / ${steps.length}</span>
        </div>`;

      function go(idx) {
        current = idx;
        el.querySelectorAll('.llm-stage').forEach((s,i) => s.classList.toggle('active', i===idx));
        el.querySelectorAll('.llm-arrow').forEach((a,i) => a.classList.toggle('active', i<idx));
        el.querySelector('.llm-panel-title').textContent = steps[idx].title;
        el.querySelector('.llm-panel-desc').textContent = steps[idx].desc;
        el.querySelector('.llm-progress').textContent = `${idx+1} / ${steps.length}`;
      }

      el.addEventListener('click', e => {
        const action = e.target.closest('[data-action]')?.dataset.action;
        if (action === 'next' && current < steps.length-1) go(current+1);
        if (action === 'prev' && current > 0) go(current-1);
      });
    });
  }

  /* ─────────────────────────────────────────────
     5. TOKEN VISUALIZER  [data-anim="token-viz"]
     Simple heuristic tokenizer for demonstration
  ───────────────────────────────────────────── */
  function simpleTokenize(text) {
    // Heuristic: split on punctuation, spaces, and Thai character boundaries
    // This is illustrative — real tokenizers use BPE which is more complex
    const tokens = [];
    let i = 0;
    while (i < text.length) {
      // Thai chars: codepoint 0x0E00–0x0E7F
      const code = text.charCodeAt(i);
      if (code >= 0x0E00 && code <= 0x0E7F) {
        // Group 2-4 Thai chars as one token
        let end = i + Math.min(3 + Math.floor(Math.random()*2), text.length - i);
        while (end > i+1 && text.charCodeAt(end-1) >= 0x0E00 && text.charCodeAt(end-1) <= 0x0E7F) {
          // Keep Thai word-ish chunks
          if (end - i >= 3) break;
          end++;
        }
        tokens.push(text.slice(i, end));
        i = end;
      } else if (/\s/.test(text[i])) {
        // Space as own token if followed by word
        tokens.push(' ' + (text[i+1] || ''));
        i += 2;
      } else if (/[a-zA-Z]/.test(text[i])) {
        // English: split roughly every 3-4 chars (BPE-like)
        const word_end = text.slice(i).search(/[^a-zA-Z]/);
        const word = word_end === -1 ? text.slice(i) : text.slice(i, i + word_end);
        if (word.length <= 4) { tokens.push(word); i += word.length; }
        else {
          const mid = Math.ceil(word.length / 2);
          tokens.push(word.slice(0, mid));
          tokens.push(word.slice(mid));
          i += word.length;
        }
      } else {
        tokens.push(text[i]);
        i++;
      }
    }
    return tokens.filter(t => t.trim().length > 0 || t.startsWith(' '));
  }

  const TOKEN_BG = ['#fee2e2','#fef3c7','#d1fae5','#dbeafe','#ede9fe','#fce7f3','#cffafe','#fff7ed'];
  const TOKEN_COLOR = ['#991b1b','#92400e','#065f46','#1e40af','#5b21b6','#9d174d','#155e75','#c2410c'];

  function initTokenViz() {
    document.querySelectorAll('[data-anim="token-viz"]').forEach(el => {
      el.classList.add('anim-token-viz');
      el.innerHTML = `
        <div class="token-input-row">
          <input class="token-input" type="text" placeholder="พิมพ์ข้อความภาษาไทยหรืออังกฤษ..." value="${el.dataset.text || 'Hello, I am Claude'}"/>
          <button class="token-btn">แบ่ง Tokens!</button>
        </div>
        <div class="token-output"></div>
        <div class="token-count"></div>`;

      const input = el.querySelector('.token-input');
      const output = el.querySelector('.token-output');
      const count = el.querySelector('.token-count');

      function tokenize() {
        const tokens = simpleTokenize(input.value || 'Hello');
        output.innerHTML = '';
        tokens.forEach((t, i) => {
          const span = document.createElement('span');
          span.className = 'token';
          span.style.animationDelay = (i * 0.05) + 's';
          span.style.background = TOKEN_BG[i % TOKEN_BG.length];
          span.style.color = TOKEN_COLOR[i % TOKEN_COLOR.length];
          span.textContent = t;
          output.appendChild(span);
        });
        count.textContent = `✨ ${tokens.length} tokens (จริงๆ แล้ว tokenizer ของ Claude คำนวณต่างจากนี้เล็กน้อย — นี่คือ visualization เพื่อเข้าใจแนวคิด)`;
      }

      el.querySelector('.token-btn').addEventListener('click', tokenize);
      input.addEventListener('keydown', e => { if (e.key === 'Enter') tokenize(); });
      tokenize();
    });
  }

  /* ─────────────────────────────────────────────
     6. FLIP CARD (Bad vs Good Prompt)
     [data-anim="flip-card"] wrapper
     Children: [data-role="bad"] and [data-role="good"]
  ───────────────────────────────────────────── */
  function initFlipCards() {
    document.querySelectorAll('[data-anim="flip-pairs"]').forEach(container => {
      const pairs = JSON.parse(container.dataset.pairs || '[]');
      pairs.forEach(pair => {
        const wrapper = document.createElement('div');
        wrapper.className = 'flip-card-container fade-up';
        wrapper.innerHTML = `
          <div class="flip-card bad">
            <div class="flip-card-header">❌ Prompt แบบแย่</div>
            <div class="flip-card-content">${pair.bad}</div>
            <div class="flip-card-why">${pair.badWhy}</div>
          </div>
          <div class="flip-card good">
            <div class="flip-card-header">✅ Prompt แบบดี</div>
            <div class="flip-card-content">${pair.good}</div>
            <div class="flip-card-why">${pair.goodWhy}</div>
          </div>`;
        container.appendChild(wrapper);
      });
    });
  }

  /* ─────────────────────────────────────────────
     7. PROMPT BUILDER  [data-anim="prompt-builder"]
     Shows prompt parts one by one on click
  ───────────────────────────────────────────── */
  function initPromptBuilder() {
    document.querySelectorAll('[data-anim="prompt-builder"]').forEach(el => {
      el.classList.add('prompt-builder');
      const parts = JSON.parse(el.dataset.parts || '[]');
      let revealed = 0;

      const partsHTML = parts.map((p, i) => `
        <div class="prompt-part pb-${p.type}">
          <span class="prompt-part-label">${p.label}</span>
          <span class="prompt-part-text" data-part="${i}">${p.text}</span>
        </div>`).join('');

      el.innerHTML = `${partsHTML}
        <div class="pb-controls">
          <button class="pb-btn pb-btn-reveal">เปิดเผยส่วนถัดไป →</button>
          <button class="pb-btn pb-btn-reset">↺ รีเซ็ต</button>
        </div>`;

      function reveal() {
        if (revealed < parts.length) {
          el.querySelector(`[data-part="${revealed}"]`).classList.add('visible');
          revealed++;
          if (revealed === parts.length) el.querySelector('.pb-btn-reveal').textContent = '✅ Prompt สมบูรณ์!';
        }
      }

      function reset() {
        revealed = 0;
        el.querySelectorAll('.prompt-part-text').forEach(t => t.classList.remove('visible'));
        el.querySelector('.pb-btn-reveal').textContent = 'เปิดเผยส่วนถัดไป →';
      }

      el.querySelector('.pb-btn-reveal').addEventListener('click', reveal);
      el.querySelector('.pb-btn-reset').addEventListener('click', reset);
    });
  }

  /* ─────────────────────────────────────────────
     8. SKILLS FLOW  [data-anim="skills-flow"]
     data-nodes = JSON array of { icon, label }
     data-steps = JSON array of { active: [node indices], desc }
  ───────────────────────────────────────────── */
  function initSkillsFlow() {
    document.querySelectorAll('[data-anim="skills-flow"]').forEach(el => {
      el.classList.add('anim-skills-flow');
      const nodes = JSON.parse(el.dataset.nodes || '[]');
      const steps = JSON.parse(el.dataset.steps || '[]');
      let current = -1;

      let diagramHTML = '';
      nodes.forEach((n, i) => {
        if (i > 0) diagramHTML += `<div class="sf-arrow" data-arrow="${i}">→</div>`;
        diagramHTML += `<div class="sf-node" data-node="${i}">
          <div class="sf-node-icon">${n.icon}</div>
          <div class="sf-node-label">${n.label}</div>
        </div>`;
      });

      el.innerHTML = `
        <div class="skills-flow-diagram">${diagramHTML}</div>
        <div class="sf-desc">กด "เริ่ม" เพื่อดูกระบวนการทำงาน</div>
        <div class="sf-controls">
          <button class="sf-btn sf-btn-next">▶ เริ่ม</button>
          <button class="sf-btn sf-btn-reset">↺ รีเซ็ต</button>
        </div>`;

      function go(idx) {
        current = idx;
        const activeNodes = steps[idx].active;
        el.querySelectorAll('.sf-node').forEach((n, i) => n.classList.toggle('active', activeNodes.includes(i)));
        el.querySelectorAll('.sf-arrow').forEach((a, i) => a.classList.toggle('active', activeNodes.some(n => n > i)));
        el.querySelector('.sf-desc').textContent = steps[idx].desc;
        const btn = el.querySelector('.sf-btn-next');
        if (idx >= steps.length - 1) { btn.textContent = '✅ เสร็จสิ้น'; btn.disabled = true; }
        else btn.textContent = 'ถัดไป →';
      }

      el.querySelector('.sf-btn-next').addEventListener('click', () => {
        if (current < steps.length - 1) go(current + 1);
      });
      el.querySelector('.sf-btn-reset').addEventListener('click', () => {
        current = -1;
        el.querySelectorAll('.sf-node').forEach(n => n.classList.remove('active'));
        el.querySelectorAll('.sf-arrow').forEach(a => a.classList.remove('active'));
        el.querySelector('.sf-desc').textContent = 'กด "เริ่ม" เพื่อดูกระบวนการทำงาน';
        el.querySelector('.sf-btn-next').textContent = '▶ เริ่ม';
        el.querySelector('.sf-btn-next').disabled = false;
      });
    });
  }

  /* ─────────────────────────────────────────────
     9. QUIZ BLOCKS  [data-anim="quiz"]
     data-q = question text
     data-options = JSON array of { text, correct, feedback }
  ───────────────────────────────────────────── */
  function initQuizBlocks() {
    document.querySelectorAll('[data-anim="quiz"]').forEach(el => {
      el.classList.add('quiz-block');
      const q = el.dataset.q || '';
      const options = JSON.parse(el.dataset.options || '[]');

      let optHTML = options.map((o, i) =>
        `<div class="quiz-option" data-idx="${i}">
          <span class="opt-icon">○</span>
          <span>${o.text}</span>
        </div>`).join('');

      el.innerHTML = `
        <div class="quiz-q">🧠 ${q}</div>
        <div class="quiz-options">${optHTML}</div>
        <div class="quiz-feedback"></div>`;

      let answered = false;
      el.querySelectorAll('.quiz-option').forEach(opt => {
        opt.addEventListener('click', () => {
          if (answered) return;
          answered = true;
          const idx = +opt.dataset.idx;
          const o = options[idx];
          const fb = el.querySelector('.quiz-feedback');
          el.querySelectorAll('.quiz-option').forEach(o2 => {
            o2.classList.add('disabled');
            const i2 = +o2.dataset.idx;
            if (options[i2].correct) { o2.classList.add('correct'); o2.querySelector('.opt-icon').textContent = '✅'; }
            else if (i2 === idx) { o2.classList.add('wrong'); o2.querySelector('.opt-icon').textContent = '❌'; }
          });
          fb.textContent = o.feedback;
          fb.className = `quiz-feedback show ${o.correct ? 'correct-fb' : 'wrong-fb'}`;
        });
      });
    });
  }

  /* ─────────────────────────────────────────────
     10. EXAMPLE TABS
  ───────────────────────────────────────────── */
  function initExampleTabs() {
    document.querySelectorAll('.example-tabs').forEach(tabBar => {
      const container = tabBar.parentElement;
      const tabs = tabBar.querySelectorAll('.example-tab');
      const panels = container.querySelectorAll('.example-panel');

      tabs.forEach((tab, i) => {
        tab.addEventListener('click', () => {
          tabs.forEach(t => t.classList.remove('active'));
          panels.forEach(p => p.classList.remove('active'));
          tab.classList.add('active');
          if (panels[i]) panels[i].classList.add('active');
        });
      });
      // Activate first
      if (tabs[0]) tabs[0].classList.add('active');
      if (panels[0]) panels[0].classList.add('active');
    });
  }

  /* ─────────────────────────────────────────────
     INIT ALL
  ───────────────────────────────────────────── */
  document.addEventListener('DOMContentLoaded', () => {
    initFadeUp();
    document.querySelectorAll('[data-anim="timeline"]').forEach(el => new TimelineAnim(el));
    document.querySelectorAll('[data-anim="taxonomy"]').forEach(el => new TaxonomyDiagram(el));
    initLLMSteppers();
    initTokenViz();
    initFlipCards();
    initPromptBuilder();
    initSkillsFlow();
    initQuizBlocks();
    initExampleTabs();
  });

})();
