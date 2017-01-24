Vagrant.configure(2) do |config|
  config.vm.box = "bento/ubuntu-16.04"
  config.vm.network "private_network", ip: "192.168.99.12"
  # root権限で ./provision.sh を実行する
  config.vm.provision :shell, :path => "./provision.sh", :privileged => true
  # ホストと時間を揃える
  config.vm.provider :virtualbox do |vb|
    vb.customize ["setextradata", :id, "VBoxInternal/Devices/VMMDev/0/Config/GetHostTimeDisabled", 0]
  end
end