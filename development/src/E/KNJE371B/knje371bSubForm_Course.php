<?php
class knje371bSubForm_Course
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knje371bindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //対象年度
        $arg["YEAR_SHOW"] = $model->year.'年度';

        //学校名
        $schoolAllName = knje371bQuery::getSchoolAllName($db, $model);
        $arg["SCHOOLNAME_SHOW"] = (strlen(trim($schoolAllName)) > 0) ? "学校名:".$schoolAllName : "";

        /***一覧リスト***/
/*
        $query = knje371bQuery::getList2($model, "link");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //学校名リンク
            $aHash = array("cmd"            => "subCourse_main",
                           "COURSECODE"     => $row["COURSECD"] || $row["MAJORCD"] || $row["COURSECODE"]);

            $row["COURSENAME"] = View::alink("knje371bindex.php", $row["COURSENAME"], "", $aHash);

            $arg["list"][] = $row;
        }
*/
        /***入力欄***/
        //警告メッセージを表示しない場合
        if (!isset($model->warning) && ($model->cmd == "subCourse_edit" || $model->cmd == "subCourse_reset")){
            $Row = $db->getRow(knje371bQuery::getList2($model, "list"), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //履修コースコンボ
        $extra = "";
        $query = knje371bQuery::getCourseCode($model);
        makeCmb($objForm, $arg, $db, $query, "COURSECODE", $Row["COURSECODE"], $extra, 1, "blank");

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit") {
                $arg["reload"]  = "window.open('knjh111bindex.php?cmd=list&SCHREGNO={$model->schregno}','right_frame');";
        }

        $arg["IFRAME"] = View::setIframeJs();

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knje371bSubForm_Course.html", $arg);
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
function makeBtn(&$objForm, &$arg)
{
    //学校検索ボタン
    $extra = "onclick=\"loadwindow('" .REQUESTROOT."/X/KNJXSEARCH_COLLEGE/knjxcol_searchindex.php?cmd=',event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 600)\"";
    $arg["button"]["btn_schsearch"] = knjCreateBtn($objForm, "btn_schsearch", "学校検索", $extra);

    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('subCourse_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタンを作成する
    $extra = "onclick=\"return btn_submit('subCourse_delete');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    //取消しボタンを作成する
    $extra = "onclick=\"return btn_submit('subCourse_reset')\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタンを作成する
    $extra = "onclick=\"return btn_submit('main_edit')\"";
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
    //キーを保持
    knjCreateHidden($objForm, "SCHOOL_CD"   , $model->schoolCd);
    knjCreateHidden($objForm, "FACULTYCD"   , $model->facultyCd);
    knjCreateHidden($objForm, "DEPARTMENTCD", $model->departmentCd);
}

?>
