<?php

require_once('for_php7.php');

class knje360jShinro_2
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("shinro_2", "POST", "knje360jindex.php", "", "shinro_2");

        //DB接続
        $db = Query::dbCheckOut();

        //進路情報取得
        if ($model->cmd == "replace1") {
            if (isset($model->schregno) && isset($model->seq) && !isset($model->warning)) {
                $Row = $db->getRow(knje360jQuery::getSubQuery1($model, $model->entrydate), DB_FETCHMODE_ASSOC);
            } else {
                $Row =& $model->replace["field"];
            }
        } else {
            $Row =& $model->replace["field"];
        }

        //編集項目選択
        for ($i=0; $i<10; $i++) {
            $extra  = ($model->replace["check"][$i] == "1") ? "checked" : "";
            if ($i==9) {
                $extra .= " onClick=\"return check_all(this);\"";
            }

            $arg["data"]["RCHECK".$i] = knjCreateCheckBox($objForm, "RCHECK".$i, "1", $extra, "");
        }

        //登録日
        $Row["ENTRYDATE"] = ($Row["ENTRYDATE"] == "") ? str_replace("-", "/", CTRL_DATE) : str_replace("-", "/", $Row["ENTRYDATE"]);
        $arg["data"]["ENTRYDATE"] = View::popUpCalendar($objForm, "ENTRYDATE", $Row["ENTRYDATE"]);

        //調査名
        $query = knje360jQuery::getQuestionnaireList();
        makeCmb($objForm, $arg, $db, $query, "QUESTIONNAIRECD", $Row["QUESTIONNAIRECD"], "", 1, 1);

        /**第一希望**/
        //学校系列
        $query = knje360jQuery::getNameMst('E012');
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_GROUP1", $Row["SCHOOL_GROUP1"], "", 1, 1);

        //学部系列
        $query = knje360jQuery::getFacultyGroup();
        makeCmb($objForm, $arg, $db, $query, "FACULTY_GROUP1", $Row["FACULTY_GROUP1"], "", 1, 1);

        //学科系列
        $query = knje360jQuery::getDepartmentGroup();
        makeCmb($objForm, $arg, $db, $query, "DEPARTMENT_GROUP1", $Row["DEPARTMENT_GROUP1"], "", 1, 1);

        //学校情報
        $college1 = $db->getRow(knje360jQuery::getFinSchoolInfo($Row["FINSCHOOLCD1"]), DB_FETCHMODE_ASSOC);

        //学校コード
        $arg["data"]["FINSCHOOLCD1"] = $college1["FINSCHOOLCD"];

        //学校名
        $arg["data"]["SCHOOL_NAME1"] = $college1["SCHOOL_NAME"];

        //学部名
        $arg["data"]["FACULTYNAME1"] = $college1["FACULTYNAME"];

        //受験区分
        $query = knje360jQuery::getNameMst('E002');
        makeCmb($objForm, $arg, $db, $query, "HOWTOEXAM1", $Row["HOWTOEXAM1"], "", 1, 1);


        /**第二希望**/
        //学校系列
        $query = knje360jQuery::getNameMst('E012');
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_GROUP2", $Row["SCHOOL_GROUP2"], "", 1, 1);

        //学部系列
        $query = knje360jQuery::getFacultyGroup();
        makeCmb($objForm, $arg, $db, $query, "FACULTY_GROUP2", $Row["FACULTY_GROUP2"], "", 1, 1);

        //学科系列
        $query = knje360jQuery::getDepartmentGroup();
        makeCmb($objForm, $arg, $db, $query, "DEPARTMENT_GROUP2", $Row["DEPARTMENT_GROUP2"], "", 1, 1);

        //学校情報
        $college2 = $db->getRow(knje360jQuery::getFinSchoolInfo($Row["FINSCHOOLCD2"]), DB_FETCHMODE_ASSOC);

        //学校コード
        $arg["data"]["FINSCHOOLCD2"] = $college2["FINSCHOOLCD"];

        //学校名
        $arg["data"]["SCHOOL_NAME2"] = $college2["SCHOOL_NAME"];

        //学部名
        $arg["data"]["FACULTYNAME2"] = $college2["FACULTYNAME"];

        //受験区分
        $query = knje360jQuery::getNameMst('E002');
        makeCmb($objForm, $arg, $db, $query, "HOWTOEXAM2", $Row["HOWTOEXAM2"], "", 1, 1);


        //クラス情報
        $hr_name = $db->getOne(knje360jQuery::getHrName($model));
        $arg["HR_CLASSINFO"] = CTRL_YEAR.'年度　　'.CTRL_SEMESTERNAME.'　　対象クラス：'.$hr_name;

        //生徒リストToリスト作成
        makeStudentList($objForm, $arg, $db, $model);

        //生徒項目名切替
        $arg["SCH_LABEL"] = $model->sch_label;

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje360jShinro_2.html", $arg);
    }
}

