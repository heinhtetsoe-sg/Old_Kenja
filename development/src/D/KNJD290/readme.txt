# kanji=漢字
# $Id: readme.txt 56586 2017-10-22 12:52:35Z maeshiro $

2009/04/17  1.新規作成(処理：新規　レイアウト：ウィザス KNJWD515参考)

2009/05/19  1.AttendAccumulate変更に伴う修正。

2010/06/22  1.プロパティーファイルの参照方法を共通関数を使うよう修正

2011/04/07  1.処理内容の大幅変更
            -- SUBCLASS_REQUIRED_STUDY_DATの追加
            -- STANDARD_CREDIT_MSTの追加
            -- CLASS_REQUIRED_DATの追加

2011/04/09  1.対象データが無い場合にエラーが表示される不具合を修正。

2011/04/11  1.学年データはGDATより取得する。

2012/06/21  1.教育課程の追加、追加に伴う修正
              - Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応
              
2012/09/10  1.教育課程の修正漏れ対応

2013/01/11  1.CLASS_MSTテーブルのCURRICULUM_CDをカット

2013/07/05  1.名称マスタのマスタ化に伴う修正

2014/08/22  1.style指定修正

2017/01/16  1.不具合修正

2017/07/25  1.学校マスタ情報取得するとき、SCHOOLCD、SCHOOLKINDを渡すよう修正
            -- AttendAccumulate.php(1.7)に伴う修正
