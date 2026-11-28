#!/bin/sh
# Crea en AWS (us-east-1, junto a Supabase): key pair, security group, EC2 t3.micro con Docker e IP elástica.
# Uso: sh deploy/aws-up.sh   → guarda "ID IP" en deploy/.instance
set -eu
export AWS_REGION=us-east-1
N=sakura-demo
cd "$(dirname "$0")"
MYIP=$(curl -s https://checkip.amazonaws.com)/32

[ -f $N.pem ] || { aws ec2 create-key-pair --key-name $N --query KeyMaterial --output text > $N.pem; chmod 400 $N.pem; }
VPC=$(aws ec2 describe-vpcs --filters Name=isDefault,Values=true --query 'Vpcs[0].VpcId' --output text)
SG=$(aws ec2 describe-security-groups --filters Name=group-name,Values=$N-sg --query 'SecurityGroups[0].GroupId' --output text)
if [ "$SG" = "None" ]; then
  SG=$(aws ec2 create-security-group --group-name $N-sg --description "Sakura: 80/443 publico, 22 solo mi IP" --vpc-id "$VPC" --query GroupId --output text)
  aws ec2 authorize-security-group-ingress --group-id "$SG" --protocol tcp --port 443 --cidr 0.0.0.0/0 >/dev/null
  aws ec2 authorize-security-group-ingress --group-id "$SG" --protocol tcp --port 80 --cidr 0.0.0.0/0 >/dev/null
  aws ec2 authorize-security-group-ingress --group-id "$SG" --protocol tcp --port 22 --cidr "$MYIP" >/dev/null
fi
AMI=$(aws ssm get-parameter --name /aws/service/ami-amazon-linux-latest/al2023-ami-kernel-default-x86_64 --query Parameter.Value --output text)
# 1 GB de RAM no alcanza para Spring Boot + Caddy sin swap: se añade 1 GB de swap.
ID=$(aws ec2 run-instances --image-id "$AMI" --instance-type t3.micro --key-name $N --security-group-ids "$SG" \
  --metadata-options HttpTokens=required \
  --user-data '#!/bin/sh
dd if=/dev/zero of=/swapfile bs=1M count=1024 && chmod 600 /swapfile && mkswap /swapfile && swapon /swapfile && echo "/swapfile swap swap defaults 0 0" >> /etc/fstab
dnf install -y docker && systemctl enable --now docker && usermod -aG docker ec2-user' \
  --tag-specifications "ResourceType=instance,Tags=[{Key=Name,Value=$N}]" --query 'Instances[0].InstanceId' --output text)
echo "instancia $ID"
aws ec2 wait instance-running --instance-ids "$ID"
ALLOC=$(aws ec2 allocate-address --domain vpc --tag-specifications "ResourceType=elastic-ip,Tags=[{Key=Name,Value=$N}]" --query AllocationId --output text)
aws ec2 associate-address --instance-id "$ID" --allocation-id "$ALLOC" >/dev/null
IP=$(aws ec2 describe-addresses --allocation-ids "$ALLOC" --query 'Addresses[0].PublicIp' --output text)
aws ec2 wait instance-status-ok --instance-ids "$ID"
echo "$ID $IP" > .instance
echo "IP elástica $IP"
