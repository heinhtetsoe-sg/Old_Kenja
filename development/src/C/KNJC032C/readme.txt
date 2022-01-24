# kanji=漢字
# $Id: readme.txt 69938 2019-09-30 08:49:35Z ishii $

2011/06/01  1.新規作成

2011/06/03  1.名称マスタのC001/C002は年度データを参照する。

2011/06/06  1.下記の修正をした。
            -- C002はNAMECD2順とする。
            -- 集計処理修正

2011/06/13  1.C002コード追加に伴う修正

2011/06/15  1.出力項目の順序を修正
            2.出力項目の順序を修正2

2011/07/14  1.貼り付け機能の不具合を修正した。

2011/12/07  1.異動表示対象に留学、休学を追加した。

2012/01/12  1.異動表示対象に除籍を追加した。

2012/02/17  1.名称マスタ「NAMECD1:C002」の「NAMECD2」に001～004以外があるとエラーになる不具合を修正した。

2013/08/27  1.DI_CD'19','20'ウイルス/DI_CD'25','26'交止追加に伴う修正
            -- rep-attend_semes_dat.sql(rev1.9)
            -- rep-attend_subclass_dat.sql(rev1.11)
            -- v_attend_semes_dat.sql(rev1.7)
            -- v_attend_subclass_dat.sql(rev1.4)
            -- v_school_mst.sql(rev1.20)

2014/03/10  1.更新時のロック機能(レイヤ)を追加

2014/04/21  1.更新時のロック機能(レイヤ)はプロパティ「useFrameLock」= '1'の時、有効
            2.貼り付け機能の修正
                - 関数名を修正 show ⇒ showPaste

2015/04/17  1.保存→更新

2015/08/21  1.プロパティ「use_Attend_zero_hyoji」= '1'のときの処理を追加
            -- 表示：データの通りにゼロ、NULLを表示
            -- 更新：表示の通りにゼロ、NULLで更新
            2.月の背景色（データ未入力）の判定を変更
            -- ATTEND_SEMES_DATのSCHREGNOの有無
            3.V_ATTEND_SEMES_DATの参照フィールドを"KEKKA_JISU" → "M_KEKKA_JISU"に変更

2016/06/14  1.計算処理VALUEで括る

2017/03/06  1.Z005の参照をログイン校種により切替える。

2017/03/07  1.ADMIN_CONTROL_DATキー追加
            -- rep-admin_control_dat.sql(rev1.1)

2019/09/11  1.APPOINTED_DAY_MSTに校種を追加に伴う修正

2019/09/30  1.プロパティー「useSchool_KindField = 1」の時、APPOINTED_DAY_MSTに校種を追加に伴う修正 
