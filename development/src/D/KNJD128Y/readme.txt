// kanji=漢字
// $Id: readme.txt 76269 2020-08-27 08:57:15Z arakaki $

-----
学校：山形県案件

説明

新規
KNJD128Vをコピーして新規作成する。
出欠情報を成績と同時に入力したい
-----

2019/08/22  1.KNJD128Vを元に新規作成

2019/09/11  1.APPOINTED_DAY_MSTに校種を追加に伴う修正

2019/09/30  1.プロパティー「useSchool_KindField = 1」の時、APPOINTED_DAY_MSTに校種を追加に伴う修正 

2020/08/27  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/01  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更
