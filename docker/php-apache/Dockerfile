#------------------------------------------------
# base_image
#------------------------------------------------
FROM php:7.3-apache

#------------------------------------------------
# linux pacage install or make directory
#------------------------------------------------
RUN apt-get update \
    && apt-get install -y unzip \
    && mkdir -p /opt/ibm/db2/

#-------------------------------------------------
# japanese UTF-8 setting
#-------------------------------------------------
RUN apt-get install -y locales
RUN locale-gen ja_JP.UTF-8
RUN localedef -f UTF-8 -i ja_JP ja_JP
ENV LANG ja_JP.UTF-8
ENV LANGUAGE ja_JP:jp
ENV LC_ALL ja_JP.UTF-8

#--------------------------------------------------
# apache2.conf,php.ini setting
#--------------------------------------------------
RUN apt-get install -y libxml2-dev \
    && cp "$PHP_INI_DIR/php.ini-production" "$PHP_INI_DIR/php.ini"

# COPY ./docker/php-apache/conf/000-default.conf /etc/apache2/sites-enabled/000-default.conf
# COPY ./docker/php-apache/conf/apache2.conf /etc/apache2/apache2.conf

#--------------------------------------------------
# driver(db2,pdo) install
#--------------------------------------------------
ADD ./docker/php-apache/resource/ibm_data_server_driver_package_linuxx64_v11.5.tar.gz /opt/ibm/db2/
ADD ./docker/php-apache/resource/PDO_IBM-1.3.6.tgz /tmp/

ENV IBM_DB_HOME=/opt/ibm/db2/dsdriver

RUN cd /opt/ibm/db2/ \
    && ln -s $IBM_DB_HOME/include /include \
    && /bin/bash $IBM_DB_HOME/installDSDriver

RUN cd /tmp/PDO_IBM-1.3.6 \
    && phpize \
    && ./configure --with-pdo-ibm=$IBM_DB_HOME/lib \
    && make -j "$(nproc)" \
    && make install

RUN docker-php-ext-enable  pdo_ibm

# -------------------------------------------------
# シンボリックリンク定義
# -------------------------------------------------
# COPY ./demo /usr/local/demo
# ENV SYMLNK_PATH=/usr/local/development/src
# # RUN mkdir -p /usr/local/demo/src
# RUN cd /usr/local/demo/src \
#     && ln -s $SYMLNK_PATH/A A \
#     && ln -s $SYMLNK_PATH/B B \
#     && ln -s $SYMLNK_PATH/C C \
#     && ln -s $SYMLNK_PATH/D D \
#     && ln -s $SYMLNK_PATH/E E \
#     && ln -s $SYMLNK_PATH/F F \
#     && ln -s $SYMLNK_PATH/G G \
#     && ln -s $SYMLNK_PATH/H H \
#     && ln -s $SYMLNK_PATH/I I \
#     && ln -s $SYMLNK_PATH/J J \
#     && ln -s $SYMLNK_PATH/L L \
#     && ln -s $SYMLNK_PATH/M M \
#     && ln -s $SYMLNK_PATH/O O \
#     && ln -s $SYMLNK_PATH/P P \
#     && ln -s $SYMLNK_PATH/S S \
#     && ln -s $SYMLNK_PATH/W W \
#     && ln -s $SYMLNK_PATH/X X \
#     && ln -s $SYMLNK_PATH/Z Z \
#     && ln -s $SYMLNK_PATH/image image \
#     && ln -s $SYMLNK_PATH/kyotuManual kyotuManual \
#     && ln -s $SYMLNK_PATH/maintenance maintenance \
#     && ln -s $SYMLNK_PATH/common/css common/css \
#     && ln -s $SYMLNK_PATH/common/development-bundle common/development-bundle \
#     && ln -s $SYMLNK_PATH/common/js common/js \
#     && ln -s $SYMLNK_PATH/common/kenja-design-sample common/kenja-design-sample

# ENV SYMLNK_PATH2=/usr/local/development/
# RUN cd /usr/local/demo \
#     && ln -s $SYMLNK_PATH2/lib lib \
#     && ln -s $SYMLNK_PATH2/template template \
#     && ln -s $SYMLNK_PATH2/svf svf


# -------------------------------------------------
# phpディレクトリコピー、既存ディレクトリと共存できなかった為、
# volumesマウント出来ず、リファクタリング必要
# -------------------------------------------------
COPY ./php /tmp/php
RUN cp -rf /tmp/php/ /usr/local/lib/