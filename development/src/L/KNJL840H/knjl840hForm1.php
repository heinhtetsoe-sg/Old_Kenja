<?php

require_once('for_php7.php');

class knjl840hForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //DB接続
        $db = Query::dbCheckOut();

        //年度表示
        $arg["data"]["YEAR"] = $model->examyear;

        //学校種別コンボ
        $query = knjl840hQuery::getNameMst($model->examyear, "L003");
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1, "");

        //入試区分コンボ
        $query = knjl840hQuery::getTestDiv($model->examyear, $model->field["APPLICANTDIV"]);
        $extra = "onChange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1, "");

        //帳票種類ラジオボタン（1:志願者一覧表、2:出欠席記入表、3:成績記入表、4:机上タックシール）
        $opt = array(1, 2, 3, 4);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"OUTPUT{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //合否区分ラジオボタン 1:全員 2:合格者除く
        $opt_shubetsu = array(1, 2);
        $model->field["JUDGMENT_DIV"] = ($model->field["JUDGMENT_DIV"]=="") ? "1" : $model->field["JUDGMENT_DIV"];
        $extra = array("id=\"JUDGMENT_DIV1\"", "id=\"JUDGMENT_DIV2\"");
        $radioArray = knjCreateRadio($objForm, "JUDGMENT_DIV", $model->field["JUDGMENT_DIV"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //性別ラジオボタン 1:全員 2:男 3:女
        $opt_shubetsu = array(1, 2, 3);
        $model->field["SEX"] = ($model->field["SEX"]=="") ? "1" : $model->field["SEX"];
        $extra = array("id=\"SEX1\"", "id=\"SEX2\"", "id=\"SEX3\"");
        $radioArray = knjCreateRadio($objForm, "SEX", $model->field["SEX"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //重複出願ラジオボタン 1:全員 2:１回出願 3:重複出願 4:重複出願同時 5:重複出願複数
        $opt_shubetsu = array(1, 2, 3, 4, 5);
        $model->field["DECISION"] = ($model->field["DECISION"]=="") ? "1" : $model->field["DECISION"];
        $extra = array("id=\"DECISION1\"", "id=\"DECISION2\"", "id=\"DECISION3\"", "id=\"DECISION4\"", "id=\"DECISION5\"");
        $radioArray = knjCreateRadio($objForm, "DECISION", $model->field["DECISION"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //集計区分ラジオボタン 1:合計点 2:科目別
        $opt_shubetsu = array(1, 2);
        $model->field["TOTAL_DIV"] = ($model->field["TOTAL_DIV"]=="") ? "1" : $model->field["TOTAL_DIV"];
        $disTotalDiv = ($model->field["APPLICANTDIV"]=="2" && $model->field["TESTDIV"]=="01") ? " disabled" : ""; // 高校推薦では科目別は選択しない
        $extra = array("id=\"TOTAL_DIV1\"", "id=\"TOTAL_DIV2\"" . $disTotalDiv);
        $radioArray = knjCreateRadio($objForm, "TOTAL_DIV", $model->field["TOTAL_DIV"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //出力点範囲
        $extra = "";
        if ($model->field["SCORE_S"] == "") {
            $model->field["SCORE_S"] = "0.0";
        }
        $arg["data"]["SCORE_S"] = knjCreateTextBox($objForm, $model->field["SCORE_S"], "SCORE_S", 5, 5, $extra);
        if ($model->field["SCORE_E"] == "") {
            $model->field["SCORE_E"] = "0.0";
        }
        $arg["data"]["SCORE_E"] = knjCreateTextBox($objForm, $model->field["SCORE_E"], "SCORE_E", 5, 5, $extra);
        //出力点範囲 きざみ点
        if ($model->field["SCORE_KIZAMI"] == "") {
            $model->field["SCORE_KIZAMI"] = "0.0";
        }
        $arg["data"]["SCORE_KIZAMI"] = knjCreateTextBox($objForm, $model->field["SCORE_KIZAMI"], "SCORE_KIZAMI", 5, 5, $extra);

        //虫眼鏡
        $extra = "";
        if ($model->field["MUSHIMEGANE_S"] == "") {
            $model->field["MUSHIMEGANE_S"] = "0.0";
        }
        $arg["data"]["MUSHIMEGANE_S"] = knjCreateTextBox($objForm, $model->field["MUSHIMEGANE_S"], "MUSHIMEGANE_S", 5, 5, $extra);
        if ($model->field["MUSHIMEGANE_E"] == "") {
            $model->field["MUSHIMEGANE_E"] = "0.0";
        }
        $arg["data"]["MUSHIMEGANE_E"] = knjCreateTextBox($objForm, $model->field["MUSHIMEGANE_E"], "MUSHIMEGANE_E", 5, 5, $extra);
        //虫眼鏡 きざみ点
        if ($model->field["MUSHIMEGANE_KIZAMI"] == "") {
            $model->field["MUSHIMEGANE_KIZAMI"] = "0.0";
        }
        $arg["data"]["MUSHIMEGANE_KIZAMI"] = knjCreateTextBox($objForm, $model->field["MUSHIMEGANE_KIZAMI"], "MUSHIMEGANE_KIZAMI", 5, 5, $extra);

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->examyear);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJL840H");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム作成
        $arg["start"]  = $objForm->get_start("main", "POST", "knjl840hindex.php", "", "main");
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl840hForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }

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
