# kanji=漢字
# $Id: kfu_readme.txt 56576 2017-10-22 11:25:31Z maeshiro $

2007/12/11 プロパティーファイルの種類と用途
    ファイル：_remote.properties
    場所　　：0-ALP\CVS_WORK\php_kenja\withus(各サーバ名)
    用途    ：サーバ名を定義
    指定    ：server=@withus

    ファイル：_filedeploy.properties
    場所　　：0-ALP\CVS_WORK\php_kenja\_config
    用途    ：指定サーバのパスを定義
    指定    ：@withus=//withus/development

    ファイル：_dir.properties
    場所　　：0-ALP\CVS_WORK\php_kenja\_config
    用途    ：特殊なディレクトリ(*1) の場合の_filedeploy.properties以降のパス (srcdir)と、
            　対象となるファイル名の正規表現 (files)。
    指定    ：srcdir=src/P/KNJWP131
              files=m/(^.*readme.*\\.txt$|^index\\.php$|^knj.*\\..*$)/


    ○ 基本的な処理は、_remote.propertiesでサーバ名を取得。
       上記のサーバ名で、_filedeploy.propertiesから指定サーバのパスを取得。
       _dir.propertiesがなければ、/src/ファイルのKNJの次の文字(*2)/ファイル名


    *1 例えば、WITHUSはプログラム名のつけ方が、KNJWP131といったように
       賢者の命名規則(*2)に則っていない為、パス指定が必要となる。
       KNJWP131 → W：×
       KNJWP131 → P：○

    *2 KNJ以降の一文字
       例：KNJA131 → A
       例：KNJZ131 → Z
