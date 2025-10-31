# Amazon Elastic Load Balancing (ELB) Terraform Configuration
# This creates a complete ELB setup for microservices architecture

terraform {
  required_version = ">= 1.0"
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}

provider "aws" {
  region = var.aws_region
}

# Data sources
data "aws_availability_zones" "available" {
  state = "available"
}

data "aws_vpc" "default" {
  default = true
}

data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

# Security Group for ELB
resource "aws_security_group" "elb_sg" {
  name_prefix = "ecommerce-elb-"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    from_port   = 80
    to_port     = 80
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  ingress {
    from_port   = 443
    to_port     = 443
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "ecommerce-elb-sg"
  }
}

# Security Group for Microservices
resource "aws_security_group" "microservices_sg" {
  name_prefix = "ecommerce-microservices-"
  vpc_id      = data.aws_vpc.default.id

  ingress {
    from_port       = 8080
    to_port         = 8086
    protocol        = "tcp"
    security_groups = [aws_security_group.elb_sg.id]
  }

  ingress {
    from_port   = 22
    to_port     = 22
    protocol    = "tcp"
    cidr_blocks = ["0.0.0.0/0"]
  }

  egress {
    from_port   = 0
    to_port     = 0
    protocol    = "-1"
    cidr_blocks = ["0.0.0.0/0"]
  }

  tags = {
    Name = "ecommerce-microservices-sg"
  }
}

# Application Load Balancer
resource "aws_lb" "ecommerce_alb" {
  name               = "ecommerce-alb"
  internal           = false
  load_balancer_type = "application"
  security_groups    = [aws_security_group.elb_sg.id]
  subnets            = data.aws_subnets.default.ids

  enable_deletion_protection = false

  tags = {
    Name = "ecommerce-alb"
  }
}

# Target Group for API Gateway
resource "aws_lb_target_group" "api_gateway" {
  name     = "ecommerce-api-gateway"
  port     = 8080
  protocol = "HTTP"
  vpc_id   = data.aws_vpc.default.id

  health_check {
    enabled             = true
    healthy_threshold   = 2
    interval            = 30
    matcher             = "200"
    path                = "/health"
    port                = "traffic-port"
    protocol            = "HTTP"
    timeout             = 5
    unhealthy_threshold = 3
  }

  tags = {
    Name = "ecommerce-api-gateway-tg"
  }
}

# Target Group for User Service
resource "aws_lb_target_group" "user_service" {
  name     = "ecommerce-user-service"
  port     = 8081
  protocol = "HTTP"
  vpc_id   = data.aws_vpc.default.id

  health_check {
    enabled             = true
    healthy_threshold   = 2
    interval            = 30
    matcher             = "200"
    path                = "/actuator/health"
    port                = "traffic-port"
    protocol            = "HTTP"
    timeout             = 5
    unhealthy_threshold = 3
  }

  tags = {
    Name = "ecommerce-user-service-tg"
  }
}

# Target Group for Product Service
resource "aws_lb_target_group" "product_service" {
  name     = "ecommerce-product-service"
  port     = 8082
  protocol = "HTTP"
  vpc_id   = data.aws_vpc.default.id

  health_check {
    enabled             = true
    healthy_threshold   = 2
    interval            = 30
    matcher             = "200"
    path                = "/actuator/health"
    port                = "traffic-port"
    protocol            = "HTTP"
    timeout             = 5
    unhealthy_threshold = 3
  }

  tags = {
    Name = "ecommerce-product-service-tg"
  }
}

# Target Group for Cart Service
resource "aws_lb_target_group" "cart_service" {
  name     = "ecommerce-cart-service"
  port     = 8083
  protocol = "HTTP"
  vpc_id   = data.aws_vpc.default.id

  health_check {
    enabled             = true
    healthy_threshold   = 2
    interval            = 30
    matcher             = "200"
    path                = "/actuator/health"
    port                = "traffic-port"
    protocol            = "HTTP"
    timeout             = 5
    unhealthy_threshold = 3
  }

  tags = {
    Name = "ecommerce-cart-service-tg"
  }
}

