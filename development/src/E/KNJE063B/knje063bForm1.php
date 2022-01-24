<?php

require_once('for_php7.php');

class knje063bform1
{

    public function main(&$model)
    {

        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knje063bindex.php", "", "right_list");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報表示
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = htmlspecialchars($model->name);
        $arg["ENT_DIV"]  = $db->getOne(knje063bQuery::selectEnt($model->schregno));
        if ($arg["SCHREGNO"]) {
            $arg["HANDICAP"] = "(知的)";
        }

        /* Add by HPA for title start 2020/02/03 */
        $htmlTitle = "\"".$arg["SCHREGNO"]."".$arg["NAME"]."の情表画面\"";
        echo "<script>
        var title= $htmlTitle;
        </script>";
        /* Add by HPA for title end 2020/02/20*/

        //ALLチェック
        /* Edit by HPA for PC-talker 読み start 2020/02/03 */
        $extra = "aria-label = \"全てを選択\" onClick=\"return check_all(this);\"";
        /* Edit by HPA for PC-talker 読み end 2020/02/20 */
        $arg["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", $extra, "");

        //ソート順
        $order[1] = "▲";
        $order[-1] = "▼";

        //項目名表示（年度）
        /* Edit by HPA for PC-talker 読み start 2020/02/03 */
        $setOrder = $order[$model->sort["YEAR"]];
        $arg["IDYEAR"] = $setOrder == "▲" ? "sortAscYear" : "sortDesYear";
        $arg["LABELYEAR"] = $setOrder == "▲" ? "リストを年度によって昇順で並べ替えました" : "リストを年度によって降順で並べ替えました";
        $arg["YEAR"] = View::alink(
            "knje063bindex.php",
            "<font color=\"white\">年度</font>",
            "onclick=\"current_cursor('{$arg["IDYEAR"]}');\" id =\"{$arg["IDYEAR"]}\"",
            array("cmd"=>"sort", "sort"=>"YEAR")
        ) .$order[$model->sort["YEAR"]];

        //項目名表示（科目名）
        $setOrder = $order[$model->sort["SUBCLASSCD"]];
        $arg["IDNAME"] = $setOrder == "▲" ? "sortAscName" : "sortDesName";
        $arg["LABELNAME"] = $setOrder == "▲" ? "リストを科目名によって昇順で並べ替えました" : "リストを科目名によって降順で並べ替えました";
        $arg["SUBCLASSCD"] = View::alink(
            "knje063bindex.php",
            "<font color=\"white\">科目名</font>",
            "onclick=\"current_cursor('{$arg["IDNAME"]}');\" id =\"{$arg["IDNAME"]}\"",
            array("cmd"=>"sort", "sort"=>"SUBCLASSCD")
        ) .$order[$model->sort["SUBCLASSCD"]];
        /* Edit by HPA for PC-talker 読み end 2020/02/20 */

        //学校校種を取得
        $query = knje063bQuery::getSchoolKind($model);
        $getSchoolKind = $db->getOne($query);
        //指導要録文言評定データ表示
        if ($model->schregno) {
            $result = $db->query(knje063bQuery::selectQuery($model, "", $getSchoolKind));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //チェックボックス
                $extra = $this->getShomeiCheck($db, $row["YEAR"], $model->schregno, $model->Properties["useSeitoSidoYorokuShomeiKinou"]);
                /* Add by HPA for PC-talker 読み start 2020/02/03 */
                $extra .= "aria-label = \"削除". $row["YEAR"] ."年度". $row["GRADE"]."学年".$row["CLASSNAME"]."\"";
                /* Add by HPA for PC-talker 読み end 2020/02/20 */
                $row["CHECKED"] = knjCreateCheckBox($objForm, "CHECKED", $row["YEAR"].",".$row["SUBCLASSCD"], $extra, "1");
                $arg["data"][] = $row;
            }
        }

        //削除ボタン
        /* Edit by HPA for current_cursor start 2020/02/03 */
        $extra  = "id = \"delete\" aria-label = \"削除\" onclick=\"current_cursor('delete');return btn_submit('delete');\"";
        $extra .= ($model->schregno) ? "" : " disabled";
        $arg["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        /* Edit by HPA for current_cursor end 2020/02/20 */


        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "chkSCHREGNO", $model->schregno);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje063bForm1.html", $arg);
    }

    //署名チェック
    public function getShomeiCheck($db, $year, $schregno, $useSeitoSidoYorokuShomeiKinou)
    {
        $extraCheck = "";
        if ($useSeitoSidoYorokuShomeiKinou == 1) {
            $query = knje063bQuery::getOpinionsWk($year, $schregno);
            $check = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($check["CHAGE_OPI_SEQ"]) {
                $extraCheck = "disabled";
            }
        }
        return $extraCheck;
    }
}
?>
