# kanji=漢字
# $Id: readme.txt 65436 2019-01-31 04:21:10Z tawada $

2017/10/05  1.KNJL014Pを元に新規作成

2017/10/31  1.出身学校コード12桁対応。所在地コード5桁に修正

2017/11/01  1.出身校所在地コードの取込をカット

2017/11/10  1.出身学校コード12桁対応の修正
            --プロパティーuseFinschoolcdFieldSizeが12なら出身学校マスタのコードの0埋め、チェックを12桁でおこなう
            2.ラジオボタンのラベル押し下げ時の不具合修正

2017/12/06  1.出身学校コード12桁対応をカット
            -- 7桁固定に変更
            2.出身学校の変換テーブル参照に伴う修正
            -- ENTEXAM_MIRAI_FS_REP_DAT
            -- 賢者の出身学校コードに変換した値をセット

2018/01/10  1.保護者の郵便番号、都道府県、市区町村、町名・番地の未入力はエラーにしない。

2018/01/12  1.取込項目から不要なホワイトスペースを取り除くように修正

2018/01/24  1.変換用出身学校コードが存在しない時、エラーではなく空白のまま取込む。

2018/01/30  1.「5:受験不可」を「4:欠席」に変更

2018/02/01  1.取り込み時にその回以降の全てのデータを削除してから取り込む。
            ※削除テーブル
            ENTEXAM_APPLICANTADDR_DAT
            ENTEXAM_APPLICANTBASE_DAT
            ENTEXAM_APPLICANTBASE_DETAIL_DAT
            ENTEXAM_APPLICANTCONFRPT_DAT
            ENTEXAM_HALL_YDAT
            ENTEXAM_JUDGE_AVARAGE_DAT
            ENTEXAM_JUDGE_TMP
            ENTEXAM_MIRAICOMPASS_DAT
            ENTEXAM_PASSINGMARK_MST
            ENTEXAM_RECEPT_DAT
            ENTEXAM_SCORE_DAT
            ※詳細
            1回目、2回目、3回目の取り込みは、それぞれの回数の受験者に限定して取り込んでもらう運用とする。
            テストをする関係上、
            それぞれの取り込み時に1つ目のデータの受験番号で回数を判定し、
            その回以降のデータをクリアしてから取り込む。差分の取り込みという概念はない。

2018/02/02  1.緊急連絡先を追加

2018/02/03  1.処理済件数が0件の時、全件クリアされる不具合修正

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2019/01/31  1.取込チェックで「志願者SEQ」を6桁→7桁に変更
