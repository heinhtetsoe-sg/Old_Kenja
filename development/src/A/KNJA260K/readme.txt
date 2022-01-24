# kanji=漢字
# $Id: readme.txt 64756 2019-01-18 04:45:29Z matsushima $
2005.09.30 nakamoto 学年評定を出力する機能を追加(とりあえず高校用として作成)
仕様：１．指定学期までの平均を計算
      ２．平均を５段階に変換して出力
      ３．フラグ・出欠はNullとして出力
詳細：１．指定学期までの各学期成績(sem1_rec,sem2_rec,sem3_term_rec)の平均を計算
          および判定類型(judge_pattern)を抽出(参照テーブル：kin_record_dat)
      ２．平均と判定類型で、類型評定段階(type_asses_level)を抽出(参照テーブル：type_asses_mst)

2005.10.20 nakamoto 2005.09.30の再修正--->「判定類型(judge_pattern)ではなく、類型評定コード(type_asses_cd)を抽出」
前回との差異--->今回２と３の部分が2005.09.30と違う
詳細：１．指定学期までの各学期成績(sem1_rec,sem2_rec,sem3_term_rec)の平均を計算(参照テーブル：kin_record_dat)
      ２．指定学期までの各学期成績_合計(sem1_rec_sum,sem2_rec_sum,sem3_term_rec_sum)と
          各学期成績_人数(sem1_rec_cnt,sem2_rec_cnt,sem3_term_rec_cnt)で平均を計算(参照テーブル：type_group_mst)
      ３．２の平均で、類型評定コード(type_asses_cd)を抽出(参照テーブル：type_asses_hdat)
          ただし、評価類型評定コード(type_asses_cd)があればこれを優先に抽出(参照テーブル：type_group_mst)
      ４．１の平均と３の類型評定コードで、類型評定段階(type_asses_level)を抽出(参照テーブル：type_asses_mst)

2006.03.23 m-yama NO001 中学の学年末CSVフォーマットを変更
2006.04.25 o-naka NO002 NO001の修正により、高校のときの学年評定コンボリストが表示されない不具合を修正

2006/10/25 nakamoto 学期評定を出力する機能を追加(高校用として作成)
[仕様]
・指定学期のみの学期成績と類型評定コードで、類型評定段階を抽出して出力
・フラグ・出欠はNullとして出力
[詳細]
１．指定学期のみの学期成績(sem1_rec, sem2_rec, sem3_term_rec)を抽出。
    (参照テーブル：kin_record_dat)
２．指定学期のみの学期成績_類型評定コード(sem1_rec_type_asses_cd, sem2_rec_type_asses_cd, sem3_term_rec_type_asses_cd)
    を抽出。ただし、評価類型評定コード(type_asses_cd)があればこれを優先に抽出。
    (参照テーブル：type_group_mst,type_group_hr_dat)
３．１の学期成績と２の類型評定コードで、類型評定段階(type_asses_level)を抽出。
    (参照テーブル：type_asses_mst)

2006/11/07 nakamoto テスト種別リスト変更時、再読込をしないよう修正した。

2012/03/16  1.学期指定(学年末以外)で、学年評定を選択した場合。読替先科目を出力(元科目の平均、フラグと出欠はNULL)

2013/06/11  1.教育課程対応
            2.修正

2014/06/30  1.科目合併処理変更(SUBCLASS_REPLACE_DAT → SUBCLASS_REPLACE_COMBINED_DAT)

2015/03/06  1.3学期のテスト種別から学期成績、学期評定、学年評定をカット。学年末のテスト種別に学年評定を追加し、成績一覧の学年評定を出力する

2015/03/10  1.3学期のテスト種別に学期評定をもどした

2019/01/18  1.CSV出力の文字化け修正(Edge対応)