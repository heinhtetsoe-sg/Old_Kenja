# kanji=漢字
# $Id: readme.txt 76447 2020-09-04 08:24:53Z arakaki $

2011/03/31  1.KNJX160を元に新規作成

2011/04/01  1.コースをカット。

2011/04/09  1.履修1～履修6を、履修制限1～履修制限6と項目名を変更。
            -- 宮城修正

2011/12/27  1.教育課程の追加、追加に伴う修正
             - Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応
             
2013/01/12  1.CLASS_MSTテーブルのCURRICULUM_CDをカット

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2019/02/26  1.CSV出力項目に単位数、備考を追加

2020/09/04  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/09  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2021/03/12  1.CSVメッセージ統一(SG社)

2021/04/29  1.CSVメッセージ統一STEP2(SG社)