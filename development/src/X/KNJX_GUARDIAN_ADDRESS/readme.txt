/*** KNJX_GUARDIAN_ADDRESS ***/
$Id: readme.txt 56591 2017-10-22 13:04:39Z maeshiro $

2011/07/06  1.新規作成

2012/05/30  1.GUARDIAN_DATにデータがない場合、リストから選択しても保護者情報に表示されない不具合を修正した。

2013/12/11  1.住所のサイズ変更等に伴う修正
            -- rep-guardian_dat_rev1.4.sql
            -- rep-guardian_address_dat_rev1.5.sql
            
2016/02/10  1.E-mailを50文字制限に変更

2016/12/29  1.電話番号２追加

2020/12/04  1.リファクタリング
            2.プロパティー「chkAddrInputHankaku」= 1の時、住所、方書きに半角文字が混ざっていないか、チェックするよう、変更
              -- プロパティー「chkAddrInputHankaku」追加
            3.プロパティー「ADDR_INPUT_SIZE」に値が設定されている時、住所、方書きに入力文字数がこのプロパティ値以下かチェックするよう、変更
              -- プロパティー「ADDR_INPUT_SIZE」追加
