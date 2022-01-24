<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjd130dSubForm1 {
    function main(&$model) {
        $objForm = new form;
        $db = Query::dbCheckOut();
        $arg = array();
        $arg["start"]   = $objForm->get_start("subform1", "POST", "knjd130dindex.php", "", "subform1");
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //全てチェックボックス
        $extra = "onclick=\"checkAll()\"";
        $arg["ALL"] = knjCreateCheckBox($objForm, "ALL", "on", $extra);

        $query = knjd130dQuery::getClub($model);
        $result = $db->query($query);
        $i = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace("-","/",$row);
            $row["CLUBNAME"]      = $row["CLUBNAME"];

            $extra = "";
            $row["RCHECK"] = knjCreateCheckBox($objForm, "RCHECK" . $i, "on", $extra);

            $arg["data"][] = $row;

            knjCreateHidden($objForm, "HIDDEN_RCHECK" . $i, $row["CLUBNAME"] . "　" . $row["EXECUTIVE_NAME"] . "　" . $row["DETAIL_REMARK"]);
            $i++;
        }
        Query::dbCheckIn($db);

        /**********/
        /* ボタン */
        /**********/
        //終了
        $extra = "onclick=\"return parent.closeit()\"";
        $arg["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);
        //反映
        $extra = "onclick=\"addRemark();\"";
        $arg["btn_reflect"] = knjCreateBtn($objForm, "btn_reflect", "反 映", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd130dSubForm1.html", $arg);
    }
}
?>
