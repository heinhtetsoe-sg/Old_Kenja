<?php

require_once('for_php7.php');

class knjz174form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjz174index.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //コピー元
        $query = knjz174Query::selectCopyQuery($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $model->field["COPY_KEY"], "COPY_KEY", $extra, 1, "BLANK");

        //月コンボ
        makeMonthSemeCmb($objForm, $arg, $db, $model);

        //年度
        $arg["sepa"]["YEAR"] = CTRL_YEAR."年度";

        //checkbox
        $extra = "id=\"ALL_CHECK\" onClick=\"allCheck(this)\"";
        $arg["ALL_CHECK"] = knjCreateCheckBox($objForm, "ALL_CHECK", "1", $extra);

        if ($model->Properties["useFi_Hrclass"] == '1') {
            //クラス区分ラジオボタン 1:法定 2:複式
            $opt = array(1, 2);
            $model->field["HR_CLASS_DIV"] = ($model->field["HR_CLASS_DIV"] == "") ? "1" : $model->field["HR_CLASS_DIV"];
            $click = " onClick=\"btn_submit('change')\";";
            $extra = array("id=\"HR_CLASS_DIV1\"".$click, "id=\"HR_CLASS_DIV2\"".$click);
            $radioArray = knjCreateRadio($objForm, "HR_CLASS_DIV", $model->field["HR_CLASS_DIV"], $extra, $opt, get_count($opt));
            foreach($radioArray as $key => $val) $arg[$key] = $val;
            $arg["hr_class_div"] = 1;
        } else {
            $model->field["HR_CLASS_DIV"] = ($model->field["HR_CLASS_DIV"] == "") ? "1" : $model->field["HR_CLASS_DIV"];
            knjCreateHidden($objForm, "HR_CLASS_DIV", 1);
        }

        //データ作成
        makeData($objForm, $arg, $db, $model);

        //ボタン作成
        makeButton($objForm, $arg);

        //hidden作成
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if($model->auth != DEF_UPDATABLE){
            $arg["Closing"] = " closing_window(); " ;
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz174Form1.html", $arg);
    }
}

//対象月コンボ作成
function makeMonthSemeCmb(&$objForm, &$arg, $db, &$model)
{
    $query      = knjz174Query::selectSemesAll();
    $result     = $db->query($query);
    $data       = array();
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $data[] = $row;
    }
    $result->free();

    $opt_month = setMonth($db, $data, $model);

    $arg["sepa"]["MONTHCD"] = knjCreateCombo($objForm, "MONTHCD", $model->field["MONTHCD"], $opt_month, "onChange=\"btn_submit('change')\";", 1);
}

//学期＋月データ取得
function setMonth($db, $data, &$model)
{
    $opt_month = array();
    $value_flg = false;
    list($ctY, $ctM, $ctD) = preg_split("/-/", CTRL_DATE);
    $defVal = $ctM."-".CTRL_SEMESTER;
    $defValFlg = false;
    $defVal2 = "";
    $defValFlg2 = false;
    for ($dcnt = 0; $dcnt < get_count($data); $dcnt++) {
        for ($i = $data[$dcnt]["S_MONTH"]; $i <= $data[$dcnt]["E_MONTH"]; $i++) {
            $month = $i;
            if ($i > 12) {
                $month = $i - 12;
            }
            $query = knjz174Query::selectMonthQuery($month, $model);
            $getdata = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (is_array($getdata)) {
                $opt_month[] = array("label" => $getdata["NAME1"]." (".$data[$dcnt]["SEMESTERNAME"].") ",
                                     "value" => $getdata["NAMECD2"]."-".$data[$dcnt]["SEMESTER"]);
                if ($model->field["MONTHCD"] == $getdata["NAMECD2"]."-".$data[$dcnt]["SEMESTER"]) $value_flg = true;
                if (!$defValFlg && $defVal == $getdata["NAMECD2"]."-".$data[$dcnt]["SEMESTER"]) {
                    $defValFlg = true;
                } else {
                    if (!$defValFlg2 && $getdata["NAMECD2"] == $ctM) {
                        $defVal2 = $getdata["NAMECD2"]."-".$data[$dcnt]["SEMESTER"];
                        $defValFlg2 = true;
                    }
                }
            }
        }
    }
    $setDefVal = $defVal2 ? $defVal2 : $opt_month[0]["value"];
    $setDefVal = $defValFlg ? $defVal : $setDefVal;
    $model->field["MONTHCD"] = ($model->field["MONTHCD"] && $value_flg) ? $model->field["MONTHCD"] : $setDefVal;
    return $opt_month;
}

