<?php

require_once('for_php7.php');

class knjz095kForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz095kindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //年度設定
        $opt = array();
        $value_flg = false;
        $query = knjz095kQuery::getYear();
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
        $result = $db->query(knjz095kQuery::selectQuery($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            $row["PREFECTURESCD"] = View::alink("knjz095kindex.php", $row["PREFECTURESCD"], "target=\"right_frame\"",
                                                array("cmd"            => "edit",
                                                      "PREFECTURESCD"  => $row["PREFECTURESCD"],
                                                      "GRADE"          => $row["GRADE"],
                                                      "YEAR"           => $model->year
                                                      ));

            //金額をカンマ区切りにする
            $row["CURRICULUM_FLG"] = (strlen($row["CURRICULUM_FLG"])) ? "レ": "";
            $row["THIS_YEAR_FLG"]  = (strlen($row["THIS_YEAR_FLG"])) ? "レ": "";
            $row["USE_RANK"]       = (strlen($row["USE_RANK"])) ? "レ": "";

            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        if(!isset($model->warning) && (VARS::post("cmd") == "copy" || VARS::post("cmd") == "changeYear")) {
            $arg["reload"] = "parent.right_frame.location.href='knjz095kindex.php?cmd=edit"
                           . "&year=".$model->year."';";
        }
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz095kForm1.html", $arg);
    }
}
?>
