# kanji=漢字
# $Id: readme.txt 62297 2018-09-13 04:44:35Z maeshiro $

-----
学校：宮城県・常盤高校
-----

2013/06/24  1.新規。初版。

2013/07/18  1.算出設定画面ボタン追加

2014/01/14  1.出欠コントロール、実力テストコントロールの更新機能追加

2014/01/19  1.文言修正
            -- 成績入力（考査種別選択）
            -- 成績入力コントロール
            -- 出欠入力コントロール
            -- 実力テスト入力コントロール
            2.成績入力のリスト名称を修正
            -- 学期名＋ハイフン＋考査種別名

2014/02/02  1.前年度からコピーボタン追加

2014/02/24  1.出欠入力コントロールと実力テスト入力コントロールの更新、取消、終了ボタンを別々に配置し、処理を分ける
            2.出欠入力コントロールの【≫】ボタンの不具合修正
            
2014/02/26  1.取消ボタン不具合修正

2014/07/28  1.ログ取得機能追加

2015/07/28  1.文京学園のシステム開発に伴う修正
            -- 科目コンボの基本設定について、以下の通り、固定コードを変更した。
            -- 現状
            --     00-00-00-000000：基本設定
            -- 変更
            --     00-J-00-000000：基本設定（中学）
            --     00-H-00-000000：基本設定（高校）

2015/08/03  1.プロパティ「"useKNJZ350_NENKAN_TESTITEM"」= '1'のとき、年間試験設定画面ボタンを表示

2015/10/05  1.算出設定に科目コードをパラメーターで渡す。

2015/10/06  1.プロパティ「"useJviewControl"」= '1'のとき、観点コントロールを更新できるよう修正

2015/10/21  1.プロパティ「"useIBRecordControl"」= '1'のとき、ＭＹＰ・ＤＰ成績入力コントロールを更新できるよう修正

2015/12/04  1.評定換算設定画面ボタン追加

2016/08/03  1.科目コンボの校種（K）対応
            -- 基本設定（中学）ではなく、基本設定（幼稚園）と表示するように修正

2016/09/19  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照 

2017/03/06  1.Z005の参照をログイン校種により切替える。

2017/03/07  1.Z009の参照をログイン校種により切替える。

2017/03/07  1.ADMIN_CONTROL_DATキー追加
            -- rep-admin_control_dat.sql(rev1.1)

2017/03/30  1.前年度コピーのエラー対応

2017/05/16  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind
            2.パラメーター追加

2017/05/17  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTに設定がない場合の不具合を修正

2018/03/07  1.評定換算設定画面ボタンをコメント

2018/09/13  1.出欠入力コントロールの更新ボタンで他の設定を削除しないように修正

