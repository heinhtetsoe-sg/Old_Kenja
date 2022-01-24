◆実行イメージ
cd ~takaesu/markServer/tmp/
java -cp /waslib/nao_package.jar:/waslib/commons-logging.jar:/waslib/commons-dbutils-1.0.jar:/opt/IBM/db2/V8.1/java/db2jcc.jar:/opt/IBM/db2/V8.1/java/db2jcc_license_cu.jar \
	jp.co.alp.kenja.batch.withus.markServer.Main.class \
		//localhost:50000/witestdb \
		2007-11-10 \
		-out ~takaesu/markServer/

◆コンパイルイメージ
javac -encoding sjis -d ~/markServer/tmp \
	-sourcepath src \
	src/jp/co/alp/kenja/batch/withus/markServer/{*.java,*/*.java} \
	-classpath /waslib/nao_package.jar:/waslib/commons-logging.jar:/waslib/commons-dbutils-1.0.jar -target 1.3