# kanji=漢字
# $Id: readme.txt 57106 2017-11-15 05:53:47Z nakamoto $

科目合併・評定自動計算実行（新賢者版）

2009/08/04  1.KNJD219を元に新規作成。

2011/10/07  1.パターン３の対応。
            -- 合併元科目(単位加算)の[評定=1(履修のみ)]の扱いについて、
            -- 現状パターン１、２に加え、パターン３を対応するために以下の通り修正した。
            -- 修正前：両方とも[予備１]を参照。
            -- 修正後：単位計算は[予備１]、評定計算は[予備２]を参照に変更。

2012/07/04  1.教育課程の追加、追加に伴う修正
            -- Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応

2013/07/05  1.名称マスタのマスタ化に伴う修正

2014/05/30  1.更新/削除等のログ取得機能を追加

2015/08/20  1.仕様追加
            -- パータタイプの学校（TESTITEM_MST_COUNTFLG_NEW_SDIV使用）は、
            -- RECORD_SCORE_DATの学年評定は、SCORE_DIV'09'のSCOREに登録する。
            ※ プロパティuseTestCountflg参照

2016/09/19  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照 

2017/09/08  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/11/10  1.前回の修正。校種コンボ追加。参考：KNJZ219
            --ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind
            2.履歴のスクロール表示修正

2017/11/15  1.校種対応修正漏れ
            --SCHOOL_MSTのSQL条件