# Target Group for Order Service
resource "aws_lb_target_group" "order_service" {
  name     = "ecommerce-order-service"
  port     = 8084
  protocol = "HTTP"
  vpc_id   = data.aws_vpc.default.id

  health_check {
    enabled             = true
    healthy_threshold   = 2
    interval            = 30
    matcher             = "200"
    path                = "/actuator/health"
    port                = "traffic-port"
    protocol            = "HTTP"
    timeout             = 5
    unhealthy_threshold = 3
  }

  tags = {
    Name = "ecommerce-order-service-tg"
  }
}

# Target Group for Payment Service
resource "aws_lb_target_group" "payment_service" {
  name     = "ecommerce-payment-service"
  port     = 8085
  protocol = "HTTP"
  vpc_id   = data.aws_vpc.default.id

  health_check {
    enabled             = true
    healthy_threshold   = 2
    interval            = 30
    matcher             = "200"
    path                = "/actuator/health"
    port                = "traffic-port"
    protocol            = "HTTP"
    timeout             = 5
    unhealthy_threshold = 3
  }

  tags = {
    Name = "ecommerce-payment-service-tg"
  }
}

# Target Group for Notification Service
resource "aws_lb_target_group" "notification_service" {
  name     = "ecommerce-notification-service"
  port     = 8086
  protocol = "HTTP"
  vpc_id   = data.aws_vpc.default.id

  health_check {
    enabled             = true
    healthy_threshold   = 2
    interval            = 30
    matcher             = "200"
    path                = "/actuator/health"
    port                = "traffic-port"
    protocol            = "HTTP"
    timeout             = 5
    unhealthy_threshold = 3
  }

  tags = {
    Name = "ecommerce-notification-service-tg"
  }
}

# ALB Listener for HTTP (Port 80)
resource "aws_lb_listener" "http" {
  load_balancer_arn = aws_lb.ecommerce_alb.arn
  port              = "80"
  protocol          = "HTTP"

  default_action {
    type = "redirect"
    redirect {
      port        = "443"
      protocol    = "HTTPS"
      status_code = "HTTP_301"
    }
  }
}

# ALB Listener for HTTPS (Port 443)
resource "aws_lb_listener" "https" {
  load_balancer_arn = aws_lb.ecommerce_alb.arn
  port              = "443"
  protocol          = "HTTPS"
  ssl_policy        = "ELBSecurityPolicy-TLS-1-2-2017-01"
  certificate_arn   = var.ssl_certificate_arn

  default_action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.api_gateway.arn
  }
}

# ALB Listener Rules for Service Routing
resource "aws_lb_listener_rule" "user_service" {
  listener_arn = aws_lb_listener.https.arn
  priority     = 100

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.user_service.arn
  }

  condition {
    path_pattern {
      values = ["/api/users*"]
    }
  }
}

resource "aws_lb_listener_rule" "product_service" {
  listener_arn = aws_lb_listener.https.arn
  priority     = 200

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.product_service.arn
  }

  condition {
    path_pattern {
      values = ["/api/products*"]
    }
  }
}

resource "aws_lb_listener_rule" "cart_service" {
  listener_arn = aws_lb_listener.https.arn
  priority     = 300

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.cart_service.arn
  }

  condition {
    path_pattern {
      values = ["/api/cart*"]
    }
  }
}

resource "aws_lb_listener_rule" "order_service" {
  listener_arn = aws_lb_listener.https.arn
  priority     = 400

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.order_service.arn
  }

  condition {
    path_pattern {
      values = ["/api/orders*"]
    }
  }
}

resource "aws_lb_listener_rule" "payment_service" {
  listener_arn = aws_lb_listener.https.arn
  priority     = 500

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.payment_service.arn
  }

  condition {
    path_pattern {
      values = ["/api/payments*"]
    }
  }
}

