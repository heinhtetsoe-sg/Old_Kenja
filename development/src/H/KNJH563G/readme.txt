# kanji=漢字
# $Id: readme.txt 68370 2019-07-01 02:59:56Z maeshiro $

2015/02/05  1.新規作成(処理：KNJH563　レイアウト：KNJH563参考)
            2.偏差値出力、保護者欄印刷、提出日をカット

2015/02/10  1.年組コンボの参照・更新可（制限付き）の条件を修正
                - 学年主任はSTAFF_DETAIL_MSTテーブルのSTAFF_SEQ='005'または'006'でかつFIELD1='0200'の時のFIELD2の値で判断する

2015/06/08  1.学年コンボの参照・更新可（制限付き）の条件を追加修正
                - 学年主任はSTAFF_DETAIL_MSTテーブルのSTAFF_SEQ='005'または'006'または'007'でかつFIELD1='0200'の時のFIELD2の値で判断する

2016/11/28  1.プロパティーuseSchool_KindField使用追加

2017/05/10  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2019/07/01  1.学年コンボの制限を追加
