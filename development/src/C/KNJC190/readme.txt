# kanji=漢字
# $Id: readme.txt 65642 2019-02-12 06:09:51Z kawata $

2010/02/09  1.tokio:/usr/local/development/src/C/KNJC190 からコピー
            2.ラジオボタンにラベル機能を追加した。

2011/10/18  1.対象年度のSCH_STF_DATのデータを表示するように修正

2011/10/24  以下を修正
            1.時間割にあって、職員マスタにない職員は、職員名はnullで表示される。
            2.職員年度データにない職員名が表示されている。

2012/01/25  1.教育課程の追加、追加に伴う修正
            -- Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応
            
2012/03/08  1.開始日の日付の初期値をログイン学期の開始日とする

2014/04/30  1.職員コードを表示しないパラメータ追加
              - Properties["notShowStaffcd"]=1のとき、職員コードを表示しない
              
2014/05/29  1.更新/削除等のログ取得機能を追加

2014/08/27  1.style指定修正

2016/09/21  1.校種条件追加
            -- プロパティー「useSchool_KindField」とSCHOOLKINDを参照

2017/05/19  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/05/19  1.SCHOOLKINDの空白チェックを追加

