<?php

require_once('for_php7.php');

class knjd620qForm1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd620qForm1", "POST", "knjd620qindex.php", "", "knjd620qForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjd620qQuery::getSemester(0);
        $extra = "onchange=\"return btn_submit('knjd620q');\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);
        //学年末は今学期とする
        $seme = $model->field["SEMESTER"] == "9" ? CTRL_SEMESTER : $model->field["SEMESTER"];

        //学科名コンボ
        $query = knjd620qQuery::getCourseMajor($model);
        $extra = "onchange=\"return btn_submit('knjd620q');\"";
        makeCmb($objForm, $arg, $db, $query, "MAJOR", $model->field["MAJOR"], $extra, 1);

        //学年コンボ作成
        $query = knjd620qQuery::getGrade($seme, $model);
        $extra = "onchange=\"return btn_submit('knjd620q');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //テストコンボ作成
        $query = knjd620qQuery::getTest($model, $model->field["SEMESTER"], $model->field["GRADE"]);
        $extra = "onchange=\"return btn_submit('knjd620q');\"";
        makeCmb($objForm, $arg, $db, $query, "TESTKINDCD", $model->field["TESTKINDCD"], $extra, 1);

        //出欠集計日付作成
        $model->yearSdate = str_replace("-", "/", $db->getOne(knjd620qQuery::getYearSdate()));
        if ($model->field["SDATE"] == "") {
            $model->field["SDATE"] = $model->yearSdate;
        }
        $arg["data"]["SDATE"] = View::popUpCalendar($objForm, "SDATE", $model->field["SDATE"]);

        $model->field["DATE"] = $model->field["DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["DATE"];
        $arg["data"]["DATE"] = View::popUpCalendar($objForm, "DATE", $model->field["DATE"]);

        //欠点
        if (($model->Properties["checkKettenDiv"] != '1' && $model->Properties["checkKettenDiv"] != '2') || 
            ($model->Properties["checkKettenDiv"] == '1')) {
            $arg["KETTEN_FLG"] = '1'; //null以外なら何でもいい
        } else {
            unset($arg["KETTEN_FLG"]);
        }

        //欠点
        $query = knjd620qQuery::getGdat($model->field["GRADE"]);
        $h_j = $db->getOne($query);
        if ($model->Properties["checkKettenDiv"] == '1' && $model->field["SEMESTER"] == '9' && $model->field["TESTKINDCD"]== '990009') {
            $model->field["KETTEN"] = 1;
        } else {
            $model->field["KETTEN"] = 29;
        }
        $extra = " style=\"text-align: right;\"  onblur=\"calc(this);\"";
        $arg["data"]["KETTEN"] = knjCreateTextBox($objForm, $model->field["KETTEN"], "KETTEN", 3, 3, $extra);

        //ボタン作成
        //ＣＳＶ出力ボタンを作成する
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'csv');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "ＣＳＶ出力", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJD620Q");
        knjCreateHidden($objForm, "cmd");
        //日付範囲チェック用
        if($model->field["SEMESTER"] == '9'){
            $sseme = $model->control["学期開始日付"][9];
            $eseme = $model->control["学期終了日付"][9];
            $semeflg = CTRL_SEMESTER;
        } else {
            $sseme = $model->control["学期開始日付"][$seme];
            $eseme = $model->control["学期終了日付"][$seme];
            $semeflg = $model->field["SEMESTER"];
        }
        knjCreateHidden($objForm, "YEAR_SDATE", $model->yearSdate);
        knjCreateHidden($objForm, "SEME_SDATE", $sseme);
        knjCreateHidden($objForm, "SEME_EDATE", $eseme);
        knjCreateHidden($objForm, "SEME_FLG", $semeflg);
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
        knjCreateHidden($objForm, "useClassDetailDat", $model->Properties["useClassDetailDat"]);
        knjCreateHidden($objForm, "useVirus", $model->Properties["useVirus"]);
        knjCreateHidden($objForm, "useKekkaJisu", $model->Properties["useKekkaJisu"]);
        knjCreateHidden($objForm, "useKekka", $model->Properties["useKekka"]);
        knjCreateHidden($objForm, "useLatedetail", $model->Properties["useLatedetail"]);
        knjCreateHidden($objForm, "useKoudome", $model->Properties["useKoudome"]);
        knjCreateHidden($objForm, "use_SchregNo_hyoji", $model->Properties["use_SchregNo_hyoji"]);
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
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
        View::toHTML($model, "knjd620qForm1.html", $arg); 
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
