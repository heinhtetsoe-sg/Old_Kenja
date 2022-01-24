<?php

require_once('for_php7.php');

class knjp121kForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $db     = Query::dbCheckOut();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp121kindex.php", "", "edit");

        //生徒の名前を取得
        $row = $db->getRow(knjp121kQuery::getStudentName($model->schregno),DB_FETCHMODE_ASSOC);
        $arg["NAME"] = $row["SCHREGNO"]."　".$row["NAME_SHOW"];

        //リスト取得
        $result = $db->query(knjp121kQuery::getList($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            if ($row["EXPENSE_S_EXIST_FLG"] == "1") {
                $row["MONEY_DUE"] = $db->getOne(knjp121kQuery::getMoneyDueS($model->schregno, $row["EXPENSE_M_CD"]));

                $sRepaiData = $db->getRow(knjp121kQuery::getRepayDiv($model->schregno, $row["EXPENSE_M_CD"]), DB_FETCHMODE_ASSOC);
                $row["REPAY_DEV"] = $sRepaiData["MIN_DIV"];
            }

            //リンク設定
            $row["EXPENSE_M_CD"] = View::alink("knjp121kindex.php", $row["EXPENSE_L_CD"].$row["EXPENSE_M_CD"], "target=bottom_frame",
                                    array("EXPENSE_M_CD"        => $row["EXPENSE_M_CD"],
                                          "EXPENSE_L_CD"        => $row["EXPENSE_L_CD"],
                                          "EXPENSE_M_NAME"      => $row["EXPENSE_M_NAME"],
                                          "MONEY_DUE"           => $row["MONEY_DUE"],
                                          "EXPENSE_S_EXIST_FLG" => $row["EXPENSE_S_EXIST_FLG"],
                                          "PAID_INPUT_FLG"      => $row["PAID_INPUT_FLG"],
                                          "INST_CD"             => $row["INST_CD"],
                                          "BANK_TRANS_SDATE"    => $row["BANK_TRANS_SDATE"],
                                          "cmd"                 => "edit"));

            //金額フォーマット
            $row["MONEY_DUE"] = (strlen($row["MONEY_DUE"])) ? number_format($row["MONEY_DUE"]): "";
            $row["PAID_MONEY"] = (strlen($row["PAID_MONEY"])) ? number_format($row["PAID_MONEY"]): "";
            $row["REPAY_MONEY"] = (strlen($row["REPAY_MONEY"])) ? number_format($row["REPAY_MONEY"]): "";

            //入金区分
            if (strlen($row["PAID_MONEY_DIV"])) {
                $row["PAID_MONEY_DIV"] = $db->getOne(knjp121kQuery::getName($model->year, "G205", $row["PAID_MONEY_DIV"]));
            }
            $repayNmst = $row["EXPENSE_S_EXIST_FLG"] == "1" ? "G209" : "G212";
            if (strlen($row["REPAY_DEV"])) {
                $row["REPAY_DEV"] = $db->getOne(knjp121kQuery::getName($model->year, $repayNmst, $row["REPAY_DEV"]));
                if($row["EXPENSE_S_EXIST_FLG"] == "1") {
                    $row["REPAY_DEV"] .= $sRepaiData["MIN_DIV"] == $sRepaiData["MAX_DIV"] ? "" : " 他";
                    $row["REPAY_DEV"] .= "({$sRepaiData["CNT"]})";
                }
            }

            //日付変換
            $row["PAID_MONEY_DATE"] = str_replace("-", "/", $row["PAID_MONEY_DATE"]);

            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);   

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjp121kForm1.html", $arg);
    }
}
?>
