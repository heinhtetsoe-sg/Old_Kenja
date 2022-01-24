<?php
class knjl032yForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl032yindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjl032yQuery::getNameMst("L003", $model->year);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $model->testdiv = ($model->applicantdiv == $model->appHold) ? $model->testdiv : "";
        $namecd = ($model->applicantdiv == "1") ? "L024" : "L004";
        $query = knjl032yQuery::getNameMst($namecd, $model->year);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //全てチェック
        $objForm->ae(array("type"      => "checkbox",
                           "name"      => "CHECKALL",
                           "extrahtml" => "onClick=\"return check_all(this);\"" ));
        $arg["CHECKALL"] = $objForm->ge("CHECKALL");

        //試験時間割一覧
        makePtrnList($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl032yForm1.html", $arg); 
    }
}

//試験時間割一覧
function makePtrnList(&$objForm, &$arg, $db, $model)
{
    $arg["data"] = array();
    $query = knjl032yQuery::getPtrnList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $objForm->ae( array("type"        => "checkbox",
                            "name"        => "CHECKED",
                            "value"       => $row["PATTERN_NO"],
                            "extrahtml"   => "tabindex=\"-1\"",
                            "multiple"    => "1" ));
        $row["CHECKED"]      = $objForm->ge("CHECKED");
        $row["PATTERN_NAME"] = View::alink("#",htmlspecialchars($row["PATTERN_NAME"]),
                        "onclick=\"loadwindow('knjl032yindex.php?cmd=edit&mode=update&pattern_no=".$row["PATTERN_NO"] ."',event.x, event.y,450,375);\"");

        $arg["data"][] = $row;
    }
    $result->free();
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

    $arg["TOP"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //削除ボタン
    $disabled = (0 < count($arg["data"])) ? "" : "disabled ";
    $extra = $disabled ."onclick=\"return btn_submit('delete');\"";
    $arg["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    //試験時間割追加ボタン
    $extra = "onclick=\"loadwindow('knjl032yindex.php?cmd=edit&mode=insert',body.clientWidth/2-200,body.clientHeight/2-100,450,375);\"";
    $arg["btn_add"] = knjCreateBtn($objForm, "btn_add", "試験時間割追加", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");

    knjCreateHidden($objForm, "APP_HOLD", $model->applicantdiv);
}
?>
