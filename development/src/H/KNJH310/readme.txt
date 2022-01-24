# kanji=漢字
# $Id: readme.txt 71423 2019-12-24 10:31:42Z maeshiro $

2007/03/09  新規作成(処理：新規　レイアウト：新規)

2007/06/11  Z010のNAMESPARE1を使用して、成績テーブルを切り替える処理を追加した。
            -- NAMESPARE1 = 1 → RECORD_SCORE_DAT
               上記以外は、RECORD_DAT

2007/08/15  名称マスタ「Z010」の条件に NAMECD2='00' を追加した。

2008/03/29  1.近大対応

2008/03/31  1.prgInfo.propertiesの保管場所変更

2008/04/03  1.KIN_RECORD_DATを使用している場合は、偏差値はブランク

2009/05/21  1.学校マスタを参照し、出力対象を変更

2010/06/23  1.プロパティーファイルの参照方法を共通関数を使うよう修正

2010/08/09  1.上記修正の修正漏れ対応

2010/08/10  1.UnUseSchChrTestをプロパティーファイルに追加
            -- 1:SCH_CHR_TESTを使用しない それ以外：SCH_CHR_TESTを使用する

2012/06/22  1.プロパティー「useCurriculumcd」を追加した。
            -- '1'がセットされている場合は教育課程コード等を使うようにした。

2013/08/16  1.DI_CD'19','20'ウイルス/DI_CD'25','26'交止追加に伴う修正
            -- rep-attend_semes_dat.sql(rev1.9)
            -- rep-attend_subclass_dat.sql(rev1.11)
            -- v_attend_semes_dat.sql(rev1.7)
            -- v_attend_subclass_dat.sql(rev1.4)
            -- v_school_mst.sql(rev1.20)

2015/12/15  1.IEのバージョンによるエラーを修正

2016/10/06  1.toHTML5⇒toHTML6に変更 Form1.php
            2.$model->subclassをmakeHeadで返せるように変更 Form1.php
            3.getScore,getRank,getAbsentでDBエラーにならないように$model->subclassが空のときに回避 Query.inc

2017/02/22  表示得点を素点と評価を選択できるように変更。
            $model->recordTableがRECORD_SCORE_DATのときのみ表示。
            Query内で、SCORE_DIVを01か08を分岐していたところを選択したラジオの値で分岐するように。

2017/04/04  1.プロパティuse_school_detail_gcm_dat=1に対応
            （甲府など、学年ではなく学科ごとの順位や平均点、偏差値を表示できるように機能分け）

2017/05/16  1.低解像度に対応 html(金丸)

2018/10/22  1.科目コードの取得処理で、引数の変数に科目コードが正しく設定されない不具合に対応

2019/12/24  1.プロパティuseTestCountflgがTESTITEM_MST_COUNTFLG_NEW_SDIVの際のTESTITEMCD='01'の条件をカット

