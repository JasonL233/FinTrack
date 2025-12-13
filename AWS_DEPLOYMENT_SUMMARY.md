# AWS Deployment Summary

## Overview

The FinTrack microservices application is deployed on AWS using a cloud-native architecture with ECS Fargate, RDS PostgreSQL, Application Load Balancer, and AWS-managed services. The deployment infrastructure is defined using AWS CloudFormation, with automated deployment scripts and ECS task definitions.

## Architecture Components

### 1. Networking Infrastructure

#### VPC (Virtual Private Cloud)
- **CIDR Block**: `10.0.0.0/16`
- **DNS Support**: Enabled for both hostnames and resolution
- **Purpose**: Isolated network environment for all FinTrack resources

#### Subnets
- **Public Subnets** (2 zones for high availability):
  - `10.0.1.0/24` - Availability Zone 1
  - `10.0.2.0/24` - Availability Zone 2
  - **Purpose**: Host Application Load Balancer and NAT Gateways
  - **Public IP**: Auto-assigned

- **Private Subnets** (2 zones for high availability):
  - `10.0.11.0/24` - Availability Zone 1
  - `10.0.12.0/24` - Availability Zone 2
  - **Purpose**: Host ECS tasks and RDS databases (enhanced security)

#### Internet Gateway
- Provides internet access for public subnets
- Enables ALB to accept external traffic

#### Route Tables
- **Public Route Table**: Routes `0.0.0.0/0` to Internet Gateway
- **Private Route Tables**: (Not explicitly defined in CloudFormation, would typically use NAT Gateway for outbound internet)

### 2. Security Groups

#### ALB Security Group
- **Inbound Rules**:
  - Port 80 (HTTP) from `0.0.0.0/0`
  - Port 443 (HTTPS) from `0.0.0.0/0`
- **Purpose**: Allow public access to the Application Load Balancer

#### ECS Security Group
- **Inbound Rules**:
  - Ports 8080-8083 from ALB Security Group
- **Purpose**: Allow ALB to route traffic to ECS containers

#### RDS Security Group
- **Inbound Rules**:
  - Port 5432 (PostgreSQL) from ECS Security Group
- **Purpose**: Allow ECS tasks to connect to RDS databases
- **No Public Access**: Databases are not publicly accessible

### 3. Compute: ECS (Elastic Container Service)

#### ECS Cluster
- **Name**: `fintrack-cluster`
- **Launch Type**: Fargate (serverless containers)
- **Capacity Providers**:
  - `FARGATE` - Standard pricing
  - `FARGATE_SPOT` - Cost-optimized (up to 70% savings)
- **Default Strategy**: Fargate with weight 1

#### Task Definitions

##### User Service
- **Family**: `fintrack-user-service`
- **Resources**: 256 CPU units, 512 MB memory
- **Container Port**: 8081
- **Image**: ECR repository `fintrack-user-service:latest`
- **Environment Variables**:
  - `DB_HOST`: RDS endpoint
  - `DB_USER_PORT`: 5432
  - `DB_USER`: postgres
  - `JWT_EXPIRATION`: 86400000
- **Secrets** (from AWS Secrets Manager):
  - `DB_PASSWORD`
  - `JWT_SECRET`
- **Health Check**: `GET /api/users/health` every 30 seconds
- **Logs**: CloudWatch Log Group `/ecs/fintrack-user-service`

##### Transaction Service
- **Family**: `fintrack-transaction-service`
- **Resources**: 256 CPU units, 512 MB memory
- **Container Port**: 8082
- **Image**: ECR repository `fintrack-transaction-service:latest`
- **Environment Variables**:
  - `DB_HOST`: RDS endpoint
  - `DB_TRANSACTION_PORT`: 5432
  - `DB_USER`: postgres
  - `JWT_EXPIRATION`: 86400000
  - `KAFKA_BOOTSTRAP_SERVERS`: Kafka endpoint
- **Secrets** (from AWS Secrets Manager):
  - `DB_PASSWORD`
  - `JWT_SECRET`
