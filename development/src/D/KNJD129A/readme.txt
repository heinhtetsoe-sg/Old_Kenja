// kanji=漢字
// $Id: readme.txt 76784 2020-09-11 05:34:36Z arakaki $

KNJD129A
成績入力（３学期制：２回、２回、２回）

-----
学校：明治学園
-----

2012/02/02  1.KNJD128Bを元に新規作成。

2012/02/14  1.スペースチェックを追加
            -- スペース文字があればエラーメッセージを表示する。
            -- 例：入力された値は不正です。「スペース」が混ざっています。

2012/03/09  1.修正漏れ
            -- 考査満点マスタ（PERFECT_RECORD_DAT）を参照
            -- する条件「コースグループ（DIV=04）」を追加

2012/03/13  1.スペースチェックをカット。
            -- スペース文字があればスペース文字を削除する。

2012/04/26  1.科目のコンボの科目名に合併科目の場合は●を追加

2012/05/18  1.合併先科目の参照テーブル修正
              - SUBCLASS_REPLACE_COMBINED_DAT → SUBCLASS_WEIGHTING_COURSE_DAT
              
2012/05/30  1.テーブル名変更
              - COURSE_GROUP_DAT → COURSE_GROUP_CD_DAT
              
2012/06/22  1.教育課程の追加、追加に伴う修正
                - Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応

2013/03/07  1.満点チェックの初期値を100点から999点に変更
            -- 学期・学年成績は、100点を超える場合があるため

2013/03/08  1.黄色表示（異動生徒）の修正
            -- 卒業区分の日付も常にチェックする。

2013/08/20  1.DI_CD'19','20'ウイルス/DI_CD'25','26'交止追加に伴う修正
            -- rep-attend_semes_dat.sql(rev1.9)
            -- rep-attend_subclass_dat.sql(rev1.11)
            -- v_attend_semes_dat.sql(rev1.7)
            -- v_attend_subclass_dat.sql(rev1.4)
            -- v_school_mst.sql(rev1.20)
            2.プロパティーuseVirus、useKekkaJisu、useKekka、useLatedetail、useKoudome追加

2013/08/23  1.DI_CD'19','20'ウイルス/DI_CD'25','26'交止追加に伴う修正漏れ対応

2013/12/16  1.Enterキーによるカーソル移動機能を追加
            2.仮評定の項目追加

2013/12/25  1.仮評定の追加対応するプロパティ追加
              - useProvFlg = '1'の時のみ対応する

2014/02/26  1.更新時、サブミットする項目使用不可

2014/02/28  1.更新時、サブミットする項目使用不可の記述を変更

2014/03/06  1.更新時のロック機能(レイヤ)を追加

2014/04/21  1.更新時のロック機能(レイヤ)はプロパティ「useFrameLock」= '1'の時、有効
            2.貼り付け機能の修正
                - 関数名を修正 show ⇒ showPaste
            3.Enterキーによるカーソル移動機能修正

2016/04/01  1.成績が別講座の生徒に上書される不具合修正
            -- 生徒を表示する時に、学籍番号を保持しておくのではなく、
            -- 更新時に、学籍番号を取得し更新するように修正。
            -- ※その他も同様
            -- ・単位情報：単位自動計算で使用
            -- ・表示のみ成績：入力窓がない欄
            2.修正

2017/03/07  1.ADMIN_CONTROL_DATキー追加
            -- rep-admin_control_dat.sql(rev1.1)

2017/07/04  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/09/20  1.DI_CD(29-32)追加

2017/10/03  1.ATTEND_DI_CD_DAT移行
            -- attend_di_cd_dat.sql(1.2)

2020/09/11  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/01  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更
