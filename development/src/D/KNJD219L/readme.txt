# kanji=漢字
# $Id: readme.txt 76896 2020-09-15 10:27:13Z yogi $

2015/01/14  1.新規作成

2015/01/15  1.評定をRECORD_SCORE_DATへ更新する処理を追加

2015/01/20  1.達成度(JVIEWSTAT_RECORD_DETAIL_DAT.REMARK3)の型をDECIMAL(4,1)に変更
            2.学期コンボ追加(ただし、SEMESTER='2'は表示しない)
            3.IBSUBCLASS_SCORE_DATのSCOREがNULLの時は、算出しない
            4.評定は評定記号が無いためSEQをセットする
            5.各観点コードごとの評価はMYPのみ作成

2015/01/22  1.学期コンボは1学期と学年末のみを表示
            2.指定対象クラスと母集団の在籍データの学期に学年末以外は指定学期、学年末にはログイン学期を使用する

2015/11/30  1.同じ値の観点がある場合に観点合計が正しくない不具合を修正

2016/02/02  1.合併科目が設定されている場合、合併先科目の観点・評定を算出する
            2.修正

2016/02/10  1.同じ値の観点がある場合に観点合計が正しくない不具合を修正

2020/09/15  1.プロパティ[KNJD126J_useCtlHyoutei]=1かつ評価の入力が可能な時、
              RECORD_SCORE_DATに評定を登録する際に未入力済フラグ(JVIEWSTAT_RECORD_PROV_FLG_DAT.PROV_FLG)が1の場合、
              更新しないよう、変更
            --プロパティー追加：KNJD126J_useCtlHyoutei
            --新規テーブル追加：JVIEWSTAT_RECORD_PROV_FLG_DAT
