# kanji=漢字
# $Id: b7c43a0c5c0f5962c62f85c21fc29b83f07e132e $

2010/04/01  1.tokio:/usr/local/development/src/I/KNJI030からコピーした。
            2.「現氏名」、「現氏名かな」の桁数を変更した。

2010/08/04  1.「住所フラグ」を追加

2013/12/08  1.住所のサイズ変更等に伴う修正
            -- rep-grd_base_mst_rev1.15.sql

2013/12/11  1.住所のサイズ変更等に伴う修正

2014/08/22  1.ログ取得機能追加

2018/10/29  1.リファクタリング
            -- オブジェクト作成をメソッドにした。
            -- 不要な処理コメント等カット
            2.住所不明フラグ、備考欄追加
            -- grd_base_detail_mst.sql(rev.63088)

2019/08/22  1.左画面の校種コンボ用パラメーター追加(URL_SCHOOLKIND)

2020/11/12  1.プロパティー「NAME_INPUT_SIZE」に値が設定されている時、現氏名の入力文字数がこのプロパティ値以下かチェックするよう、変更
              ※未設定時は既存値。
            2.プロパティー「chkNameInputHankaku」= 1の時、現氏名に半角文字が混ざっていないか、チェックするよう、変更
            3.プロパティー「chkAddrInputHankaku」= 1の時、住所1/2、実家住所1/2に半角文字が混ざっていないか、チェックするよう、変更
            -- プロパティー「NAME_INPUT_SIZE」追加
            -- プロパティー「chkNameInputHankaku」追加
            -- プロパティー「chkAddrInputHankaku」追加

2020/12/02  1.リファクタリング
            2.プロパティー「ADDR_INPUT_SIZE」に値が設定されている時、現住所の住所1/2、実家住所1/2の入力文字数が
              このプロパティ値以下かチェックするよう、変更
              -- プロパティー「ADDR_INPUT_SIZE」追加