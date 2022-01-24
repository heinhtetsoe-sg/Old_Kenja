 kanji=漢字
# $Id$

2014/01/30  1.KNJD192Aを元に新規作成
            2.不要な処理をカット

2014/05/29  1.更新/削除等のログ取得機能を追加

2014/07/04  1.下方に文言を追加
            2.レイアウトの崩れを修正
            3.管理者コントロールのテストのみコンボに表示する

2014/07/05  1.コメント修正

2014/07/09  1.順位を出力しないチェックを追加

2014/10/08  1.最大科目数選択ラジオを追加

2015/04/27  1.平均・席次に学級・学科ラジオボタンを追加
            2.style修正

2015/07/28  1.テストコンボを学校種別参照に変更
            2.レイアウト変更
            3.style修正漏れ

2016/06/16  1.プロパティーuse_SchregNo_hyoji追加

2016/09/19  1.学年コンボ修正
            -- プロパティー「useSchool_KindField」とSCHOOLKINDを参照
            -- SCHREG_REGD_GDATを参照

2016/10/04  1.対象学年の「12未満」の条件をカット

2016/12/15  1.制限つきの条件に副担任を追加

2017/04/28  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/10/06  1.平均点を出力しないチェックを追加

2017/10/30  1.プロパティー「notUseAttendSubclassSpecial」追加

2017/11/22  1.「欠課数を出力しない」チェックボックスを追加
            2.プロパティー追加
            --knjd192vDefaultAttendDateDiv 出欠集計範囲デフォルト値
            --knjd192vDefaultGroupDiv 平均・席次
            --knjd192vDefaultOutputKijun 順位の基準点ラジオボタン
            --knjd192vDefaultNotPrintRank 順位を出力しない
            --knjd192vDefaultNotPrintAvg 平均点を出力しない
            --knjd192vDefaultNotPrintKekka 欠課数を出力しない

2018/02/19  1.「裁断用にソートして出力」チェックボックスを追加

2018/06/04  1.「選択科目を後に出力する」チェックボックスを追加

2018/06/15  1.「整数で表示する」チェックボックスを追加
            -- 欠課数換算が小数点ありの場合、表示する
            -- 「欠課数を出力しない」チェックボックスがoffのとき、使用可

2018/06/18  1.「裁断用にソートして出力」チェックボックス修正漏れ

2018/07/17  1.プロパティー「notUseKetten」追加

2019/03/01  1.最大科目数のデフォルト値をプロパティから取得するよう修正
            2.プロパティー追加
            --knjd192vDefaultSubclassMax 最大科目数
            3.プロパティー値変更
            --knjd192vDefaultGroupDiv 平均・席次 2:学級･コース
            --knjd192vDefaultOutputKijun 順位の基準点 2:平均点
            --knjd192vDefaultSubclassMax 順位を出力しない 0：チェックしない

2019/05/27  1.プロパティーknjd192vNotDefaultTestOnly追加 1:チェックしない

2019/08/13  1.プロパティーprintSubclassLastChairStd追加

2019/11/01  1.プロパティーuseSchoolMstSemesAssesscd追加

2019/12/05  1.プロパティーknjd192vPrintClass90追加

2020/11/09  1.プロパティーknjd192vDefaultSortCuttingが1の場合、裁断用にソートして出力するチェックボックスをデフォルトonとする

