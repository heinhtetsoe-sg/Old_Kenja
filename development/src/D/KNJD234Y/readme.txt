// kanji=漢字
// $Id: readme.txt 56586 2017-10-22 12:52:35Z maeshiro $

-----
概要：成績不振者を抽出して、評定と履修単位・修得単位の値を訂正する画面
-----

2010/03/04  1.新規作成。

2010/03/05  1.更新チェックボックスを追加
            -- 選択したデータを更新する。

2010/03/09  1.以下の通り修正
            -- jaguarでエラーがでたため、$thisをカット

2010/03/18  1.生徒の抽出条件に下記を追加
            -- SCHREG_BASE_MST の GRD_DATE が入力日付'以下'の人は対象外

2010/05/10  1.「教科・科目」「総合的な時間」チェックボックスを追加

2010/08/18  1.出欠コード「23＝遅刻２、24＝遅刻３」の追加に伴い修正。

2012/07/13  1.教育課程の追加、追加に伴う修正
            -- Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応

2013/08/19  1.DI_CD'19','20'ウイルス/DI_CD'25','26'交止追加に伴う修正
            -- rep-attend_semes_dat.sql(rev1.9)
            -- rep-attend_subclass_dat.sql(rev1.11)
            -- v_attend_semes_dat.sql(rev1.7)
            -- v_attend_subclass_dat.sql(rev1.4)
            -- v_school_mst.sql(rev1.20)

2014/05/22  1.修正
            -- 勤怠コード27が入力された日付は一日出欠の授業日数にカウントしない
            -- 勤怠コード27が入力された日付校時は科目出欠の授業時数にカウントしない

2015/06/02  1.勤怠コード'28'は時間割にカウントしない

2017/07/25  1.学校マスタ情報取得するとき、SCHOOLCD、SCHOOLKINDを渡すよう修正
            -- AttendAccumulate.php(1.7)に伴う修正

2017/09/20  1.DI_CD(29-32)追加

2017/10/02  1.ATTEND_DI_CD_DAT移行
            -- attend_di_cd_dat.sql(1.2)
