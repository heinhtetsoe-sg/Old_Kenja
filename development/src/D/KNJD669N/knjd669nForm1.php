<?php

require_once('for_php7.php');

class knjd669nForm1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]  = $objForm->get_start("knjd669nForm1", "POST", "knjd669nindex.php", "", "knjd669nForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $extra = "onChange=\"return btn_submit('knjd669n');\"";
        $query = knjd669nQuery::getSemester();
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //学年コンボ
        $extra = "onChange=\"return btn_submit('knjd669n');\"";
        $query = knjd669nQuery::getGrade($model);
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //学校校種を取得
        $model->schoolkind = $db->getOne(knjd669nQuery::getSchoolKind($model));

        //テスト種別コンボ
        $extra = "onChange=\"return btn_submit('knjd669n');\"";
        $query = knjd669nQuery::getTestitem($model);
        makeCmb($objForm, $arg, $db, $query, "TESTKIND_ITEMCD", $model->field["TESTKIND_ITEMCD"], $extra, 1);
        
        //クラスコンボ
        $extra = "";
        $query = knjd669nQuery::getClassName($model);
        makeCmb($objForm, $arg, $db, $query, "HR_CLASS", $model->field["HR_CLASS"], $extra, 1, "SELALL");

        //帳票に引き渡すCOURSECODE->COURSECD、MAJORCDをHIDDEN保持する
        //COURSECODEが被ったら先取りするよう、チェックする。
        //※この学校はCOURSECD、MAJORCDが1種しかない前提だが、念のためCOURSECODEと1対で保持。
        $query = knjd669nQuery::getCourseCode($model, true);
        $result = $db->query($query);
        $hidout = array();
        $codeChkList = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (!in_array($row["COURSECODE"], $codeChkList)) {
                $hidout[] = $row["COURSECODE"].'-'.$row["COURSECD"].$row["MAJORCD"];
                $codeChkList[] = $row["COURSECODE"];
            }
        }
        $result->free();
        knjCreateHidden($objForm, "COURSEMAJOR_LIST", implode(",", $hidout));

        //評価合計
        $model->field["BORDERVAL"] = $model->field["BORDERVAL"] ? $model->field["BORDERVAL"] : "4";
        $extra = "id=\"BORDERVAL\" onblur=\"chkSuji(this);\"";
        $arg["data"]["BORDERVAL"] = knjCreateTextBox($objForm, $model->field["BORDERVAL"], "BORDERVAL", 2, 2, $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $db);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd669nForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    if ($blank == "SELALL") {
        $opt[] = array("label" => "全て", "value" => "ALL");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    if ($name != "SEMESTER") {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    } else {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model, $db) {
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "PRGID", "KNJD669N");
    knjCreateHidden($objForm, "STAFFCD", $model->staffcd);
    knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
}
?>
