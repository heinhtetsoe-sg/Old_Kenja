<?php

require_once('for_php7.php');

class knjz350_nenkan_testitemForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjz350_nenkan_testitemindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //対象年度
        $arg["YEAR"] = CTRL_YEAR;

        //校種
        $query = knjz350_nenkan_testitemQuery::getNameMst($model);
        $extra = "onChange=\"return btn_submit('leftChange');\"";
        makeCmb($objForm, $arg, $db, $query, $model->leftScoolkind, "LEFT_SCHOOL_KIND", $extra, "");
        $model->setSchoolKind = $model->leftScoolkind;

        //リスト作成
        makeList($arg, $db, $model);

        //コピーボタンを作成する
        $extra = "style=\"width:130px\" onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //左のフレームを表示し終わってから右のフレームを表示しないとセッションの値がレスポンスのタイミングによって変わる
        //indexの「分割フレーム作成」では右フレームを呼ばない。
        if (VARS::get("shori") != "add") {
            $arg["reload"] = "window.open('knjz350_nenkan_testitemindex.php?cmd=edit','right_frame')";
        }

        
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz350_nenkan_testitemForm1.html", $arg); 
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $blank = "")
{
    $result = $db->query($query);
    $opt = array();

    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
    }
    $value = ($value) ? $value : $opt[0]["value"];
    $arg["data1"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, 1);
}
//リスト作成
function makeList(&$arg, $db, $model)
{
    $query = knjz350_nenkan_testitemQuery::getList($model, $model->Properties["useTestCountflg"]);
    $result = $db->query($query);
    $bifKey = "";
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_walk($row, "htmlspecialchars_array");

        if ($bifKey !== $row["KEY"]) {
            $cnt = $db->getOne(knjz350_nenkan_testitemQuery::getListCount($model, $row));
            $row["ROWSPAN"] = $cnt > 0 ? $cnt : 1;
        }
        $bifKey = $row["KEY"];
        if ($row["SET_SUBCLASSCD"] === '00-J-00-000000') {
            $row["SET_SUBCLASSNAME"] = $row["SET_SUBCLASSCD"].':基本設定(中学)';
        }
        if ($row["SET_SUBCLASSCD"] === '00-H-00-000000') {
            $row["SET_SUBCLASSNAME"] = $row["SET_SUBCLASSCD"].':基本設定(高校)';
        }
        $arg["data"][] = $row;
    }

    $result->free();
}
?>
