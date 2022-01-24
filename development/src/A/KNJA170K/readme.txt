# kanji=漢字
# $Id: readme.txt 56585 2017-10-22 12:47:53Z maeshiro $

2009/10/28  1.KNJA170を元に新規作成
            2.近大のKNJA170を元に作り直し

2014/05/21  1.ラベル機能追加
            2.リファクタリング
            
2015/12/22  1.リストTOリストの生徒氏名はNAME_SHOWより表示するよう修正

2016/09/19  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照 

2017/04/27  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2021/02/18  1.リファクタリング
            2.印刷指定に実クラス、学年混合を追加
            3.上記に合わせて、クラス一覧が変わるよう、処理を変更
            -- dispMTokuHouJituGrdMixChkRadプロパティを追加
            -- 下記プロパティを参照
               useSpecial_Support_Hrclass,useFi_Hrclass,use_finSchool_teNyuryoku_P
