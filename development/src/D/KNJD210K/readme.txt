// kanji=漢字
// $Id: readme.txt 66283 2019-03-14 09:33:46Z yamashiro $

/*** KNJD210K 類型グループ平均計算処理 readme.txt***/

2004/08/10 新規リリース
2004/09/01 各学期・テスト毎の点数合計値、人数を保管<近大-作業依頼書20040820-01.doc>
2004/09/13 開発サーバでNull値のPHP変数セット時の値が違うに対応<近大-作業依頼書20040908-01.doc>
2004/11/04 平均値算出時のSQL文を修正、小数点以下の値を取り出せていなかった。<近大-作業依頼書20041102-02>
2004/11/19 ｢スポーツクラス等の固定類型評定について｣<近大-作業依頼書20041101-01>
2004/11/30 追試験処理追加に伴う、追試験対象者データ作成処理追加。
2004/12/01 1.固定評定処理において、更新時、学年の指定がもれていた。
           2.ALP開発用サーバの固定類型評定コードの値がNullではない、スペース文字がセットされていた為
            固定評定処理においてのAND TYPE_ASSES_CD is not nullの判定が正常に行えない。
            AND TYPE_ASSES_CD IN('A','B','C')へ変更。
2005/01/21 追試対象者抽出条件仕様変更追加<近大-作業依頼書20050117-02>
2005/02/17 ｢基準日追加、異動生徒計算対象外｣近大-作業依頼書20050214-02
2005/03/02 ｢科目読替処理｣近大-作業依頼書20050210-01
2005/03/04 ALP送付
2005/03/07 異動生徒は、追試験対象者にしない。
2005/03/07 8013：学年平均(総学用) 処理対象外
2005/03/07 追試対象者データ作成処理の実行を分けて実行可能にする。
2005/03/07 ALP送付
2005/03/09 科目読替、読替元での集計を変更、読替後の読替先の内容で集計する。近大-作業依頼書20050309-01
2005/03/09 ALP送付
2005/03/09 短期留学(３学期の開始終了日内に留学の開始終了日が含まれる生徒)の生徒は対象とする
2005/03/09 ALP送付
2005/03/10 3学期留学者対応
2005/03/10 科目読替処理(合併)の実行を分けて実行可能にする。
2005/07/14 読替え元データが揃っていない場合、評価1ではなく、NULLで作成するへ変更。
2006/02/04 alp o-naka NO001 学期コンボは、管理者コントロールを見る条件を追加。

2007/09/18 alp o-naka
★ ボタンの位置を変更した。
-- 「科目読替処理」と「類型平均算出」
★ ボタンの項目名の前に番号をつけた。
-- 「①科目読替処理」「②類型平均算出」「③追試験データ作成」
2007/09/20 alp o-naka
★ コメントを追加した。
-- 「補点・補充処理後、類型平均算出は行わないで下さい。」
★ 仮評定の処理を追加した。
-- 「科目読替処理」と「類型平均算出」において、
-- 第３学年で、１・２学期平均を実行する場合、学年平均も実行する
2007/09/25 alp o-naka
★ 「①科目読替処理」ボタン押した時、確認メツセージを表示するようにした。
-- 「事前準備：科目読替先の類型グループ設定されていますか？」
-- 科目読替先の類型グループ設定のし忘れを防ぐため
★ コメントを変更した。
-- 「補点・補充処理後は、科目読替処理のみ行って下さい。」
-- 「（類型平均算出は行わないで下さい。）」
2007/09/26 alp o-naka
★ 実行中、以下のテロップを画面下に表示するようにした。
-- 「処理しています...しばらくおまちください」
2007/10/19 alp o-naka
★ 下記の「仮評定の処理」を削除した。
-- 「科目読替処理」と「類型平均算出」において、
-- 第３学年で、１・２学期平均を実行する場合、学年平均も実行する
-- ※不要な処理と判断されたため
2008/10/14 alp o-naka
★ 追試験対象者データ削除ＳＱＬに年度の条件を追加した。

2012/07/19  1.教育課程の追加、追加に伴う修正
            -- Properties["useCurriculumcd"]=1のときのみ、教育課程処理に対応

2013/12/04  1.欠課時数によりA～Cパターンを０で更新する。
            2.修正

2013/12/05  1.欠課時数による更新に、読替科目の処理を追加

2013/12/10  1.欠課時数によりA～Cパターンを０で更新する。処理をカット

2014/06/26  1.科目合併処理変更(SUBCLASS_REPLACE_DAT → SUBCLASS_REPLACE_COMBINED_DAT)

2014/06/27  1.D015を参照し、評定１を含む/含まないの判断はせず。含むとする。

2014/07/10  1.整数でROUND（ROUND(FLOAT(), 0)）したが、FLOATを使用しているとダメ？なのでINTでキャストした。
            2.*100/100したので、ROUND(,0) → ROUND(,-2)に修正

2014/07/11  1.COMB_GCALC_DATが設定されていない場合は、平均として扱う。

2015/01/16  1.SUBCLASS_REPLACE_DAT → SUBCLASS_REPLACE_COMBINED_DATに変更

2016/05/20  1.RECORD_PROV_FLG_DATの更新処理追加

2016/07/07  1.課題研究(D065)の処理を追加
            -- RECORD_PROV_FLG_DATの更新処理追加
            2.課題研究(D065)の処理を修正
            -- 学期コンボに(8013)を追加。(8013)の時、課題研究(D065)の処理のみ実行。
            3.成績がある時、RECORD_PROV_FLG_DATのレコードを追加
            4.履歴に(8013)の表示を追加

2017/03/07  1.ADMIN_CONTROL_DATキー追加
            -- rep-admin_control_dat.sql(rev1.1)

2018/10/23  1.④補点・補充後の評定フラグ設定を追加

2019/03/14  1.KNJD500K/510Kからの呼び出しに対応
