<?php

require_once('for_php7.php');

class knjl030yForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl030yindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjl030yQuery::getNameMst("L003", $model->year);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $model->testdiv = ($model->applicantdiv == $model->appHold) ? $model->testdiv : "";
        $namecd = ($model->applicantdiv == "1") ? "L024" : "L004";
        $query = knjl030yQuery::getNameMst($namecd, $model->year);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //全てチェック
        $objForm->ae(array("type"      => "checkbox",
                           "name"      => "CHECKALL",
                           "extrahtml" => "onClick=\"return check_all(this);\"" ));
        $arg["CHECKALL"] = $objForm->ge("CHECKALL");

        //会場一覧
        makeHallList($objForm, $arg, $db, $model);

        //座席番号生成済みリスト
        makeReceptList($objForm, $arg, $db, $model);

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
        View::toHTML($model, "knjl030yForm1.html", $arg); 
    }
}

//会場一覧
function makeHallList(&$objForm, &$arg, $db, $model)
{
    $arg["data"] = array();
    unset($model->max_examhallcd);
    unset($model->e_receptno);
    $query = knjl030yQuery::selectQuery($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $objForm->ae( array("type"        => "checkbox",
                            "name"        => "CHECKED",
                            "value"       => $row["EXAMHALLCD"],
                            "extrahtml"   => "tabindex=\"-1\"",
                            "multiple"    => "1" ));
        $row["CHECKED"]     = $objForm->ge("CHECKED");
        $row["EXAMHALL_NAME"] = View::alink("#",htmlspecialchars($row["EXAMHALL_NAME"]),
                        "onclick=\"loadwindow('knjl030yindex.php?cmd=edit&mode=update&examhallcd=".$row["EXAMHALLCD"] ."',event.x, event.y,400,350);\"");

        $arg["data"][] = $row;
    }
    $result->free();
}

//座席番号生成済みリスト
function makeReceptList(&$objForm, &$arg, $db, $model)
{
    $model->capa_cnt_recept = 0;
    $query = knjl030yQuery::getReceptCnt($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //高校(1:学特、3:一般)の場合、共愛中学出身者のうちわけも表示する
        if ($model->applicantdiv == "2" && ($model->testdiv == "1" || $model->testdiv == "3")) {
            $naibu_label  = ($row["NAIBU"] == "6") ? "(共愛中)" : "";
            $row["LABEL"] = $row["LABEL"] .$naibu_label;
        }
        //帰国生対応(高校のみ)の場合、帰国生のうちわけも表示する
        $kikoku_label = ($row["KIKOKU"] == "1") ? "(帰国生)" : "";
        $row["LABEL"] = $row["LABEL"] .$kikoku_label;
        //受験番号取得
        $row["S_EXAMNO"] = $db->getOne(knjl030yQuery::getReceptExamno($model, $row["S_RECEPTNO"]));
        $row["E_EXAMNO"] = $db->getOne(knjl030yQuery::getReceptExamno($model, $row["E_RECEPTNO"]));
        $arg["data2"][] = $row;
        $model->capa_cnt_recept += $row["CNT_RECEPTNO"];
    }
    $result->free();
    //受付データの合計人数を保持
    knjCreateHidden($objForm, "CAPA_CNT_RECEPT", $model->capa_cnt_recept);
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
    $disabled = (0 < get_count($arg["data"])) ? "" : "disabled ";
    $extra = $disabled ."onclick=\"return btn_submit('delete');\"";
    $arg["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    //会場追加ボタン
    $extra = "onclick=\"loadwindow('knjl030yindex.php?cmd=edit&mode=insert',body.clientWidth/2-200,body.clientHeight/2-100,400,350);\"";
    $arg["btn_add"] = knjCreateBtn($objForm, "btn_add", "会場追加", $extra);
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
