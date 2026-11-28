#!/bin/sh
# Compila y sube Sakura a la EC2: backend (jar en Java 21, perfil supabase) y frontend estático detrás de Caddy
# (HTTPS automático en <ip-con-guiones>.sslip.io; /api va al backend, así no hay CORS). Uso: sh deploy/aws-ship.sh
set -eu
cd "$(dirname "$0")/.."
read ID IP < deploy/.instance
HOST=$(echo "$IP" | tr . -).sslip.io
KEY=deploy/sakura-demo.pem
SSH="ssh -i $KEY -o StrictHostKeyChecking=accept-new ec2-user@$IP"

(cd backend && ./mvnw -q -DskipTests package) 
JAR=$(ls backend/target/backend-*.jar | grep -v plain | head -1)
scp -i $KEY -o StrictHostKeyChecking=accept-new "$JAR" ec2-user@$IP:app.jar
scp -i $KEY backend/.env deploy/Caddyfile ec2-user@$IP:
$SSH "set -e; until docker info >/dev/null 2>&1; do sleep 3; done; chmod 600 .env
  docker rm -f sakura-api >/dev/null 2>&1 || true
  docker run -d --name sakura-api --restart unless-stopped --network host -v \$HOME/app.jar:/app.jar:ro -v \$HOME/.env:/.env:ro -w / \
    -e SAKURA_CORS_ORIGENES=https://$HOST eclipse-temurin:21-jre java -Xmx400m -jar /app.jar --spring.profiles.active=supabase --server.address=127.0.0.1
  until curl -sf -o /dev/null http://127.0.0.1:8080/api/menu; do sleep 3; done"

# Las páginas leen la API al compilar: se compila contra el backend ya publicado (dar la vuelta por Caddy).
$SSH "docker rm -f caddy >/dev/null 2>&1 || true; mkdir -p site
  docker run -d --name caddy --restart unless-stopped --network host -e HOST=$HOST -v \$HOME/Caddyfile:/etc/caddy/Caddyfile:ro -v \$HOME/site:/srv:ro -v caddy_data:/data caddy:2-alpine"
until curl -sf -o /dev/null "https://$HOST/api/menu"; do sleep 3; done
(cd frontend && PUBLIC_API_URL="https://$HOST/api" npm run build >/dev/null)
COPYFILE_DISABLE=1 tar --no-xattrs -czf deploy/site.tgz -C frontend/dist .
scp -i $KEY deploy/site.tgz ec2-user@$IP:
$SSH "rm -rf site/* && tar xzf site.tgz -C site && rm site.tgz"
rm deploy/site.tgz
echo "https://$HOST"
