# kanji=漢字
# $Id: readme.txt 56586 2017-10-22 12:52:35Z maeshiro $

2011/02/04  1.KNJD617(rev1.25)を元に作成（中京用）

2011/02/05  1.不要なif文をカットした。

2012/01/18  1.教育課程の追加、追加に伴う修正
             - Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応
             
2012/01/20  1.教育課程処理修正漏れ部分を追加

2012/06/06  1.プロパティーuseCurriculumcdを追加

2012/07/02  1.プロパティーのスペルミスを修正

2013/08/12  1.プロパティーuseVirus、useKekkaJisu、useKekka、useLatedetail追加

2013/08/14  1.プロパティーuseKoudome追加

2013/08/16  1.DI_CD'19','20'ウイルス/DI_CD'25','26'交止追加に伴う修正
            -- rep-attend_semes_dat.sql(rev1.9)
            -- rep-attend_subclass_dat.sql(rev1.11)
            -- v_attend_semes_dat.sql(rev1.7)
            -- v_attend_subclass_dat.sql(rev1.4)
            -- v_school_mst.sql(rev1.20)

2014/05/22  1.修正
            -- 勤怠コード27が入力された日付は一日出欠の授業日数にカウントしない
            -- 勤怠コード27が入力された日付校時は科目出欠の授業時数にカウントしない

2014/08/11  1.style指定修正

2015/06/02  1.勤怠コード'28'は時間割にカウントしない

2016/03/28  1.学年コンボの参照先をSCHREG_REGD_GDATに変更
            2.欠課数上限値のラベル機能追加
            3.欠点テキストボックス内を右寄せに変更
            4.fontタグをスタイルシートに変更

2016/06/20  1.権限制限付の不具合修正

2017/09/20  1.DI_CD(29-32)追加

2017/10/03  1.ATTEND_DI_CD_DAT移行
            -- attend_di_cd_dat.sql(1.2)
