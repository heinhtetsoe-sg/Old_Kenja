<?php

require_once('for_php7.php');

class knja090Form1
{
    function main(&$model)
    {
        $objForm        = new form;
        $arg["start"]   = $objForm->get_start("list", "POST", "knja090index.php", "", "list");
        $db             = Query::dbCheckOut();
        
        $query  = knja090Query::getNameMstZ010();
        $rtnRow = array();
        $rtnRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $model->schoolName           = $rtnRow["NAME1"];     // 学校区分

        //ＨＲ割振り未処理生徒チェック
        $HRclass_flg = $db->getOne(knja090Query::IsClass_NULL($model, $model->control["new_year"], $model->control["new_semes"]));

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        } else if ($HRclass_flg > "0"){
             $arg["error"] = "OnErrorStat(2);";
        }    

		//NO001
        $model->sorttype = $db->getOne(knja090Query::GetSort());

        //最終学期のとき
        if (CTRL_SEMESTER == $model->control["学期数"]) {
            $model->control["new_year"] = ((int)CTRL_YEAR+1);
            $model->control["new_semes"] = 1;
        } else {
            $model->control["new_year"] = CTRL_YEAR;
            $model->control["new_semes"] = ((int)CTRL_SEMESTER+1);
        }
        //学期名
        $arg["term"] = $model->control["new_year"]."年度　".$model->control["学期名"][$model->control["new_semes"]];

