<?php

require_once('for_php7.php');

    class knja100Form1
    {
        function main(&$model)
        {
            $objForm        = new form;
            $arg["start"]   = $objForm->get_start("main", "POST", "knja100index.php", "", "main");

            //処理年度
            $arg["data"]["YEAR"] = CTRL_YEAR;
            //処理学期
#            $arg["data"]["this_semester"] = CTRL_SEMESTER."学期";
            $arg["data"]["this_semester"] = CTRL_SEMESTERNAME;
            //設定学期
            $selected = CTRL_SEMESTER+1;

            $db = Query::dbCheckOut();

            //設定学期コンボ作成
            $opt       = array();
            $result    = $db->query(knja100Query::getSemester());   
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt[] = array("label" => $row["SEMESTERNAME"], 
                                "value" => $row["SEMESTER"]);
            }

            //対象学期
            $objForm->ae( array("type"        => "select",
                                "name"        => "semester",
                                "size"        => "1",
                                "extrahtml"   => "",
                                "value"       => $model->semester,
                                "options"     => $opt));

            $arg["data"]["SEMESTER"] = $objForm->ge("semester");


            //起動条件処理
            if ($model->cmd == "") {

                //CLASS_FORMATION_DATにHR_CLASS,ATTENDONO,COURSCODEが未設定のデータが1件でも存在すれば画面を閉じる
                $row = $db->getOne(knja100Query::getEx_Class_Formation($model));
                if ($row >='1'){
                    $arg["close"] = "close_window(3);";
                } 

                //更新権限チェック
                if($model->sec_competence != DEF_UPDATABLE){
                    $arg["close"] = "close_window(2);";
                }

                //最終学期チェック(最終学期のとき)
                if ( CTRL_SEMESTER == $model->max_semester ) {
                    $arg["close"] ="close_window(1);";
                }

            }

            Query::dbCheckIn($db);

            if ($model->semester == "") { 
                $arg["combo"]   = "document.forms[0].semester.selectedIndex = \"".($selected-1)."\"";
            }


            //実行ボタン
            $objForm->ae( array("type"        => "button",
                                "name"        => "btn_ok",
                                "value"       => "実  行",
                                "extrahtml"   => "onclick=\"return btn_submit('execute');\"" ));

            //終了ボタン
            $objForm->ae( array("type"        => "button",
                                "name"        => "btn_end",
                                "value"       => "終  了",
                                "extrahtml"   => "onclick=\"closeWin();\"" ));

            $arg["button"] = array("BTN_OK"   => $objForm->ge("btn_ok"),
                                   "BTN_END"  => $objForm->ge("btn_end") );

            //hidden
            $objForm->ae( array("type"      => "hidden",
                                "name"      => "cmd") );  

            //現在処理学期
            $objForm->ae( array("type"      => "hidden",
                                "name"      => "ctrl_semester",
                                "value"     => CTRL_SEMESTER ) );

            $arg["finish"]  = $objForm->get_finish();

            //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
            View::toHTML($model, "knja100Form1.html", $arg); 
        }
    }
?>
