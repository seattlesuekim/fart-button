#!/system/bin/sh
# Keeps the package service registered. Intent listener + FartSession tick
# will replace this loop once the APK/receiver and ClimatePort exist.
export PATH=/data/nemesis/bin:/sbin:/vendor/bin:/system/sbin:/system/bin:/system/xbin
LOGDIR=/data/nemesis/logs/fart.button
STATEDIR=/data/nemesis/apps/fart.button/state
mkdir -p "$LOGDIR" "$STATEDIR" 2>/dev/null
echo "fart-daemon start $(date)" >> "$LOGDIR/daemon.log"
while true; do
    sleep 30
done
