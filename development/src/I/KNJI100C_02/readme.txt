# kanji=漢字
# $Id$

2011/05/25  1.KNJI100Aを元に新規作成

2012/05/10  1.index.phpの$ProgramIDを"KNJI100C"に変更した。

2013/08/12  1.DI_CD'19','20'ウイルス追加に伴う修正
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

2017/01/30  1.校種対応

2017/01/31  1.書き出し項目を保存する。

2017/02/10  1.サブシステムコンボボックスの表示を統一

2017/03/06  1.Z005の参照をログイン校種により切替える。

2017/05/15  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/11/06  1.項目一覧の表示順を修正
            2.年組コンボ、対象月コンボ変更後も書き出し項目一覧を保持する

2018/01/29  1.学年コンボ追加
            -- パラメータに学年追加
            2.レイアウト調整

2019/09/17  1.CSVのヘッダ部においてコードの前に対象項目の名称を記載するよう修正

2019/11/13  1.ラジオボタンの不具合対応
             - 「区分に関しての出力設定」の値を変更

2020/09/18  1.レイアウト修正
              -- 「selectdata_r」は左のリスト、「selectdata_l」は右のリストとなる
            2.課程学科、性別のコンボボックスを追加
            3.CSV書出し 複数の項目を固定で出力するよう修正
              -- 学籍番号, 生徒氏名, 性別区分, 学年, 組
            4.画面 項目一覧から固定項目を削除

2020/11/09  1.学年、クラスのコンボにブランクを追加。
            2.右のリストの初期表示を空に変更。
            3.右のリストの並び順を学年、クラス、出席番号、学籍番号の順に変更。
