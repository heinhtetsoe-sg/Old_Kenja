<?php

require_once('for_php7.php');

/********************************************************************/
/* 個人別学習状況一覧                               山城 2005/06/27 */
/*                                                                  */
/* 変更履歴                                                         */
/* ･NO001：                                         name yyyy/mm/dd */
/********************************************************************/

class knjm490Form1
{
    function main(&$model){

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjm490Form1", "POST", "knjm490index.php", "", "knjm490Form1");

        //年度テキストボックスを作成する
        $arg["data"]["YEAR"] = CTRL_YEAR;

        //年度
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => CTRL_YEAR
                            ) );

        //学期テキストボックスを作成する
        $arg["data"]["GAKKI"] = CTRL_SEMESTER;

        //学期
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GAKKI",
                            "value"     => CTRL_SEMESTER
                            ) );

        //科目選択コンボボックスを作成する
        $opt_class = array();
        $classcnt  = 0;
        $db = Query::dbCheckOut();
        $query = knjm490Query::getSubclass($model);
        $result = $db->query($query);

        //ブランク設定
        $opt_class[$classcnt] = array('label' => '',
                                      'value' => '0');
        $classcnt++;

        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_class[$classcnt]= array('label' => $row["SUBCLASSNAME"],
                                         'value' => $row["SUBCLASSCD"].$row["CHAIRCD"]);
            $classcnt++;
        }
        $result->free();
        Query::dbCheckIn($db);

        if(!isset($model->field["SUBCLASS"])) {
            $model->field["SUBCLASS"] = $opt_class[0]["value"];
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "SUBCLASS",
                            "size"       => "1",
                            "value"      => $model->field["SUBCLASS"],
                            "extrahtml"  => " onChange=\"return btn_submit('knjm490');\"",
                            "options"    => $opt_class));

        $arg["data"]["SUBCLASS"] = $objForm->ge("SUBCLASS");
    
        //クラス選択コンボボックスを作成する
        $db = Query::dbCheckOut();
        $query = knjm490Query::getClass($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt1[]= array('label' => $row["HR_NAME"],
                           'value' => $row["VALUE"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        //対象者リストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "category_name",
                            "extrahtml"  => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right')\"",
                            "size"       => "20",
                            "options"    => array()));

        $arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");

        //生徒一覧リストを作成する
        $objForm->ae( array("type"       => "select",
                            "name"       => "category_selected",
                            "extrahtml"  => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left')\"",
                            "size"       => "20",
                            "options"    => isset($opt1)?$opt1:array()));

        $arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");

        //対象取り消しボタンを作成する(個別)
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right1",
                            "value"       => "　＞　",
                            "extrahtml"   => " onclick=\"move('right');\"" ) );

        $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");

        //対象取り消しボタンを作成する(全て)
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_right2",
                            "value"       => "　≫　",
                            "extrahtml"   => " onclick=\"move('rightall');\"" ) );

        $arg["button"]["btn_right2"] = $objForm->ge("btn_right2");

        //対象選択ボタンを作成する(個別)
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left1",
                            "value"       => "　＜　",
                            "extrahtml"   => " onclick=\"move('left');\"" ) );

        $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

        //対象選択ボタンを作成する(全て)
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_left2",
                            "value"       => "　≪　",
                            "extrahtml"   => " onclick=\"move('leftall');\"" ) );

        $arg["button"]["btn_left2"] = $objForm->ge("btn_left2");

        //印刷対象ラジオボタンを作成する
        $opt1[0]=1;
        $opt1[1]=2;

        if (!$model->field["OUTPUT"]) $model->field["OUTPUT"] = 1;

        $objForm->ae( array("type"       => "radio",
                            "name"       => "OUTPUT",
                            "value"      => $model->field["OUTPUT"],
                            "multiple"   => $opt1));

        $arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
        $arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);

        //印刷対象ラジオボタンを作成する
        $opt2[0]=1;
        $opt2[1]=2;
        $opt2[2]=3;

        if (!$model->field["OUTPUT2"]) $model->field["OUTPUT2"] = 1;

        $objForm->ae( array("type"       => "radio",
                            "name"       => "OUTPUT2",
                            "value"      => $model->field["OUTPUT2"],
                            "multiple"   => $opt2));

        $arg["data"]["OUTPUT3"] = $objForm->ge("OUTPUT2",1);
        $arg["data"]["OUTPUT4"] = $objForm->ge("OUTPUT2",2);
        $arg["data"]["OUTPUT5"] = $objForm->ge("OUTPUT2",3);

        //印刷ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_print",
                            "value"       => "プレビュー／印刷",
                            "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

        $arg["button"]["btn_print"] = $objForm->ge("btn_print");

        //終了ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "DBNAME",
                            "value"      => DB_DATABASE
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "PRGID",
                            "value"     => "KNJM490"
                            ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );
        knjCreateHidden($objForm, "useCurriculumcd", $model->Properties["useCurriculumcd"]);

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjm490Form1.html", $arg); 
    }
}
?>
