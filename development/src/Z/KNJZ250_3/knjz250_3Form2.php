<?php

require_once('for_php7.php');

class knjz250_3Form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz250_3index.php", "", "edit");
        //警告メッセージを表示しない場合
        if (isset($model->certif_div) && !isset($model->warning))
        {
            $Row = knjz250_3Query::getRow($model->certif_div);
            $temp_cd = $Row["CERTIF_DIV"];
        }else{
            $Row =& $model->field;
        }

        $db = Query::dbCheckOut();

        /******************/
        /* リストtoリスト */
        /******************/
        knjz250_3Form2::makeListToList($objForm, $arg, $db, $model, $disabled, $Row);

        Query::dbCheckIn($db);

        //証明書区分
        //$extra = "onblur=\"btn_submit('edit');\"";
        //$extra = "onchange=\"btn_submit('edit');\"";
        $extra = "";
        $arg["data"]["CERTIF_DIV"] = knjCreateTextBox($objForm, $Row["CERTIF_DIV"], "CERTIF_DIV", 1, 1, $extra);

        $extra = "";
        $arg["data"]["CERTIF_DIV_NAME"] = knjCreateTextBox($objForm, $Row["CERTIF_DIV_NAME"], "CERTIF_DIV_NAME", 60, 30, $extra);

        //追加ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\"" ) );

        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //修正ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );

        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //削除ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\"" ) );

        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタンを作成する
        $objForm->ae( array("type" => "reset",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return Btn_reset('reset');\"" ) );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //戻るボタンを作成する
        $link = REQUESTROOT."/Z/KNJZ250/knjz250index.php?year_code=".$model->year_code;
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_back",
                            "value"       => "戻 る",
                            "extrahtml"   => "onclick=\"parent.location.href='$link';\"" ) );

        $arg["button"]["btn_back"] = $objForm->ge("btn_back");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata"
                            ) );

        $cd_change = true;
        if ($temp_cd==$Row["CERTIF_DIV"] ) $cd_change = false;

        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz250_3index.php?cmd=list';";
        }

        $arg["TITLE"] = "マスタメンテナンスー証明書グルーピング";

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz250_3Form2.html", $arg); 
    }

    //一覧リストToリスト作成
    function makeListToList(&$objForm, &$arg, $db, $model, $disabled, $Row) {
        //右フレームのリストの左側に表示すべきコードを取得
        $selectedArray = array();
        if (isset($model->warning)) { //warningの時
            $selectedArray = $model->selectdata;
        } else {
            $query = knjz250_3Query::getSubclass2($model, $Row["CERTIF_DIV"]);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $selectedArray[] = $row["VALUE"];
            }
        }

        //一覧
        $leftList = $rightList = array();
        $query = knjz250_3Query::getSubclass($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if (in_array($row["VALUE"], $selectedArray)) { //リストの左側を作る
                $leftList[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
            } else {                                         //リストの右側を作る
                $rightList[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
            }
        }
        $result->free();

        //一覧作成
        $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('left')\"".$disabled;
        $arg["data"]["CATEGORY_NAME"] = knjCreateCombo($objForm, "CATEGORY_NAME", "", $rightList, $extra, 20);

        //出力対象作成
        $extra = "multiple style=\"width:230px\" width=\"230px\" ondblclick=\"move1('right')\"".$disabled;
        $arg["data"]["CATEGORY_SELECTED"] = knjCreateCombo($objForm, "CATEGORY_SELECTED", "", $leftList, $extra, 20);

        // << ボタン作成
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('left');\"".$disabled;
        $arg["button"]["btn_lefts"] = knjCreateBtn($objForm, "btn_lefts", "<<", $extra);
        // ＜ ボタン作成
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('left');\"".$disabled;
        $arg["button"]["btn_left1"] = knjCreateBtn($objForm, "btn_left1", "＜", $extra);
        // ＞ ボタン作成
        $extra = "style=\"height:20px;width:40px\" onclick=\"move1('right');\"".$disabled;
        $arg["button"]["btn_right1"] = knjCreateBtn($objForm, "btn_right1", "＞", $extra);
        // >> ボタン作成
        $extra = "style=\"height:20px;width:40px\" onclick=\"moves('right');\"".$disabled;
        $arg["button"]["btn_rights"] = knjCreateBtn($objForm, "btn_rights", ">>", $extra);
    }

}
?>
