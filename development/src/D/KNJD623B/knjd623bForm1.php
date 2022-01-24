<?php

require_once('for_php7.php');

class knjd623bForm1 {
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]  = $objForm->get_start("knjd623bForm1", "POST", "knjd623bindex.php", "", "knjd623bForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //学期コンボ
        $extra = "onChange=\"return btn_submit('chgsemes');\"";
        $query = knjd623bQuery::getSemester();
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->field["SEMESTER"], $extra, 1);

        //学年コンボ
        $extra = "onChange=\"return btn_submit('knjd623b');\"";
        $query = knjd623bQuery::getGrade();
        makeCmb($objForm, $arg, $db, $query, "GRADE", $model->field["GRADE"], $extra, 1);

        //学校校種を取得
        $model->schoolkind = $db->getOne(knjd623bQuery::getSchoolKind($model));

        //テスト種別コンボ
        $extra = "onChange=\"return btn_submit('knjd623b');\"";
        $query = knjd623bQuery::getTestitem($model);
        makeCmb($objForm, $arg, $db, $query, "TESTKIND_ITEMCD", $model->field["TESTKIND_ITEMCD"], $extra, 1);


        
        //異動対象日付（カレンダー）
        if (cmd == "chgsemes") {
            $model->field["SDATE"] = "";
            $model->field["EDATE"] = "";
        }
        $query = knjd623bQuery::getSemesterDetail($model);
        $row_sd = $db->getRow($query, DB_FETCHMODE_ASSOC);
        global $sess;
        makeDataObj($objForm, $arg, $model, "SDATE", 1, $sess, $row_sd["SDATE"]);
        makeDataObj($objForm, $arg, $model, "EDATE", 2, $sess, $row_sd["EDATE"]);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model, $db);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjd623bForm1.html", $arg); 
    }
}

function makeDataObj(&$objForm, &$arg, $model, $dateStr, $btnIdx, $sess, $defStr) {
        //異動対象日付初期値セット
        if ($model->field[$dateStr] == "") $model->field[$dateStr] = str_replace("-", "/", $defStr);
        //異動対象日付（テキスト）
        $disabled = "";
        $extra = "onblur=\"isDate(this); btn_submit('seldate')\"".$disabled;
        $date_textbox = knjCreateTextBox($objForm, $model->field[$dateStr], $dateStr, 12, 12, $extra);
        $extra = "onclick=\"tmp_list('seldate', 'off'); loadwindow('" .REQUESTROOT ."/common/calendar.php?name=".$dateStr."&frame='+getFrameName(self) + '&date=' + document.forms[0]['".$dateStr."'].value + '&CAL_SESSID=$sess->id&reload=true', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 200)\"";
        $date_button = knjCreateBtn($objForm, "btn_calen".$btnIdx, "･･･", $extra);
        //異動対象日付
        $arg["data"][$dateStr] = View::setIframeJs().$date_textbox.$date_button;
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
    knjCreateHidden($objForm, "PRGID", "KNJD623B");
}
?>
