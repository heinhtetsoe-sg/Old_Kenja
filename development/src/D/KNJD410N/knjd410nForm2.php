<?php

require_once('for_php7.php');

class knjd410nForm2
{
    public function main(&$model)
    {

        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd410nindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //学部表示
        $arg["SCHOOL_KIND"] = $db->getOne(knjd410nQuery::getSchoolKind($model->gakubu_school_kind));
        
        //状態表示
        if ($model->condition) {
            $arg["CONDITION"] = $db->getOne(knjd410nQuery::getCondition($model->condition));
        }
        //グループコード
        if ($model->condition != "" && $model->groupcd != "") {
            $arg["GROUP_NAME"] = $db->getOne(knjd410nQuery::getGroupName($model, $model->condition, $model->groupcd));
        }

        //リストToリスト作成
        makeStudentList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm);

        if (VARS::post("cmd") == "update") {
            $arg["jscript"] = "window.open('knjd410nindex.php?cmd=list&shori=update','left_frame');";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd410nForm2.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $result = $db->query($query);
    $opt = array();
    $serch = array();

    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        $serch[] = $row["VALUE"];
    }

    $value = ($value && in_array($value, $serch)) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//生徒リストToリスト作成
function makeStudentList(&$objForm, &$arg, $db, $model)
{
    //左生徒リスト(溜める式)
    $selectdata      = ($model->selectdata != "")       ? explode(",", $model->selectdata)      : array();
    $selectdataLabel = ($model->selectdataLabel != "")  ? explode(",", $model->selectdataLabel) : array();

    //左生徒リスト
    $cnt = 0;
    $opt_left = array();
    if (isset($model->warning)) {
        for ($i = 0; $i < get_count($selectdata); $i++) {
            $opt_left[] = array("label" => $selectdataLabel[$i],
                                "value" => $selectdata[$i]);
            $cnt++;
        }
    } else {
        $result = $db->query(knjd410nQuery::getGhrStudents($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_left[] = array("label" => $row["HR_NAME"].$row["ATTENDNO"]."番　".$row["SCHREGNO"]."　".$row["NAME"],
                                "value" => $row["GHR_DIV"].'-'.$row["GHA"].'-'.$row["SCHREGNO"]);
            $cnt++;
        }
        $result->free();
    }
    $arg["RIGHT_NUM"] = $cnt;

    //右生徒リスト
    $cnt = 0;
    $opt_right = array();
    if ($model->condition != "" && $model->groupcd != "") {
        $result = $db->query(knjd410nQuery::getHrStudents($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_right[]= array("label" => $row["HR_NAME"].$row["ATTENDNO"]."番　".$row["SCHREGNO"]."　".$row["NAME"],
                                "value" => $row["GHR_DIV"].'-'.$row["GHA"].'-'.$row["SCHREGNO"]);
            $cnt++;
        }
        $result->free();
    }
    $arg["LEFT_NUM"] = $cnt;

    //生徒一覧リスト(右)
    $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"move1('left')\"";
    $arg["main_part"]["RIGHT_PART"] = knjCreateCombo($objForm, "RIGHT_PART", "", $opt_right, $extra, 25);

    //対象者一覧リスト(左)
    $extra = "multiple style=\"width:100%\" width=\"100%\" ondblclick=\"move1('right')\"";
    $arg["main_part"]["LEFT_PART"] = knjCreateCombo($objForm, "LEFT_PART", "", $opt_left, $extra, 25);

    //対象選択ボタン
    $extra = "onclick=\"moves('left');\"";
    $arg["main_part"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "≪", $extra);
    //対象選択ボタン
    $extra = "onclick=\"move1('left');\"";
    $arg["main_part"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);
    //対象取消ボタン
    $extra = "onclick=\"move1('right');\"";
    $arg["main_part"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);
    //対象取消ボタン
    $extra = "onclick=\"moves('right');\"";
    $arg["main_part"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", "≫", $extra);
}
//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //追加ボタン
    $extra = (AUTHORITY < DEF_UPDATE_RESTRICT) ? "disabled" : "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//Hidden作成
function makeHidden(&$objForm)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "selectdataLabel");
    knjCreateHidden($objForm, "selectschno");
}
