<?php

require_once('for_php7.php');

class knjz090kForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz090kindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度設定
        $opt = array();
        $value_flg = false;
        $query = knjz090kQuery::getYear();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->year == $row["VALUE"]) $value_flg = true;
        }
        $model->year = ($model->year && $value_flg) ? $model->year : CTRL_YEAR;
        $extra = "onChange=\"btn_submit('changeYear')\"";
        $arg["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->year, $opt, $extra, 1);

        //button
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["btn_pre_copy"] = knjCreateBtn($objForm, "btn_pre_copy", "前年度からコピー", $extra);

        //リスト表示
        $query = knjz090kQuery::selectQuery($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            $row["PREFECTURESCD"] = View::alink("knjz090kindex.php", $row["PREFECTURESCD"], "target=\"right_frame\"",
                                                array("cmd"                 => "edit",
                                                      "PREFECTURESCD"       => $row["PREFECTURESCD"],
                                                      "GRADE"               => $row["GRADE"],
                                                      "YEAR"                => $model->year,
                                                      "INCOME_LOW1"         => $row["INCOME_LOW1"],
                                                      "INCOME_HIGH1"        => $row["INCOME_HIGH1"],
                                                      "INCOME_SIBLINGS1"    => $row["INCOME_SIBLINGS1"],
                                                      "REDUCTIONMONEY_1"    => $row["REDUCTIONMONEY_1"],
                                                      "INCOME_LOW2"         => $row["INCOME_LOW2"],
                                                      "INCOME_HIGH2"        => $row["INCOME_HIGH2"],
                                                      "INCOME_SIBLINGS2"    => $row["INCOME_SIBLINGS2"],
                                                      "REDUCTIONMONEY_2"    => $row["REDUCTIONMONEY_2"],
                                                      "REDUCTION_SEQ"       => $row["REDUCTION_SEQ"]
                                                      ));

            //金額をカンマ区切りにする
            $row["INCOME_LOW1"]       = (strlen($row["INCOME_LOW1"])) ?     number_format($row["INCOME_LOW1"]): "";
            $row["INCOME_HIGH1"]      = (strlen($row["INCOME_HIGH1"])) ?    number_format($row["INCOME_HIGH1"]): "";
            $row["REDUCTIONMONEY_1"]  = (strlen($row["REDUCTIONMONEY_1"])) ? number_format($row["REDUCTIONMONEY_1"]): "";
            $row["INCOME_LOW2"]       = (strlen($row["INCOME_LOW2"])) ?     number_format($row["INCOME_LOW2"]): "";
            $row["INCOME_HIGH2"]      = (strlen($row["INCOME_HIGH2"])) ?    number_format($row["INCOME_HIGH2"]): "";
            $row["REDUCTIONMONEY_2"]  = (strlen($row["REDUCTIONMONEY_2"])) ? number_format($row["REDUCTIONMONEY_2"]): "";

            $row["INCOME_SIBLINGS1"] = $row["INCOME_SIBLINGS1"] > 0 ? $row["INCOME_SIBLINGS1"]."人" : "";
            $row["INCOME_SIBLINGS2"] = $row["INCOME_SIBLINGS2"] > 0 ? $row["INCOME_SIBLINGS2"]."人" : "";

            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        if(!isset($model->warning) && (VARS::post("cmd") == "copy" || VARS::post("cmd") == "changeYear")) {
            $arg["reload"] = "parent.right_frame.location.href='knjz090kindex.php?cmd=edit"
                           . "&year=".$model->year."';";
        }
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz090kForm1.html", $arg);
    }
}
?>
