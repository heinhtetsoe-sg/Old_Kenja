# kanji=漢字
# $Id: readme.txt 76441 2020-09-04 08:21:08Z arakaki $

2009/03/31  1.tokio:/usr/local/development/src/Z/KNJZ300からコピーした。
            2.追加、更新した際にスクロールバーを対象となった行へ移動させる処理を追加
            3.再度開いたときに前回の行へ移動してしまうのを修正

2014/03/24  1.リンク内の年度を使用する用修正

2014/07/25  1.ログ取得機能追加

2015/07/08  1.職員コード00999999は更新/削除/取消不可

2016/05/24  1.フィールドサイズ変更に伴う修正(rep-user_mst.sql(rev1.2))
            -- STAFFCD 8 → 10
            -- USERID 15 → 32
            -- REGISTERCD 8 → 10

2016/07/13  1.USER_MST、USERGROUP_DATのキー追加に伴う修正
            -- プロパティー「useSchool_KindField」とSCHOOLKINDで切替

2016/07/26  1.00999999または0000999999は、左側リストに表示しない。
            -- アルプの職員コード
            2.前回修正カット
            3.パスワードの桁数、文字チェック追加
            -- 8桁以上、英数字&大文字小文字の混在

2016/07/27  1.パスワードの履歴管理とチェック機能を追加
            -- メッセージ「過去に使用したパスワードは入力できません。」
            ※ パスワード変更した時にチェックします。
            ※ パスワード変更した時に履歴追加します。

2017/05/12  1.ＣＳＶ入出力機能を追加
            2.無効フラグ/パスワード期限のCSV処理不具合を修正

2018/03/06  1.入試管理者の職員は職員コードのリンクなしに変更
            -- 入試管理者:STAFF_DETAIL_MST.(STAFF_SEQ="009", FIELD1="1")
            
2018/03/09  1.入試管理者のフラグが立っていた場合はユーザIDを空にする

2018/07/20  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind
            --校種コンボ追加
            2.校種コンボ削除

2018/07/23  1.「設定」のリンクからSEND_selectSchoolKindのキーを削除

2018/07/30  1.USER_MSTのデータがない場合に職員コードのリンクを選択した際に右に表示されるように修正

2018/10/17  1.リファクタリング
            2.次回強制PWD変更機能追加
            -- rep-user_mst.sql(rev.62877)

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2020/09/04  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/09  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2021/01/15  1.CSVメッセージ統一(SG社)

2021/04/29  1.CSVメッセージ統一STEP2(SG社)