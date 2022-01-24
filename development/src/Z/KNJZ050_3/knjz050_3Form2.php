<?php

require_once('for_php7.php');

class knjz050_3Form2
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz050_3index.php", "", "edit");
        //警告メッセージを表示しない場合

        if (!isset($model->warning))
        {
            $Row = knjz050_3Query::getRow($model);
        } else {
            $Row =& $model->field;
        }
        //DB接続
        $db     = Query::dbCheckOut();
        
        //選択した課程学科を表示
        $arg["data"]["COURSE_MAJORCD_SET"] = $Row["COURSE_MAJORCD"].'　 '.$Row["COURSE_MAJORNAME"];
        
        //基本の学校区分
        //$school_div = $db->getOne(knjz050_3Query::getSchoolDiv($model, "CD"));

        //課程コード
        $opt = array();
        $opt[] = array('label' => "",'value' => "XXX");
        $query = knjz050_3Query::getMajorSchoolDiv();
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $extra = "";
        $arg["data"]["SCHOOLDIV"] = knjCreateCombo($objForm, "SCHOOLDIV", $Row["SCHOOLDIV"], $opt, $extra, 1);

        //修正ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_udpate",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

        //終了ボタン
        $link = REQUESTROOT."/Z/KNJZ050/knjz050index.php";
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_back",
                            "value"       => "戻 る",
                            "extrahtml"   => "onclick=\"parent.location.href='$link';\"" ) );
        $arg["button"]["btn_back"] = $objForm->ge("btn_back");

        //hidden
        $objForm->ae( array("type"        => "hidden",
                            "name"        => "cmd"
                            ) );

        $objForm->ae( array("type"        => "hidden",
                            "name"        => "UPDATED",
                            "value"       => $Row["UPDATED"]
                            ) );
        $arg["finish"]  = $objForm->get_finish();

        Query::dbCheckIn($db);

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.left_frame.location.href='knjz050_3index.php?cmd=list';";
        }
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz050_3Form2.html", $arg);
    }
}
?>
