<?php

require_once('for_php7.php');

class knjb103cSubFormReplace
{
    public function main(&$model)
    {
        $objForm        = new form();
        $arg["start"]   = $objForm->get_start("replace", "POST", "knjb103cindex.php", "", "replace");
        $arg["jscript"] = "";

        $db = Query::dbCheckOut();

        //講座一覧
        $opt_left = $opt_right = array();

        $array = explode(",", $model->replace_data["selectdata"]);

        if ($model->cmd == 'subReplace') {
            $Row = $model->replace_data["field"];

            //返却先教職員
            if ($Row["RETURN_STAFFCD"] != "") {
                $staffRow = $db->getRow(knjb103cQuery::getStaffMst($model, $Row["RETURN_STAFFCD"]), DB_FETCHMODE_ASSOC);
                $Row["RETURN_STAFF_NAME"] = $staffRow["STAFFNAME"];
            }
        }

        //講座一覧
        $query = knjb103cQuery::getChairList($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!in_array($row["CHAIRCD"], $array)) {
                $opt_right[]  = array("label" => $row["CHAIRCD"]."  ".$row["CHAIRNAME"],
                                      "value" => $row["CHAIRCD"]);
            } else {
                $opt_left[] = array("label" => $row["CHAIRCD"]."  ".$row["CHAIRNAME"],
                                    "value" => $row["CHAIRCD"]);
            }
        }
        $result->free();

        //チェックボックス
        for ($idx = 0; $idx < 8; $idx++) {
            $extra = ($model->replace_data["check"][$idx] == "1") ? "checked" : "";
            $arg["data"]["RCHECK".$idx] = knjCreateCheckBox($objForm, "RCHECK".$idx, "1", $extra);
        }

        $extra = "onClick=\"return check_all(this);\"";
        $extra .= ($model->replace_data["check_all"] == "1") ? "checked" : "";
        $arg["data"]["RCHECKALL"] = knjCreateCheckBox($objForm, "RCHECKALL", "1", $extra);

        //返却先教職員
        $extra = "style=\"background-color:darkgray\" readonly";
        $arg["data"]["RETURN_STAFF_NAME"] = knjCreateTextBox($objForm, $Row["RETURN_STAFF_NAME"], "RETURN_STAFF_NAME", 30, 30, $extra);
        knjCreateHidden($objForm, "RETURN_STAFFCD", $Row["RETURN_STAFFCD"]);

        //教職員選択ボタンを作成する
        $extra = "onclick=\"return btn_submit_subform('substaff', '0', '{$model->semester}', '', 'RETURN_STAFFCD');\" style=\"width:110px\"";
        $arg["data"]["btn_subform2"] = knjCreateBtn($objForm, "btn_subform2", "教職員選択", $extra);

        //問題用紙枚数
        $extra = "onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["Q_PAPERS"] = knjCreateTextBox($objForm, $Row["Q_PAPERS"], "Q_PAPERS", 3, 3, $extra);

        //解答用紙枚数
        $extra = "onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["A_PAPERS"] = knjCreateTextBox($objForm, $Row["A_PAPERS"], "A_PAPERS", 3, 3, $extra);

        //問題用紙両面有無
        $extra = ($Row["Q_BOTH_DIV"] == "1") ? "checked" : "";
        $arg["data"]["Q_BOTH_DIV"] = knjCreateCheckBox($objForm, "Q_BOTH_DIV", "1", $extra);

        //解答用紙両面有無
        $extra = ($Row["A_BOTH_DIV"] == "1") ? "checked" : "";
        $arg["data"]["A_BOTH_DIV"] = knjCreateCheckBox($objForm, "A_BOTH_DIV", "1", $extra);

        //成績締切日
        $Row["DUE_DATE"] = ($Row["DUE_DATE"] != "") ? $Row["DUE_DATE"] : CTRL_DATE;
        $Row["DUE_DATE"] = str_replace("-", "/", $Row["DUE_DATE"]);
        $arg["data"]["DUE_DATE"] = View::popUpCalendar2($objForm, "DUE_DATE", $Row["DUE_DATE"], "", "", "");

        //成績締切時刻
        $optDueTime = array();
        $optDueTime[] = "";
        for ($idx = 8; $idx <= 20; $idx++) {
            $optDueTime[] = sprintf("%02d", $idx);
        }

        $extra = "";
        $arg["data"]["DUE_TIME"] = knjCreateCombo($objForm, "DUE_TIME", $Row["DUE_TIME"], $optDueTime, $extra, 1);

