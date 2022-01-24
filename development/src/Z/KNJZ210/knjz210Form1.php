<?php

require_once('for_php7.php');

class knjz210form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjz210index.php", "", "main");

        //リスト表示
        if(!isset($model->sepa)) $model->sepa = 1;
        $db = Query::dbCheckOut();

        $query = knjz210Query::getZ040();
        $result = $db->query($query);
        $opt = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array("label" => $row["LABEL"], "value" => $row["VALUE"]);
        }
        $result->free();

        //Z040がなければ固定
        if (get_count($opt) == 0) {
            $opt[] = array("label" => "1：中間用評定   ", "value" =>"1");
            $opt[] = array("label" => "2：学期末用評価 ", "value" =>"2");
            $opt[] = array("label" => "3：学年末用評定 ", "value" =>"3");
            $opt[] = array("label" => "4：調査書用概評 ", "value" =>"4");
        }

        $query = knjz210Query::selectHdat();
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if($row["ASSESSCD"]==$model->sepa){
                
                $assesslevelcnt = $row["ASSESSLEVELCNT"]; 
                $assessmark = $row["ASSESSMEMO"];
                $modify_flg = ($row["MODIFY_FLG"] == "1")? " disabled " : "" ;
            }
            if($row["ASSESSCD"]=="2"){
                $levelcnt2 = $row["ASSESSLEVELCNT"]; 
            }elseif($row["ASSESSCD"]=="3"){
                $levelcnt3 = $row["ASSESSLEVELCNT"];
            }
        }
        $result->free();

        if($model->cmd=="level" || ($model->cmd=="main" && $model->level !="")){
            $assesslevelcnt = $model->level;
        }
        $cnt = $assesslevelcnt;

        //SQL文発行
        $ar[] = array();
        
        //警告メッセージを表示しない場合
        if (isset($model->sepa) && !isset($model->warning)){
            $query = knjz210Query::selectQuery($model);
            $result = $db->query($query);
        }else{
            $row =& $model->field;
            $result = "";
        }

        for ($i=1; $i<=$cnt; $i++){
            if($result != ""){
                if($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    //レコードを連想配列のまま配列$arg[data]に追加していく。 
                    array_walk($row, "htmlspecialchars_array");
                    //評定区分1と2は小数点を取り除く。
                    if($model->sepa == 4){
                        $row["ASSESSLOW"] = $row["ASSESSLOW"];
                        $row["ASSESSHIGH"] = $row["ASSESSHIGH"];
                        $up = $row["UPDATED"];
                    }else{
                        $ar =explode(".",$row["ASSESSLOW"]);
                        $row["ASSESSLOW"] = $ar[0];
                        $ar =explode(".",$row["ASSESSHIGH"]);
                        $row["ASSESSHIGH"] = $ar[0];
                        $up = $row["UPDATED"];
                    }
                }
            }
            $row["ASSESSLEVEL"] = $i;

            //textの有無設定(下限部分)
            if ($row["ASSESSLEVEL"] == 1 && $model->sepa == "2"){
                $row["ASSESSLOWTEXT"] = "0";
            }else{
                $row["ASSESSLOWTEXT"]  = "<input type=\"text\" name=\"";
                $row["ASSESSLOWTEXT"] .= "ASSESSLOW".$row["ASSESSLEVEL"];
                $row["ASSESSLOWTEXT"] .= "\" value=\"";
                if($model->cmd !="level"){
                    if($result != ""){
                        $row["ASSESSLOWTEXT"] .= $row["ASSESSLOW"];
                    }else if ($result == ""){
                        $row["ASSESSLOWTEXT"] .= $row["ASSESSLOW$i"];
                    }
                }
                $row["ASSESSLOWTEXT"] .= "\" size=\"";
                $row["ASSESSLOWTEXT"] .= "4";
                $row["ASSESSLOWTEXT"] .= "\" maxlength=\"";
                if($model->sepa == 4){
                    $row["ASSESSLOWTEXT"] .= "4";
                }elseif($model->sepa == 1 || $model->sepa == 2){
                    $row["ASSESSLOWTEXT"] .= "3";
                }else{
                    $row["ASSESSLOWTEXT"] .= "2";
                }
                $row["ASSESSLOWTEXT"] .= "\" onblur=\"isNumb(this,".($row["ASSESSLEVEL"] -1).",'".(($model->sepa == 4)? "ABCD" : "ELSE" )."');\"";
                $row["ASSESSLOWTEXT"] .= " STYLE=\"text-align: right\"> ";
                $stock[] = $row["ASSESSLOW"];
            }

            //上限部分作成
            if ($row["ASSESSLEVEL"] == $cnt){

                switch($model->sepa){

                    case 4:     //4：調査書用概評
                        $row["ASSESSHIGHTEXT"] = $levelcnt3;
                        break;
                    case 3:     //3：学年末用評定

                        $semes_assesscd = $db->getOne("SELECT semes_assesscd FROM school_mst WHERE year='".CTRL_YEAR."'");
                        if($semes_assesscd == 0){         //学校マスタの期末評価処理が==0
                            $row["ASSESSHIGHTEXT"] = "100";
                        }else{
                            $row["ASSESSHIGHTEXT"] = $levelcnt2;
                        }

                        break;
                    case 2:     //2：学期末用評価 
                        $row["ASSESSHIGHTEXT"] = "100";
                        break;
                    case 1:     //1：中間用評定
                        $row["ASSESSHIGHTEXT"]  = "<input type=\"text\" name=\"";
                        $row["ASSESSHIGHTEXT"] .= "ASSESSHIGH".$row["ASSESSLEVEL"];
                        $row["ASSESSHIGHTEXT"] .= "\" value=\"";
                        if($model->cmd !="level"){
                            if($result != ""){
                                $row["ASSESSHIGHTEXT"] .= $row["ASSESSHIGH"];
                            }else if ($result == ""){
                                $row["ASSESSHIGHTEXT"] .= $row["ASSESSHIGH$i"];
                            }
                        }
                        $row["ASSESSHIGHTEXT"] .= "\" size=\"";
                        $row["ASSESSHIGHTEXT"] .= "6";
                        $row["ASSESSHIGHTEXT"] .= "\" maxlength=\"";
                        $row["ASSESSHIGHTEXT"] .= "3";
                        $row["ASSESSHIGHTEXT"] .= "\" onblur=\"this.value=toInteger(this.value);";
                        $row["ASSESSHIGHTEXT"] .= "\" STYLE=\"text-align: right\"> ";
                        break;
#                    default:
#                        $row["ASSESSHIGHTEXT"] = $levelcnt2;
                }

            }else{
                $row["ASSESSHIGHTEXT"]  = "<span id=\"strID";
                $row["ASSESSHIGHTEXT"] .= $row["ASSESSLEVEL"];
                $row["ASSESSHIGHTEXT"] .= "\">";
                if($result != ""){
                    if($model->cmd !="level"){
                            $row["ASSESSHIGHTEXT"] .= $row["ASSESSHIGH"];
                    }       
                //                      $high_stock[$i] = $row["ASSESSHIGH"];
                }else if ($result == ""){
                    $row["ASSESSHIGHTEXT"] .= ((int)$row["ASSESSLOW".($i + 1)] - (($model->sepa == 4)? 0.1 : 1));
                }
                $row["ASSESSHIGHTEXT"] .= "</span>";
            }
            //記号部分作成
            //                       if($model->sepa == 4){
            $row["ASSESSMARKTEXT"]  = "<input type=\"text\" name=\"";
            $row["ASSESSMARKTEXT"] .= "ASSESSMARK".$row["ASSESSLEVEL"];
            $row["ASSESSMARKTEXT"] .= "\" value=\"";
            if($model->cmd !="level"){
                if($result != ""){
                    $row["ASSESSMARKTEXT"] .= $row["ASSESSMARK"];
                }else if ($result == ""){
                    $row["ASSESSMARKTEXT"] .= $row["ASSESSMARK$i"];
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

        Query::dbCheckIn($db);
        
        if($model->level==""){
            $levelcntShow = $assesslevelcnt;
        }else{
            $levelcntShow = $model->level;
        }

        //評定区分コンボボックス
        $objForm->ae( array("type"      => "select",
                            "name"      => "sepa",
                            "size"      => "1",
                            "value"     => $model->sepa,
                            "extrahtml" => "onChange=\"return btn_submit('change');\"",
                            "options"   => $opt) );

        //段階数
        $objForm->ae( array("type"      => "text",
                            "name"      => "ASSESSLEVELCNT",
                            "size"      => 6,
                            "maxlength" => 3,
                            "extrahtml" => "onblur=\"this.value=toInteger(this.value);\"".$modify_flg,
                            "value"     => $assesslevelcnt ));

        //確定ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_level",
                            "value"       => "確 定",
                            "extrahtml"   => "onclick=\"return level(".$cnt.");\" ".$modify_flg ) );
        
        $arg["sepa"] = array( "ASSESSLEVELCNT" => $objForm->ge("ASSESSLEVELCNT"),
                              "ASSESSMEMO"     => $assessmark,
                              "btn_level"      => $objForm->ge("btn_level"),
                              "VAL"            => $objForm->ge("sepa"));

        if(trim($modify_flg) == "disabled"){
            $objForm->ae( array("type"      => "hidden",
                                "name"      => "MODIFY",
                                "value"     => $assesslevelcnt ) );
        }



        //デフォルトに戻すボタン
/*              $objForm->ae( array("type" => "button",
                            "name"        => "btn_def",
                            "value"       => "デフォルトに戻す",
                            "extrahtml"   => "onclick=\"return btn_submit('default');\"" ) );

        $arg["button"]["btn_def"] = $objForm->ge("btn_def");
*/

        //更新ボタン
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_udpate",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\"" ) );

        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");


        //取消ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ) );

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
        View::toHTML($model, "knjz210Form1.html", $arg);
    }       
}       
?>
