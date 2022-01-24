# kanji=漢字
# $Id: readme.txt 76554 2020-09-08 08:06:29Z arakaki $

2015/11/12  1.KNJX093C(rev1.2)を元に新規作成

2016/09/21  1.校種条件追加
            -- プロパティー「useSchool_KindField」とSCHOOLKINDを参照

2017/05/25  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2020/09/08  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/07  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2021/01/29  1.CSVメッセージ統一(SG社)

2021/04/22  1.CSVメッセージ統一STEP2(SG社)