- **Health Check**: `GET /api/transactions/health` every 30 seconds
- **Logs**: CloudWatch Log Group `/ecs/fintrack-transaction-service`

##### API Gateway
- **Family**: `fintrack-api-gateway`
- **Resources**: 256 CPU units, 512 MB memory
- **Container Port**: 8080
- **Image**: ECR repository `fintrack-api-gateway:latest`
- **Environment Variables**:
  - `JWT_EXPIRATION`: 86400000
  - `USER_SERVICE_URL`: `http://fintrack-user-service.local:8081`
  - `TRANSACTION_SERVICE_URL`: `http://fintrack-transaction-service.local:8082`
- **Secrets** (from AWS Secrets Manager):
  - `JWT_SECRET`
- **Health Check**: `GET /actuator/health` every 30 seconds
- **Logs**: CloudWatch Log Group `/ecs/fintrack-api-gateway`

##### Notification Service
- **Family**: `fintrack-notification-service` (task definition not included, but log group exists)
- **Logs**: CloudWatch Log Group `/ecs/fintrack-notification-service`

### 4. Database: RDS PostgreSQL

#### Users Database
- **Instance Identifier**: `fintrack-users-db`
- **Engine**: PostgreSQL 15.4
- **Instance Class**: `db.t3.micro`
- **Storage**: 20 GB GP2
- **Database Name**: `fintrack_users`
- **Master Username**: `postgres`
- **Master Password**: Retrieved from AWS Secrets Manager
- **Network**: Private subnets only
- **Public Access**: Disabled
- **Multi-AZ**: Disabled (single-AZ for cost optimization)
- **Backup Retention**: 7 days
- **Security**: Only accessible from ECS Security Group

#### Transactions Database
- **Instance Identifier**: `fintrack-transactions-db`
- **Engine**: PostgreSQL 15.4
- **Instance Class**: `db.t3.micro`
- **Storage**: 20 GB GP2
- **Database Name**: `fintrack_transactions`
- **Master Username**: `postgres`
- **Master Password**: Retrieved from AWS Secrets Manager
- **Network**: Private subnets only
- **Public Access**: Disabled
- **Multi-AZ**: Disabled (single-AZ for cost optimization)
- **Backup Retention**: 7 days
- **Security**: Only accessible from ECS Security Group

#### RDS Subnet Group
- **Name**: `fintrack-rds-subnet-group`
- **Subnets**: Private Subnet 1 and Private Subnet 2
- **Purpose**: Ensures RDS instances are deployed in multiple AZs for high availability

### 5. Load Balancing

#### Application Load Balancer (ALB)
- **Name**: `fintrack-alb`
- **Type**: Internet-facing (public)
- **Scheme**: External
- **Subnets**: Public Subnet 1 and Public Subnet 2
- **Security Group**: ALB Security Group
- **Listeners**: Port 80 (HTTP) - forwards to API Gateway target group

#### Target Group
- **Name**: `fintrack-api-gateway-tg`
- **Type**: IP-based (for Fargate)
- **Protocol**: HTTP
- **Port**: 8080
- **Health Check**:
  - Path: `/actuator/health`
  - Protocol: HTTP
  - Interval: 30 seconds
  - Timeout: 5 seconds
  - Healthy Threshold: 2 consecutive successes
  - Unhealthy Threshold: 3 consecutive failures

### 6. Logging: CloudWatch

#### Log Groups
All services have dedicated CloudWatch Log Groups:
- `/ecs/fintrack-user-service` (7-day retention)
- `/ecs/fintrack-transaction-service` (7-day retention)
- `/ecs/fintrack-api-gateway` (7-day retention)
- `/ecs/fintrack-notification-service` (7-day retention)

#### Log Configuration
- **Driver**: `awslogs`
- **Stream Prefix**: `ecs`
- **Region**: `us-east-1`
- **Retention**: 7 days (configurable)

### 7. Secrets Management: AWS Secrets Manager

