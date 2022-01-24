<?php

require_once('for_php7.php');

class knjl315yForm1
{
    function main(&$model){

        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjl315yForm1", "POST", "knjl315yindex.php", "", "knjl315yForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試制度
        $model->field["APPLICANTDIV"] = "2";
        $arg["data"]["APPLICANTDIV"] = $db->getOne(knjl315yQuery::getNameMst($model->ObjYear, "L003", $model->field["APPLICANTDIV"]));

        //入試区分コンボボックス
        $model->field["TESTDIV"] = "";
        $query = knjl315yQuery::getNameMst($model->ObjYear, "L004");
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], "", 1);

        //対象者ラジオボタン 1:外部生のみ 2:内部生のみ 3:全て
        $opt = array(1, 2, 3);
        $model->field["INOUT"] = ($model->field["INOUT"]) ? $model->field["INOUT"] : "1";
        $extra = array("id=\"INOUT1\"", "id=\"INOUT2\"", "id=\"INOUT3\"");
        $radioArray = knjCreateRadio($objForm, "INOUT", $model->field["INOUT"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //対象者(帰国生)ラジオボタン 1:帰国生除く 2:帰国生のみ
        $opt = array(1, 2);
        $model->field["KIKOKU"] = ($model->field["APPLICANTDIV"] != "1" && $model->field["INOUT"] != "2" && $model->field["KIKOKU"]) ? $model->field["KIKOKU"] : "1";
        $disKikoku = ($model->field["APPLICANTDIV"] != "1" && $model->field["INOUT"] != "2") ? "" : "disabled";
        $extra = array("id=\"KIKOKU1\" {$disKikoku}", "id=\"KIKOKU2\" {$disKikoku}");
        $radioArray = knjCreateRadio($objForm, "KIKOKU", $model->field["KIKOKU"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //最高点
        $value = ($model->field["MAX_SCORE"]) ? $model->field["MAX_SCORE"] : "440";
        $extra = " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["MAX_SCORE"] = knjCreateTextBox($objForm, $value, "MAX_SCORE", 5, 3, $extra);

        //最低点
        $value = ($model->field["MIN_SCORE"]) ? $model->field["MIN_SCORE"] : "100";
        $extra = " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["MIN_SCORE"] = knjCreateTextBox($objForm, $value, "MIN_SCORE", 5, 3, $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl315yForm1.html", $arg); 
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
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    //終了ボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "PRGID", "KNJL315Y");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");

    knjCreateHidden($objForm, "APPLICANTDIV", $model->field["APPLICANTDIV"]);
}
?>
