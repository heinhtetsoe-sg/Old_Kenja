/*** KNJX_GUARANTOR_ADDRESS ***/
$Id: readme.txt,v 1.4 2013/12/11 05:14:00 m-yama Exp $

2012/03/16  1.新規作成

2012/04/10  1.文言修正 「保護者」⇒「保証人」

2012/05/30  1.履歴表示の余分な列をカットした。
            2.生年月日、勤務先名称、勤務先電話番号をカットした。
            3.兼ねている公職を追加した。
            4.GUARDIAN_DATにデータがない場合、リストから選択しても保証人情報に表示されない不具合を修正した。

2013/12/11  1.住所のサイズ変更等に伴う修正
            -- rep-guardian_dat_rev1.4.sql
            -- rep-guarantor_address_dat_rev1.4.sql

2020/12/04  1.リファクタリング
            2.プロパティー「chkAddrInputHankaku」= 1の時、住所、方書きに半角文字が混ざっていないか、チェックするよう、変更
              -- プロパティー「chkAddrInputHankaku」追加
            3.プロパティー「ADDR_INPUT_SIZE」に値が設定されている時、住所、方書きに入力文字数がこのプロパティ値以下かチェックするよう、変更
              -- プロパティー「ADDR_INPUT_SIZE」追加
            4.require_once漏れを修正

2021/04/06  1.プロパティー「PublicOffice_TitleNameSettei」= 1の時、保証人等の勤務先タイトル名称を「就業先」、初期値を「兼ねている公職」に設定
            2.php7対応
