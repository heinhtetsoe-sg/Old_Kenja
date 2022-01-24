# kanji=漢字
# $Id: readme.txt 69938 2019-09-30 08:49:35Z ishii $

2014/10/01  1.KNJC032Kを元に新規作成

2014/10/02  1.プロパティ「usrSpecial_Support_School = 1」のとき、学級コンボ内容ラジオボタンの表示を追加

2014/10/07  1.プロパティ名を修正
            -- usrSpecial_Support_School
            -- ↓
            -- useSpecial_Support_School

2014/10/20  1.プロパティ名を変更
            -- useSpecial_Support_School
            -- ↓
            -- useSpecial_Support_Hrclass

2015/04/17  1.保存→更新

2015/06/05  1.ATTEND_SEMES_DATのフィールドで更新対象項目以外は"0"をセット

2015/08/12  1.プロパティ「use_Attend_zero_hyoji」= '1'のときの処理を追加
            -- 表示：データの通りにゼロ、NULLを表示
            -- 更新：表示の通りにゼロ、NULLで更新
            2.月の背景色（データ未入力）の判定を変更
            -- ATTEND_SEMES_DATのSCHREGNOの有無

2015/12/09  1.学級コンボ内容ラジオボタンの固定表示「複式クラス」→「実クラス」に変更

2017/03/06  1.Z005の参照をログイン校種により切替える。

2017/03/07  1.ADMIN_CONTROL_DATキー追加
            -- rep-admin_control_dat.sql(rev1.1)

2017/06/15  1.更新中は使用不可
            -- 全コンボ、全ボタン、全ラジオボタン

2017/09/28  1.前回の修正漏れ

2019/09/11  1.APPOINTED_DAY_MSTに校種を追加に伴う修正

2019/09/30  1.プロパティー「useSchool_KindField = 1」の時、APPOINTED_DAY_MSTに校種を追加に伴う修正 
