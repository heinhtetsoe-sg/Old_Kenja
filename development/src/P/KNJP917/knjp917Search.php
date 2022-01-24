<?php

require_once('for_php7.php');

class knjp917Search {
    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjp917index.php", "", "list");

        //DB接続
        $db = Query::dbCheckOut();

        //extraセット
        $extraInt = "onblur=\"this.value=toInteger(this.value)\";";

        //年組番号を表示するチェックボックス
        $extra = "id=\"HR_CLASS_HYOUJI_FLG\"";
        if ($model->search["HR_CLASS_HYOUJI_FLG"] == "1") {
            $extra .= "checked='checked' ";
        } else {
            $extra .= "";
        }
        $arg["HR_CLASS_HYOUJI_FLG"] = knjCreateCheckBox($objForm, "HR_CLASS_HYOUJI_FLG", "1", $extra);

        //学年
        $query = knjp917Query::getGrade($model);
        makeCombo($objForm, $arg, $db, $query, $model->search["GRADE"], "GRADE", $disabled, 1, "BLANK");

        //年組
        $query = knjp917Query::getHrClass($model);
        makeCombo($objForm, $arg, $db, $query, $model->search["HR_CLASS"], "HR_CLASS", $disabled, 1, "BLANK");

        //入学年度
        $query = knjp917Query::getEntYear();
        makeCombo($objForm, $arg, $db, $query, $model->search["ENT_YEAR"], "ENT_YEAR", $disabled, 1, "BLANK");

        //卒業予定年度
        $query = knjp917Query::getGrdYear();
        makeCombo($objForm, $arg, $db, $query, $model->search["GRD_YEAR"], "GRD_YEAR", $disabled, 1, "BLANK");

        //学籍番号
        $arg["SCHREGNO"] = knjCreateTextBox($objForm, $model->search["SCHREGNO"], "SCHREGNO", 8, 8, $extraInt);

        //氏名
        $arg["NAME"] = knjCreateTextBox($objForm, $model->search["NAME"], "NAME", 40, 40, "");

        //かな氏名
        $arg["NAME_KANA"] = knjCreateTextBox($objForm, $model->search["NAME_KANA"], "NAME_KANA", 40, 40, "");

        //ボタン作成
        //検索ボタンを作成する
        $extra = "onclick=\"btn_submit('search')\"";
        $arg["btn_search"] = knjCreateBtn($objForm, "btn_search", "検　索", $extra);

        //生徒リスト表示
        if ($model->cmd == "search") {
            makeStudentList($objForm, $arg, $db, $model);
            $arg["search"] = 1;
        }

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if ($model->searchBack == "back") {
            $arg["jscript"] = "btn_submit('search')";
        }

        View::toHTML($model, "knjp917Search.html", $arg);
    }
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
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
    $value = ($value != "") ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//生徒リスト表示
function makeStudentList(&$objForm, &$arg, $db, $model) {
    if ($model->search["HR_CLASS_HYOUJI_FLG"] == "1") {
        $arg["KOUMOKU1"] = 'クラス - 番';
        $arg["KOUMOKU2"] = '学籍番号';
    } else {
        $arg["KOUMOKU1"] = '学籍番号';
        $arg["KOUMOKU2"] = 'クラス - 番';
    }
    $query = knjp917Query::GetStudents($model);
    $result = $db->query($query);
    $image  = array(REQUESTROOT ."/image/system/boy1.gif", REQUESTROOT ."/image/system/girl1.gif");
    $i = 0;
    $schList = $sep = "";
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $a = array("cmd"                    => "edit",
                   "SCHOOL_KIND"            => $row["SCHOOL_KIND"],
                   "SCHREGNO"               => $row["SCHREGNO"],
                   "NAME"                   => $row["NAME"]);

        $row["NAME"] = View::alink("knjp917index.php", htmlspecialchars($row["NAME"]), "target=\"right_frame\"", $a);

        $row["INFONO"]      = $row["SCHREGNO"];
        $row["IMAGE"]       = $image[($row["SEX"]-1)];
        $row["GRD_DATE"]    = str_replace("-", "/", $row["GRD_DATE"]);

        if ($model->search["HR_CLASS_HYOUJI_FLG"] == "1") {
            $row["KOUMOKU1_VALUE"] = $row["HR_ATTEND"];
            $row["KOUMOKU2_VALUE"] = $row["SCHREGNO"];
            $row["KOUMOKU1_align"] = "center";
            $row["KOUMOKU2_align"] = "right";
        } else {
            $row["KOUMOKU1_VALUE"] = $row["SCHREGNO"];
            $row["KOUMOKU2_VALUE"] = $row["HR_ATTEND"];
            $row["KOUMOKU1_align"] = "right";
            $row["KOUMOKU2_align"] = "center";
        }

        $schList .= $sep.$row["SCHREGNO"];
        $sep = "-";

        $arg["data"][] = $row;
        $i++;
    }
    $arg["send_list"] = "sendPrintList('{$schList}')";
    $result->free();
}
?>
