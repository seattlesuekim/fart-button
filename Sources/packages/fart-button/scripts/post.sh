#!/system/bin/sh
export PATH=/data/nemesis/bin:/sbin:/vendor/bin:/system/sbin:/system/bin:/system/xbin
mkdir -p /data/nemesis/apps/fart.button/state
mkdir -p /data/nemesis/logs/fart.button
chmod 755 /data/nemesis/apps/fart.button/bin/fart.sh 2>/dev/null
chmod 755 /data/nemesis/apps/fart.button/svc/daemon.sh 2>/dev/null
echo "fart.button postinstall ok"
exit 0
