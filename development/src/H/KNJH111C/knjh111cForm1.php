<?php

require_once('for_php7.php');

class knjh111cForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjh111cindex.php", "", "edit");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学年コンボ
        $query = knjh111cQuery::getGrade($model);
        $extra = "onchange=\"return btn_submit('list');\"";
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->grade, $extra, 1, "");

        //組コンボ
        $query = knjh111cQuery::GetHr_Class($model);
        $extra = "onchange=\"return btn_submit('list');\"";
        makeCmb($objForm, $arg, $db, $query, "HR_CLASS", $model->hrClass, $extra, 1, "");

        //資格コンボ
        $query = knjh111cQuery::getQualifiedMst();
        $extra = "onchange=\"return btn_submit('list');\"";
        makeCmb($objForm, $arg, $db, $query, "SIKAKUCD", $model->sikakuCd, $extra, 1, "");

        //試験日コンボ
        $query = knjh111cQuery::getTestDate($model);
        $extra = "onchange=\"return btn_submit('list');\"";
        makeCmb($objForm, $arg, $db, $query, "TOP_TEST_DATE", $model->topTestDate, $extra, 1, "");

        //受験級コンボ
        $query = knjh111cQuery::getTestCdLeft($model);
        $extra = "onchange=\"return btn_submit('list');\"";
        makeCmb($objForm, $arg, $db, $query, "TOP_TEST_CD", $model->topTestCd, $extra, 1, "");

        //リスト作成
        makeList($arg, $db, $model);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd == "list") {
            $arg["reload"]  = "parent.right_frame.location.href='knjh111cindex.php?cmd=edit&chFlg=1&SIKAKUCD={$model->sikakuCd}&GRADE={$model->grade}&HR_CLASS={$model->hrClass}&TOP_TEST_DATE={$model->topTestDate}&TOP_TEST_CD={$model->topTestCd}';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjh111cForm1.html", $arg); 
    }
}

//リスト作成
function makeList(&$arg, $db, $model) {
    $result = $db->query(knjh111cQuery::getList($model));

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
         array_walk($row, "htmlspecialchars_array");
         $row["TEST_DATE"]     = str_replace('-', '/', $row["TEST_DATE"]);
         $row["BEF_TEST_DATE"] = str_replace('-', '/', $row["BEF_TEST_DATE"]);

         $arg["data"][] = $row;
    }

    $result->free();
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($name == "TOP_TEST_DATE") {
            $row["LABEL"] = str_replace('-', '/', $row["LABEL"]);
        }
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    if ($name == "YEAR") {
        $value = ($value != "" && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
//権限チェック
function authCheck(&$arg) {
    if (AUTHORITY != DEF_UPDATABLE) {
        $arg["jscript"] = "OnAuthError();";
    }
}
?>
