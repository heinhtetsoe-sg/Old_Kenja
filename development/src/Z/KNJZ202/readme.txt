# kanji=漢字
# $Id: readme.txt 56591 2017-10-22 13:04:39Z maeshiro $


2009/10/20  1.新規作成

2009/11/02  1.以下の通り仕様変更
            -- 名称マスタ「C040」は参照しないように変更
            -- 生成処理を帳票と同様に端数計算処理に変更

2009/11/24  1.生徒の特別活動科目グループ毎の欠課数上限値計算を実装した。
            -- schreg_absence_high_special_dat.sql-1.1

2009/11/25  1.特別活動グループコード999の欠課数上限値データを作成するようにした。

2009/11/27  1.上限値の算出方法を変更
            -- 以下のV_SCHOOL_MST.JOUGENTI_SANSYUTU_HOUの値を参照
            -- 1.四捨五入・・・小数点第1位を四捨五入
            -- 2.切り上げ・・・小数点第1位を切り上げ
            -- 3.切り捨て・・・小数点第1位を切り捨て
            -- 4.実数・・・・・小数点第2位を四捨五入

2010/03/08  1.上限値の算出を「授業時数」ではなく「出席すべき時数」で行うように変更

2010/06/03  1.合併先科目の上限値の生成処理を追加
            -- 合併元科目の授業数から算出（合併元科目の上限値からは算出しない）
            -- SCHREG_ABSENCE_HIGH_DAT テーブルのみ生成

2010/06/21  1.欠課数上限値テーブルの変更に伴い修正。
            -- フィールド追加「授業時数（LESSON）」
            -- rep-schreg_absence_high_dat_rev1.2.sql
            -- rep-schreg_absence_high_special_dat_rev1.2.sql

2010/09/01  1.特別活動を計算する分母の1時間当たりの授業時分の参照場所を変更
            -- v_school_mst_rev1.13.sql

2010/09/14  1.集計フラグを参照するテーブルを以下の通りに修正。
            -- [SCH_CHR_DATのDATADIV = '2']
            --     テスト項目マスタの集計フラグを参照する。
            -- [SCH_CHR_DATのDATADIV = '0'または'1']
            --     SCH_CHR_COUNTFLGの集計フラグを参照する。

2010/09/17  1.(rev1.9)に戻した。
            -- 集計フラグの修正は、後から行う。
            2.KNJZ201と同様に単位マスタの欠課数オーバーを更新する処理を追加
            -- 詳細は、下記メール参照
            -- Sent: Thursday, September 16, 2010 7:45 PM
            -- Subject: KNJZ202とKNJZ200の修正依頼

2012/07/18  1.教育課程の追加、追加に伴う修正 (前回の修正不足に対応）
            - Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応
             
2013/08/13  1.DI_CD'19','20'ウイルス追加に伴う修正
            -- rep-attend_semes_dat.sql(rev1.8)
            -- rep-attend_subclass_dat.sql(rev1.10)
            -- v_attend_semes_dat.sql(rev1.6)
            -- v_attend_subclass_dat.sql(rev1.3)
            
2013/08/14  1.DI_CD'19','20'ウイルス/DI_CD'25','26'交止追加に伴う修正
            -- rep-attend_semes_dat.sql(rev1.9)
            -- rep-attend_subclass_dat.sql(rev1.11)
            -- v_attend_semes_dat.sql(rev1.7)
            -- v_attend_subclass_dat.sql(rev1.4) 
            -- v_school_mst.sql(rev1.20)
            
2015/06/15  1.2学期制のJavaScriptエラー対応

2017/10/03  1.ATTEND_DI_CD_DAT移行
            -- attend_di_cd_dat.sql(1.2)
            2.記述ミス修正