function makeData(&$objForm, &$arg, $db, &$model)
{
    list($month, $seme) = preg_split("/-/", $model->field["MONTHCD"]);
    $year = $month < "04" ? CTRL_YEAR + 1 : CTRL_YEAR;
    $lastday = date("t", mktime( 0, 0, 0, $month, 1, $year ));

    $query = knjz174Query::selectData($model);
    $result = $db->query($query);
    $gradeHr = "";
    $sep = "";
    $lessonFlg = false;
    list ($target_month, $seme) = preg_split("/-/", $model->field["MONTHCD"]);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row["GRADE_HR"] = $row["HR_NAME"];
        if ($row["LESSON"]) {
            $lessonFlg = true;
        }
        $lesson = $row["LESSON"];
        $defLesson = 0;
        $setStyle = "";
        //日数計算
        if ($model->cmd == "defMonth") {
            $feDate = getFdateEdate($db, $target_month, $seme);

            //開始日付・終了日付
            $setYear = ($target_month * 1) < 4 ? CTRL_YEAR + 1 : CTRL_YEAR;
            $fDay = $setYear."-".sprintf('%02d', $target_month)."-".sprintf('%02d', $feDate["SDAY"]);
            $eDay = $setYear."-".sprintf('%02d', $target_month)."-".sprintf('%02d', $feDate["EDAY"]);

            //行事予定日数取得
            $query = knjz174Query::getHoliCnt($row["VALUE"], $seme, $fDay, $eDay, $model->field["HR_CLASS_DIV"]);
            $holiCnt = $db->getOne($query);

            //日数計算（月毎）
            $defLesson = $feDate["EDAY"] - $feDate["SDAY"] + 1 - $holiCnt;
            if ($model->fields["AUTO_CHECK"][$row["VALUE"]] == "1" && $model->cmd == "defMonth") {
                $lesson = $defLesson;
                $setStyle = " background-color : #ff0099 ";
            }
        }

        //textbox
        $extra = "STYLE=\"text-align: right; {$setStyle}\" onblur=\"this.value=toInteger(this.value);\"";
        $row["LESSON"] = knjCreateTextBox($objForm, $lesson, "LESSON".$row["VALUE"], 3, 3, $extra);

        //checkbox
        $model->fields["AUTO_CHECK"][$row["VALUE"]] = $model->fields["AUTO_CHECK"][$row["VALUE"]] ? $model->fields["AUTO_CHECK"][$row["VALUE"]] : "";
        $extra = $model->fields["AUTO_CHECK"][$row["VALUE"]] == "1" ? " checked " : "";
        $row["AUTO_CHECK"] = knjCreateCheckBox($objForm, "AUTO_CHECK".$row["VALUE"], "1", $extra);

        $arg["data"][] = $row;
        $gradeHr .= $sep.$row["VALUE"];
        $sep = ",";
    }
    //hidden
    knjCreateHidden($objForm, "H_GRADEHR", $gradeHr);
    knjCreateHidden($objForm, "lessonFlg", $lessonFlg);

    $result->free();
}

function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["sepa"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

function getFdateEdate($db, $month, $seme) {
    $query = knjz174Query::getLessonDay($month, $seme);
    $lessonDay = $db->getRow($query, DB_FETCHMODE_ASSOC);
    if ($lessonDay["SDAY_FLG"] == "1") {
        $sDay = $lessonDay["SDAY"];
    } else {
        $sDay = 1;
    }
    if ($lessonDay["EDAY_FLG"] == "1") {
        $eDay = $lessonDay["EDAY"];
    } else {
        $eDay = $lessonDay["DAY_MAX"];
    }

    return array("SDAY" => $sDay, "EDAY" => $eDay);
}

//ボタン作成
function makeButton(&$objForm, &$arg)
{

    //デフォルト
    $extra = "onclick=\"return btn_submit('defMonth');\"";
    $arg["button"]["DEF_MONTH_BT"] = knjCreateBtn($objForm, "DEF_MONTH_BT", "行事から\n日数計算(月毎)", $extra);

    //コピーボタン
    $extra = "onclick=\"return btn_submit('copy');\"";
    $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "コピー元をコピー", $extra);

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //取消ボタン
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタン
    $extra = "onclick=\"return closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "CT_YEAR", CTRL_YEAR);
}

?>