#### Secrets
- `fintrack/db/password` - Database master password
- `fintrack/jwt/secret` - JWT signing secret

**Usage**: Task definitions reference secrets using ARNs, and ECS injects them as environment variables at runtime.

## Deployment Process

### Automated Deployment Script (`deploy.sh`)

The deployment script automates the following steps:

1. **ECR Repository Creation**
   - Creates Elastic Container Registry repositories for all services:
     - `fintrack-user-service`
     - `fintrack-transaction-service`
     - `fintrack-api-gateway`
     - `fintrack-notification-service`
   - Enables image scanning on push
   - Uses AES256 encryption

2. **Docker Image Build and Push**
   - Builds Docker images for each service from their Dockerfiles
   - Tags images with `latest`
   - Pushes images to respective ECR repositories
   - Authenticates with ECR using AWS CLI

3. **ECS Cluster Creation**
   - Creates ECS cluster named `fintrack-cluster`
   - Configures Fargate and Fargate Spot capacity providers
   - Sets default capacity provider strategy

### Manual Deployment Steps

After running the automated script, the following steps must be completed manually:

1. **Infrastructure Deployment**
   - Deploy CloudFormation stack using `infrastructure.yml`
   - Creates VPC, subnets, security groups, RDS, ALB, and ECS cluster

2. **Secrets Creation**
   - Create secrets in AWS Secrets Manager:
     - `fintrack/db/password`
     - `fintrack/jwt/secret`

3. **Task Definition Updates**
   - Update task definition JSON files with:
     - Actual AWS Account ID
     - Actual RDS endpoints (from CloudFormation outputs)
     - Actual Kafka endpoint (if using MSK or external Kafka)
     - Actual IAM role ARNs for task execution

4. **Task Registration**
   - Register task definitions using AWS CLI or Console:
     ```bash
     aws ecs register-task-definition --cli-input-json file://task-definitions/user-service-task.json
     aws ecs register-task-definition --cli-input-json file://task-definitions/transaction-service-task.json
     aws ecs register-task-definition --cli-input-json file://task-definitions/api-gateway-task.json
     ```

5. **Service Creation**
   - Create ECS services for each task definition
   - Configure service discovery (if using service mesh)
   - Set desired task count, auto-scaling policies, and deployment strategies

6. **ALB Configuration**
   - Verify target group health checks
   - Configure listener rules if needed
   - Set up SSL/TLS certificates for HTTPS (recommended)

## Service Communication

### Service Discovery

Services communicate using:
- **Internal Service Names**: ECS Service Discovery or internal DNS
  - User Service: `http://fintrack-user-service.local:8081`
  - Transaction Service: `http://fintrack-transaction-service.local:8082`
- **Alternative**: Use Cloud Map service discovery for dynamic DNS resolution

### Network Flow

1. **External Request**: Internet → ALB (Port 80) → API Gateway (Port 8080)
2. **Internal Routing**: API Gateway → User Service / Transaction Service
3. **Database Access**: Services → RDS (Port 5432) via private subnets
4. **Event Streaming**: Transaction Service → Kafka → Notification Service

## Security Features

### Network Security
- **Private Subnets**: ECS tasks and databases isolated from public internet
- **Security Groups**: Least-privilege access (only necessary ports open)
- **No Public RDS**: Databases cannot be accessed from internet

### Secrets Management
- **AWS Secrets Manager**: Sensitive data (passwords, JWT secrets) stored securely
- **No Hardcoded Secrets**: All secrets injected at runtime
- **IAM Integration**: ECS tasks use IAM roles to access secrets

### Authentication & Authorization
- **JWT Tokens**: Stateless authentication using signed JWTs
- **API Gateway**: Centralized authentication and authorization
- **Service-to-Service**: Services validate JWT tokens independently

## Scalability & High Availability

### Horizontal Scaling
- **ECS Services**: Can scale tasks based on CloudWatch metrics
- **Load Balancing**: ALB distributes traffic across multiple tasks
- **Multi-AZ**: Services deployed across multiple availability zones

