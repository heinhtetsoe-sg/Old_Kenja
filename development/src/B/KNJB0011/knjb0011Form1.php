<?php

require_once('for_php7.php');

class knjb0011Form1 {
    function main(&$model) {
        /* フォーム作成 */
        $objForm = new form;
        $db = Query::dbCheckOut();

        $arg["start"]   = $objForm->get_start("main", "POST", "knjb0011index.php", "", "main");

        /* 処理年度 */
        $query = knjb0011Query::getExeYear($model);
        $extra = "onChange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, $model->year, "YEAR", $extra, 1);

        /* 処理学期 */
        $query = knjb0011Query::getSemester($model);
        $extra = "onChange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, $model->semester, "SEMESTER", $extra, 1);
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        //radio
        $opt = array(1, 2);
        $model->selectGroup = ($model->selectGroup == "") ? "1" : $model->selectGroup;
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"SELECT_GROUP{$val}\" onClick=\"btn_submit('')\"");
        }
        $radioArray = knjCreateRadio($objForm, "SELECT_GROUP", $model->selectGroup, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //教科コンボ
        $query = knjb0011Query::getClassMst($model);
        $extra = "onChange=\"btn_submit('change')\"";
        makeCmb($objForm, $arg, $db, $query, $model->selectClass, "selectClass", $extra, 1, "BLANK", "ALL");

        /* コース */
        $query = knjb0011Query::getCourse($model);
        $extra = "onChange=\"return btn_submit('change');\"";
        makeCmb($objForm, $arg, $db, $query, $model->course, "COURSE", $extra, 1, "BLANK", "");

        //グループ名
        $query = knjb0011Query::getGroupCnt($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["SIZE"] = (int)$row["CNT"] * 50;

            $arg["GROUP"][] = $row;
            $cd = ($model->selectGroup == "1") ? $row["CLASSCD"]."-".$row["SCHOOL_KIND"] : $row["GROUPCD"];
            $group_name_width[$cd] = mb_strlen($row["GROUPNAME"]) * 16;
        }

        //講座名
        $model->listArray = array(); //リストを作るときに使う
        $query = knjb0011Query::getChairName($model);
        $result = $db->query($query);
        $model->groupInstate = "";
        $sep = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["CHAIRABBV"][] = $row;
            $model->listArray[] = $row;
            $seru_no_haba = 80;
            $cd = ($model->selectGroup == "1") ? $row["CLASSCD"]."-".$row["SCHOOL_KIND"] : $row["GROUPCD"];
            $subclass_name_width[$cd] += ($seru_no_haba > 50) ? (int)$seru_no_haba : 50;
            $model->groupInstate .= ($model->selectGroup == "1") ? $sep."'".$row["CLASSCD"]."-".$row["SCHOOL_KIND"]."'" : $sep."'".$row["GROUPCD"]."'";
            $sep = ",";
        }

        $total_width = 0;
        if ($group_name_width) {
            foreach ($group_name_width as $groupcd => $dummy_val) {
                $total_width += ($group_name_widthas[$groupcd] > $subclass_name_width[$groupcd]) ? (int)$group_name_widthas[$groupcd] : (int)$subclass_name_width[$groupcd];
            }
        }
        $arg["TOTAL_WIDTH"] = $total_width;

        /* 編集対象データリスト */
        makeList($objForm, $arg, $db, $model);

        /**********/
        /* ボタン */
        /**********/
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJB0011");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        //CSVファイルアップロードコントロール
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjb0011Form1.html", $arg);
    }
}
/******************************/
/* 編集対象データリスト作成成 */
/******************************/
function makeList(&$objForm, &$arg, $db, $model) {
    $query  = knjb0011Query::getList($model);
    $result = $db->query($query);
    $data = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $td = '';
        $check_name_array = array();
        $color = '';
        $color_count = 0;
        $setcolor1 = "#dddddd";
        $setcolor2 = "#FFFFFF";
        $check_color;
        $tmpcd = "";

        $query = knjb0011Query::getSemesterEndDate($model, $row["GRADE"]);
        $model->setEndDate = $db->getOne($query);

        $query = knjb0011Query::getChairStd($model, $row["SCHREGNO"]);
        $resultStd = $db->query($query);
        $chairStd = array();
        while ($rowStd = $resultStd->fetchRow(DB_FETCHMODE_ASSOC)) {
            $chairStd[$rowStd["CHAIRCD"]] = $rowStd["CHAIRCD"];
        }
        $resultStd->free();

        foreach ($model->listArray as $subclass_select_data) {
            if (in_array($subclass_select_data["CHAIRCD"], $chairStd)) {
                $check_box = "レ";
            } else {
                $check_box = "　";
            }

            $comparecd = ($model->selectGroup == "1") ? $subclass_select_data["CLASSCD"]."-".$subclass_select_data["SCHOOL_KIND"] : $subclass_select_data["GROUPCD"];
            //各グループごとに色分けを行なう
            //一列目
            if ($color_count == 0) {
                $color = "bgcolor=$setcolor1 ";
                $check_color = $setcolor1;
            //群／教科コードが異なる場合
            } else if ($comparecd != $tmpcd) {
                if ($check_color == $setcolor1) {
                    $color = "bgcolor=$setcolor2 ";
                    $check_color = $setcolor2;
                } else {
                    $color = "bgcolor=$setcolor1 ";
                    $check_color = $setcolor1;
                }
            //群／教科コードが同じ場合
            } else {
                $color = "bgcolor=$check_color ";
            }
            $tmpcd = $comparecd;
            $color_count++;

            $td .="<td {$color} width=\"70px\">{$check_box}</td>\n";

            if ($checked_flg) {
                $check_name_array[$check_name] = '1';
            } else {
                $check_name_array[$check_name] = '';
            }
        }
        $row["TD"] = $td;

        //hidden用に学籍番号を配列に
        $schregnoArray[] = $row["SCHREGNO"];
        //リンク設定
        $subdata = "loadwindow('knjb0011index.php?cmd=subForm1&cmdSub=subForm1&SCHREGNO={$row["SCHREGNO"]}&YEAR={$model->year}&SEMESTER={$model->semester}&selectClass={$model->selectClass}&SELECT_GROUP={$model->selectGroup}&COURSE={$model->course}',0,0,1150,250)";
        $row["NAME"] = View::alink("#", htmlspecialchars($row["NAME"]),"onclick=\"$subdata\"");

        $data[] = $row;
    }

    //hidden
    if (is_array($schregnoArray)) {
        $schregno_all = implode(',', $schregnoArray);
    }
    knjCreateHidden($objForm, "SCHREGNO_ALL", $schregno_all);

    $arg["attend_data2"] = $data;
    $arg["attend_data"]  = $data;
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "", $all = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }

    if ($all == "ALL") {
        $opt[] = array ("label" => "--全て--",
                        "value" => "ALL");
    }

    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();

    $value = ($value) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
