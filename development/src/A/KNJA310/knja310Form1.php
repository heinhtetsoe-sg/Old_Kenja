<?php

require_once('for_php7.php');

class knja310Form1
{
    function main(&$model){

        $objForm = new form;
        $arg["start"]   = $objForm->get_start("knja310Form1", "POST", "knja310index.php", "", "knja310Form1");


        //各生徒の出力枚数テキスト
        $objForm->ae( array("type"        => "text",
                            "name"        => "BUSUU",
                            "size"        => 2,
                            "maxlength"   => 1,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => isset($model->field["BUSUU"]) ? $model->field["BUSUU"] : 1 ));//初期値
        $arg["data"]["BUSUU"] = $objForm->ge("BUSUU");

        //年度・学期
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => CTRL_YEAR ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GAKKI",
                            "value"     => CTRL_SEMESTER ) );
        $arg["data"]["YEAR"]    = CTRL_YEAR . "年度";
        $arg["data"]["GAKKI"]   = CTRL_SEMESTERNAME;

        //年組コンボ
        $row1 = array();
        $db = Query::dbCheckOut();
        $query = common::getHrClassAuth(CTRL_YEAR ,CTRL_SEMESTER ,AUTHORITY ,STAFFCD);//共通関数(年組一覧を取得するSQL)
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        if (!isset($model->field["GRADE_HR_CLASS"])) 
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];//初期値(年組)

        $objForm->ae( array("type"       => "select",
                            "name"       => "GRADE_HR_CLASS",
                            "size"       => "1",
                            "value"      => $model->field["GRADE_HR_CLASS"],
        					"extrahtml"  => "onchange=\"return btn_submit('knja310');\"",
                            "options"    => $row1));
        $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");

        //生徒一覧リスト
        $opt1 = array();
        $db = Query::dbCheckOut();
        $query = knja310Query::getSchno($model ,CTRL_YEAR ,CTRL_SEMESTER);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt1[]= array('label' => $row["NAME"],
                           'value' => $row["SCHREGNO"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        //生徒一覧リスト
        $objForm->ae( array("type"       => "select",
                            "name"       => "category_name",
        					"extrahtml"  => "multiple style=\"width:250px\" ondblclick=\"move1('left')\"",
                            "size"       => "20",
                            "options"    => $opt1 ));

        $arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");
        //出力対象一覧リスト
        $objForm->ae( array("type"       => "select",
                            "name"       => "category_selected",
        					"extrahtml"  => "multiple style=\"width:250px\" ondblclick=\"move1('right')\"",
                            "size"       => "20",
                            "options"    => array() ));

        $arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");

        //対象取消ボタン（全部）
        $objForm->ae( array("type"  => "button",
                            "name"        => "btn_rights",
                            "value"       => ">>",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right');\"" ) );

        $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");

        //対象選択ボタン（全部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_lefts",
                            "value"       => "<<",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left');\"" ) );

        $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");

        //対象取消ボタン（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right1",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right');\"" ) );

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");

        //対象選択ボタン（一部）
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left1",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left');\"" ) );

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");


        //印刷ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //ボタン
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJA310" ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );


        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knja310Form1.html", $arg); 
    }
}
?>
