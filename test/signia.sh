
cd
cd postgra

c0gen() {
  openssl genrsa -out rsakey.pem
  openssl rsa -in rsakey.pem -pubout > rsakey.pub
  ls -l rsakey.pem rsakey.pub
  cat rsakey.pem rsakey.pub
}

c0default() {
  c0gen
}

if [ $# -gt 0 ]
then
  command=$1
  shift
  c$#$command $@
else
  c0default
fi

