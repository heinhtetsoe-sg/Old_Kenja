<?php

require_once('for_php7.php');

class knjxexp_prischoolForm1
{
    function main(&$model){

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

        //塾コード
        $arg["PRISCHOOLCD"] = knjCreateTextBox($objForm, $model->search["PRISCHOOLCD"], "PRISCHOOLCD", 7, 7, "");

        //塾名
        $arg["PRISCHOOL_NAME"] = knjCreateTextBox($objForm, $model->search["PRISCHOOL_NAME"], "PRISCHOOL_NAME", 50, 75, "");

        //塾名かな
        $arg["PRISCHOOL_KANA"] = knjCreateTextBox($objForm, $model->search["PRISCHOOL_KANA"], "PRISCHOOL_KANA", 50, 75, "");

        //教室名
        $arg["PRISCHOOL_CLASS_NAME"] = knjCreateTextBox($objForm, $model->search["PRISCHOOL_CLASS_NAME"], "PRISCHOOL_CLASS_NAME", 50, 75, "");

        //教室名かな
        $arg["PRISCHOOL_CLASS_KANA"] = knjCreateTextBox($objForm, $model->search["PRISCHOOL_CLASS_KANA"], "PRISCHOOL_CLASS_KANA", 50, 75, "");

        //最寄路線名
        $arg["ROSEN_NAME"] = knjCreateTextBox($objForm, $model->search["ROSEN_NAME"], "ROSEN_NAME", 50, 75, "");

        //最寄駅名
        $arg["NEAREST_STATION_NAME"] = knjCreateTextBox($objForm, $model->search["NEAREST_STATION_NAME"], "NEAREST_STATION_NAME", 50, 75, "");

        //地区
        $query = knjxexp_prischoolQuery::getNameMst("Z003");
        $extra = "onChange=\"btn_submit('edit')\"";
        makeCombo($objForm, $arg, $db, $query, $model->search["DISTRICTCD"], "DISTRICTCD", $extra, 1, "BLANK");

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

        //塾リスト表示
        if ($model->cmd == "search") {
            makePriSchoolList($objForm, $arg, $db, $model);
            $model->firstFlg = false;
            $arg["search"] = 1;
        } else if ($model->cmd == "search2") {
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

        View::toHTML($model, "knjxexp_prischoolForm1.html", $arg);
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

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
    }
    $result->free();
    $value = ($value != "") ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//生徒リスト表示
function makePriSchoolList(&$objForm, &$arg, $db, $model)
{
    $query = knjxexp_prischoolQuery::GetPriSchool($model);
    $result = $db->query($query);
    $image  = array(REQUESTROOT ."/image/system/boy1.gif", REQUESTROOT ."/image/system/girl1.gif");
    $i = 0;
    list($path, $cmd) = explode("?cmd=", $model->path[$model->programid]);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $a = array("cmd" => $cmd,
                   "PRISCHOOLCD" => $row["PRISCHOOLCD"],
                   "PRISCHOOL_CLASS_CD" => $row["PRISCHOOL_CLASS_CD"]);

        $row["SELECT_PRISCHOOL_NAME"] = View::alink(REQUESTROOT .$path, htmlspecialchars($row["PRISCHOOL_NAME"]), "target=" .$model->target[$model->programid] ." onclick=\"Link(this)\"",$a);
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
