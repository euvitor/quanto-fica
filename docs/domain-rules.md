# 📐 Regras de Negócio e Matemática Financeira

Este documento formaliza as regras matemáticas e as diretrizes do setor financeiro e imobiliário brasileiro utilizadas no motor de cálculo do **Quanto Fica**.

---

## 1. Sistema de Amortização Constante (SAC)

O Sistema SAC é o modelo predominante nos financiamentos habitacionais no Brasil (especialmente pela Caixa Econômica Federal no âmbito do SFH - Sistema Financeiro da Habitação).

### 1.1 Premissas Fundamentais
* **Amortização Mensal ($A$):** É fixa e constante durante todo o prazo do contrato.
* **Saldo Devedor ($SD_k$):** Diminui linearmente a cada mês pelo valor da amortização.
* **Juros do Mês ($J_k$):** São calculados sobre o saldo devedor remanescente do mês anterior. Como o saldo cai todo mês, os juros diminuem a cada parcela.
* **Valor da Parcela ($P_k$):** Como a amortização é fixa e os juros caem, **as parcelas são decrescentes** (a primeira é a mais cara, a última é a mais barata).

---

### 1.2 Fórmulas Matemáticas

#### a) Saldo Devedor Inicial ($SD_0$)
$$SD_0 = \text{Valor do Imóvel} - \text{Valor de Entrada}$$

#### b) Taxa de Juros Mensal ($i$)
No SFH / crédito imobiliário brasileiro, a taxa de contrato anual pactuada é tradicionalmente convertida em taxa mensal linear (taxa nominal):
$$i = \frac{\text{Taxa Anual}}{12}$$

*Exemplo:* Para uma taxa anual de $9,99\%$, a taxa mensal nominal é:
$$i = \frac{0,0999}{12} = 0,008325 \text{ ao mês } (0,8325\%)$$

#### c) Amortização Mensal ($A$)
Para um prazo de $n$ meses:
$$A = \frac{SD_0}{n}$$

#### d) Para cada mês $k \in \{1, 2, \dots, n\}$:
1. **Juros do mês:**
   $$J_k = SD_{k-1} \times i$$
2. **Parcela do mês:**
   $$P_k = A + J_k$$
3. **Novo saldo devedor ao final do mês:**
   $$SD_k = SD_{k-1} - A$$

---

### 1.3 Detalhes Críticos de Engenharia (Nível Sênior / Padrão Bancário)

#### 🔹 Precisão com `BigDecimal`
* **Nunca** usar primitivos `double` ou `float` para cálculos financeiros.
* Escala adotada:
  * Cálculos intermediários de taxa: precisão de 8 a 10 casas decimais.
  * Moeda final (R$): escala de 2 casas decimais com arredondamento `RoundingMode.HALF_EVEN` (arredondamento bancário de Gauss, norma IEEE 754).

#### 🔹 Ajuste de Centavo na Última Parcela (Breakage / Rounding Discrepancy)
Em divisões que geram dízimas (ex: $R\$ 100.000,00 / 360 = 277,777777\dots$), a soma das amortizações arredondadas para 2 casas pode acumular uma diferença de centavos ao final de 360 meses.
* **Regra bancária:** Na última parcela ($k = n$):
  $$A_n = SD_{n-1}$$
  $$P_n = A_n + J_n$$
  $$SD_n = 0,00$$
Garantindo que a dívida seja quitada com precisão absoluta de zero centavos.

---

## 2. Custos Acessórios de Aquisição de Imóvel

Além da entrada e das parcelas, o comprador deve desembolsar à vista custos cartorários e tributários obrigatórios:

### 2.1 ITBI (Imposto sobre a Transmissão de Bens Imóveis)
* **Natureza:** Tributo municipal cobrado pela prefeitura onde o imóvel está localizado.
* **Alíquota típica:** Entre $2,0\%$ e $3,0\%$ do valor do imóvel (ou valor venal de referência, o que for maior).
* **Descontos:** Em vários municípios, o primeiro imóvel pelo SFH possui desconto de até $50\%$ no ITBI ou isenção em faixas sociais.

### 2.2 Escritura Pública e Registro de Imóveis
* **Natureza:** Emolumentos estaduais tabelados por lei estadual e fiscalizados pelas Corregedorias de Justiça.
* **Alíquota efetiva média:** Cerca de $1,0\%$ a $2,0\%$ do valor do imóvel.
* **Observação:** Em financiamentos bancários com força de escritura pública (alienação fiduciária), a confecção de escritura pública em tabelionato de notas costuma ser dispensada, pagando-se apenas a prenotação e registro no Cartório de Registro de Imóveis (RGI).
* **Desconto legal federal (Art. 290 da Lei 6.015/73):** $50\%$ de desconto nas custas de registro para a primeira aquisição imobiliária financiada pelo SFH.

### 2.3 Taxas Bancárias Iniciais
* **Tarifa de Avaliação de Garantia:** Cobrada pelo banco para enviar engenheiro perito ao imóvel (geralmente entre $R\$ 3.000,00$ e $R\$ 4.000,00$).

---

## 3. Resumo da Composição de Desembolso

$$\text{Custo Total Inicial à Vista} = \text{Entrada} + \text{ITBI} + \text{Registro de Imóveis} + \text{Tarifa Bancária de Avaliação}$$

$$\text{Custo Total da Operação ao Longo do Tempo} = \text{Custo Total Inicial} + \sum_{k=1}^{n} P_k$$
