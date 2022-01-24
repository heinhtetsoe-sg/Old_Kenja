# kanji=漢字
# $Id: readme.txt 76562 2020-09-08 08:10:02Z arakaki $

2011/01/31  1.KNJX180を元に新規作成

2011/02/02  1.データ出力の年組コンボを"SCHREG_REGD_GDAT"の"SCHOOL_KIND"が'H'の年組のみ表示した。
            --（全て出力）の場合、出力するデータは'H'の年組の生徒のみに変更した。

2011/10/28  1.署名あり画面からの処理を追加した。

2013/04/02  1.行数チェックを追加

2015/01/20  1.署名チェックの署名済み判断条件を修正
            -- LAST_OPI_SEQ → CHAGE_OPI_SEQ

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2019/04/01  1.奈良県仕様奈良Time(HTRAINREMARK_DETAIL2_DAT(HTRAIN_SEQ='005').REMARK1)対応

2019/04/26  1.佐賀県通信制仕様 特別活動出席時数(HTRAINREMARK_DETAIL2_DAT(HTRAIN_SEQ='006').REMARK1)対応

2020/09/08  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/08  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2021/02/10  1.CSVメッセージ統一(SG社)

2021/04/27  1.CSVメッセージ統一STEP2(SG社)