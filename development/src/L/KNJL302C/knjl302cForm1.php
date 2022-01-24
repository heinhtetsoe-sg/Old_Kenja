<?php

require_once('for_php7.php');


class knjl302cForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjl302cForm1", "POST", "knjl302cindex.php", "", "knjl302cForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //extra
        $extra = " onChange=\"return btn_submit('knjl302c');\"";

        //入試制度コンボの設定
        $query = knjl302cQuery::getApctDiv("L003", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1);

        //入試区分コンボの設定
        $query = knjl302cQuery::getTestDiv("L004", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1);

        //五條中学は、「専願」コンボをグレーアウトにする
        $disShdiv = ($model->isGojou && $model->field["APPLICANTDIV"] == "1" || $model->isCollege && $model->field["APPLICANTDIV"] == "2") ? " disabled" : "";

        //専併区分コンボの設定
        $div = (($model->field["APPLICANTDIV"] == "2" && $model->field["TESTDIV"] == "3") || $model->isGojou) ? "" : "1";
        $query = knjl302cQuery::getSHDiv("L006", $model->ObjYear, $div);
        makeCmb($objForm, $arg, $db, $query, "SHDIV", $model->field["SHDIV"], $disShdiv, 1);

        //合格者は除くチェックボックス
        if ($model->field["TESTDIV"] == 2) {
            $extra = ($model->field["OUTPUT"] == "1") ? "checked" : "";
        } else {
            $extra .= " disabled='disabled'";
        }
        $arg["data"]["OUTPUT"] = knjCreateCheckBox($objForm, "OUTPUT", "1", $extra, "");

        //カレッジは”一般入試”、それ以外は”前期”
        if ($model->isCollege) {
            $arg["data"]["OUTPUT_NAME"] = "一般入試合格者は除く";
        } else if ($model->isGojou) {
            $arg["data"]["OUTPUT_NAME"] = "前期合格者は除く";
        } else {
            $arg["data"]["OUTPUT_NAME"] = "前期Ｓ選抜合格者は除く";
        }

        //保護者出力チェックボックス
        $extra = ($model->field["PRINT_TYPE"] == "1" || $model->cmd == "") ? "checked" : "";
        $arg["data"]["PRINT_TYPE"] = knjCreateCheckBox($objForm, "PRINT_TYPE", "1", $extra, "");

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl302cForm1.html", $arg); 
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
        if ($name == "TESTDIV" && $value == "9") $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg){
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }

    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    if($name == "TESTDIV"){
        $opt[]= array("label" => "-- 全て --", "value" => "9");
    }

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
    knjCreateHidden($objForm, "PRGID", "KNJL302C");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
}
?>
