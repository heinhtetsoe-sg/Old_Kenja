<?php

require_once('for_php7.php');

class knjp140kForm2
{
    function main(&$model)
    {
        $objForm = new form;
        $db     = Query::dbCheckOut();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp140kindex.php", "", "edit");
        $arg["close"] = "";
        $sum = $sum2 = $sum3 = 0;

        $result = $db->query(knjp140kQuery::getList2($model->schregno, $model->inst_cd));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            array_walk($row, "htmlspecialchars_array");
            
            $row["INST_SEQ"] = View::alink("knjp140kindex.php", $row["INST_CD"]."-".$row["INST_SEQ"],"target=bot_frame",
                                             array("INST_SEQ" => $row["INST_SEQ"],
                                                   "cmd"      => "edit2"));
            $sum  += (int)$row["INST_MONEY_DUE"];
            $sum2 += (int)$row["PAID_MONEY"];
            $sum3 += (int)$row["REPAY_MONEY"];
            $row["INST_MONEY_DUE"]  = number_format($row["INST_MONEY_DUE"]);
            $row["PAID_MONEY"]      = number_format($row["PAID_MONEY"]);
            $row["REPAY_MONEY"]     = number_format($row["REPAY_MONEY"]);
            $row["INST_DUE_DATE"]   = str_replace("-","/",$row["INST_DUE_DATE"]);
            $row["PAID_MONEY_DATE"] = str_replace("-","/",$row["PAID_MONEY_DATE"]);
            $row["REPAY_DATE"]      = str_replace("-","/",$row["REPAY_DATE"]);
            $arg["data"][] = $row;
        }

        $arg["SUM"] = number_format($sum);
        $arg["SUM2"] = number_format($sum2);
        $arg["SUM3"] = number_format($sum3);

        $result->free();
        Query::dbCheckIn($db);

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));
    
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjp140kForm2.html", $arg);
    }
}
?>
