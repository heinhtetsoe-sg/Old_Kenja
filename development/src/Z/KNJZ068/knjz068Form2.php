<?php

require_once('for_php7.php');

class knjz068Form2
{
    public function main(&$model)
    {
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz068index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        if (!isset($model->warning) && $model->cmd != "edit2" && $model->ibyear && $model->ibgrade && $model->ibclasscd && $model->ibprg_course && $model->ibcurriculum_cd && $model->ibsubclasscd && $model->ibeval_div1 != "" && $model->ibeval_div2 != "" && $model->ibeval_mark) {
            $query = knjz068Query::getIBViewNameYmst($model->ibyear, $model->ibgrade, $model->ibclasscd, $model->ibprg_course, $model->ibcurriculum_cd, $model->ibsubclasscd, $model->ibeval_div1, $model->ibeval_div2, $model->ibeval_mark);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //IB科目コンボ
        $query = knjz068Query::getIBSubclasscd($model, "list");
        $value = $Row["IBCLASSCD"].'-'.$Row["IBPRG_COURSE"].'-'.$Row["IBCURRICULUM_CD"].'-'.$Row["IBSUBCLASSCD"];
        makeCmb($objForm, $arg, $db, $query, "IBSUBCLASS", $value, "", 1, "BLANK");

        //評価区分1コンボ
        $query = knjz068Query::getNameMst($model, "Z035");
        $extra = "onchange=\"return btn_submit('edit2');\"";
        makeCmb($objForm, $arg, $db, $query, "IBEVAL_DIV1", $Row["IBEVAL_DIV1"], $extra, 1);

        //評価区分2コンボ
        if ($Row["IBEVAL_DIV1"] == "1") {
            $namecd1 = "Z037";
        } elseif ($Row["IBEVAL_DIV1"] == "2") {
            $namecd1 = "Z038";
        } else {
            $namecd1 = "Z036";
        }
        $query = knjz068Query::getNameMst($model, $namecd1);
        makeCmb($objForm, $arg, $db, $query, "IBEVAL_DIV2", $Row["IBEVAL_DIV2"], "", 1);

        //評価規準記号
        $extra = "STYLE=\"text-align: center\" onblur=\"this.value=ValueCheck(this.value)\"";
        $arg["data"]["IBEVAL_MARK"] = knjCreateTextBox($objForm, $Row["IBEVAL_MARK"], "IBEVAL_MARK", 3, 2, $extra);

        //評価規準名称
        $arg["data"]["IBEVAL_NAME"] = knjCreateTextBox($objForm, $Row["IBEVAL_NAME"], "IBEVAL_NAME", 50, 90, "");

        //評価規準略称
        $arg["data"]["IBEVAL_ABBV"] = knjCreateTextBox($objForm, $Row["IBEVAL_ABBV"], "IBEVAL_ABBV", 50, 90, "");

        //評価規準名称英字
        $arg["data"]["IBEVAL_NAME_ENG"] = knjCreateTextBox($objForm, $Row["IBEVAL_NAME_ENG"], "IBEVAL_NAME_ENG", 50, 90, "");

        //評価規準略称英字
        $arg["data"]["IBEVAL_ABBV_ENG"] = knjCreateTextBox($objForm, $Row["IBEVAL_ABBV_ENG"], "IBEVAL_ABBV_ENG", 50, 90, "");

        //表示順
        $extra = "onblur=\"this.value=toInteger(this.value)\" STYLE=\"text-align: right\"";
        $arg["data"]["IBSORT"] = knjCreateTextBox($objForm, $Row["IBSORT"], "IBSORT", 2, 2, $extra);

        //満点
        $extra = "onblur=\"this.value=toInteger(this.value)\" STYLE=\"text-align: right\"";
        $arg["data"]["IBPERFECT"] = knjCreateTextBox($objForm, $Row["IBPERFECT"], "IBPERFECT", 2, 2, $extra);

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CHK_IBYEAR", $model->ibyear);
        knjCreateHidden($objForm, "CHK_IBGRADE", $model->ibgrade);
        knjCreateHidden($objForm, "CHK_IBPRG_COURSE", $model->ibprg_course);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit" && $model->cmd != "edit2") {
            $arg["reload"] = "window.open('knjz068index.php?cmd=list&shori=add','left_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz068Form2.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $BLANK = "")
{
    $opt = array();
    $value_flg = false;
    if ($BLANK) {
        $opt[] = array('label' => "", 'value' => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //登録ボタン
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "登 録", $extra);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset')\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

    //CSV出力ボタン
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = knjCreateBtn($objForm, "btn_csv", "ＣＳＶ出力", $extra);
}
