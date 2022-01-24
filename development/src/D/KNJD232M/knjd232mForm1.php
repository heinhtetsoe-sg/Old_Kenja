<?php

require_once('for_php7.php');

class knjd232mForm1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjd232mForm1", "POST", "knjd232mindex.php", "", "knjd232mForm1");

        //DB接続
        $db = Query::dbCheckOut();
        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボボックスを作成する
        $query = knjd232mQuery::getSemester();
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], "onchange=\"return btn_submit('gakki');\"", 1);

        //学年末の場合、$semeを今学期にする。
        $seme = $model->field["SEMESTER"];
        if ($seme == 9) {
            $seme    = CTRL_SEMESTER;
            $semeflg = CTRL_SEMESTER;
        } else {
            $semeflg = $seme;
        }

        //学年コンボ
        $query = knjd232mQuery::getSelectGrade($model);
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], "onChange=\"return btn_submit('grade');\"", 1);

        //序列
        $opt = array(1, 2, 3); //1:学年 2:クラス 3:コース
        $model->field["JORETU_DIV"] = ($model->field["JORETU_DIV"] == "") ? "1" : $model->field["JORETU_DIV"];
        $extra = array("id=\"JORETU_DIV1\"", "id=\"JORETU_DIV2\"", "id=\"JORETU_DIV3\"");
        $radioArray = knjCreateRadio($objForm, "JORETU_DIV", $model->field["JORETU_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //序列の基準点
        $opt = array(1, 2); //1:総合点 2:平均点
        $query = knjd232mQuery::getSchregRegdGdat($model->field["GRADE"]);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $syokiti = $model->field["JORETU_BASE"] ? $model->field["JORETU_BASE"] : 1;
        if ($model->cmd == 'grade' || $model->cmd == '') {
            if ($row["SCHOOL_KIND"] == 'H' && $row["GRADE_CD"] >= '02') {
                $syokiti = 2;
            } else {
                $syokiti = 1;
            }
        }
        $extra = array("id=\"JORETU_BASE1\"", "id=\"JORETU_BASE2\"");
        $radioArray = knjCreateRadio($objForm, "JORETU_BASE", $syokiti, $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //対象日付
        $arg["data"]["TAISYOU_DATE"] = View::popUpCalendar($objForm,"TAISYOU_DATE", str_replace("-", "/", CTRL_DATE));

        //判定区分で改ページチェックボックス
        $checked = $model->field["PAGE_CHANGE"] == "1" ? " checked" : "";
        $extra = "id=\"PAGE_CHANGE\"".$checked;
        $arg["data"]["PAGE_CHANGE"] = knjCreateCheckBox($objForm, "PAGE_CHANGE", "1", $extra);


        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hiddenを作成する
        makeHidden($objForm, $model, $seme, $semeflg);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd232mForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model = "") {
    $opt = array();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();
    if ($name == "SEMESTER") {
        $value = ($value == "") ? CTRL_SEMESTER : $value;
    } else {
        $value = ($value == "") ? $opt[0]["value"] : $value;
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//チェックボックスを作成する
function makeCheckBox(&$objForm, &$arg, $model, $name) {
    $extra  = ($model->field[$name] == "1") ? "checked" : "";
    $extra .= " id=\"$name\"";
    $value = isset($model->field[$name]) ? $model->field[$name] : 1;

    $arg["data"][$name] = knjCreateCheckBox($objForm, $name, $value, $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //実行ボタン
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", "onclick=\"return newwin('" . SERVLET_URL . "');\"");
    //閉じるボタン
    $arg["button"]["btn_end"]   = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, $model, $seme, $semeflg) {
    knjCreateHidden($objForm, "CHK_SDATE", CTRL_YEAR . "/04/01");
    knjCreateHidden($objForm, "CHK_EDATE", (CTRL_YEAR + 1) . "/03/31");
    knjCreateHidden($objForm, "DBNAME",            DB_DATABASE);
    knjCreateHidden($objForm, "PRGID" ,            "KNJD232M");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "YEAR",              CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_YEAR",         CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER",     CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE",         CTRL_DATE);
    knjCreateHidden($objForm, "useCurriculumcd",   $model->Properties["useCurriculumcd"]);
    knjCreateHidden($objForm, "useClassDetailDat", $model->Properties["useClassDetailDat"]);
}
?>
