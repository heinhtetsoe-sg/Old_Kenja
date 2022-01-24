# kanji=漢字
# $Id: readme.txt 56586 2017-10-22 12:52:35Z maeshiro $

2015/02/05  1.新規作成(処理：KNJD653B　レイアウト：KNJD653B参考)

2015/02/10  1.年組コンボの参照・更新可（制限付き）の条件を修正
                - 学年主任はSTAFF_DETAIL_MSTテーブルのSTAFF_SEQ='005'または'006'でかつFIELD1='0200'の時のFIELD2の値で判断する
            2.usePerfectCourseGroupにより、コースグループの表示/非表示を制御する。

2015/06/05  1.パラメータuseCurriculumcd追加

2015/06/10  1.学年コンボの参照・更新可（制限付き）の条件を追加修正
                - 学年主任はSTAFF_DETAIL_MSTテーブルのSTAFF_SEQ='005'または'006'または'007'でかつFIELD1='0200'の時のFIELD2の値で判断する

2017/01/13  1.学年表示をSCHREG_REGD_GDATから取得に変更
