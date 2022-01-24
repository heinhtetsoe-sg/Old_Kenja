<?php

require_once('for_php7.php');

class knjl074mform1 {
    function main(&$model) {
        $objForm = new form;
        $db = Query::dbCheckOut();
        $arg["start"] = $objForm->get_start("main", "POST", "knjl074mindex.php", "", "main");
        $arg["Closing"] = "";

        /**************************/
        /* セキュリティーチェック */
        /**************************/
        if(AUTHORITY != DEF_UPDATABLE) {
            $arg["Closing"] = " closing_window(); ";
        }

        /********/
        /* 年度 */
        /********/
        $opt = array();
        $value_flg = false;
        $query = knjl074mQuery::getTestYear();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->test_year == $row["VALUE"]) $value_flg = true;
        }
        $model->test_year = ($model->test_year && $value_flg) ? $model->test_year : $opt[0]["value"];
        $objForm->ae( array("type"       => "select",
                            "name"       => "TEST_YEAR",
                            "size"       => "1",
                            "extrahtml"  => "Onchange=\"btn_submit('main');\"",
                            "value"      => $model->test_year,
                            "options"    => $opt));
        $arg["TEST_YEAR"] = $objForm->ge("TEST_YEAR");

        /**********/
        /* リスト */
        /**********/
        $query = knjl074mQuery::selectQuery($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $tr["JUDGEMENT_NAME"] = $row["JUDGEMENT_NAME"];
            $extra = " onBlur=\"return this.value = toInteger(this.value);\"";
            $tr["JUDGE_CNT"] = knjCreateTextBox($objForm, $row["JUDGE_CNT"], "JUDGE_CNT_{$row["JUDGEMENT"]}", 3, 3, $extra);
            $arg["data"][] = $tr;
        }

        /**********/
        /* ボタン */
        /**********/
        //更新
        $extra = "onClick=\"return btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消
        $extra = "onClick=\"return btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onClick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl074mForm1.html", $arg);
    }
}
?>
