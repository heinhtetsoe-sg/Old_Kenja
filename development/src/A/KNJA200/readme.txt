# kanji=漢字
# $Id: readme.txt 69989 2019-10-02 07:39:10Z yogi $

2009/11/16  1.tokio:/usr/local/development/src/A/KNJA200からコピーした。
            2.チェックボックスにラベル機能を追加した。

2010/07/28  1.フォーム選択ラジオを追加。

2015/02/18  1.リファクタリング
            2.出身学校を出力するを追加

2016/09/19  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照 

2017/04/28  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/12/29  1.チェックボックス・ラジオボタンの初期値設定機能追加
            -- common.js
            -- Model.php
            -- prg_default_value_dat.sql(rev.57783)

2018/02/23  1.フォーム選択に「8列×6行」を追加

2019/10/02  1.出身学校の出力を、出身学校/ふりがなで選択できるよう、変更

2021/02/18  1.リファクタリング
            2.法定クラス、実クラスラジオ、及び、学年混合チェックボックスを追加
            3.上記に合わせて、対象クラスコンボボックスが変わるよう、処理を変更
            -- dispMTokuHouJituGrdMixChkRadプロパティを追加
            -- 下記プロパティを参照
               useSpecial_Support_Hrclass,useFi_Hrclass,use_finSchool_teNyuryoku_P
