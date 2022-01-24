# $Id: readme.txt 76306 2020-08-28 08:10:20Z arakaki $

2019/11/19  1.新規作成

2020/04/20  1.SCORE_DIV='09'の場合も取り込み可能な成績にする
             -- 考査種別コンボにSCORE_DIV='09'の考査種別も表示されるように抽出条件を変更

2020/08/28  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/01  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2021/01/29  1.CSVメッセージ統一(SG社)