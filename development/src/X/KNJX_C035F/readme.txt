# kanji=漢字
# $Id: readme.txt 76571 2020-09-08 08:14:48Z arakaki $

2017/03/09  1.KNJX_C035E(rev1.7)を元に新規作成

2017/04/18  1.テーブルにキー追加に伴い修正
            -- admin_control_attend_dat.sql(rev1.2)
            -- admin_control_attend_itemname_dat.sql(rev1.2)
            -- プロパティー「use_school_detail_gcm_dat」が"1"のとき、課程学科コンボ表示
            2.異動生徒の取込チェック追加

2017/04/27  1.対象月コンボの不具合修正

2017/05/16  1.権限グループコード取得を変更
            -- ADMIN_CONTROL_ATTEND_DATが設定されているグループで
            -- 管理グループ(9999)、なければMIN権限グループを取得

2017/05/29  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/09/22  1.親画面よりデータを受け取った時は、その値をセットするよう修正

2017/05/31  1.グループコードの対象を修正
            -- プロパティーuse_prg_schoolkind = '1'の場合、USERGROUP_DATの権限は校種ごとに作成しない

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2019/07/16  1.CSV処理画面終了時、親画面に結果が反映されるように修正

2019/09/12  1.CSV出力の誤りを修正
            2.APPOINTED_DAY_MSTに校種を追加に伴う修正

2019/09/30  1.プロパティー「useSchool_KindField = 1」の時、APPOINTED_DAY_MSTに校種を追加に伴う修正 

2020/01/31  PC-Talker機能追加

2020/09/08  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/08  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2021/02/18  1.講座に「-全て-」を追加
            2.CSVの末端に累積表示項目追加

2021/03/15  1.CSVメッセージ統一(SG社)

2021/04/23  1.CSV出力のSQLを修正

2021/04/28  1.CSVメッセージ統一STEP2(SG社)