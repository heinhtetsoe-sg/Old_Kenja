<?php

require_once('for_php7.php');

class knjp905_mainSubForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1", "POST", "knjp905_mainindex.php", "", "subform1");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        if(!isset($model->warning)){
            $query = knjp905_mainQuery::getRowSub($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->subField;
        }

        //初期値取得
        $query = knjp905_mainQuery::getLevyTaxMst($model, "TAX_VALUE");
        $syokiTesuu = $db->getOne($query);
        $query = knjp905_mainQuery::getLevyTaxMst($model, "TAX_SUMMARY");
        $syokiSummary = $db->getOne($query);

        //振込手数料
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $reqestTesuuRyo = ($Row["REQUEST_TESUURYOU"] != "") ? $Row["REQUEST_TESUURYOU"]: $syokiTesuu;
        $arg["data"]["REQUEST_TESUURYOU"] = knjCreateTextBox($objForm, $reqestTesuuRyo, "REQUEST_TESUURYOU", 5, 5, $extra);

        //振込手数料摘要
        $extra = "";
        $summary = ($Row["TESUURYOU_SUMMARY"] != "") ? $Row["TESUURYOU_SUMMARY"]: $syokiSummary;
        $arg["data"]["TESUURYOU_SUMMARY"] = knjCreateTextBox($objForm, $summary, "TESUURYOU_SUMMARY", 21, 30, $extra);

        //備考
        $extra = "";
        $arg["data"]["REMARK"] = knjCreateTextArea($objForm, "REMARK", "2", "60", "wrap", $extra, $Row["REMARK"]);

        //ボタン作成
        //更新
        $extra = "onclick=\"return btn_submit('update_remark')\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('cancel_remark');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra, "reset");
        //戻るボタン
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjp905_mainSubForm1.html", $arg); 
    }
}
?>

