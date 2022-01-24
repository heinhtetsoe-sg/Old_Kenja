# kanji=漢字
# $Id: readme.txt 56586 2017-10-22 12:52:35Z maeshiro $
            
2012/05/17  1. KNJD615 (rev.1.19)をコピーして作成
            2. 総合順位出力に「コースグループ」を追加、「 同一クラスでコース毎に改頁する」をカット
            3. 総合順位出力のデフォルトを「コースグループ」に変更

2012/07/04  1.パラメータにuseCurriculumcdを追加

2013/08/12  1.プロパティーuseVirus、useKekkaJisu、useKekka、useLatedetail追加

2013/08/14  1.プロパティーuseKoudome追加

2013/08/15  1.DI_CD'19','20'ウイルス/DI_CD'25','26'交止追加に伴う修正
            -- rep-attend_semes_dat.sql(rev1.9)
            -- rep-attend_subclass_dat.sql(rev1.11)
            -- v_attend_semes_dat.sql(rev1.7)
            -- v_attend_subclass_dat.sql(rev1.4)
            -- v_school_mst.sql(rev1.20)

2014/05/22  1.修正
            -- 勤怠コード27が入力された日付は一日出欠の授業日数にカウントしない
            -- 勤怠コード27が入力された日付校時は科目出欠の授業時数にカウントしない

2014/08/22  1.style指定修正

2015/06/02  1.勤怠コード'28'は時間割にカウントしない
            2.CSV出力で総合点が表示されない不具合修正
            3.CSV出力にコースグループ順位を追加

2016/08/12  1.学年コンボの参照先をSCHREG_REGD_GDATに変更

2017/09/20  1.DI_CD(29-32)追加

2017/10/03  1.ATTEND_DI_CD_DAT移行
            -- attend_di_cd_dat.sql(1.2)
