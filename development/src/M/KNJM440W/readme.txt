# kanji=漢字
# $Id: 4fd159b48a096652023c06b4181f2d77cfa62e43 $

2016/09/14  1.KNJM440Mをコピーして作成

2016/09/14  1.フィールド名が間違っていたので修正。PASS_FLG → SEM_PASS_FLG

2018/03/28  1.useTsushinSemesKonboHyoji=1ならばテスト種別を出さない形式にするよう修正

2018/03/28  1.上記useTsushinSemesKonboHyojiの修正ミスでSQLの選択が不正だった件を修正

2018/03/28  1.存在チェックが効いていなかったためTESTCDの設定位置変更

2020/05/13  1.プロパティ「useRepStandarddateCourseDat」を追加
             -- useRepStandarddateCourseDat = '1'の場合はREP_STANDARDDATE_COURSE_DATテーブルを使用

2020/05/15  1.レポート回数取得時、グループ化するよう修正

2020/09/29  1.useRepStandarddateCourseDat=1でレポートの最大の連番はコースごとに参照する
