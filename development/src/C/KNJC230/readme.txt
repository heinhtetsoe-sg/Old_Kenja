$Id: readme.txt 56769 2017-10-26 01:44:51Z tawada $

2011/10/11  1.新規作成(KNJF150_2を元に作成)

2012/03/16  1.フィールド追加（DI_REMARK_CD）に伴う更新処理を追加
                - リストから選択した名称マスタC900のNAMECD2をDI_REMARK_CDに追加する
                - その他のテキスト内容入力時はDI_REMARK_CDはNULLとする

2014/08/27  1.style指定修正

2015/04/20  1.生徒リストtoリストの項目名を変更

2015/12/03  1.更新権限での制御

2015/12/04  1.文言修正

2016/07/21  1.生徒リストtoリストで右に生徒を移動したときのソート順を修正
            2.生徒リストtoリスト内の印字位置を調整
            3.レイアウト修正

2016/09/21  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照 

2017/09/11  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/10/03  1.ATTEND_DI_CD_DAT移行
            -- attend_di_cd_dat.sql(1.2)

2017/10/26  1.校種コンボ追加、プロパティー「use_prg_schoolkind」が"1"の時

2021/04/14  1.リファクタリング
            2.SQLの不具合修正
