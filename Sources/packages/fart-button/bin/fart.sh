#!/system/bin/sh
# Manual launch from Manager (dev / backup trigger).
# Full climate + My Menu wiring lands after the on-car HVAC spike.
export PATH=/data/nemesis/bin:/sbin:/vendor/bin:/system/sbin:/system/bin:/system/xbin
PKG=/data/nemesis/apps/fart.button
LOGDIR=/data/nemesis/logs/fart.button
mkdir -p "$LOGDIR" 2>/dev/null
echo "fart.button: launch $(date)" >> "$LOGDIR/app.log"
echo "fart.button: OEM climate adapter not installed yet; host core is test-fart only" >> "$LOGDIR/app.log"
echo "fart.button: OEM climate not wired (see package README)"
exit 0
