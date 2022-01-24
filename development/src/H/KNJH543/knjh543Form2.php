<?php

require_once('for_php7.php');

class knjh543Form2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("sel", "POST", "knjh543index.php", "", "sel");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && $model->cmd != "change" && $model->send_flg == "A_SUB") {
            $query = knjh543Query::getProficiencySubclassRepCombDat($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //合併先科目表示
        $subclassname = "";
        $subclassname = $db->getOne(knjh543Query::getProficiencySubclassName($model->proficiency_subclass_cd));
        $arg["data"]["PROFICIENCY_SUBCLASS_CD"] = ($subclassname) ? $subclassname : '左のリストから選択してください。';
        $arg["FONT_COLOR"] = ($subclassname) ? "black" : "red";

        //コンボ使用不可
        $disabled = ($subclassname) ? "" : " disabled";

        //学年コンボ
        $query = knjh543Query::getGrade($model);
        $extra = "onChange=\"btn_submit('change')\";";
        makeCmb($objForm, $arg, $db, $query, $Row["GRADE"], "GRADE", $extra.$disabled);

        //初期値セット
        if($model->div == "") $model->div = "1";

        //課程学科コース／コースグループコンボ
        if($model->div == "1") {
            $query = knjh543Query::getCourseMajor($model, $Row["GRADE"]);
            $arg["data"]["TITLE"] = '課程学科コース';
        } else {
            $query = knjh543Query::getCourseGroup($Row["GRADE"]);
            $arg["data"]["TITLE"] = 'コースグループ';
        }
        $extra = "onChange=\"btn_submit('change')\";";
        makeCmb($objForm, $arg, $db, $query, $Row["COURSE"], "COURSE", $extra.$disabled);

        //タイトル設定
        makeTitle($arg, $db);

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $Row, "SUBCLASS", "0", 15, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (isset($model->message)) {   //更新できたら左のリストを再読込
            $arg["reload"] = "window.open('knjh543index.php?cmd=list_update', 'left_frame')";
        }

        View::toHTML($model, "knjh543Form2.html", $arg); 
    }
}

//タイトル設定
function makeTitle(&$arg, $db) {
    $arg["info"]    = array("LEFT_LIST"     => "合併元科目一覧",
                            "RIGHT_LIST"    => "科目一覧" );
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra) {
    $opt = array();
    $opt[] = array("label" => $row["LABEL"], "value" => $row["VALUE"]);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
    }
    $value = ($value) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, 1);
}

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, &$Row, $name, $target, $size, $model) {
    $opt_left = $opt_right = array();

    //extraセット
    if ($model->Properties["weightingHyouki"] == "1") {
        $extraWeight = "onblur=\"calcProperties(this);\"";
        $size = "5";
    } else {
        $extraWeight = "onblur=\"calc(this);\"";
        $size = "4";
    }
    if ($Row["GRADE"] && $Row["COURSE"] && $model->proficiency_subclass_cd) {
        $query = knjh543Query::selectQuery($model, $Row["GRADE"], $Row["COURSE"], $model->proficiency_subclass_cd);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["JOINCD"]) {
                $opt_left[]  = array("label" => $row["LABEL"], 
                                     "value" => $row["VALUE"]);

                $setData["KAKUSI"] = "<input type=\"hidden\" name=\"SUBCLASS_CD[]\" value=\"".$row["VALUE"]."\">";
                $setData["WEIGHTING"] = knjCreateTextBox($objForm, $row["WEIGHTING"], "WEIGHTING".$row["VALUE"], $size, $size, $extraWeight);
                $setData["ATTEND_SUBCLASS_NAME"] = $row["LABEL"];
                $arg["data2"][] = $setData;
            } else {
                $opt_right[] = array("label" => $row["LABEL"],
                                     "value" => $row["VALUE"]);
            }
        }
        $result->free();
    }

    //対象考査科目
    $extra = "multiple style=\"width:400px\" ondblclick=\"move('right', '".$name."')\"";
    $arg["main_part"][$name."LEFT_PART"]   = knjCreateCombo($objForm, "L".$name, "right", $opt_left, $extra, $size);

    //考査科目一覧
    $extra = "multiple style=\"width:400px\" ondblclick=\"move('left', '".$name."')\"";
    $arg["main_part"][$name."RIGHT_PART"]  = knjCreateCombo($objForm, "R".$name, "left", $opt_right, $extra, $size);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"return moves('sel_add_all', '".$name."');\"";
    $arg["main_part"][$name."SEL_ADD_ALL"] = knjCreateBtn($objForm, $name."sel_add_all", "≪", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move('left', '".$name."');\"";
    $arg["main_part"][$name."SEL_ADD"]     = knjCreateBtn($objForm, $name."sel_add", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"return move('right', '".$name."');\"";
    $arg["main_part"][$name."SEL_DEL"]     = knjCreateBtn($objForm, $name."sel_del", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"return moves('sel_del_all', '".$name."');\"";
    $arg["main_part"][$name."SEL_DEL_ALL"] = knjCreateBtn($objForm, $name."sel_del_all", "≫", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {

    //更新ボタン
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", "onclick=\"return doSubmit('update');\"");
    //更新ボタン
    $arg["button"]["btn_update2"] = knjCreateBtn($objForm, "btn_update2", "更 新", "onclick=\"return doSubmit('updateWeight');\"");
    //取消ボタン
    $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", "onclick=\"return btn_submit('clear');\"");
    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "COURSE_DIV", $model->div);
    knjCreateHidden($objForm, "P_SUBCLASSCD", $model->proficiency_subclass_cd);
}
?>
