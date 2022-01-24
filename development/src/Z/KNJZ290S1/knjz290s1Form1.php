<?php

require_once('for_php7.php');

class knjz290s1Form1
{
    function main(&$model) {

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz290s1index.php", "", "edit");

        $db = Query::dbCheckOut();

        $result = $db->query(knjz290s1Query::getList($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");
            $row["FROM_DATE"] = str_replace("-", "/", $row["FROM_DATE"]);
            $row["TO_DATE"] = str_replace("-", "/", $row["TO_DATE"]);
            if ($row["IS_MAX"]) {
                $row["Link"] = "1";
                $row["notLink"] = "";
            } else {
                $row["Link"] = "";
                $row["notLink"] = "1";
            }
            $arg["data"][] = $row; 
        }
        $result->free();
        Query::dbCheckIn($db);

        //hidden
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz290s1Form1.html", $arg);
    }
} 
?>
