# kanji=漢字
# $Id: readme.txt 56585 2017-10-22 12:47:53Z maeshiro $

2009/02/18  新規作成(KNJWC034K参考)

2009/02/19  1.文言の修正

2009/10/15  1.SICKをカット

2012/01/18  1.教育課程の追加、追加に伴う修正
            -- Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応
            2.修正

2012/02/28  1.更新対象月のATTEND_SEMES_DATにデータが無ければ追加するように修正した。

2014/03/10  1.更新時のロック機能(レイヤ)を追加

2014/04/28  1.更新時のロック機能(レイヤ)はプロパティ「useFrameLock」= '1'の時、有効

2015/04/17  1.保存→更新

2016/01/29  1.学年学期対応

2016/03/30  1.サブミット時に別データでの上書が発生する事案に伴い
            -- model変数をPHPでセットせず、postする処理に変更
