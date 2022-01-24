# kanji=漢字
# $Id: readme.txt 76530 2020-09-07 08:45:56Z arakaki $

2016/06/16  1.新規作成

2016/09/27  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照 

2017/09/15  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2018/10/12  1.部活動複数校種設定対応
            -- プロパティー「useClubMultiSchoolKind」参照

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2020/09/07  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/09  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2021/03/15  1.CSVメッセージ統一(SG社)

2021/04/26  1.CSVメッセージ統一STEP2(SG社)