# kanji=漢字
# $Id: readme.txt 76566 2020-09-08 08:11:54Z arakaki $

2014/07/29  1.新規作成

2014/10/03  1.プロパティ「usrSpecial_Support_School = 1」のとき、学級コンボ内容ラジオボタンの表示を追加

2014/10/07  1.プロパティ名を修正
            -- usrSpecial_Support_School
            -- ↓
            -- useSpecial_Support_School

2014/10/20  1.プロパティ名を変更
            -- useSpecial_Support_School
            -- ↓
            -- useSpecial_Support_Hrclass

2014/04/15  1.名称マスタ「C002」NAMECD2="101"の処理を追加
            2.取込でデータがNULLのとき、それ以降の項目もNULLで更新される不具合を修正
            3.取込でデータがNULLのとき、"0"をセットに変更
            4.取込（削除）にATTEND_SEMES_DETAIL_DATの処理を追加

2015/04/17  1.忌引きの位置変更

2015/04/20  1.rev1.5に戻す。

2015/04/24  1.名称マスタ「C002」NAMECD2="101"に範囲チェックを追加
            2.年度、対象月、学期、学籍番号以外は削除時の数値チェックをカット

2015/06/03  1.ATTEND_SEMES_DATのフィールドで表示項目以外は"0"をセット

2015/08/13  1.データ取込修正
            -- プロパティ「use_Attend_zero_hyoji」= '1'のとき、データ通りに取込
            2.データ出力修正
            -- プロパティ「use_Attend_zero_hyoji」= '1'のとき、
            -- C002項目の参照テーブルをV_ATTEND_SEMES_DAT→ATTEND_SEMES_DETAIL_DATに変更

2015/10/22  1.更新可（制限付）の処理を追加

2015/10/27  1.更新可（制限付）の処理の修正漏れ

2015/12/09  1.学級コンボ内容ラジオボタンの固定表示「複式クラス」→「実クラス」に変更

2016/09/19  1.学級コンボ、データ取込時の在籍チェック変更
            -- プロパティー「useSchool_KindField」とSCHOOLKINDの参照

2016/09/20  1.年度学期コンボ変更
            -- プロパティー「useSchool_KindField」とSCHOOLKINDの参照

2016/10/07  1.名称マスタ「C001」の2,25,19のヘッダを固定表示からNAME1に変更

2017/03/06  1.Z005の参照をログイン校種により切替える。

2017/03/07  1.ADMIN_CONTROL_DATキー追加
            -- rep-admin_control_dat.sql(rev1.1)

2017/09/22  1.親画面よりデータを受け取った時は、その値をセットするよう修正

2018/05/29  1.プロパティーuse_prg_schoolkind追加

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2019/07/11  1.対象月コンボの校種参照変更
            2.データ出力のとき、必須選択チェック追加

2019/07/17  1.CSV処理画面終了時、親画面に結果が反映されるように修正

2019/09/11  1.APPOINTED_DAY_MSTに校種を追加に伴う修正

2019/09/30  1.プロパティー「useSchool_KindField = 1」の時、APPOINTED_DAY_MSTに校種を追加に伴う修正 

2020/09/08  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/08  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2021/02/08  1.CSVメッセージ統一(SG社)

2021/04/27  1.CSVメッセージ統一STEP2(SG社)