������ CSV����DB �� linux �Ŏ��s�B�����R�[�h���̒��� ������
Eclipse(Windows)��ł͖��Ȃ������B
���Ɂu���s�R�[�h���v������B

����
[takaesu@withus javaDeCsv]$ find . -type f
./src/jp/co/alp/kenja/batch/sample/CSVReader.java
./src/jp/co/alp/kenja/batch/sample/CSVWriter.java
./src/jp/co/alp/kenja/batch/sample/CsvSample1.java
./tmp/jp/co/alp/kenja/batch/sample/CSVWriter.class
./tmp/jp/co/alp/kenja/batch/sample/CSVReader.class
./tmp/jp/co/alp/kenja/batch/sample/CsvSample1.class
[takaesu@withus javaDeCsv]$


���R���p�C���B
[takaesu@withus javaDeCsv]$ javac -encoding sjis -d ~/javaDeCsv/tmp/ \
	-sourcepath src \
	src/jp/co/alp/kenja/batch/sample/C*.java \
	-classpath /waslib/nao_package.jar:/waslib/commons-logging.jar:/waslib/commons-dbutils-1.0.jar \
	-target 1.3


�����s(�R�}���h���C��)
db2jcc*.jar �� ~db2inst1/sqllib/{java,java12}/ �z���̂���NG�������B

java -cp /waslib/nao_package.jar:/waslib/commons-logging.jar:/waslib/commons-dbutils-1.0.jar:/opt/IBM/db2/V8.1/java/db2jcc.jar:/opt/IBM/db2/V8.1/java/db2jcc_license_cu.jar \
	jp.co.alp.kenja.batch.sample.CsvSample1.class

�ȉ��A���ۂ̎��s���ʁB
[takaesu@withus tmp]$ java -cp /waslib/nao_package.jar:/waslib/commons-logging.jar:/waslib/commons-dbutils-1.0.jar:/opt/IBM/db2/V8.1/java/db2jcc.jar:/opt/IBM/db2/V8.1/java/db2jcc_
license_cu.jar  jp.co.alp.kenja.batch.sample.CsvSample1.class
2008/02/01 15:57:34 jp.co.alp.kenja.batch.sample.CsvSample1 <init>
SEVERE: �J�����g�f�B���N�g��=/home/takaesu/javaDeCsv/tmp
2008/02/01 15:57:34 jp.co.alp.kenja.batch.sample.CsvSample1 main
SEVERE: CSV�t�@�C����DB�ɓ����B
�V�X�e���v���p�e�B�̒l nao_package.db.DB2UDB.isDB2V8=null
ISDB2V8=true
url=jdbc:db2://withus:50000/WITESTDB
>>[�{�򌫎�, 1896, 1933]
>>[��_����, 1900, 1977]
>>[�O���R�I�v, 1925, 1970]
2008/02/01 15:57:35 jp.co.alp.kenja.batch.sample.CsvSample1 main
SEVERE: DB�̓��e��CSV�t�@�C�����B
url=jdbc:db2://withus:50000/WITESTDB
-->M1, �c�u�c
-->M2, �u�n�c
-->S1, �W���X�N�[�����O
-->00, �{�򌫎�
-->01, ��_����
-->02, �O���R�I�v
[takaesu@withus tmp]$
