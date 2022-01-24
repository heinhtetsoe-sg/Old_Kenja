# kanji=漢字
# $Id: readme.txt 56588 2017-10-22 12:57:09Z maeshiro $

2014/02/25  1.KNJA110Aを元に新規作成

2014/03/18  1.復学日の重複チェック追加
            2.更新対象テーブルからGRD_REGD_HDATをカット
            3.SCHREG_REGD_DATの更新処理をupdate/insertに変更
            4.復学対象生徒チェックを変更
            5.バックアップテーブルの削除に復学日の条件を追加

2014/04/25  1.生徒検索を修正
            -- 検索条件の対象生徒を取得し、各生徒のMAX年度・学期のデータを表示するよう修正
            -- 検索条件の年度すると、SCHREG_BASE_MSTのGRD_DATEの年度と一致する生徒を取得
            2.検索画面の項目名「年度」「年組」に「卒業」を追加

2014/04/30  1.以下を修正した。
            -- 入学の入力項目追加
            -- 復学する、学年のSCHREG_ENT_GRD_HIST_DATの更新、追加
            -- SCHREG_BASE_MSTの入学関連の更新

2014/05/01  1.内外区分コンボを追加
            2.エラー後、入学の入力項目に取得したデータが表示される不具合を修正
            3.SCHREG_ENT_GRD_HIST_DATの更新項目漏れ
            4.SCHREG_ENT_GRD_HIST_DATのバックアップテーブルにはMAXのみ更新
            5.入力可能項目の背景色を変更

2014/05/19  1.対象データ選択時の右画面にログイン年度、ログイン学期を表示

2014/06/30  1.SCHREG_BASE_DETAIL_MSTの処理を追加（BASE_SEQが"001"のみ）
            2.コンボの不具合修正（内外区分が'0'で復学したあとの表示がnull）

2014/07/01  1.前回のSCHREG_BASE_DETAIL_MSTの処理をカット
            2.SCHREG_ENT_GRD_HIST_DATのTENGAKU_SAKI_ZENJITU、NYUGAKUMAE_SYUSSIN_JOUHOUにNULLをセット
            3.SCHREG_BASE_DETAIL_MSTの更新処理を追加
            4.SCHREG_BASE_MST.GRD_TERMの値セットの不具合を修正
            5.復学後の入学入力項目のNULL表示をカット

2014/09/22  1.検索画面の項目名を「卒業」→「異動」に変更
            2.入力画面の「※ 入学」→「※ 再入学」に変更

2015/07/29  1.DBエラー修正

2015/11/12  1.卒業欄に「転学先学年」を追加
            2.対象生徒の校種が高校のとき、卒業欄の「学校住所2」の項目名を「課程・学科等」に変更

2016/09/19  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照 

2016/09/27  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照 

2016/02/15  1.卒業欄修正
            -- 固定文言「卒業」→「異動/卒業」に変更
            -- 固定文言「日付」→「自校退校日」に変更
            -- 区分と日付の位置を入れ替え
            -- 転学先前日の校種による表示制限をカット

2017/02/20  1.出身学校手入力用プロパティー追加
            --プロパティー「use_finSchool_teNyuryoku_校種」= 1の時手入力

2017/02/22  1.出身学校欄修正
            --テキストボックス（プロパティー「use_finSchool_teNyuryoku_校種」= 1の時）を通常表示に変更。

2017/02/22  1.顔写真の表示/非表示機能(useDispUnDispPicture)

2017/09/15  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2020/12/01  1.リファクタリング
            2.php7対応
