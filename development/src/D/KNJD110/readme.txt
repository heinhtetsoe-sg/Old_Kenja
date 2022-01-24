# kanji=漢字
/*** KNJD110_出欠累積データ生成処理 readme.txt***/

2005/01/28 日指定、三部制対応
2005/01/28 ALP殿修正内容をkind110Query.inc(2005/01/04)とマージ
           ・knjd110Query_TOKYO.inc マージ前OCCカスタマイズファイル
           ・knjd110Query_ALP.inc   ALPカスタマイズファイル
2005/02/08 ALPカスタマイズ内容TEXTファイル20050104_alp_modify.txt
2005/02/08 ATTEND_SEMES_DAT.LATE(遅刻回数),ATTEND_SEMES_DAT.EARLY(早退回数)の集計より、3:忌引、10:1日忌引をはずす。東京-作業依頼書20050204-01
2005/03/01 画面表示文字 出力入力制御日付 → 出欠入力制御日付
2005/06/11 ATTEND_SUBCLASS_DAT(出欠科目別累積データ)集計時、SCH_CHR_COUNTFLG_DAT(時間割講座データ集計フラグ)の内容で、集計するしないを判定追加
2005/06/11 テーブル名変更SCH_CHR_COUNTFLG_DAT→SCH_CHR_COUNTFLG


2005/09/21 ALP修正---処理時間短縮のためkind110Query.incを修正
           ・knjd110Query_20050921.inc ALP修正前ファイル
2005.10.22 ALP修正(出停・忌引が一つでもあれば、遅刻・早退はカウントしない)
2005.11.02 ALP修正
           ・attend_semes_datテーブル変更による修正。offdays(休学日数),abroad(留学日数)を追加
           ・attend_subclass_datテーブル変更による修正。offdays(休学時数),abroad(留学時数)を追加
           ・集計から不在日数を除く
2006.04.07 ALP修正 NO002 o-naka 留学・休学中の出欠は集計しない。（attend_datから除く）
2006/05/08 o-naka alp NO003 出欠累積データ生成済みリストにて、表示順の不具合を修正。(学期＋年月の順に表示する)
2006/10/06 o-naka alp 「出欠入力制御日付」の更新ができない不具合を修正した。
2006/11/09 o-naka alp 「未実施チェック」の処理の不具合を修正した。
2007/05/18 o-naka alp 校時コード'0'は対象外としているロジックをカットした。
2007/07/24 o-naka alp 留学日数について、コアタイムの条件を追加した。
2008/03/22 o-naka alp SQL0407Nの対応。講座データに存在する条件に変更。

2009/01/21  1.新規テーブル（APPOINTED_DAY_MST）追加に伴い修正。

2009/03/26  1.「生成済みデータ削除」機能の追加。「日指定テキストボックス」の値のクリア処理。

2009/06/15  1.処理速度改善のためＳＱＬ修正。（sch_chr_datとchair_std_datの参照部分）

2009/08/07  1.実授業数の欠課数上限値テーブル（SCHREG_ABSENCE_HIGH_DAT）の生成処理を追加。

2009/10/07  1.実授業数の欠課数上限値テーブル（SCHREG_ABSENCE_HIGH_DAT）の生成処理を変更。
            -- 変更前：全て（4/1～3/31）の時間割を参照する。
            -- 変更後：実行月以下（実行月と過去）は集計済みテーブル（attend_subclass_dat）を参照し、実行月の翌月からは時間割を参照する。
            2.名称マスタ「C040」に登録されている、年度・学期・月は、実行不可とする。
            3.上限値の設定が実授業数の場合、上限値のコメント部分を表示する。

2009/10/08  1.実授業数の上限値の生成処理を変更。
            -- 変更前：実行月の翌月からは時間割を参照する。
            -- 変更後：実行月の指定日（締め日）の翌日からは時間割を参照する。

2009/10/19  1.文言「前回実行履歴情報」追加。

2009/11/24  1.生徒の特別活動科目グループ毎の欠課数上限値計算を実装した。
            -- schreg_absence_high_special_dat.sql-1.1

2009/11/25  1.特別活動グループコード999の欠課数上限値データを作成するようにした。
            -- v_school_mst.sql-1.6

