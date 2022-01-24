<?php

require_once('for_php7.php');

class knjx_iinkaiForm1 {
    function main(&$model) {

        $objForm = new form;

        //DB接続
        $db = Query::dbCheckOut();

        $arg = array();
        $arg["start"]   = $objForm->get_start("form1", "POST", "knjx_iinkaiindex.php", "", "form1");

        //生徒情報
        $schInfo = $db->getRow(knjx_iinkaiQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$schInfo["NAME"];

        //全てチェックボックス
        $extra = "onclick=\"checkAll()\"";
        $arg["ALL"] = knjCreateCheckBox($objForm, "ALL", "on", $extra);

        $query = knjx_iinkaiQuery::getCommittee($model);
        $result = $db->query($query);
        $i = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace("-","/",$row);
            $row["COMMITTEENAME_CHARGENAME"] = $row["SEQ"] . ":" . $row["COMMITTEENAME"] . " ／ " . $row["CHARGENAME"];

            $extra = "";
            $row["RCHECK"] = knjCreateCheckBox($objForm, "RCHECK" . $i, "on", $extra);

            $arg["data"][] = $row;

            knjCreateHidden($objForm, "HIDDEN_RCHECK" . $i, $row["COMMITTEENAME"] . " ／ " . $row["CHARGENAME"] . "　" . $row["DETAIL_REMARK"]);
            $i++;
        }

        //終了ボタンを作成する
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //反映ボタンを作成する
        $extra = "onclick=\"addRemark();\"";
        $arg["btn_reflect"] = knjCreateBtn($objForm, "btn_reflect", "反 映", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "setField", $model->setField);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjx_iinkaiForm1.html", $arg);
    }
}
?>
