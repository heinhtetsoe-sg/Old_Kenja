# kanji=漢字
# $Id: readme.txt 76314 2020-08-28 08:17:13Z arakaki $

2015/12/02  1.KNJE361(rev1.7)を元に新規作成

2016/09/26  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照

2017/05/01  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2019/11/15  1.CSV項目「受験方式」を追加

2020/08/28  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/02  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2021/03/12  1.CSVメッセージ統一(SG社)