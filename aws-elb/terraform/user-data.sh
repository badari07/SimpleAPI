#!/bin/bash

# User Data Script for EC2 instances running microservices
# This script sets up the microservice on the EC2 instance

set -e

# Update system
yum update -y

# Install Docker
yum install -y docker
systemctl start docker
systemctl enable docker
usermod -a -G docker ec2-user

# Install Docker Compose
curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
chmod +x /usr/local/bin/docker-compose

# Install Java 17
yum install -y java-17-amazon-corretto-headless

# Install Maven
yum install -y maven

# Create application directory
mkdir -p /opt/ecommerce
cd /opt/ecommerce

# Clone the microservices repository (replace with your actual repository)
# git clone https://github.com/your-username/ecommerce-microservices.git .

# For now, we'll create a simple health check endpoint
cat > /opt/ecommerce/health-check.sh << 'EOF'
#!/bin/bash
# Simple health check script
echo "Service is healthy"
exit 0
EOF

chmod +x /opt/ecommerce/health-check.sh

# Create systemd service for the microservice
cat > /etc/systemd/system/ecommerce-${service_name}.service << EOF
[Unit]
Description=Ecommerce ${service_name} Service
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/opt/ecommerce
ExecStart=/usr/bin/java -jar /opt/ecommerce/${service_name}-service.jar --server.port=${service_port}
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
EOF

# Enable and start the service
systemctl daemon-reload
systemctl enable ecommerce-${service_name}
systemctl start ecommerce-${service_name}

# Create health check endpoint
cat > /opt/ecommerce/health-endpoint.sh << 'EOF'
#!/bin/bash
# Health check endpoint for ELB
echo "HTTP/1.1 200 OK"
echo "Content-Type: text/plain"
echo "Content-Length: 13"
echo ""
echo "Service OK"
EOF

chmod +x /opt/ecommerce/health-endpoint.sh

# Install and configure nginx for health checks
yum install -y nginx

cat > /etc/nginx/conf.d/health.conf << EOF
server {
    listen 80;
    server_name _;
    
    location /health {
        access_log off;
        return 200 "healthy\n";
        add_header Content-Type text/plain;
    }
    
    location / {
        proxy_pass http://localhost:${service_port};
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
    }
}
EOF

systemctl start nginx
systemctl enable nginx

# Configure CloudWatch agent
yum install -y amazon-cloudwatch-agent

# Create CloudWatch agent configuration
cat > /opt/aws/amazon-cloudwatch-agent/etc/amazon-cloudwatch-agent.json << EOF
{
    "metrics": {
        "namespace": "Ecommerce/Microservices",
        "metrics_collected": {
            "cpu": {
                "measurement": ["cpu_usage_idle", "cpu_usage_iowait", "cpu_usage_user", "cpu_usage_system"],
                "metrics_collection_interval": 60
            },
            "disk": {
                "measurement": ["used_percent"],
                "metrics_collection_interval": 60,
                "resources": ["*"]
            },
            "mem": {
                "measurement": ["mem_used_percent"],
                "metrics_collection_interval": 60
            }
        }
    },
    "logs": {
        "logs_collected": {
            "files": {
                "collect_list": [
                    {
                        "file_path": "/var/log/messages",
                        "log_group_name": "/aws/ec2/ecommerce",
                        "log_stream_name": "{instance_id}/messages"
                    }
                ]
            }
        }
    }
}
EOF

# Start CloudWatch agent
systemctl start amazon-cloudwatch-agent
systemctl enable amazon-cloudwatch-agent

# Log completion
echo "Ecommerce ${service_name} service setup completed at $(date)" >> /var/log/ecommerce-setup.log
