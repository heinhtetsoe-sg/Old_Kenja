<?php

require_once('for_php7.php');


class knjz220Form2{

    function main(&$model){

        $objForm      = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjz220index.php", "", "edit");

        #####
        //var_dump($model->TBL_COUNT);
        //var_dump($model->field1);
        #####

        //DBオープン
        $db  = Query::dbCheckOut();

        //学年名称取得
        $grade_name = array();
        $result = $db->query(knjz220Query::combo_grdQuery($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $grade_name[$row["GRADE"]] = $row["SHOWGRADE"];
        }

        //ヘッダー設定
        $row = $db->getRow(knjz220Query::getHeaderInfo(), DB_FETCHMODE_ASSOC);
        $arg["head"] = array( "CODE"           => $model->field1["SUBCLASSCD"],
                              "SUBJECT"        => $model->subclassname,
                              "GRADE"          => $grade_name[$model->grade],
                              "ASSESSLEVELCNT" => $row["ASSESSCD"]." ： ".$row["ASSESSMEMO"]);

        //デフォルト値を作成
        $default_val = array();
        $cnt = 0;
        $ay  = array();

        $result = $db->query(knjz220Query::getDefultData());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){

            //小数点を取り除く。
            $ar =explode(".",$row["ASSESSLOW"]);
            $row["ASSESSLOW"]  = $ar[0];
            $ar =explode(".",$row["ASSESSHIGH"]);
            $row["ASSESSHIGH"] = $ar[0];

            if($default_val != ""){
                $default_val["ASSESSLOW"]  .= ",";
                $default_val["ASSESSHIGH"] .= ",";
                $default_val["ASSESSMARK"] .= ",";
            }else{
                //最小値保持
                $default_val["MIN"]  = $row["ASSESSLOW"];
                //初期化設定
                $default_val["ASSESSLOW"]  = "";
                $default_val["ASSESSHIGH"] = "";
                $default_val["ASSESSMARK"] = "";
            }

            //デフォルト値格納
            $default_val["ASSESSLOW"]  .= $row["ASSESSLOW"];
            $default_val["ASSESSHIGH"] .= $row["ASSESSHIGH"];
            $default_val["ASSESSMARK"] .= $row["ASSESSMARK"];

            //テーブル作成時の最大値計算
            $cnt++;
            //最大値保持
            $default_val["MAX"]  = $row["ASSESSHIGH"];
        }

#######  リスト表示--------

        //初期化
        $ar[] = array();
        $up   = "";

        //warning処理
        if (!isset($model->warning)){
            $result = $db->query(knjz220Query::selectQuery($model));
        } else {
            $row    =& $model->field2;
            $result =  "";
            $up = $model->field1["UPDATED"];
        }

        //テーブルテンプレート作成。
        $tmp_tbl      = "<span id=\"strID%s\">%s</span>";
        $tmp_JScript  = " STYLE=\"text-align: right\" ";
        $tmp_JScript .= " onChange=\"document.all['strID%s'].innerHTML=(this.value -1);\" ";
        $tmp_JScript .= " onblur=\"this.value=isNumb(this.value), cleaning_val('off')\" ";
        $JScript = " STYLE=\"text-align: center\" onblur=\"this.value=toAlphaNumber(this.value)\"; ";

        //テーブル作成
        for ($i=1; $i<=$cnt; $i++){

            //代入値初期化
            $textvalue  = 0 ;
            $tablevalue = 0 ;

            if($result != ""){
                if($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    array_walk($row, "htmlspecialchars_array");
                    //小数点を取り除く。
                    $ar =explode(".",$row["ASSESSLOW"]);
                    $row["ASSESSLOW"]  = $ar[0];
                    $ar =explode(".",$row["ASSESSHIGH"]);
                    $row["ASSESSHIGH"] = $ar[0];
                    if($up == ""){
                        $up = $row["UPDATED"];
                    }else{
                        $up .= ",".$row["UPDATED"];
                    }
                }
            }

            $row["ASSESSLEVEL"] = $i;

            if($result != ""){
                $row["ASSESSMARK"]  = (isset($row["ASSESSMARK"]))? $row["ASSESSMARK"] : "" ;
            }else if ($result == ""){
                $row["ASSESSMARK"]  = $row["ASSESSMARK$i"];
            }

            //評定名称テキストボックス作成(ASSESSMARK)
            $objForm->ae( array("type"        => "text",
                                "name"        => "ASSESSMARK".$i,
                                "size"        => "4",
                                "maxlength"   => "2",
                                "extrahtml"   => $JScript,
                                "value"       => $row["ASSESSMARK"]));
            $row["ASSESSMARKTEXT"] = $objForm->ge("ASSESSMARK".$i);

            //下限値テキストの有無設定
            if ($row["ASSESSLEVEL"] == 1) {
                $row["ASSESSLOWTEXT"] = $default_val["MIN"];
            } else {
                if($result != ""){
                    $textvalue  = (isset($row["ASSESSLOW"]))? $row["ASSESSLOW"] : "" ;
                }else if ($result == ""){
                    $textvalue  = $row["ASSESSLOW$i"];
                }

                //テキストボックス作成(ASSESSLOW)
                $objForm->ae( array("type"        => "text",
                                    "name"        => "ASSESSLOW".$i,
                                    "size"        => "4",
                                    "maxlength"   => "2",
                                    "extrahtml"   => sprintf($tmp_JScript,($i-1)),
                                    "value"       => $textvalue));
                $row["ASSESSLOWTEXT"] = $objForm->ge("ASSESSLOW".$i);

                $stock[] = (isset($row["ASSESSLOW"]))? $row["ASSESSLOW"] : "" ;
            }

            //非text部分作成
            if ($row["ASSESSLEVEL"] == $cnt){
                $row["ASSESSHIGHTEXT"] = $default_val["MAX"];
                //$high_stock[$cnt] = $row["ASSESSHIGH"];
            } else {
                if($result != ""){
                    $tablevalue     = (isset($row["ASSESSHIGH"]))? $row["ASSESSHIGH"] : "" ;
                    $high_stock[$i] = (isset($row["ASSESSHIGH"]))? $row["ASSESSHIGH"] : "" ;
                }else if ($result == ""){
                    $tablevalue = ((int)$row["ASSESSLOW".($i + 1)] -1);
                }
                $row["ASSESSHIGHTEXT"] = sprintf($tmp_tbl,$row["ASSESSLEVEL"],$tablevalue);
            }
            $arg["data"][] = $row;
        }

        //コピー機能コンボボックス内データ取得
        $result = $db->query(knjz220Query::Copy_comboQuery($model->grade, $model));
        $copy_opt = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $copy_opt[] = array("label" => htmlspecialchars("　".$grade_name[$row["GRADE"]]."： "
                                           .$row["SUBCLASSCD"]." ".$row["SUBCLASSNAME"]."　"), 
                                "value" => ($row["GRADE"].",".$row["SUBCLASSCD"]));
        }
        Query::dbCheckIn($db);

