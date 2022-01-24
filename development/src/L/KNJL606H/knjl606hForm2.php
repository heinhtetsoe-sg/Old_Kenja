<?php
class knjl606hForm2
{
    public function main(&$model)
    {
        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjl606hindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && !VARS::get("chFlg")) {
            $query = knjl606hQuery::getRow($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //類別コード
        $extra = " onblur=\"this.value=toInteger(this.value);\" ";
        $arg["TOP"]["CLASSIFY_CD"] = knjCreateTextBox($objForm, $Row["CLASSIFY_CD"], "CLASSIFY_CD", 2, 2, $extra);

        //類別
        $extra = "";
        $arg["TOP"]["CLASSIFY_NAME"] = knjCreateTextBox($objForm, $Row["CLASSIFY_NAME"], "CLASSIFY_NAME", 20, 10, $extra);

        //記号
        $extra = "";
        $arg["TOP"]["MARK"] = knjCreateTextBox($objForm, $Row["MARK"], "MARK", 2, 2, $extra);

        //加点
        $extra = " onblur=\"this.value=toInteger(this.value);\" ";
        $arg["TOP"]["PLUS_POINT"] = knjCreateTextBox($objForm, $Row["PLUS_POINT"], "PLUS_POINT", 3, 3, $extra);

        //確約
        $extra = " id=\"COMMITMENT_FLG\"";
        $extra .= ($Row["COMMITMENT_FLG"]) ? " checked": "";
        $arg["TOP"]["COMMITMENT_FLG"] = knjCreateCheckBox($objForm, "COMMITMENT_FLG", "1", $extra);

        //集計
        $extra = " id=\"CALC_FLG\"";
        $extra .= ($Row["CALC_FLG"]) ? " checked": "";
        $arg["TOP"]["CALC_FLG"] = knjCreateCheckBox($objForm, "CALC_FLG", "1", $extra);

        //表示順
        $extra = "onblur=\"this.value=toInteger(this.value);\"";
        $arg["TOP"]["ORDER"] = knjCreateTextBox($objForm, $Row["ORDER"], "ORDER", 2, 1, $extra);

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
            $arg["reload"]  = "parent.left_frame.location.href='knjl606hindex.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl606hForm2.html", $arg);
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
