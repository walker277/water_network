#!/bin/bash
if ! bash Build.sh; then
    echo "Chyba: Kompilace selhala."
    exit 1
fi


if ! bash Makedoc.sh; then
    echo "Chyba: Vytváření dokumentace selhalo."
fi

java -cp ./bin:./lib/Jama-1.0.3.jar:./lib/jfreechart-1.5.3.jar WNVis_SP2024

