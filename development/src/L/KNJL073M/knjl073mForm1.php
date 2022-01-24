<?php

require_once('for_php7.php');

class knjl073mform1 {
    function main(&$model) {
        $objForm = new form;
        $db = Query::dbCheckOut();
        $arg["start"] = $objForm->get_start("main", "POST", "knjl073mindex.php", "", "main");
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
        $arg["YEAR"] = $model->test_year;

        /**********/
        /* リスト */
        /**********/
        $query = knjl073mQuery::selectQuery($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $tr["EXAMNO"] = preg_replace('/^0*/', '', $row["EXAMNO"]);
            $tr["NAME"] = $row["NAME"];
            $extra = " onBlur=\"return this.value = toInteger(this.value);\"";
            $tr["SUB_ORDER"] = knjCreateTextBox($objForm, $row["SUB_ORDER"], "SUB_ORDER_{$row["EXAMNO"]}", 3, 3, $extra);
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
        View::toHTML($model, "knjl073mForm1.html", $arg);
    }
}
?>
