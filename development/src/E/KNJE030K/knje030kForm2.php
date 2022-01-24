<?php

require_once('for_php7.php');

class knje030kForm2
{
    function main(&$model)
    {

        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knje030kindex.php", "", "edit");
        $db = Query::dbCheckOut();

        $temp_cd = "";
        $Row = array();
        $Row["SCHREGNO"]            = "";
        $Row["YEAR"]                = "";
        $Row["SUBCLASSCD"]          = "";
        $Row["SUBCLASSNAME"]        = "";
        $Row["SUBCLASSABBV"]        = "";
        $Row["SUBCLASSNAME_ENG"]    = "";
        $Row["SUBCLASSABBV_ENG"]    = "";
        $subclasscd="";
        //警告メッセージを表示しない場合
        if(isset($model->knje030kcd) && !isset($model->warning)){
            $subclasscd = ($model->subclasscd)?"$model->subclasscd":"$model->knje030kcd";
            //転入生成績データ
            $query = knje030kQuery::getRow($subclasscd,$model->knje030kyear,$model->knje030kschreg);
            $result = $db->query($query);
            while($Mid = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $Row["SCHREGNO"]          = $Mid["SCHREGNO"];
                $Row["YEAR"]              = $Mid["YEAR"];
                $Row["SUBCLASSCD"]        = $Mid["SUBCLASSCD"];
                $Row["SUBCLASSNAME"]      = $Mid["SUBCLASSNAME"];
                $Row["SUBCLASSABBV"]      = $Mid["SUBCLASSABBV"];
                $Row["SUBCLASSNAME_ENG"]      = $Mid["SUBCLASSNAME_ENG"];
                $Row["SUBCLASSABBV_ENG"]      = $Mid["SUBCLASSABBV_ENG"];
                $Row[$Mid["ANNUAL"]]["ANNUAL"]      = $Mid["ANNUAL"];
                $Row[$Mid["ANNUAL"]]["VALUATION"]   = $Mid["VALUATION"];
                $Row[$Mid["ANNUAL"]]["GET_CREDIT"]  = $Mid["GET_CREDIT"];
                $Row[$Mid["ANNUAL"]]["ADD_CREDIT"]  = $Mid["ADD_CREDIT"];
                $Row[$Mid["ANNUAL"]]["UPDATED"] = $Mid["UPDATED"];
                $temp_cd = $Mid["SUBCLASSCD"];
            }
        }else{
            $Row =& $model->field;
            //教育課程対応
            if ($model->Properties["useCurriculumcd"] == '1') {
                $Row["SUBCLASSCD"]=substr($model->field["CLASSCD"], 0, 2).$model->field["SUBCLASSCD"];
            } else {
                $Row["SUBCLASSCD"]=$model->field["CLASSCD"].$model->field["SUBCLASSCD"];
            }
        }

        $show = array();

        //教科取得
        $query = knje030kQuery::getClass($model);
        $result = $db->query($query);
        $opt_c = array();
        $i = 0;
        while($row_c = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_c[] = array("label" => htmlspecialchars($row_c["LABEL"]), 
            "value" => $row_c["VALUE"]);

            if ($model->class_select=="" && $i==0){

                $CLASS_CHK = $row_c["VALUE"];
                $show["CLASSNAME"]        = $row_c["CLASSNAME"];
                $show["CLASSABBV"]        = $row_c["CLASSABBV"];
                $show["CLASSNAME_ENG"] = $row_c["CLASSNAME_ENG"];
                $show["CLASSABBV_ENG"] = $row_c["CLASSABBV_ENG"];
                $i++;

            } else if ($model->class_select==$row_c["VALUE"]){

                $CLASS_CHK = $row_c["VALUE"];
                $show["CLASSNAME"]        = $row_c["CLASSNAME"];
                $show["CLASSABBV"]        = $row_c["CLASSABBV"];
                $show["CLASSNAME_ENG"] = $row_c["CLASSNAME_ENG"];
                $show["CLASSABBV_ENG"] = $row_c["CLASSABBV_ENG"];

            }

        }

        //教科
        $objForm->ae( array("type"       => "select",
                            "name"       => "CLASSCD",
                            "size"       => "1",
                            "value"      => $CLASS_CHK,
                            "extrahtml"  => "onchange=\"btn_submit('class');\"",
                            "options"    => $opt_c
                            ));

        $arg["data"]["CLASSCD"] = $objForm->ge("CLASSCD");

        //科目取得
        $query = knje030kQuery::getSublass($CLASS_CHK, $model);
        $result = $db->query($query);
        $opt_s = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt_s[] = array("label" => htmlspecialchars(substr($row["SUBCLASSCD"],2,6).":".$row["SUBCLASSNAME"]), 
                             "value" => $row["SUBCLASSCD"].":".$row["SUBCLASSABBV"].":".$row["SUBCLASSNAME_ENG"].":".$row["SUBCLASSABBV_ENG"]);

        }

