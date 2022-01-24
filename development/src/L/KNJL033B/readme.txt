# kanji=漢字
# $Id: readme.txt 56583 2017-10-22 12:43:10Z maeshiro $

2013/11/12  1.新規作成

2013/11/15  1.年度コンボ追加　
             - ENTEXAM_PAYMENT_MONEY_MSTテーブルにENTEXAMYEARフィールド追加
            2.次年度作成機能追加
            3.文言修正
            
2013/11/18  1.テーブル名変更
             -ENTEXAM_PAYMENT_MONEY_MST　⇒　ENTEXAM_PAYMENT_MONEY_YMST

2014/12/15  1.次年度作成ボタンを押したらエラーとなる不具合修正
            -- メソッド名称を訂正
            -- 誤：getCheckPaymentMoneyMstSql
            -- 正：getCheckPaymentMoneyYmstSql
