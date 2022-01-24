# kanji=漢字
# $Id: readme.txt 61621 2018-08-02 01:17:05Z kawata $

2017/03/08  1.新規作成

2017/04/01  1.useSchool_KindField=1の時のみ、MENU_MST.SCHOOLCD/SCHOOL_KIND参照

2017/04/13  1.テーブルにキー追加に伴い修正
            -- admin_control_attend_dat.sql(rev1.2)
            -- admin_control_attend_itemname_dat.sql(rev1.2)
            -- プロパティー「use_school_detail_gcm_dat」が"1"のとき、課程学科コンボ表示
            2.対象プログラムに"KNJC031F"追加
            -- 累積表示項目設定追加

2017/05/16  1.課程学科コンボにSCHREG_REGD_DAT参照追加

2017/05/17  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/06/20  1.前年度からのコピーのSQLの不具合修正
            2.変数名間違いを直した

2017/08/22  1.出欠項目追加
            -- 名称マスタ「C002/102」

2017/10/23  1.設定可能プログラムに"KNJC032F"追加
            2.対象プログラムコンボの出力に名称マスタ「C043」参照追加

2018/04/09  1.累積表示項目で病欠、事故欠、無届欠のうち複数存在した場合に欠席日数を表示するように修正

2018/05/31  1.グループコードの対象を修正
            -- プロパティーuse_prg_schoolkind = '1'の場合、USERGROUP_DATの権限は校種ごとに作成しない

2018/08/02  1.対象グループの初期値を9999に設定

2021/02/18  1.対象プログラムKNJC035Fに累積表示項目追加
