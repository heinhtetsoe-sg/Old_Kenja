<?php

require_once('for_php7.php');

class knjl345qForm1 {
    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl345qForm1", "POST", "knjl345qindex.php", "", "knjl345qForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = " onchange=\"return btn_submit('knjl345q');\"";
        $query = knjl345qQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1);

        //入試区分コンボボックス
        $extra = "";
        if (SCHOOLKIND == "P") {
            $namecd1Str = "LP24";
        } else {
            $namecd1Str = "L024";
        }
        $query = knjl345qQuery::getNameMst($model->ObjYear, $namecd1Str);
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1);

        //通知日付
        $model->field["NOTICEDATE"] = ($model->field["NOTICEDATE"] == "") ? str_replace("-", "/", CTRL_DATE) :$model->field["NOTICEDATE"];
        $arg["data"]["NOTICEDATE"] = View::popUpCalendarAlp($objForm, "NOTICEDATE", $model->field["NOTICEDATE"], "", "");

        //発送日
        $model->field["SEND_DATE"] = ($model->field["SEND_DATE"] == "") ? str_replace("-", "/", CTRL_DATE) :$model->field["SEND_DATE"];
        $arg["data"]["SEND_DATE"] = View::popUpCalendarAlp($objForm, "SEND_DATE", $model->field["SEND_DATE"], "", "");

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJL345Q");
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "IMAGEPATH", $model->control["LargePhotoPath"]);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl345qForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg) {
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>