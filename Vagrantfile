$onInit = <<-SCRIPT
	apt-get update
	apt-get install -y openjdk-8-jre-headless
SCRIPT

$onBoot = <<-SCRIPT
	MOCK_DIR=$HOME/mocks
	LOG_FILE=$HOME/faker.log
	if [ -d "$MOCK_DIR" ]; then
		echo "$MOCK_DIR found. Starting faker ..."
		sh -c "$(curl -sSL https://era.li/pS4p76)" -s --source $MOCK_DIR > $LOG_FILE &
	fi
SCRIPT

Vagrant.configure("2") do |config|
  config.vm.box = "ubuntu/xenial64"
  config.vm.provision "shell", inline: $onInit
  config.vm.provision "shell", inline: $onBoot, run: "always", privileged: false
end
