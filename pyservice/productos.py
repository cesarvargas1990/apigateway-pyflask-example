from flask import Flask, request, jsonify
import jwt
from pymongo import MongoClient
import consul
import time

app = Flask(__name__)

# Configuración MongoDB
client = MongoClient('mongodb', 27017)
db = client['mydatabase']
products = db['products']

# Configuración Consul
consul_client = consul.Consul(host="consul")  # Asume que Consul corre en localhost:8500, modifica si es diferente

SERVICE_NAME = 'my_flask_service'
SERVICE_PORT = 5000

@app.route('/productos', methods=['POST'])
def create_product():
    # Extraer el JWT del encabezado
    token = request.headers.get('Authorization').split(' ')[1]

    # Validar el JWT
    if not validate_jwt(token):
        return jsonify({"error": "Token inválido o sin permisos"}), 401
    
    # Insertar en MongoDB
    data = request.json
    products.insert_one({
        'name': data.get('name'),
        'price': data.get('price')
    })

    return jsonify({"message": "Producto creado"}), 201

def validate_jwt(token):
    try:
        # Decodificar el JWT sin verificar la firma
        decoded_token = jwt.decode(token, options={"verify_signature": False})
        
        # Validar el rol
        if 'rol_productos_crear' not in decoded_token.get('realm_access', {}).get('roles', []):
            return False

        # Validar la fecha de expiración
        exp_time = decoded_token.get('exp')
        current_time = time.time()
        if exp_time is None or current_time > exp_time:
            return False

        return True
    except jwt.ExpiredSignatureError:
        return False
    except jwt.DecodeError:
        return False

if __name__ == "__main__":
    # Registra el servicio en Consul
    consul_client.agent.service.register(
        SERVICE_NAME,
        service_id=SERVICE_NAME + "-1",
        address="consul",
        port=SERVICE_PORT,
        tags=["flask", "product-api"]
    )
    
    try:
        app.run(host='0.0.0.0', debug=True)
    finally:
        # Deregistra el servicio al salir
        consul_client.agent.service.deregister(SERVICE_NAME + "-1")
