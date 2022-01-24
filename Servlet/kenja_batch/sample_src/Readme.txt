★★★ CSV←→DB を linux で実行。文字コード問題の調査 ★★★
Eclipse(Windows)上では問題なかった。
他に「改行コード問題」がある。

◆環境
[takaesu@withus javaDeCsv]$ find . -type f
./src/jp/co/alp/kenja/batch/sample/CSVReader.java
./src/jp/co/alp/kenja/batch/sample/CSVWriter.java
./src/jp/co/alp/kenja/batch/sample/CsvSample1.java
./tmp/jp/co/alp/kenja/batch/sample/CSVWriter.class
./tmp/jp/co/alp/kenja/batch/sample/CSVReader.class
./tmp/jp/co/alp/kenja/batch/sample/CsvSample1.class
[takaesu@withus javaDeCsv]$


◆コンパイル。
[takaesu@withus javaDeCsv]$ javac -encoding sjis -d ~/javaDeCsv/tmp/ \
	-sourcepath src \
	src/jp/co/alp/kenja/batch/sample/C*.java \
	-classpath /waslib/nao_package.jar:/waslib/commons-logging.jar:/waslib/commons-dbutils-1.0.jar \
	-target 1.3


◆実行(コマンドライン)
db2jcc*.jar は ~db2inst1/sqllib/{java,java12}/ 配下のだとNGだった。

java -cp /waslib/nao_package.jar:/waslib/commons-logging.jar:/waslib/commons-dbutils-1.0.jar:/opt/IBM/db2/V8.1/java/db2jcc.jar:/opt/IBM/db2/V8.1/java/db2jcc_license_cu.jar \
	jp.co.alp.kenja.batch.sample.CsvSample1.class

以下、実際の実行結果。
[takaesu@withus tmp]$ java -cp /waslib/nao_package.jar:/waslib/commons-logging.jar:/waslib/commons-dbutils-1.0.jar:/opt/IBM/db2/V8.1/java/db2jcc.jar:/opt/IBM/db2/V8.1/java/db2jcc_
license_cu.jar  jp.co.alp.kenja.batch.sample.CsvSample1.class
2008/02/01 15:57:34 jp.co.alp.kenja.batch.sample.CsvSample1 <init>
SEVERE: カレントディレクトリ=/home/takaesu/javaDeCsv/tmp
2008/02/01 15:57:34 jp.co.alp.kenja.batch.sample.CsvSample1 main
SEVERE: CSVファイルをDBに入れる。
システムプロパティの値 nao_package.db.DB2UDB.isDB2V8=null
ISDB2V8=true
url=jdbc:db2://withus:50000/WITESTDB
>>[宮沢賢治, 1896, 1933]
>>[稲垣足穂, 1900, 1977]
>>[三島由紀夫, 1925, 1970]
2008/02/01 15:57:35 jp.co.alp.kenja.batch.sample.CsvSample1 main
SEVERE: DBの内容をCSVファイル化。
url=jdbc:db2://withus:50000/WITESTDB
-->M1, ＤＶＤ
-->M2, ＶＯＤ
-->S1, 集中スクーリング
-->00, 宮沢賢治
-->01, 稲垣足穂
-->02, 三島由紀夫
[takaesu@withus tmp]$
