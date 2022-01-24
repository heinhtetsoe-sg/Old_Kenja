# kanji=漢字
# $Id: readme.txt 76517 2020-09-07 08:35:18Z arakaki $
/*** KNJX231 個人別欠時数情報入力CSV取込(ATTEND_SUBCLASS_DAT) readme.txt***/

2007/05/19 s-yama   新規作成(東京都KNJX152参考)

2007/05/19 s-yama   対象月と指定日のエラーチェックを追加しました。
					データの出力順に科目と指定月を追加しました。（学籍番号カット）
					科目一覧コンボボックスを追加しました。

2007/05/23 s-yama   単独メニューとなるためフィールド取得を変更しました。

2011/04/11  1.XLS出力処理追加
            -- プロパティー追加：useXLS(何かセットされていれば、XLS出力。基本'1'をセット。)
            2.PROGRAMIDの記入ミス

2012/05/29  1.プロパティー「useCurriculumcd」を追加した。
            -- '1'がセットされている場合は教育課程コード等を使うようにした。
            2.科目コンボの表示でコードと名称の間にスペースを追加した。

2012/07/11  1.パラメータuseCurriculumcdを追加

2013/08/13  1.DI_CD'19','20'ウイルス追加に伴う修正
            -- rep-attend_semes_dat.sql(rev1.8)
            -- rep-attend_subclass_dat.sql(rev1.10)
            -- v_attend_semes_dat.sql(rev1.6)
            -- v_attend_subclass_dat.sql(rev1.3)
            
2013/08/14  1.DI_CD'19','20'ウイルス/DI_CD'25','26'交止追加に伴う修正
            -- rep-attend_semes_dat.sql(rev1.9)
            -- rep-attend_subclass_dat.sql(rev1.11)
            -- v_attend_semes_dat.sql(rev1.7)
            -- v_attend_subclass_dat.sql(rev1.4) 
            -- v_school_mst.sql(rev1.20)

2016/12/16  1.校種対応

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2020/09/07  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/08  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2021/03/20  1.CSVメッセージ統一(SG社)