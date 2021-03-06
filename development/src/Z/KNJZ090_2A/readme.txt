# kanji=漢字
# $Id: readme.txt 76434 2020-09-04 08:16:03Z arakaki $

2009/01/16  1.puma:/usr/local/deve_chiben/src/Z/KNJZ090_2をもとに新規作成。
            2.左フレームの学校名を3列目に移動

2009/02/19  1.ソースの整理
            2.学区コードを学校立コードに名称変更した。
            3.新たに学区コードを追加した。

2009/02/24  出身学校テーブルの校種のフィールド名変更に伴い修正した。

2009/03/11  1.CSV取込み機能追加

2009/03/12  1.エラー処理の修正(コードの有無をチェック)

2009/03/31  1.tokio:/usr/local/development/src/Z/KNJZ090_2Aからコピーした。
            2.追加、更新した際にスクロールバーを対象となった行へ移動させる処理を追加

2009/07/29  1.エラー出力時にエラーデータがなければ何も表示されないバグを修正

2009/12/07  1.「FINSCHOOL_DISTCD」の桁数修正(3桁 ⇒ 4桁)

2010/06/01  1.「ヘッダ出力(見本)」ラジオボタンを追加

2011/01/06  1.都道府県を追加した。

2011/08/11  1.以下の修正をした。
            -- エラー文言修正
            -- 電話番号、FAX番号の文字チェック追加

2012/04/20  1.校種を必須にした。
            2.エラーメッセージテーブルを最新にした。(W_CSVMSG_PRG_DAT)
            3.校種により登録テーブルを変更する。
            -- 4:FINHIGHSCHOOL_MST 以外:FINSCHOOL_MST

2012/05/10  1.校種を必須から外した
            2.校種により登録テーブルを変更をキャンセルした
            -- 修正前：4:FINHIGHSCHOOL_MST 以外:FINSCHOOL_MST
            -- 修正後：:FINSCHOOL_MST

2013/12/11  1.住所のサイズ変更等に伴う修正
            -- rep-finschool_mst_rev1.8.sql

2014/04/14  1.ラベル機能追加

2015/01/11  1.校種が0の場合にリストに表示されるように修正

2015/01/13  1.校種が0の場合に右で更新後に表示されるように修正

2017/03/02  1.CSVの取込、出力で校種対応

2017/09/12  1.統廃校フラグの追加
            2.文言変更「統廃校」→「統廃合」

2017/10/17  1.プロパティーuseFinschoolcdFieldSizeが12ならコードの0埋め、チェックを12桁でおこなう

2017/10/31  1.所在地コードコンボ追加

2018/01/11  1.ミライコンパス学校コードテキスト追加。プロパティー「useMiraicompass = 1」の時、表示する。
            --志願者CSV取込（KNJL014U）で参照しているmiraicompass出身学校コードと賢者出身学校コードとの対応表の更新処理を追加
            --更新テーブル：ENTEXAM_MIRAI_FS_REP_DAT

2018/05/18  1.固定文言変更。
            -- 学校立コード → 地区コード
            -- 学校種別 → 学校立コード

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2019/01/30  1.指導要録用「卒業」の印字判定チェックボックスを追加
            2.CSV取込、出力に「印字判定フラグ」の項目を追加
            3.FINSCHOOL_DETAIL_MST(SEQ = '003')の登録、更新処理を追加

2019/01/30  1.プロパティー「useLocationMst = 1」の時、所在地コードを表示するように修正。
            2.プロパティー「useLocationMst = 1」の時、CSV取込、出力に「所在地コード」を追加するように修正。
            3.プロパティー「useLocationMst = 1」の時、FINSCHOOL_DETAIL_MST(SEQ = '002')の登録、更新処理を行うように修正。
            4.プロパティー「useLocationMst = 1」以外の時、DBエラーで落ちていた障害の修正。
            -- プロパティー useLocationMstを追加

2019/02/04  1.CSV取込、出力の項目名「卒業印字フラグ」を「「卒業」印字しない」に変更
            2.上記項目の出力データを、「1」または空白となるように修正
            3.CSVの出力順を、学校コード順となるように修正
            4.CSV取込時の、上記項目のエラーチェックを追加

2019/06/21  1.2018/05/18の修正漏れ

2020/09/04  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/09  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2021/01/27  1.以下を修正
            -- 担当者コード、市区町村コードを追加
            -- CSVに担当者コード、担当者名、地区名、都道府県名、市区町村コード、市区町村名の項目を追加

2021/01/28  1.都道府県コンボを変更した場合に修正した内容が修正前の状態に戻る不具合の修正

2021/03/20  1.CSVメッセージ統一(SG社)