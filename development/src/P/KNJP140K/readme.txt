# kanji=漢字
# $Id: readme.txt 56580 2017-10-22 12:35:29Z maeshiro $

/*** knjp140k授業料分納・延納入力readme.txt***/
2005/04/25 納品
2005/04/28 HRリストのクラスコンボ変更時の画面初期化必要　対応済
2005/05/31 KNJXEXPK_生徒情報参照画面(校納金用共通部品)呼出変更
2005/05/31 PGID変更
2005/12/14 詳細編集画面(分納の期限と金額編集)表示時、編集対象データ未選択状態で編集画面が表示されるパターンへの対応。

2006/02/22 ・NO001 alp m-yama 交付情報表示を追加
2006/03/22 ・NO002 alp m-yama 金額チェックを追加
2006/04/14 ・NO003 alp m-yama 金額データの取得がおかしかったので修正。(原因不明?)
							  <現象>
							  model->moneyにPHP1でセットするが、PHP4に移動する辺りで
							  ↑の値がクリア？されてしまう。為、金額チェックでエラーとなってしまう。
2006/04/25 ・NO004 alp m-yama 名称マスタ、Z006→G212に変更
2006/08/22  m-yama   テーブル変更に伴う修正(EXPENSE_S_MST/MONEY_DUE_S_DAT)。
