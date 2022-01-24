# kanji=漢字
# $Id: readme.txt 76568 2020-09-08 08:12:49Z arakaki $

2011/03/30  1.新規作成

2011/04/01  1.XLS出力処理追加
            -- プロパティー追加：useXLS(何かセットされていれば、XLS出力。基本'1'をセット。)
            2.以下の修正をした
            -- 単独で呼び出された場合閉じる
            -- 親から呼ばれた場合は、親の権限を使用する

2013/08/13  1.DI_CD'19','20'ウイルス追加に伴う修正
            -- rep-attend_semes_dat.sql(rev1.8)
            -- rep-attend_subclass_dat.sql(rev1.10)
            -- v_attend_semes_dat.sql(rev1.6)
            -- v_attend_subclass_dat.sql(rev1.3)
            2.文言変更

2013/08/14  1.DI_CD'25','26'交止追加に伴う修正
            -- rep-attend_semes_dat.sql(rev1.9)
            -- rep-attend_subclass_dat.sql(rev1.11)
            -- v_attend_semes_dat.sql(rev1.7)
            -- v_attend_subclass_dat.sql(rev1.4)
            -- v_school_mst.sql(rev1.20)

2013/08/16  1.名称マスタから取得するタイトルの修正

2013/08/23  1.C001の状態により可変に変更

2014/05/28  1.更新/削除等のログ取得機能を追加

2015/04/17  1.忌引きの位置変更

2015/04/20  1.rev1.9に戻す。

2015/08/13  1.データ取込修正
            -- データがNULLのとき、"0"をセット
            -- ただし、プロパティ「use_Attend_zero_hyoji」= '1'のときはデータ通りに取込

2017/03/06  1.Z005の参照をログイン校種により切替える。

2017/03/07  1.ADMIN_CONTROL_DATキー追加
            -- rep-admin_control_dat.sql(rev1.1)

2017/09/22  1.親画面よりデータを受け取った時は、その値をセットするよう修正

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2019/09/11  1.APPOINTED_DAY_MSTに校種を追加に伴う修正

2019/09/30  1.プロパティー「useSchool_KindField = 1」の時、APPOINTED_DAY_MSTに校種を追加に伴う修正 

2019/10/03  1.親画面の校種がコンボの初期値となるよう修正
            2.対象月コンボを「use_prg_schoolkind」に対応するよう修正

2020/09/08  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/08  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2021/02/08  1.CSVメッセージ統一(SG社)