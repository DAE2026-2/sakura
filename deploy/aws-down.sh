#!/bin/sh
# Borra TODO lo de AWS: instancia (con su disco), IP elástica, security group y key pair. Supabase no se toca.
set -eu
export AWS_REGION=us-east-1
N=sakura-demo
cd "$(dirname "$0")"
for ID in $(aws ec2 describe-instances --filters Name=tag:Name,Values=$N Name=instance-state-name,Values=pending,running,stopped --query 'Reservations[].Instances[].InstanceId' --output text); do
  aws ec2 terminate-instances --instance-ids "$ID" >/dev/null; aws ec2 wait instance-terminated --instance-ids "$ID"; echo "terminada $ID"
done
for A in $(aws ec2 describe-addresses --filters Name=tag:Name,Values=$N --query 'Addresses[].AllocationId' --output text); do
  aws ec2 release-address --allocation-id "$A" && echo "liberada IP $A"
done
SG=$(aws ec2 describe-security-groups --filters Name=group-name,Values=$N-sg --query 'SecurityGroups[0].GroupId' --output text)
[ "$SG" != "None" ] && aws ec2 delete-security-group --group-id "$SG" && echo "borrado $SG"
aws ec2 delete-key-pair --key-name $N && rm -f $N.pem .instance && echo "borrado key pair"
