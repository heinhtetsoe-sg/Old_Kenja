# kanji=漢字
# $Id: readme.txt 76724 2020-09-10 08:11:13Z arakaki $

2014/03/12  1.KNJL140Aを元に新規作成
            2.ヘッダー文言修正
            
2014/03/13  1."すべて"を合格コースに追加し、出力できるよう修正

2014/12/15  1.style指定修正

2018/12/10  1.キー["APPLICANTDIV"]追加に伴う修正

2019/02/25  1.CSV出力に電話番号を追加

2019/09/17  1.入試制度をログイン校種で制御する。
            -- H:APPLICANTDIV = 1
            -- J:APPLICANTDIV = 2

2020/03/10  1.広島国際の中学の時、CSV項目「受験番号」に正しく受験番号が表示されるよう修正
            -- 受験番号ではなく管理番号が表示されていた

2020/09/10  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/04  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更
