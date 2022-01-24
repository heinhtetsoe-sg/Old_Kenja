<?php

require_once('for_php7.php');


class knjm260aForm1
{
    function main(&$model)
    {
        $objForm      = new form;

        $arg["start"] = $objForm->get_start("main", "POST", "knjm260aindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期
        $query = knjm260aQuery::getSemesterName();
        $setSemeName = $db->getOne($query);
        $arg["SEMESTER"] = $setSemeName;

        //生徒データ表示
        makeStudentInfo($arg, $db, $model);

        //登録日
        if ($model->field["DATE"] == "") $model->field["DATE"] = str_replace("-","/",CTRL_DATE);
        $arg["data"]["DATE"] = View::popUpCalendar($objForm	,"DATE"	,$model->field["DATE"]);

        //科目一覧
        makeListInfo($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $db);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjm260aForm1.html", $arg);

    }
}

//生徒データ表示
function makeStudentInfo(&$arg, $db, &$model)
{
    $info = $db->getRow(knjm260aQuery::getStudentInfoData($model), DB_FETCHMODE_ASSOC);
    if (is_array($info)) {
        foreach ($info as $key => $val) {
            $setRow[$key] = $val;
        }
        $model->schregno = $info["SCHREGNO"];
        $model->grade = $info["GRADE"];
        $model->courseCd = $info["COURSECD"];
        $model->majorCd = $info["MAJORCD"];
        $model->courseCode = $info["COURSECODE"];
        $model->trCd1 = $info["TR_CD1"];
    }

    $arg["data"] = $setRow;

    //担当
    $staffname = $db->getOne(knjm260aQuery::getStaffMst($model->trCd1));
    $arg["data"]["STAFFNAME"] = $staffname;

}

//科目データ表示
function makeListInfo(&$objForm, &$arg, $db, &$model)
{
    //extraセット
    $extraClr    = " onchange=\"this.style.background='#ccffcc'\";";
    $extraInt    = " onblur=\"this.value=toInteger(this.value)\";";
    $extraRight  = " STYLE=\"text-align: right\"";

    $query = knjm260aQuery::getNameMst("M006");
    $result = $db->query($query);
    $optHyouka = array();
    $optHyouka[] = array('value' => "", 'label' => "");
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $optHyouka[] = array('value' => $row["VALUE"], 'label' => $row["LABEL"]);
    }
    $result->free();

    $model->subclassArray = array();
    $extra1 = $extraClr.$extraInt.$extraRight;
    $query = knjm260aQuery::getTestData($model);
    $result = $db->query($query);
    $setCheckHidden = "";
    $setCheckSep = "";
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");

        $model->subclassArray[] = $row["SUBCLASSCD"];

        $setCheckHidden .= $setCheckSep.$row["SUBCLASSCD"].":".$row["REPO_MAX_CNT"].":".$row["SUBCLASSNAME"];
        $setCheckSep = ",";

        $row["R_VAL1"] = knjCreateTextBox($objForm, $row["R_VAL1"], "R_VAL1_".$row["SUBCLASSCD"], 3, 3, $extra1);
        $row["R_VAL2"] = knjCreateTextBox($objForm, $row["R_VAL2"], "R_VAL2_".$row["SUBCLASSCD"], 3, 3, $extra1);
        $row["R_VAL3"] = knjCreateTextBox($objForm, $row["R_VAL3"], "R_VAL3_".$row["SUBCLASSCD"], 3, 3, $extra1);
        $row["T_VAL1"] = knjCreateCombo($objForm, "T_VAL1_".$row["SUBCLASSCD"], $row["T_VAL1"], $optHyouka, "", 1);

        if ($row["USE_MEDIA1"] == "1") {
            $row["IS_DVD"] = "あり";
            $extra = " class=\"changeColor\" data-name=\"S_VAL1_{$row["SUBCLASSCD"]}\" id=\"S_VAL1_{$row["SUBCLASSCD"]}\" ";
            $checked = $row["S_VAL1"] == "1" ? " checked " : "";
            $row["S_VAL1"] = knjCreateCheckBox($objForm, "S_VAL1_".$row["SUBCLASSCD"], "1", $checked.$extra);
        }

        $arg["testdata"][] = $row;
    }
    //hidden
    knjCreateHidden($objForm, "CHECK_MAX", $setCheckHidden);

    $result->free();
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $db) {

    $extraUp  = (AUTHORITY >= DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('update');\"" : "disabled";
    $extraRst = "onclick=\"return btn_submit('reset');\"";
    $extraEnd = "onclick=\"closeWin();\"";

    //更新
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extraUp);

    //取消
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extraRst);

    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extraEnd);

}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
}

?>