        //科目
        $objForm->ae( array("type"       => "select",
                            "name"       => "SUBCLASS",
                            "size"       => "1",
                            "value"      => "",
                            "options"    => $opt_s
                            ));

        $arg["data"]["SUBCLASS"] = $objForm->ge("SUBCLASS");

        $result->free();

        $arg["data"]["CLASSABBV"]     = $show["CLASSABBV"];
        $arg["data"]["CLASSNAME_ENG"] = $show["CLASSNAME_ENG"];
        $arg["data"]["CLASSABBV_ENG"] = $show["CLASSABBV_ENG"];

        //教科名
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "CLASSNAME",
                            "value"     => $show["CLASSNAME"]
                            ));
        //教科名略称
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "CLASSABBV",
                            "value"     => $show["CLASSABBV"]
                            ));
        //教科名英字
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "CLASSNAME_ENG",
                            "value"     => $show["CLASSNAME_ENG"]
                            ));
        //教科名略称英字
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "CLASSABBV_ENG",
                            "value"     => $show["CLASSABBV_ENG"]
                            ));

        if($model->knje030kschreg==""){
            $model->knje030kschreg = $Row["SCHREGNO"];
        }

        //学籍番号
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SCHREGNO",
                            "value"     => $model->knje030kschreg
                            ));

        //年度
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "YEAR",
                            "value"     => $Row["YEAR"]
                            ));
        //教育課程
        $query = knje030kQuery::getCurriculum();
        $extra = "onChange=\"return btn_submit('class');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["CURRICULUM_CD"], "CURRICULUM_CD", $extra, 1, "BLANK");

        //科目コード
        $objForm->ae( array("type"        => "text",
                            "name"        => "SUBCLASSCD",
                            "size"        => 8,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"return check(this)\"",
                            "value"       => substr($Row["SUBCLASSCD"],2,6)
                            ));

        $arg["data"]["SUBCLASSCD"] = $objForm->ge("SUBCLASSCD"); 

        //科目名
        $objForm->ae( array("type"        => "text",
                            "name"        => "SUBCLASSNAME",
                            "size"        => 40,
                            "maxlength"   => 60,
                            "value"       => $Row["SUBCLASSNAME"]
                            ));

        $arg["data"]["SUBCLASSNAME"] = $objForm->ge("SUBCLASSNAME"); 

        //科目略称
        $objForm->ae( array("type"        => "text",
                            "name"        => "SUBCLASSABBV",
                            "size"        => 5,
                            "maxlength"   => 9,
                            "value"       => $Row["SUBCLASSABBV"]
                            ));

        $arg["data"]["SUBCLASSABBV"] = $objForm->ge("SUBCLASSABBV");

        //英字科目名
        $objForm->ae( array("type"        => "text",
                            "name"        => "SUBCLASSNAME_ENG",
                            "size"        => 40,
                            "maxlength"   => 40,
                            "extrahtml"   => "onblur=\"return moji_hantei(this)\" STYLE=\"ime-mode:disabled\"",
                            "value"       => $Row["SUBCLASSNAME_ENG"]
                            ));

        $arg["data"]["SUBCLASSNAME_ENG"] = $objForm->ge("SUBCLASSNAME_ENG"); 

        //英字略称
        $objForm->ae( array("type"        => "text",
                            "name"        => "SUBCLASSABBV_ENG",
                            "size"        => 20,
                            "maxlength"   => 20,
                            "extrahtml"   => "onblur=\"return moji_hantei(this)\" STYLE=\"ime-mode:disabled\"",
                            "value"       => $Row["SUBCLASSABBV_ENG"]
                            ));

        $arg["data"]["SUBCLASSABBV_ENG"] = $objForm->ge("SUBCLASSABBV_ENG");

        //学年数にあわせてrowspan値を変化
        $arg["span"]=$model->grade_range+1;

        $list_d = array();
        for($i=1;$i<=$model->grade_range;$i++){
            //学年
            //表示は２桁と１桁どちらがいいですか？
#            $grade = $i;
#            $Row_d["ANNUAL"] =  $grade;
            $grade = sprintf("%02d", $i);
            $Row_d["ANNUAL"] =  $i;

                if(!in_array($grade,array_keys($Row))){
                    $Row[$grade]["VALUATION"]   = "";
                    $Row[$grade]["GET_CREDIT"]  = "";
                    $Row[$grade]["ADD_CREDIT"]  = "";
                    $Row[$grade]["UPDATED"]     = "";
                }

            //評価
            $objForm->ae( array("type"        => "text",
                                "name"        => "VALUATION".$grade,
                                "size"        => 6,
                                "maxlength"   => 2,
                                "extrahtml"   => "onblur=\"return check(this)\" STYLE=\"text-align: right\"",
                                "value"       => $Row[$grade]["VALUATION"]
                                ));

            $Row_d["VALUATION"] = $objForm->ge("VALUATION".$grade); 

            //修得単位
            $objForm->ae( array("type"        => "text",
                                "name"        => "GET_CREDIT".$grade,
                                "size"        => 6,
                                "maxlength"   => 2,
                                "extrahtml"   => "onblur=\"return check(this)\" STYLE=\"text-align: right\"",
                                "value"       => $Row[$grade]["GET_CREDIT"]
                                ));

            $Row_d["GET_CREDIT"] = $objForm->ge("GET_CREDIT".$grade); 

            //増加単位
            $objForm->ae( array("type"        => "text",
                                "name"        => "ADD_CREDIT".$grade,
                                "size"        => 6,
                                "maxlength"   => 2,
                                "extrahtml"   => "onblur=\"return check(this)\" STYLE=\"text-align: right\"",
                                "value"       => $Row[$grade]["ADD_CREDIT"]
                                ));

            $Row_d["ADD_CREDIT"] = $objForm->ge("ADD_CREDIT".$grade); 

            //hiddenを作成する
            $objForm->ae( array("type"      => "hidden",
                                "name"      => "UPDATED".$grade,
                                "value"     => $Row[$grade]["UPDATED"]
                                ));

            $arg["list_d"][] = $Row_d;
        }

        //科目の読込ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_sub",
                            "value"       => "＜",
                            "extrahtml"   => "onclick=\"return add('');\""
                            ));
        $arg["button"]["btn_sub"] = $objForm->ge("btn_sub");

        //学籍番号
        if($model->knje030kschreg !=""){
            $arg["data"]["SCHREGNO"] = "(学籍番号：".$model->knje030kschreg.")";
        }

        //追加ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_add",
                            "value"       => "追 加",
                            "extrahtml"   => "onclick=\"return btn_submit('add');\""
                            ));
        $arg["button"]["btn_add"] = $objForm->ge("btn_add");

        //更新ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\""
                            ));
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\""
                            ));
        $arg["button"]["btn_del"] = $objForm->ge("btn_del");

        //クリアボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset');\""
                            ));
        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_back",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\""
                            ));
        $arg["button"]["btn_back"] = $objForm->ge("btn_back");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ));

        if($temp_cd == ""){
         $temp_cd = $model->field["temp_cd"];
        }
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "temp_cd",
                            "value"     => $temp_cd
                            ));

        //listデータをセット
        $Row_list = array();       

        //学期末データ
        $query = knje030kQuery::Record($model, $model->knje030kschreg);
        $result = $db->query($query);
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            $arg["list"][]=$row;

        }


        if ($model->Properties["useCurriculumcd"] == '1') {
            $arg["useCurriculumcd"] = "1";
        } else {
            $arg["NoCurriculumcd"] = "1";
        }        

        $cd_change = false;                                                                               

        if ($temp_cd==$Row["SUBCLASSCD"] ) $cd_change = true;

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        if ($model->cmd != "class") {
            if (VARS::get("cmd") != "edit" && ($cd_change==true || $model->isload != 1)){
                $arg["reload"]  = "window.open('knje030kindex.php?cmd=list&SCHREGNO=$model->knje030kschreg','left_frame');";
            }
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje030kForm2.html", $arg); 
    }
}
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>