2009/11/27  1.上限値の算出方法を変更
            -- 以下のV_SCHOOL_MST.JOUGENTI_SANSYUTU_HOUの値を参照
            -- 1.四捨五入・・・小数点第1位を四捨五入
            -- 2.切り上げ・・・小数点第1位を切り上げ
            -- 3.切り捨て・・・小数点第1位を切り捨て
            -- 4.実数・・・・・小数点第2位を四捨五入
            -- schreg_absence_high_dat.sql-1.3
            -- schreg_absence_high_special_dat.sql-1.2
            -- v_school_mst.sql-1.7

2010/02/18  1.生徒の欠席日付データ生成処理を追加した。
            -- ATTEND_SEMES_DAT(SICK,NOTICE,NONOTICE)をカウントした日付を登録する
            -- attend_absence_dat.sql-1.1

2010/03/08  1.上限値の算出を「授業時数」ではなく「出席すべき時数」で行うように変更

2010/06/03  1.合併先科目の上限値の生成処理を追加
            -- 合併元科目の授業数から算出（合併元科目の上限値からは算出しない）
            -- SCHREG_ABSENCE_HIGH_DAT テーブルのみ生成

2010/06/21  1.欠課数上限値テーブルの変更に伴い修正。
            -- フィールド追加「授業時数（LESSON）」
            -- rep-schreg_absence_high_dat_rev1.2.sql
            -- rep-schreg_absence_high_special_dat_rev1.2.sql

2010/08/10  1.出欠コード「23＝遅刻２、24＝遅刻３」の追加に伴い修正。

2010/09/01  1.特別活動を計算する分母の1時間当たりの授業時分の参照場所を変更
            -- v_school_mst_rev1.13.sql

2010/09/14  1.集計フラグを参照するテーブルを以下の通りに修正。
            -- [SCH_CHR_DATのDATADIV = '2']
            --     テスト項目マスタの集計フラグを参照する。
            -- [SCH_CHR_DATのDATADIV = '0'または'1']
            --     SCH_CHR_COUNTFLGの集計フラグを参照する。

2010/10/19  1.生成済みリストに登録者を追加

2010/10/28  1.夜間バッチの実行有無をプロパティーファイルに書き込む
            2.DOCUMENTROOTを使用するように修正
            3.以下の通り修正。
            -- 夜間バッチのプロパティーファイルがない場合、チェックボックス選択不可とする。
            -- 書き込み権限がない場合、チェックボックス選択時にエラーメッセージを表示する。

2011/02/21  1.以下の通り修正。
            -- SYUKESSEKI_HANTEI_HOU = '1' は、改訂版（１日出欠席の判定方法の変更版）
            -- v_school_mst_rev1.15.sql

2011/03/15  1.集計から不在日数を除く条件について、以下の通り修正。
            -- 修正前
            --     転学(2)・退学(3)者 但し異動日がEXECUTEDATEより小さい場合
            --     転入(4)・編入(5)者 但し異動日がEXECUTEDATEより大きい場合
            -- 修正後
            --     転学(2)・退学(3)者 但し異動日がEXECUTEDATEより小さい場合
            --     入学（1・2・3）・転入(4)・編入(5)者 但し異動日がEXECUTEDATEより大きい場合

2011/04/12  1.以下の通り修正。
            -- prgInfo.propertiesを参照し、AccumulateSummaryBatch = 1 の場合、
            -- 「夜間バッチを実行する 」チェックボックス選択不可とする。

2011/06/17  1.以下の通り修正。
            -- ATTEND_SUBCLASS_DETAIL_DATを作成。
            -- ATTEND_SEMES_DETAIL_DATを作成。
            -- [前提]
            -- attend_subclass_detail_dat.sql(1.2)
            -- attend_semes_detail_dat.sql(1.2)
            -- 名称マスタ「C002」「C003」

2011/11/04  1.ATTEND_DAY_DATの生成処理を追加した。
            -- ATTEND_SEMES_DAT(ABSENT,SUSPEND,MOURNING,SICK,NOTICE,NONOTICE,LATE,EARLY)
            -- をカウントした日付・出欠コード・出欠備考を登録する。
            -- rep-attend_day_dat_rev.1.1.sql

2012/03/16  1.ATTEND_DATのDI_REMARK_CDの追加に伴い修正
            -- ATTEND_DAY_DATのDI_REMARKに登録する仕様を変更
            -- DI_REMARK_CDがある場合、名称マスタ「C901」「NAME1」を登録

