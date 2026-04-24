#!/usr/bin/env bash
# Script de demonstracao - executa os principais fluxos da aplicacao.
# Uso: ./demo.sh     (assume app rodando em http://localhost:8080)

set -u
BASE_URL="${BASE_URL:-http://localhost:8080}"

echo ""
echo "==> 1) CEP valido (Av. Paulista)"
curl -s -w "\nHTTP %{http_code}\n" "$BASE_URL/api/cep/01310-100"

echo ""
echo "==> 2) CEP valido (Vila Olimpia)"
curl -s -w "\nHTTP %{http_code}\n" "$BASE_URL/api/cep/04567000"

echo ""
echo "==> 3) CEP com formato invalido -> 400"
curl -s -w "\nHTTP %{http_code}\n" "$BASE_URL/api/cep/abc"

echo ""
echo "==> 4) CEP nao encontrado -> 404"
curl -s -w "\nHTTP %{http_code}\n" "$BASE_URL/api/cep/99999999"

echo ""
echo "==> 5) Listagem de logs persistidos"
curl -s "$BASE_URL/api/cep/logs" | head -c 800
echo ""
echo ""
echo "==> 6) Logs por CEP especifico"
curl -s "$BASE_URL/api/cep/logs/01310100"
echo ""
