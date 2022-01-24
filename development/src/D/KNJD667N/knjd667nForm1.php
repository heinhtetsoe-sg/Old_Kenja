<?php

require_once('for_php7.php');

class knjd667nForm1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]  = $objForm->get_start("knjd667nForm1", "POST", "knjd667nindex.php", "", "knjd667nForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $extra = "onChange=\"return btn_submit('knjd667n');\"";
        $query = knjd667nQuery::getSemester();
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //学年コンボ
        $extra = "onChange=\"return btn_submit('knjd667n');\"";
        $query = knjd667nQuery::getGrade($model);
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //学校校種を取得
        $model->schoolkind = $db->getOne(knjd667nQuery::getSchoolKind($model));

        //テスト種別コンボ
        $extra = "onChange=\"return btn_submit('knjd667n');\"";
        $query = knjd667nQuery::getTestitem($model);
        makeCmb($objForm, $arg, $db, $query, "TESTKIND_ITEMCD", $model->field["TESTKIND_ITEMCD"], $extra, 1);
        
        //コースコンボ
        //※この学校はCOURSECD、MAJORCDが1種しかない前提なので、テスト時にコンボに重複表示される可能性があるので注意。
        $extra = "onChange=\"return btn_submit('knjd667n');\"";
        $query = knjd667nQuery::getCourseCode($model);
        makeCmb($objForm, $arg, $db, $query, "COURSECODE", $model->field["COURSECODE"], $extra, 1);

        //帳票に引き渡すCOURSECODE->COURSECD、MAJORCDをHIDDEN保持する
        //COURSECODEが被ったら先取りするよう、チェックする。
        //※この学校はCOURSECD、MAJORCDが1種しかない前提だが、念のためCOURSECODEと1対で保持。
        $query = knjd667nQuery::getCourseCode($model, true);
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

        //出欠集計日付
        //学期切り替わり時のみ、DB参照(学期から取得)
        if ($model->field["SEMESTER"] != $model->backSemester) {
            $arryDate = array();
            $query = knjd667nQuery::getSemesterTerm($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->field["DATE_FROM"] = $row["SDATE"];
                $model->field["DATE_TO"] = $model->field["SEMESTER"] == CTRL_SEMESTER ? CTRL_DATE : $row["EDATE"];
            }
            $result->free();
        }
        $arg["data"]["DATE_FROM"] = View::popUpCalendar($objForm, "DATE_FROM", str_replace("-", "/", $model->field["DATE_FROM"]));
        $arg["data"]["DATE_TO"] = View::popUpCalendar($objForm, "DATE_TO", str_replace("-", "/", $model->field["DATE_TO"]));

        //10段階評価ボーダーライン
        $model->field["BORDERVAL"] = $model->field["BORDERVAL"] ? $model->field["BORDERVAL"] : "2";
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
        View::toHTML($model, "knjd667nForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
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
    knjCreateHidden($objForm, "PRGID", "KNJD667N");
    knjCreateHidden($objForm, "HID_SEMESTER", $model->field["SEMESTER"]);
    knjCreateHidden($objForm, "STAFFCD", $model->staffcd);
    knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
}
?>