### Auto Scaling (Recommended)
Configure ECS Auto Scaling based on:
- CPU utilization
- Memory utilization
- Request count per target
- Custom CloudWatch metrics

### Database Scaling
- **Read Replicas**: Can create read replicas for read-heavy workloads
- **Vertical Scaling**: Upgrade instance class if needed
- **Storage Auto-Scaling**: Enable storage autoscaling for RDS

## Monitoring & Observability

### CloudWatch Metrics
- ECS task metrics (CPU, memory, network)
- ALB metrics (request count, latency, error rates)
- RDS metrics (CPU, connections, storage)
- Custom application metrics

### CloudWatch Logs
- Centralized logging for all services
- Log retention: 7 days (configurable)
- Log aggregation and search capabilities

### Health Checks
- **ECS Task Health**: Container health checks every 30 seconds
- **ALB Health Checks**: Target group health checks every 30 seconds
- **Spring Boot Actuator**: `/actuator/health` endpoints

## Cost Optimization

### Current Configuration
- **Fargate**: Pay-per-use container compute
- **Fargate Spot**: Up to 70% cost savings for fault-tolerant workloads
- **db.t3.micro**: Cost-effective database instances
- **Single-AZ RDS**: Lower cost (can enable Multi-AZ for production)
- **7-Day Log Retention**: Reduced log storage costs

### Cost Savings Recommendations
1. Use Fargate Spot for non-critical services (notification-service)
2. Enable Reserved Capacity for predictable workloads
3. Implement auto-scaling to scale down during low traffic
4. Use S3 for log archival instead of CloudWatch for long-term retention
5. Enable RDS storage autoscaling to avoid over-provisioning

## CloudFormation Outputs

The CloudFormation stack exports the following values:

- **VPCId**: VPC ID for reference in other stacks
- **PublicSubnet1Id**: Public subnet 1 ID
- **PublicSubnet2Id**: Public subnet 2 ID
- **PrivateSubnet1Id**: Private subnet 1 ID
- **PrivateSubnet2Id**: Private subnet 2 ID
- **ECSClusterName**: ECS cluster name
- **LoadBalancerDNS**: ALB DNS name (public endpoint)
- **UsersDatabaseEndpoint**: RDS endpoint for users database
- **TransactionsDatabaseEndpoint**: RDS endpoint for transactions database

## Prerequisites

Before deployment, ensure:

1. **AWS CLI** configured with appropriate credentials
2. **Docker** installed and running
3. **IAM Permissions** for:
   - ECR (create repositories, push images)
   - ECS (create cluster, register tasks, create services)
   - CloudFormation (create/update stacks)
   - Secrets Manager (create/read secrets)
   - RDS (create instances)
   - VPC (create networking resources)
   - CloudWatch (create log groups)

## Required IAM Roles

### ECS Task Execution Role
- **Purpose**: Allows ECS tasks to pull images from ECR and write logs to CloudWatch
- **Required Permissions**:
  - `ecr:GetAuthorizationToken`
  - `ecr:BatchCheckLayerAvailability`
  - `ecr:GetDownloadUrlForLayer`
  - `ecr:BatchGetImage`
  - `logs:CreateLogStream`
  - `logs:PutLogEvents`
  - `secretsmanager:GetSecretValue`

### ECS Task Role
- **Purpose**: Allows tasks to access AWS services on behalf of the application
- **Required Permissions**: (Depends on application needs)
  - Access to S3 buckets
  - Access to other AWS services

## Deployment Configuration

### Region
- **Default**: `us-east-1`
- **Configurable**: Set in `deploy.sh` script

### Project Name
- **Default**: `fintrack`
- **Configurable**: CloudFormation parameter `ProjectName`

### Environment
- **Default**: `production`
- **Allowed Values**: `development`, `staging`, `production`
- **Configurable**: CloudFormation parameter `Environment`

## Known Limitations & Considerations

