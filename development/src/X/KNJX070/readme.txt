// kanji=漢字
// $Id: readme.txt 76498 2020-09-07 08:23:52Z arakaki $

2005/09/28 CSV書出にて、最終項目にダミー項目を追加・・・「１６行目以降のカンマが消える」の対応策として修正

2007/04/20 nakamoto
★ 取込時の「０埋め」の処理を修正した。（d → s）
-- 「数字以外を組み合わせているコード」を取込む場合の対応である。
-- 例）
-- 修正前：sprintf("%08d", "1000M2") → 結果（誤）："00001000"
-- 修正後：sprintf("%08s", "1000M2") → 結果（正）："001000M2"

2011/02/22  1.XLS出力処理追加
            -- プロパティー追加：useXLS(何かセットされていれば、XLS出力。基本'1'をセット。)

2011/02/28  1.encoding指定

2011/03/07  1.タイトル名変更

2011/03/18  1.テンプレートを1本に統一

2011/03/30  1.以下の修正をした
            -- 単独で呼び出された場合閉じる
            -- 親から呼ばれた場合は、親の権限を使用する

2011/04/01  1.テンプレートのパス修正

2011/04/07  1.タイトルの固定文字「(1.1)」を削除した。

2011/06/17  1.W_CSVMSG_PRG_DAT作成に伴う修正

2011/06/22  1.拡張子チェック追加

2011/11/24  1.取込後のメッセージを変更した。

2014/01/17  1.STAFFCDフィールドサイズ変更に伴う修正
             - Properties["useStaffcdFieldSize"]=10のときのみ、対応

2014/06/26  1.ログ取得機能追加

2016/09/21  1.校種条件追加
            -- プロパティー「useSchool_KindField」とSCHOOLKINDを参照

2017/05/25  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2020/09/07  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/07  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2021/02/26  1.CSVメッセージ統一(SG社)

2021/03/10  1.京都PHPバージョンアップ対応
