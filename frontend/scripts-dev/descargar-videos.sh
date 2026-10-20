#!/bin/sh
# Descarga los 6 clips del recorrido desde midudev/mcp-higgsfield-landing (no se versionan en este repo).
# Uso: sh scripts-dev/descargar-videos.sh   (desde la carpeta frontend)
set -e
mkdir -p public/video
for i in 1 2 3 4 5 6; do
  echo "Descargando paseo-$i.mp4…"
  curl -fsSL "https://raw.githubusercontent.com/midudev/mcp-higgsfield-landing/main/public/video/paseo-$i.mp4" -o "public/video/paseo-$i.mp4"
done
echo "Listo. Sin los vídeos la landing también funciona: muestra los pósters fijos."
