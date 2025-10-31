#!/bin/bash

# Kong Configuration Script
# This script configures Kong API Gateway with all microservices

echo "🚀 Configuring Kong API Gateway..."

# Wait for Kong to be ready
echo "⏳ Waiting for Kong to be ready..."
until curl -f http://kong:8001/status > /dev/null 2>&1; do
  echo "Waiting for Kong..."
  sleep 5
done

echo "✅ Kong is ready!"

# Function to create service and route
create_service_route() {
    local service_name=$1
    local service_url=$2
    local route_path=$3
    
    echo "🔧 Configuring $service_name..."
    
    # Create service
    curl -X POST http://kong:8001/services \
        --data "name=$service_name" \
        --data "url=$service_url" \
        --silent --output /dev/null
    
    # Create route
    curl -X POST http://kong:8001/services/$service_name/routes \
        --data "paths[]=$route_path" \
        --data "strip_path=true" \
        --silent --output /dev/null
    
    echo "✅ $service_name configured!"
}

# Configure all microservices
create_service_route "user-service" "http://user-service:8081" "/api/users"
create_service_route "product-service" "http://product-service:8082" "/api/products"
create_service_route "cart-service" "http://cart-service:8083" "/api/cart"
create_service_route "order-service" "http://order-service:8084" "/api/orders"
create_service_route "payment-service" "http://payment-service:8085" "/api/payments"
create_service_route "notification-service" "http://notification-service:8086" "/api/notifications"

# Add plugins for enhanced functionality
echo "🔧 Adding Kong plugins..."

# Rate limiting plugin
curl -X POST http://kong:8001/plugins \
    --data "name=rate-limiting" \
    --data "config.minute=100" \
    --data "config.hour=1000" \
    --silent --output /dev/null

# CORS plugin
curl -X POST http://kong:8001/plugins \
    --data "name=cors" \
    --data "config.origins=*" \
    --data "config.methods=GET,POST,PUT,DELETE,OPTIONS" \
    --data "config.headers=Accept,Accept-Version,Content-Length,Content-MD5,Content-Type,Date,X-Auth-Token" \
    --silent --output /dev/null

# Request/Response logging
curl -X POST http://kong:8001/plugins \
    --data "name=file-log" \
    --data "config.path=/tmp/kong.log" \
    --silent --output /dev/null

echo "✅ Kong configuration completed!"
echo "🌐 Kong Proxy: http://localhost:8000"
echo "🔧 Kong Admin: http://localhost:8001"
echo "📊 Kong GUI: http://localhost:8002"
echo "⚖️  Load Balancer: http://localhost:80"
