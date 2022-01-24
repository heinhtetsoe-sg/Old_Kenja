# kanji=漢字
# $Id: readme.txt 75513 2020-07-18 00:03:08Z maeshiro $

2015/07/14  1.KNJD617Gを元に新規作成
            2.平均点・順位ラジオボタンのコースグループをカット

2015/07/28  1.テストコンボを学校種別参照に変更
            2.レイアウト変更

2016/09/19  1.学年コンボ修正
            -- プロパティー「useSchool_KindField」とSCHOOLKINDを参照

2016/10/12  1.学年12未満の条件をカット

2016/11/24  1.プロパティーuse_school_detail_gcm_datが1の場合、テーブルTESTITEM_MST_COUNTFLG_NEW_GCM_SDIVを使用する

2017/03/03  1.SQL変更

2017/09/14  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/11/24  1.プロパティーknjd617vDefaultGroupDiv追加

2018/05/24  1.順位の基準点に平均点の指定を追加

2018/10/26  1.useLc_Hrclassプロパティを帳票側に渡すHIDDENを追加
            --プロパティー参照：useLc_Hrclass

2019/04/04  1.山村学園の時、CSV出力ボタン表示

2019/07/17  1.学期評価のCSV出力時にDBエラーが発生する障害の修正

2020/07/18  1.knjd617vSelectSubclassGroupが1の場合、平均点・順位のコースに科目グループの選択を追加する
