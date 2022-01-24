// kanji=漢字
// $Id: readme.txt 58282 2018-01-30 08:01:58Z yamashiro $

2017/01/10  1.KNJL021Gを元に新規作成

2017/01/12  1.文言修正（受験番号→受検番号）

2017/07/25  1.テーブル変更（APPLICANTDIV追加）に伴い修正
            2.下記項目追加（項番22）
            --１．学習の記録
            --２．総合的な学習の時間の記録
            --３．特別活動の記録及び行動の記録
            --４．出欠・健康の記録
            --５．その他参考となる諸事項

2017/07/27  1.各学年の評定の合計は、更新時に自動でセット（入力項目はなし）

2017/07/31  1.欠席日数、欠席理由は下記フィールドとする。
            -- ENTEXAM_APPLICANTCONFRPT_DATの
            -- ABSENCE_DAYS、ABSENCE_DAYS2、ABSENCE_DAYS3
            -- ABSENCE_REMARK、ABSENCE_REMARK2、ABSENCE_REMARK3

2017/09/20  1.欠席理由の主なものを全角30文字に変更
            2.総合的な学習の時間の記録を全角250文字に変更
            -- ALTER TABLE ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT ALTER COLUMN REMARK4 SET DATA TYPE VARCHAR(750)
            -- ALTER TABLE ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT ALTER COLUMN REMARK5 SET DATA TYPE VARCHAR(750)
            -- ALTER TABLE ENTEXAM_APPLICANTCONFRPT_DETAIL_DAT ALTER COLUMN REMARK6 SET DATA TYPE VARCHAR(750)

2017/11/14  1.「１.学習の記録」に10列目を追加し、評定、観点を登録できるようにする。
            --また、教科名は文言で登録できるようにする（名称マスタ「L008」で登録してもらう）
            2.「５.その他参考となる諸事項」の文字数を増やす
            -- 20×4行　⇒　80×7行に変更
            -- rep-entexam_applicantconfrpt_dat.sql (rev.57082)

2017/12/21  1.ENTEXAM_APPLICANTCONFRPT_DAT.TOTAL_ALLは、9科目までの合計に変更

2018/01/10  1.【<<】【>>】ボタン追加

2018/01/30  1.受付番号 → 受検番号

2020/12/21  1.リファクタリング
            2.「性別」を追加
