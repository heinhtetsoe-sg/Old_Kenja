・賢者夜間バッチ
	ant

・ウィザス、マークサーバ連携
	ant -f withus_build.xml

	・ICASSデータ移行
		1) ant -f withus_icass_migration_build.xml
		2) 以下の成果物をサーバに設置
			- withus_icass_migration.sh
			- withus_icass_migration.<Version>.jar
			- kenja_batch.jar
			- log4j.properties
		3) 実行は withus_icass_migration.sh

・熊本県教育庁、NBIグループウェア連携