2012/07/02  1. [SCH_CHR_DATのDATADIV = '3'(実力テスト)]追加に伴う修正。
            -- [SCH_CHR_DATのDATADIV = '2']
            --     テスト項目マスタの集計フラグを参照する。
            -- [SCH_CHR_DATのDATADIV = '0'または'1'または'3']
            --     SCH_CHR_COUNTFLGの集計フラグを参照する。

2012/07/03  1.教育課程の追加、追加に伴う修正
            -- Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応

2013/08/13  1.DI_CD'19','20'ウイルス追加に伴う修正
            -- rep-attend_semes_dat.sql(rev1.8)
            -- rep-attend_subclass_dat.sql(rev1.10)
            -- v_attend_semes_dat.sql(rev1.6)
            -- v_attend_subclass_dat.sql(rev1.3)

2013/08/14  1.DI_CD'25','26'交止追加に伴う修正
            -- rep-attend_semes_dat.sql(rev1.9)
            -- rep-attend_subclass_dat.sql(rev1.11)
            -- v_attend_semes_dat.sql(rev1.7)
            -- v_attend_subclass_dat.sql(rev1.4)
            -- v_school_mst.sql(rev1.20)

2013/11/15  1.参照テーブルを変更
            -- 現状：SCHREG_BASE_MST
            -- 変更：SCHREG_ENT_GRD_HIST_DAT

2014/02/02  1.TESTITEM_MST_COUNTFLG_NEW_SDIV対応
            2.修正

2014/05/28  1.修正
            -- 勤怠コード27が入力された日付は一日出欠の授業日数にカウントしない
            -- 勤怠コード27が入力された日付校時は科目出欠の授業時数にカウントしない

2014/05/29  1.更新/削除等のログ取得機能を追加

2014/12/17  1.処理速度改善
            -- 約１５分 → 約３分

2015/05/11  1.フィールド追加に伴い修正
            -- ATTEND_SEMES_DETAIL_DAT.CNT_DECIMAL

2015/06/04  1.勤怠コード'28'は時間割にカウントしない

2016/12/22  1.職員番号マスク機能追加
            -- プロパティー「showMaskStaffCd」追加
            -- showMaskStaffCd = 4 | *
            -- この設定だと、下４桁以外は「*」でマスク
            -- 「showMaskStaffCd」が無いときは通常表示

2017/06/15  1.ログイン校種およびADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind
            --プロパティー追加：useSchool_KindField

2017/08/09  1.IE11からの仕様(document.allが廃止)に対応

2017/09/21  1.DI_CD(29-32)追加

2017/10/03  1.ATTEND_DI_CD_DAT移行
            -- attend_di_cd_dat.sql(1.2)

2018/04/26  1.講座の「1日出欠対象外」がONの時間割は1日出欠の母集団から対象外とする
            -- 影響するテーブルは ATTEND_SEMES_DAT、ATTEND_ABSENCE_DAT

2018/05/10  1.1日の時間割がすべて1日出欠対象外の場合も授業日数に含まれる不具合を修正。(前回の修正不具合対応)

2018/12/08  1.対象の除籍区分に6、7を追加

2019/03/12  1.下段の履歴表示の登録者は、
            -- 修正前：ATTEND_SEMES_DATの更新者のMAX職員番号
            -- 修正後：処理日付欄で表示したレコードの更新者（複数いたら、MAX職員番号）を表示する。

2019/09/09  1.APPOINTED_DAY_MSTに校種を追加に伴う修正
            -- プロパティー「useSchool_KindField = 1」の時、校種コンボを表示する
            -- プロパティー「useSchool_KindField = 1」の時、APPOINTED_DAY_MSTに校種を追加する

2019/09/10  1.前回の修正漏れ

2020/05/29  1.校時に実施時間（分）指定を追加 (名称マスタ「B001」名称予備3)し通常授業の実施時間（50分、学校マスタ他条件設定）で換算した時数(端数切り上げ)で更新する

2021/02/02  1.コード自動整形

2021/03/10  1.京都PHPバージョンアップ対応

2021/03/23  1.新プロパティー「notCreateAttendDayDat = 1」の時、ATTEND_DAY_DATを作成しない
