// kanji=漢字
// $Id: $

2021/03/30  1.新規作成

2021/04/02  1.CSVデータ取得SQLに以下の修正をした。
            --誤った表別名への参照を訂正
            --ENTEXAM_STD_APPLICANTBASE_DATへのENTERING_FLGフィールド追加に伴う修正
            --リファクタリング
            2.画面レイアウト調整
            3.志望コース取得SQLに以下の修正をした。
            --同じコースが重複して出てくる不具合修正
            --リファクタリング
            4.「回数」コンボ取得SQLに以下の修正をした。
            --抽出条件におけるAPPLICANT_DIVの指定漏れにより、同じ回数が重複して出てくる不具合修正
            --リファクタリング
            5.必須項目チェック処理を追加(校種、入試区分)

2021/04/03  1.以下の修正をした。
            -- 母集団の条件修正。
            -- 受験番号 EXAMNO → RECEPTNO
            -- 余分な一列出力をカット

2021/04/16  1.CSV出力のENTEXAM_STD_RANK_DAT結合条件の修正（EXAMNO → RECEPTNO）
