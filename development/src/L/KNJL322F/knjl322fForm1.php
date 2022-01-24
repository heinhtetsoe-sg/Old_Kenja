<?php

require_once('for_php7.php');

class knjl322fForm1
{
    function main(&$model){

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl322fForm1", "POST", "knjl322findex.php", "", "knjl322fForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度
        $model->field["APPLICANTDIV"] = "2";
        $arg["data"]["APPLICANTDIV"] = $db->getOne(knjl322fQuery::getNameMst($model->ObjYear, "L003", $model->field["APPLICANTDIV"]));

        //入試区分コンボボックス
        $extra = "onchange=\"return btn_submit('knjl322f');\"";
        $query = knjl322fQuery::getNameMst($model->ObjYear, "L004");
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], $extra, 1);

        if ($model->field["TESTDIV"] != "9") {
            $arg["useTestcount"] = "1";
            //入試回数コンボボックス
            $query = knjl322fQuery::getTestCount($model->ObjYear, $model->field["TESTDIV"]);
            makeCmb($objForm, $arg, $db, $query, "TESTCOUNT", $model->field["TESTCOUNT"], "", 1);
        }

        //コース区分コンボボックス
        $query = knjl322fQuery::getCourseDiv($model);
        makeCmb($objForm, $arg, $db, $query, "COURSEDIV", $model->field["COURSEDIV"], "", 1);

        //出力順
        $opt = array(1, 2);
        $model->field["SORT"] = ($model->field["SORT"] == "") ? "1" : $model->field["SORT"];
        $extra = array("id=\"SORT1\"", "id=\"SORT2\"");
        $radioArray = knjCreateRadio($objForm, "SORT", $model->field["SORT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //アドバンス変更希望者に網掛け
        $extra = $model->field["ADVANCE_AMIKAKE"] == "1" ? "checked" : "";
        $extra .= " id=\"ADVANCE_AMIKAKE\"";
        $arg["data"]["ADVANCE_AMIKAKE"] = knjCreateCheckBox($objForm, "ADVANCE_AMIKAKE", "1", $extra, "");

        //アドバンス変更希望者に網掛け ○点以上
        $extra = " onBlur=\"this.value=toInteger(this.value);\" STYLE=\"text-align:right;\"";
        $arg["data"]["ADVANCE_SCORE"] = knjCreateTextBox($objForm, $model->field["ADVANCE_SCORE"], "ADVANCE_SCORE", 4, 4, $extra);

        //学力診断テストを表示しない
        $extra = $model->field["NOT_SHOW_FORCE"] == "1" ? "checked" : "";
        $extra .= " id=\"NOT_SHOW_FORCE\"";
        $arg["data"]["NOT_SHOW_FORCE"] = knjCreateCheckBox($objForm, "NOT_SHOW_FORCE", "1", $extra, "");

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl322fForm1.html", $arg); 
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
    $value = ($value && $value_flg) ? $value : $opt[$default]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'pdf');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //CSVボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
     //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "PRGID", "KNJL322F");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);

    knjCreateHidden($objForm, "APPLICANTDIV", $model->field["APPLICANTDIV"]);
}
?>
