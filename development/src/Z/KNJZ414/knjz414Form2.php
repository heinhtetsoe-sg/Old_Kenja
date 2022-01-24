<?php

require_once('for_php7.php');

class knjz414Form2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz414index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning) && isset($model->industry_lcd) && isset($model->industry_mcd)) {
            $query = knjz414Query::getIndustryMst($model->industry_lcd, $model->industry_mcd);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //大分類コードテキスト作成
        $extra = "onBlur=\"this.value=toAlphaNumber(this.value);\"";
        $arg["data"]["INDUSTRY_LCD"] = knjCreateTextBox($objForm, $Row["INDUSTRY_LCD"], "INDUSTRY_LCD", 1, 1, $extra);

        //大分類名称テキスト作成
        $arg["data"]["INDUSTRY_LNAME"] = knjCreateTextBox($objForm, $Row["INDUSTRY_LNAME"], "INDUSTRY_LNAME", 60, 60, "");

        //中分類コードテキスト作成
        $extra = "onBlur=\"this.value=toAlphaNumber(this.value);\"";
        $arg["data"]["INDUSTRY_MCD"] = knjCreateTextBox($objForm, $Row["INDUSTRY_MCD"], "INDUSTRY_MCD", 2, 2, $extra);

        //中分類名称テキスト作成
        $arg["data"]["INDUSTRY_MNAME"] = knjCreateTextBox($objForm, $Row["INDUSTRY_MNAME"], "INDUSTRY_MNAME", 60, 60, "");

        //出力しないチェックボックス
        $extra = ($Row["NO_OUTPUT"] == "1") ? "checked" : "";
        $extra .= " id=\"NO_OUTPUT\"";
        $arg["data"]["NO_OUTPUT"] = knjCreateCheckBox($objForm, "NO_OUTPUT", "1", $extra, "");

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz414index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz414Form2.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //更新
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_upate"] = knjCreateBtn($objForm, "btn_upate", "更 新", $extra);
    //削除
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
    //クリア
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $Row)
{
    knjCreateHidden($objForm, "cmd");
}
?>