        //コピー科目コンボボックス
        $objForm->ae( array("type"    => "select",
                            "name"    => "copy",
                            "size"    => "1",
                            "value"   => $model->copy,
                            "options" => $copy_opt));
                    
        //コピーボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_copy",
                            "value"       => "左の科目の評定をコピー",
                            "extrahtml"   => "style=\"width:200px\"onclick=\"return btn_submit('copy')\"" ));

        $arg["copy"] = array( "VAL"     => $objForm->ge("copy"),
                              "BUTTON"  => $objForm->ge("btn_copy"));

        //デフォルトに戻すボタン
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_def",
                            "value"       => "デフォルトに戻す",
                            "extrahtml"   => "onclick=\"return insertDefVal('".$cnt."');\"" ) );

        $arg["button"]["btn_def"] = $objForm->ge("btn_def");

        //更新ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_udpate",
                            "value"       => "更 新",
                            "extrahtml"   => " onclick=\"return btn_submit('update'), cleaning_val('off');\"" ) );

        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

        //取消ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => " onclick=\"return btn_submit('reset');\""  ) );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SUBCLASSCD",
                            "value"     =>$model->field1["SUBCLASSCD"]) );

        //テーブルの作成数
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "TBL_COUNT",
                            "value"     => $cnt ) );

        //データ処理日の保持
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "UPDATED",
                            "value"     => ($model->copy_flg == true)? $model->field1["UPDATED"] : $up ) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "Cleaning",
                            "value"     => $model->Clean) );

        //デフォルト値
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "default_val_low",
                            "value"     => $default_val["ASSESSLOW"] ));

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "default_val_high",
                            "value"     => $default_val["ASSESSHIGH"] ));

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "default_val_mark",
                            "value"     => $default_val["ASSESSMARK"] ));

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjz220Form2.html", $arg); 

    }
}
?>
