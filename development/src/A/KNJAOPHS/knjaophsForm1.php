<?php

require_once('for_php7.php');

class knjaophsForm1 {

    function main(&$model) {

        $objForm = new form;

        $arg["start"] = $objForm->get_start("edit", "POST", "knjaophsindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TITLE"]["YEAR"] = CTRL_YEAR;

        //年度コンボ作成
        $query = knjaophsQuery::getYear();
        $extra = "onchange=\"return btn_submit('edit'), AllClearList();\"";
        makeCmb($objForm, $arg, $db, $query, "YEAR", $model->field["YEAR"], $extra, 1, "ALL");

        //学期コンボ作成
//        $query = knjaophsQuery::getSemester($model);
//        $extra = "onchange=\"return btn_submit('edit'), AllClearList();\"";
//        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1, "ALL");

        //学年コンボ作成
//        $query = knjaophsQuery::getGrade($model->field["SEMESTER"], $model, "GRADE");
//        $extra = "onchange=\"return btn_submit('edit'), AllClearList();\"";
//        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1, "ALL");

        //リストToリスト作成
//        makeStudentList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $ineiFlg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = VIEW::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjaophsForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $ALL = "")
{
    $opt = array();
    if ($ALL) {
        $opt[] = array('label' => "全て",
                       'value' => "");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

function makeStudentList(&$objForm, &$arg, $db, $model) {

    //対象クラスリストを作成する
    $query = knjaophsQuery::getGradeHrClass($model);
    $result = $db->query($query);
    $opt1 = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt1[] = array('label' => $row["LABEL"],
                        'value' => $row["VALUE"]);
    }
    $result->free();
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt1, $extra, 20);

    $arg["data"]["NAME_LIST"] = 'クラス一覧';

    //出力対象一覧リストを作成する//
    $extra = "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 20);

    //extra
    $extra_rights = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $extra_lefts  = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $extra_right1 = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $extra_left1  = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";

    //対象選択ボタンを作成する
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra_rights);
    //対象取消ボタンを作成する
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra_lefts);
    //対象選択ボタンを作成する
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra_right1);
    //対象取消ボタンを作成する
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra_left1);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $ineiFlg, $model)
{
    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"deleteCookie(); closeWin();\"");

    //署名
    $extraDis = "";
    if ($model->exe_type == "CHARGE") {
        //担当
        if ($ineiFlg["CHAGE_OPI_SEQ"]) {
            $extraDis = " disabled ";
        }
        $extra = "onclick=\"return btn_submit('shomei');\"";
        $arg["button"]["btn_shomei"] = knjCreateBtn($objForm, "btn_check1", "署名", $extraDis.$extra);
    } else if ($model->exe_type == "PRINCIPAL") {
        if ($ineiFlg["CHAGE_OPI_SEQ"]) {
            //校長
            if ($ineiFlg["LAST_OPI_SEQ"]) {
                $extraDis = "disabled ";
            }
        } else {
            $extraDis = "disabled ";
        }
        $extra = "onclick=\"return btn_submit('sslApplet');\"";
        $arg["button"]["btn_shomei"] = knjCreateBtn($objForm, "btn_check1", "校長署名", $extraDis.$extra);

        $extra = "onclick=\"return btn_submit('sasimodosi');\"";
        $arg["button"]["btn_sasimodosi"] = knjCreateBtn($objForm, "btn_sasimodosi", "差戻し", $extraDis.$extra);
    }
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "SIGNATURE", $model->signature);
    knjCreateHidden($objForm, "GOSIGN", $model->gosign);
    knjCreateHidden($objForm, "RNDM", $model->rndm);
    knjCreateHidden($objForm, "selectData");
}
?>
