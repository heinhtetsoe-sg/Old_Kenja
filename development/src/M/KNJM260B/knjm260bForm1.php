<?php

require_once('for_php7.php');


class knjm260bForm1 {
    function main(&$model) {
        $objForm      = new form;

        $arg["start"] = $objForm->get_start("main", "POST", "knjm260bindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期
        $query = knjm260bQuery::getSemesterName();
        $setSemeName = $db->getOne($query);
        $arg["SEMESTER"] = $setSemeName;

        //科目リスト
        $query = knjm260bQuery::getSubClassCd($model);
        $extra = "onChange=\"btn_submit('change')\";";
        makeCmb($objForm, $arg, $db, $query, $model->subclassCd, "SUBCLASSCD", $extra, 1, "BLANK");

        //講座リスト
        $query = knjm260bQuery::getChrSubCd($model);
        $extra = "onChange=\"btn_submit('change')\";";
        makeCmb($objForm, $arg, $db, $query, $model->chairCd, "CHAIRCD", $extra, 1, "BLANK");

        //生徒一覧
        $model->disabled = true;
        makeListInfo($objForm, $arg, $db, $model);

        //ボタン作成
        $extraUp  = (AUTHORITY >= DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('update');\"" : "disabled";
        $extraUp .= ($model->disabled) ? " disabled" : "";
        $extraRst = "onclick=\"return btn_submit('reset');\"";
        $extraEnd = "onclick=\"closeWin();\"";
        //更新
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extraUp);
        //取消
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extraRst);
        //終了
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extraEnd);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "H_SUBCLASSCD");
        knjCreateHidden($objForm, "H_CHAIRCD");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjm260bForm1.html", $arg);

    }
}
//科目データ表示
function makeListInfo(&$objForm, &$arg, $db, &$model) {
    //extraセット
    $extraClr    = " onchange=\"this.style.background='#ccffcc'\";";
    $extraInt    = " onblur=\"this.value=toInteger(this.value)\";";
    $extraRight  = " STYLE=\"text-align: right\"";

    $query = knjm260bQuery::getNameMst("M006");
    $result = $db->query($query);
    $optHyouka = array();
    $optHyouka[] = array('value' => "", 'label' => "");
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $optHyouka[] = array('value' => $row["VALUE"], 'label' => $row["LABEL"]);
    }
    $result->free();

    $model->schregNoArray = array();
    $extra1 = $extraClr.$extraInt.$extraRight;
    $query = knjm260bQuery::getTestData($model);
    $result = $db->query($query);
    $setCheckHidden = "";
    $setCheckSep = "";
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");

        $model->schregNoArray[] = $row["SCHREGNO"];

        $setCheckHidden .= $setCheckSep.$row["SCHREGNO"].":".$row["REPO_MAX_CNT"].":".$row["SCHREGNO"];
        $setCheckSep = ",";

        $row["R_VAL1"] = knjCreateTextBox($objForm, $row["R_VAL1"], "R_VAL1_".$row["SCHREGNO"], 3, 3, $extra1);
        $row["R_VAL2"] = knjCreateTextBox($objForm, $row["R_VAL2"], "R_VAL2_".$row["SCHREGNO"], 3, 3, $extra1);
        $row["R_VAL3"] = knjCreateTextBox($objForm, $row["R_VAL3"], "R_VAL3_".$row["SCHREGNO"], 3, 3, $extra1);
        $row["T_VAL1"] = knjCreateCombo($objForm, "T_VAL1_".$row["SCHREGNO"], $row["T_VAL1"], $optHyouka, "", 1);

        if ($row["USE_MEDIA1"] == "1") {
            $row["IS_DVD"] = "あり";
            $extra = " class=\"changeColor\" data-name=\"S_VAL1_{$row["SCHREGNO"]}\" id=\"S_VAL1_{$row["SCHREGNO"]}\" ";
            $checked = $row["S_VAL1"] == "1" ? " checked " : "";
            $row["S_VAL1"] = knjCreateCheckBox($objForm, "S_VAL1_".$row["SCHREGNO"], "1", $checked.$extra);
        }

        $model->disabled = false;
        $arg["testdata"][] = $row;
    }
    //hidden
    knjCreateHidden($objForm, "CHECK_MAX", $setCheckHidden);

    $result->free();
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    
    if ($name == "SEMESTER") {
        $value = ($value != "" && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
