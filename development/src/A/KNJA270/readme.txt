# kanji=漢字
# $Id: readme.txt 71226 2019-12-13 09:00:29Z ishii $
2006/03/14 alp o-naka 集計日付の不具合修正
2006/06/15 alp o-naka NO001 「科目別：対象学期までの累計」を追加
2006/06/20 alp o-naka NO002 NO001の修正内容について、近大高校にも対応。つまり、賢者版として修正。
2006/06/22 alp o-naka NO003 近大高校にて、通常授業でｃｓｖ出力したら最後の列に’/00/00’が出力される不具合を修正。
2006/06/26 alp o-naka NO004 集計日付・時数は、
①累積データの対象学期のみ－－＞欠席のみ出力のチェック有無に関係なく、集計日付は指定学期内のMAX月日を出力。時数も指定学期内の合計を出力。
②対象学期までの累計－－＞欠席のみ出力のチェック有無に関係なく、集計日付は指示画面の出欠集計日付を出力（対応済み）。時数も指示画面の出欠集計日付までの時数を出力。
2006/10/03 alp o-naka ＣＳＶ出力項目に 単位数 と 欠課数 を追加した。(科目別のみ)
2006/10/04 alp o-naka
★ ＣＳＶ出力項目名を下記の通り変更した。(科目別のみ)
-- 遅刻 ===> 遅刻（欠課数換算前の数字）
-- 早退 ===> 早退（欠課数換算前の数字）
2006/10/10 alp o-naka
★ ＣＳＶの項目名称について、表示位置を固定に変更した。
2007/06/07 alp o-naka
★ 「欠課数」を求めるＳＱＬが間違っていたので修正した。
-- 科目別・集計内容（対象学期までの累計）の場合のみ

2009/05/21  1.学校マスタを参照し、出力対象を変更

2009/05/25  1.休学と留学出力を追加

2009/08/31  1.通常授業に講座、講座担当職員出力を追加した。
            2.通常授業のSQLをリファクタリングした。

2009/09/04  1.通常授業の講座担当職員件数取得ＳＱＬに講座名簿日付範囲の条件を追加した。

2009/11/25  1.ラジオボタン、チェックボックスにラベル機能を追加した。

2010/06/09  1.プロパティーファイルの参照方法を共通関数を使うよう修正

2010/08/17  1.出欠コード「23＝遅刻２、24＝遅刻３」の追加に伴い修正。

2010/09/15  1.集計フラグを参照するテーブルを以下の通りに修正。
            -- [SCH_CHR_DATのDATADIV = '2']
            --     テスト項目マスタの集計フラグを参照する。
            -- [SCH_CHR_DATのDATADIV = '0'または'1']
            --     SCH_CHR_COUNTFLGの集計フラグを参照する。

2011/03/13  1.XLS出力処理追加
            -- プロパティー追加：useXLS(何かセットされていれば、XLS出力。基本'1'をセット。)

2011/03/18  1.テンプレートを1本に統一

2011/03/30  1.以下の修正をした
            -- 単独で呼び出された場合閉じる
            -- 親から呼ばれた場合は、親の権限を使用する

2011/04/01  1.テンプレートのパス修正

2011/12/14  1.プロパティー「useCurriculumcd」を追加した。
            -- '1'がセットされている場合は教育課程コード等を使うようにした。

2012/03/16  1.備考コード欄を追加した。

2012/07/19  1.学期のデータを学期マスタから取得するように修正した。

2012/07/23  1.教育課程用のパラメータ追加

2012/10/30  1.更新可能制限付きの権限を追加

2013/01/09  1.CLASS_MSTテーブルのCURRICULUM_CDをカット

2013/08/12  1.プロパティーuseKekkaJisu、useKekka、useLatedetail追加
            2.DI_CD'19','20'ウイルス追加に伴う修正
            -- rep-attend_semes_dat.sql(rev1.8)
            -- rep-attend_subclass_dat.sql(rev1.10)
            -- v_attend_semes_dat.sql(rev1.6)
            -- v_attend_subclass_dat.sql(rev1.3)

2013/08/14  1.プロパティーuseKoudome追加
            2.DI_CD'19','20'ウイルス/DI_CD'25','26'交止追加に伴う修正
            -- rep-attend_semes_dat.sql(rev1.9)
            -- rep-attend_subclass_dat.sql(rev1.11)
            -- v_attend_semes_dat.sql(rev1.7)
            -- v_attend_subclass_dat.sql(rev1.4)
            -- v_school_mst.sql(rev1.20)

2014/05/22  1.修正
            -- 勤怠コード27が入力された日付は一日出欠の授業日数にカウントしない
            -- 勤怠コード27が入力された日付校時は科目出欠の授業時数にカウントしない

2014/05/26  1.更新/削除等のログ取得機能を追加

2014/06/12  1.TESTITEM_MST_COUNTFLG_NEW_SDIV対応
            -- プロパティーuseTestCountflgを参照する

2015/06/02  1.勤怠コード'28'は時間割にカウントしない

2016/09/19  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照 

2016/11/14  1.年組取得に年組ソート追加

2016/12/13  1.通常授業出欠範囲の学期固定条件をカット

2017/02/20  1.DBエラー対応

2017/04/25  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/09/01  1.＜出力種別＞に欠席者を追加

2017/09/08  1.[欠席者]、ＣＶＳ出力で講座コードを追加、日付エラーチェック修正

2017/09/20  1.DI_CD(29-32)追加

2017/10/03  1.ATTEND_DI_CD_DAT移行
            -- attend_di_cd_dat.sql(1.2)

2018/11/02  1.0校時を除く条件をカット

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2019/09/13  1.年度コンボを修正

2019/12/12  1.以下の修正をした。(土佐女子限定)
            --●出力種別が「科目別」の場合
            --「欠課」項目を欠席と遅刻の間に追加
            --「早退回数と欠課日数の合計」項目を早退の後ろに追加

2019/12/13  1.以下の修正をした。(土佐女子限定)
            --前回の修正を出力種別が「学期別」の場合にも適用
            2.以下の修正をした。(土佐女子限定)
            --複数の項目の合算値(例：欠課数はNOTICE+NONOTICE+SICK+NURSEOFF等の合算)の計算で合算元がNULLの時は0に置き換えて計算するように修正
            --学期別の"欠課"の値の参照先を変更
