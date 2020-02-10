$onInit = <<-SCRIPT
	apt-get update
	apt-get install -y openjdk-8-jre-headless
	mkdir -p /opt/faker
	FAKER_SERVICE=/etc/init.d/faker
	cp -f /vagrant/faker.service $FAKER_SERVICE
	chown root:root $FAKER_SERVICE
	chmod 755 $FAKER_SERVICE
	update-rc.d -f faker remove
	update-rc.d faker defaults
SCRIPT

Vagrant.configure("2") do |config|
  config.vm.box = "ubuntu/xenial64"
  config.vm.provision "shell", inline: $onInit
end