<?php

require_once('for_php7.php');

class knjz261_dutyshare_reflectionForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjz261_dutyshare_reflectionForm1", "POST", "knjz261_dutyshare_reflectionindex.php", "", "knjz261_dutyshare_reflectionForm1");

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        //DB接続
        $db = Query::dbCheckOut();
        $db2 = Query::dbCheckOut2();

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $db2, $model);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "selectdata");

        //DB切断
        Query::dbCheckIn($db);
        Query::dbCheckIn($db2);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz261_dutyshare_reflectionForm1.html", $arg);
    }
}

//リストTOリスト作成
function makeListToList(&$objForm, &$arg, $db, $db2, $model) {
    //教育委員会校務分掌一覧取得
    $opt_left = $opt_right = array();
    $query  = knjz261_dutyshare_reflectionQuery::selectQuery();
    $result = $db2->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //学校校務分掌一覧取得
        $query = knjz261_dutyshare_reflectionQuery::selectQuery();
        $school_dutyshare = $db->getCol($query);
        //学校DBにあるものは、左リストに表示し、それ以外は、右リストに表示する。
        if (0 < get_count($school_dutyshare) && in_array($row["DUTYSHARECD"], $school_dutyshare)) {
            $opt_left[]     = array("label" => $row["DUTYSHARECD"]."　".$row["SHARENAME"],
                                    "value" => $row["DUTYSHARECD"]);
        } else {
            $opt_right[]    = array("label" => $row["DUTYSHARECD"]."　".$row["SHARENAME"],
                                    "value" => $row["DUTYSHARECD"]);
        }
    }
    $result->free();

    //教育委員会校務分掌一覧リスト
    $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('left')\"";
    $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 20);

    //学校校務分掌一覧リスト
    $extra = "multiple style=\"width:230px\" width=\"230px\"";
    $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 20);

    //対象選択ボタン（全部）
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタン
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //更新ボタン
    $extra = "onclick=\"return doSubmit();\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "取 込", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);
    //戻るボタン
    $link = REQUESTROOT."/Z/KNJZ261A/knjz261aindex.php?";
    $extra = "onclick=\"document.location.href='$link'\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
}
?>
