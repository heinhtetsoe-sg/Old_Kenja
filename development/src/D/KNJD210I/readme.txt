// kanji=漢字
// $Id: readme.txt 74698 2020-06-04 13:04:31Z maeshiro $

KNJD210I
読替先科目生成処理
-----
学校：明治学園
-----
更新テーブル
・RECORD_SCORE_DAT
主なテーブル
・SUBCLASS_WEIGHTING_COURSE_DAT

2012/02/21  1.KNJD210Cを元に新規作成

2012/03/09  1.文言修正。「XXXX年度年度」を「XXXX年度」に修正。

2012/06/25  1.教育課程の追加、追加に伴う修正
                - Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応

2014/08/22  1.style指定修正

2015/06/03  1.プロパティ「weightingHyouki」= '1'のとき、重みはWEIGHTING2を参照する。それ以外はWEIGHTINGを参照する。
            -- 海城から要望

2015/12/18  1.海城は学年末をコンボに追加する

2016/06/29  1.海城は元科目の得点が1個でもnullの場合、得点がnullの先科目のレコードを作成する

2020/06/02  1.プロパティー「useTestCountflg = TESTITEM_MST_COUNTFLG_NEW_SDIV」の時、テスト種別コンボ切替
            2.リストtoリストに表示する科目に、制限付きの条件を追加

2020/06/04  1.校種制限追加
