# kanji=漢字
# $Id: e97e57f01088601225255f3e4d43786b38236743 $

2009/06/24  新規作成

2009/06/25  1.サーブレットＵＲＬを修正（KNJC ⇒ KNJF）。

2013/05/01  1.印影出力のチェックボックスを追加。
            2.パラメータDOCUMENTROOTを追加。

2013/08/12  1.プロパティーuseVirus、useKekkaJisu、useKekka、useLatedetail追加

2013/08/14  1.プロパティーuseKoudome追加

2014/08/07  1.ログ取得機能追加

2016/09/20  1.プロパティー「useSchool_KindField」とSCHOOLKINDを参照 

2016/09/29  1.プロパティー「useNurseoffAttend」が'1'の場合、「欠席者一覧も印刷する。」チェックボックスを表示しない

2017/02/27  1.以下の修正。
            --名称マスタ'F012'にデータがある時、印影出力チェック欄にデータを表示。
            --プロパティー「useFormNameF175」追加。

2017/03/13  1.印影出力項目を「V_NAME_MST」→「PRG_STAMP_DAT」に変更

2017/03/23  1.PRG_STAMP_DATの参照の際、useSchool_KindFieldで校種参照をする/しないを判定

2017/05/18  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/09/14  1.前回の修正漏れ

2019/02/06  1.特別支援学校プロパティ(useSpecial_Support_School)が"1"の場合、校種コンボを非表示にするよう、変更
            --プロパティー参照：useSpecial_Support_School

2019/04/08  1.プロパティーknjf175PrintSchoolKind追加

2020/02/12  1.パラメータ不具合を修正

2020/09/14  1.KNJF175_NoUse_NurseOffDiaryプロパティが"1"の場合、出力範囲の下段に”休業日も出力する”の
              メッセージを出力するよう、変更
              --プロパティー追加：KNJF175_NoUse_NurseOffDiary
            2.KNJF175_NoUse_NurseOffDiaryプロパティを帳票側に引き継ぐよう、変更

2021/03/04  1.熊本の場合は、印影チェックボックスの設定が６つ以上あっても、
              ５つまでしか表示しないように変更

2021/03/18  1.CASE WHEN ～ THEN TRUE ～ END というように、SQLのCASE文でbooleanを返そうとすると、
              DB2のver10以上だとエラーが発生するため、SQLではなくPHPで判定するように修正

2021/03/19  1.リファクタリング