# kanji=漢字
# $Id: readme.txt 60446 2018-06-01 10:04:54Z yamauchi $

2009/11/30  1.tokio:/usr/local/development/src/C/KNJC053からコピーした。
            2.「注意」、「超過」のラジオボタン追加

2010/02/09  1.ラジオボタン、チェックボックスにラベル機能を追加した。

2010/03/30  1.プロパティーchikokuHyoujiFlgを追加した。
            2.改行処理を修正した。

2010/06/16  1.プロパティーファイルの参照方法を共通関数を使うよう修正

2010/09/21  1.法定授業とか、実授業とかの区別はなく、以下の通りに変更。
            -- 欠課数上限値（履修／修得）
            -- 　◎注意　　　　○超過

2011/03/06  1.プロパティーuseTestCountflgを追加した。

2012/01/20  1.教育課程の追加、追加に伴う修正
            -- Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応
            
2012/03/16  1.教育課程対応のHiddenの修正

2013/01/12  1.教科の教育課程CD削除

2013/08/12  1.プロパティーuseVirus、useKekkaJisu、useKekka、useLatedetail追加

2013/08/14  1.プロパティーuseKoudome追加

2014/02/02  1.プロパティーuseTestCountflg追加

2016/06/16  1.プロパティーuse_SchregNo_hyoji追加

2018/06/01  1.SQLから校時の条件をカット

2021/03/02  1.リファクタリング（至急の為、fixerのリファクタのみ）
            2.パラメーター追加。useSchool_KindField

2021/04/23  1.プロパティーknjc053useMeisaiExecutedate追加
