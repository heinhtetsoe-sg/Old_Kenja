# kanji=漢字
# $Id: readme.txt 76435 2020-09-04 08:16:46Z arakaki $

2016/09/30  1.新規作成

2016/10/24  1.権限不具合修正

2016/11/22  1.教室の表示を追加

2017/07/14  1.IE11対応。availheight → availHeight

2018/04/13  1.CSV機能追加

2018/07/02  1.左画面に塾コード、塾名、検索ボタンを追加
            2.元に戻した（前回修正カット）

2018/07/25  1.ミライコンパス塾コードテキスト追加。プロパティー「useMiraicompass = 1」の時、表示する。
            --志願者データ生成処理（KNJL515H）で参照しているmiraicompass塾コードと賢者塾コードとの対応表の更新処理を追加
            --更新テーブル：ENTEXAM_MIRAI_PS_REP_DAT

2018/07/30  1.CSV取込で塾の教室データの列が設定されていない場合、塾教室データを追加・更新しない

2018/08/09  1.取込のファイルサイズを1M → 5M、取込み件数を5000 → 10000に変更した

2019/01/18  1.CSV出力の文字化け修正(Edge対応)

2020/09/04  1.CSV DUMMYの名称をLASTCOLUMNに修正

2020/12/09  1.リファクタリング
            2.CSVの最終列をDUMMYかLASTCOLUMNで扱うかを
              設定ファイル「prgInfo.properties」のプロパティー「csv_LastColumn」で
              切り替えるように変更

2021/01/27  1.リファクタリング
            2.以下を修正
            -- 担当者コード、都道府県コード、市区町村コードを追加
            -- CSVに担当者コード、担当者名、地区名、都道府県コード、都道府県名、市区町村コード、市区町村名の項目を追加
            -- 塾マスタの地区コードで参照する名称マスタのコード「Z003」を「Z060」へ変更

2021/01/28  1.画面表示速度改善のため一覧表示の処理を修正

2021/03/20  1.CSVメッセージ統一(SG社)