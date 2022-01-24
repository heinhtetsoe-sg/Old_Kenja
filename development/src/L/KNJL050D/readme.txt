// kanji=漢字
// $Id: readme.txt 76712 2020-09-10 07:23:02Z arakaki $

2018/01/18  1.新規作成

2018/01/19  1.入力教科コンボの「'4'を除く」をカット

2018/01/22  1.Enterキーの入力欄移動追加

2018/01/24  1.参照テーブル変更
            -- ENTEXAM_RECEPT_DAT → ENTEXAM_APPLICANTBASE_DAT

2018/01/25  1.得点チェックやEnterキー移動の際、全選択になるよう修正

2018/01/29  1.入力教科コンボのENTEXAM_PERFECT_MST参照をカット

2020/01/07  1.会場コンボに'全て'を追加

2020/01/10  1.受験生の表示切替ボタンの表示名称を変更

2020/09/10  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/03  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更
