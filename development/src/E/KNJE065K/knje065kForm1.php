<?php

require_once('for_php7.php');

class knje065kForm1
{
    function main(&$model)
    {
       //権限チェック
       if (AUTHORITY != DEF_UPDATABLE){
           $arg["jscript"] = "OnAuthError();";
       }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knje065kindex.php", "", "edit");

        //コントロールマスタの学籍処理年度で学年進行データを検索。
        $db     = Query::dbCheckOut();
        $result = $db->query(knje065kQuery::getGradeQuery());

#        //2005.10.17 minei
#        //在籍基準日
#        $arg["base_date"] = View::popUpCalendar($objForm, "BASE_DATE", str_replace("-","/",CTRL_DATE),"");
#        //2005.10.17 minei

        //2005/10/19 arakaki
        $arg["base_date"] = View::popUpCalendar($objForm, "base_date", $model->base_date);
        //2005/10/19 arakaki



        //処理年度と処理学期
        $arg["TOP"] = array("TRANSACTION"   => CTRL_YEAR,
                            "SEMESTER"      => $model->control_data["学期名"][CTRL_SEMESTER]);

        //処理学年
        $opt = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            $grade = ltrim($row["GRADE"],"0");

            $opt[] = array("label" => $grade."学年",
                           "value" => $row["GRADE"]);
        }
        if($model->gc_select == ""){
            $model->gc_select = $opt[0]["value"];
        }
        $objForm->ae( array("type"          => "select",
                            "name"          => "gc_select",
                            "size"          => "1",
                            "value"         => $model->gc_select,
                            "extrahtml"     => "onChange=\"return btn_submit('main');\"",
                            "options"       => $opt));
        $arg["gc_select"] = $objForm->ge("gc_select");
        $result->free();

        if($model->cmd == "recalc" && $model->radio == 1){
            $result = $db->query(knje065kQuery::getRecalculateQuery($model));
        }else if($model->cmd == "recalc" && $model->radio == 2){
            $result = $db->query(knje065kQuery::getRecalculateGroupQuery($model));
        }else if ($model->radio == 1){
            $result = $db->query(knje065kQuery::ReadQuery($model));
        }else if ($model->radio == 2){
            $result = $db->query(knje065kQuery::ReadGroupQuery($model));
        }
        unset($model->fields["CODE"]);

        if($model->cmd == "recalc"){
            //学年人数合計
            $total = $db->getOne(knje065kQuery::getAllCntQuery($model));
        }
        $count=0;
        while($Row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $model->fields["CODE"][] = $Row["COURSECD"].",".$Row["MAJORCD"].",".$Row["COURSECODE"];
            //コース人数合計
            $Row["COURSE_MEMBER"] = $Row["A_MEMBER"]+$Row["B_MEMBER"]+$Row["C_MEMBER"]+$Row["D_MEMBER"]+$Row["E_MEMBER"];

            //text
            $a = ($Row["A_MEMBER"] == "")? "0" : $Row["A_MEMBER"] ;
            $objForm->ae( array("type"        => "text",
                                "name"        => "A_MEMBER",
                                "size"        => "4",
                                "maxlength"   => "3",
                                "multiple"    => "1",
                                "extrahtml"   => "style=\"text-align:right\" onblur=\"sumNum(this)\" ",
                                "value"       => $a ));
            $Row["A_MEMBER"] = $objForm->ge("A_MEMBER");

            $b = ($Row["B_MEMBER"] == "")? "0" : $Row["B_MEMBER"] ;
            $objForm->ae( array("type"        => "text",
                                "name"        => "B_MEMBER",
                                "size"        => "4",
                                "maxlength"   => "3",
                                "multiple"    => "1",
                                "extrahtml"   => "style=\"text-align:right\" onblur=\"sumNum(this)\" ",
                                "value"       => $b ));
            $Row["B_MEMBER"] = $objForm->ge("B_MEMBER");

            $c = ($Row["C_MEMBER"] == "")? "0" : $Row["C_MEMBER"] ;
            $objForm->ae( array("type"        => "text",
                                "name"        => "C_MEMBER",
                                "size"        => "4",
                                "maxlength"   => "3",
                                "multiple"    => "1",
                                "extrahtml"   => "style=\"text-align:right\" onblur=\"sumNum(this)\" ",
                                "value"       => $c ));
            $Row["C_MEMBER"] = $objForm->ge("C_MEMBER");

            $d = ($Row["D_MEMBER"] == "")? "0" : $Row["D_MEMBER"] ;
            $objForm->ae( array("type"        => "text",
                                "name"        => "D_MEMBER",
                                "size"        => "4",
                                "maxlength"   => "3",
                                "multiple"    => "1",
                                "extrahtml"   => "style=\"text-align:right\" onblur=\"sumNum(this)\" ",
                                "value"       => $d ));
            $Row["D_MEMBER"] = $objForm->ge("D_MEMBER");

            $e = ($Row["E_MEMBER"] == "")? "0" : $Row["E_MEMBER"] ;
            $objForm->ae( array("type"        => "text",
                                "name"        => "E_MEMBER",
                                "size"        => "4",
                                "maxlength"   => "3",
                                "multiple"    => "1",
                                "extrahtml"   => "style=\"text-align:right\" onblur=\"sumNum(this)\" ",
                                "value"       => $e ));
            $Row["E_MEMBER"] = $objForm->ge("E_MEMBER");

            if($model->cmd == "recalc"){
                $objForm->ae( array("type"        => "text",
                                    "name"        => "GRADE_MEMBER",
                                    "size"        => 4,
                                    "maxlength"   => 4,
                                    "multiple"    => "1",
                                    "extrahtml"   => "style=\"text-align:right\" onblur=\"getZero(this)\"",
                                    "value"       => $total ));
                $Row["GRADE_MEMBER"] = $objForm->ge("GRADE_MEMBER");
            }else{
                $gm = ($Row["GRADE_MEMBER"] == "")? "0" : $Row["GRADE_MEMBER"] ;
                $objForm->ae( array("type"        => "text",
                                    "name"        => "GRADE_MEMBER",
                                    "size"        => 4,
                                    "maxlength"   => 4,
                                    "multiple"    => "1",
                                    "extrahtml"   => "style=\"text-align:right\" onblur=\"getZero(this)\"",
                                    "value"       => $gm ));
                $Row["GRADE_MEMBER"] = $objForm->ge("GRADE_MEMBER");
            }
            $Row["INDEX"] = $count;
            $arg["data"][] = $Row;
            $count++;
        }
        
