#!/usr/bin/env bash

apt-get install sysv-rc-conf

# Timezone
timedatectl set-timezone Asia/Tokyo

# JDK
apt-get install -y software-properties-common
add-apt-repository -y ppa:webupd8team/java
apt-get update
## ライセンス許諾のプロンプトが出ないようにする
echo debconf shared/accepted-oracle-license-v1-1 select true | debconf-set-selections
## `oracle-java8-set-default` は環境変数とかを設定してくれるらしい
apt-get install -y oracle-java8-installer oracle-java8-set-default

# Elasticsearch
wget -qO - https://artifacts.elastic.co/GPG-KEY-elasticsearch | apt-key add -
apt-get install apt-transport-https
echo "deb https://artifacts.elastic.co/packages/5.x/apt stable main" | tee -a /etc/apt/sources.list.d/elastic-5.x.list
apt-get update
apt-get install -y elasticsearch
## kuromojiプラグインをインストール
/usr/share/elasticsearch/bin/elasticsearch-plugin install analysis-kuromoji
## デフォルトメモリが2gになってるのでよしなに変更する
sed -i 's/-Xmx2g/-Xmx256m/' /etc/elasticsearch/jvm.options
sed -i 's/-Xms2g/-Xms256m/' /etc/elasticsearch/jvm.options
## デフォルトだとローカルのみなのでどこからでもアクセス出来るようにする(開発用なんで)
sed -i 's/#network.host: 192.168.0.1/network.host: 0.0.0.0/' /etc/elasticsearch/elasticsearch.yml
## Cross-Origin Resource Sharingを有効にする(開発用なんで * で)
echo "http.cors.enabled: true" >> /etc/elasticsearch/elasticsearch.yml
echo "http.cors.allow-origin: \"*\"" >> /etc/elasticsearch/elasticsearch.yml
sysv-rc-conf elasticsearch on
service elasticsearch start

# Elasticsearch Head
apt-get install -y nodejs npm
npm cache clean
## n を使うとさくっと最新のnodeが入れられる
npm install n -g
n stable
## 紛らわしいので削除しとく
apt-get purge -y nodejs npm
## Elasticserch5からsite pluginが使えなくなったのでStand Aloneサーバを使う
## https://www.elastic.co/jp/blog/running-site-plugins-with-elasticsearch-5-0
git clone git://github.com/mobz/elasticsearch-head.git
cd elasticsearch-head
/usr/local/bin/npm install
chown -R vagrant:vagrant ../elasticsearch-head
su - vagrant -c "cd /home/vagrant/elasticsearch-head && node_modules/grunt/bin/grunt server > server.log &"