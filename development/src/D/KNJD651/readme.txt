// kanji=漢字
// $Id: readme.txt 56586 2017-10-22 12:52:35Z maeshiro $

2010/03/19  1.新規作成

2010/04/26  1.Model内で$thisを引数に渡すのをやめた。

2010/05/08  1.レイアウトの修正、CSVのボタン追加
            2.コピペの機能追加
            3.CSVのボタンを隠した

2010/08/13  1.「貼付け」機能の修正(キャンセルで普通の貼付け)

2012/01/16  1.教育課程の追加、追加に伴う修正
               - プロパティーuseCurriculumcd=1のときのみ、教育課程処理に対応

2012/05/29  1.CSV処理プログラムの廃番に伴い、CSVボタンをカットした。

2013/06/21  1.出欠備考参照ボタンの参照先を切替処理追加
                - Properties["useAttendSemesRemarkDat"]の値により切替を行う
                
2013/06/25  1.出欠備考参照ボタンの表示名を修正
                - Properties["useAttendSemesRemarkDat"] = 1の時、まとめ出欠備考参照
                - それ以外、日々出欠備考参照

2014/04/21  1.貼り付け機能の修正
                - 関数名を修正 show ⇒ showPaste