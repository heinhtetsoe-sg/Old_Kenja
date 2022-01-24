<?php

require_once('for_php7.php');

class knjz182rForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjz182rindex.php", "", "list");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        //年度、学期表示
        $arg["YEAR"] = CTRL_YEAR."年度";

        //更新後に各フィールドにセット
        if ($model->cmd == "up_list") {
            $model->field["TESTCD"] = $model->testcd;
            $model->field["DIV"] = $model->div;
        }

        //参照テスト種別コンボ
        $extra = "onChange=\"return btn_submit('list_change');\" ";
        $query = knjz182rQuery::getTestkindcd();
        makeCmb($objForm, $arg, $db, $query, $model->field["PRE_TESTCD"], "PRE_TESTCD", $extra, 1, "BLANK");

        //対象テスト種別コンボ
        $extra = "onChange=\"return btn_submit('list_change');\" ";
        $query = knjz182rQuery::getTestkindcd();
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTCD"], "TESTCD", $extra, 1, "BLANK");

        //区分ラジオ 1:学年 2:クラス 3:コース 4:学科 5:コースグループ
        $model->field["DIV"] = $model->field["DIV"] ? $model->field["DIV"] : '5';
        $opt_div = array(1, 2, 3, 4, 5);
        $extra = "onClick=\"return btn_submit('list_change')\"";
        $label = array($extra." id=\"DIV1\"", $extra." id=\"DIV2\"", $extra." id=\"DIV3\"", $extra." id=\"DIV4\"", $extra." id=\"DIV5\"");
        $radioArray = knjCreateRadio($objForm, "DIV", $model->field["DIV"], $label, $opt_div, get_count($opt_div));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        if ($model->field["DIV"] === '3') {
            $arg["CourseMajor"] = "1";
            $arg["SET_DIV_NAME"] = 'コース';
        } else {
            $arg["CourseGroup"] = "1";
            $arg["SET_DIV_NAME"] = 'コースグループ';
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //リスト
        makeList($arg, $db, $model);

        //hidden
        makeHidden(&$objForm, $model, $db);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //右画面に渡す
        if ($model->cmd === 'list_change') {
            $model->testcd      = $model->field["TESTCD"];
            $model->div         = $model->field["DIV"];;
        }

        //左のフレームを表示し終わってから右のフレームを表示しないとセッションの値がレスポンスのタイミングによって変わる
        //indexの「分割フレーム作成」では右フレームを呼ばない。
        if (VARS::get("shori") != "add") {
            $arg["reload"] = "window.open('knjz182rindex.php?cmd=sel','right_frame')";
        }

        View::toHTML($model, "knjz182rForm1.html", $arg); 
    }
}

//権限チェック
function authCheck(&$arg) {
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }

    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//コースグループリスト
function makeList(&$arg, $db, $model) {
    $g_cnt = $c_cnt = 1;
    $result = $db->query(knjz182rQuery::getLeftList($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //行数取得
        $grade_cnt  = $db->getOne(knjz182rQuery::getGradeCnt($model, $row["GRADE"]));
        $course_cnt = $db->getOne(knjz182rQuery::getCourseGroupCnt($model, $row["GRADE"], $row["SET_CD"]));

        $row["SET_NAME"] = View::alink("knjz182rindex.php",
                            $row["SET_NAME"],
                            "target=right_frame",
                            array("cmd"         => "sel",
                                  "SEND_FLG"    => "1",
                                  "SEND_TESTCD"     => $row["TESTCD"],
                                  "SEND_DIV"        => $row["DIV"],
                                  "SEND_GRADE"      => $row["GRADE"],
                                  "SEND_HR_CLASS"   => $row["HR_CLASS"],
                                  "SEND_COURSECD"   => $row["COURSECD"],
                                  "SEND_MAJORCD"    => $row["MAJORCD"],
                                  "SEND_COURSECODE" => $row["COURSECODE"]));


        if ($g_cnt == 1) $row["ROWSPAN1"] = $grade_cnt;     //学年の行数
        if ($c_cnt == 1) $row["ROWSPAN2"] = $course_cnt;    //コースグループの行数

        $arg["data"][] = $row;

        if ($g_cnt == $grade_cnt) {
            $g_cnt = 1;
        } else {
            $g_cnt++;
        }
        if ($c_cnt == $course_cnt) {
            $c_cnt = 1;
        } else {
            $c_cnt++;
        }
    }
    $result->free();

}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //コピーボタン
    //$extra = "onclick=\"return btn_submit('copy');\"";
    //$arg["COPYBTN"] = knjCreateBtn($objForm, "COPYBTN", "前年度からコピー", $extra);
    //コピーボタン
    $extra = "style=\"width:150px\" onclick=\"return btn_submit('copy');\" ";
    $arg["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "左のデータをコピー", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $db) {
    knjCreateHidden($objForm, "cmd");
    $queryMoto = knjz182rQuery::getCntCopyQuery($model, $model->field["PRE_TESTCD"]);
    $querySaki = knjz182rQuery::getCntCopyQuery($model, $model->field["TESTCD"]);
    knjCreateHidden($objForm, "COPY_MOTO_CNT", $db->getOne($queryMoto));
    knjCreateHidden($objForm, "COPY_SAKI_CNT", $db->getOne($querySaki));
}
?>
