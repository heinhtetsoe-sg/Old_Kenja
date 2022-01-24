# kanji=漢字
# $Id: 30729522898108142fa89ebc09989935c2240409 $

2020/08/13  1.新規作成
            2.AFT_SCHREG_HOPE_DEPARTMENTの更新処理を修正(DELETE INSERTに変更)

2020/09/07  1.CSV DUMMYの名称をLASTCOLUMNに修正
            2.以下の修正をした
              --CSV取込時、「校友会活動CD」が未入力の場合はデフォルト値1をセット
              --ヘッダ名称の修正

2020/12/08  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2021/02/04  1.CSVメッセージ統一(SG社)

2021/04/28  1.CSVメッセージ統一STEP2(SG社)