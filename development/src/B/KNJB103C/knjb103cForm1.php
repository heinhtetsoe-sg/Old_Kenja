<?php

require_once('for_php7.php');

class knjb103cForm1
{
    public function main(&$model)
    {
        $objForm        = new form();
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjb103cindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学年コンボ
        $extra = "onChange=\"btn_submit('edit');\"";
        $query = knjb103cQuery::getSchregRegdGdat($model);
        makeCmb($objForm, $arg, $db, $query, $model->grade, "GRADE", $extra, 1);

        //学期コンボ
        $extra = "onChange=\"btn_submit('edit');\"";
        $query = knjb103cQuery::getSemester($model);
        makeCmb($objForm, $arg, $db, $query, $model->semester, "SEMESTER", $extra, 1);

        //考査コンボ
        $extra = "onChange=\"btn_submit('edit');\"";
        $query = knjb103cQuery::getTest($model);
        makeCmb($objForm, $arg, $db, $query, $model->testcd, "TESTCD", $extra, 1);

        //教科コンボ
        $extra = "onChange=\"btn_submit('edit');\"";
        $query = knjb103cQuery::getClass($model);
        makeCmb($objForm, $arg, $db, $query, $model->classcd, "CLASSCD", $extra, 1, "BLANK");

        //科目コンボ
        $extra = "onChange=\"btn_submit('edit');\"";
        $query = knjb103cQuery::getSubclass($model);
        makeCmb($objForm, $arg, $db, $query, $model->subclasscd, "SUBCLASSCD", $extra, 1, "BLANK");

        $chairList = array();

        if ($model->subclasscd != "") {
            //講座一覧
            $query = knjb103cQuery::getChairList($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //正担任取得
                $staff = array();
                $resultStaff = $db->query(knjb103cQuery::getChairStaffList($model, $row["CHAIRCD"]));
                while ($rowStaff = $resultStaff->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $staff[] = $rowStaff['STAFFNAME'];
                }
                $resultStaff->free();
                $row["STAFFNAME"] = implode(",", $staff);

                $chairList[] = $row;
            }
            $result->free();
        }

        //更新時のチェックでエラーの場合、画面情報をセット
        if (isset($model->warning)) {
            for ($counter = 0; $counter < $model->data_cnt; $counter++) {
                $chairList[$counter]["PROCTOR_STAFFCD"] = $model->fields[$counter]["PROCTOR_STAFFCD"];
                $chairList[$counter]["RETURN_STAFFCD"]  = $model->fields[$counter]["RETURN_STAFFCD"];
                $chairList[$counter]["Q_PAPERS"]        = $model->fields[$counter]["Q_PAPERS"];
                $chairList[$counter]["A_PAPERS"]        = $model->fields[$counter]["A_PAPERS"];
                $chairList[$counter]["Q_BOTH_DIV"]      = $model->fields[$counter]["Q_BOTH_DIV"];
                $chairList[$counter]["A_BOTH_DIV"]      = $model->fields[$counter]["A_BOTH_DIV"];
                $chairList[$counter]["DUE_DATE"]        = $model->fields[$counter]["DUE_DATE"];
                $chairList[$counter]["DUE_TIME"]        = $model->fields[$counter]["DUE_TIME"];
                $chairList[$counter]["REMARK"]          = $model->fields[$counter]["REMARK"];
            }
        }

        //成績締切時刻
        $optDueTime = array();
        $optDueTime[] = "";
        for ($idx = 8; $idx <= 20; $idx++) {
            $optDueTime[] = sprintf("%02d", $idx);
        }

        //講座一覧を表示
        foreach ($chairList as $counter => $Row) {
            //講座コード
            $setData["CHAIRCD"] = $Row["CHAIRCD"];
            knjCreateHidden($objForm, "CHAIRCD"."-".$counter, $Row["CHAIRCD"]);

            //講座名称
            $setData["CHAIRNAME"] = $Row["CHAIRNAME"];

            //正担任
            $setData["STAFFNAME"] = $Row["STAFFNAME"];

            //監督者
            $extra = "style=\"width:100%; background-color:darkgray\" readonly";
            $setData["PROCTOR_STAFF_NAME"] = knjCreateTextBox($objForm, $Row["PROCTOR_STAFF_NAME"], "PROCTOR_STAFF_NAME"."-".$counter, 30, 30, $extra);
            knjCreateHidden($objForm, "PROCTOR_STAFFCD"."-".$counter, $Row["PROCTOR_STAFFCD"]);

            //返却先教職員
            $extra = "style=\"width:100%; background-color:darkgray\" readonly";
            $setData["RETURN_STAFF_NAME"] = knjCreateTextBox($objForm, $Row["RETURN_STAFF_NAME"], "RETURN_STAFF_NAME"."-".$counter, 30, 30, $extra);
            knjCreateHidden($objForm, "RETURN_STAFFCD"."-".$counter, $Row["RETURN_STAFFCD"]);

            //問題用紙枚数
            $extra = "onblur=\"this.value=toInteger(this.value);\"";
            $setData["Q_PAPERS"] = knjCreateTextBox($objForm, $Row["Q_PAPERS"], "Q_PAPERS"."-".$counter, 3, 3, $extra);

            //解答用紙枚数
            $extra = "onblur=\"this.value=toInteger(this.value);\"";
            $setData["A_PAPERS"] = knjCreateTextBox($objForm, $Row["A_PAPERS"], "A_PAPERS"."-".$counter, 3, 3, $extra);

            //問題用紙両面有無
            $extra = ($Row["Q_BOTH_DIV"] == "1") ? "checked" : "";
            $setData["Q_BOTH_DIV"] = knjCreateCheckBox($objForm, "Q_BOTH_DIV"."-".$counter, "1", $extra);

            //解答用紙両面有無
            $extra = ($Row["A_BOTH_DIV"] == "1") ? "checked" : "";
            $setData["A_BOTH_DIV"] = knjCreateCheckBox($objForm, "A_BOTH_DIV"."-".$counter, "1", $extra);

            //成績締切日
            $Row["DUE_DATE"] = ($Row["DUE_DATE"] != "") ? $Row["DUE_DATE"] : CTRL_DATE;
            $Row["DUE_DATE"] = str_replace("-", "/", $Row["DUE_DATE"]);
            $setData["DUE_DATE"] = View::popUpCalendar2($objForm, "DUE_DATE"."-".$counter, $Row["DUE_DATE"], "", "", "");

            //成績締切時刻
            $extra = "";
            $setData["DUE_TIME"] = knjCreateCombo($objForm, "DUE_TIME"."-".$counter, $Row["DUE_TIME"], $optDueTime, $extra, 1);

            //備考
            $setData["REMARK"] = getTextOrArea($objForm, "REMARK"."-".$counter, 20, 3, $Row["REMARK"]);
            $setData["REMARK_COMMENT"] = getTextAreaComment(20, 3);
            $setData["REMARK_STAT_ID"] = "statusarea_REMARK"."-".$counter;
            setInputChkHidden($objForm, "REMARK"."-".$counter, 20, 3, $arg);

            //教職員選択ボタンを作成する (監督者)
            $extra = "onclick=\"return btn_submit_subform('substaffProctor', '{$counter}', '{$model->semester}', '{$Row["CHAIRCD"]}', 'PROCTOR_STAFFCD-{$counter}');\" style=\"width:110px\"";
            $setData["btn_subformProctor"] = knjCreateBtn($objForm, "btn_subformProctor".$counter, "教職員選択", $extra);

            //教職員選択ボタンを作成する
            $extra = "onclick=\"return btn_submit_subform('substaff', '{$counter}', '{$model->semester}', '{$Row["CHAIRCD"]}', 'RETURN_STAFFCD-{$counter}');\" style=\"width:110px\"";
            $setData["btn_subform2"] = knjCreateBtn($objForm, "btn_subform2".$counter, "教職員選択", $extra);

            $arg["data"][] = $setData;
        }

        //講座一覧の表示件数
        knjCreateHidden($objForm, "DATA_CNT", get_count($chairList));

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        View::toHTML5($model, "knjb103cForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                        "value" => "");
    }

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    $btnSize = "";
    //更新
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$btnSize);
    //取消
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra.$btnSize);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra.$btnSize);
    //一括更新
    $url = REQUESTROOT."/B/KNJB103C/knjb103cindex.php";
    $param  = "?cmd=replace";
    $param .= "&SEMESTER=".$model->semester;
    $param .= "&GRADE=".$model->grade;
    $param .= "&TESTCD=".$model->testcd;
    $param .= "&CLASSCD=".$model->classcd;
    $param .= "&SUBCLASSCD=".$model->subclasscd;
    $extra = "onclick=\"openKogamen('{$url}{$param}');\"";
    $arg["button"]["btn_replace"] = knjCreateBtn($objForm, "btn_replace", "一括更新", $extra);
}

//hidden作成
function makeHidden(&$objForm)
{
    knjCreateHidden($objForm, "cmd");
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
    // $arg["data"][$setHiddenStr."_COMMENT"] = getTextAreaComment($keta, $gyo);
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
