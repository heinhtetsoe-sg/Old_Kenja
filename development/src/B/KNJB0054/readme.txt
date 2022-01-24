# kanji=漢字
# $Id: readme.txt 76210 2020-08-26 08:30:01Z arakaki $

2012/04/24  1.新規作成

2012/07/01  1.教育課程の追加、追加に伴う修正
                - Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応
                
2014/05/26  1.更新/削除等のログ取得機能を追加

2015/09/08  1.履修登録の履歴に伴う修正

2015/10/19  1.テーブルSUBCLASS_STD_SELECT_DATをSUBCLASS_STD_SELECT_RIREKI_DATに変更
            2.2014/05/26(rev. 1.3)修正にもどした（履修登録の履歴はKNJB1254）

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2020/08/26  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/11/27  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを設定ファイルの内容で切り替えるように変更
