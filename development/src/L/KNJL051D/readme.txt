// kanji=漢字
// $Id: readme.txt 76713 2020-09-10 07:58:14Z arakaki $

2018/01/19  1.新規作成

2018/01/22  1.評価を名称マスタ「L027」参照に変更
            2.Enterキーの入力欄移動追加

2018/01/24  1.参照テーブル変更
            -- ENTEXAM_RECEPT_DAT → ENTEXAM_APPLICANTBASE_DAT

2018/01/25  1.評価チェックやEnterキー移動の際、全選択になるよう修正

2018/02/23  1.面接欠席の場合は、出欠の欄を欠席として評価の欄を非活性とする

2020/01/07  1.入力済みの評価が削除される処理の修正
            2.欠席の条件を修正

2020/01/10  1.更新処理で、削除->登録の一連の処理を、登録状況に合わせて登録、更新のいずれかを処理するよう、変更
            2.入力不可の項目については登録/更新しないよう、変更
            3.受験生の表示切替ボタンの表示名称を変更

2020/09/10  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/03  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更
