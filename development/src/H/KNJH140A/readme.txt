# kanji=漢字
# $Id$

2009/11/13  1.tokio:/usr/local/development/src/H/KNJH140からコピーした。
            2.リファクタ

2010/02/17  1.通学手段情報の最寄駅路線、最寄駅、通学経由駅をカット。
            2.災害時帰宅グループ番号、通学方法（乗車駅、路線、下車駅、通学手段）を追加した。

2010/03/04  1.通学手段情報に乗車駅名、路線名、下車駅名を追加した。
            　（通学手段が「1:電車通学」の場合、乗車駅名、路線名、下車駅名を表示する。）

2011/02/28  1.XLS出力処理追加
            -- プロパティー追加：useXLS(何かセットされていれば、XLS出力。基本'1'をセット。)

2011/03/08  1.取込エラーを修正。
            -- （３：通学手段）でCSV取込時のエラー「通学手段1が数値ではありません。」

2011/03/18  1.テンプレートを1本に統一

2011/03/25  1.「退学者・転学者・卒業生は出力しない」チェックボックスを追加した。

2011/03/29  1.ボタン名称変更

2011/03/30  1.以下の修正をした
            -- 単独で呼び出された場合閉じる
            -- 親から呼ばれた場合は、親の権限を使用する

2011/04/01  1.テンプレートのパス修正

2011/04/07  1.タイトル文言をプロパティー「useXLS」で切り替えるよう修正した。

2011/04/21  1.修正

2011/07/06  1.対象データにその他（通知票等 送付先）を追加し、CSV出力／取込に対応

2011/07/07  1.対象データのその他（通知票等 送付先）CSVファイル取り込み時にInsert処理追加

2011/08/03  1.権限チェック「checkAuth」をカットした。

2012/09/18  1.SCHREG_SEND_ADDRESS_DATのキー追加修正

2012/12/13  1.対象データが5:その他の時、CSV取込時に更新先のデータに複数（div=2のその他２情報）が存在する場合のDBエラー対応

2013/05/12  1.更新後のメッセージ修正
            2.テンプレートの修正
            
2013/07/09  1.プログラムIDを変更して、新規登録した
                - KNJH140 ⇒ KNJH140A

2013/07/22  1.異動情報の条件を修正
                - GRD_DATE < EDATE　⇒ GRD_DATE <= EDATE

2013/12/07  1.住所のサイズ変更等に伴う修正
            -- rep-schreg_send_address_dat_rev1.5.sql

2016/12/14  1.校種対応

2017/01/06  1.校種対応の修正漏れ
            2.家族情報に親族の在卒区分、学年追加

2017/03/07  1.文京の場合、「新入生」チェックを追加し通学手段情報でデータを取り込む。学籍番号を新入生データでチェックする。

2017/05/10  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/12/11  1.緊急連絡先情報に項目追加
            -- 本人電話番号、緊急連絡先電話番号２、緊急連絡先電話番号備考、優先順位

2018/04/05  1.取り込み処理で、学籍番号のゼロ埋めを追加

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2020/06/30  1.「3：通学手段情報」CSV取込 最寄駅までの手段に数値以外も入力できるように修正

2020/08/28  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/11/12  1.プロパティー「NAME_INPUT_SIZE」に値が設定されている時、親族氏名、緊急連絡先1/2氏名、その他氏名の入力文字数がこのプロパティ値以下かチェックするよう、変更
              ※未設定時は既存値。
            2.プロパティー「chkNameInputHankaku」= 1の時、親族氏名、緊急連絡先1/2氏名、その他氏名に半角文字が混ざっていないか、チェックするよう、変更
            3.プロパティー「chkAddrInputHankaku」= 1の時、その他住所1/2に半角文字が混ざっていないか、チェックするよう、変更
              -- プロパティー「NAME_INPUT_SIZE」追加
              -- プロパティー「chkNameInputHankaku」追加
              -- プロパティー「chkAddrInputHankaku」追加

2020/12/02  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更
            3.プロパティー「ADDR_INPUT_SIZE」に値が設定されている時、その他住所1/2の入力文字数がこのプロパティ値以下かチェックするよう、変更
              -- プロパティー「ADDR_INPUT_SIZE」追加

2021/02/17  1.CSVメッセージ統一(SG社)