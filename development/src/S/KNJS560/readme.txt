// kanji=漢字
// $Id: readme.txt 76753 2020-09-10 10:07:12Z arakaki $

2011/09/26  1.新規作成

2011/09/30  1.CVSのフォーマットよりSEQを削除し、教科名を追加
            2.CVS変換処理時にSEQの項番自動割振り処理を削除
            3.UNIT_L_NAME、UNIT_M_NAME,UNIT_S_NAME、UNIT_ARRIVAL_TARGETの文字制限を変更
            4.CVS更新処理時にSEQの項番自動割振り処理を追加
            5.DIV（種類）エラーチェック追加

2011/10/05  1.CSV出力の記載内容修正
             - 必須項目に※追加
             - フィールドの「項目」の制限を全角30文字に変更
             
2012/04/20  1.教育課程の追加、追加に伴う修正
              - Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2020/09/10  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/04  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2021/02/18  1.CSVメッセージ統一(SG社)