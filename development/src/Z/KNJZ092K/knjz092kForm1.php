<?php

require_once('for_php7.php');

class knjz092kForm1 {
    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz092kindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度設定
        $opt = array();
        $value_flg = false;
        $query = knjz092kQuery::getYear();
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
        $result = $db->query(knjz092kQuery::selectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            $row["PREFECTURESCD"] = View::alink("knjz092kindex.php", $row["PREFECTURESCD"], "target=\"right_frame\"",
                                                array("cmd"                 => "edit",
                                                      "PREFECTURESCD"       => $row["PREFECTURESCD"],
                                                      "GRADE"               => $row["GRADE"],
                                                      "RANK_DIV"            => $row["RANK_DIV"],
                                                      "YEAR"                => $model->year,
                                                      "REDUCTIONMONEY_1"    => $row["REDUCTIONMONEY_1"],
                                                      "REDUCTIONMONEY_2"    => $row["REDUCTIONMONEY_2"],
                                                      "MIN_MONEY_1"         => $row["MIN_MONEY_1"],
                                                      "MIN_MONEY_2"         => $row["MIN_MONEY_2"]
                                                      ));

            //金額をカンマ区切りにする
            $row["REDUCTIONMONEY_1"] = (strlen($row["REDUCTIONMONEY_1"])) ? number_format($row["REDUCTIONMONEY_1"]): "";
            $row["REDUCTIONMONEY_2"] = (strlen($row["REDUCTIONMONEY_2"])) ? number_format($row["REDUCTIONMONEY_2"]): "";
            $row["MAX_MONEY"] = (strlen($row["MAX_MONEY"])) ? number_format($row["MAX_MONEY"]): "";
            $row["MIN_MONEY_1"] = (strlen($row["MIN_MONEY_1"])) ? number_format($row["MIN_MONEY_1"]): "";
            $row["MIN_MONEY_2"] = (strlen($row["MIN_MONEY_2"])) ? number_format($row["MIN_MONEY_2"]): "";
            $row["MIN_MONEY"] = (strlen($row["MIN_MONEY"])) ? number_format($row["MIN_MONEY"]): "";

            $arg["data"][] = $row;
        }
        $result->free();

        //DB切断
        Query::dbCheckIn($db);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        if(!isset($model->warning) && (VARS::post("cmd") == "copy" || VARS::post("cmd") == "changeYear")) {
            $arg["reload"] = "parent.right_frame.location.href='knjz092kindex.php?cmd=edit"
                           . "&year=".$model->year."';";
        }

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz092kForm1.html", $arg);
    }
}
?>
