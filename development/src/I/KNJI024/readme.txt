# kanji=漢字
# $Id: readme.txt 56588 2017-10-22 12:57:09Z maeshiro $

2007/05/15  新規作成(処理：近大TESTI022　レイアウト：近大TESTI022参考)

2009/07/21  1.下記テーブル変更に伴う修正
            -- grd_hexam_entremark_dat
            -- grd_medexam_det_dat
            -- grd_medexam_hdat
            -- grd_medexam_tooth_dat
            -- grd_regd_hdat
            -- grd_studyrec_dat

2009/11/12  1.下記テーブル変更に伴う修正
            -- grd_hexam_entremark_dat
            -- grd_htrainremark_dat
            -- grd_medexam_det_dat
            -- grd_base_mst

2013/12/07  1.住所のサイズ変更等に伴う修正
            -- rep-schreg_address_dat_rev1.9.sql
            -- rep-schreg_base_mst_rev1.9.sql
            -- rep-grd_base_mst_rev1.15.sql
            2.下記修正未対応分追加
            〇フィールド追加に伴う修正
            -- GRD_GUARDIAN_DAT
            -- GRD_MEDEXAM_DET_DAT
            -- GRD_MEDEXAM_TOOTH_DAT
            -- HEXAM_ENTREMARK_DAT
            〇プロパティー「useCurriculumcd」を追加した。
            -- '1'がセットされている場合は教育課程コード等を使うようにした。
            〇「GRD_BASE_MST」のCUR_ADDR_FLG更新内容修正
            -- NULL ⇒ 「SCHREG_ADDRESS_DAT」のADDR_FLG
            〇GUARDIAN_DAT.GUARANTOR_REAL_NAME、GUARANTOR_REAL_KANA、GUARANTOR_ADDR_FLGを追加

2013/12/08  1.廃盤
