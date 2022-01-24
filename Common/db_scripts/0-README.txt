kanji=漢字
$Id: 0-README.txt 56577 2017-10-22 11:35:50Z maeshiro $

2007年03月30日[金曜日]
  以下のディレクトリを新設した。
      db_scripts/kenja/common/tables/replace
  今後、変換スクリプトは、このフォルダで管理する。
  例) freshman_dat-1.1to1.5.sql
  db_scripts/kenja/common/tables/freshman_dat.sql ファイルの
  Rev1.1 → Rev1.5に変換するスクリプト。

----
2006年01月30日[月曜日]
  ディレクトリ構造を少しだけ変えた
      旧
      db_scripts
        |
        +-- kenja/
        +-- koumyou/
        +-- syougaku/

      新
      db_scripts
        |
        +-- gakuseki/
        |     +-- koumyou/
        |     +-- syougaku/
        +-- kenja/

eof
