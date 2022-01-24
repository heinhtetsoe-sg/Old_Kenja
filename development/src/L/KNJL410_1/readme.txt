# kanji=漢字
# $Id: readme.txt 56589 2017-10-22 12:59:34Z maeshiro $

2016/10/05  1.新規作成。来校者情報登録（高校）

2016/10/18  1.受験種別コンボのＳＱＬ条件修正

2016/10/19  1.特待生コンボは、NAME2を表示（NAME2がある分）

2016/10/20  1.テーブル変更にともない修正
            下記のテーブルのkeyからTOUROKU_DATEをカット。
            RECRUIT_VISIT_DAT
            下記のテーブルのフィールドTOUROKU_DATEをカット。
            RECRUIT_VISIT_MOCK_DAT
            RECRUIT_VISIT_SCORE_DAT
            RECRUIT_VISIT_ACTIVE_DAT

2016/10/28  1.受験種別の参照を名称マスタ「L407」に変更。
            2.模試偏差値の各科目を整数から少数に変更。
            -- RECRUIT_VISIT_MOCK_DAT

2016/10/31  1.諸活動名称は、名称マスタ「L408」を参照するように変更
            -- NAMECD2　　：RECRUIT_VISIT_ACTIVE_DAT（SEQ_DIV='1'）のSEQ
            -- NAME1　　　：諸活動名称
            -- NAMESPARE1 ：活動評価点
            ※ 名称マスタ「L408」設定例
            -- NAMECD2　　：002
            -- NAME1　　　：英検・漢検・数検３級以上
            -- NAMESPARE1 ：2

2016/11/01  1.３・５・９科合計算出ボタンを追加（通知票評定）
            2.模試名テキスト追加（00009999:その他を選択した時、入力可能）
            3.３・５科平均算出ボタンを追加（模試偏差値）
            4.入力確認チェックボックスを追加（通知票評定と模試偏差値）
            5.諸活動のポイント合計を表示
            6.担当コンボにUSERGROUP_DATのGROUPCD（'0009'または'9999'）条件を追加
            -- rep-recruit_visit_mock_dat.sql (1.2)
            -- rep-recruit_visit_dat.sql (1.1)

2016/11/02  1.備考（全角２０文字×３行）を追加
            -- rep-recruit_visit_dat.sql (1.2)

2016/11/04  1.追加ボタンカット
            2.通知票評定の学期名を固定表示に変更
            3.模試偏差値の3科、5科の算出修正

2016/11/08  1.V_STAFF_MSTは、ログイン年度を参照するように変更。
            2.担当コンボのSQL条件修正
            -- V_STAFF_MSTは、ログイン年度を参照するように変更。
            -- GROUPCD'0009'を'0010'に変更。

2016/12/20  1.担当コンボのSQL条件修正
            -- GROUPCDを'0010'のみ参照に変更
