#!/bin/bash
# ============================================================================
# SCRIPT DE PUBLICAÇÃO — Diário de Emoções Android → GitHub
# Autor: Tiago Santos Mota | METSW DAM | ISLA Santarém 2025/2026
#
# COMO USAR:
# 1. Copia a pasta do projeto para o teu computador
# 2. Abre o terminal (Git Bash no Windows / Terminal no Mac/Linux)
# 3. Navega até à pasta: cd caminho/para/diario-emocoes-android
# 4. Executa: bash publicar_github.sh
# ============================================================================

echo ""
echo "=================================================="
echo "  Diário de Emoções — Publicação no GitHub"
echo "=================================================="
echo ""

# ---------------------------------------------------------------------------
# PASSO 1: Verificar se o git está instalado
# ---------------------------------------------------------------------------
if ! command -v git &> /dev/null; then
    echo "❌ Git não encontrado. Instala em: https://git-scm.com/downloads"
    exit 1
fi
echo "✅ Git encontrado: $(git --version)"

# ---------------------------------------------------------------------------
# PASSO 2: Configurar identidade git (se ainda não configurado)
# Substitui os valores entre aspas pelos teus dados reais
# ---------------------------------------------------------------------------
echo ""
echo "--- Configurando identidade Git ---"
git config --global user.name "Tiago Santos Mota"
git config --global user.email "SEU_EMAIL@gmail.com"
echo "✅ Identidade configurada"

# ---------------------------------------------------------------------------
# PASSO 3: Criar o repositório no GitHub via API
#
# REQUISITO: Precisas de um Personal Access Token (PAT) do GitHub
# Como criar:
#   1. Acede a https://github.com/settings/tokens
#   2. "Generate new token (classic)"
#   3. Nome: "diario-emocoes-android"
#   4. Scopes: marca "repo" (acesso total a repositórios)
#   5. "Generate token" — copia o token (só aparece uma vez!)
#   6. Substitui GITHUB_TOKEN abaixo pelo token copiado
# ---------------------------------------------------------------------------
GITHUB_USERNAME="SEU_USERNAME_GITHUB"   # ← Substitui pelo teu username GitHub
GITHUB_TOKEN="ghp_XXXXXXXXXXXXXXXXXX"   # ← Substitui pelo teu Personal Access Token
REPO_NAME="diario-emocoes-android"
REPO_DESCRIPTION="Aplicação Android nativa (Java) — Diário de Emoções com MVVM, Room/SQLite. Trabalho de grupo DAM, METSW ISLA Santarém 2025/26."

echo ""
echo "--- Criando repositório no GitHub ---"
RESPONSE=$(curl -s -o /tmp/github_response.json -w "%{http_code}" \
    -X POST \
    -H "Authorization: token $GITHUB_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
        \"name\": \"$REPO_NAME\",
        \"description\": \"$REPO_DESCRIPTION\",
        \"private\": false,
        \"has_issues\": true,
        \"has_projects\": false,
        \"has_wiki\": false,
        \"auto_init\": false
    }" \
    "https://api.github.com/user/repos")

if [ "$RESPONSE" = "201" ]; then
    echo "✅ Repositório criado com sucesso no GitHub!"
elif [ "$RESPONSE" = "422" ]; then
    echo "⚠️  Repositório já existe — continuando com push..."
else
    echo "❌ Erro ao criar repositório (HTTP $RESPONSE)"
    echo "   Verifica o teu token e username."
    cat /tmp/github_response.json
    exit 1
fi

# ---------------------------------------------------------------------------
# PASSO 4: Ligar o repositório local ao GitHub (remote origin)
# ---------------------------------------------------------------------------
echo ""
echo "--- Configurando remote origin ---"
REMOTE_URL="https://$GITHUB_TOKEN@github.com/$GITHUB_USERNAME/$REPO_NAME.git"

# Remove remote existente se houver (evita conflitos)
git remote remove origin 2>/dev/null || true
git remote add origin "$REMOTE_URL"
echo "✅ Remote configurado: https://github.com/$GITHUB_USERNAME/$REPO_NAME"

# ---------------------------------------------------------------------------
# PASSO 5: Push para GitHub
# ---------------------------------------------------------------------------
echo ""
echo "--- Enviando código para GitHub ---"
git push -u origin main

if [ $? -eq 0 ]; then
    echo ""
    echo "=================================================="
    echo "  ✅ SUCESSO! Projeto publicado no GitHub"
    echo "=================================================="
    echo ""
    echo "  🔗 URL: https://github.com/$GITHUB_USERNAME/$REPO_NAME"
    echo ""
    echo "  Próximos passos no Android Studio:"
    echo "  1. File → New → Project from Version Control"
    echo "  2. URL: https://github.com/$GITHUB_USERNAME/$REPO_NAME.git"
    echo "  3. Sync Gradle e executar no emulador"
    echo ""
else
    echo ""
    echo "❌ Erro no push. Verifica:"
    echo "   - O token tem permissão 'repo'"
    echo "   - O username está correto"
    echo "   - Estás na pasta correta do projeto"
fi
