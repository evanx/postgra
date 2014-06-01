

server=ngena.com

c0wait() {
  while ! ls -l /pri/nb/postgra/dist/postgra.jar 
  do
    sleep 1
  done
}

c0rsyncApp() {
  rsync -ra /pri/nb/git/postgra/src/reader/web/* $server:/pri/postgra/app/.
}

c0rsync() {
  c0wait
  ssh $server touch /pri/postgra/.rsyncing
  ssh $server 'kill -HUP `pgrep -f postgra.jar`'
  rsync /pri/nb/postgra/dist/postgra.jar $server:postgra/.
  rsync /pri/nb/vellumcore/dist/vellumcore.jar $server:postgra/lib/.
  ssh $server rm -f /pri/postgra/.rsyncing
}

c0default() {
  c0rsync
}


if [ $# -gt 0 ]
then
  command=$1
  shift
  c$#$command
else
  c0default
fi


