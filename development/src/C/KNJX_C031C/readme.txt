# kanji=漢字
# $Id: readme.txt 69608 2019-09-11 00:05:06Z ishii $

2011/05/31  1.新規作成

2011/06/03  1.名称マスタのC001/C002は年度データを参照する。

2011/06/06  1.C002はコード順とする。

2011/06/13  1.C002コード追加に伴う修正

2011/06/15  1.出力項目の順序を修正

2013/08/22  1.C002コード追加に伴う修正

2013/08/22  1.DI_CD'19','20'ウイルス/DI_CD'25','26'交止追加に伴う修正
            -- rep-attend_semes_dat.sql(rev1.9)
            -- rep-attend_subclass_dat.sql(rev1.11)
            -- v_attend_semes_dat.sql(rev1.7)
            -- v_attend_subclass_dat.sql(rev1.4)
            -- v_school_mst.sql(rev1.20)

2015/04/17  1.忌引きの位置変更

2015/04/20  1.rev1.7に戻す。

2015/08/25  1.データ取込修正
            -- データがNULLのとき、"0"をセット
            -- ただし、プロパティ「use_Attend_zero_hyoji」= '1'のときはデータ通りに取込
            2.データ出力修正
            -- プロパティ「use_Attend_zero_hyoji」= '1'のとき、
            -- C002項目の参照テーブルをV_ATTEND_SEMES_DAT→ATTEND_SEMES_DETAIL_DATに変更
            3.詳細テーブルは「SEQ：001～004」のみ参照に変更
            4.V_ATTEND_SEMES_DATの参照フィールドをKEKKA_JISU→M_KEKKA_JISUに変更

2017/03/06  1.Z005の参照をログイン校種により切替える。

2017/03/07  1.ADMIN_CONTROL_DATキー追加
            -- rep-admin_control_dat.sql(rev1.1)

2017/09/22  1.親画面よりデータを受け取った時は、その値をセットするよう修正

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2019/07/17  1.CSV処理画面終了時、親画面に結果が反映されるように修正

2019/09/11  1.APPOINTED_DAY_MSTに校種を追加に伴う修正

2021/02/05  1.CSVメッセージ統一(SG社)