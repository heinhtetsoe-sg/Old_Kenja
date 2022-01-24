<?php

require_once('for_php7.php');

require_once('knjp120kQuery2.inc');
class knjp120kForm10
{
    function main(&$model)
    {
        $objForm = new form;
        $db     = Query::dbCheckOut();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp140kindex.php", "", "edit");
        $arg["close"] = "";
        $arg["data"] = array();
        $sum = $sum2 = $sum3 = 0;

        $result = $db->query(knjp120kQuery2::getList6($model->schregno));
        $tmp = array();
        $i = 0;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
#            //先頭の分納コードを初期値にする
#            if ($i == 0) {
#                $model->inst_cd = $row["INST_CD"];
#            }
            array_walk($row, "htmlspecialchars_array");

            if($row["EXPENSE_S_EXIST_FLG"] == "1") {
                $row["MONEY_DUE"] = $db->getOne(knjp120kQuery2::getMoneyDue($model->schregno, $row["EXPENSE_M_CD"]));
            }

            $tmp[$row["INST_CD"]]["INST_CD"]          = $row["INST_CD"];
            $tmp[$row["INST_CD"]]["EXPENSE_M_NAME"][] = $row["EXPENSE_M_NAME"];
            $tmp[$row["INST_CD"]]["MONEY_DUE"][]      = $row["MONEY_DUE"];
            $tmp[$row["INST_CD"]]["PAID_MONEY"]       = $row["PAID_MONEY"];
            $tmp[$row["INST_CD"]]["REPAY_MONEY"]      = $row["REPAY_MONEY"];
            $tmp[$row["INST_CD"]]["PAID_MONEY_DATE"]  = str_replace("-","/",$row["PAID_MONEY_DATE"]);
            $sum  += (int)$row["MONEY_DUE"];

            $i++;
        }

        $model->money = array();

        foreach ($tmp as $key => $val)
        {
            //MODELに必要金額と入金額を保持
            $model->money[$val["INST_CD"]] = array("MONEY_DUE" => array_sum($val["MONEY_DUE"]), "PAID_MONEY" => $val["PAID_MONEY"]);

            //必要金額と入金額が同じ場合のみ完了日表示
            if (((int)$val["PAID_MONEY"] - (int)$val["REPAY_MONEY"]) != (int)array_sum($val["MONEY_DUE"])) {
                $val["PAID_MONEY_DATE"] = "";
            }

            $sum2 += (int)$val["PAID_MONEY"];
            $val["PAID_MONEY"]     = number_format($val["PAID_MONEY"]);
            $val["MONEY_DUE"]      = number_format(array_sum($val["MONEY_DUE"]));

            $sum3 += (int)$val["REPAY_MONEY"];
            $val["REPAY_MONEY"]     = number_format($val["REPAY_MONEY"]);

            $val["EXPENSE_M_NAME"] = implode("<BR>", $val["EXPENSE_M_NAME"]);
            
            $arg["data"][] = $val;
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
        View::toHTML($model, "knjp120kForm10.html", $arg);
    }
}
?>
