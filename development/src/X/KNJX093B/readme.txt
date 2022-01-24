# kanji=漢字
# $Id: readme.txt 76745 2020-09-10 08:33:52Z arakaki $

2015/11/12  1.KNJX091B(rev1.1)を元に新規作成

2016/06/20  1.履修登録日コンボは履修登録年度を参照に変更

2016/09/21  1.校種条件追加
            -- プロパティー「useSchool_KindField」とSCHOOLKINDを参照

2017/05/25  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2019/08/21  1.Warning対応

2020/09/10  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/07  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2021/03/05  1.CSVメッセージ統一(SG社)

2021/04/22  1.CSVメッセージ統一STEP2(SG社)