# kanji=漢字
# $Id: a300264dd88abb773d81b710414d45ecf349c2ff $

2016/09/13  1.新規作成

2016/09/15  1.SUBCLASS_STD_PASS_SDIV_DAT.SEM_PASS_FLG を参照するように変更
            -- １の時、更新可

2017/10/10  1.科目コンボ、テスト種別コンボは受験資格ありのみ表示

2018/08/08  1.
            -- rep-record_score_hist_detail_dat.sql(rev.61699)

2019/08/22  1.左画面の校種コンボ用パラメーター追加(URL_SCHOOLKIND)

2020/08/17  1.動作するよう修正

2020/09/29  1.プロパティknjm431wPassScoreに合格点を指定した場合、合否判定は合格点で判定し不合格は得点を赤字表示する。

2020/09/30  1.プロパティknjm431wPassScoreに合格点を指定した場合、得点なしは合否欄に「欠」を表示する。

2021/01/04  1.コード自動整形
            2.プロパティknjm431wUseGakkiHyouka=1の場合、KNJXEXP_SUBCLASSに学期評価表示のパラメータを指定する

2021/01/07  1.knjm431wModel.incの文字コードをUTF-8に変更
            2.プロパティknjm431wUseGakkiHyouka= 1の場合、学期評価はRECORD_SCORE_HIST_DAT.VALUEを読込・更新する

