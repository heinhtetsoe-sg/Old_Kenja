<?php

require_once('for_php7.php');

class knjd186dForm1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjd186dForm1", "POST", "knjd186dindex.php", "", "knjd186dForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjd186dQuery::getSemester();
        $extra = "onchange=\"return btn_submit('knjd186d'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //年組コンボ作成
        $opt = array();
        $arr_trcd = array();
        $value_flg = false;
        $query = knjd186dQuery::gethrName($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
            $arr_trcd[$row["VALUE"]] = $row["TR_CD1"];
            if ($model->field["GRADE_HR_CLASS"] == $row["VALUE"]) $value_flg = true;
        }
        $result->free();
        $model->field["GRADE_HR_CLASS"] = ($model->field["GRADE_HR_CLASS"] && $value_flg) ? $model->field["GRADE_HR_CLASS"] : $opt[0]["value"];

        $extra = "onchange=\"return btn_submit('knjd186d'),AllClearList();\"";
        $arg["data"]["GRADE_HR_CLASS"] = knjCreateCombo($objForm, "GRADE_HR_CLASS", $model->field["GRADE_HR_CLASS"], $opt, $extra, 1);

        //対象者リストを作成する
        $query = knjd186dQuery::getSchno(CTRL_YEAR, $model->field["SEMESTER"], $model->field["GRADE_HR_CLASS"]);
        $result = $db->query($query);
        $opt_right = $opt_left = array();
        $selectdata = ($model->selectdata) ? explode(',', $model->selectdata) : array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if (in_array($row["SCHREGNO"], $selectdata)) {
                $opt_left[] = array('label' => $row["SCHREGNO"]."　".$row["ATTENDNO"]."番"."　".$row["NAME_SHOW"],
                                    'value' => $row["SCHREGNO"]);
            } else {
                $opt_right[] = array('label' => $row["SCHREGNO"]."　".$row["ATTENDNO"]."番"."　".$row["NAME_SHOW"],
                                     'value' => $row["SCHREGNO"]);
            }
        }
        $result->free();

        //生徒一覧リストを作成する
        $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('left')\"";
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "category_name", "", $opt_right, $extra, 20);

        //出力対象一覧リストを作成する
        $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('right')\"";
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "category_selected", "", $opt_left, $extra, 20);

        //出欠集計開始日付（SDATE）
        $query = knjd186dQuery::getSdate();
        $sDate = $db->getOne($query);
        if ($model->field["SDATE"] == "") $model->field["SDATE"] = str_replace("-", "/", $sDate);
        $arg["data"]["SDATE"] = View::popUpCalendar($objForm, "SDATE", $model->field["SDATE"]);

        //出欠集計終了日付（EDATE）
        if ($model->field["EDATE"] == "") $model->field["EDATE"] = str_replace("-", "/", CTRL_DATE);
        $arg["data"]["EDATE"] = View::popUpCalendar($objForm, "EDATE", $model->field["EDATE"]);

        //記載日付
        $query = knjd186dQuery::getSemestercnt();
        $semesCnt = $db->getOne($query);
        if ($model->field["SEMESTER"] !== $semesCnt && $semesCnt !== "1") $disabled = " disabled";
        if ($model->field["DESC_DATE"] == "") $model->field["DESC_DATE"] = str_replace("-", "/", CTRL_DATE);
        $arg["data"]["DESC_DATE"] = View::popUpCalendarAlp($objForm, "DESC_DATE", $model->field["DESC_DATE"], $disabled, "");

        /********/
        /*ボタン*/
        /********/
        //対象選択ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);

        //対象取消ボタンを作成する（全部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);

        //対象選択ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);

        //対象取消ボタンを作成する（一部）
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);

        //印刷ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する(必須)
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "PRGID", "KNJD186D");
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "TR_CD1", $arr_trcd[$model->field["GRADE_HR_CLASS"]]);
        knjCreateHidden($objForm, "IMAGEPATH", $model->control["LargePhotoPath"]);
        knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CHECKS", $model->field["CHECKS"]);
        knjCreateHidden($objForm, "HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_P", $model->Properties["HREPORTREMARK_DAT_ATTENDREC_REMARK_SIZE_P"]);
        knjCreateHidden($objForm, "HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_P", $model->Properties["HREPORTREMARK_DAT_TOTALSTUDYTIME_SIZE_P"]);
        knjCreateHidden($objForm, "HREPORTREMARK_DAT_REMARK1_SIZE_P", $model->Properties["HREPORTREMARK_DAT_REMARK1_SIZE_P"]);
        knjCreateHidden($objForm, "HREPORTREMARK_DAT_COMMUNICATION_SIZE_P", $model->Properties["HREPORTREMARK_DAT_COMMUNICATION_SIZE_P"]);
        knjCreateHidden($objForm, "RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYTIME_SIZE_P", $model->Properties["RECORD_TOTALSTUDYTIME_DAT_TOTALSTUDYTIME_SIZE_P"]);
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "knjdBehaviorsd_UseText_P", $model->Properties["knjdBehaviorsd_UseText_P"]);
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        knjCreateHidden($objForm, "use_school_detail_gcm_dat", $model->Properties["use_school_detail_gcm_dat"]);
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
        knjCreateHidden($objForm, "use_prg_schoolkind", $model->Properties["use_prg_schoolkind"]);
        knjCreateHidden($objForm, "selectSchoolKind", $model->selectSchoolKind);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd186dForm1.html", $arg); 
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
