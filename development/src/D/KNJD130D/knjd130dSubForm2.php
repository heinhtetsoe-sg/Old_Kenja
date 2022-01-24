<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjd130dSubForm2 {
    function main(&$model) {
        $objForm = new form;
        $db = Query::dbCheckOut();
        $arg = array();
        $arg["start"]   = $objForm->get_start("subform2", "POST", "knjd130dindex.php", "", "subform2");
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //年度・学期表示
        $arg["YEAR_SEMESTER"] = CTRL_YEAR."年度　".CTRL_SEMESTERNAME;

        //全てチェックボックス
        $extra = "onclick=\"checkAll()\"";
        $arg["ALL"] = knjCreateCheckBox($objForm, "ALL", "on", $extra);

        //委員会リスト
        $query = knjd130dQuery::getCommittee($model);
        $result = $db->query($query);
        $i = 0;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace("-","/",$row);
            $row["COMMITTEENAME_CHARGENAME"] = $row["SEQ"] . ":" . $row["COMMITTEENAME"] . " ／ " . $row["CHARGENAME"];

            $extra = "";
            $row["RCHECK"] = knjCreateCheckBox($objForm, "RCHECK" . $i, "on", $extra);

            $arg["data"][] = $row;

            knjCreateHidden($objForm, "HIDDEN_RCHECK" . $i, $row["COMMITTEENAME"] . " ／ " . $row["CHARGENAME"] . "　" . $row["EXECUTIVE_NAME"] . "　" . $row["DETAIL_REMARK"]);
            $i++;
        }

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

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjd130dSubForm2.html", $arg);
    }
}
?>
