#!/bin/bash

# Amazon Elastic Load Balancing (ELB) Deployment Script
# This script deploys the complete ELB setup for microservices

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

# Check if AWS CLI is installed
if ! command -v aws &> /dev/null; then
    print_error "AWS CLI is not installed. Please install AWS CLI first."
    exit 1
fi

# Check if Terraform is installed
if ! command -v terraform &> /dev/null; then
    print_error "Terraform is not installed. Please install Terraform first."
    exit 1
fi

# Check AWS credentials
if ! aws sts get-caller-identity &> /dev/null; then
    print_error "AWS credentials not configured. Please run 'aws configure' first."
    exit 1
fi

print_success "AWS CLI and Terraform are available!"
print_success "AWS credentials are configured!"

# Navigate to terraform directory
cd "$(dirname "$0")/terraform"

# Initialize Terraform
print_status "Initializing Terraform..."
terraform init

# Plan Terraform deployment
print_status "Planning Terraform deployment..."
terraform plan -out=tfplan

# Ask for confirmation
echo ""
print_warning "This will create AWS resources that may incur costs."
read -p "Do you want to proceed with the deployment? (y/N): " -n 1 -r
echo ""

if [[ ! $REPLY =~ ^[Yy]$ ]]; then
    print_status "Deployment cancelled by user."
    exit 0
fi

# Apply Terraform
print_status "Deploying AWS ELB infrastructure..."
terraform apply tfplan

# Get outputs
ALB_DNS_NAME=$(terraform output -raw alb_dns_name)
ALB_ARN=$(terraform output -raw alb_arn)

print_success "🎉 AWS ELB Infrastructure Deployed Successfully!"
echo ""
echo "🌐 Load Balancer Information:"
echo "├── DNS Name: $ALB_DNS_NAME"
echo "├── ARN: $ALB_ARN"
echo "└── HTTPS URL: https://$ALB_DNS_NAME"
echo ""
echo "🔧 Service Endpoints:"
echo "├── API Gateway: https://$ALB_DNS_NAME/api"
echo "├── User Service: https://$ALB_DNS_NAME/api/users"
echo "├── Product Service: https://$ALB_DNS_NAME/api/products"
echo "├── Cart Service: https://$ALB_DNS_NAME/api/cart"
echo "├── Order Service: https://$ALB_DNS_NAME/api/orders"
echo "├── Payment Service: https://$ALB_DNS_NAME/api/payments"
echo "└── Notification Service: https://$ALB_DNS_NAME/api/notifications"
echo ""
echo "📊 Monitoring:"
echo "├── CloudWatch: https://console.aws.amazon.com/cloudwatch/"
echo "├── EC2 Console: https://console.aws.amazon.com/ec2/"
echo "└── ELB Console: https://console.aws.amazon.com/ec2/v2/home#LoadBalancers:"
echo ""
print_success "🚀 Amazon Elastic Load Balancing is now active!"

# Create a summary file
cat > ../elb-deployment-summary.md << EOF
# AWS ELB Deployment Summary

## Load Balancer Information
- **DNS Name**: $ALB_DNS_NAME
- **ARN**: $ALB_ARN
- **Type**: Application Load Balancer (ALB)
- **Scheme**: Internet-facing

## Service Endpoints
- **API Gateway**: https://$ALB_DNS_NAME/api
- **User Service**: https://$ALB_DNS_NAME/api/users
- **Product Service**: https://$ALB_DNS_NAME/api/products
- **Cart Service**: https://$ALB_DNS_NAME/api/cart
- **Order Service**: https://$ALB_DNS_NAME/api/orders
- **Payment Service**: https://$ALB_DNS_NAME/api/payments
- **Notification Service**: https://$ALB_DNS_NAME/api/notifications

## Features Implemented
- ✅ Application Load Balancer (ALB)
- ✅ Auto Scaling Groups
- ✅ Health Checks
- ✅ SSL/TLS Termination
- ✅ CloudWatch Monitoring
- ✅ Security Groups
- ✅ Target Groups for each service

## Next Steps
1. Configure SSL certificate ARN in variables.tf
2. Update DNS records to point to the ALB
3. Deploy microservices to EC2 instances
4. Configure auto scaling policies
5. Set up monitoring and alerting

## Cost Optimization
- Use Spot Instances for non-critical workloads
- Configure auto scaling based on demand
- Monitor CloudWatch metrics for optimization
- Use Reserved Instances for predictable workloads
EOF

print_success "📄 Deployment summary saved to elb-deployment-summary.md"
