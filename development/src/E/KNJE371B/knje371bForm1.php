<?php
class knje371bForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knje371bindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //対象年度
        $arg["YEAR_SHOW"] = CTRL_YEAR.'年度';

        //学校名
        $schoolAllName = knje371bQuery::getSchoolAllName($db, $model);
        $arg["SCHOOLNAME_SHOW"] = (strlen(trim($schoolAllName)) > 0) ? "学校名:".$schoolAllName : "";

        /***一覧リスト***/
        $query = knje371bQuery::getListMain($model, $model->year, "list");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学校名リンク
            $aHash = array("cmd"            => "main_edit",
                           "SCHOOL_CD"      => $row["SCHOOL_CD"],
                           "FACULTYCD"      => $row["FACULTYCD"],
                           "DEPARTMENTCD"   => $row["DEPARTMENTCD"]);
            $row["SCHOOL_NAME"] = View::alink("knje371bindex.php", $row["SCHOOL_NAME"], "", $aHash);
            $departmentAllFlg1 = ($row["DEPARTMENTCD"] == sprintf("%03d", "")) ? true : false;
            $row["DEPARTMENTNAME"]  = !$departmentAllFlg1 ? $row["DEPARTMENTNAME"] : "※全学科対象";


            //要件チェック
            $row["COURSE_CONDITION_FLG"]    = ($row["COURSE_CONDITION_FLG"] == "1") ? "レ" : "";
            $row["SUBCLASS_CONDITION_FLG"]  = ($row["SUBCLASS_CONDITION_FLG"] == "1") ? "レ" : "";
            $row["SUBCLASS_NUM"]            = ($row["SUBCLASS_NUM"] >= 2) ? "{$row["SUBCLASS_NUM"]}科目" : "1科目"; 
            $row["QUALIFIED_CONDITION_FLG"] = ($row["QUALIFIED_CONDITION_FLG"] == "1") ? "レ" : "";

            $arg["list"][] = $row;
        }

        /***入力欄***/
        //警告メッセージを表示しない場合
        if (!isset($model->warning) && ($model->cmd == "main_edit" || $model->cmd == "main_reset")){
            $Row = $db->getRow(knje371bQuery::getListMain($model, $model->year, "link"), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //学校・学部・学科コード
        $departmentAllFlg2 = ($Row["DEPARTMENTCD"] == sprintf("%03d", "")) ? true : false;
        $Row["DEPARTMENTCD_DISP"] = (!$departmentAllFlg2) ? $Row["DEPARTMENTCD"] : "";

        $extra = "";
        $arg["data"]["SCHOOL_CD"]     = knjCreateTextBox($objForm,  $Row["SCHOOL_CD"],    "SCHOOL_CD", 8, 8, $extra);
        $arg["data"]["FACULTYCD"]     = knjCreateTextBox($objForm,  $Row["FACULTYCD"],    "FACULTYCD", 3, 3, $extra);
        $arg["data"]["DEPARTMENTCD"]  = knjCreateTextBox($objForm,  $Row["DEPARTMENTCD_DISP"], "DEPARTMENTCD", 3, 3, $extra);

        if ($model->cmd == "main_kakutei") {
            $Row["SCHOOL_NAME"] = $db->getOne(knje371bQuery::getSchoolName($Row["SCHOOL_CD"]));
            $Row["FACULTYNAME"] = $db->getOne(knje371bQuery::getFacultyName($Row["SCHOOL_CD"], $Row["FACULTYCD"]));
            $Row["DEPARTMENTNAME"] = $db->getOne(knje371bQuery::getDepartmentName($Row["SCHOOL_CD"], $Row["FACULTYCD"], $Row["DEPARTMENTCD"]));
        }

        $arg["data"]["SCHOOL_NAME"]     = $Row["SCHOOL_NAME"];
        $arg["data"]["FACULTYNAME"]     = $Row["FACULTYNAME"];
        $arg["data"]["DEPARTMENTNAME"]  = !$departmentAllFlg2 ? $Row["DEPARTMENTNAME"] : "※全学科対象";

        //コース有無チェック
        $extra = " id=\"COURSE_CONDITION_FLG\" ";
        $extra .= ($Row["COURSE_CONDITION_FLG"]) ? " checked " : "";
        $arg["data"]["COURSE_CONDITION_FLG"] = knjCreateCheckBox($objForm, "COURSE_CONDITION_FLG", 1, $extra);

        //履修科目チェック
        $extra = " id=\"SUBCLASS_CONDITION_FLG\" ";
        $extra .= ($Row["SUBCLASS_CONDITION_FLG"]) ? " checked " : "";
        $arg["data"]["SUBCLASS_CONDITION_FLG"] = knjCreateCheckBox($objForm, "SUBCLASS_CONDITION_FLG", 1, $extra);

        //資格チェック
        $extra = " id=\"QUALIFIED_CONDITION_FLG\" ";
        $extra .= ($Row["QUALIFIED_CONDITION_FLG"]) ? " checked " : "";
        $arg["data"]["QUALIFIED_CONDITION_FLG"] = knjCreateCheckBox($objForm, "QUALIFIED_CONDITION_FLG", 1, $extra);

        //ボタン作成
        makeRegisterBtn($objForm, $arg, $model, $Row);
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        $arg["IFRAME"] = View::setIframeJs();

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knje371bForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space="")
{
    $opt = array();
    if($space) $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) $value_flg = true;
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeRegisterBtn(&$objForm, &$arg, $model, $Row)
{
    $disable = ($model->cmd != "main_edit" || ($Row["SCHOOL_CD"] == "" || $Row["FACULTYCD"] == "" || $Row["DEPARTMENTCD"] == "")) ? " disabled " : "";

    $link  = REQUESTROOT."/E/KNJE371B/knje371bindex.php";
    $link .= "?SCHOOL_CD={$Row["SCHOOL_CD"]}&FACULTYCD={$Row["FACULTYCD"]}&DEPARTMENTCD={$Row["DEPARTMENTCD"]}";

    //履修コース要件ボタン
    $extra = "onclick=\"parent.location.href='{$link}&cmd=subCourse_edit';\"";
    $arg["button"]["btn_course"] = knjCreateBtn($objForm, "btn_course", "登録画面", $extra.$disable);
    //履修科目要件ボタン
    $extra = "onclick=\"parent.location.href='{$link}&cmd=subSubclass';\"";
    $arg["button"]["btn_subclass"] = knjCreateBtn($objForm, "btn_subclass", "登録画面", $extra.$disable);
    //資格要件ボタン
    $extra = "onclick=\"parent.location.href='{$link}&cmd=subQualified';\"";
    $arg["button"]["btn_qualified"] = knjCreateBtn($objForm, "btn_qualified", "登録画面", $extra.$disable);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //前年度コピーボタン
    $extra = "onclick=\"return btn_submit('main_copy');\"";
    $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

    //学校検索ボタン
    $extra = "onclick=\"loadwindow('" .REQUESTROOT."/X/KNJXSEARCH_COLLEGE/knjxcol_searchindex.php?cmd=',event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 600)\"";
    $arg["button"]["btn_schsearch"] = knjCreateBtn($objForm, "btn_schsearch", "学校検索", $extra);

    //確定ボタンを作成する
    $extra = "onclick=\"return btn_submit('main_kakutei');\"";
    $arg["button"]["btn_kakutei"] = knjCreateBtn($objForm, "btn_kakutei", "確 定", $extra);

    //追加ボタンを作成する
    $extra = "onclick=\"return btn_submit('main_insert');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);
    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('main_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタンを作成する
    $extra = "onclick=\"return btn_submit('main_delete');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    //取消しボタンを作成する
    $extra = "onclick=\"return btn_submit('main_reset')\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJE371B");
    knjCreateHidden($objForm, "SCHOOL_NAME", $Row["SCHOOL_NAME"]);
    knjCreateHidden($objForm, "FACULTYNAME", $Row["FACULTYNAME"]);
    //knjCreateHidden($objForm, "DEPARTMENTCD", $Row["DEPARTMENTCD"]);
    knjCreateHidden($objForm, "DEPARTMENTNAME", $Row["DEPARTMENTNAME"]);
    knjCreateHidden($objForm, "SUBCLASS_NUM", $Row["SUBCLASS_NUM"]);
}

?>
