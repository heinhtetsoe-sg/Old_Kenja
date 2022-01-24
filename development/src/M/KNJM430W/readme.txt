# kanji=漢字
# $Id: 2db37031acf777baf85487668008aa9695c89149 $

2016/08/26  1.KNJM430Mを元に新規作成

2016/09/15  1.SUBCLASS_STD_PASS_SDIV_DAT.SEM_PASS_FLG を参照するように変更
            -- １の時、入力テキストを表示

2017/03/07  1.ADMIN_CONTROL_DATキー追加
            -- rep-admin_control_dat.sql(rev1.1)

2017/03/11  1.不要な処理カット

2020/05/12  1.プロパティ useRepStandarddateCourseDat = 1の場合
            -- REP_STANDARDDATE_COURSE_DATのテーブルを見るように修正

2020/09/29  1.プロパティ knjm430wCreateScoreNullRecord = 1の場合、受験資格のある生徒はレコードを作成する

2021/01/04  1.コード自動整形
            2.プロパティknjm430wSelectChaircd = 1の場合、科目選択を追加する
            3.プロパティknjm430wUseGakkiHyouka= 1の場合、学年評価入力欄を表示する
              プロパティknjm430wZenkiKamokuUpdateSeme1=1の場合、前期科目は学年評価入力欄を前期評価入力欄に変更する

2021/01/07  1.プロパティknjm430wUseGakkiHyouka= 1の場合、学期評価はRECORD_SCORE_HIST_DAT.VALUEを読込・更新する
