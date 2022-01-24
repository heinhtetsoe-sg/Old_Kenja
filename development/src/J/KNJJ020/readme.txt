# kanji=漢字
# $Id: readme.txt 72679 2020-03-03 04:39:41Z gushiken $

2014/06/02  1.tokio:/usr/local/development/src/J/KNJJ020からコピー
            2.ログ取得機能追加

2016/09/21  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照 

2016/12/22  1.職員番号マスク機能追加
            -- プロパティー「showMaskStaffCd」追加
            -- showMaskStaffCd = 4 | *
            -- この設定だと、下４桁以外は「*」でマスク
            -- 「showMaskStaffCd」が無いときは通常表示

2017/05/23  1.リファクタリング
            2.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/09/14  1.前回の修正漏れ

2018/10/03  1.年度を自由に入力できるように修正

2018/10/11  1.部活動複数校種設定対応
            -- プロパティー「useClubMultiSchoolKind」参照

2020/03/03  1.クラブコンボに「－全て－」を追加
             -- 「全て」を選択した時は、下の職員一覧リストtoリストを非表示
             -- 「更新」で全クラブの顧問を新年度に追加する（年度データにない先生は、コピーしない）

2021/03/10  1.京都PHPバージョンアップ対応