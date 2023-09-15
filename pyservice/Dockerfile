FROM python:3.11.4-slim

# Establecer el directorio de trabajo
WORKDIR /app

# Instalar dependencias
RUN pip install --no-cache-dir Flask flask_pymongo pyjwt requests python-consul

# Copiar el archivo
COPY . .

# Exponer el puerto
EXPOSE 5000

# Comando de inicio
CMD ["python", "productos.py"]
