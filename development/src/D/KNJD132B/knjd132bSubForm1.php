<?php

require_once('for_php7.php');

class knjd132bSubForm1 {
    function main(&$model) {
        $objForm = new form;
        $db = Query::dbCheckOut();
        $arg = array();
        $arg["start"]   = $objForm->get_start("subform1", "POST", "knjd132bindex.php", "", "subform1");
        $arg["NAME_SHOW"] = $model->schregno."  :  ".$model->name;

        //全てチェックボックス
        $extra = "onclick=\"checkAll()\"";
        $arg["ALL"] = knjCreateCheckBox($objForm, "ALL", "on", $extra);

        $query = knjd132bQuery::getClub($model);
        $result = $db->query($query);
        $i = 0;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row = str_replace("-","/",$row);
            $row["CLUBNAME"]      = $row["CLUBNAME"];

            $extra = "";
            $row["RCHECK"] = knjCreateCheckBox($objForm, "RCHECK" . $i, "on", $extra);

            $arg["data"][] = $row;

            knjCreateHidden($objForm, "HIDDEN_RCHECK" . $i, $row["CLUBNAME"] . "　" . $row["DETAIL_REMARK"]);
            $i++;
        }
        Query::dbCheckIn($db);

        //終了ボタンを作成する
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_back",
                            "value"     => "戻 る",
                            "extrahtml" => "onclick=\"return parent.closeit()\"" ));
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

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd132bSubForm1.html", $arg);
    }
}
?>
