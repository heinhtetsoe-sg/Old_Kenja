# kanji=漢字
# $Id: readme.txt,v 1.13 2015/12/15 05:28:03 m-yama Exp $

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
