# kanji=漢字
# $Id: readme.txt 74315 2020-05-15 02:13:03Z gushiken $

評定コピー／クリア
--------------------
学校：文京（パーツタイプ）
--------------------

2015/08/19  1.KNJD214V(1.11)を元に新規作成

2016/03/01  1.中学も使用するように修正
            -- ASSESS_LEVEL_SDIV_MSTは、コース（DIV = 3）を参照

2016/03/10  1.中学は、以下のテーブル参照に変更
            -- ASSESS_SUBCLASS_MST
            -- RECORD_SCORE_DAT

2016/06/03  1.高校の評定換算テーブルASSESS_LEVEL_SDIV_MSTの参照条件を修正
            -- コピー元になる評価の学期が９の時、RUISEKI_DIV = '3'
            -- それ以外の時、RUISEKI_DIV = '2'　を参照する

2016/10/25  1.高校の評定換算テーブルASSESS_LEVEL_SDIV_MSTの参照条件を修正
            -- RUISEKI_DIV = '3'　を参照する
            -- 2016/06/03の修正をカット

2020/05/15  1.名称マスタ「"D" + "校種" + "08"」が1件でもあれば「"D" + "校種" + "08"」を参照する
             -- 上記以外は「D008」を使用
