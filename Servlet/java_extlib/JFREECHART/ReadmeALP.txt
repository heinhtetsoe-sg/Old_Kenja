# kanji=漢字
# $Id: ReadmeALP.txt,v 1.3 2007-05-22 05:59:26 takaesu Exp $
# 2007-02-07 Takaesu

jfreechart-1.0.3.tar.gz	Webから入手

----
◆オンライン購入
jfreechart-1.0.3-A4.pdf	開発者ガイド
jfreechart-1.0.3-demo.zip	デモのソース

----
1) jfreechart-1.0.3.jar を解凍
2) cd ant; ant
3) 以下が必要なファイル
	jfreechart-1.0.3.jar
	jfreechart-1.0.3-javadocs.zip
	lib/jcommon-1.0.6.jar
	src/
4) 以下はサンプル
	jfreechart-1.0.3-demo.jar

----
◆デモ(サンプル)実行方法
このフォルダ上で
  java -jar jfreechart-1.0.3-demo.jar
を実行する。

jar ファイルが関連付けされていれば、エクスプローラ上で該当ファイルをWクリックでも動くはず。

Class-Path は以下の通り。(すなわち、jfreechart-1.0.3-demo.jar と lib/ は同じフォルダ)
	jfreechart-1.0.3-demo.jar
	lib/jfreechart-1.0.3.jar
	lib/jcommon-1.0.6.jar
	lib/itext-1.4.6.jar
	lib/jfreechart-1.0.3-experimental.jar
