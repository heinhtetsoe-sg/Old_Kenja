// kanji=漢字
// $Id: readme.txt,v 1.8 2013/05/24 08:35:37 m-yama Exp $

成績入力（熊本・前期３回／後期２回）

2011/01/05  1.KNJD122Sを元に新規作成。

2011/10/27  1.「参照可能」「参照可能・制限付」の時、更新ボタンを押したら、メッセージを表示する。
            -- 例：この処理は許可されていません。("MSG300")

2012/01/30  1.読み込み中は、更新ボタンをグレー（押せないよう）にする。

2012/02/14  1.スペースチェックを追加
            -- スペース文字があればエラーメッセージを表示する。
            -- 例：入力された値は不正です。「スペース」が混ざっています。

2012/03/13  1.スペースチェックをカット。
            -- スペース文字があればスペース文字を削除する。

2012/05/25  1.教育課程の追加、追加に伴う修正
                - Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応

2013/03/15  1.プロパティ追加に伴う修正
            -- ・useAssessSubclassMst
            -- 1の場合、科目別評定マスタを参照し、なければ評定マスタを参照する。
            -- 1以外の場合、評定マスタを参照する。

2013/05/24  1.得点欄の変更があった場合、終了時にメッセージを表示する。

2014/02/27  1.更新時、サブミットする項目使用不可　※本番機直接修正のため、使い捨て
