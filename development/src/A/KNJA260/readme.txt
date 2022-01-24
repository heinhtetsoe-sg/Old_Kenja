# kanji=漢字
# $Id: readme.txt 64754 2019-01-18 04:45:03Z matsushima $

2007/03/13 s_yama 期末2を追加

2007/07/10  出欠集計日付の出力を追加
            得点データの出力を変更、～_DIの値が、'-'/'='の場合は、'-'/'='を出力それ以外は得点データ
            抽出対象生徒のSQLをWITH文にした
            欠課時数にATTEND_SUBCLASS_DATから取得したデータを出力する

2007/07/11  誤字を修正した。結果次数→欠課時数
            試験(××_SCORE)の場合も欠課時数を出力する

2007/07/18  出欠集計日付を追加した。
            欠課時数の取得方法を変更した。
            -- ATTEND_SUBCLASS：ATTEND_SUBCLASS_DAT
                                出欠集計日付以前の月データを集計
            -- SCHEDULE       ：SCH_CHR_DATから日付、校時、講座を取得
                                ０校時以外且つ、端数の開始日付～出欠集計日付
            -- T_attend_dat   ：ATTEND_DATから学籍番号、日付、校時、DI_CDを取得
                                端数の開始日付～出欠集計日付且つ、
                                SCHREG_TRANSFER_DATの1と2で日付が開始～終了日付の人は除く
            -- T_attend       ：①(SCHEDULE、CHAIR_STD_DAT、CHAIR_DAT、SCH_T(対象生徒))
                                対象生徒の受講講座でSCH_CHR_COUNTFLGのCOUNTFLGが０以外且つ、
                                学籍基礎の卒業以外で、移動者でないデータ
                                ②T_attend_dat(①に、LEFT JOIN)
                                ①と②を生徒、科目、学期でグループ化
                                ②のDI_CDにより各項目を求める。下記参照。
                                ABSENT   : SUM(CASE S2.DI_CD WHEN  '1' THEN 1 WHEN '8' THEN 1 ELSE 0 END)
                                SUSPEND  : SUM(CASE S2.DI_CD WHEN  '2' THEN 1 WHEN '9' THEN 1 ELSE 0 END)
                                MOURNING : SUM(CASE S2.DI_CD WHEN  '3' THEN 1 WHEN '10' THEN 1 ELSE 0 END)
                                SICK     : SUM(CASE S2.DI_CD WHEN  '4' THEN 1 WHEN '11' THEN 1 ELSE 0 END)
                                NOTICE   : SUM(CASE S2.DI_CD WHEN  '5' THEN 1 WHEN '12' THEN 1 ELSE 0 END)
                                NONOTICE : SUM(CASE S2.DI_CD WHEN  '6' THEN 1 WHEN '13' THEN 1 ELSE 0 END)
                                NURSEOFF : SUM(CASE S2.DI_CD WHEN '14' THEN 1 ELSE 0 END)
                                LATE     : SUM(CASE S2.DI_CD WHEN '15' THEN 1 ELSE 0 END)
                                EARLY    : SUM(CASE S2.DI_CD WHEN '16' THEN 1 ELSE 0 END)
            -- ATTEND_SUM     ：①ATTEND_SUBCLASS、SCH_T(対象生徒)
                                一致する生徒を抽出し、生徒、科目、学期でグループ化
                                ATTEND_SUBCLASSの下記項目をSUMする。
                                LESSON,ABSENT,SUSPEND,MOURNING,SICK,NOTICE,NONOTICE,NURSEOFF,LATE,EARLY
                                ②T_attendをUNION ALLで取得する。
            -- ATTEND_SUM2    ：ATTEND_SUM
                                生徒、科目、学期でグループ化して、下記をSUMする。(※１も求める)
                                LESSON,ABSENT,SUSPEND,MOURNING,SICK,NOTICE,NONOTICE,NURSEOFF,LATE,EARLY

                                ※１
                                if (($absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                                    $query .= "      ,decimal((float(sum(LATE) + sum(EARLY)) / ".$absent_cov_late.") + (sum(NOTICE) + sum(NONOTICE) + sum(SICK)),4,1) as NOTICE_LATE ";
                                } elseif (($absent_cov == "1") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                                    $query .= "      ,((sum(LATE) + sum(EARLY)) / ".$absent_cov_late.") + (sum(NOTICE) + sum(NONOTICE) + sum(SICK)) as NOTICE_LATE ";
                                } else {
                                    $query .= "      ,sum(NOTICE) + sum(NONOTICE) + sum(SICK) as NOTICE_LATE ";
                                }
            -- ATTEND_SUM3    ：ATTEND_SUM2
                                生徒、科目でグループ化して、下記をSUMする。(※２も求める)
                                NOTICE_LATE

                                ※２
                                if (($absent_cov == "4") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                                    $query .= "      ,decimal((float(sum(LATE) + sum(EARLY)) / ".$absent_cov_late.") + (sum(NOTICE) + sum(NONOTICE) + sum(SICK)),4,1) as NOTICE_LATE2 ";
                                } elseif (($absent_cov == "2") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                                    $query .= "      ,((sum(LATE) + sum(EARLY)) / ".$absent_cov_late.") + (sum(NOTICE) + sum(NONOTICE) + sum(SICK)) as NOTICE_LATE2 ";
                                } else {
                                    $query .= "      ,sum(NOTICE) + sum(NONOTICE) + sum(SICK) as NOTICE_LATE2 ";
                                }
            -- ATTEND_T       ：SCH_T(対象生徒)、ATTEND_SUM3
                                一致する生徒を抽出し下記条件で出力データを切り替える。
                                if (($absent_cov == "1" || $absent_cov == "3") && (is_numeric($absent_cov_late) && (int)$absent_cov_late != 0)) {
                                    $query .= "    NOTICE_LATE1 AS NOTICE_LATE ";
                                } else {
                                    $query .= "    NOTICE_LATE2 AS NOTICE_LATE ";
                                }

2007/09/26  結果時数処理の修正。(NURSEOFFも加算する)
2008/02/15  SQL修正。３年生の３学期は除外する条件を削除。
            SQL修正。学年末指定時の欠課時数の不具合を修正。

2008/09/10  テスト種別コンボのテーブルを
            prgInfo.propertiesを参照するように修正

2008/09/10  テスト種別コンボの該当データなし時の
            エラー表示修正

2008/09/11  CSV出力内容の修正。

2008/09/16  1.テスト種別コンボの修正（"評価成績"追加等）。
            2.テスト種別のテーブルをprgInfo.propertiesから参照に伴い
              成績データ取得ＳＱＬを修正した。
            3.学年評定取得の条件を変更

2009/01/07  1.fopen関数の第一引数のパス指定の不具合を修正した。
            2.テスト取得ＳＱＬにTESTKINDCDの条件を追加した。

2009/05/11  1.学校マスタLESSON_ABSENT_DAYSでの、処理切り分け。

2009/05/20  1.学校マスタLESSON_ABSENT_DAYSでの、処理切り分け。

2010/06/09  1.プロパティーファイルの参照方法を共通関数を使うよう修正

2010/08/16  1.出欠コード「23＝遅刻２、24＝遅刻３」の追加に伴い修正。

2010/09/15  1.集計フラグを参照するテーブルを以下の通りに修正。
            -- [SCH_CHR_DATのDATADIV = '2']
            --     テスト項目マスタの集計フラグを参照する。
            -- [SCH_CHR_DATのDATADIV = '0'または'1']
            --     SCH_CHR_COUNTFLGの集計フラグを参照する。

2011/03/11  1.XLS出力処理追加
            -- プロパティー追加：useXLS(何かセットされていれば、XLS出力。基本'1'をセット。)

2011/03/18  1.テンプレートを1本に統一

2011/03/30  1.以下の修正をした
            -- 単独で呼び出された場合閉じる
            -- 親から呼ばれた場合は、親の権限を使用する

2011/04/01  1.テンプレートのパス修正

2011/04/08  1.以下の通り修正。
            -- テストコンボが表示されず、文言「該当データなし」の場合、
            -- 出力ボタンをグレー（押し下げ不可）にする。

2011/04/15  1.権限修正。
            -- AUTHORITY ==> $model->auth

2011/12/09  1.プロパティー「useCurriculumcd」を追加した。
            -- '1'がセットされている場合は教育課程コード等を使うようにした。

2012/05/17  1.プロパティーuseCurriculumcd追加

2012/10/25  1.更新可能制限付きの権限を追加

2013/08/12  1.プロパティーuseKekkaJisu、useKekka、useLatedetail追加

2013/08/14  1.プロパティーuseKoudome追加
            2.DI_CD'19','20'ウイルス/DI_CD'25','26'交止追加に伴う修正
            -- rep-attend_semes_dat.sql(rev1.9)
            -- rep-attend_subclass_dat.sql(rev1.11)
            -- v_attend_semes_dat.sql(rev1.7)
            -- v_attend_subclass_dat.sql(rev1.4)
            -- v_school_mst.sql(rev1.20)

2013/08/15  1.テスト種別のDBエラー回避対応

2014/05/22  1.修正
            -- 勤怠コード27が入力された日付は一日出欠の授業日数にカウントしない
            -- 勤怠コード27が入力された日付校時は科目出欠の授業時数にカウントしない

2014/09/08  1.style指定修正

2015/06/02  1.勤怠コード'28'は時間割にカウントしない

2017/09/20  1.DI_CD(29-32)追加

2017/10/02  1.ATTEND_DI_CD_DAT移行
            -- attend_di_cd_dat.sql(1.2)

2019/01/18  1.CSV出力の文字化け修正(Edge対応)