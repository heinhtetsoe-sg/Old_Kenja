<?php

require_once('for_php7.php');

class knjp150kForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $db     = Query::dbCheckOut();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp150kindex.php", "", "edit");

        //生徒の名前を取得
        $row = $db->getRow(knjp150kQuery::getStudentName($model->schregno),DB_FETCHMODE_ASSOC);
        $arg["NAME"] = $row["SCHREGNO"]."　".$row["NAME_SHOW"];


        //リスト取得
        $result = $db->query(knjp150kQuery::getList($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            array_walk($row, "htmlspecialchars_array");

            //リンク設定
            $row["EXPENSE_S_CD"] = View::alink("knjp150kindex.php", $row["TITLECD"], "target=bottom_frame",
                                    array("TITLECD"             => $row["TITLECD"],
                                          "EXPENSE_L_CD"        => $row["EXPENSE_L_CD"],
                                          "EXPENSE_M_CD"        => $row["EXPENSE_M_CD"],
                                          "EXPENSE_S_CD"        => $row["EXPENSE_S_CD"],
                                          "EXPENSE_S_NAME"      => $row["EXPENSE_S_NAME"],
                                          "PAID_INPUT_FLG"      => $row["PAID_INPUT_FLG"],
                                          "INST_CD"             => $row["INST_CD"],
                                          "cmd"                 => "edit"));

            //金額フォーマット
            $row["MONEY_DUE"] = (strlen($row["MONEY_DUE"])) ? number_format($row["MONEY_DUE"]): "";
            $row["PAID_MONEY"] = (strlen($row["PAID_MONEY"])) ? number_format($row["PAID_MONEY"]): "";
            $row["REPAY_MONEY"] = (strlen($row["REPAY_MONEY"])) ? number_format($row["REPAY_MONEY"]): "";

            //日付変換
            $row["REPAY_MONEY_DATE"] = str_replace("-", "/", $row["REPAY_MONEY_DATE"]);

            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);   

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));
    
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjp150kForm1.html", $arg);
    }
}
?>
