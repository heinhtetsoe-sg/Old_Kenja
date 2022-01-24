<?php

require_once('for_php7.php');

class knjl330fForm1 {

    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl330fForm1", "POST", "knjl330findex.php", "", "knjl330fForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = " onchange=\"return btn_submit('knjl330fApplicantdiv');\"";
        $query = knjl330fQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1);

        //入試区分コンボボックス
        $extra = " onchange=\"return btn_submit('knjl330fTestdiv');\"";
        $namecd1 = ($model->field["APPLICANTDIV"] == "1") ? "L024" : "L004";
        $query = knjl330fQuery::getNameMst($model->ObjYear, $namecd1);
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1);

        //高校のみ
        if ($model->field["APPLICANTDIV"] == "2") {
            //入試回数コンボボックス
            $query = knjl330fQuery::getTestdiv0($model->ObjYear, $model->field["TESTDIV"]);
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV0"], "TESTDIV0", $extra, 1);
        }

        //出力対象選択ラジオボタン 1:合格者全員 2:志願者全員 3:受験者指定
        $opt_output = array(1, 2, 3);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "2" : $model->field["OUTPUT"];
        $click = " onclick=\"OptionUse(this);\"";
        $extra = array("id=\"OUTPUT1\"".$click, "id=\"OUTPUT2\"".$click, "id=\"OUTPUT3\"".$click);
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_output, get_count($opt_output));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //FROM受験番号テキストボックス
        $extra  = "onblur=\"this.value=toInteger(this.value)\"";
        $extra .= ($model->field["OUTPUT"] == "3") ? "" : " disabled";
        $arg["data"]["F_EXAMNO"] = knjCreateTextBox($objForm, $model->field["F_EXAMNO"], "F_EXAMNO", 5, 5, $extra);

        //TO受験番号テキストボックス
        $extra  = "onblur=\"this.value=toInteger(this.value)\"";
        $extra .= ($model->field["OUTPUT"] == "3") ? "" : " disabled";
        $arg["data"]["T_EXAMNO"] = knjCreateTextBox($objForm, $model->field["T_EXAMNO"], "T_EXAMNO", 5, 5, $extra);

        //納入期限
        $model->field["LIMIT_DATE"] = ($model->field["LIMIT_DATE"] == "") ? CTRL_DATE : $model->field["LIMIT_DATE"];
        $arg["data"]["LIMIT_DATE"] = View::popUpCalendar($objForm, "LIMIT_DATE", str_replace("-","/",$model->field["LIMIT_DATE"]),"");

        //販売日付
        $model->field["SALES_DATE"] = ($model->field["SALES_DATE"] == "") ? CTRL_DATE : $model->field["SALES_DATE"];
        $arg["data"]["SALES_DATE"] = View::popUpCalendar($objForm, "SALES_DATE", str_replace("-","/",$model->field["SALES_DATE"]),"");

        //販売時間
        if ($model->field["APPLICANTDIV"] == "2" && $model->field["TESTDIV"] != '6') {
            if ($model->cmd == "" || $model->cmd == "knjl330fApplicantdiv" || $model->cmd == "knjl330fTestdiv" && $model->field["OLD_TESTDIV"] == "6") {
                $model->field["SALES_HOUR"]  = "※入学コース別に販売時間を設定いたします。";
                $model->field["SALES_HOUR2"] = "【理数キャリア・スポーツ科学】 １３：０５～１４：００";
                $model->field["SALES_HOUR3"] = "【国際教養】 １４：０５～１５：００";
            }
            $extra = "";
            $arg["data"]["SALES_HOUR"] = knjCreateTextBox($objForm, $model->field["SALES_HOUR"], "SALES_HOUR", 60, 60, $extra);

            $extra = "";
            $arg["data"]["SALES_HOUR2"] = knjCreateTextBox($objForm, $model->field["SALES_HOUR2"], "SALES_HOUR2", 60, 60, $extra);

            $extra = "";
            $arg["data"]["SALES_HOUR3"] = knjCreateTextBox($objForm, $model->field["SALES_HOUR3"], "SALES_HOUR3", 60, 60, $extra);
        } else {
            if ($model->cmd == "" || $model->cmd == "knjl330fApplicantdiv" || $model->cmd == "knjl330fTestdiv" && $model->field["OLD_TESTDIV"] != "6") {
                $model->field["SALES_HOUR"] = "10:00～12:00　13:00～15:00";
            }
            $extra = "";
            $arg["data"]["SALES_HOUR"] = knjCreateTextBox($objForm, $model->field["SALES_HOUR"], "SALES_HOUR", 26, 26, $extra);
        } 

        //販売場所
        $extra = "";
        $model->field["SALES_LOCATION"] = ($model->cmd == "") ? "進学棟校舎" : $model->field["SALES_LOCATION"];
        $arg["data"]["SALES_LOCATION"] = knjCreateTextBox($objForm, $model->field["SALES_LOCATION"], "SALES_LOCATION", 20, 20, $extra);

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
        knjCreateHidden($objForm, "OLD_TESTDIV", $model->field["TESTDIV"]);
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJL330F");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl330fForm1.html", $arg); 
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
