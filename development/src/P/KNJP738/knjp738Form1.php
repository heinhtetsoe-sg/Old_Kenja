<?php

require_once('for_php7.php');

class knjp738Form1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjp738index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度コンボ
        $query = knjp738Query::getYear($model);
        $extra = "onChange=\"return btn_submit('change')\"";
        makeCmb($objForm, $arg, $db, $query, $model->year, "YEAR", $extra, 1);

        //校種コンボ
        $query = knjp738Query::getSchoolKind($model);
        $extra = "onChange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, $model->schoolkind, "SCHOOL_KIND", $extra, 1, "");

        //リスト取得
        $listArray = $rowCntArray = array();
        $query = knjp738Query::getList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");
            $rowCntArray[$row["GRP_CD"]] += 1;
            $listArray[] = $row;
        }
        $result->free();

        //一覧表示
        $grp_cd = "";
        foreach ($listArray as $key => $row) {
            $setData = array();

            if ($grp_cd != $row["GRP_CD"]) {
                $setData["GRP_CD"] = View::alink("knjp738index.php", $row["GRP_CD"].":".$row["GRP_NAME"], "target=right_frame",
                                                 array("YEAR"           => $model->year,
                                                       "SCHOOL_KIND"    => $model->schoolkind,
                                                       "GRP_CD"         => $row["GRP_CD"],
                                                       "cmd"            => "edit"
                                                       ));

                $setData["ROWSPAN"] = ($rowCntArray[$row["GRP_CD"]] > 1) ? "rowspan=\"{$rowCntArray[$row["GRP_CD"]]}\"" : "";
            }

            $sep = ($row["COLLECT_M_CD"]) ? " : " : "";
            $setData["COLLECT_LM_CD"] = $row["COLLECT_L_CD"].$row["COLLECT_M_CD"].$sep.$row["COLLECT_M_NAME"];

            $arg["data"][] = $setData;
            $grp_cd = $row["GRP_CD"];
        }

        //コピー
        $extra = "onclick=\"return btn_submit('copy')\"";
        $arg["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        if ($model->cmd == "change" || $model->cmd == "copy") {
            unset($model->grp_cd);
            $arg["reload"] = "window.open('knjp738index.php?cmd=edit&YEAR={$model->year}&SCHOOL_KIND={$model->schoolkind}','right_frame');";
        }

        View::toHTML($model, "knjp738Form1.html", $arg);
    }

}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size) {
    $opt = array();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SCHOOL_KIND" && SCHOOLKIND) {
        $value = ($value && $value_flg) ? $value : SCHOOLKIND;
    } else if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value) ? $value : $opt[0]["value"];
    }
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
