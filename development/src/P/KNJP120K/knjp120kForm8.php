<?php

require_once('for_php7.php');

require_once('knjp120kQuery2.inc');
class knjp120kForm8
{
    function main(&$model)
    {
        $objForm = new form;
        $db     = Query::dbCheckOut();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp070kindex.php", "", "edit");
        $arg["close"] = "";
        $arg["data"] = array();
        $sum = 0;

        $result = $db->query(knjp120kQuery2::getList4($model->schregno));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            array_walk($row, "htmlspecialchars_array");
            
            $sum += (int)$row["MONEY_DUE"];
            $row["MONEY_DUE"] = number_format($row["MONEY_DUE"]);

            $arg["data"][] = $row;
        }
        $arg["M_SUM"] = number_format($sum);

        $result->free();
        Query::dbCheckIn($db);

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));
    
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjp120kForm8.html", $arg);
    }
}
?>
