<?php

require_once('for_php7.php');

class knjp908Form1 
{
    function main(&$model)
    {
        $objForm = new form;

        $arg["start"] = $objForm->get_start("edit", "POST", "knjp908index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        $arg["YEAR"] = CTRL_YEAR;

        //生徒名
        $row = $db->getRow(knjp908Query::getStudentName($model->schregno),DB_FETCHMODE_ASSOC);
        $arg["SCHREGNO"] = $row["SCHREGNO"];
        $arg["NAME_SHOW"] = $row["NAME_SHOW"];

        //月
        $monthArray = array("04", "05", "06", "07", "08", "09", "10", "11", "12", "01", "02", "03");
        $opt = array();
        foreach ($monthArray as $key => $month) {
            $opt[] = array("label" => $month."月", "value" => $month);
        }
        $extra = "onchange=\"return btn_submit('edit')\"";
        list($ctrlY, $ctrlM, $ctrlD) = explode("-", CTRL_DATE);
        $model->month = $model->month ? $model->month : $ctrlM;
        $arg["MONTH_CMB"] = knjCreateCombo($objForm, "MONTH_CMB", $model->month, $opt, $extra, 1);

        $dispDataArray = array();
        $dispDataTotalArray = array();
        $query = knjp908Query::getIncomeData($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dispDataArray[$row["LCD"]]["LCD"] = $row["LCD"];
            $dispDataArray[$row["LCD"]]["L_NAME"] = $row["LEVY_L_NAME"];
            $dispDataArray[$row["LCD"]]["INCOME_MONEY"] += $row["INCOME_MONEY"];
            $dispDataArray[$row["LCD"]]["DIFFERENCE_MONEY"] += $row["INCOME_MONEY"];
            $dispDataArray[$row["LCD"]]["INCOME_DATA"][$row["INCOME_M_CD"]]["INCOME_M_NAME"] = $row["INCOME_M_NAME"];
            $dispDataArray[$row["LCD"]]["INCOME_DATA"][$row["INCOME_M_CD"]]["INCOME_M_MONEY"] = $row["INCOME_MONEY"];
            $dispDataTotalArray["TOTAL_INCOME_MONEY"] += $row["INCOME_MONEY"];
            $dispDataTotalArray["TOTAL_DIFFERENCE_MONEY"] += $row["INCOME_MONEY"];
            $dispDataArray[$row["LCD"]]["OUTGO_MONEY"] = 0;
        }
        $result->free();

        $query = knjp908Query::getOutGoData($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $dispDataArray[$row["LCD"]]["LCD"] = $row["LCD"];
            $dispDataArray[$row["LCD"]]["L_NAME"] = $row["LEVY_L_NAME"];
            $dispDataArray[$row["LCD"]]["OUTGO_MONEY"] += $row["OUTGO_MONEY"];
            $dispDataArray[$row["LCD"]]["DIFFERENCE_MONEY"] -= $row["OUTGO_MONEY"];
            $dispDataArray[$row["LCD"]]["OUTGO_DATA"][$row["MCD"].$row["SCD"]]["OUTGO_S_NAME"] = $row["OUTGO_S_NAME"];
            $dispDataArray[$row["LCD"]]["OUTGO_DATA"][$row["MCD"].$row["SCD"]]["OUTGO_S_MONEY"] = $row["OUTGO_MONEY"];
            $dispDataTotalArray["TOTAL_OUTGO_MONEY"] += $row["OUTGO_MONEY"];
            $dispDataTotalArray["TOTAL_DIFFERENCE_MONEY"] -= $row["OUTGO_MONEY"];
            $dispDataArray[$row["LCD"]]["INCOME_MONEY"] = $dispDataArray[$row["LCD"]]["INCOME_MONEY"] ? $dispDataArray[$row["LCD"]]["INCOME_MONEY"] : 0;
        }
        $result->free();

        ksort($dispDataArray);

        //総額
        $arg["TOTAL_INCOME_MONEY"]      = number_format($dispDataTotalArray["TOTAL_INCOME_MONEY"]);
        $arg["TOTAL_DIFFERENCE_MONEY"]  = number_format($dispDataTotalArray["TOTAL_DIFFERENCE_MONEY"]);
        $arg["TOTAL_OUTGO_MONEY"]       = number_format($dispDataTotalArray["TOTAL_OUTGO_MONEY"]);
        $arg["TOTAL_DIFFERENCE_MONEY"]  = number_format($dispDataTotalArray["TOTAL_DIFFERENCE_MONEY"]);

        $dataCnt = 0;
        foreach ($dispDataArray as $lCd => $lData) {
            $setData = array();
            $lData["INCOME_MONEY"]      = number_format($lData["INCOME_MONEY"]);
            $lData["OUTGO_MONEY"]       = number_format($lData["OUTGO_MONEY"]);
            $lData["DIFFERENCE_MONEY"]  = number_format($lData["DIFFERENCE_MONEY"]);
            $setData = $lData;
            if (isset($lData["INCOME_DATA"])) {
                foreach ($lData["INCOME_DATA"] as $mCd => $mData) {
                    $mData["INCOME_M_MONEY"] = number_format($mData["INCOME_M_MONEY"]);
                    $setData["dataPaid"][] = $mData;
                }
            }
            if (isset($lData["OUTGO_DATA"])) {
                foreach ($lData["OUTGO_DATA"] as $mCd => $mData) {
                    $mData["OUTGO_S_MONEY"] = number_format($mData["OUTGO_S_MONEY"]);
                    $setData["dataLevy"][] = $mData;
                }
            }
            if (get_count($dispDataArray) > ($dataCnt + 1)) {
                $setData["nextAri"] = 1;
            }
            $arg["data"][$dataCnt] = $setData;

            $dataCnt++;
        }

        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML5($model, "knjp908Form1.html", $arg);
    }
}
?>
