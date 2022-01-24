<?php

require_once('for_php7.php');

class knjl321uForm1 {
    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl321uForm1", "POST", "knjl321uindex.php", "", "knjl321uForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = " onchange=\"return btn_submit('knjl321u');\"";
        $query = knjl321uQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1);

        //入試区分コンボボックス
        $extra = " onchange=\"return btn_submit('knjl321u');\"";
        $query = knjl321uQuery::getNameMst($model->ObjYear, "L004");
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1);

        //通知日付
        $model->field["NOTICEDATE"] = ($model->field["NOTICEDATE"] == "") ? str_replace("-", "/", CTRL_DATE) :$model->field["NOTICEDATE"];
        $arg["data"]["NOTICEDATE"] = View::popUpCalendarAlp($objForm, "NOTICEDATE", $model->field["NOTICEDATE"], "", "");

        //ラジオボタン（1:合格証、2:入学許可書、3:入学通知書）
        $opt = array(1, 2, 3);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"OUTPUT{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        $optB = array("A", "B", "C");
        for ($i = 1; $i <= 3; $i++) {
            //出力範囲ラジオボタン 1:合格者全員 2:志願者指定 3:受験者指定
            $opt_outputA = array(1, 2, 3);
            $optOutName = "OUTPUT".$optB[$i - 1];		
            $extra = array("id=\"".$optOutName."1\"", "id=\"".$optOutName."2\"", "id=\"".$optOutName."3\"");
            $model->field[$optOutName] = ($model->field[$optOutName] == "") ? "1" : $model->field[$optOutName];
            $radioArray = knjCreateRadio($objForm, $optOutName, $model->field[$optOutName], $extra, $opt_outputA, get_count($opt_outputA));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

            //受験番号テキストボックス
            $setExName = "EXAMNO".$optB[$i - 1];
            $value = ($model->field[$setExName]) ? $model->field[$setExName] : "";
            $extra_examA = " id=\"".$setExName."\" STYLE=\"text-align: right\"; onBlur=\"this.value=toInteger(this.value);\"";
            $arg["data"][$setExName] = knjCreateTextBox($objForm, $value, $setExName, 5, 5, $extra_examA);
        }

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
        knjCreateHidden($objForm, "PRGID", "KNJL321U");
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
        knjCreateHidden($objForm, "IMAGEPATH", $model->control["LargePhotoPath"]);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl321uForm1.html", $arg); 
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
    if ($name === "TESTDIV") {
        $opt[] = array('label' => "－全て－",
                       'value' => "Z");
        if ($value === "Z") $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
