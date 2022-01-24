<?php

require_once('for_php7.php');


class knjl327fForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjl327fForm1", "POST", "knjl327findex.php", "", "knjl327fForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //extra
        $extra = " onChange=\"return btn_submit('knjl327f');\"";

        //入試制度コンボの設定
        $query = knjl327fQuery::getApctDiv("L003", $model->ObjYear, $model);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1);

        //入試区分コンボボックス
        $query = knjl327fQuery::getNameMst($model->ObjYear, "L004");
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1);

        $query = knjl327fQuery::getTestdiv0($model->ObjYear, $model->field["TESTDIV"]);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "TESTDIV0", $model->field["TESTDIV0"], $extra, 1);

        //通知日付作成
        $model->field["PRINT_DATE"] = $model->field["PRINT_DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["PRINT_DATE"];
        $arg["data"]["PRINT_DATE"] = View::popUpCalendar($objForm, "PRINT_DATE", $model->field["PRINT_DATE"]);

        $click = ""; // " onClick=\"return btn_submit('knjl327f');\"";

        //帳票種類ラジオボタン 1:合格通知書 2:不合格通知書 3:補欠合格通知書 4:特待生合格通知書 5:特待生背印聖書 6.入学金振込
        $opt_output = array(1, 2, 3, 4, 5, 6);
        $model->field["OUTPUT"] = ($model->field["OUTPUT"] == "") ? "1" : $model->field["OUTPUT"];
        $extra = array("id=\"OUTPUT1\"".$click, "id=\"OUTPUT2\"".$click, "id=\"OUTPUT3\"".$click, "id=\"OUTPUT4\"".$click, "id=\"OUTPUT5\"".$clic, "id=\"OUTPUT6\"".$clic);
        $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_output, get_count($opt_output));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
        $optB = array("A", "B", "C", "D", "E", "F");
        for ($i = 1; $i <= 8; $i++) {
            //出力範囲ラジオボタン 1:合格者全員 2:受験者指定 3:受験者全員 4:候補者
            $opt_outputA = array(1, 2, 3, 4);
            $optOutName = "OUTPUT".$optB[$i - 1];		
            $extra = array("id=\"".$optOutName."1\"".$click, "id=\"".$optOutName."2\"".$click, "id=\"".$optOutName."3\"".$click, "id=\"".$optOutName."4\"".$click);
            $extra_outA = "";
            if ($model->field[$optOutName] == "") {
                if ($i == 1 || $i == 2) {
                    $model->field[$optOutName] = "2";
                } else {
                    $model->field[$optOutName] = "1";
                }
            }
            $radioArray = knjCreateRadio($objForm, $optOutName, $model->field[$optOutName], $extra, $opt_outputA, get_count($opt_outputA));
            foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

            //受験番号テキストボックス
            $setExName = "EXAMNO".$optB[$i - 1];
            $value = ($model->field[$setExName]) ? $model->field[$setExName] : "";
            $extra_examA = " id=\"".$setExName."\" STYLE=\"text-align: right\"; onBlur=\"this.value=toInteger(this.value);\"";
            $arg["data"][$setExName] = knjCreateTextBox($objForm, $value, $setExName, 8, 8, $extra_examA);
        }

        //来校日付作成
        $model->field["TEISHUTSU_DATE"] = $model->field["TEISHUTSU_DATE"] == "" ? str_replace("-", "/", CTRL_DATE) : $model->field["TEISHUTSU_DATE"];
        $arg["data"]["TEISHUTSU_DATE"] = View::popUpCalendar($objForm, "TEISHUTSU_DATE", $model->field["TEISHUTSU_DATE"]);

        //合格通知連絡先テキストボックス
        $value = ($model->field["GOUKAKUTUUCHI_TELNO"]) ? $model->field["GOUKAKUTUUCHI_TELNO"] : "";
        $extra = " id=\"GOUKAKUTUUCHI_TELNO\" ";
        $arg["data"]["GOUKAKUTUUCHI_TELNO"] = knjCreateTextBox($objForm, $value, "GOUKAKUTUUCHI_TELNO", 12, 12, $extra);

        //合格通知入試担当テキストボックス
        $value = ($model->field["GOUKAKUTUUCHI_NYUSHITANTOU"]) ? $model->field["GOUKAKUTUUCHI_NYUSHITANTOU"] : "";
        $extra = " id=\"GOUKAKUTUUCHI_NYUSHITANTOU\" ";
        $arg["data"]["GOUKAKUTUUCHI_NYUSHITANTOU"] = knjCreateTextBox($objForm, $value, "GOUKAKUTUUCHI_NYUSHITANTOU", 20, 20, $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl327fForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg){
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }

    $result->free();
    $value = (($value && $value_flg) || $value == "9") ? $value : $opt[$default]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "PRGID", "KNJL327F");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);

}
?>
