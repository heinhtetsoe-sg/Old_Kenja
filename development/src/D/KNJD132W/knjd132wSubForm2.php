<?php

require_once('for_php7.php');

class knjd132wSubForm2 {
    function main(&$model) {
        $objForm = new form;
        $db = Query::dbCheckOut();
        $arg = array();
        $arg["start"]   = $objForm->get_start("subform2", "POST", "knjd132windex.php", "", "subform2");
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //年度・学期表示
        $arg["YEAR_SEMESTER"] = CTRL_YEAR."年度　".CTRL_SEMESTERNAME;

        //全てチェックボックス
        $extra = "onclick=\"checkAll()\"";
        $arg["ALL"] = knjCreateCheckBox($objForm, "ALL", "on", $extra);

        //委員会リスト
        $query = knjd132wQuery::getCommittee($model);
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
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_back",
                            "value"     => "戻 る",
                            "extrahtml" => "onclick=\"return top.main_frame.right_frame.closeit()\"" ));
        $arg["btn_back"] = $objForm->ge("btn_back");

        //反映ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_reflect",
                            "value"     => "反 映",
                            "extrahtml" => "onclick=\"addRemark();\"" ));
        $arg["btn_reflect"] = $objForm->ge("btn_reflect");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );

        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjd132wSubForm2.html", $arg);
    }
}
?>

