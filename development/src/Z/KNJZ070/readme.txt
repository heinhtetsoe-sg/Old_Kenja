# kanji=漢字
# $Id: readme.txt 74216 2020-05-12 05:46:40Z ishii $

2011/12/26  1.tokio:/usr/local/development/src/Z/KNJZ070からコピーした。
            2.教育課程の追加、追加に伴う修正
               - Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応
               
2013/04/19  1.リストTOリストの並びを学校校種 + 教育課程コード + 教科コード + 科目コードの並びに修正

2013/07/04  1.詳細登録ボタン追加
                - 追加に伴い、同一フォルダ内にsel.htmlを新規作成し、画面表示するようにした

2013/07/29  1.詳細登録ボタンをプロパティuseClassDetailDat = 1の時のみ、表示するよう修正

2013/08/01  1.詳細登録ボタンをプロパティkari_useMiyagiTokiwa = 1の時も表示するよう修正

2014/08/08  1.詳細登録ボタンのパラメータを追加

2014/08/13  1.詳細登録ボタンに権限のパラメータを追加
            2.詳細登録ボタンの表示プロパティ作成により下記を変更
              - useClassDetailDat ⇒ hyoujiClassDetailDat

2014/08/15  1.詳細登録ボタンの表示にプロパティuseCurriculumcdも参照するよう修正

2014/11/14  1.ログ取得機能追加

2015/03/30  1.詳細登録ボタンの表示条件に「useClassDetailDat = 1」を追加

2016/09/22  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照 

2017/09/01  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/09/06  1.前回の修正漏れ

2018/05/10  1.useCurriculumcd、useClassDetailDat、hyoujiClassDetailDatが1で
              年度追加したときSUBCLASS_DETAIL_DATも年度コピーする

2020/05/12  1.YDATから科目を除く更新時にその科目を参照しているテーブルが存在する場合はエラーを出力するよう修正

2021/03/24  1.リファクタリング
            2.テーブルにデータが存在するかのチェックに年度条件を追加、SUBCLASS_DETAIL_DATの削除処理追加
