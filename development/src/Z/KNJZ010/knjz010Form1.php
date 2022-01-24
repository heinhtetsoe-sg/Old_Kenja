<?php

require_once('for_php7.php');

class knjz010form1
{
    function main(&$model)
    {
        //権限チェック          
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz010index.php", "", "edit");


        $arg["C_YEAR"] = CTRL_YEAR;
        $arg["C_SEMESTER"] = CTRL_SEMESTER;

        //初期表示時の年度/学期
        if ($model->field["YEAR"]=="" || $model->field["SEMESTER"]=="") {
                $Row = knjz010Query::getRow();
                $model->field["YEAR"] = $Row["CTRL_YEAR"];
                $model->field["SEMESTER"] = $Row["CTRL_SEMESTER"];
        }

        $db     = Query::dbCheckOut();
        
        $result = $db->query(knjz010Query::getYear());

        //年度コンボボックスの中身を作成
        $year = array();
        while($rowYear = $result->fetchRow(DB_FETCHMODE_ASSOC)){
              $year[] = array("label" => htmlspecialchars($rowYear["YEAR"]),"value" => $rowYear["YEAR"]);
        }
        
        $result = $db->query(knjz010Query::getSemester($model->field["YEAR"]));

        //学期コンボボックスの中身を作成
        $semester = array();
        while($rowSemester = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        
              $semester[] = array("label" => htmlspecialchars($rowSemester["SEMESTERNAME"]),"value" => $rowSemester["SEMESTER"]);
        }
        
//              $result->free();
        Query::dbCheckIn($db);
        
        //警告メッセージを表示しない場合
        if (!isset($model->warning)){
            $Row = knjz010Query::getRow();
        }else{
            $Row =& $model->field;
        }
        //年度
        $objForm->ae( array("type"   => "select",
                        "name"       => "YEAR",
                        "size"       => "1",
                        "value"      => $model->field["YEAR"],
                        "extrahtml"   => "onchange=\"return btn_submit('change');\"",
                        "options"    => $year));
            
    $arg["data"]["YEAR"] = $objForm->ge("YEAR");
        
        //学期
        $objForm->ae( array("type"       => "select",
                        "name"       => "SEMESTER",
                        "size"       => "1",
                        "value"      => $model->field["SEMESTER"],
                        "options"    => $semester));
            
    $arg["data"]["SEMESTER"] = $objForm->ge("SEMESTER");

        //年月日
$ctrl_date = strtr($Row["CTRL_DATE"],"-","/");

    $arg["data"]["CTRL_DATE"] = View::popUpCalendar($objForm, "CTRL_DATE", $ctrl_date);
        
        //出欠制御日付
$attend_ctrl_date = strtr($Row["ATTEND_CTRL_DATE"],"-","/");

    $arg["data"]["ATTEND_CTRL_DATE"] = View::popUpCalendar($objForm, "ATTEND_CTRL_DATE", $attend_ctrl_date);
        
        //出欠期間
    $objForm->ae( array("type"        => "text",
                        "name"        => "ATTEND_TERM",
                        "size"        => 3,
                        "maxlength"   => 3,
                        "value"       => $Row["ATTEND_TERM"],
                        "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\"; style='text-align: right;'" ) );

    $arg["data"]["ATTEND_TERM"] = $objForm->ge("ATTEND_TERM");
        
        //パスワードの有効期間
    $objForm->ae( array("type"        => "text",
                        "name"        => "PWDVALIDTERM",
                        "size"        => 2,
                        "maxlength"   => 2,
                        "value"       => $Row["PWDVALIDTERM"],
                        "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\";" ) );

    $arg["data"]["PWDVALIDTERM"] = $objForm->ge("PWDVALIDTERM");

        
    //パス
    $objForm->ae( array("type"        => "text",
                        "name"        => "IMAGEPATH",
                        "size"        => 60,
                        "maxlength"   => 60,
                        "value"       => $Row["IMAGEPATH"] ));

    $arg["data"]["IMAGEPATH"] = $objForm->ge("IMAGEPATH");

    //拡張子
    $objForm->ae( array("type"        => "text",
                        "name"        => "EXTENSION",
                        "size"        => 4,
                        "maxlength"   => 4,
                        "value"       => $Row["EXTENSION"] ));

    $arg["data"]["EXTENSION"] = $objForm->ge("EXTENSION");

    //メッセージ
    $objForm->ae( array("type"        => "textarea",
                            "name"        => "MESSAGE",
                            "cols"        => 76,
                            "rows"        => 25,
                            "extrahtml"   => "",
                            "wrap"        => "hard",
                            "value"       => $Row["MESSAGE"] ));

        $arg["data"]["MESSAGE"] = $objForm->ge("MESSAGE");

        //修正ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );

        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //クリアボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return Btn_reset('clear');\"" ) );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //戻るボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );
            
        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED",
                            "value"     => $Row["UPDATED"]
                            ) );

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz010Form1.html", $arg); 
    }
}               
?>
