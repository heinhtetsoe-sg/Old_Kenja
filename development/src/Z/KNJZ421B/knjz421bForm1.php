<?php

require_once('for_php7.php');

class knjz421bForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz421bindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度
        $opt = array();
        $value_flg = false;
        $result = $db->query(knjz421bQuery::getJobOfferYear());
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["YEAR"] == $row["VALUE"]) {
                $value_flg = true;
            }
        }
        $model->field["YEAR"] = ($model->field["YEAR"] && $value_flg) ? $model->field["YEAR"] : CTRL_YEAR ;
        $extra = "onChange=\"return btn_submit('list');\"";
        $arg["top"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->field["YEAR"], $opt, $extra, 1);

        $result = $db->query(knjz421bQuery::getList($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        //hidden
        knjCreateHidden($objForm, "cmd");

        if ($model->cmd == "change_year") {
            $model->senkou_no = "";
            $arg["reload"] = "window.open('knjz421bindex.php?cmd=edit','right_frame')";
        }

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz421bForm1.html", $arg);
    }
}
