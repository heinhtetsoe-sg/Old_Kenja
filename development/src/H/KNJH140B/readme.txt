# kanji=漢字
# $Id: readme.txt 76390 2020-09-03 08:18:20Z arakaki $

2016/11/21  1.KNJH140A(rev. 1.3)をコピーして作成
            2.テンプレート修正

2017/01/06  1.校種対応
            2.家族情報に親族の在卒区分、学年追加

2017/05/10  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2020/09/03  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/02  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2021/02/17  1.CSVメッセージ統一(SG社)