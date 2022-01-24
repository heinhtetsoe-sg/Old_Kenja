// kanji=漢字
// $Id: readme.txt 56591 2017-10-22 13:04:39Z maeshiro $

2012/10/23  1.新規作成

2012/10/24  1.住所1がNULLの時の処理を修正
               - 郵便番号、住所１、住所２をセット（FRESHMAN_DATまたはSCHREG_ADDRESS_DATより）

2012/10/25  1.名前、電話番号がNULLの時の処理を追加

2012/11/01  1.スクーリング日の参照・更新テーブルを変更した。
            -- SCHREG_BASE_YEAR_DETAIL_MST.BASE_REMARK1
            -- BASE_SEQ : 002

2012/11/08  1.戻る押下で履修登録画面をサブミットする。

2012/11/14  1.前籍校入力完了チェックボックス追加
               - SCHREG_BASE_DETAIL_MST.BASE_REMARK5
               - BASE_SEQ : 004
               - チェックON時、査定単位数入力不可