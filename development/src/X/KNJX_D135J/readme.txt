# kanji=漢字
# $Id: readme.txt 76574 2020-09-08 08:16:41Z arakaki $

2018/05/25  1.KNJX_D132Hをもとに新規作成

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2019/07/22  1.名称マスタ「Z010」kyotoの時、「道徳」を追加

2019/12/24  1.3学期制でtutisyoShokenntunenプロパティが立っていたら、所見欄の取込/出力が通年入力となるよう、変更
              -- tutisyoShokenntunenプロパティ追加
            2.CSV出力で、所見欄がされていなかった不具合を修正

2020/09/08  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/08  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2021/02/15  1.CSVメッセージ統一(SG社)

2021/04/27  1.CSVメッセージ統一STEP2(SG社)