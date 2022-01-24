<?php
class knjl300yForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjl300yForm1", "POST", "knjl300yindex.php", "", "knjl300yForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('knjl300y');\"";
        $query = knjl300yQuery::getNameMst("L003", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1);

        //入試区分コンボボックス
        $model->field["TESTDIV"] = ($model->field["APPLICANTDIV"] == $model->field["APP_HOLD"]) ? $model->field["TESTDIV"] : "";
        $namecd = ($model->field["APPLICANTDIV"] == "1") ? "L024" : "L004";
        $query = knjl300yQuery::getNameMst($namecd, $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1);

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        //受付開始番号（開始）
        $value = ($model->field["RECEPTNO_FROM"]) ? $model->field["RECEPTNO_FROM"] : "";
        $extra = " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["RECEPTNO_FROM"] = knjCreateTextBox($objForm, $value, "RECEPTNO_FROM", 5, 5, $extra);

        //受付開始番号（終了）
        $value = ($model->field["RECEPTNO_TO"]) ? $model->field["RECEPTNO_TO"] : "";
        $extra = " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["RECEPTNO_TO"] = knjCreateTextBox($objForm, $value, "RECEPTNO_TO", 5, 5, $extra);

        //出力ラベル選択ラジオボタン 1:会場 2:礼拝堂
        $opt_label = array(1, 2);
        $model->field["LABEL"] = ($model->field["LABEL"] == "") ? "1" : $model->field["LABEL"];
        $extra = array("id=\"LABEL1\" onClick=\"return btn_submit('knjl300y')\"", "id=\"LABEL2\" onClick=\"return btn_submit('knjl300y')\"");
        $radioArray = knjCreateRadio($objForm, "LABEL", $model->field["LABEL"], $extra, $opt_label, count($opt_label));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //開始位置（行）コンボの設定
        $col_no = ($model->field["LABEL"] == "1") ? "7" : "4";
        for( $i = 0; $i < $col_no; $i ++ ){
            $line[$i] = array('label' => mb_convert_kana($i+1,"N")."行", 'value' => $i+1);
        }
        $model->field["LINE"] = ($model->field["LINE"]) ? $model->field["LINE"] : $line[0]["value"];
        $arg["data"]["LINE"] = knjCreateCombo($objForm, "LINE", $model->field["LINE"], $line, "", 1);

        //開始位置（列）コンボの設定
        $row_no = ($model->field["LABEL"] == "1") ? "3" : "2";
        for( $i = 0; $i < $row_no; $i ++ ){
            $row[$i] = array('label' => mb_convert_kana($i+1,"N")."列", 'value' => $i+1);
        }
        $model->field["ROW"] = ($model->field["ROW"]) ? $model->field["ROW"] : $row[0]["value"];
        $arg["data"]["ROW"] = knjCreateCombo($objForm, "ROW", $model->field["ROW"], $row, "", 1);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl300yForm1.html", $arg); 
    }
}

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    $query = knjl300yQuery::getExamHall($model);
    $result = $db->query($query);
    $opt = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $s_examno = $db->getOne(knjl300yQuery::getExamNo($model, $row["S_RECEPTNO"]));
        $e_examno = $db->getOne(knjl300yQuery::getExamNo($model, $row["E_RECEPTNO"]));
        $row["LABEL"] = ($s_examno && $e_examno) ? $row["LABEL"].'［'.$s_examno.'～'.$e_examno.'］' : $row["LABEL"];

        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
    }
    $result->free();

    //会場一覧を作成する
    $extra = "multiple style=\"width=300px\" width=\"300px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt, $extra, 20);

    //出力対象一覧を作成する
    $extra = "multiple style=\"width=300px\" width=\"300px\" ondblclick=\"move1('right')\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", array(), $extra, 20);

    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
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
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "PRGID", "KNJL300Y");
    knjCreateHidden($objForm, "cmd");

    knjCreateHidden($objForm, "APP_HOLD", $model->field["APPLICANTDIV"]);
}
?>
