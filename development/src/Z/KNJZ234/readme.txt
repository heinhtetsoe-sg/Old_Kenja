# kanji=漢字
# $Id: readme.txt 56591 2017-10-22 13:04:39Z maeshiro $

2007/08/16  新規作成(処理：東京都KNJH333　レイアウト：東京都KNJH333参考)

2007/08/20  今年度、今学期表示を追加。
            コピー処理のメッセージをMSG102 → MSG101に変更。
            テーブル変更に伴う修正。

2007/08/21  学期表示を追加。

2011/12/22  1.教育課程の追加、追加に伴う修正
                - Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応

2015/02/25  1.リストTOリストのIE10以上の対応
                - IE10以上ではnameタグはdocument.getElementsByID()で取得不可能のため、document.forms[0]でnameタグを取得
