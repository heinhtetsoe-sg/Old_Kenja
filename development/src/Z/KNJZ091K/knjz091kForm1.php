<?php

require_once('for_php7.php');

class knjz091kForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz091kindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度設定
        $arg["year"] = $model->year;

        //button
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_pre_copy"] = knjCreateBtn($objForm, "btn_pre_copy", "前年度コピー", $extra);

        //リスト表示
        $result = $db->query(knjz091kQuery::selectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            $row["REDUCTION_ADD_MONEY"] = (strlen($row["REDUCTION_ADD_MONEY"])) ? number_format($row["REDUCTION_ADD_MONEY"]): "";
            $row["REDUCTION_ADD_MONEY"] = View::alink("knjz091kindex.php", $row["REDUCTION_ADD_MONEY"], "target=\"right_frame\"",
                                                array("cmd"                 => "edit",
                                                      "GRADE"               => $row["GRADE"],
                                                      "REDUCTIONMONEY"      => $row["REDUCTIONMONEY"],
                                                      "REDUCTION_ADD_MONEY" => $row["REDUCTION_ADD_MONEY"],
                                                      "INCOME_LOW"          => $row["INCOME_LOW"],
                                                      "INCOME_HIGH"         => $row["INCOME_HIGH"],
                                                      "REDUCTION_SEQ"       => $row["REDUCTION_SEQ"]
                                                      ));

            //金額をカンマ区切りにする
            $row["REDUCTIONMONEY"] = (strlen($row["REDUCTIONMONEY"])) ? number_format($row["REDUCTIONMONEY"]): "";
            $row["INCOME_LOW"]     = (strlen($row["INCOME_LOW"])) ?     number_format($row["INCOME_LOW"]): "";
            $row["INCOME_HIGH"]    = (strlen($row["INCOME_HIGH"])) ?    number_format($row["INCOME_HIGH"]): "";

            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        if(!isset($model->warning) && VARS::post("cmd") == "copy"){
            $arg["reload"] = "parent.right_frame.location.href='knjz091kindex.php?cmd=edit"
                           . "&year=".$model->year."';";
        }
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz091kForm1.html", $arg);
    }
}
?>
