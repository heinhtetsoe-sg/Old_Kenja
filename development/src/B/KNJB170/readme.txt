# kanji=漢字
# $Id: readme.txt 64773 2019-01-18 05:02:53Z matsushima $

2005.04.19 授業料ＣＳＶ出力(KNJB170) 新規作成

/*** 授業料電算ファイル(ＣＳＶ)生成画面 ***/

2006/04/12 alp o-naka 再作成。参照：授業料電算ファイルＣＳＶ作成画面仕様書20060406.xls
2006/04/14 alp o-naka NO001 科目数と合計単位数のＳＱＬを修正および仕様追加：メール2006/04/14 9:41

2009/03/12  1.CVSに登録されてなかったので登録

2009/03/17  1.単位固定と単位加算でカウントの仕方を分ける修正
            2.チェック情報出力追加
            3.生徒情報の出力の最後に授業料電算に渡すデータを追加

2009/04/14  1.チェック情報出力は科目コードの先頭2桁が90以下という条件を追加

2011/03/07  1.XLS出力処理追加
            -- プロパティー追加：useXLS(何かセットされていれば、XLS出力。基本'1'をセット。)

2011/03/18  1.テンプレートを1本に統一

2011/03/29  1.ボタン名称変更

2011/03/30  1.以下の修正をした
            -- 単独で呼び出された場合閉じる
            -- 親から呼ばれた場合は、親の権限を使用する

2011/04/01  1.テンプレートのパス修正

2011/04/07  1.固定文字「ＣＳＶ」をプロパティー「useXLS」で切り替えるよう修正した。

2012/04/12  1.プロパティー「useCurriculumcd」を追加した。
            -- '1'がセットされている場合は教育課程コード等を使うようにした。

2012/07/12  1.パラメータuseCurriculumcdを追加

2019/01/18  1.CSV出力の文字化け修正(Edge対応)