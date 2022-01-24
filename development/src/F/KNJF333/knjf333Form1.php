<?php

require_once('for_php7.php');

class knjf333form1 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("list", "POST", "knjf333index.php", "", "edit");

        //学籍資格データよりデータを取得
        $db = Query::dbCheckOut();

        //教育委員会判定
        $query = knjf333Query::z010Abbv1();
        $model->z010Abbv1 = $db->getOne($query);

        if ($model->z010Abbv1 == "1" || $model->z010Abbv1 == "2") {
            $arg["Z010ABBV1"] = "1";
        }

        $query = knjf333Query::ReadQuery($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["REGDDATE"]  = str_replace("-","/",$row["REGDDATE"]);

            $row["URL"] = View::alink("knjf333index.php", $row["HR_NAME"]."-".$row["ATTENDNO"]."番", "target=edit_frame",
                                        array("cmd"         => "edit",
                                              "SCHREGNO"    => $row["SCHREGNO"]
                                              ));

            $arg["data"][] = $row;
        }

        //checkbox
        $extra = "id=\"SCH_NASI\" ";
        $arg["SCH_NASI"] = knjCreateCheckBox($objForm, "SCH_NASI", "1", $extra);

        //県への報告用登録日付(テーブルは報告履歴テーブルのみ)
        $arg["EXECUTE_DATE"] = View::popUpCalendar($objForm, "EXECUTE_DATE", $model->execute_date, "");

        //県への報告
        $extra = "onclick=\"return btn_submit('houkoku');\"";
        $arg["btn_houkoku"] = knjCreateBtn($objForm, "btn_houkoku", "県への報告", $extra);

        //報告履歴
        $query = knjf333Query::getReport($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "REPORT", $model->report, $extra, 1);

        //印刷
        $extra = "onclick=\"newwin('".SERVLET_URL."');\"";
        $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);

        //CSVボタン
        $extra = "onclick=\"return btn_submit('csv');\"";
        $arg["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "CSV出力", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "SCHOOL_KIND", $model->schoolKind);
        knjCreateHidden($objForm, "PRGID", "KNJF333");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "SCHOOLCD", $model->schoolcd);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") == "right_list"){ 
            $arg["reload"]  = "parent.edit_frame.location.href='knjf333index.php?cmd=edit&SCHREGNO={$model->schregno}'";
        }

        View::toHTML($model, "knjf333Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank != "") $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "YEAR") {
        $value = ($value && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
