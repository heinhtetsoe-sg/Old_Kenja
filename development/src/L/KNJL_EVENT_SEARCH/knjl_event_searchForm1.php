<?php

require_once('for_php7.php');

class knjl_event_searchForm1
{
    public function main(&$model)
    {

        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "index.php", "", "list");
        //DB接続
        $db     = Query::dbCheckOut();

        //extraセット
        $extraInt = "onblur=\"this.value=toInteger(this.value)\";";

        //表示項目設定
        foreach ($model->dispData as $key => $val) {
            $arg[$key] = $val;
        }

        $query = knjl_event_searchQuery::getA023();
        $model->schoolKindArray = array();
        $result = $db->query($query);
        while ($schoolRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->schoolKindArray[$schoolRow["NAME1"]] = $schoolRow["NAME1"];
        }

        //校種
        $defKind = $model->schoolKindArray["J"] ? "1" : "2";
        $opt = array(1, 2);
        $model->search["SCHOOL_KIND"] = ($model->search["SCHOOL_KIND"] == "") ? $defKind : $model->search["SCHOOL_KIND"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"SCHOOL_KIND{$val}\" onClick=\"btn_submit('edit')\"");
        }
        $radioArray = knjCreateRadio($objForm, "SCHOOL_KIND", $model->search["SCHOOL_KIND"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg[$key] = $val;
        }

        //管理番号
        $arg["RECRUIT_NO"] = knjCreateTextBox($objForm, $model->search["RECRUIT_NO"], "RECRUIT_NO", 8, 8, "");

        //氏名
        $value = ($model->cmd == "list" && !$model->search["NAME"]) ? $model->name : $model->search["NAME"];
        $arg["NAME"] = knjCreateTextBox($objForm, $value, "NAME", 40, 40, "");

        //かな氏名
        $value = ($model->cmd == "list" && !$model->search["NAME_KANA"]) ? $model->kana : $model->search["NAME_KANA"];
        $arg["NAME_KANA"] = knjCreateTextBox($objForm, $value, "NAME_KANA", 40, 40, "");

        //分類
        $query = knjl_event_searchQuery::getRecruitClass();
        $extra = "onChange=\"btn_submit('edit')\"";
        makeCombo($objForm, $arg, $db, $query, $model->search["EVENT_CLASS_CD"], "EVENT_CLASS_CD", $extra.$disabled, 1, "BLANK");

        //イベント名
        $query = knjl_event_searchQuery::getEventMst($model);
        makeCombo($objForm, $arg, $db, $query, $model->search["EVENT_CD"], "EVENT_CD", $disabled, 1, "BLANK");

        //媒体
        $query = knjl_event_searchQuery::getNameMst("L401");
        makeCombo($objForm, $arg, $db, $query, $model->search["MEDIA_CD"], "MEDIA_CD", $disabled, 1, "BLANK");

        //出身学校
        $value = ($model->cmd == "list" && !$model->search["FINSCHOOLCD"]) ? $model->finschoolcd : $model->search["FINSCHOOLCD"];
        $query = knjl_event_searchQuery::getFinschoolCd($model);
        makeCombo($objForm, $arg, $db, $query, $value, "FINSCHOOLCD", $disabled, 1, "BLANK");

        //学年
        $query = knjl_event_searchQuery::getGrade($model);
        makeCombo($objForm, $arg, $db, $query, $model->search["GRADE"], "GRADE", $disabled, 1, "BLANK");

        //塾
        $query = knjl_event_searchQuery::getPrischoolCd($model);
        if ($model->dispData["prischoolClassCd"] == "1") {
            $extra = " onChange=\"btn_submit('priChange')\" ";
        }
        makeCombo($objForm, $arg, $db, $query, $model->search["PRISCHOOLCD"], "PRISCHOOLCD", $disabled.$extra, 1, "BLANK");

        //教室
        $query = knjl_event_searchQuery::getPrischoolClassMst($model);
        makeCombo($objForm, $arg, $db, $query, $model->search["PRISCHOOL_CLASS_CD"], "PRISCHOOL_CLASS_CD", $disabled, 1, "BLANK");

        //ボタン作成
        makeButton($objForm, $arg, $model);

        if ($model->cmd == "searchUpd") {
            foreach ($model->search as $searchName => $searchVal) {
                if ($searchVal) {
                    $model->cmd = "search";
                    break;
                }
            }
        }

        //生徒リスト表示
        if ($model->cmd == "search") {
            makeStudentList($objForm, $arg, $db, $model);
            $model->firstFlg = false;
            $arg["search"] = 1;
        } elseif ($model->cmd == "search2") {
            $arg["reload"] = "Link2('".REQUESTROOT."')";
        }

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if ($model->searchBack == "back") {
            echo "aa";
            $arg["jscript"] = "btn_submit('search')";
        }

        View::toHTML($model, "knjl_event_searchForm1.html", $arg);
    }
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

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();
    $value = ($value != "") ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//生徒リスト表示
function makeStudentList(&$objForm, &$arg, $db, $model)
{
    $query = knjl_event_searchQuery::getStudents($model);
    $result = $db->query($query);
    $image  = array(REQUESTROOT ."/image/system/boy1.gif", REQUESTROOT ."/image/system/girl1.gif");
    $i = 0;
    list($path, $cmd) = explode("?cmd=", $model->path[$model->programid]);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $a = array("cmd"         => $cmd,
                   "RECRUIT_YEAR" => $row["YEAR"],
                   "RECRUIT_NO"  => $row["RECRUIT_NO"],
                   "NAME"        => $row["NAME"]);
        $row["NAME"] = View::alink(REQUESTROOT .$path, htmlspecialchars($row["NAME"]), "target=" .$model->target[$model->programid] ." onclick=\"Link(this)\"", $a);
        $row["IMAGE"] = $image[($row["SEX"]-1)];
        $arg["data"][] = $row;
        $i++;
    }
    $result->free();
}

//ボタン作成
function makeButton(&$objForm, &$arg, $model)
{
    //検索ボタンを作成する
    if ($model->searchMode) {
        $extra = "onclick=\"btn_submit('search2')\"";
    } else {
        $extra = "onclick=\"btn_submit('search')\"";
    }
    $arg["btn_search"] = knjCreateBtn($objForm, "btn_search", "検　索", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "path", REQUESTROOT .$model->path[$model->programid]);
    knjCreateHidden($objForm, "PROGRAMID", $model->programid);
    knjCreateHidden($objForm, "searchMode", $model->searchMode);

    list($path, $cmd) = explode("?cmd=", $model->path[$model->programid]);
    knjCreateHidden($objForm, "right_path", $path);

    foreach ($model->dispData as $key => $val) {
        knjCreateHidden($objForm, $key, $val);
    }
}

?>
