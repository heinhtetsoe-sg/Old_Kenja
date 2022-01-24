<?php

require_once('for_php7.php');

class knjp070kForm1
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

        //生徒名
        $row = $db->getRow(knjp070kQuery::getStudentName($model->schregno),DB_FETCHMODE_ASSOC);
        $arg["SCHREGNO"] = $row["SCHREGNO"];
        $arg["NAME_SHOW"] = $row["NAME_SHOW"];

        $result = $db->query(knjp070kQuery::getList1($model->schregno));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            array_walk($row, "htmlspecialchars_array");
            
            $row["EXPENSE_M_CD"] = View::alink("knjp070kindex.php", $row["EXPENSE_L_CD"].$row["EXPENSE_M_CD"],"target=bot_frame",
                                             array("EXPENSE_M_CD" => $row["EXPENSE_M_CD"],
                                                   "cmd"          => "edit1"));
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
        View::toHTML($model, "knjp070kForm1.html", $arg);
    }
}
?>
