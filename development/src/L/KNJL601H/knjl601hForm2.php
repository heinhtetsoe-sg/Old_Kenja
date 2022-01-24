<?php
class knjl601hForm2
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjl601hindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && !VARS::get("chFlg")) {
            $query = knjl601hQuery::getRow($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //入試日程コード
        $extra = " onblur=\"this.value=toInteger(this.value);\" ";
        $arg["TOP"]["TESTDIV"] = knjCreateTextBox($objForm, $Row["TESTDIV"], "TESTDIV", 2, 2, $extra);

        //入試日程
        $extra = "";
        $arg["TOP"]["TESTDIV_NAME"] = knjCreateTextBox($objForm, $Row["TESTDIV_NAME"], "TESTDIV_NAME", 40, 20, $extra);

        //略称
        $extra = "";
        $arg["TOP"]["TESTDIV_ABBV"] = knjCreateTextBox($objForm, $Row["TESTDIV_ABBV"], "TESTDIV_ABBV", 10, 5, $extra);

        //入試日（西暦）
        $extra = "";
        $arg["TOP"]["TEST_DATE"] = View::popUpCalendar2($objForm, "TEST_DATE", str_replace("-", "/", $Row["TEST_DATE"]), "", "", $extra);

        //入試科目
        $result = $db->query(knjl601hQuery::getRowTestSubClass($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            array_walk($row, "htmlspecialchars_array");

            $subClassCd = "TESTSUBCLASSCD".$row["TESTSUBCLASSCD"];
            $extra = " id=\"".$subClassCd."\"";

            $extra .= ($row["CHECKED"]) ? " checked": "";
            $row["TESTSUBCLASSCD"] = knjCreateCheckBox($objForm, "TESTSUBCLASSCD[]", $row["TESTSUBCLASSCD"], $extra);
            $row["TESTSUBCLASS_NAME"] = "<label for=\"".$subClassCd."\">".$row["TESTSUBCLASS_NAME"]."</label>";

            $arg["data"][] = $row;
        }
        $result->free();
    
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

        if (VARS::get("cmd") != "edit") {
            $arg["reload"]  = "parent.left_frame.location.href='knjl601hindex.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl601hForm2.html", $arg);
    }
}

/********************************************* 以下関数 *******************************************************/
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
