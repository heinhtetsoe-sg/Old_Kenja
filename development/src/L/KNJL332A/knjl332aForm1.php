<?php

require_once('for_php7.php');

class knjl332aForm1
{
    public function main(&$model)
    {
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl332aForm1", "POST", "knjl332aindex.php", "", "knjl332aForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        if ($model->field["APPLICANTDIV"] == "") {
            $query = knjl332aQuery::getApplicantDiv($model);
            $model->field["APPLICANTDIV"] = $db->getOne($query);
        }

        //受験校種コンボ
        $extra = " onchange=\"return btn_submit('knjl332a');\"";
        $query = knjl332aQuery::getNameMst($model->ObjYear, "L003", true);
        makeCmb($objForm, $arg, $db, $query, $model->field["APPLICANTDIV"], "APPLICANTDIV", $extra, 1);

        //印刷種別コンボ
        $model->field["PRINT_TYPE"] = $model->field["PRINT_TYPE"] == "" ? "1" : $model->field["PRINT_TYPE"];
        $extra = " onchange=\"return btn_submit('knjl332a');\"";
        $opt[] = array("label" => "1:受験者（住所あり）", "value" => "1" );
        $opt[] = array("label" => "2:受験者（住所なし）", "value" => "2" );
        $opt[] = array("label" => "3:出身学校宛", "value" => "3" );
        $opt[] = array("label" => "4:保護者宛", "value" => "4" );
        $arg["data"]["PRINT_TYPE"] = knjCreateCombo($objForm, "PRINT_TYPE", $model->field["PRINT_TYPE"], $opt, $extra, 1);

        //用紙サイズ名称
        $arg["data"]["SIZE_NAME"] = $model->field["PRINT_TYPE"] == "1" || $model->field["PRINT_TYPE"] == "2" ? "角２" : "";

        //用紙サイズラジオボタン 1:角２ 2:長３ 3:タックシール
        $opt = array();
        $extra = array();
        if (($model->field["PRINT_TYPE"] == "1") or ($model->field["PRINT_TYPE"] == "2")) {
            $opt = array(1, 2, 3);
            $extra = array("id=\"SIZE1\"", "id=\"SIZE2\"", "id=\"SIZE3\"");
            if (!$model->field["SIZE"]) {
                $model->field["SIZE"] = 1;
            }
        } else {
            $opt = array(1, 2, 3);
            $extra = array("disabled style=\"visibility: hidden;\"", "id=\"SIZE2\"", "id=\"SIZE3\"");
            if (!$model->field["SIZE"] or ($model->field["SIZE"] == "1")) {
                $model->field["SIZE"] = 2;
            }
        }
        $radioArray = knjCreateRadio($objForm, "SIZE", $model->field["SIZE"], $extra, $opt, count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //表示切替
        $schoolKind = $model->field["APPLICANTDIV"] == "2" ? "H" : "J";

        //試験回コンボ
        $extra = "";
        $query = knjl332aQuery::getTestdivMst($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTDIV"], "TESTDIV", $extra, 1, "ALL");

        //出願コースコンボ
        $extra = "";
        $namecd1 = 'L'.$schoolKind.'58';
        $query = knjl332aQuery::getNameMst($model->ObjYear, $namecd1, false);
        makeCmb($objForm, $arg, $db, $query, $model->field["DESIREDIV"], "DESIREDIV", $extra, 1, "ALL");

        //合否コンボ
        $opt = array();
        $opt[] = array("label" => "-- 全て --", "value" => "ALL");
        $opt[] = array("label" => "合格",      "value" => "1");
        $opt[] = array("label" => "不合格",    "value" => "2");
        $arg["data"]["PASS_COURSE"] = knjCreateCombo($objForm, "PASS_COURSE", $model->field["PASS_COURSE"], $opt, "", 1);

        //入学コースコンボ
        $extra = "";
        $namecd1 = 'L'.$schoolKind.'58';
        $query = knjl332aQuery::getNameMst($model->ObjYear, $namecd1, false);
        makeCmb($objForm, $arg, $db, $query, $model->field["ENT_COURSE"], "ENT_COURSE", $extra, 1, "ALL");

        //専併区分コンボ
        $extra = "";
        $query = knjl332aQuery::getNameMst($model->ObjYear, "L006", true);
        makeCmb($objForm, $arg, $db, $query, $model->field["SHDIV"], "SHDIV", $extra, 1, "ALL");

        //受験番号範囲
        $extra = "";
        $arg["TOP"]["RECEPTNO_FROM"] = knjCreateTextBox($objForm, $model->field["RECEPTNO_FROM"], "RECEPTNO_FROM", 7, 7, $extra);
        $arg["TOP"]["RECEPTNO_TO"]   = knjCreateTextBox($objForm, $model->field["RECEPTNO_TO"], "RECEPTNO_TO", 7, 7, $extra);

        //CSVボタン
        $extra = "onclick=\"btn_submit('csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
        
        /********************/
        /* チェックボックス */
        /********************/
        //在中印字なし
        if ($model->field["PRINT_TYPE"] == "3") {
            $row = array();
            $extra = "checked";
            $extra .= " id=\"BLANK\"";
            $row["BLANK"] = knjCreateCheckBox($objForm, "BLANK", "on", $extra, "");
            $row["BLANK_NAME"] = "在中あり";
            $opt = array(1, 2);
            $extra = array("id=\"COMMENT1\"", "id=\"COMMENT2\"");
            $value = $model->field["COMMENT"] ? $model->field["COMMENT"] : "1";
            $radioArray = knjCreateRadio($objForm, "COMMENT", $value, $extra, $opt, count($opt));
            foreach ($radioArray as $key => $val) {
                $row[$key] = $val;
            }
            $row["COMMENT_LABEL1"] = "入試関係書類在中";
            $row["COMMENT_LABEL2"] = "個人報告書受領書在中";
            $arg["blanktags"][] = $row;
        }

        //hidden作成
        knjCreateHidden($objForm, "ENTEXAMYEAR", $model->ObjYear);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "LOGIN_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "LOGIN_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJL332A");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl332aForm1.html", $arg);
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
