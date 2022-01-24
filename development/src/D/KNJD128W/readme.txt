// kanji=漢字
// $Id: readme.txt 76268 2020-08-27 08:56:53Z arakaki $

2017/05/12  1.新規作成
            2.制限無し更新権限は、出力データ制限なし、取込機能なし。

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2019/05/30  1.更新可制限付のみ取込機能ありに変更

2019/08/14  1.校種コンボ追加

2019/09/17  1.「useSchool_KindField」プロパティが1の時にコンボを表示するよう修正
            2. use_prg_schoolkindに対応

2020/08/27  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/01  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2021/01/15  1.CSVメッセージ統一(SG社)