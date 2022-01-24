# kanji=漢字
# $Id: readme.txt 62320 2018-09-13 11:56:11Z nakamoto $

2012/03/12  1.新規作成(KNJD210KはIDが重複していたので、ID変更)

2012/06/26  1.教育課程の追加、追加に伴う修正
                - Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応

2013/03/27  1.以下の通り修正
            -- JVIEWSTAT_LEVEL_MSTを参照して算出するように修正
            -- JVIEWSTAT_LEVEL_MSTがあるデータのみ作成するように修正
            
2014/02/04  1.年組取得時の学校校種'P'の参照条件をカット

2014/07/31  1.参照テーブル修正 NAME_MST ⇒ JVIEWSTAT_LEVEL_MST
              - D029のABBV1 ⇒ ASSESSMARK
              - D029のNAMESPARE2 ⇒ ASSESSLEVEL
            2.レイアウト修正
            
2014/08/01  1.style指定修正

2018/08/15  1.プロパティーuseJviewstatLevel=JVIEWSTAT_LEVEL_SEMES_MSTの場合、同名のテーブルを使用。学期コンボも表示
            -- 各学期の場合、観点は生成しない。コメントも切替

2018/08/16  1.学年末でプロパティーunCreateJviewstatRecordDatSeme9=1の場合、
            -- 学年末の観点データを作成しない。コメントも切替
            -- 学年末の観点データを参照し、学年末の成績データを作成（JVIEWSTAT_INPUTSEQ_DATは参照しない）

2018/09/13  1.JVIEWSTAT_LEVEL_SEMES_MST or JVIEWSTAT_LEVEL_MSTのDIV='2'のVIEWCD='9999'を'0000'に変更（宮城さんから依頼）
