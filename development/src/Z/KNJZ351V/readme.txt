# kanji=漢字
# $Id: readme.txt 60974 2018-07-02 10:13:24Z yamauchi $

-----
学校：宮城県
-----

2013/07/18  1.新規。初版。

2014/01/19  1.文言修正
            -- 成績入力（算出元）
            2.成績入力のリスト名称を修正
            -- 学期名＋ハイフン＋考査種別名

2014/01/22  1.算出先コンボを２行目に移動

2014/07/28  1.ログ取得機能追加

2015/07/28  1.文京学園のシステム開発に伴う修正
            -- 科目コンボの基本設定について、以下の通り、固定コードを変更した。
            -- 現状
            --     00-00-00-000000：基本設定
            -- 変更
            --     00-J-00-000000：基本設定（中学）
            --     00-H-00-000000：基本設定（高校）

2015/10/05  1.親画面で選択した科目をデフォルト値とする。

2016/02/08  1.登録内容を画面左に表示するよう修正
            2.前年度コピー機能を追加

2016/09/19  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照 

2017/05/16  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/09/15  1.パラメーターの受け取り方を修正

2018/07/02  1.左画面のデータ取得にソート追加
