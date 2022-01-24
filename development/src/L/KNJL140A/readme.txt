# kanji=漢字
# $Id: readme.txt 76718 2020-09-10 08:01:31Z arakaki $

2014/01/28  1.KNJL140を元に新規作成
            2.受験コース名ではなく、COURSECODE_MSTのコース名を表示
            3.広島国際は賢者として取込むため、氏名カナは"かたかな"表示

2014/12/15  1.style指定修正

2018/12/10  1.キー["APPLICANTDIV"]追加に伴う修正

2019/02/12  1.入学者出力の際、学年を追加

2019/06/06  1.合格コースコンボに「すべて」を追加

2019/09/17  1.入試制度をログイン校種で制御する。(広島国際)
            -- H:APPLICANTDIV = 1
            -- J:APPLICANTDIV = 2

2020/03/10  1.広島国際の中学の時、CSV項目「受験番号」に正しく受験番号が表示されるよう修正
            -- 受験番号ではなく管理番号が表示されていた

2020/09/10  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/03  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更
            3.保護者情報の続柄を取得する SQL の RELATIONSHIP を GRELATIONSHIP に修正
