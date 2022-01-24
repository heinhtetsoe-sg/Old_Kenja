<?php

require_once('for_php7.php');

class knjp070kForm2
{
    function main(&$model)
    {
        $objForm = new form;
        $db     = Query::dbCheckOut();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp070kindex.php", "", "edit");
        $arg["close"] = "";
        $sum = 0;
        
        $result = $db->query(knjp070kQuery::getList2($model->schregno));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            array_walk($row, "htmlspecialchars_array");
            
            $row["EXPENSE_S_CD"] = View::alink("knjp070kindex.php", $row["EXPENSE_L_CD"].$row["EXPENSE_M_CD"].$row["EXPENSE_S_CD"],"target=bot_frame",
                                             array("EXPENSE_S_CD" => $row["EXPENSE_S_CD"].$row["EXPENSE_M_CD"],
                                                   "cmd"          => "edit2"));
            $sum += (int)$row["MONEY_DUE"];
            $row["MONEY_DUE"] = number_format($row["MONEY_DUE"]);
            $arg["data"][] = $row;
        }

        $arg["S_SUM"] = number_format($sum);
        $result->free();
        Query::dbCheckIn($db);   

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));
    
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjp070kForm2.html", $arg);
    }
}
?>
