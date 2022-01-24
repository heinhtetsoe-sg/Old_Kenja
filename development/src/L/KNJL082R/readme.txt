# kanji=漢字
# $Id: readme.txt 76623 2020-09-09 07:22:40Z arakaki $

2019/01/15  1.KNJL016Rを元に新規作成

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2019/01/22  1.手続日付追加、
            --RECEPT_DAT「PROCEDUREDATE1」を更新
            --RECEPT_DAT「PROCEDUREDIV1」は'1'を立てる
            --BASE_DAT「PROCEDUREDATE」は、RECEPT_DAT「PROCEDUREDATE1」のmaxをセットする。

2019/01/22  1.タイトルを「志願者情報取込」から「手続者情報取込」に修正

2019/02/04  1.受験番号の０埋めをカット
            2.CSVデータ取込時に手続終了者以外を登録対象とするよう、修正

2019/09/17  1.入試制度をログイン校種で制御する。
            -- H:APPLICANTDIV = 1
            -- J:APPLICANTDIV = 2

2020/09/09  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/03  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2021/01/14  1.ユーザ番号(EXAMNO)を5桁0埋めで取り込み
