<?php

require_once('for_php7.php');


class knjl342cForm1
{
    function main(&$model){

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjl342cForm1", "POST", "knjl342cindex.php", "", "knjl342cForm1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->ObjYear;

        //入試制度コンボの設定
        $extra = "onChange=\"btn_submit('knjl342c')\"";
        $query = knjl342cQuery::getApctDiv("L003", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->field["APPLICANTDIV"], $extra, 1, $model);

        //入試区分コンボの設定
        $query = knjl342cQuery::getTestDiv($model, "L004", $model->ObjYear);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->field["TESTDIV"], "", 1, $model);

        //追加合格者のみ出力チェックボックス
        $extra = ($model->field["OUTPUT"] == "1" || $model->cmd == "") ? "" : "checked";
        $arg["data"]["OUTPUT"] = knjCreateCheckBox($objForm, "OUTPUT", "1", $extra, "");

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl342cForm1.html", $arg); 
	}
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $model)
{
    $opt = array();
    $value_flg = false;
    $default = 0;
    $i = 0;
    $default_flg = true;
    $setName = $model->isCollege ? "一般／表現力" : ($model->isGojou ? "前／後期／自己推薦" : "前／後期");
    if ($model->field["APPLICANTDIV"] == "1" && $name == "TESTDIV") {
        $opt[] = array('label' => $setName,
                       'value' => "0");
    }

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

    if($name == "TESTDIV"){
        $opt[]= array("label" => "-- 全て --", "value" => "9");
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
    knjCreateHidden($objForm, "PRGID", "KNJL342C");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "YEAR", $model->ObjYear);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
}
?>
