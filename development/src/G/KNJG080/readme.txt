# kanji=漢字
# $Id: readme.txt 56587 2017-10-22 12:54:51Z maeshiro $

2010/04/20  1.KNJZ176を元に新規作成

2010/04/28  1.制限付きに副担任も対象になるよう修正

2010/08/11  1.詳細設定を追加した。
            2.通称名出力フラグが"1"の場合、名前の前に「[通]」を出力する。
            3.取消ボタンが有効となるよう修正した。
            4.リストのサイズを大きくした。

2011/11/07  1.職員ボタンを追加し、各ボタンの動作を追加
              - 職員ボタン選択時に年度ボタンの表示（コンボの年度はSCHOOL_MSTのYEAR）
              - 職員ボタン選択時に一覧名の表示を変更
                    ・生徒対象者一覧 ⇒ 職員対象者一覧
                    ・生徒一覧 ⇒ 職員一覧
              - 職員ボタン選択時の更新ボタン押下時は職員対象者一覧の内容をSTAFF_NAME_SETUP_DATに更新
              - 職員ボタン選択時の詳細ボタン押下時は職員対象者一覧を表示し、通称名印字の有無を設定

2012/05/24  1.生徒の詳細画面にて、保護者の通称名印字チェックボックス追加
            2.保護者用のテーブル（GUARDIAN_NAME_SETUP_DAT）テーブル作成の為、元に戻す
              - 生徒の詳細画面にて、保護者の通称名印字チェックボックスをカット
            3.保護者ボタンを追加、各ボタンの動作を追加
              - 保護者ボタン選択時の更新ボタン押下時は生徒対象者一覧の内容をGUARDIAN_NAME_SETUP_DATに更新
              - 保護者ボタン選択時の詳細ボタン押下時は生徒対象者一覧を表示し、通称名印字の有無を設定

2012/05/25  1.保護者ボタン選択時の詳細ボタン押下時に保護者氏名を出力するよう修正
            2.コンボの位置を修正

2014/08/11  1.ログ取得機能追加
            2.ラベル機能追加
            3.style指定修正

2016/09/20  1.年組コンボ、年度コンボ、生徒一覧等修正
            -- プロパティー「useSchool_KindField」とSCHOOLKINDの参照

2016/12/21  1.職員番号マスク機能追加
            -- プロパティー「showMaskStaffCd」追加
            -- showMaskStaffCd = 4 | *
            -- この設定だと、下４桁以外は「*」でマスク

2016/12/22  1.プロパティー「showMaskStaffCd」が無いときは通常表示になるよう修正

2017/03/23  1.LABEL変更 "GRADE年 HR_CLASS組 ATTENDNO番" → "HR_NAME ATTENDNO番"

2017/05/30  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/07/20  1.エラーチェック修正
