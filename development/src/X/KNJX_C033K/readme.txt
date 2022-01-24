# kanji=漢字
# $Id: readme.txt 76569 2020-09-08 08:13:48Z arakaki $

2011/03/31  1.新規作成

2011/04/01  1.XLS出力処理追加
            -- プロパティー追加：useXLS(何かセットされていれば、XLS出力。基本'1'をセット。)
            2.以下の修正をした
            -- 単独で呼び出された場合閉じる
            -- 親から呼ばれた場合は、親の権限を使用する

2012/02/28  1.指定月のATTEND_SEMES_DATにデータが無ければ追加するように修正した。

2012/05/17  1.プロパティー「useCurriculumcd」を追加した。
            -- '1'がセットされている場合は教育課程コード等を使うようにした。

2012/05/25  1.学校種別のヘッダー例を名称マスタから参照するように変更した。
            2.学校種別、教育課程コードの存在チェックを名称マスタの値でするように変更した。

2012/07/11  1.パラメータuseCurriculumcdを追加

2013/03/27  1.年度＆学期コンボで選択した範囲内の月を対象月コンボに表示するよう修正した。
            2.CSV出力で1人の生徒に対し受講生徒名簿が複数あるとき、複数行出力される不具合を修正した。

2013/03/28  1.CSV出力のSQLを修正

2013/08/13  1.DI_CD'19','20'ウイルス追加に伴う修正
            -- rep-attend_semes_dat.sql(rev1.8)
            -- rep-attend_subclass_dat.sql(rev1.10)
            -- v_attend_semes_dat.sql(rev1.6)
            -- v_attend_subclass_dat.sql(rev1.3)

2013/08/14  1.DI_CD'25','26'交止追加に伴う修正
            -- rep-attend_semes_dat.sql(rev1.9)
            -- rep-attend_subclass_dat.sql(rev1.11)
            -- v_attend_semes_dat.sql(rev1.7)
            -- v_attend_subclass_dat.sql(rev1.4)
            -- v_school_mst.sql(rev1.20)

2015/08/13  1.データ取込修正
            -- データがNULLのとき、"0"をセット
            -- ただし、プロパティ「use_Attend_zero_hyoji」= '1'のときはデータ通りに取込

2016/09/29  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照

2017/03/06  1.Z005の参照をログイン校種により切替える。

2017/03/07  1.ADMIN_CONTROL_DATキー追加
            -- rep-admin_control_dat.sql(rev1.1)

2017/09/22  1.親画面よりデータを受け取った時は、その値をセットするよう修正

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2019/07/17  1.CSV処理画面終了時、親画面に結果が反映されるように修正

2020/09/08  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/08  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2021/02/04  1.CSVメッセージ統一(SG社)

2021/04/27  1.CSVメッセージ統一STEP2(SG社)