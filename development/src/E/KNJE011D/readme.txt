# kanji=漢字
# $Id: ca14ac57d057b0adf91813b57191de5b6accbb6c $

2020/03/11  1.KNJE011A rev. 72059をコピーして作成
            2.ボタン名称変更
            --【プレビュー／印刷】→【所見確認用】

2020/03/30  1.ボタン名称変更

2020/04/08  1.3年用フォームの特別活動の記録の文字数設定追加

2020/05/01  1.出欠備考の文字数設定追加
            2.フォームのX年制取得修正

2020/05/08  1.備考の1行文字数を45に変更

2020/05/11  1.指導上参考となる諸事項6分割の内部のスクロールバーを非表示に変更
            2.備考欄のヘッダーについて、2を表示しない場合は1の番号も表示しない
            3.備考2欄を表示しない場合の更新ボタン位置修正

2020/06/01  1.「生徒指導要録より読込」押下時に確認ダイアログを追加
             -- 「OK」押下時は上書き読込、「キャンセル」押下時は追加読込

2020/06/08  1.指導上参考となる諸事項のボタンをひとつにまとめた

2020/06/09  1.指導要録の総合所見欄をカット
            2.ボタン文言変更
            3.指導上参考となる諸事項の取込画面のタイトルに年度を追加

2020/06/11  1.指導上参考となる諸事項の取込ボタン位置を変更
            2.記録備考選択が3)部活動...ではなく2)行動の特徴...に取り込まれる不具合を修正
            3.諸事項の取込みはカーソル位置への挿入ではなく既存データの最後に追加する。
            4.ボタン文言変更

2020/06/13  1.指導要録の総合所見欄を追加 (2020/06/09の修正を取消)
            2.特記事項なしチェックボックス追加

2020/06/16  1.KNJE070で使用しているプロパティを追加

2020/06/19  1.年度毎入力のウィンドウの大きさを変更
            2.諸事項に特記事項なしのボタンを追加

2020/06/24  1.京都府で総合的な学習の時間が入力できない不具合を修正

2020/06/25  1.諸事項と備考のスクロール内のスクロールをカット
            2.学習成績概評のマルＡ表示対象者が表示されない不具合を修正

2020/06/26  1.特別活動の記録の文字数を16文字18行に変更
            2.プロパティーtyousasho6bunkatsu_homeroomShojikouTorikomiが1の場合、指導上参考となる諸事項年組一括取込ボタンを表示
            3.京都府は要録の総合的な学習の時間の取込みの頭に学年名称を追加する

2020/07/02  1.「指導上参考となる諸事項」の項目名表記の統一対応

2020/07/13  1.入学区分が'4','5','7'のときに表示される追加年度コンボの不具合修正
            2.指導上参考となる諸事項の要録総合所見参照に3分割の場合の処理を追加

2020/07/16  1.プロパティーuseAttendrecRemarkSlashFlgを固定OFFに変更

2020/07/17  1.常磐の処理追加
              指導上参考となる諸事項の要録総合所見参照に3分割を表示する
              右上生徒指導要録より読み込みでダイアログを表示せずにクリアして読み込みを実行する

2020/07/27  1.学習成績概評のマルA表示対象者が更新されない不具合を修正

2020/07/29  1.項目名「出校の記録備考」を「出欠の記録備考」に変更。ただしプロパティuseTitleShukkounoKiroku = 1の場合、各「出欠～」を「出校～」に変更
            2.諸事項(5)表彰・顕彰等の記録に記録備考選択ボタンを追加

2020/07/30  1.プロパティnotUseAttendrecRemarkTokkiJikouNasiが1の場合、出欠備考に特記事項なしチェックボックスを表示しない
            2.HEXAM_ENTREMARK_DATの元レコードがある場合、TRAIN_REF1、TRAIN_REF2、TRAIN_REF3は元レコードで更新する

2020/07/31  1.プロパティuseAttendSemesRemarkDat=1の場合の取込み・参照ボタンの文言「まとめ出欠備考取込」「まとめ出欠備考参照」を「出欠備考全月取込」「出欠備考全月参照」