### Current Architecture
1. **No NAT Gateway**: Private subnets don't have internet access (ECS tasks can't pull external dependencies unless using VPC endpoints)
2. **No Service Mesh**: Service-to-service communication uses basic DNS
3. **No HTTPS**: ALB only configured for HTTP (should add HTTPS for production)
4. **No Kafka Managed Service**: Kafka endpoint must be configured separately (MSK, Confluent Cloud, or external)
5. **Single-AZ RDS**: No automatic failover (consider Multi-AZ for production)

### Recommended Enhancements
1. **Add NAT Gateway** for private subnet internet access
2. **Configure HTTPS** with ACM certificate
3. **Enable Multi-AZ RDS** for production workloads
4. **Set up AWS MSK** (Managed Streaming for Apache Kafka)
5. **Implement Service Mesh** (AWS App Mesh) for advanced traffic management
6. **Add WAF** (Web Application Firewall) for DDoS protection
7. **Enable VPC Flow Logs** for network monitoring
8. **Implement CI/CD Pipeline** (CodePipeline, CodeBuild, CodeDeploy)
9. **Add CloudWatch Alarms** for automated alerting
10. **Configure Auto Scaling Policies** for dynamic resource management

## File Structure

```
aws-deployment/
├── cloudformation/
│   └── infrastructure.yml      # CloudFormation template for infrastructure
├── scripts/
│   └── deploy.sh               # Automated deployment script
└── task-definitions/
    ├── api-gateway-task.json
    ├── transaction-service-task.json
    └── user-service-task.json
```

## Deployment Commands

### 1. Deploy Infrastructure
```bash
aws cloudformation create-stack \
  --stack-name fintrack-infrastructure \
  --template-body file://aws-deployment/cloudformation/infrastructure.yml \
  --parameters ParameterKey=ProjectName,ParameterValue=fintrack \
               ParameterKey=Environment,ParameterValue=production \
  --capabilities CAPABILITY_IAM
```

### 2. Run Deployment Script
```bash
cd aws-deployment/scripts
chmod +x deploy.sh
./deploy.sh
```

### 3. Register Task Definitions
```bash
aws ecs register-task-definition \
  --cli-input-json file://aws-deployment/task-definitions/user-service-task.json

aws ecs register-task-definition \
  --cli-input-json file://aws-deployment/task-definitions/transaction-service-task.json

aws ecs register-task-definition \
  --cli-input-json file://aws-deployment/task-definitions/api-gateway-task.json
```

### 4. Create ECS Services
```bash
# Get outputs from CloudFormation
ALB_TG_ARN=$(aws cloudformation describe-stacks \
  --stack-name fintrack-infrastructure \
  --query 'Stacks[0].Outputs[?OutputKey==`APIGatewayTargetGroupArn`].OutputValue' \
  --output text)

# Create API Gateway service
aws ecs create-service \
  --cluster fintrack-cluster \
  --service-name fintrack-api-gateway \
  --task-definition fintrack-api-gateway \
  --desired-count 2 \
  --launch-type FARGATE \
  --network-configuration "awsvpcConfiguration={subnets=[subnet-xxx,subnet-yyy],securityGroups=[sg-xxx],assignPublicIp=ENABLED}" \
  --load-balancers targetGroupArn=$ALB_TG_ARN,containerName=api-gateway,containerPort=8080
```

## Next Steps

1. **Complete Infrastructure Setup**: Deploy CloudFormation stack
2. **Create Secrets**: Set up AWS Secrets Manager secrets
3. **Update Task Definitions**: Replace placeholders with actual values
4. **Build and Push Images**: Run deployment script
5. **Register Task Definitions**: Register all task definitions
6. **Create ECS Services**: Deploy services to the cluster
7. **Configure Service Discovery**: Set up Cloud Map for service-to-service communication
8. **Test Deployment**: Verify all services are healthy and communicating
9. **Set Up Monitoring**: Configure CloudWatch alarms and dashboards
10. **Enable Auto Scaling**: Configure scaling policies for production workloads

