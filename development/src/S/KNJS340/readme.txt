// kanji=漢字
// $Id: readme.txt 75072 2020-06-24 06:22:38Z ishimine $

2011/09/27  1.新規作成

2011/10/14  1.対象日のテキスト追加
              - 初期値に末日を表示する。
              - 対象月に学期の最終日があるとき、学期の最終日を表示する。
              - 対象日空文字チェック追加

2012/03/08  1.対象月の最終日の取得を修正

2013/08/12  1.プロパティーuseVirus、useKekkaJisu、useKekka、useLatedetail追加

2013/08/14  1.プロパティーuseKoudome追加

2015/06/10  1.プロパティー「useFi_Hrclass」が'1'の場合、法定クラス・複式クラスの選択を追加
            2.ラジオのonchangeをonclickに変更

2015/12/08  1.プロパティー「useSpecial_Support_Hrclass」が'1'の場合、法定クラス・複式クラスの選択を追加

2015/12/09  1.プロパティー「useSpecial_Support_Hrclass」が'1'の場合、法定クラス・実クラス・統計学級の選択に変更

2015/12/11  1.統計学級選択時の対象クラスを修正

2015/12/18  1.帳票選択ボタンを追加

2015/01/08  1.帳票選択のデフォルト値を変更
            2.15行印刷のチェックボックスを追加

2015/02/05  1.特別支援は15行をデフォルトに変更

2016/02/16  1.プロパティー「useFi_Hrclass」が'1'の場合を修正
            -- クラス方式選択ラジオボタンをカット
            -- "HR_CLASS_TYPE"に固定で"2"をセット
            -- 固定文言「対象クラス」を「複式クラス」に変更

2016/03/12  1.2/16修正をカット
            2.プロパティーのuseFi_Hrclass=1　の時は、初期値は、複式クラスとする。

2016/07/27  1.プロパティー「useSpecial_Support_Hrclass」が'1'の場合、統計学級選択のラジオをカットし学年混合のチェックを追加

2016/12/19  1.校種対応

2017/01/12  1.15行で印刷チェックボックスのPHP5.6対応漏れ

2017/02/28  1.対象月の初期値指定を追加

2017/03/06  1.Z005の参照をログイン校種により切替える。

2017/04/25  1.対象月の年月が空の場合の警告対応

2017/04/26  1.ADMIN_CONTROL_PRG_SCHOOLKIND_MSTを使って、校種を制御する。
            --プロパティー追加：use_prg_schoolkind

2017/05/10  1.プロパティーのuseFi_Hrclass=1　の時は、初期値は、法定クラスとする。(2016/03/12-2の修正をカット)

2018/03/23  1.帳票選択のラジオボタンとチェックボックスの行を入れ替え
            2.帳票選択のラジオボタンに"Cパターン"を追加し、"Cパターン"選択時はチェックボックスを非活性にする処理を追加

2018/03/26  1.帳票選択のラジオボタンの"Cパターン"を"A4横(出席統計有)"に変更

2018/05/21  1.帳票選択のラジオボタンの"A4横(出席統計有)"を"A3縦/A4横(出席統計有)"に変更
            2."15行で印刷"のチェックボックス非活性、及びチェック外し処理を削除

2018/05/28  1.未在籍者は詰めて印字するチェックボックスを追加

2019/02/14  1.帳票選択のラジオボタンに"A4横2(出席統計有)"を追加
            2.表示領域の調整
            3."A4横2(出席統計有)"の際に利用するHIDDEN情報の追加

2019/04/22  1.A4横2の文言「出席統計有」を「連続欠席調査」に変更

2019/04/23  1.指示画面の幅変更

2019/05/16  1.指示画面の幅変更(固定)

2019/09/04  1.帳票選択のラジオボタンに"A4横3(連続欠席調査)"を追加
            --プロパティー「useEventAbbv」が'1'の場合

2019/12/02  1.プロパティーknjs340FORM_DIVが設定されている場合、設定された帳票選択のみを表示

2020/01/17  1.PC-Talker 機能追加

2020/06/24  1.プロパティー「knjs340FORM_DIV」が'6'の時、A4横4パターンの帳票タイプを選択するように変更
