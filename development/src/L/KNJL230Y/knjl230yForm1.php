<?php

require_once('for_php7.php');

class knjl230yForm1
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl230yindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        //入試制度コンボボックス
        $extra = "onchange=\"return btn_submit('main');\"";
        $query = knjl230yQuery::getNameMst("L003", $model->year);
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $model->applicantdiv, $extra, 1);

        //入試区分コンボボックス
        $query = knjl230yQuery::getNameMst("L004", $model->year);
        makeCmb($objForm, $arg, $db, $query, "TESTDIV", $model->testdiv, $extra, 1);

        //全てチェック
        $objForm->ae(array("type"      => "checkbox",
                           "name"      => "CHECKALL",
                           "extrahtml" => "onClick=\"return check_all(this);\"" ));
        $arg["CHECKALL"] = $objForm->ge("CHECKALL");

        //グループ一覧
        $capa_cnt_all = makeHallList($objForm, $arg, $db, $model);

        //割振り人数ALL
        $arg["dataCnt"]["CAPA_CNT_ALL"] = $capa_cnt_all;
        //受付データ人数ALL
        $recept_cnt_all = $db->getOne(knjl230yQuery::getReceptCntALL($model));
        $arg["dataCnt"]["RECEPT_CNT_ALL"] = $recept_cnt_all;

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
        View::toHTML($model, "knjl230yForm1.html", $arg); 
    }
}

//グループ一覧
function makeHallList(&$objForm, &$arg, $db, $model) {
    $capa_cnt_all = 0;

    $arg["data"] = array();
    $query = knjl230yQuery::selectQuery($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $objForm->ae( array("type"        => "checkbox",
                            "name"        => "CHECKED",
                            "value"       => $row["EXAMHALLCD"],
                            "extrahtml"   => "tabindex=\"-1\"",
                            "multiple"    => "1" ));
        $row["CHECKED"]     = $objForm->ge("CHECKED");
        $row["EXAMHALL_NAME"] = View::alink("#",htmlspecialchars($row["EXAMHALL_NAME"]),
                                "onclick=\"loadwindow('knjl230yindex.php?cmd=edit&mode=update&examhallcd=".$row["EXAMHALLCD"] ."',event.x, event.y,700,460);\"");

        $arg["data"][] = $row;
        $capa_cnt_all += $row["CAPA_CNT"];
    }
    $result->free();

    return $capa_cnt_all;
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
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
    //グループ追加ボタン
    $extra = "onclick=\"loadwindow('knjl230yindex.php?cmd=edit&mode=insert',body.clientWidth/2-200,body.clientHeight/2-200,700,460);\"";
    $arg["btn_add"] = knjCreateBtn($objForm, "btn_add", "グループ追加", $extra);
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