        //備考
        $arg["data"]["REMARK"] = getTextOrArea($objForm, "REMARK", 20, 3, $Row["REMARK"]);
        setInputChkHidden($objForm, "REMARK", 20, 3, $arg);

        //学期名称
        $semesterRow = $db->getRow(knjb103cQuery::getSemester($model, $model->semester), DB_FETCHMODE_ASSOC);
        $semesterName = $semesterRow["SEMESTERNAME"];

        //考査名称
        $testRow = $db->getRow(knjb103cQuery::getTest($model, $model->testcd), DB_FETCHMODE_ASSOC);
        $testName = $testRow["TESTITEMNAME"];

        //科目名称
        $subclassRow = $db->getRow(knjb103cQuery::getSubclass($model, $model->subclasscd), DB_FETCHMODE_ASSOC);
        $subclassName = $subclassRow["SUBCLASSNAME"];

        Query::dbCheckIn($db);

        //更新ボタン
        $extra = "onclick=\"return doSubmit('replace_update')\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //戻るボタン
        $link = REQUESTROOT."/B/KNJB103C/knjb103cindex.php";
        $param  = "?cmd=edit&ini2=1";
        $param .= "&SEMESTER=".$model->semester;
        $param .= "&GRADE=".$model->grade;
        $param .= "&TESTCD=".$model->testcd;
        $param .= "&CLASSCD=".$model->classcd;
        $param .= "&SUBCLASSCD=".$model->subclasscd;
        $link .= $param;
        $extra = "onclick=\"window.open('$link','_self');\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //対象講座一覧
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','left_select','right_select',1)\"";
        $arg["date"]["left_select"] = knjCreateCombo($objForm, "left_select", "left", $opt_left, $extra, "20");

        //講座一覧
        $extra = "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','left_select','right_select',1)\"";
        $arg["date"]["right_select"] = knjCreateCombo($objForm, "right_select", "left", $opt_right, $extra, "20");

        //全て追加
        $extra = "onclick=\"return move('sel_add_all','left_select','right_select',1);\"";
        $arg["button"]["sel_add_all"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);

        //追加
        $extra = "onclick=\"return move('left','left_select','right_select',1);\"";
        $arg["button"]["sel_add"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);

        //削除
        $extra = "onclick=\"return move('right','left_select','right_select',1);\"";
        $arg["button"]["sel_del"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);

        //全て削除
        $extra = "onclick=\"return move('sel_del_all','left_select','right_select',1);\"";
        $arg["button"]["sel_del_all"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);

        //ヘッダ
        $arg["info"]    = array("TOP"        =>  CTRL_YEAR."年度  "
                                                .$semesterName."  "
                                                .$testName
                                                ."  対象科目  ".$subclassName,
                                "LEFT_LIST"  => "対象講座一覧",
                                "RIGHT_LIST" => "講座一覧");

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");

        knjCreateHidden($objForm, "GRADE", $model->grade);
        knjCreateHidden($objForm, "SEMESTER", $model->semester);
        knjCreateHidden($objForm, "TESTCD", $model->testcd);
        knjCreateHidden($objForm, "CLASSCD", $model->classcd);
        knjCreateHidden($objForm, "SUBCLASSCD", $model->subclasscd);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML5($model, "knjb103cSubFormReplace.html", $arg);
    }
}

//テキストボックス or テキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val)
{
    $retArg = "";
    if ($gyou > 1) {
        //textArea
        $extra = "style=\"overflow-y:scroll\" id=\"".$name."\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2), "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\" id=\"".$name."\"";
        // $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), ($moji * 2), $extra);
    }
    return $retArg;
}

function setInputChkHidden(&$objForm, $setHiddenStr, $keta, $gyo, &$arg)
{
    $arg["data"][$setHiddenStr."_COMMENT"] = getTextAreaComment($keta, $gyo);
    KnjCreateHidden($objForm, $setHiddenStr."_KETA", $keta*2);
    KnjCreateHidden($objForm, $setHiddenStr."_GYO", $gyo);
    KnjCreateHidden($objForm, $setHiddenStr."_STAT", "statusarea_".$setHiddenStr);
}

function getTextAreaComment($moji, $gyo)
{
    $comment = "";
    if ($gyo > 1) {
        $comment .= "(全角{$moji}文字X{$gyo}行まで)";
    } else {
        $comment .= "(全角{$moji}文字まで)";
    }
    return $comment;
}
