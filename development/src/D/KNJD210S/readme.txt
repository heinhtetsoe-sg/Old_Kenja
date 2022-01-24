# kanji=漢字
# $Id: readme.txt 61231 2018-07-12 13:46:25Z maeshiro $

KNJD210S
序列確定処理

-----
学校：文京
-----
参照テーブル
    TESTITEM_MST_COUNTFLG_NEW_SDIV
    ADMIN_CONTROL_SDIV_DAT
    COURSE_GROUP_CD_DAT
更新テーブル
  実行履歴
    RECORD_RANK_EXEC_SDIV_DAT
  単体
    RECORD_AVERAGE_SDIV_DAT
    RECORD_RANK_SDIV_DAT
  累計(高校)
    RECORD_AVERAGE_RUIKEI_SDIV_DAT
    RECORD_RANK_RUIKEI_SDIV_DAT
データ
    777777：必修科目（つまり、選択科目を除く）
    888888：選択科目

-----
学校：武蔵野東
-----
参照テーブル
    TESTITEM_MST_COUNTFLG_NEW_SDIV
    ADMIN_CONTROL_SDIV_DAT
    SCHREG_REGD_FI_HDAT.RECORD_DIV
    SCHREG_REGD_FI_DAT
更新テーブル
  実行履歴
    RECORD_RANK_EXEC_SDIV_DAT
  法定
    RECORD_AVERAGE_CHAIR_SDIV_DAT
    RECORD_AVERAGE_SDIV_DAT
    RECORD_RANK_CHAIR_SDIV_DAT
    RECORD_RANK_SDIV_DAT
  複式
    RECORD_AVERAGE_CHAIR_FI_SDIV_DAT
    RECORD_AVERAGE_FI_SDIV_DAT
    RECORD_RANK_CHAIR_FI_SDIV_DAT
    RECORD_RANK_FI_SDIV_DAT
データ
    777777：必修科目（つまり、選択科目を除く）
    888888：選択科目
合算科目の母集団から合併元科目を除く
評定（9-9900-09） SUBCLASS_REPLACE_COMBINED_DAT
評定以外          SUBCLASS_WEIGHTING_COURSE_DAT

2015/08/14  1.KNJD210V(1.4)を元に新規作成

2015/10/14  1.選択科目の条件をSUBCLASS_MST.ELECT_DIV='1'からCREDIT_MST.REQUIRE_FLG='3'に変更

2015/12/16  1.合算科目の母集団から合併元科目をのぞいた。

2015/12/25  1.プロパティuseFi_Hrclassが１の時、FI対応の複式クラスの生成処理を追加
            -- 武蔵野東

2016/03/01  1.中学も使用するように修正

2016/03/02  1.合算科目の母集団に得点がnullのレコードがある場合、合算科目のレコードを作成しない
            2.合算科目のレコードを作成する条件の科目から元科目を除く
            -- NULLの学籍番号を取得するクエリーを修正
            -- 前回の修正漏れ

2016/03/10  1.中学は、累計用序列テーブルを生成しない。
            2.中学は、累計用序列テーブルを生成する。前回に戻しただけ。

2016/07/13  1.累計用序列テーブルを生成する時、見込点を含めるように修正

2016/11/25  1.プロパティーuseSchool_KindField使用追加

2018/07/12  1.累計の平均点の算出を変更
            -- 修正前：指定学期内の9900以外の小区分08の総得点/成績のある生徒の数
            -- 修正後：指定学期内の9900以外の小区分08の各平均点の合計
