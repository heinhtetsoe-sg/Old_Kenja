<?php

require_once('for_php7.php');

/*
 *　修正履歴
 *
 */
class knja143aForm1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knja143aForm1", "POST", "knja143aindex.php", "", "knja143aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        if ($model->Properties["useFormNameA143A"]) {
            $arg["data"]["SELFORMLABELSTR"] = "Ｂ５用紙";
        } else {
            $arg["data"]["SELFORMLABELSTR"] = "Ａ４用紙";
        }
        //フォーム選択(1:Ａ４用紙 2:カード)
        $opt_output[0]=1;
        $opt_output[1]=2;
        $model->field["OUTPUT"] = isset($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : "1";
        $extra = array("id=\"OUTPUT1\" onchange=\"chkOut2Print('1');\"", "id=\"OUTPUT2\" onchange=\"chkOut2Print('2');\"");
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_output, get_count($opt_output));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出力面選択(1:表 2:裏)
        $radioArray = array();
        $opt_output = array();
        $opt_output[0]=1;
        $opt_output[1]=2;
        $model->field["OUT2PRINT_SIDE"] = isset($model->field["OUT2PRINT_SIDE"]) ? $model->field["OUT2PRINT_SIDE"] : "1";
        $disableflg = $model->field["OUTPUT"] == "1" ? "disabled" : "";
        $extra = array("id=\"OUT2PRINT_SIDE1\" ".$disableflg, "id=\"OUT2PRINT_SIDE2\" ".$disableflg);
        $radioArray = knjCreateRadio($objForm, "OUT2PRINT_SIDE", $model->field["OUT2PRINT_SIDE"], $extra, $opt_output, get_count($opt_output));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //学期マスタ
        $query = knja143aQuery::getSemeMst(CTRL_YEAR, CTRL_SEMESTER);
        $Row_Mst = $db->getRow($query,DB_FETCHMODE_ASSOC);

        //年度
        // $objForm->ae(createHiddenAe("YEAR", $Row_Mst["YEAR"]));
        knjCreateHidden($objForm, "YEAR",  $Row_Mst["YEAR"]);
        $arg["data"]["YEAR"] = $Row_Mst["YEAR"];

        //学期
        // $objForm->ae(createHiddenAe("GAKKI", $Row_Mst["SEMESTER"]));
        knjCreateHidden($objForm, "GAKKI", $Row_Mst["SEMESTER"]);
        $arg["data"]["GAKKI"] = $Row_Mst["SEMESTERNAME"];

        //発行日
        if( !isset($model->field["TERM_SDATE"]) ) 
            $model->field["TERM_SDATE"] = str_replace("-","/",CTRL_DATE);
        $arg["data"]["TERM_SDATE"]=View::popUpCalendar($objForm,"TERM_SDATE",$model->field["TERM_SDATE"]);

        //学年
        $query = knja143aQuery::getSelectGrade($model);
        $grade_flg = false;
        $row1 = $gradeCd = array();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["GRADE"] == $row["VALUE"]) $grade_flg = true;
            $gradeCd[$row["VALUE"]] = $row["GRADE_CD"];
        }
        $result->free();
        if (!isset($model->field["GRADE"]) || !$grade_flg) {
            $model->field["GRADE"] = $row1[0]["value"];
        }
        $extra = "onchange=\"return btn_submit('grade');\"";
        $arg["data"]["GRADE"] = knjCreateCombo($objForm, "GRADE", $model->field["GRADE"], $row1, $extra, 1);

        //有効期限
        $monthDay = "-03-31";
        $tmpGrade = (3 <= (int)$gradeCd[$model->field["GRADE"]]) ? 3 : (int)$gradeCd[$model->field["GRADE"]];
        if ($model->field["OUTPUT"] == "1" && $model->Properties["useFormNameA143A"]) {
            $year = (int)CTRL_YEAR + 1;
        } else {
            $year = (int)CTRL_YEAR + (4 - (int)$tmpGrade);
        }
        $eDate = $year .$monthDay;
        if ($model->field["TERM_EDATE"] == "") $model->field["TERM_EDATE"] = str_replace("-","/",$eDate);
        $arg["data"]["TERM_EDATE"] = View::popUpCalendar($objForm,"TERM_EDATE",$model->field["TERM_EDATE"]);
        knjCreateHidden($objForm, "selectGradeCd", $tmpGrade);

        //クラス
        $query = knja143aQuery::getAuth(CTRL_YEAR, CTRL_SEMESTER, $model->field["GRADE"]);
        $class_flg = false;
        $row1 = array();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            if ($model->field["GRADE_HR_CLASS"] == $row["VALUE"]) $class_flg = true;
        }
        $result->free();

        if (!isset($model->field["GRADE_HR_CLASS"]) || !$class_flg) {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
        }

        $extra = "onchange=\"return btn_submit('knja143a');\"";
        $arg["data"]["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $row1, $extra, 1);

        //生徒一覧リスト
        $opt_right = array();
        $opt_left  = array();

        $selectStudent = ($model->selectStudent != "") ? explode(",",$model->selectStudent) : array();
        $selectStudentLabel = ($model->selectStudentLabel != "") ? explode(",",$model->selectStudentLabel) : array();

        for ($i = 0; $i < get_count($selectStudent); $i++) {
            $opt_left[] = array('label' => $selectStudentLabel[$i],
                                'value' => $selectStudent[$i]);
        }

        $query = knja143aQuery::getSchno($model, CTRL_YEAR, CTRL_SEMESTER);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if (!in_array($row["SCHREGNO"], $selectStudent)) {
                $opt_right[] = array('label' => $row["NAME"],
                                     'value' => $row["SCHREGNO"]);
            }
        }
        $result->free();

        //生徒一覧リスト
        $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", $opt_right, $extra, 20);

        //出力対象一覧リスト
        $extra = "multiple style=\"width:250px\" width:\"250px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", $opt_left, $extra, 20);

        //対象取消ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

        //対象選択ボタン（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

        //対象取消ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

        //対象選択ボタン（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        knjCreateHidden($objForm, "selectStudent");
        knjCreateHidden($objForm, "selectStudentLabel");

        //ボタンを作成する
        makeButton($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knja143aForm1.html", $arg); 
    }
}

//ボタン作成
function makeButton(&$objForm, &$arg, $model)
{
    //印刷ボタン
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJA143A");
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
    knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "useAddrField2" , $model->Properties["useAddrField2"]);
    knjCreateHidden($objForm, "useFormNameA143A", $model->Properties["useFormNameA143A"]);
}

?>
