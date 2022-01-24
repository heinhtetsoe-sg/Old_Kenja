<?php

require_once('for_php7.php');

require_once('knjp120kQuery2.inc');
class knjp120kForm7
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp040kindex.php", "", "edit");

        $db = Query::dbCheckOut();

        $query = knjp120kQuery2::getRow2($model);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (is_array($row)){
            @array_walk($row, "htmlspecialchars_array");

            //軽減額
            $row["REDUCTIONMONEY"] = "";
            if (is_numeric($row["REDUCTIONMONEY_1"]) && is_numeric($row["REDUCTIONMONEY_2"])) {
                $row["REDUCTIONMONEY"] = $row["REDUCTIONMONEY_1"] + $row["REDUCTIONMONEY_2"];
            } else if (is_numeric($row["REDUCTIONMONEY_1"])) {
                $row["REDUCTIONMONEY"] = $row["REDUCTIONMONEY_1"];
            } else if (is_numeric($row["REDUCTIONMONEY_2"])) {
                $row["REDUCTIONMONEY"] = $row["REDUCTIONMONEY_2"];
            }
            if (is_numeric($row["REDUCTIONMONEY"])) {
                $row["REDUCTIONMONEY"] = number_format($row["REDUCTIONMONEY"]);
            }
            $arg["data"] = $row;
        }
        Query::dbCheckIn($db);

        View::toHTML($model, "knjp120kForm7.html", $arg);
    }
}
?>
