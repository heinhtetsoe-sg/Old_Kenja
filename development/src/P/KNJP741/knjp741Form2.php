<?php

require_once('for_php7.php');

class knjp741form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjp741index.php", "", "edit");

        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if ($model->cmd == "edit" && isset($model->schregno) && !isset($model->warning)) { 
            $query = knjp741Query::selectQuery($model, "bottom");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $model->field = $row;
            }
        }

        //返金基準日
        $model->field["REPAY_DATE"] = str_replace("-", "/", CTRL_DATE);
        $arg["data"]["REPAY_DATE"] = View::popUpCalendar($objForm, "REPAY_DATE", str_replace("-", "/", $model->field["REPAY_DATE"]));

        //精算額
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["REPAY_MONEY"] = knjCreateTextBox($objForm, $model->field["REPAY_MONEY"], "REPAY_MONEY", 7, 7, $extra);

        //返金区分
        $opt = array(1, 2, 3);
        $model->field["REPAY_DIV"] = $model->field["REPAY_DIV"] ? $model->field["REPAY_DIV"] : "1";
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"REPAY_DIV{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "REPAY_DIV", $model->field["REPAY_DIV"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //キャンセル
        $extra = " id=\"CANCEL_FLG\" ";
        $checked = $model->field["CANCEL_FLG"] == "1" ? " checked " : "";
        $arg["data"]["CANCEL_FLG"] = knjCreateCheckBox($objForm, "CANCEL_FLG", "1", $extra.$checked);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "checkSchreg", $model->schregno);
        knjCreateHidden($objForm, "SCHOOLCD", $model->field["SCHOOLCD"]);
        knjCreateHidden($objForm, "SCHOOL_KIND", $model->field["SCHOOL_KIND"]);
        knjCreateHidden($objForm, "YEAR", $model->field["YEAR"]);
        knjCreateHidden($objForm, "REPAY_SLIP_NO", $model->field["REPAY_SLIP_NO"]);

        $arg["finish"]  = $objForm->get_finish();
        if (isset($model->message)){
            $arg["reload"]  = "window.open('knjp741index.php?cmd=list&SCHREGNO=$model->schregno','top_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjp741Form2.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg) {

    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('insert');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);

    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //削除ボタンを作成する
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

    //クリアボタンを作成する
    $extra = "onclick=\"return Btn_reset('edit');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタンを作成する
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

?>
