<?php

require_once('for_php7.php');

class knja114Form1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knja114index.php", "", "edit");

        //権限チェック
        authCheck($arg);

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["YEAR"] = CTRL_YEAR;

        //学期
        $arg["SEMESTER"] = $db->getOne(knja114Query::getSemesterName());

        //年組コンボ
        $query = knja114Query::getAuth($model);
        $extra = "onchange=\"return btn_submit('list');\"";
        makeCmb($objForm, $arg, $db, $query, "LEFT_GRADE_HR_CLASS", $model->gradeHrClass, $extra, 1, "");

        //システムコンボ
        $query = knja114Query::getSystemName();
        $extra = "onchange=\"return btn_submit('list');\"";
        makeCmb($objForm, $arg, $db, $query, "SYSTEMID", $model->systemId, $extra, 1, "");

        //リスト作成
        makeList($arg, $db, $model);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knja114Form1.html", $arg); 
    }
}

//リスト作成
function makeList(&$arg, $db, $model) {
    $result = $db->query(knja114Query::getList($model));

    $fuseji = "*";
    $ume = "";
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");
        $row["TEST_DATE"]     = str_replace('-', '/', $row["TEST_DATE"]);
        $row["BEF_TEST_DATE"] = str_replace('-', '/', $row["BEF_TEST_DATE"]);
        $cnt = mb_strlen($row["PASSWORD"]);
        for ($umecnt = 1; $umecnt <= $cnt; $umecnt++) {
            $ume .= $fuseji;
        }
        $row["PASSWORD"] = $ume;

        $ume = "";
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
