<?php

require_once('for_php7.php');

class knjz232Form2
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("sel", "POST", "knjz232index.php", "", "sel");

        $opt_left_id = $opt_left = $opt_right = array();
        $db             = Query::dbCheckOut();

        //リスト選択データ取得
        $ChosenData = array();
        $ChosenData = $db->getRow(knjz232Query::getChosenData($model->subclasscd, $model),DB_FETCHMODE_ASSOC);

        $arg["info"]    = array("TOP"        => "対象科目 : ".$ChosenData["LABEL"],
                                "LEFT_LIST"  => "読替え科目一覧",
                                "RIGHT_LIST" => "科目一覧" );

        //学年取得
        if ($model->grade == "") {
	        $opt2 = array();
	        $result = $db->query(knjz232Query::GetGrade(CTRL_YEAR));
	        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
	        {
	            $opt2[] = array("label" => $row["GRADE"]."学年", 
	                            "value" => $row["GRADE"]);
	        }
	        if ($model->grade == "") $model->grade = $opt2[0]["value"];
        }

        //教科取得
        $opt = array();
        $result = $db->query(knjz232Query::GetClass(CTRL_YEAR, $model));
            //コンボボックスの一番上は空で全教科表示とする
            $opt[] = array("label" => "", 
                           "value" => "00");
                           
        //教育課程対応
        if ($model->Properties["useCurriculumcd"] == '1') {
	        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
	        {
	            $opt[] = array("label" => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"]."：".$row["CLASSNAME"],
	                           "value" => $row["CLASSCD"].'-'.$row["SCHOOL_KIND"]);
	        }
	        if($model->rightclasscd == "") $model->rightclasscd = substr($ChosenData["VALUE"],0,4);
        } else {
	        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
	        {
	            $opt[] = array("label" => $row["CLASSCD"]."：".$row["CLASSNAME"],
	                           "value" => $row["CLASSCD"]);
	        }
	        if($model->rightclasscd == "") $model->rightclasscd = substr($ChosenData["VALUE"],0,2);
        }

        //教科コンボボックス
        $objForm->ae( array("type"        => "select",
                            "name"        => "rightclasscd",
                            "size"        => "1",
                            "extrahtml"   => "onChange=\"btn_submit('list2')\"",
                            "value"       => $model->rightclasscd,
                            "options"     => $opt ));
        $arg["rightclasscd"] = $objForm->ge("rightclasscd");

        //科目取得
        if (isset($model->subclasscd)) {
            $model->CNT = 0;
            
            //読替え科目一覧取得
            $result      = $db->query(knjz232Query::selectQuery($model->subclasscd,$model->rightclasscd, $model->grade, CTRL_YEAR, $model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt_left[]    = array("label" => $row["LABEL"], 
                                       "value" => $row["VALUE"]);
                $opt_left_id[] = $row["VALUE"];

                $model->CNT++;
            }
            //変更前の読替えコードを保持
            $model->org_data = $opt_left_id;

            //科目一覧
            $result = $db->query(knjz232Query::selectClassQuery(CTRL_YEAR, $model->rightclasscd, $model->subclasscd, $model->grade, $model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt_right[] = array("label" => $row["LABEL"], 
                                     "value" => $row["VALUE"]);
            }

            $result->free();
        }
        Query::dbCheckIn($db);


        //読替え科目一覧
        $objForm->ae( array("type"        => "select",
                            "name"        => "classyear",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('left')\"",
                            "options"     => $opt_left));

        //科目一覧
        $objForm->ae( array("type"        => "select",
                            "name"        => "classmaster",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move1('right')\"",
                            "options"     => $opt_right));

        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "onclick=\"return move('sel_add_all');\"" ) );

        //追加ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return move('left');\"" ) );

        //削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "onclick=\"return move('right');\"" ) );

        //削除ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "onclick=\"return move('sel_del_all');\"" ) );

        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("classyear"),
                                   "RIGHT_PART"  => $objForm->ge("classmaster"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all") );

        //更新ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_keep",
                            "value"       => "更新",
                            "extrahtml"   => "onclick=\"return doSubmit();\"" ) );

        //取消ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_clear",
                            "value"       => "取消",
                            "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ) );

        //終了ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"] = array("BTN_OK"     =>$objForm->ge("btn_keep"),
                               "BTN_CLEAR"  =>$objForm->ge("btn_clear"),
                               "BTN_END"    =>$objForm->ge("btn_end"));

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata" ) );

        //評価区分
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "grading_flg",
                            "value"        => $grading_flg ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "record_dat_flg",
                            "value"        => "0" ) );

        $arg["finish"]  = $objForm->get_finish();

        if ($model->record_dat_flg == "1"){
            $arg["show_confirm"] = "Show_Confirm();";
        }

        if (isset($model->message)) { //更新できたら左のリストを再読込
            $arg["reload"] = "window.open('knjz232index.php?cmd=list&init=1', 'left_frame')";
        }

        View::toHTML($model, "knjz232Form2.html", $arg); 
    }
}
?>