//生徒リストToリスト作成
function makeStudentList(&$objForm, &$arg, $db, $model)
{
    //置換処理選択時の生徒の情報
    $array = explode(",", $model->replace["selectdata"]);
    if ($array[0] == "") {
        $array[0] = $model->schregno;
    }

    //生徒一覧取得
    $result = $db->query(knje360jQuery::getStudent($model));
    $opt_left = $opt_right = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (!in_array($row["SCHREGNO"], $array)) {
            $opt_right[]  = array("label" => $row["ATTENDNO"]."番 ".$row["NAME_SHOW"],
                                  "value" => $row["SCHREGNO"]);
        } else {
            $opt_left[]   = array("label" => $row["ATTENDNO"]."番 ".$row["NAME_SHOW"],
                                  "value" => $row["SCHREGNO"]);
        }
    }
    $result->free();

    //生徒一覧リストを作成する//
    $extra = "multiple style=\"width:170px\" width=\"170px\" ondblclick=\"move('left','left_select','right_select',1)\"";
    $arg["data"]["RIGHT_PART"] = knjCreateCombo($objForm, "right_select", "", $opt_right, $extra, 20);

    //対象者一覧リストを作成する//
    $extra = "multiple style=\"width:170px\" width=\"170px\" ondblclick=\"move('right','left_select','right_select',1)\"";
    $arg["data"]["LEFT_PART"] = knjCreateCombo($objForm, "left_select", "", $opt_left, $extra, 20);

    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:30px\" onclick=\"return move('sel_add_all','left_select','right_select',1);\"";
    $arg["button"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "<<", $extra);
    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:30px\" onclick=\"return move('left','left_select','right_select',1);\"";
    $arg["button"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:30px\" onclick=\"return move('left','left_select','right_select',1);\"";
    $arg["button"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:30px\" onclick=\"return move('sel_del_all','left_select','right_select',1);\"";
    $arg["button"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", ">>", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space="")
{
    $opt = array();
    if ($space) {
        $opt[] = array('label' => "", 'value' => "");
    }
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) {
            $value_flg = true;
        }
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //学校検索ボタンを作成する
    $extra = "onclick=\"loadwindow('" .REQUESTROOT."/X/KNJXSEARCH_COLLEGE/knjxcol_searchindex.php?cmd=&target_number=1',event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 600)\"";
    $arg["button"]["btn_schsearch1"] = knjCreateBtn($objForm, "btn_schsearch1", "学校検索", $extra);
    $extra = "onclick=\"loadwindow('" .REQUESTROOT."/X/KNJXSEARCH_COLLEGE/knjxcol_searchindex.php?cmd=&target_number=2',event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 600)\"";
    $arg["button"]["btn_schsearch2"] = knjCreateBtn($objForm, "btn_schsearch2", "学校検索", $extra);

    //更新ボタンを作成する
    $extra = "onclick=\"return doSubmit()\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //戻るボタン
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", "onclick=\"return btn_submit('shinroA');\"");
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");

    knjCreateHidden($objForm, "COURSE_KIND", '1');

    knjCreateHidden($objForm, "FINSCHOOLCD1", $Row["FINSCHOOLCD1"]);
    knjCreateHidden($objForm, "FACULTYCD1", $Row["FACULTYCD1"]);
    knjCreateHidden($objForm, "DEPARTMENTCD1", $Row["DEPARTMENTCD1"]);
    knjCreateHidden($objForm, "FINSCHOOLCD2", $Row["FINSCHOOLCD2"]);
    knjCreateHidden($objForm, "FACULTYCD2", $Row["FACULTYCD2"]);
    knjCreateHidden($objForm, "DEPARTMENTCD2", $Row["DEPARTMENTCD2"]);

    $semes = $db->getRow(knje360jQuery::getSemesterMst(), DB_FETCHMODE_ASSOC);
    knjCreateHidden($objForm, "SDATE", str_replace("-", "/", $semes["SDATE"]));
    knjCreateHidden($objForm, "EDATE", str_replace("-", "/", $semes["EDATE"]));
}
?>

