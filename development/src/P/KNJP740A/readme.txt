# kanji=漢字
# $Id: readme.txt 72871 2020-03-11 00:37:11Z tawada $

2019/04/02  1.新規作成(返金は未作成)

2019/04/04  1.COLLECT_SCHOOL_BANK_MSTのキー追加に伴う修正
            --校種「--全て--」を選択した時のヘッダーデータはログイン校種のマスタデータを参照する

2019/04/09  1.以下の修正
            --CSV出力時、「COLLECT_SLIP_PLAN_CSV_DAT」を更新する
            --CSV出力時、顧客番号が未設定が一人でもいればエラー

2019/05/29  1.校種に共通を追加

2019/06/25  1.校種はCOLLECT_SCHOOL_BANK_MSTに設定されているもののみ表示する。

2019/07/26  1.相殺済の金額を減算する。

2019/08/06  1.以下の修正をした。
            -- 学校負担金の変更(学校減免＋学校負担 → 学校負担)に伴い、学校減免を減算対象に加える。
            -- SQLの不具合を修正

2019/10/01  1.取扱銀行を追加。
            -- rep-collect_school_bank_mst.sql(rev.69964)

2019/10/16  1.プロパティ「IncludeBankTransferFee」が"1"の時、
            --金額 + 手数料で出力
            2.COLLECT_SLIP_PLAN_CSV_DATに登録する金額にも減免額を考慮

2019/11/19  1.０円の人は対象外

2019/12/06  1.月の横に、指定月の引き落とし月日コンボを追加
            2.出力ファイル名変更。ファイル名＋指定月日

2019/12/09  1.手数料を加算しない学校の入金額の不具合を修正
            2.学校減免の取得フィールドを変更。PLAN_MONEY → DECISION_MONEY

2019/12/18  1.入金予定日のデータのみ抽出するよう修正

2020/01/15  1.CSV出力後、振替日変更の値が引き落とし月日コンボにセットされてしまう問題を修正

2020/01/28  1.REGISTBANK_DATに登録しているレコードのBANKCDが3桁の場合に日付コンボが出ない問題を修正

2020/01/29  1.銀行コードを4桁に0埋めして比較するように修正

2020/03/11  1.REGISTBANK_DATの「SCHOOL_KIND」削除に伴う修正

2020/03/18  1.リファクタリング
