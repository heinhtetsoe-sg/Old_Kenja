<?php

require_once('for_php7.php');

class knjh181Form1
{
    function main(&$model){

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        //オブジェクト作成
        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjh181Form1", "POST", "knjh181index.php", "", "knjh181Form1");

        //年度を作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        $query = knjh181Query::getSemeName();
        $setSeme = $db->getOne($query);
        $arg["data"]["SEMESTER"] = $setSeme;

        //出力対象ラジオボタン 1:電車通学 2:その他通学
        $opt = array(1, 2);
        $model->field["FLG"] = ($model->field["FLG"] == "") ? "1" : $model->field["FLG"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"FLG{$val}\" onClick=\"btn_submit('knjh181')\"");
        }
        $radioArray = knjCreateRadio($objForm, "FLG", $model->field["FLG"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //出力対象ラジオボタン 1:上り 2:下り
        $opt = array(1, 2);
        $model->field["UP_DOWN"] = ($model->field["UP_DOWN"] == "") ? "1" : $model->field["UP_DOWN"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"UP_DOWN{$val}\" onClick=\"btn_submit('knjh181')\" ");
        }
        $radioArray = knjCreateRadio($objForm, "UP_DOWN", $model->field["UP_DOWN"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //路線
        if ($model->field["FLG"] == "1") {
            $query = knjh181Query::getTrainLine($model);
        } else {
            $query = knjh181Query::getRosen($model);
        }
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $value, "ROSEN", $extra, 1, "BLANK");

        //出力順ラジオボタン 1:年組番号 2:通学詳細+年組番号
        $opt = array(1, 2);
        $model->field["SORTFLG"] = ($model->field["SORTFLG"] == "") ? "1" : $model->field["SORTFLG"];
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"SORTFLG{$val}\"");
        }
        $radioArray = knjCreateRadio($objForm, "SORTFLG", $model->field["SORTFLG"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //button
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "', 'csv');\"";
        $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJH181");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "useSchool_KindField", $model->Properties["useSchool_KindField"]);
        knjCreateHidden($objForm, "SCHOOLCD", SCHOOLCD);
        knjCreateHidden($objForm, "SCHOOLKIND", SCHOOLKIND);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjh181Form1.html", $arg); 
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>
