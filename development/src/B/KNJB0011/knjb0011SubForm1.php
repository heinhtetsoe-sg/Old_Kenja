<?php

require_once('for_php7.php');

class knjb0011SubForm1 {
    function main(&$model) {
        /* フォーム作成 */
        $objForm = new form;
        $db = Query::dbCheckOut();

        $arg["start"]   = $objForm->get_start("main", "POST", "knjb0011index.php", "", "main");

        //初期化
        if ($model->cmd == "subForm1") unset($model->appDate);

        /* 生徒情報 */
        $query = knjb0011Query::getSchInfo($model);
        $schInfo = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["HR_NAME"] = $schInfo["HR_NAME"];
        $arg["ATTENDNO"] = $schInfo["ATTENDNO"];
        $arg["NAME"] = $schInfo["NAME"];

        //学期終了日・開始日取得
        $query = knjb0011Query::getSemesterEndDate($model, $schInfo["GRADE"]);
        $sem = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $model->setEndDate = $sem["EDATE"];
        $model->setStartDate = $sem["SDATE"];

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
        $groupList = $chairList = "";
        $sep = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg["CHAIRABBV"][] = $row;
            $model->listArray[] = $row;
            $seru_no_haba = 80;
            $cd = ($model->selectGroup == "1") ? $row["CLASSCD"]."-".$row["SCHOOL_KIND"] : $row["GROUPCD"];
            $subclass_name_width[$cd] += ($seru_no_haba > 50) ? (int)$seru_no_haba : 50;
            $model->groupInstate .= ($model->selectGroup == "1") ? $sep."'".$row["CLASSCD"]."-".$row["SCHOOL_KIND"]."'" : $sep."'".$row["GROUPCD"]."'";
            $groupList .= $sep.$row["GROUPCD"];
            $chairList .= $sep.$row["CHAIRCD"];
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
        $setDate = makeList($objForm, $arg, $db, $model);

        //講座適用開始日
        $setDate = ($setDate) ? $setDate : $sem["SDATE"];
        $arg["APPDATE"] = View::popUpCalendar($objForm, "APPDATE", str_replace("-", "/", $setDate));

        /**********/
        /* ボタン */
        /**********/
        //更新
        $extra = "onclick=\"btn_submit('update');\"";
        $arg["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //戻る
        $extra = "onclick=\"btn_submit('form1');\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "YEAR", $model->year);
        knjCreateHidden($objForm, "SEMESTER", $model->semester);
        knjCreateHidden($objForm, "COURSE", $model->course);
        knjCreateHidden($objForm, "SCHREGNO", $model->schregNo);
        knjCreateHidden($objForm, "SETENDDATE", $model->setEndDate);
        knjCreateHidden($objForm, "SETSTARTDATE", $model->setStartDate);
        knjCreateHidden($objForm, "selectClass", $model->selectClass);
        knjCreateHidden($objForm, "SELECT_GROUP", $model->selectGroup);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJB0011");
        knjCreateHidden($objForm, "selectdata");
        knjCreateHidden($objForm, "GROUPLIST", $groupList);
        knjCreateHidden($objForm, "CHAIRLIST", $chairList);

        Query::dbCheckIn($db);
        $arg["finish"] = $objForm->get_finish();
        View::toHTML($model, "knjb0011SubForm1.html", $arg);
    }
}
/****************************/
/* 編集対象データリスト作成 */
/****************************/
function makeList(&$objForm, &$arg, $db, $model) {
    $setDate = "";
    $query  = knjb0011Query::getList($model, $model->schregNo);
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

        $chairStd = array();
        if (isset($model->warning)) {
            if ($model->selectdata) {
                foreach ($model->selectdata as $selectdata) {
                    $chairStd[$selectdata] = $selectdata;
                }
            }
            $setDate = $model->appDate;
        } else {
            $query = knjb0011Query::getChairStd($model, $model->schregNo);
            $resultStd = $db->query($query);
            while ($rowStd = $resultStd->fetchRow(DB_FETCHMODE_ASSOC)) {
                $chairStd[$rowStd["CHAIRCD"]] = $rowStd["CHAIRCD"];
                //MAX開始日付
                if (str_replace("/", "-", $setDate) < $rowStd["APPDATE"]) {
                    $setDate = $rowStd["APPDATE"];
                }
            }
            $resultStd->free();
        }

        $chairCnt = 0;
        foreach ($model->listArray as $subclass_select_data) {
            if (in_array($subclass_select_data["CHAIRCD"], $chairStd)) {
                $check_box = " checked ";
            } else {
                $check_box = "";
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

            //checkbox
            if ($model->selectGroup == "1") {
                $extra = $check_box;
            } else {
                $extra = $check_box." onclick=\"chkSameGroup(this);\"";
            }
            $check_box = knjCreateCheckBox($objForm, "CHAIR{$chairCnt}", $subclass_select_data["CHAIRCD"], $extra);

            $td .="<td {$color} width=\"70px\">{$check_box}</td>\n";

            if ($checked_flg) {
                $check_name_array[$check_name] = '1';
            } else {
                $check_name_array[$check_name] = '';
            }
            $chairCnt++;
        }
        $row["TD"] = $td;

        $data[] = $row;
        //hidden
        knjCreateHidden($objForm, "CHAIRCNT", $chairCnt);

    }

    $arg["attend_data2"] = $data;
    $arg["attend_data"]  = $data;

    return $setDate;
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
