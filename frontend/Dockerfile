# ==========================================
# Estágio 1: Build da Aplicação Angular
# ==========================================
FROM node:22-alpine AS builder

WORKDIR /app

# Copia primeiro os manifests de dependências para aproveitar o cache do Docker
COPY package.json package-lock.json ./

# Instala as dependências de forma reproduzível com npm clean install
RUN npm ci

# Copia o código-fonte
COPY . .

# Compila a aplicação Angular para produção (saída em dist/frontend/browser)
RUN npm run build

# ==========================================
# Estágio 2: Servidor Web Nginx de Produção
# ==========================================
FROM nginx:alpine AS runner

# Remove a página padrão do Nginx
RUN rm -rf /usr/share/nginx/html/*

# Copia os arquivos compilados do estágio 1 para a pasta servida pelo Nginx
COPY --from=builder /app/dist/frontend/browser /usr/share/nginx/html

# Copia nossa configuração customizada do Nginx com suporte a SPA e Gzip
COPY nginx.conf /etc/nginx/conf.d/default.conf

# Expõe a porta 80 HTTP
EXPOSE 80

# Inicia o Nginx em primeiro plano (daemon off) para manter o container ativo
CMD ["nginx", "-g", "daemon off;"]
