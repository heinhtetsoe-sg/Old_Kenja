# kanji=漢字
# $Id: readme.txt 76503 2020-09-07 08:27:32Z arakaki $

2016/05/24  1.新規作成

2016/06/02  1.CSV出力の生徒取得でSCHREG_BASE_MSTをINNER JOINに変更
            2.CSV取込の速度改善
            3.エラー出力のプログラムIDをカット

2016/09/24  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照

2017/06/27  1.DBエラー修正

2017/07/05  1.DBエラー修正

2017/09/14  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            -- プロパティー追加：use_prg_schoolkind

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2020/09/07  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/07  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2021/03/19  1.CSVメッセージ統一(SG社)

2021/04/23  1.CSVメッセージ統一STEP2(SG社)

2021/04/28  1.implode関数に入る変数をarrayでキャストしエラー回避