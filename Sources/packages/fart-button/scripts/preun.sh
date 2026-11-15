#!/system/bin/sh
export PATH=/data/nemesis/bin:/sbin:/vendor/bin:/system/sbin:/system/bin:/system/xbin
# Drop session snapshot so a partial uninstall cannot restore stale climate later.
rm -f /data/nemesis/apps/fart.button/state/active.snapshot 2>/dev/null
echo "fart.button preuninstall cleared snapshot"
exit 0
