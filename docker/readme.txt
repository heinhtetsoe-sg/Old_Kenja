
追って環境構築の詳細手順書を作成するため、仮置き。

1.必須ドライバーをダウンロードし、php-apache/resourceディレクトリに格納する。

・PDO_IBM-1.3.6.tgz
    https://pecl.php.net/package/PDO_IBM

・ibm_data_server_driver_package_linuxx64_v11.5.tar.gz
    https://www.ibm.com/support/pages/node/387577


2.docker-composeコマンドにてデプロイ

・docker-compose build

・docker-compose up -d
※ここまでで、ローカル環境が立ち上がる

・docker-compose ps …コンテナが有効か確認する。

・docker exec -it [コンテナ名] /bin/bash
※これでコンテナ内にターミナル起動する。

3.バージョン確認
・php -v　…7.3.27であること。
・php -m　…pdo_ibmが存在すること。

4.http://localhost/htdocs/dbtest.phpにアクセスする。10.250.250.208に接続する記述になってる。