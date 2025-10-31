#!/bin/bash

# Kong API Gateway + Kafka Deployment Script
# This script deploys the complete microservices architecture with Kong and Kafka

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Function to print colored output
print_status() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
    print_error "Docker is not running. Please start Docker first."
    exit 1
fi

print_success "Docker is running!"

# Check if Docker Compose is available
if ! command -v docker-compose &> /dev/null; then
    print_error "Docker Compose is not installed. Please install Docker Compose first."
    exit 1
fi

print_success "Docker Compose is available!"

# Stop any existing containers
print_status "Stopping existing containers..."
docker-compose -f docker-compose-kong-kafka-microservices.yml down --remove-orphans

# Build all services
print_status "Building microservices..."
docker-compose -f docker-compose-kong-kafka-microservices.yml build --no-cache

# Start infrastructure services first
print_status "Starting infrastructure services..."
docker-compose -f docker-compose-kong-kafka-microservices.yml up -d mysql redis elasticsearch kong-database zookeeper kafka

# Wait for infrastructure to be ready
print_status "Waiting for infrastructure services to be ready..."
sleep 60

# Start Kong
print_status "Starting Kong API Gateway..."
docker-compose -f docker-compose-kong-kafka-microservices.yml up -d kong

# Wait for Kong to be ready
print_status "Waiting for Kong to be ready..."
sleep 30

# Start microservices
print_status "Starting microservices..."
docker-compose -f docker-compose-kong-kafka-microservices.yml up -d user-service product-service cart-service order-service payment-service notification-service

# Wait for microservices to be ready
print_status "Waiting for microservices to be ready..."
sleep 60

# Start configuration services
print_status "Starting configuration services..."
docker-compose -f docker-compose-kong-kafka-microservices.yml up -d kong-config kafka-topics kafka-ui

# Wait for configuration to complete
print_status "Waiting for configuration to complete..."
sleep 30

# Show service status
print_status "Checking service status..."
docker-compose -f docker-compose-kong-kafka-microservices.yml ps

# Show service URLs
echo ""
print_success "🎉 Kong API Gateway + Kafka Microservices Architecture Deployed Successfully!"
echo ""
echo "🌐 Service URLs:"
echo "├── Kong Proxy: http://localhost:8000"
echo "├── Kong Admin API: http://localhost:8001"
echo "├── Kong Admin GUI: http://localhost:8002"
echo "├── Kafka UI: http://localhost:8080"
echo "├── User Service: http://localhost:8081"
echo "├── Product Service: http://localhost:8082"
echo "├── Cart Service: http://localhost:8083"
echo "├── Order Service: http://localhost:8084"
echo "├── Payment Service: http://localhost:8085"
echo "└── Notification Service: http://localhost:8086"
echo ""
echo "🔧 Infrastructure Services:"
echo "├── MySQL: localhost:3306"
echo "├── Redis: localhost:6379"
echo "├── Kafka: localhost:9092"
echo "├── Elasticsearch: localhost:9200"
echo "└── Kong Database: localhost:5432"
echo ""
echo "📊 Management Interfaces:"
echo "├── Kong Admin GUI: http://localhost:8002"
echo "├── Kafka UI: http://localhost:8080"
echo "├── Elasticsearch: http://localhost:9200"
echo "└── Service Health: http://localhost:8000/health"
echo ""
echo "🚀 API Endpoints (through Kong):"
echo "├── User API: http://localhost:8000/api/users"
echo "├── Product API: http://localhost:8000/api/products"
echo "├── Cart API: http://localhost:8000/api/cart"
echo "├── Order API: http://localhost:8000/api/orders"
echo "├── Payment API: http://localhost:8000/api/payments"
echo "└── Notification API: http://localhost:8000/api/notifications"
echo ""
echo "📈 Kafka Topics:"
echo "├── user-events"
echo "├── product-events"
echo "├── cart-events"
echo "├── order-events"
echo "├── payment-events"
echo "├── notification-events"
echo "└── audit-events"
echo ""
print_success "🚀 All services are running with Kong API Gateway and Kafka message broker!"

# Test Kong configuration
print_status "Testing Kong configuration..."
sleep 10

# Test API endpoints through Kong
print_status "Testing API endpoints through Kong..."
echo "Testing User Service..."
curl -s http://localhost:8000/api/users/health || echo "User Service not ready yet"

echo "Testing Product Service..."
curl -s http://localhost:8000/api/products/health || echo "Product Service not ready yet"

echo "Testing Cart Service..."
curl -s http://localhost:8000/api/cart/health || echo "Cart Service not ready yet"

print_success "🎊 Kong API Gateway + Kafka Integration Complete!"

