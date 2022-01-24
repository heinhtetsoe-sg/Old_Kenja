<?php

require_once('for_php7.php');

class knjl344qForm1 {
    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl344qForm1", "POST", "knjl344qindex.php", "", "knjl344qForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = " onchange=\"return btn_submit('knjl344q');\"";
        $query = knjl344qQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1);

        //入試区分コンボボックス
        if (SCHOOLKIND == "P") {
            $query = knjl344qQuery::getNameMst($model->ObjYear, "LP24");
            $arg["SCHOOLKIND_P"] = 1;
        } else if (SCHOOLKIND == "J") {
            $query = knjl344qQuery::getNameMst($model->ObjYear, "L024");
            $arg["SCHOOLKIND_J"] = 1;
        }
        $extra = " onchange=\"return btn_submit('knjl344q');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1);

        //通知日付
        $model->field["NOTICEDATE"] = ($model->field["NOTICEDATE"] == "") ? str_replace("-", "/", CTRL_DATE) :$model->field["NOTICEDATE"];
        $arg["data"]["NOTICEDATE"] = View::popUpCalendarAlp($objForm, "NOTICEDATE", $model->field["NOTICEDATE"], "", "");

        //事前印刷
        $disJizen = ($model->field["TAISYOU"] == "1" || $model->field["TAISYOU"] == "") ? "" : "disabled";
        $extra = "id=\"JIZEN\"".$disJizen;
        $arg["data"]["JIZEN"] = knjCreateCheckBox($objForm, "JIZEN", "1", $extra);

        //事前印刷
        $disJizen4 = ($model->field["TAISYOU"] == "4") ? "" : "disabled";
        $extra = "id=\"JIZEN_NKYOKA\"".$disJizen4;
        $arg["data"]["JIZEN_NKYOKA"] = knjCreateCheckBox($objForm, "JIZEN_NKYOKA", "1", $extra);

        //ラジオボタン（1:合格通知書、2:結果通知書、3:入学通知書、4:入学許可書）
        $opt = array(1, 2, 3, 4);
        $model->field["TAISYOU"] = ($model->field["TAISYOU"] == "") ? "1" : $model->field["TAISYOU"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"TAISYOU{$val}\" onClick=\"btn_submit('')\"");
        }
        if ($model->field["TESTDIV"] == "9") {
            $extra[1] .= " disabled";
        } else {
            $extra[2] .= " disabled";
        }
        $radioArray = knjCreateRadio($objForm, "TAISYOU", $model->field["TAISYOU"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

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
        $ketasuu = (SCHOOLKIND == "P") ? 4 : 5;
        $disExamno = ($model->field["OUTPUT"] == "2") ? "" : "disabled";
        $extra = "onblur=\"this.value=toInteger(this.value)\" style=\"text-align:right;\"".$disExamno;
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $model->field["EXAMNO"], "EXAMNO", $ketasuu, $ketasuu, $extra);

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
        knjCreateHidden($objForm, "PRGID", "KNJL344Q");
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "IMAGEPATH", $model->control["LargePhotoPath"]);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl344qForm1.html", $arg); 
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
