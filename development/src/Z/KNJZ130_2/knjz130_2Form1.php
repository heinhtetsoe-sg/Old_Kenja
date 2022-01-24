<?php

require_once('for_php7.php');

class knjz130_2Form1
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz130_2index.php", "", "edit");

        $db = Query::dbCheckOut();

        $opt = array();
        $query = knjz130_2Query::getCombo();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            $opt[] = array("label" => $row["NAMECD"]."　".htmlspecialchars($row["CDMEMO"]),
                           "value" => $row["NAMECD"]);
            
            if (!isset($model->namecd1)) {
                $model->namecd1 = $row["NAMECD"];
            }
        }

        //コンボボックスを作成
        $extra = "onchange=\"return btn_submit('list');\"";
        $arg["top"]["NAMECD1"] = knjCreateCombo($objForm, "NAMECD1", $model->namecd1, $opt, $extra, 1);

        $query = knjz130_2Query::getList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");
            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz130_2Form1.html", $arg);
    }
}
