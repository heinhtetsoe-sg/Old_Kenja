<?php

require_once('for_php7.php');

class knjl333aForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl333aForm1", "POST", "knjl333aindex.php", "", "knjl333aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //受験校種コンボ
        $extra = " onchange=\"return btn_submit('knjl333a');\"";
        $query = knjl333aQuery::getNameMst($model->ObjYear, "L003");
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1);

        //届・受領書種別コンボ
        $model->field["DOC_TYPE"] = $model->field["DOC_TYPE"] == "" ? "1" : $model->field["DOC_TYPE"];
        $extra = " onchange=\"return btn_submit('knjl333a');\"";
        $opt[] = array("label" => "1:交通機関利用届", "value" => "1" );
        if ($model->field["APPLICANTDIV"] == "2") {
            $opt[] = array("label" => "2:親権者登録", "value" => "2" );
        }
        $opt[] = array("label" => "3:個人報告書", "value" => "3" );
        $opt[] = array("label" => "4:指導要録等受領書", "value" => "4" );
        if ($model->field["APPLICANTDIV"] == "1") {
            $opt[] = array("label" => "5:【中学】入学に伴う書類送付願い", "value" => "5" );
        }
        $arg["data"]["DOC_TYPE"] = knjCreateCombo($objForm, "DOC_TYPE", $model->field["DOC_TYPE"], $opt, $extra, 1);

        //表示切替
        if (in_array($model->field["DOC_TYPE"], array("1", "2"))) {
            $arg["isReceptno"] = 1;
        } else {
            $arg["isFslist"] = 1;
            if ($model->field["DOC_TYPE"] == "5") {
                $arg["layoutInputDate"] = 1;
            } else {
                $arg["layoutInputYear"] = 1;
            }
        }

        //試験回コンボ
        $extra = " onchange=\"return btn_submit('knjl333a');\"";
        $query = knjl333aQuery::getTestdivMst($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1, "ALL");

        //受験番号範囲
        $extra = "";
        $arg["TOP"]["RECEPTNO_FROM"] = knjCreateTextBox($objForm, $model->field["RECEPTNO_FROM"], "RECEPTNO_FROM", 7, 7, $extra);
        $arg["TOP"]["RECEPTNO_TO"]   = knjCreateTextBox($objForm, $model->field["RECEPTNO_TO"], "RECEPTNO_TO", 7, 7, $extra);

        $gengouCd = "";
        $printDate = str_replace("-", "/", CTRL_DATE);
        //名称マスタより和暦の元号を取得
        $result = $db->query(knjl333aQuery::getCalendarno($model->ObjYear));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($row["NAMESPARE2"] <= $printDate && $printDate <= $row["NAMESPARE3"]) {
                //出力年度の初期値
                $gengouCd = $row["NAMECD2"];
                $defPrintEraY = $model->ObjYear - $row['NAMESPARE1'] + 1;
                $defPrintEraY = sprintf("%02d", $defPrintEraY);
            }
        }

        //出力年度元号コンボ
        $query = knjl333aQuery::getNameMst($model->ObjYear, "L007");
        $extra = "";
        $model->field["PRINT_ERACD"] = ($model->field["PRINT_ERACD"]) ? $model->field["PRINT_ERACD"]: $gengouCd;
        makeCmb($objForm, $arg, $db, $query, $model->field["PRINT_ERACD"], "PRINT_ERACD", $extra, 1);
        //出力年度年
        $model->field["PRINT_ERAY"] = ($model->field["PRINT_ERAY"]) ? $model->field["PRINT_ERAY"]: $defPrintEraY;
        $extra = "STYLE=\"ime-mode: inactive\" style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["PRINT_ERAY"] = knjCreateTextBox($objForm, $model->field["PRINT_ERAY"], "PRINT_ERAY", 2, 2, $extra);
        //出力月
        $extra = "STYLE=\"ime-mode: inactive\" style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["PRINT_EMONTH"] = knjCreateTextBox($objForm, $model->field["PRINT_EMONTH"], "PRINT_EMONTH", 2, 2, $extra);
        //出力日
        $extra = "STYLE=\"ime-mode: inactive\" style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["PRINT_EDAY"] = knjCreateTextBox($objForm, $model->field["PRINT_EDAY"], "PRINT_EDAY", 2, 2, $extra);

        //クラス一覧リストToリスト
        makeListToList($objForm, $arg, $db, $model);

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
        knjCreateHidden($objForm, "PRGID", "KNJL333A");
        knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl333aForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "ALL") {
        $opt[] = array("label" => "-- 全て --", "value" => "ALL");
    }
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $value_flg = false;
    $default = 0;
    $i = ($blank) ? 1 : 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }

        if ($row["NAMESPARE2"] && $default_flg && $value != "ALL") {
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

//クラス一覧リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model)
{
    //クラス一覧
    $row1 = array();
    $result = $db->query(knjl333aQuery::getFinschool($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $row1[]= array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
    }
    $result->free();

    //クラス一覧作成
    $extra = "multiple style=\"width:300px\" width=\"300px\" ondblclick=\"move1('left')\"";
    $arg["data"]["GROUP_NAME"] = knjCreateCombo($objForm, "GROUP_NAME", "", $row1, $extra, 15);

    //出力対象作成
    $extra = "multiple style=\"width:300px\" width=\"300px\" ondblclick=\"move1('right')\"";
    $arg["data"]["GROUP_SELECTED"] = knjCreateCombo($objForm, "GROUP_SELECTED", "", array(), $extra, 15);

    // << ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    // ＜ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
    // ＞ ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    // >> ボタン作成
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
}
