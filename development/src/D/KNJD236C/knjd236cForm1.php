<?php

require_once('for_php7.php');


class knjd236cForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd236cForm1", "POST", "knjd236cindex.php", "", "knjd236cForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ作成
        $query = knjd236cQuery::getSemester();
        $extra = "onchange=\"return btn_submit('knjd236c'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);
        //学年末は最新の学期とする
        $query = knjd236cQuery::getMaxSemester();
        $seme = $model->field["SEMESTER"] == "9" ? $db->getOne($query) : $model->field["SEMESTER"];

        //学年コンボ作成
        $query = knjd236cQuery::getGrade($seme);
        $extra = "onchange=\"return btn_submit('knjd236c'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //グループリストToリスト作成
        makeClassList($objForm, $arg, $db, $model, $seme);

        //欠点者
        $query = knjd236cQuery::getJHFlg();
        $first_val = ($db->getOne($query) && $model->field["GRADE"] <= "03") ? "60" : "40";
        $value = ($model->field["BAD_SCORE"]) ? $model->field["BAD_SCORE"] : $first_val;
        $extra = " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["BAD_SCORE"] = knjCreateTextBox($objForm, $value, "BAD_SCORE", 4, 2, $extra);

        //下位者
        $value = ($model->field["RANK_NO"]) ? $model->field["RANK_NO"] : "10";
        $extra = " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["RANK_NO"] = knjCreateTextBox($objForm, $value, "RANK_NO", 4, 2, $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $seme);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd236cForm1.html", $arg); 
    }
}

function makeClassList(&$objForm, &$arg, $db, $model, $seme) {

    //グループ一覧リストを作成する
    $query = knjd236cQuery::getHrClass($seme, $model->field["GRADE"]);
    $result = $db->query($query);
    $opt1 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt1[] = array('label' => $row["VALUE"]."：".$row["LABEL"],
                        'value' => $row["VALUE"]);
    }
    $result->free();
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CLASS_NAME"] = knjCreateCombo($objForm, "CLASS_NAME", "", $opt1, $extra, 20);

    //対象グループリストを作成する
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CLASS_SELECTED"] = knjCreateCombo($objForm, "CLASS_SELECTED", "", array(), $extra, 20);

    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        if ($name == "GRADE"){
            $opt[] = array('label' => sprintf("%d",$row["LABEL"]).'学年',
                           'value' => $row["VALUE"]);
        } else {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeBtn(&$objForm, &$arg) {
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm, $model, $seme) {
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "PRGID", "KNJD236C");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);
}

?>
