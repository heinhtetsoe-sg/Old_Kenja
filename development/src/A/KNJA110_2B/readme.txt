# kanji=漢字
# $Id: readme.txt 56585 2017-10-22 12:47:53Z maeshiro $

2014/10/03  1.KNJA110_2A(1.34)を元に作成

2014/10/08  1.以下の通り、ＤＢエラー(桁数オーバー)となる不具合修正
            -- [住]を選択して、生徒の急用連絡先を入力(桁数オーバーの入力)
            -- [更新]するとＤＢエラー(桁数オーバー)となる不具合修正
            ※ メニュー起動後、最初の１回のみ発生
            2.生徒の住所欄に、急用連絡先3を追加
            -- SCHREG_BASE_DETAIL_MST
            -- BASE_SEQ 005
            -- BASE_REMARK1 急用連絡先 20文字
            -- BASE_REMARK2 急用連絡氏名 20文字
            -- BASE_REMARK3 急用連絡続柄 10文字
            -- BASE_REMARK4 急用連絡電話番号 半角14byt

2016/04/19  1.氏名等履歴の予約機能追加

2017/01/06  1.生徒、保護者、保護者２に電話番号２を追加
            2.保護者の勤務先電話番号の更新チェックの不具合修正

2017/05/19  1.互換での表示が崩れるのを修正
