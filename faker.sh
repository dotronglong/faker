#! /bin/sh

### BEGIN INIT INFO
# Provides:		        faker
# Required-Start:
# Required-Stop:
# Default-Start:	    2 3 4 5
# Default-Stop:
# Short-Description:	Faker server
### END INIT INFO

FAKER_NAME=faker
FAKER_DIR=/opt/faker
MOCK_DIR=$FAKER_DIR/mocks
LOG_FILE=$FAKER_DIR/$FAKER_NAME.log
cd $FAKER_DIR

case "$1" in
	start)
		if [ -d "$MOCK_DIR" ]; then
			echo "$MOCK_DIR found. Starting $FAKER_NAME ..."
			sh -c "$(curl -H 'cache-control: no-cache' -sSL https://era.li/pS4p76)" -s --source $MOCK_DIR > $LOG_FILE &
		fi
		;;
	stop)
		ps ax | grep $FAKER_NAME | grep -v grep | awk '{print $1}' | xargs -I $ kill -9 $
        rm -rf $LOG_FILE
		exit 0
		;;
	*)
		echo "Usage: $0 start|stop" >&2
		exit 3
		;;
esac
exit 0