<?php

require_once('for_php7.php');

class knjz405Form2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz405index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $Row = $db->getRow(knjz405Query::getRow($model->point_l_cd, $model->point_m_cd), DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
        }

        //ねらいコード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["POINT_L_CD"] = knjCreateTextBox($objForm, $Row["POINT_L_CD"], "POINT_L_CD", 2, 2, $extra);

        //ねらい名称
        $extra = "";
        $arg["data"]["REMARK_L"] = knjCreateTextBox($objForm, $Row["REMARK_L"], "REMARK_L", 8, 8, $extra);
        $arg["data"]["REMARK_L_COMMENT"] = "(全角4文字まで)";

        //内容コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["POINT_M_CD"] = knjCreateTextBox($objForm, $Row["POINT_M_CD"], "POINT_M_CD", 2, 2, $extra);

        //内容
        $height = 3 * 13.5 + (3 - 1) * 3 + 5;
        $arg["data"]["REMARK_M"] = KnjCreateTextArea($objForm, "REMARK_M", 3, (17 * 2 + 1), "soft", "style=\"height:{$height}px;\"", $Row["REMARK_M"]);
        $arg["data"]["REMARK_M_COMMENT"] = "(全角17文字X3行まで)";

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.reload();";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz405Form2.html", $arg); 
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model) {
    //追加ボタン
    $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('add');\"" : "disabled";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
    //更新ボタン
    $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('update');\"" : "disabled";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタン
    $extra = (AUTHORITY >= DEF_UPDATE_RESTRICT) ? "onclick=\"return btn_submit('delete');\"" : "disabled";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"return closeWin();\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);
}
?>
