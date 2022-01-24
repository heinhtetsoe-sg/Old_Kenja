<?php

require_once('for_php7.php');

class knjc033kSubForm {
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("new", "POST", "knjc033kindex.php", "", "new");

        $db = Query::dbCheckOut();

        $result = $db->query(knjc033kQuery::selectStudent($model->schregno, $model->chaircd, $model->grade));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $row["ATTENDDATE"] = str_replace("-", "/", $row["ATTENDDATE"]);
            $row["PERIODCD"] = $row["NAME1"]; 
            $arg["data"][] = $row;
        }
        Query::dbCheckIn($db);

        //戻るボタンを作成する
        $objForm->ae( array("type"          => "button",
                            "name"          => "btn_back",
                            "value"         => "戻 る",
                            "extrahtml"     => "onclick=\"return top.main_frame.closeit()\""
                           ) );
        $arg["btn_back"] = $objForm->ge("btn_back");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                           ) );

        $arg["finish"]  = $objForm->get_finish();
        
        $arg["info"]["NAME_SHOW"] = knjc033kQuery::getStudentName($model->schregno);
        $arg["info"]["SCHREGNO"] = $model->schregno;
        $arg["info"]["ATTENDNO"] = $model->attendno;
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();        
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc033kSubForm.html", $arg);
    }
}
?>