        //[3]対象クラスコンボ
        $grade_class_arr = array();
        $result = $db->query(knja090Query::GetClass($model, $model->control["new_year"], $model->control["new_semes"]));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $grade_class_arr[] = array("label" => $row["GRADE"].$row["HR_CLASS"]."　".$row["HR_NAME"], 
                                       "value" => $row["GRADE"]."-".$row["HR_CLASS"] );
        }

        if ($model->grade_class=="")
            $model->grade_class = $grade_class_arr[0]["value"];
        //[4]担任名
        $arg["teacher"] = $db->getOne(knja090Query::GetTeacher($model->grade_class,
                                                               $model->control["new_year"],
                                                               $model->control["new_semes"]));
        $objForm->ae( array("type"      => "select",
                            "name"      => "grade_class",
                            "size"      => "1",
                            "extrahtml" => "tabindex=\"1\" OnChange=\"return btn_submit('list')\"",
                            "value"     => $model->grade_class,
                            "options"   => $grade_class_arr));

        $arg["grade_class"] = $objForm->ge("grade_class");

        //ソート
        $mark = array("▼","▲");

        switch ($model->s_id) {
            case "1":
                $mark1 = $mark[$model->sort[$model->s_id]];break;
            case "2":
                $mark2 = $mark[$model->sort[$model->s_id]];break;
            case "3":
                $mark3 = $mark[$model->sort[$model->s_id]];break;
            case "4":
                $mark4 = $mark[$model->sort[$model->s_id]];break;
        }

        $arg["sort1"] = View::alink("knja090index.php", "性別＋氏名かな".$mark1, "target=_self tabindex=\"-1\"", 
                                array("cmd"   => "list",
                                      "sort1" => ($model->sort["1"] == "1")?"0":"1",
                                      "s_id"  => "1") );

        $arg["sort2"] = View::alink("knja090index.php", "氏名かな＋性別".$mark2, "target=_self tabindex=\"-1\"", 
                                array("cmd"   => "list",
                                      "sort2" => ($model->sort["2"] == "1")?"0":"1",
                                      "s_id"  => "2") );

        $arg["sort3"] = View::alink("knja090index.php", "出席番号順".$mark3, "target=_self tabindex=\"-1\"", 
                                array("cmd"   => "list",
                                      "sort3" => ($model->sort["3"] == "1")?"0":"1",
                                      "s_id"  => "3") );

        $arg["sort4"] = View::alink("knja090index.php", "氏名かな＋学籍番号順".$mark4, "target=_self tabindex=\"-1\"", 
                                array("cmd"   => "list",
                                      "sort4" => ($model->sort["4"] == "1")?"0":"1",
                                      "s_id"  => "4") );

        //生徒一覧
        $i  = 0; //タブインデックス用
        $ii = 2; 
        $attendno = $model->attendno;
        if ($grade_class_arr[0] != "") {
            $result = $db->query(knja090Query::GetStudent($model));

            $model->schregno = array();
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $model->schregno[] = $row["SCHREGNO"];

                if($model->cmd == "copy"){
                    if($attendno[$i]){
                        $objForm->ae( array("type"        => "text",
                                            "name"        => "ATTENDNO",
                                            "size"        => 4,
                                            "maxlength"   => 3,
                                            "multiple"    => 1,
                                            "extrahtml"   => "tabindex=\"$ii\" style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"",
                                            "value"       => $attendno[$i] ));
                    } else {
                        $objForm->ae( array("type"        => "text",
                                            "name"        => "ATTENDNO",
                                            "size"        => 4,
                                            "maxlength"   => 3,
                                            "multiple"    => 1,
                                            "extrahtml"   => "tabindex=\"$ii\" style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"",
                                            "value"       => $row["OLD_ATTENDNO"] ));
                    }
                } else {
                    $objForm->ae( array("type"        => "text",
                                        "name"        => "ATTENDNO",
                                        "size"        => 4,
                                        "maxlength"   => 3,
                                        "multiple"    => 1,
                                        "extrahtml"   => "tabindex=\"$ii\" style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"",
                                        "value"       => $row["ATTENDNO"] ));
                }

                $row["ATTENDNO"]    = $objForm->ge("ATTENDNO");

                $row["SCHREGNO"] = View::alink("knja090index.php", $row["SCHREGNO"], "target=_self tabindex=\"-1\"",
                                                array("schregno"   => $row["SCHREGNO"],
                                                      "COURSEMAJORCD1"    => $row["COURSEMAJORCD"],
                                                      "course1"    => $row["COURSECODE"],
                                                      "cmd"        => "subform1"));
                  
                $row["backcolor"] = ($i%2 == 0) ? "#ffffff" : "#ccffcc";
                $arg["data"][] = $row; 
                $i++;
                $ii++;
            }
        }
        Query::dbCheckIn($db);

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_delete",
                            "value"       => "出席番号クリア",
                            "extrahtml"   => "tabindex=\"$ii\" onclick=\"return ClearAttendno();\"") ); 
    
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_master",
                            "value"       => "出席番号自動作成",
                            "extrahtml"   => "tabindex=\"$ii+1\" onclick=\"return MakeOrder();\"") ); 

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_copy",
                            "value"       => "旧学期の出席番号をコピー",
                            "extrahtml"   => "tabindex=\"$ii+2\" onclick=\"return btn_submit('copy');\"") ); 

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_keep",
                            "value"       => "更新",
                            "extrahtml"   => "tabindex=\"$ii+3\" onclick=\"return btn_submit('update');\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_clear",
                            "value"       => "取消",
                            "extrahtml"   => "tabindex=\"$ii+4\" onclick=\"return btn_submit('clear');\"" ) );

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終了",
                            "extrahtml"   => "tabindex=\"$ii+5\" onclick=\"closeWin();\"" ) );


        $arg["button"] = array("BTN_DELETE"     => $objForm->ge("btn_delete"),
                               "BTN_ATTEND"     => $objForm->ge("btn_master"),
                               "BTN_COPY"       => $objForm->ge("btn_copy"),
                               "BTN_OK"         => $objForm->ge("btn_keep"),
                               "BTN_CLEAR"      => $objForm->ge("btn_clear"),
                               "BTN_END"        => $objForm->ge("btn_end"));  
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );
    
        $arg["finish"] = $objForm->get_finish();
        
        View::toHTML($model, "knja090Form1.html", $arg); 
    }
}
?>
