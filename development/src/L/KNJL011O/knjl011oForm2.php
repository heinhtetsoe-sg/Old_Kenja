<?php

require_once('for_php7.php');

class knjl011oForm2
{
    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl011oindex.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //基礎データ
        if ((!isset($model->warning)) && (!is_array($existdata))) {
            $query = knjl011oQuery::getBrotherInfo($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->brother;
        }

        //受験番号・氏名
        $arg["data"]["BROTHER_EXAMNO"] = $Row["BROTHER_EXAMNO"];
        $arg["data"]["BROTHER_NAME"] = $Row["BROTHER_NAME"];
        knjCreateHidden($objForm, "BROTHER_EXAMNO", $Row["BROTHER_EXAMNO"]);
        knjCreateHidden($objForm, "BROTHER_NAME", $Row["BROTHER_NAME"]);

        //以下、入力項目------------------------------------

        //第１志望
        $extra = (strlen($Row["BROTHER_REMARK1"])) ? "checked" : "";
        $arg["data"]["BROTHER_REMARK1"] = knjCreateCheckBox($objForm, "BROTHER_REMARK1", "1", $extra);

        //在校生氏名
        $extra = "";
        $arg["data"]["BROTHER_REMARK2"] = knjCreateTextBox($objForm, $Row["BROTHER_REMARK2"], "BROTHER_REMARK2", 30, 30, $extra);

        //在籍クラス
        $extra = "";
        $arg["data"]["BROTHER_REMARK3"] = knjCreateTextBox($objForm, $Row["BROTHER_REMARK3"], "BROTHER_REMARK3", 30, 30, $extra);

        //併願予定校
        //size   = 文字数 * 2 + 1
        //height = 行数 * 13.5 + (行数 -1) * 3 + 5
        $extra = "style=\"height:35px;\"";
        $arg["data"]["BROTHER_REMARK4"] = knjCreateTextArea($objForm, "BROTHER_REMARK4", 2, 31, "soft", $extra, $Row["BROTHER_REMARK4"]);

        //更新ボタン
        $disabled = (strlen($Row["BROTHER_EXAMNO"])) ? "" : " disabled";
        $extra = "onclick=\"btn_submit('brother_update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);

        //戻るボタン
        $extra = "onclick=\"top.main_frame.closeit();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjl011oForm2.html", $arg); 
    }
}
?>
