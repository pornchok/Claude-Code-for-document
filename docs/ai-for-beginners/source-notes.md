# AI for Beginners — Source Notes

ไฟล์นี้บันทึก QUOTE จาก official sources สำหรับทุก concept หลักในหลักสูตร
ลบได้หลังจาก Quality Review ผ่านทั้งหมดแล้ว

---

## Chapter 1: AI คืออะไร?

SOURCE: https://www.ibm.com/topics/artificial-intelligence
VERSION: accessed 2026-05-28
CONCEPT: นิยาม Artificial Intelligence
QUOTE: "Artificial intelligence (AI) is technology that enables computers and machines to simulate human learning, comprehension, problem solving, decision making, creativity and autonomy."

SOURCE: https://developers.google.com/machine-learning/glossary
VERSION: accessed 2026-05-28
CONCEPT: Machine Learning
QUOTE: "Machine learning (ML) programs learn to adjust their behavior based on data. ML's main advantages over conventional programming are in situations involving large amounts of data and the need for sophisticated rules."

SOURCE: https://developers.google.com/machine-learning/glossary#deep-learning
VERSION: accessed 2026-05-28
CONCEPT: Deep Learning
QUOTE: "A type of machine learning where the model's architecture is a deep neural network. Deep learning models have been particularly effective for unstructured data like images, audio, and text."

SOURCE: https://developers.google.com/machine-learning/glossary#large-language-model
VERSION: accessed 2026-05-28
CONCEPT: Large Language Model
QUOTE: "A large (often billions of parameters) model trained on large amounts of text and other language-based data. LLMs are capable of translation, summarization, question answering, code writing, and more."

SOURCE: https://arxiv.org/abs/1706.03762
VERSION: Vaswani et al., 2017
CONCEPT: Transformer Architecture — จุดเริ่มต้น LLM ยุคปัจจุบัน
QUOTE: "We propose a new simple network architecture, the Transformer, based solely on attention mechanisms, dispensing with recurrence and convolutions entirely."
NOTE: paper นี้เป็น "Attention Is All You Need" — foundation ของ LLM ทุกตัวในปัจจุบัน

---

## Chapter 2: LLM ทำงานอย่างไร?

SOURCE: https://docs.anthropic.com/en/docs/about-claude/models/overview
VERSION: accessed 2026-05-28
CONCEPT: Claude model family
NOTE: Claude ใช้ Transformer-based architecture — Anthropic ไม่เปิดเผย architecture details ทั้งหมด

SOURCE: https://platform.openai.com/tokenizer
VERSION: accessed 2026-05-28
CONCEPT: Tokenization
NOTE: GPT-4 tokenizer ใช้ BPE (Byte Pair Encoding) — Claude ใช้ tokenizer ของตัวเอง แต่หลักการคล้ายกัน
VERIFIED: 1 token ≈ 4 characters ภาษาอังกฤษ, ภาษาไทยอาจใช้ token มากกว่า

SOURCE: https://developers.google.com/machine-learning/glossary#embedding
VERSION: accessed 2026-05-28
CONCEPT: Embedding
QUOTE: "An embedding is a relatively low-dimensional space into which you can translate high-dimensional vectors. Embeddings make it easier to do machine learning on large inputs like sparse vectors representing words."

SOURCE: https://developers.google.com/machine-learning/glossary#attention-mechanism
VERSION: accessed 2026-05-28
CONCEPT: Attention Mechanism
QUOTE: "A mechanism used in neural networks that allows the model to focus on specific parts of the input sequence when generating the output."

SOURCE: https://docs.anthropic.com/en/docs/about-claude/claude-in-practice
VERSION: accessed 2026-05-28
CONCEPT: Context window
NOTE: Claude มี context window ที่ใหญ่มาก — ข้อมูลใน context ไม่ใช่ "memory" ถาวร รีเซ็ตเมื่อจบ conversation

---

## Chapter 3: Prompting

SOURCE: https://docs.anthropic.com/en/docs/build-with-claude/prompt-engineering/overview
VERSION: accessed 2026-05-28
CONCEPT: Prompt Engineering Overview
QUOTE: "Prompt engineering is the practice of designing and refining prompts to get the best possible outputs from Claude. Claude responds best to clear and direct prompts that provide enough context."

SOURCE: https://docs.anthropic.com/en/docs/build-with-claude/prompt-engineering/be-clear-and-direct
VERSION: accessed 2026-05-28
CONCEPT: Clear and Direct Prompts
QUOTE: "Think of Claude as a brilliant friend who happens to have the knowledge of a doctor, lawyer, financial advisor, and expert in whatever you need. As a friend, they can give us real information based on our specific situation."

SOURCE: https://docs.anthropic.com/en/docs/build-with-claude/prompt-engineering/use-examples
VERSION: accessed 2026-05-28
CONCEPT: Few-shot Examples
QUOTE: "Including a few examples in your prompt (also called 'few-shot prompting') is one of the most effective ways to improve Claude's performance."

SOURCE: https://docs.anthropic.com/en/docs/build-with-claude/prompt-engineering/chain-of-thought
VERSION: accessed 2026-05-28
CONCEPT: Chain of Thought
QUOTE: "Encouraging Claude to think step by step before answering can dramatically improve its performance on complex reasoning tasks."

SOURCE: https://docs.anthropic.com/en/docs/build-with-claude/prompt-engineering/use-xml-tags
VERSION: accessed 2026-05-28
CONCEPT: XML Tags for structure
QUOTE: "When your prompts involve multiple components like context, instructions, and examples, XML tags can be a powerful way to structure your prompts and help Claude parse them accurately."

---

## Chapter 4: Claude Superpowers / Skills

SOURCE: Internal Claude Code documentation
VERSION: 2026-05-28 (active in this session)
CONCEPT: Skills system priority
QUOTE: "User instructions always take precedence: 1. User's explicit instructions (CLAUDE.md, GEMINI.md, AGENTS.md, direct requests) — highest priority; 2. Superpowers skills — override default system behavior where they conflict; 3. Default system prompt — lowest priority."

SOURCE: Internal (CLAUDE.md of this project)
VERSION: 2026-05-28
CONCEPT: Skill invocation method
NOTE: Skills invoke ผ่าน Skill tool ใน Claude Code — user type `/skill-name` หรือ Claude detect ว่าควรใช้ skill ใด

---

## Chapter 5: AI Ethics & Safety

SOURCE: https://www.anthropic.com/responsible-scaling-policy
VERSION: accessed 2026-05-28
CONCEPT: Responsible AI development
NOTE: Anthropic มี Responsible Scaling Policy ที่กำหนดว่าจะ deploy model อย่างไร

SOURCE: https://docs.anthropic.com/en/docs/test-and-evaluate/strengthen-guardrails/reduce-hallucinations
VERSION: accessed 2026-05-28
CONCEPT: Hallucination (ภาษา Anthropic เรียก "confabulation")
QUOTE: "Claude can sometimes generate information that sounds plausible but isn't accurate. This is sometimes called 'hallucination'."
NOTE: Anthropic แนะนำให้ใช้ grounding และ citations เพื่อลด hallucination

SOURCE: https://arxiv.org/abs/2302.07459
VERSION: Weidinger et al., 2023
CONCEPT: AI Bias
NOTE: Bias ใน LLM มาจาก training data ที่มี bias อยู่แล้ว — เช่น over-representation ของ western English content
