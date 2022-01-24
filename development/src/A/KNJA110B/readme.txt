# kanji=漢字
# $Id: readme.txt 69336 2019-08-22 11:07:09Z ishii $

2014/10/03  1.KNJA110A(1.58)を元に作成

2014/10/07  1.プロパティ名を修正
            -- usrSpecial_Support_School
            -- ↓
            -- useSpecial_Support_School

2014/10/20  1.プロパティ名を変更
            -- useSpecial_Support_School
            -- ↓
            -- useSpecial_Support_Hrclass

2014/12/18  1.検索条件追加に伴い、学校検索の呼び出し時のウインドウサイズを変更

2015/04/10  1.削除ボタンをカット

2015/05/18  1.FI複式クラスを使うためのプロパティuseFi_Hrclassに対応

2015/05/22  1.その他（身体状態）をSCHREG_BASE_YEAR_DETAIL_MST(BASE_SEQ='004')にも更新するよう修正
                - SCHREG_BASE_MSTのその他（身体状態）は常に最新データを保持するよう修正
            2.プロパティuseFi_Hrclass='1'の時、個人情報の許諾を追加(SCHREG_BASE_DETAIL_MSTのBASE_SEQ='006')
            
2015/06/04  1.JavascriptのgetYear()メソッドをgetFullYear()メソッドに変更

2015/06/11  1.複式年組または番号が変更されたら、サブミットするよう修正

2015/06/18  1.入学卒業履歴修正画面で、小学校の時は学校名をNYUGAKUMAE_SYUSSIN_JOUHOUより表示するよう修正

2015/11/27  1.仕様変更による個人情報の許諾の項目をカット(SCHREG_BASE_DETAIL_MSTのBASE_SEQ='006')

2015/12/10  1.プロパティ「useSpecial_Support_Hrclass」= '1'のとき
            -- 文言「複式クラス」→「実クラス」に変更

2016/02/12  1.幼稚園の時は学校名をNYUGAKUMAE_SYUSSIN_JOUHOUより表示するよう修正
            2.一括更新画面も小学校・幼稚園の時は学校名をNYUGAKUMAE_SYUSSIN_JOUHOUより表示するよう修正

2016/03/09  1.幼稚園の入園、卒園とする。生活管理ボタン追加(F323)

2016/04/20  1.KNJA110A(rev1.75)との差分を追加・修正
            -- 一括更新画面で、更新されない不具合修正
            -- 氏名等履歴の予約機能追加
            -- プロパティー「useAddrField2」だけではなく、SCHOOL_KIND='H'の時は、ENT_ADDR2およびGRD_ADDR2を表示する
            --    ただし、項目名は課程・学科等とする
            -- 転学先学年を追加
            -- 多数のバグ修正
            -- 履歴画面の課程学科フラグを課程フラグ、学科フラグに分けた。
            -- getStudent_data_beforeの引数が足りなかったので修正
            -- 顔写真の表示/非表示機能(useDispUnDispPicture)
            -- 備考1,2,3の入力文字の制限以降は入力できないよう修正

2016/09/16  1.課程学科コンボ、コースコンボに空行追加

2016/10/26  1.プロパティ「useSpecial_Support_Hrclass」が'1'のとき、訪問生チェックボックス追加

2017/02/23  1.php新環境でエラーCall-time pass-by-referenceが表示される不具合修正

2017/10/16  1.プロパティーuseFinschoolcdFieldSizeが12ならコードの0埋め、チェックを12桁でおこなう

2018/03/22  1.フィールド追加に伴う修正
            -- SCHREG_BASE_HIST_DAT.HANDICAP, SCHREG_BASE_HIST_DAT.HANDICAP_FLG
            -- rep-schreg_base_hist_dat.sql(rev.59114)

2018/03/27  1.実クラス-番の※をカット

2018/05/18  1.幼稚園の時、生活管理ボタンを表示していたが常に表示する。

2019/08/22  1.左画面の校種コンボ用パラメーター追加(URL_SCHOOLKIND)
