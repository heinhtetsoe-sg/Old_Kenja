<?php

require_once('for_php7.php');


class knjg030Form1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成////////////////////////////////////////////////////////////////////////////////////////////////////////////////
        $arg["start"]   = $objForm->get_start("knjg030Form1", "POST", "knjg030index.php", "", "knjg030Form1");


        $arg["data"]["DATE"]=View::popUpCalendar($objForm,"DATE",isset($model->field["DATE"])?$model->field["DATE"]:$model->control["学籍処理日"]);

        //学期（カレンダーの日付より取得・デフォルトは現在の学期）/////////////////////////////////////////////////////////
        //$seme = isset($model->field["SEMESTER"])?$model->field["SEMESTER"]:$model->control["学期"];


        //年度テキストボックスを作成する///////////////////////////////////////////////////////////////////////////////////

        $arg["data"]["YEAR"] = $model->control["年度"];

        knjCreateHidden($objForm, "YEAR", $model->control["年度"]);
        $db = Query::dbCheckOut();

        //学期コンボボックスを作成する///////////////////////////////////////////////////////////////////////////////
        $query = knjg030Query::getSemester($model->control["年度"]);
        $row2 = array();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row2[]= array('label' => $row["SEMESTERNAME"],
                            'value' => $row["SEMESTER"]);
        }
        $result->free();

        if ($model->field["SEMESTER"]=="") {
            $model->field["SEMESTER"] = $model->control["学期"];
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "SEMESTER",
                            "size"       => "1",
                            "value"      => $model->field["SEMESTER"],
                            "extrahtml"  => "onchange=\"return btn_submit('knjg030');\"",
                            "options"    => $row2));

        $arg["data"]["SEMESTER"] = $objForm->ge("SEMESTER");

        //クラス選択コンボボックスを作成する///////////////////////////////////////////////////////////////////////////////

        $query = knjg030Query::getAuth($model->control["年度"],$model->field["SEMESTER"], $model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
        $result->free();

        if ($model->field["GRADE_HR_CLASS"]=="") {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "GRADE_HR_CLASS",
                            "size"       => "1",
                            "value"      => $model->field["GRADE_HR_CLASS"],
                            "extrahtml"  => "onchange=\"return btn_submit('knjg030');\"",
        //                  "extrahtml"  => "onchange=\"AllClearList();\"",
                            "options"    => isset($row1)?$row1:array()));

        $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");

        //対象者リストを作成する/////////////////////////////////////////////////////////////////////////////////////////////
        $query = "SELECT SCHREG_REGD_DAT.SCHREGNO AS SCHREGNO,
        SCHREG_REGD_DAT.SCHREGNO || '　' || ATTENDNO || '番' || '　' || NAME_SHOW AS NAME ".
                    "FROM SCHREG_BASE_MST INNER JOIN SCHREG_REGD_DAT ON SCHREG_BASE_MST.SCHREGNO = SCHREG_REGD_DAT.SCHREGNO ".
                    "WHERE (((SCHREG_REGD_DAT.YEAR)='" .$model->control["年度"] ."') AND ".
                    "((SCHREG_REGD_DAT.SEMESTER)='" .$model->field["SEMESTER"] ."') AND ".
                    "((SCHREG_REGD_DAT.GRADE || SCHREG_REGD_DAT.HR_CLASS)='" .$model->field["GRADE_HR_CLASS"] ."'))". 
                    "ORDER BY ATTENDNO";
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt1[]= array('label' =>  $row["NAME"],
                            'value' => $row["SCHREGNO"]);
        }
        $result->free();

        $objForm->ae( array("type"       => "select",
                            "name"       => "category_name",
                            "extrahtml"  => "multiple style=\"width:230px\" ondblclick=\"move1('left')\"",
                            "size"       => "20",
                            "options"    => isset($opt1)?$opt1:array()));

        $arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");


        //生徒一覧リストを作成する/////////////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type"       => "select",
                            "name"       => "category_selected",
                            "extrahtml"  => "multiple style=\"width:230px\" ondblclick=\"move1('right')\"",
                            "size"       => "20",
                            "options"    => array()));

        $arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");



        //対象選択ボタンを作成する（全部）/////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_rights",
                            "value"       => ">>",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ) );

        $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");


        //対象取消ボタンを作成する（全部）//////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_lefts",
                            "value"       => "<<",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );

        $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");


        //対象選択ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right1",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right');\"" ) );

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");


        //対象取消ボタンを作成する（一部）//////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left1",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ) );

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");


        //印刷ボタンを作成する///////////////////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");


        //終了ボタンを作成する//////////////////////////////////////////////////////////////////////////////////////////////
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //記載責任者コンボボックス
        if ($model->Properties["tannishutokushoumeishoKisaisekininsha"] == "1") {
            $arg["kisaisekininsha"] = "1";
            $query = knjg030Query::getStaffList();
            if ($model->cmd == '') {
                $model->field["SEKI"] = STAFFCD;
            }
            makeCmb($objForm, $arg, $db, $query, "SEKI", $model->field["SEKI"], "", 1, $model);
        }
        Query::dbCheckIn($db);

        //hiddenを作成する(必須)/////////////////////////////////////////////////////////////////////////////////////////////

        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJG030");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useGakkaSchoolDiv", $model->Properties["useGakkaSchoolDiv"]);
        knjCreateHidden($objForm, "tannishutokushoumeishoNotPrintAnotherStudyrec", $model->Properties["tannishutokushoumeishoNotPrintAnotherStudyrec"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "certifPrintRealName", $model->Properties["certifPrintRealName"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "tannishutokushoumeishoPrintCoursecodename", $model->Properties["tannishutokushoumeishoPrintCoursecodename"]);
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "cmd");

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();


        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjg030Form1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    list($simo, $fuseji) = explode(" | ", $model->Properties["showMaskStaffCd"]);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        if ($name == "SEKI") {
            $ume = "" ;
            for ($umecnt = 1; $umecnt <= strlen($row["VALUE"]) - (int)$simo; $umecnt++) {
                $ume .= $fuseji;
            }
            if ($fuseji) {
                $SET_VALUE = $ume.substr($row["VALUE"], (strlen($row["VALUE"]) - (int)$simo), (int)$simo);
            } else {
                $SET_VALUE = $row["VALUE"];
            }
            $row["LABEL"] = str_replace($row["VALUE"], $SET_VALUE, $row["LABEL"]);
        }

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEKI") {
        $value = ($value && $value_flg) ? $value : STAFFCD;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}


?>
