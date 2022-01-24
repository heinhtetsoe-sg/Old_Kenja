<?php

require_once('for_php7.php');

class knjl339qForm1 {
    function main(&$model) {

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl339qForm1", "POST", "knjl339qindex.php", "", "knjl339qForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = " onchange=\"return btn_submit('knjl339q');\"";
        $query = knjl339qQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1);

        //入試区分コンボボックス
        $extra = " onchange=\"return btn_submit('knjl339q');\"";
        if (SCHOOLKIND == "J") {
            $query = knjl339qQuery::getNameMst($model->ObjYear, "L024");
        } else {
            $query = knjl339qQuery::getNameMstL004($model->ObjYear);
        }
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1);

        if ($model->field["TESTDIV"] == "2") {
            $arg["suisen"] = 1;
            //校長推薦
            $extra = "id=\"KOCHO\"";
            $arg["data"]["KOCHO"] = knjCreateCheckBox($objForm, "KOCHO", "1", $extra);
            //自己推薦
            $extra = "id=\"JIKO\"";
            $arg["data"]["JIKO"] = knjCreateCheckBox($objForm, "JIKO", "1", $extra);
        } elseif ($model->field["TESTDIV"] == "3") {
            $arg["ippan"] = 1;
            //一般
            $extra = "id=\"IPPAN\"";
            $arg["data"]["IPPAN"] = knjCreateCheckBox($objForm, "IPPAN", "1", $extra);
            //基準（普通クラス）
            $extra = "id=\"NORMAL\"";
            $arg["data"]["NORMAL"] = knjCreateCheckBox($objForm, "NORMAL", "1", $extra);
            //基準（スポーツクラス）
            $extra = "id=\"SPORT\"";
            $arg["data"]["SPORT"] = knjCreateCheckBox($objForm, "SPORT", "1", $extra);
            //基準（駿中生）
            $extra = "id=\"SUNCHU\"";
            $arg["data"]["SUNCHU"] = knjCreateCheckBox($objForm, "SUNCHU", "1", $extra);
            //基準・一般（スポーツ）
            $extra = "id=\"SGSPORT\"";
            $arg["data"]["SGSPORT"] = knjCreateCheckBox($objForm, "SGSPORT", "1", $extra);
            //基準・一般（普通）
            $extra = "id=\"SGNORMAL\"";
            $arg["data"]["SGNORMAL"] = knjCreateCheckBox($objForm, "SGNORMAL", "1", $extra);
        }

        //radio（1.高得点順、2.受験番号順）
        $opt = array(1, 2);
        $model->field["ORDER"] = ($model->field["ORDER"] == "") ? "1" : $model->field["ORDER"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"ORDER{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "ORDER", $model->field["ORDER"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

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
        knjCreateHidden($objForm, "PRGID", "KNJL339Q");
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl339qForm1.html", $arg); 
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
