# kanji=漢字
# $Id: readme.txt 56586 2017-10-22 12:52:35Z maeshiro $

2010/06/01  1.KNJD617を元に作成

2010/06/02  1.欠時数の算出方法の修正
            2.学年コンボの11未満の制限をカット

2010/06/03  1.欠時数(後期)の算出方法の修正

2011/04/11  1.XLS出力処理追加
            -- プロパティー追加：useXLS(何かセットされていれば、XLS出力。基本'1'をセット。)
            -- 単独で呼び出された場合閉じる
            -- 親から呼ばれた場合は、親の権限を使用する
            2.パラメータ追加

2012/06/01  1.教育課程の追加、追加に伴う修正
             - Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応
             
2013/08/15  1.DI_CD'19','20'ウイルス/DI_CD'25','26'交止追加に伴う修正
            -- rep-attend_semes_dat.sql(rev1.9)
            -- rep-attend_subclass_dat.sql(rev1.11)
            -- v_attend_semes_dat.sql(rev1.7)
            -- v_attend_subclass_dat.sql(rev1.4)
            -- v_school_mst.sql(rev1.20)

2014/08/25  1.style指定修正
