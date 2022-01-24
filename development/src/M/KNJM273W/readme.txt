# kanji=漢字
# $Id: readme.txt 74338 2020-05-15 08:13:22Z ooshiro $

2016/09/01  1.新規作成

2017/01/26  1.フォーカスのセット、レポートNo.から学籍番号分離

2018/05/07  1.読替処理追加
            -- rep_subclass_combinde_dat.sql(rev.60011)

2018/07/11  1.読替処理修正

2018/11/19  1.リストの上部にもトータル件数を表示する

2020/03/16  1.以下のプロパティを追加。
            -- 「REPORT_RECEIPT_DATE_SHIME」 '1'以外の場合、受付日を処理日付まで指定可能
            -- 「REPORT_RECEIPT_DATE_SHIME」 '1'の場合、受付日を年度内の範囲の日付まで指定可能

2020/05/13  1.プロパティ useRepStandarddateCourseDat = 1の場合
            -- REP_STANDARDDATE_COURSE_DATのテーブルを見るように修正

2020/05/15  1.読替元科目取得時、課程学科での絞込みを追加

2021/03/10  1.京都PHPバージョンアップ対応