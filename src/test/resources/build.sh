#!/bin/sh

if [ $# -ne 1 ]; then
  echo "usage: $0 <USER@HOST>"
  exit 1
fi

HOST=$1
ROOT=$(dirname $(realpath $0))

APT=10.65.54.116
DST=/opt/havis.app.etb

scp -r $ROOT/tar/* $HOST:/

ssh $HOST <<EOF
chmod 755 /etc/init.d/httpd $DST/testd $DST/www/cgi-bin/exec.sh
ln -s ../init.d/httpd /etc/rc.d/S40httpd
ln -s $DST/testd /etc/rc.d/S50testd
ln -s ../log.txt $DST/www/log.txt
cd $DST

wget -q http://$APT/ivy/havis/app/etb/1.0/havis.app.etb.jar
wget -q http://$APT/ivy/havis/util/monitor/1.0/havis.util.monitor.jar
EOF
