#!/bin/bash

# FinTrack AWS Deployment Script
# This script automates the entire AWS deployment process

set -e  # Exit on error

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Configuration
AWS_REGION="us-east-1"
AWS_ACCOUNT_ID=$(aws sts get-caller-identity --query Account --output text)
PROJECT_NAME="fintrack"

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}FinTrack AWS Deployment${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "AWS Account ID: $AWS_ACCOUNT_ID"
echo "AWS Region: $AWS_REGION"
echo ""

# Step 1: Create ECR Repositories
echo -e "${YELLOW}Step 1: Creating ECR Repositories...${NC}"

SERVICES=("user-service" "transaction-service" "api-gateway" "notification-service")

for service in "${SERVICES[@]}"; do
    echo "Creating ECR repository for $service..."
    aws ecr describe-repositories --repository-names "${PROJECT_NAME}-${service}" --region $AWS_REGION 2>/dev/null || \
    aws ecr create-repository \
        --repository-name "${PROJECT_NAME}-${service}" \
        --region $AWS_REGION \
        --image-scanning-configuration scanOnPush=true \
        --encryption-configuration encryptionType=AES256
done

echo -e "${GREEN}✓ ECR Repositories created${NC}"
echo ""

# Step 2: Build and Push Docker Images
echo -e "${YELLOW}Step 2: Building and Pushing Docker Images...${NC}"

# Login to ECR
aws ecr get-login-password --region $AWS_REGION | docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com

cd ../..  # Go to project root

for service in "${SERVICES[@]}"; do
    echo "Building ${service}..."
    docker build -t ${PROJECT_NAME}-${service}:latest -f ${service}/Dockerfile .
    
    echo "Tagging ${service}..."
    docker tag ${PROJECT_NAME}-${service}:latest ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${PROJECT_NAME}-${service}:latest
    
    echo "Pushing ${service}..."
    docker push ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com/${PROJECT_NAME}-${service}:latest
    
    echo -e "${GREEN}✓ ${service} pushed to ECR${NC}"
done

echo -e "${GREEN}✓ All images pushed to ECR${NC}"
echo ""

# Step 3: Create VPC and Networking
echo -e "${YELLOW}Step 3: Setting up VPC and Networking...${NC}"
echo "This step will be done via AWS Console or CloudFormation template"
echo ""

# Step 4: Create RDS Databases
echo -e "${YELLOW}Step 4: Creating RDS Databases...${NC}"
echo "This step will be done via AWS Console"
echo ""

# Step 5: Create ECS Cluster
echo -e "${YELLOW}Step 5: Creating ECS Cluster...${NC}"

aws ecs describe-clusters --clusters ${PROJECT_NAME}-cluster --region $AWS_REGION 2>/dev/null || \
aws ecs create-cluster \
    --cluster-name ${PROJECT_NAME}-cluster \
    --region $AWS_REGION \
    --capacity-providers FARGATE FARGATE_SPOT \
    --default-capacity-provider-strategy capacityProvider=FARGATE,weight=1

echo -e "${GREEN}✓ ECS Cluster created${NC}"
echo ""

echo -e "${GREEN}========================================${NC}"
echo -e "${GREEN}Deployment script completed!${NC}"
echo -e "${GREEN}========================================${NC}"
echo ""
echo "Next steps:"
echo "1. Create RDS databases via AWS Console"
echo "2. Update task definition files with actual ARNs"
echo "3. Create ECS services"
echo "4. Configure Application Load Balancer"
echo ""