# kanji=漢字
# $Id: readme.txt 69022 2019-08-01 04:06:24Z maeshiro $

2014/04/03  新規作成

2016/07/21  1.生徒コンボ、生徒リストtoリスト内の印字位置を調整
            2.在籍期間外の生徒チェックの不具合修正
            3.レイアウト修正

2016/09/22  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照 

2016/09/30  1.追加処理後、左画面の部クラブコンボに右画面の部クラブをセット

2016/10/04  1.ＣＳＶ処理ボタン追加
            2.GROUP_CLUB_HDETAIL_DATのフィールド追加に伴う修正
            -- プロパティー「useSchool_KindField」とSCHOOLKINDを参照

2018/10/12  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー「use_prg_schoolkind」参照
            2.部活動複数校種設定対応
            -- プロパティー「useClubMultiSchoolKind」参照

2019/03/04  1.区分で団体を選択時に大会名称をSCHREG_CLUB_HDETAIL_DAT.MEET_NAMEに反映するよう修正

2019/03/20  1.エラーチェックで引っかかったとき、DBエラーになる不具合修正
            2.区分を団体で更新したときのSCHREG_CLUB_HDETAIL_DATの更新対象項目を追加
            -- 開催地域、種目、種目種類、成績、記録、備考

2019/08/01  1.大会名称のあるデータが1件もない場合にヘッダがずれないよう修正
