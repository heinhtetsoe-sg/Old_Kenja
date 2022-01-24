<?php

require_once('for_php7.php');

class knjz220fform1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjz220findex.php", "", "main");

        //リスト表示
        $db = Query::dbCheckOut();
        if(!isset($model->year)) $model->year = CTRL_YEAR;
        if(!isset($model->level) || $model->cmd != "level") $model->level = $db->getOne(knjz220fQuery::selectQuerycnt($model));
        if(!isset($model->avg) || $model->cmd != "level") $model->avg = $db->getOne(knjz220fQuery::selectQueryAvg($model));
        
        //SQL文発行
        $ar[] = array();
        
        //警告メッセージを表示しない場合
        if (!isset($model->warning)){
            $query = knjz220fQuery::selectQuery($model);
            $result = $db->query($query);
        }else{
            $row =& $model->field;
            $result = "";
        }

        for ($i=1; $i<=$model->level; $i++){
            if($result != ""){
                if($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    //レコードを連想配列のまま配列$arg[data]に追加していく。 
                    array_walk($row, "htmlspecialchars_array");
                    $ar =explode(".",$row["ASSESSLOW"]);
                    $row["ASSESSLOW"] = $ar[0];
                    $ar =explode(".",$row["ASSESSHIGH"]);
                    $row["ASSESSHIGH"] = $ar[0];
                    $up = $row["UPDATED"];
                }
            }
            $row["ASSESSLEVEL"] = $i;

            //textの有無設定(下限部分)
            $row["ASSESSLOWTEXT"]  = "<input type=\"text\" name=\"";
            $row["ASSESSLOWTEXT"] .= "ASSESSLOW".$row["ASSESSLEVEL"];
            $row["ASSESSLOWTEXT"] .= "\" value=\"";
            if($model->cmd !="level"){
                if($result != ""){
                    $row["ASSESSLOWTEXT"] .= $row["ASSESSLOW"];
                }else if ($result == ""){
                    $row["ASSESSLOWTEXT"] .= $row["ASSESSLOW".$i];
                }
            }
            $row["ASSESSLOWTEXT"] .= "\" size=\"";
            $row["ASSESSLOWTEXT"] .= "4";
            $row["ASSESSLOWTEXT"] .= "\" maxlength=\"";
            $row["ASSESSLOWTEXT"] .= "3";
            $row["ASSESSLOWTEXT"] .= "\" onblur=\"isNumb(this,".($row["ASSESSLEVEL"] -1).",'ELSE');\"";
            $row["ASSESSLOWTEXT"] .= " STYLE=\"text-align: right\"> ";
            $stock[] = $row["ASSESSLOW"];

            //上限部分作成
            if ($row["ASSESSLEVEL"] == $model->level){
                $row["ASSESSHIGHTEXT"]  = "<input type=\"text\" name=\"";
                $row["ASSESSHIGHTEXT"] .= "ASSESSHIGH".$row["ASSESSLEVEL"];
                $row["ASSESSHIGHTEXT"] .= "\" value=\"";
                if($model->cmd !="level"){
                    if($result != ""){
                        $row["ASSESSHIGHTEXT"] .= $row["ASSESSHIGH"];
                    }else if ($result == ""){
                        $row["ASSESSHIGHTEXT"] .= $row["ASSESSHIGH".$i];
                    }
                }
                $row["ASSESSHIGHTEXT"] .= "\" size=\"";
                $row["ASSESSHIGHTEXT"] .= "6";
                $row["ASSESSHIGHTEXT"] .= "\" maxlength=\"";
                $row["ASSESSHIGHTEXT"] .= "3";
                $row["ASSESSHIGHTEXT"] .= "\" onblur=\"this.value=toInteger(this.value);";
                $row["ASSESSHIGHTEXT"] .= "\" STYLE=\"text-align: right\"> ";
            }else{
                $row["ASSESSHIGHTEXT"]  = "<span id=\"strID";
                $row["ASSESSHIGHTEXT"] .= $row["ASSESSLEVEL"];
                $row["ASSESSHIGHTEXT"] .= "\">";
                if($result != ""){
                    if($model->cmd !="level"){
                        $row["ASSESSHIGHTEXT"] .= $row["ASSESSHIGH"];
                    }
                }else if ($result == ""){
                    $row["ASSESSHIGHTEXT"] .= ($row["ASSESSLOW".($i + 1)] - 1);
                }
                $row["ASSESSHIGHTEXT"] .= "</span>";
            }
            //記号部分作成
            $row["ASSESSMARKTEXT"]  = "<input type=\"text\" name=\"";
            $row["ASSESSMARKTEXT"] .= "ASSESSMARK".$row["ASSESSLEVEL"];
            $row["ASSESSMARKTEXT"] .= "\" value=\"";
            if($model->cmd !="level"){
                if($result != ""){
                    $row["ASSESSMARKTEXT"] .= $row["ASSESSMARK"];
                }else if ($result == ""){
                    $row["ASSESSMARKTEXT"] .= $row["ASSESSMARK".$i];
                }              
            }else{
                $row["ASSESSMARKTEXT"] .= $i;
            }
            $row["ASSESSMARKTEXT"] .= "\" size=\"";
            $row["ASSESSMARKTEXT"] .= "8";
            $row["ASSESSMARKTEXT"] .= "\" maxlength=\"";
            $row["ASSESSMARKTEXT"] .= "6";
            $row["ASSESSMARKTEXT"] .= "\" STYLE=\"text-align: right\"> ";
                    
            $arg["data"][] = $row;
        }

        //前年度データ件数
        $pre_year = $model->year - 1;
        $preYear_cnt = $db->getOne(knjz220fQuery::getCopyData($pre_year, "cnt"));
        knjCreateHidden($objForm, "PRE_YEAR_CNT", $preYear_cnt);
        //今年度データ件数
        $this_year = $model->year;
        $thisYear_cnt = $db->getOne(knjz220fQuery::getCopyData($this_year, "cnt"));
        knjCreateHidden($objForm, "THIS_YEAR_CNT", $thisYear_cnt);

        Query::dbCheckIn($db);
        
        $modify_flg   = ($model->level == 0) ? "disabled" : "";

        //年度
        $objForm->ae( array("type"      => "text",
                            "name"      => "YEAR",
                            "size"      => 6,
                            "maxlength" => 4,
                            "extrahtml" => "onblur=\"this.value=toInteger(this.value);　return btn_submit('change_year');\"",
                            "value"     =>  $model->year ));

        //評定段階数
        $objForm->ae( array("type"      => "text",
                            "name"      => "ASSESSLEVELCNT",
                            "size"      => 6,
                            "maxlength" => 3,
                            "extrahtml" => "onblur=\"this.value=toInteger(this.value);\"",
                            "value"     => $model->level ));

        //平均点
        $objForm->ae( array("type"      => "text",
                            "name"      => "AVG",
                            "size"      => 6,
                            "maxlength" => 2,
                            "extrahtml" => "onblur=\"this.value=toInteger(this.value);\"",
                            "value"     => $model->avg ));

        //確定ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_level",
                            "value"       => "確 定",
                            "extrahtml"   => "onclick=\"return level(".$model->level.");\" " ) );
        
        $arg["head"] = array(
                            "YEAR"           => $objForm->ge("YEAR"),
                            "ASSESSLEVELCNT" => $objForm->ge("ASSESSLEVELCNT"),
                            "ASSESSMEMO"     => $assessmark,
                            "btn_level"      => $objForm->ge("btn_level"),
                            "AVG"            => $objForm->ge("AVG")
                            );

        //コピーボタン
        $extra = "onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);

        //更新ボタン
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_udpate",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\" ".$modify_flg ) );

        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

        //削除ボタン
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_delete",
                            "value"       => "削 除",
                            "extrahtml"   => "onclick=\"return btn_submit('delete');\" ".$modify_flg ) );

        $arg["button"]["btn_delete"] = $objForm->ge("btn_delete");

        //取消ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('clear');\" ".$modify_flg ) );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"return closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED",
                            "value"     => $up  ) );


        $arg["finish"]  = $objForm->get_finish();

        if($model->sec_competence != DEF_UPDATABLE){
            $arg["Closing"] = " closing_window(); " ;
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz220fForm1.html", $arg);
    }       
}
?>