2020/08/06  1.総合的な学習の時間の指導要録からの取込で対象範囲から中学をカット
            2.プロパティtyousashoShokenNyuryokuTorikomiTotalstudyHeaderが1の場合、指導要録の年度ごとの総合的な学習の時間取込でSCHREG_REGD_GDAT.GRADE_NAME1を先頭に追加する
            3.プロパティtyousasyo2020TotalstudyactGyou、tyousasyo2020TotalstudyvalGyou追加

2020/08/07  1.プロパティtyousashoShokenNyuryokuSpecialActNotUseClubが1の場合、特別活動に部活動選択を表示しない

2020/08/12  1.ボタン「マラソン大会選択」,「臘八摂心皆勤」の追加
            --NAME_MST の NAMECD1='Z010' が「koma」の時だけ表示

2020/08/14  1.プロパティtyousasyo2020remarkGyou、tyousasyo2020specialactrecGyou3、tyousasyo2020specialactrecGyou4、tyousasyo2020shojikouGyou3、tyousasyo2020shojikouGyou4追加

2020/08/17  1.奈良県は「特記事項なし」を「特記事項なし。」に変更

2020/08/24  1.備考で特記事項なし選択時に備考欄が非活性にならない不具合を修正

2020/09/04  1.プロパティuseTotalstudySlashFlgが1の場合、総合的な学習の時間に「斜線を入れる」チェックボックスを追加。 プロパティuseAttendrecRemarkSlashFlgが1の場合、出欠備考に「斜線を入れる」チェックボックスを追加。

2020/09/23  1.プロパティtyousasyo2020shojikouGyou追加
            2.不具合修正

2020/11/19  1.島根県用のプロパティtyousasyo_shokenTable_Seqが1の場合の処理追加
              参照パターンコンボ、対象パターンコンボを追加し、対象パターンにセットしたパターンでxxx_SEQ_DATのテーブルを更新を行う。
              子画面では親画面でセットした対象パターンでxxx_SEQ_DATのテーブルを更新を行う。

2020/11/26  1.プロパティtyousasyo_shokenTable_Seqが1の場合、指導要録所見一括取込処理の追加

2020/11/30  1.指導要録所見一括取込画面の生徒一覧ソート不具合の修正

2020/11/30  1.指導要録所見一括取込処理の修正
            --HEXAM_ENTREMARK_TRAINREF_DAT→HTRAINREMARK_TRAINREF_DATへ修正
            --取込み先フィールドTRAIN_SEQのコードを001～006→101～106へ修正
            --指導要録所見一括取込処理のSQL文のメソッド化

2020/02/03  1.変数名修正($this → $model)

2020/02/04  1.備考欄をパターン毎に参照、登録するよう修正
            -- 備考のパターン用テーブルは「HEXAM_ENTREMARK_REMARK_SEQ_HDAT」を使用
            2.リファクタリング

2020/02/08  1.指導要録所見一括取込（プロパティ「tyousasyo_shokenTable_Seq」=1の場合）の不具合修正のため下記の対応を実施
            -- 指導要録所見一括取込で、指導要録所見データ（HTRAINREMARK_DAT）を取得する条件からANNUALを削除
            -- 指導要録所見一括取込から取り込む対象となる調査書データ（HEXAM_ENTREMARK_SEQ_DAT）の条件からANNUALを削除
            -- 「指導要録所見一括取込」ボタンを表示する条件に在籍の条件を追加

2020/02/16  1.指導要録所見一括取込（プロパティ「tyousasyo_shokenTable_Seq」=1の場合）の不具合修正のため下記の対応を実施
            -- 総合的な学習の時間の内容・評価の取り込み処理の追加（HEXAM_ENTREMARK_SEQ_HDAT へ取込み）
            -- 取込対象の生徒の年度の取得SQLを修正

2020/02/17  1.卒業生入力の場合、島根県用のプロパティtyousasyo_shokenTable_Seqが1でもパターンコンボを使用せずxxx_SEQ_DATのテーブルを使用しない

2021/03/08  1.プロパティ「useTransferButton_H」が"1"の時、備考の下に異動情報ボタン表示する
            -- 異動情報ボタン押下時、異動情報選択画面を開き、取込ボタン押下時、備考に異動情報を取り込む

2021/03/10  1.プロパティ「useTransferButton_H」が"1"の時に使用する、異動情報選択画面を「KNJX_TRANSFER_SELECT/knjx_transfer_selectindex.php」を開くよう修正
            -- 異動情報選択画面に使用していた不要な処理、および、ファイルの削除