        $objForm->ae( array("type"       => "radio",
                        "name"       => "RADIO",
                        "value"      => isset($model->radio)?$model->radio:1,
                        "extrahtml"  => "onclick=\"btn_submit('main');\""));

        $arg["RADIO1"] = $objForm->ge("RADIO",1) ."コースコード";
        $arg["RADIO2"] = $objForm->ge("RADIO",2) ."コースグループ";
        $arg["RADIO:".$model->radio] = $model->radio;
        //ボタン
        $objForm->ae( array("type"          => "button",
                            "name"          => "btn_recalc",
                            "value"         => "再計算",
                            "extrahtml"     => "onclick=\"return btn_submit('recalc');\"" ) );
        $arg["btn_recalc"] = $objForm->ge("btn_recalc");

        $objForm->ae( array("type"          => "button",
                            "name"          => "btn_update",
                            "value"         => "更 新",
                            "extrahtml"     => "onclick=\"return btn_submit('update');\"" ) );
        $arg["btn_update"] = $objForm->ge("btn_update");

        $objForm->ae( array("type"          => "reset",
                            "name"          => "btn_can",
                            "value"         => "取 消",
                            "extrahtml"     => "onclick=\"return btn_submit('reset');\"" ) );
        $arg["btn_can"] = $objForm->ge("btn_can");

        $objForm->ae( array("type"          => "button",
                            "name"          => "btn_end",
                            "value"         => "終 了",
                            "extrahtml"     => " onclick=\"return closeWin();\"" ) );
        $arg["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"          => "hidden",
                            "name"          => "cmd") );

        $objForm->ae( array("type"          => "hidden",
                            "name"          => "UPDATED[]",
                            "value"         => $model->updated) );

        knjCreateHidden($objForm, "useFrameLock", $model->Properties["useFrameLock"]);

        //成績学年データ、学籍在籍データ、評定マスタが作成済みかチェック
        $getRecSchregno = $db->getOne(knje065kQuery::getRecSchregno());
        $getSchSchregno = $db->getOne(knje065kQuery::getSchSchregno());
        $getAssesscd    = $db->getOne(knje065kQuery::getAssesscd());

            if($getRecSchregno < 1){
                $arg["Closing"] = " closing_window('Rec'); " ;
            }
            elseif($getSchSchregno < 1){
                $arg["Closing"] = " closing_window('Sch'); " ;
            }
            elseif($getAssesscd < 1){
                $arg["Closing"] = " closing_window(); " ;
            }

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knje065kForm1.html", $arg); 
    }
}
?>
