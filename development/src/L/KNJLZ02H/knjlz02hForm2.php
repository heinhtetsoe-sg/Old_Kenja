<?php
class knjlz02hForm2 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjlz02hindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && !VARS::get("chFlg") && $model->testSubCd) {
            $query = knjlz02hQuery::getRow($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //科目コード
        $extra = " onblur=\"this.value=toInteger(this.value);\" ";
        $arg["data"]["TESTSUBCLASSCD"] = knjCreateTextBox($objForm, $Row["TESTSUBCLASSCD"], "TESTSUBCLASSCD", 2, 2, $extra);

        //科目名称
        $extra = "";
        $arg["data"]["TESTSUBCLASS_NAME"] = knjCreateTextBox($objForm, $Row["TESTSUBCLASS_NAME"], "TESTSUBCLASS_NAME", 12, 10, $extra);

        //満点
        $extra = "";
        $arg["data"]["PERFECT"] = knjCreateTextBox($objForm, $Row["PERFECT"], "PERFECT", 3, 3, $extra);

        //氏名・性別非表示チェックボックス
        $extra  = "id=\"NAMESEX_HIDE_FLAG\" ";
        $extra .= $Row["NAMESEX_HIDE_FLAG"] == '1' ? "checked" : "";
        $arg["data"]["NAMESEX_HIDE_FLAG"] = knjCreateCheckBox($objForm, "NAMESEX_HIDE_FLAG", "1", $extra);

        //科目種別コンボ 1:面接 2:作文 BLANKあり
        $extra = "";
        $testSubClassKindList = array(
            array("VALUE" => "1", "LABEL" => "面接"),
            array("VALUE" => "2", "LABEL" => "作文"),
        );
        makeCmbDataList($objForm, $arg, $testSubClassKindList, "TESTSUBCLASS_KIND", $Row["TESTSUBCLASS_KIND"], $extra, 1, "BLANK");

        /********/
        /*ボタン*/
        /********/
        //追加
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //更新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if (!isset($model->warning) && $model->cmd == "updEdit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjlz02hindex.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjlz02hForm2.html", $arg); 
    }
}

/********************************************* 以下関数 *******************************************************/
//コンボ作成
function makeCmbDataList(&$objForm, &$arg, $dataList, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }

    foreach ($dataList as $row) {
        $opt[] = array('label' => $row["VALUE"].":".$row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
