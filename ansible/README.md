## Spark and Infinispan cluster on Openstack

Download and source the rc file from Openstack:

```
source openstack.rc
```

Install package novaclient using dnf/yum or your system's package manager:

```
dnf install python-novaclient'
```

Check the variable ```OS_NETWORK_NAME``` in the script file ```inventory.py``` to 
match the Openstack installation


To provision a cluster:

```
./create.sh <num_servers>
```
