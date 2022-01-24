<?php

require_once('for_php7.php');


class knjl327jForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjl327jForm1", "POST", "knjl327jindex.php", "", "knjl327jForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試区分コンボ作成
        $query = knjl327jQuery::get_test_div("L004", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], "", 1);

        //通知年コンボボックス
        $opt_year[] = array('label' => CTRL_YEAR, 'value' => CTRL_YEAR);
        $opt_year[] = array('label' => CTRL_YEAR+1, 'value' => CTRL_YEAR+1);
        $value = ($model->field["DATE_YEAR"]) ? $model->field["DATE_YEAR"] : substr(CTRL_DATE,0,4);
        $arg["data"]["DATE_YEAR"] = knjCreateCombo($objForm, "DATE_YEAR", $value, $opt_year, "", 1);

        //通知月テキストボックス
        $value = ($model->field["DATE_MONTH"]) ? sprintf("%02d",$model->field["DATE_MONTH"]) : substr(CTRL_DATE,5,2);
        $extra = " onblur=\"date_check(this),btn_submit('knjl327j')\"";
        $arg["data"]["DATE_MONTH"] = knjCreateTextBox($objForm, $value, "DATE_MONTH", 2, 2, $extra);

        //帳票種類ラジオボタン 1:合格通知書 2:合格通知書台帳 3:入学許可証 4:入学許可証台帳 5:特待合格通知書
        $opt_type = array(1, 2, 3, 4, 5);
        $model->field["PRINT_TYPE"] = ($model->field["PRINT_TYPE"]) ? $model->field["PRINT_TYPE"] : "1";
        $radioArray = knjCreateRadio($objForm, "PRINT_TYPE", $model->field["PRINT_TYPE"], "", $opt_type, get_count($opt_type));
        foreach ($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出力範囲ラジオボタン 1:全員 2:受験者指定
        $opt_range = array(1, 2);
        $model->field["PRINT_RANGE"] = ($model->field["PRINT_RANGE"]) ? $model->field["PRINT_RANGE"] : "1";
        $extra = " onclick=\"return btn_submit('knjl327j');\"";
        $radioArray = knjCreateRadio($objForm, "PRINT_RANGE", $model->field["PRINT_RANGE"], $extra, $opt_range, get_count($opt_range));
        foreach ($radioArray as $key => $val) $arg["data"][$key] = $val;

        //disabled
        $disabled = ($model->field["PRINT_RANGE"] == 2) ? "" : "disabled STYLE=\"background-color:darkgray\"";

        //extra
        $extra = " onblur=\"this.value=toInteger(this.value)\"";

        //受験番号開始テキストボックス
        $value = ($model->field["EXAMNO_FROM"]) ? $model->field["EXAMNO_FROM"] : "";
        $arg["data"]["EXAMNO_FROM"] = knjCreateTextBox($objForm, $value, "EXAMNO_FROM", 5, 5, $disabled.$extra);

        //受験番号終了テキストボックス
        $value = ($model->field["EXAMNO_TO"]) ? $model->field["EXAMNO_TO"] : "";
        $arg["data"]["EXAMNO_TO"] = knjCreateTextBox($objForm, $value, "EXAMNO_TO", 5, 5, $disabled.$extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl327jForm1.html", $arg); 
	}
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
    $value_flg = false;
    $default = $i = 0 ;
    $default_flg = false;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] == 1 && !$default_flg){
            $default = $i;
            $default_flg = true;
        } else {
            $i++;
        }
    }
    $result->free();

    if ($name == "TESTDIV") {
        //入試区分データが存在する時
        if (get_count($db->getcol($query)) > 0) {
            $opt[] = array("label" => "-- 全て --", "value" => 0);
        } else {
            $opt[] = array("label" => "　　　", "value" => 0);
        }
        $value = ($value && $value_flg) ? $value : $opt[$default]["value"];
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷ボタンを作成する
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
    knjCreateHidden($objForm, "PRGID", "KNJL327J");
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
}

?>
