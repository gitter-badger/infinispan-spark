#!/bin/bash
set -e
set -x 

# Number of nodes in the cluster (default 1)
N=${1:-1}

FLAVOUR="m1.large"
IMAGE="f2df087c-4e54-4047-98c0-8e03dbf6412b"  
KEY_NAME="gustavo"
SECURITY_GROUPS="default,spark,infinispan_server"
METADATA_MASTER="--meta ansible_host_groups=spark,master,infinispan"
METADATA_SLAVE="--meta ansible_host_groups=spark,slave,infinispan"

START=1
for (( c=$START; c<=$N; c++))
do
  [[ $c = 1 ]] && METADATA="$METADATA_MASTER" || METADATA="$METADATA_SLAVE"
  SERVER=$(nova boot --flavor $FLAVOUR --image $IMAGE --security-groups $SECURITY_GROUPS --key-name $KEY_NAME $METADATA node$c | grep " id " | awk '{print $4}') 
  STATUS=''
  while [[ "$STATUS" != "ACTIVE" ]];
  do
    STATUS=$(nova show $SERVER | grep status | awk '{print $4'})
    sleep 5 
  done
  IP=$(nova floating-ip-create os1_public | grep os1_public | awk '{ print $2 }')
  nova floating-ip-associate $SERVER $IP
done


ansible-playbook --user fedora -i inventory.py server.yaml -f $N
