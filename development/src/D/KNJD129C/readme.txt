// kanji=漢字
// $Id: readme.txt 76785 2020-09-11 05:34:59Z arakaki $

KNJD129C
成績入力（３学期制：２回、２回、１回）

-----
学校：海城学園
-----

2015/02/03  1.KNJD129A(1.19)を元に新規作成

2015/02/10  1.科目コンボ、講座コンボの参照・更新可（制限付き）の条件を修正
                - 学年主任はSTAFF_DETAIL_MSTテーブルのSTAFF_SEQ='005'または'006'でかつFIELD1='0200'の時のFIELD2の値で判断する
                - 教科主任はSTAFF_DETAIL_MSTテーブルのSTAFF_SEQ='005'または'006'でかつFIELD1='1050'の時のFIELD2の値で判断する

2015/03/02  1.参照する科目合併設定テーブルを変更
            -- 修正前：SUBCLASS_REPLACE_COMBINED_DAT
            -- 修正後：SUBCLASS_WEIGHTING_COURSE_DAT
            ※ 海城からの要望
            2.異動者の成績は異動日以降を入力不可とする。
            -- テスト期間を参照

2015/05/21  1.得点入力のチェック　満点マスタがない時は、初期値は100点とする。

2015/05/22  1.欠試で’*’の箇所は、背景色を赤にする。

2015/06/08  1.科目コンボ、講座コンボの参照・更新可（制限付き）の条件を追加修正
                - 学年主任はSTAFF_DETAIL_MSTテーブルのSTAFF_SEQ='005'または'006'または'007'でかつFIELD1='0200'の時にもFIELD2の値で判断する
                - 教科主任はSTAFF_DETAIL_MSTテーブルのSTAFF_SEQ='005'または'006'または'007'でかつFIELD1='1050'の時にもFIELD2の値で判断する

2015/07/04  1.評価の成績入力完了チェックボックス追加

2015/07/07  1.”1学期評価”更新時のＤＢエラーを修正
            -- 異動者ありの時に発生していた

2015/07/10  1.「Enterキーで移動」の不具合を修正
            -- 異動者ありの時に発生していた

2015/07/13  1.ソース整理。不要な処理をカット
            2.未使用のため、評定マスタの参照をカット

2016/04/01  1.成績が別講座の生徒に上書される不具合修正
            -- 生徒を表示する時に、学籍番号を保持しておくのではなく、
            -- 更新時に、学籍番号を取得し更新するように修正。
            -- ※その他も同様
            -- ・単位情報：単位自動計算で使用
            -- ・表示のみ成績：入力窓がない欄

2016/07/01  1.休学・留学者の成績は入力不可とする。
            -- テスト期間を参照

2017/03/07  1.ADMIN_CONTROL_DATキー追加
            -- rep-admin_control_dat.sql(rev1.1)

2017/07/04  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/09/20  1.DI_CD(29-32)追加

2017/10/03  1.ATTEND_DI_CD_DAT移行
            -- attend_di_cd_dat.sql(1.2)

2018/06/19  1.use_staff_detail_ext_mst=1の時、STAFF_DETAIL_EXT_MST参照
            -- use_staff_detail_ext_mst

2020/09/11  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/01  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更
