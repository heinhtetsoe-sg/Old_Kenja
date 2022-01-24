# kanji=漢字
# $Id: readme.txt 76501 2020-09-07 08:25:47Z arakaki $

2016/04/07  1.KNJX080(1.13)を元に新規作成。改訂版。
            -- ＣＳＶ項目に講座名称、群名称を追加

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2020/09/07  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/07  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2020/12/24  1.受講クラスの自動生成の前にCHAIE_CLS_DATのゴミデータを削除する処理を追加

2021/01/07  1.CHAIR_DATをCSVのCHAIRCDで検索し、抽出されたGROUPCDが0000以外の時、CSVのGROUPCDが0000であった場合にエラーにする処理を追加

2021/02/25  1.CSVメッセージ統一(SG社)

2021/03/10  1.京都PHPバージョンアップ対応
