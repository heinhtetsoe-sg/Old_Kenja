// kanji=漢字
// $Id:  $

2021/01/25  1.KNJA143Uをもとに新規作成

2021/01/26  1.FACULTYCD ではなく CAMPUS_FACULTYCD で絞り込むように修正
            2.DEPARTMENTCD ではなく CAMPUS_DEPARTMENTCD で絞り込むように修正
            3.不要な処理（MAJOR名の取得やLISTAGGによるカンマ区切りでの結合）を削除

2021/04/09  1.母集団のテーブル変更
              修正前：AFT_RECOMMENDATION_INFO_DAT
              修正後：AFT_GRAD_COURSE_DAT

              出力条件に下記を追加
              進路状況が”1:決定”した人
