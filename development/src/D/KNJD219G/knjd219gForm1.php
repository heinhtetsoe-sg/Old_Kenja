<?php

require_once('for_php7.php');


class knjd219gForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("main", "POST", "knjd219gindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //権限チェック:更新可
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        //年度表示
        $arg["YEAR"] = CTRL_YEAR . "年度";

        //テスト区分　1:考査 2:学期成績
        $opt = array(1, 2);
        $model->field["TEST_FLG"] = ($model->field["TEST_FLG"] == "") ? "2" : $model->field["TEST_FLG"];
        $extra = array("id=\"TEST_FLG1\" onClick=\"return btn_submit('main');\"", "id=\"TEST_FLG2\" onClick=\"return btn_submit('main');\"");
        $radioArray = knjCreateRadio($objForm, "TEST_FLG", $model->field["TEST_FLG"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //サブミット
        $extra = "onchange=\"return btn_submit('main')\"";

        $model->z010 = $db->getOne(knjd219gQuery::getZ010());

        /**********/
        /* コンボ */
        /**********/
        //学年
        $query = knjd219gQuery::getGrade($model);
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);
        //学期
        $query = knjd219gQuery::getSemester($model);
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);
        //テスト
        $query = knjd219gQuery::getTest($model);
        makeCmb($objForm, $arg, $db, $query, "TESTKIND", $model->field["TESTKIND"], $extra, 1);
        //コース
        $query = knjd219gQuery::getCourse($model);
        makeCmb($objForm, $arg, $db, $query, "COURSE", $model->field["COURSE"], $extra, 1);

        //リストToリスト
        makeListToList($objForm, $arg, $db, $model);

        //履歴表示
        makeListRireki($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjd219gForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
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

//読替先科目一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {
    //処理対象科目(左のリスト）
    $array  = (strlen($model->selectdata) ? explode(",", $model->selectdata) : array());
    //読替先科目一覧
    $leftList = $rightList = array();
    $query = knjd219gQuery::getList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (in_array($row["VALUE"], $array)) {
            $leftList[]  = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        } else {
            $rightList[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        }
    }
    $result->free();

    //読替先科目一覧作成
    $extra = "multiple style=\"width:350px\" width:\"350px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $rightList, $extra, 15);

    //実行対象作成
    $extra = "multiple style=\"width:350px\" width:\"350px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $leftList, $extra, 15);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
}

//履歴一覧
function makeListRireki(&$objForm, &$arg, $db, $model) {
    $query = knjd219gQuery::getListRireki($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row["CALC_DATE"] = str_replace("-","/",$row["CALC_DATE"]);
        $arg["data2"][] = $row;
    }
    $result->free();
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //実行ボタン
    $extra = "onclick=\"return btn_submit('execute');\"";
    $arg["button"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

function makeHidden(&$objForm) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
}

?>
