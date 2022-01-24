<?php

require_once('for_php7.php');

class knjz900SubForm1
{
    function main(&$model){

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
           $arg["jscript"] = "OnAuthError();";
        }

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]  = $objForm->get_start("subform1", "POST", "knjz900index.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();

        if($model->cmd == "subform1"){
           $model->prg_id = "";
           $model->name = "";
           $model->menuid = "";
           $model->value = "";
           $model->field = array();
        }

        //年度
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //コース一覧取得
        $bifKey = "";
        $result = $db->query(knjz900Query::getList2());
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            //レコードを連想配列のまま配列$arg[data]に追加していく。
            array_walk($row, "htmlspecialchars_array");

            if ($bifKey != $row["NAME"].$row["VALUE"]) {
                $cnt = $db->getOne(knjz900Query::getPrgNameCnt2($row["NAME"], $row["VALUE"]));
                $row["ROWSPAN"] = ($cnt > 0) ? $cnt : 1;
            }
            $bifKey = $row["NAME"].$row["VALUE"];

            $arg["data"][] = $row;
        }
        $result->free();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && $model->name) {
            $Row = $db->getRow(knjz900Query::getRow2($model), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //リストToリスト作成
        makeListToList($objForm, $arg, $db, $model);

        /********************/
        /* テキストボックス */
        /********************/
        //パラメータ
        $extra = "onblur=\"this.value=toAlphanumeric(this.value)\"";
        $arg["data1"]["NAME"] = knjCreateTextBox($objForm, $Row["NAME"], "NAME", 50, 50, $extra);
        //値
        $arg["data1"]["VALUE"] = knjCreateTextBox($objForm, $Row["VALUE"], "VALUE", 100, 300, "");
        //備考
        $arg["data1"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 100, 300, "");

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz900SubForm1.html", $arg);
    }
}

//リストToリスト作成
function makeListToList(&$objForm, &$arg, $db, $model) {

    //対象プログラムリストを作成する
    $opt_left = $opt_right = array();
    $query = knjz900Query::getProgramList($model, "right");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_right[] = array('label' => $row["LABEL"],
                             'value' => $row["VALUE"]);
    }
    $result->free();
    $extra = "multiple style=\"width:350px\" width:\"350px\" ondblclick=\"move1('left')\"";
    $arg["data1"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $opt_right, $extra, 10);

    //プログラム一覧リストを作成する
    $query = knjz900Query::getProgramList($model, "left");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_left[] = array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
    }
    $result->free();

    $extra = "multiple style=\"width:350px\" width:\"350px\" ondblclick=\"move1('right')\"";
    $arg["data1"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $opt_left, $extra, 10);

    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"";
    $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"";
    $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"";
    $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"";
    $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('subform1_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //クリアボタンを作成する
    $extra = "onclick=\"return btn_submit('subform1_reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタンを作成する
    $extra = "onclick=\"return btn_submit('main');\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"]   = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
}
?>
