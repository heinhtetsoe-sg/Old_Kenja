# kanji=漢字
# $Id: readme.txt 59989 2018-05-01 09:58:27Z maeshiro $

2009/10/22  1.tokio:/usr/local/development/src/Z/KNJZ050_2からコピーした。
            2.テーブル変更に伴う修正(学科名称 10文字⇒20文字)

2009/10/26  1.学科名称のサイズ変更に伴い、size、maxlengthを修正した。

2010/04/05  1.リストのソート順に学科コードを追加した。

2014/03/31  1.表示用名称(MAJORNAME2)を追加
            -- rep-major_mst.sql(rev1.2)
            -- v_course_major_mst.sql(rev1.4)
            -- v_major_mst.sql(rev1.3)

2014/11/14  1.ログ取得機能追加

2018/05/01  1.MAJOR_MST.MAJORENGの桁数が20を超える場合、学科名称英字のチェック桁数を45に変更する
