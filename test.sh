export GOOGLE_APPLICATION_CREDENTIALS=/usr/local/google/home/tcoffee/Desktop/pubsubgrpctest/src/main/resources/tcoffee-test-a021a69bda91.json
LOSS=0
while [ $LOSS -lt 100 ]; do
  echo $LOSS
  tc qdisc add dev em1 root netem loss $LOSS%
  timeout 10m java -jar ./target/pubsubgrpctest.jar &> output-$LOSS.txt
  tc qdisc del dev em1 root netem
  NEXT=$(printf %.f $(echo "100 - (100 - $LOSS)*0.9" | bc -l))
  if [ $LOSS -eq $NEXT ]
  then
    let NEXT+=1
  fi
  LOSS=$NEXT
done