resource "aws_lb_listener_rule" "notification_service" {
  listener_arn = aws_lb_listener.https.arn
  priority     = 600

  action {
    type             = "forward"
    target_group_arn = aws_lb_target_group.notification_service.arn
  }

  condition {
    path_pattern {
      values = ["/api/notifications*"]
    }
  }
}

# Auto Scaling Group for API Gateway
resource "aws_autoscaling_group" "api_gateway" {
  name                = "ecommerce-api-gateway-asg"
  vpc_zone_identifier = data.aws_subnets.default.ids
  target_group_arns   = [aws_lb_target_group.api_gateway.arn]
  health_check_type   = "ELB"
  health_check_grace_period = 300

  min_size         = 2
  max_size         = 10
  desired_capacity = 3

  launch_template {
    id      = aws_launch_template.api_gateway.id
    version = "$Latest"
  }

  tag {
    key                 = "Name"
    value               = "ecommerce-api-gateway"
    propagate_at_launch = true
  }
}

# Launch Template for API Gateway
resource "aws_launch_template" "api_gateway" {
  name_prefix   = "ecommerce-api-gateway-"
  image_id      = var.ami_id
  instance_type = var.instance_type

  vpc_security_group_ids = [aws_security_group.microservices_sg.id]

  user_data = base64encode(templatefile("${path.module}/user-data.sh", {
    service_name = "api-gateway"
    service_port = "8080"
  }))

  tag_specifications {
    resource_type = "instance"
    tags = {
      Name = "ecommerce-api-gateway"
    }
  }
}

# CloudWatch Alarms for Auto Scaling
resource "aws_cloudwatch_metric_alarm" "api_gateway_cpu_high" {
  alarm_name          = "ecommerce-api-gateway-cpu-high"
  comparison_operator = "GreaterThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = "300"
  statistic           = "Average"
  threshold           = "70"
  alarm_description   = "This metric monitors api gateway cpu utilization"
  alarm_actions       = [aws_autoscaling_policy.api_gateway_scale_up.arn]

  dimensions = {
    AutoScalingGroupName = aws_autoscaling_group.api_gateway.name
  }
}

resource "aws_cloudwatch_metric_alarm" "api_gateway_cpu_low" {
  alarm_name          = "ecommerce-api-gateway-cpu-low"
  comparison_operator = "LessThanThreshold"
  evaluation_periods  = "2"
  metric_name         = "CPUUtilization"
  namespace           = "AWS/EC2"
  period              = "300"
  statistic           = "Average"
  threshold           = "20"
  alarm_description   = "This metric monitors api gateway cpu utilization"
  alarm_actions       = [aws_autoscaling_policy.api_gateway_scale_down.arn]

  dimensions = {
    AutoScalingGroupName = aws_autoscaling_group.api_gateway.name
  }
}

# Auto Scaling Policies
resource "aws_autoscaling_policy" "api_gateway_scale_up" {
  name                   = "ecommerce-api-gateway-scale-up"
  scaling_adjustment     = 1
  adjustment_type        = "ChangeInCapacity"
  cooldown               = 300
  autoscaling_group_name = aws_autoscaling_group.api_gateway.name
}

resource "aws_autoscaling_policy" "api_gateway_scale_down" {
  name                   = "ecommerce-api-gateway-scale-down"
  scaling_adjustment     = -1
  adjustment_type        = "ChangeInCapacity"
  cooldown               = 300
  autoscaling_group_name = aws_autoscaling_group.api_gateway.name
}

# Outputs
output "alb_dns_name" {
  description = "DNS name of the load balancer"
  value       = aws_lb.ecommerce_alb.dns_name
}

output "alb_zone_id" {
  description = "Zone ID of the load balancer"
  value       = aws_lb.ecommerce_alb.zone_id
}

output "alb_arn" {
  description = "ARN of the load balancer"
  value       = aws_lb.ecommerce_alb.arn
}
