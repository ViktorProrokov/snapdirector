############################################################################
######################## script for SDFS mount #############################

### usage: sudo ./mount_sdfs.sh <aws key id> <aws security key> <SDFS volume size> <cloud bucket name>

### passing variables from command line arguments
aws_key_id="$1"
aws_sec_key="$2"
sdfs_volume_size="$3"
bucket_name="$4"

### creating SDFS file system
mkfs.sdfs  --volume-name=awspool --volume-capacity=$sdfs_volume_size --aws-enabled=true --cloud-access-key=$aws_key_id --cloud-bucket-name=$bucket_name --cloud-secret-key=$aws_sec_key --chunk-store-encrypt=true

sleep 5

### adding extended config line to SDFS config file
if [[ -e /etc/sdfs/awspool-volume-cfg.xml ]]; then
  grep -q -e '<extended-config local-cache-size="0 B" read-speed="0" write-speed="0"/>' '/etc/sdfs/awspool-volume-cfg.xml'
  if [ $? -eq 1 ]; then
    sed -i '11i<extended-config local-cache-size="0 B" read-speed="0" write-speed="0"/>' '/etc/sdfs/awspool-volume-cfg.xml'
    echo ' Extended config line is inserted '
  fi
else
    echo ' Could not find /etc/sdfs/awspool-volume-cfg.xml check if SDFS volume created! '
    exit 1
fi

### creating mount directory
if [[ ! -e /mnt/awspool ]]; then
    mkdir /mnt/awspool
else
    echo ' /mnt/awspool directory exists! '
fi

touch /var/log/sdfs_mount.log

### mounting SDFS file system to /mnt/awspool
if mount | grep /mnt/awspool > /dev/null; then
    umount /mnt/awspool
    echo ' ********** SDFS sucessfully unmounted ********** '
    exit 0
else
    mount.sdfs awspool /mnt/awspool &> /var/log/sdfs_mount.log &
    echo ' ********** SDFS sucessfully mounted ********** '
    exit 0
fi
