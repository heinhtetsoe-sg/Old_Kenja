<?php

require_once('for_php7.php');

class knjl327qForm1 {
    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl327qForm1", "POST", "knjl327qindex.php", "", "knjl327qForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = " onchange=\"return btn_submit('knjl327q');\"";
        $query = knjl327qQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1);

        //入試区分コンボボックス
        $extra = " onchange=\"return btn_submit('knjl327q');\"";
        if (SCHOOLKIND == "J") {
            $query = knjl327qQuery::getNameMst($model->ObjYear, "L024");
        } else {
            $query = knjl327qQuery::getNameMstL004($model->ObjYear);
        }
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1);

        //通知日付
        $model->field["NOTICEDATE"] = str_replace("-", "/", CTRL_DATE);
        $arg["data"]["NOTICEDATE"] = View::popUpCalendarAlp($objForm, "NOTICEDATE", $model->field["NOTICEDATE"], "", "");

        //事前印刷
        $disJizen = ($model->field["TAISYOU"] == "1" || $model->field["TAISYOU"] == "") ? "" : "disabled";
        $extra = "id=\"JIZEN\"".$disJizen;
        $arg["data"]["JIZEN"] = knjCreateCheckBox($objForm, "JIZEN", "1", $extra);

        //ラジオボタン（1:合格通知書、2:不合格通知書、3:スカラー採用通知書、4:スカラー不採用通知書）
        $opt = array(1, 2, 3, 4);
        $model->field["TAISYOU"] = ($model->field["TAISYOU"] == "") ? "1" : $model->field["TAISYOU"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"TAISYOU{$val}\" onClick=\"btn_submit('')\"");
        }
        $radioArray = knjCreateRadio($objForm, "TAISYOU", $model->field["TAISYOU"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //調査書提出期限
        $disabled = ($model->field["TAISYOU"] == "2") ? "" : "disabled";
        $model->field["TEISHUTSUDATE"] = str_replace("-", "/", CTRL_DATE);
        $arg["data"]["TEISHUTSUDATE"] = View::popUpCalendarAlp($objForm, "TEISHUTSUDATE", $model->field["TEISHUTSUDATE"], $disabled, "");

        //ラジオボタン（1:全員、2:受験番号指定）
        $opt = array(1, 2);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"OUTPUT{$val}\" onClick=\"btn_submit('')\"");
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //受験番号指定テキストボックス
        $disExamno = ($model->field["OUTPUT"] == "2") ? "" : "disabled";
        $extra = "onblur=\"this.value=toInteger(this.value)\" style=\"text-align:right;\"".$disExamno;
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $model->field["EXAMNO"], "EXAMNO", 5, 5, $extra);

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
        knjCreateHidden($objForm, "PRGID", "KNJL327Q");
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "IMAGEPATH", $model->control["LargePhotoPath"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl327qForm1.html", $arg); 
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
