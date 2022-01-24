<?php

require_once('for_php7.php');

class knjb0057Form1 {
    function main(&$model) {
        /* フォーム作成 */
        $objForm = new form;
        $db = Query::dbCheckOut();

        $arg["start"]   = $objForm->get_start("main", "POST", "knjb0057index.php", "", "main");

        /* 処理年度 */
        $query = knjb0057Query::getExeYear($model);
        $extra = "onChange=\"return btn_submit('change');\"";
        makeCombo($objForm, $arg, $db, $query, $model->year, "YEAR", $extra, 1);

        /* 処理学期 */
        $query = knjb0057Query::getSemester($model);
        $extra = "onChange=\"return btn_submit('change');\"";
        makeCombo($objForm, $arg, $db, $query, $model->semester, "SEMESTER", $extra, 1);

        /* 対象学級 */
        $query      = knjb0057Query::getScregRegdHdat($model);
        $result     = $db->query($query);
        $value      = $model->field["GRADE_CLASS"];
        $opt        = array();
        $opt[]      = array("label" => "", "value" => "");
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"],
                           "value" => $row["VALUE"]);
        }
        $extra = "onChange=\"btn_submit('change')\";";
        $arg["GRADE_CLASS"] = knjCreateCombo($objForm, "GRADE_CLASS", $value, $opt, $extra, 1);

        //radio
        $opt = array(1, 2);
        $model->field["GROUP_SELECT"] = ($model->field["GROUP_SELECT"] == "") ? "1" : $model->field["GROUP_SELECT"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"GROUP_SELECT{$val}\" onClick=\"btn_submit('change')\"");
        }
        $radioArray = knjCreateRadio($objForm, "GROUP_SELECT", $model->field["GROUP_SELECT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //グループ名
        $query = knjb0057Query::getGroupCnt($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //GROUPCDが600以上は背景色を変える
            $row["COLOR"] = ($row["GROUPCD"] < '600') ? "" : "bgcolor=\"#FF69B4\"";

            $row["SIZE"] = (int)$row["COUNT"] * 50;

            $arg["GROUP"][] = $row;
            $group_name_width[$row["GROUPCD"]] = mb_strlen($row["NAME"]) * 16;
        }

        //科目名
        $model->listArray = array(); //リストを作るときに使う
        $query = knjb0057Query::getSubclassName($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //GROUPCDが600以上は背景色を変える
            $row["COLOR"] = ($row["GROUPCD"] < '600') ? "" : "bgcolor=\"#FF69B4\"";

            $arg["SUBCLASSABBV"][] = $row;
            $model->listArray[] = $row;
            $seru_no_haba = mb_strlen($row["SUBCLASSABBV"]) * 16;
            $subclass_name_width[$row["GROUPCD"]] += ($seru_no_haba > 50) ? $seru_no_haba : 50;
        }

        $total_width = 0;
        if ($group_name_width) {
            foreach ($group_name_width as $groupcd => $dummy_val) {
                $total_width += ($group_name_widthas[$groupcd] > $subclass_name_width[$groupcd]) ? $group_name_widthas[$groupcd] : $subclass_name_width[$groupcd];
            }
        }
        $arg["TOTAL_WIDTH"] = $total_width;

        /* 編集対象データリスト */
        makeList($objForm, $arg, $db, $model);

        /**********/
        /* ボタン */
        /**********/
        //印刷
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);
        //保存
        $extra = "onclick=\"btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "保 存", $extra);
        //取消
        $extra = "onclick=\"btn_submit('reset');\"";
        $arg["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
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
        knjCreateHidden($objForm, "PRGID", "KNJB0057");
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        //CSVファイルアップロードコントロール
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjb0057Form1.html", $arg);
    }
}
/***************************************************** 以下関数 *****************************************************/
/******************************/
/* 編集対象データリスト作成成 */
/******************************/
function makeList(&$objForm, &$arg, $db, $model) {
    $query  = knjb0057Query::getList($model);
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
        foreach ($model->listArray as $subclass_select_data) {
            $query = knjb0057Query::checkSubclassStdSelectDat($subclass_select_data["GROUPCD"], $subclass_select_data["SUBCLASSCD"], $row["SCHREGNO"], $model);
            $cnt = $db->getOne($query);
            $checked_flg = ($cnt > 0) ? true : false;

            $check_name = "SELECTDATA_{$row["SCHREGNO"]}_{$subclass_select_data["GROUPCD"]}_{$subclass_select_data["SUBCLASSCD"]}"; //チェックボックスの名前
            //チェックボックス
            if ($checked_flg) {
                $extra = "checked=checked";
            } else {
                $extra = "";
            }
            $extra .= " onClick=\"check_subclasscd(this)\"";

            $check_box = knjCreateCheckBox($objForm, $check_name, "VALUE", $extra);
            
            //各グループごとに色分けを行なう
            //一行目
            if ($color_count == 0) {
                $color = "bgcolor=$setcolor1 ";
                $check_color = $setcolor1;
            //科目コードが異なる場合
            } else if ($subclass_select_data["GROUPCD"] != $check_groupcd) {
                if ($check_color == $setcolor1) {
                    $color = "bgcolor=$setcolor2 ";
                    $check_color = $setcolor2;
                } else {
                    $color = "bgcolor=$setcolor1 ";
                    $check_color = $setcolor1;
                }
            //科目コードが同じ場合
            } else {
                $color = "bgcolor=$check_color ";
            }
            $check_groupcd = $subclass_select_data["GROUPCD"];
            $color_count++;
            
            $td .="<td {$color}>{$check_box}</td>\n";

            if ($checked_flg) {
                $check_name_array[$check_name] = '1';
            } else {
                $check_name_array[$check_name] = '';
            }
        }
        $row["TD"] = $td;

        //hidden用に学籍番号を配列に
        $schregnoArray[] = $row["SCHREGNO"];
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
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
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
