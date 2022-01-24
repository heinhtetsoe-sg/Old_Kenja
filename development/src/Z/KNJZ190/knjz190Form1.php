<?php

require_once('for_php7.php');

class knjz190Form1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz190index.php", "", "edit");

        $clweek_d = array();
        //$clweek["年度"]=$model->ctrl["年度"];

        //更 新ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => " onclick=\"return btn_submit('update');\"" ) );

        $arg["btn_update"] = $objForm->ge("btn_update");

        //取 消ボタンを作成する
        $objForm->ae( array("type"        => "reset",
                            "name"        => "btn_can",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return ShowConfirm()\"" ) );

        $arg["btn_can"] = $objForm->ge("btn_can");

        //終 了ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => " onclick=\"return closeWin();\"" ) );

        $arg["btn_end"] = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae( array("type"          => "hidden",
                            "name"          => "cmd"
                            ) );

        //hiddenを作成する
        $objForm->ae( array("type"          => "hidden",
                            "name"          => "cntl_dt_year",
                            "value"         => CTRL_YEAR,
                            "extrahtml"     => " onLoad=\"return closing_window();\""
                            ) );

        //処理年度表示
        $arg["TOP"] = array("TRANSACTION"   => "<font color=\"#000000\">".CTRL_YEAR."年度</font>"
                           );

        //listデータヘッダー
        $arg["HEAD"] = array("GRADE"      => "<th rowspan=\"2\" WIDTH=\"130\">クラス名称</th>",
                             "SUM_WEEK"   => "<th colspan=".$model->gakki1." WIDTH=\"*\">授業週数</th>",
                             "SUM_DAY"    => "<th colspan=".$model->gakki1." WIDTH=\"*\">授業日数</th>"
                            );

        $db = Query::dbCheckOut();

        //----------------------以下、擬似フレーム内リスト表示----------------------
        //$model->past_reserv,$model->bk_backupを初期化
        $model->past_reserv = array();
        $model->bk_update   = array();
        $c =0;

        $query = knjz190Query::ReadQuery($model);
        $result = $db->query($query);
        $count=0;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $count++;
        }

        //SQL文発行
        $query = knjz190Query::ReadQuery($model);
        $result = $db->query($query);

        //テキスト入力部分を作成
        $a = 1;
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $a++;
            for($i=0; $i <= $model->ctrl["学期数"]; $i++)
            {
                $temp = $i.$row["GRADE"].sprintf("%02d", $row["HR_CLASS"]);
                $temp2 = $row["GRADE"].sprintf("%02d", $row["HR_CLASS"]);

                //フォーム内の入力順を指定(Tabキーの制御)
                if($i==1){
                    $b=$a;
                    $e=$a+$count*4;

                }elseif($i==2){

                    $b=$a+$count;
                    $e=$a+$count*5;

                }elseif($i==3){

                    $b=$a+$count*2;
                    $e=$a+$count*6;

                }elseif($i==0){

                    $b=$a+$count*3;
                    $e=$a+$count*7;
                }
                //現在処理学期の場合（入力可）
                if($i == CTRL_SEMESTER || $i == 0) {
                    if($i == 0){
                        $type = "hidden";
                    }else{
                        $type = "text";
                    }
                    $htmlw = " STYLE=\"text-align: right\" id = CW$temp onblur=\"return get_count(this,'$temp2')\" tabindex='".($b)."'";
                    $htmld = " STYLE=\"text-align: right\" id = CD$temp onblur=\"return get_count(this,'$temp2')\" tabindex='".($e)."'";
                }
                //現在処理学期ではない場合（入力不可）
                elseif($i != CTRL_SEMESTER){
                    $type = "text";
                    $htmlw = " STYLE=\"text-align: right;background-color :#cccccc\" id = CW$temp onblur=\"return get_count(this,'$temp2')\" onFocus=\"this.blur();\" tabindex='".($b)."'";
                    $htmld = " STYLE=\"text-align: right;background-color :#cccccc\" id = CD$temp onblur=\"return get_count(this,'$temp2')\" onFocus=\"this.blur();\" tabindex='".($e)."'";
                }
                if($row["CW$i"] == ""){
                    $row["CW$i"] = "0";
                }
                if($row["CD$i"] == ""){
                    $row["CD$i"] = "0";
                }
                $objForm->ae( array("type"      => "$type",
                                    "name"      => "CW$i",
                                    "id"        => $i,
                                    "size"      => "2",
                                    "maxlength" => "2",
                                    "value"     => $row["CW$i"],
                                    "multiple"  => 1,
                                    "extrahtml" => $htmlw) );
                if($i == 0)$row["numw"] = $row["CW$i"];
                $objForm->ae( array("type"      => "$type",
                                    "name"      => "CD$i",
                                    "id"        => $i,
                                    "size"      => "2",
                                    "maxlength" => "3",
                                    "STYLE"     =>"text-align: right",
                                    "value"     => $row["CD$i"],
                                    "multiple"  => 1,
                                    "extrahtml" => $htmld) );
                if($i == 0)$row["numd"] = $row["CD$i"];
            }

            $row["col"]= ($c%2)? "#ccffcc" : "#ffffff";
             //レコードを連想配列のまま配列$arg[data]に追加していく
//                $row["GRADECLASS"] = ltrim($row["GRADE"],"0")."年".ltrim($row["HR_CLASS"],"0")."組";
                $row["GRADECLASS"] = $row["HR_NAME"];

                unset($CW);
                unset($CD);

                $weeks ="";
                $days ="";
            for($i=0; $i <= $model->ctrl["学期数"]; $i++){
                if($i == 0){
                    $CW0 = "<td width=\"65\"><span id =\"SCW0".$row["GRADE"].sprintf("%02d", $row["HR_CLASS"])."\">".$objForm->ge("CW$i").$row["numw"]."</span></td>";
                    $CD0 = "<td width=\"*\"><span id =\"SCD0".$row["GRADE"].sprintf("%02d", $row["HR_CLASS"])."\">".$objForm->ge("CD$i").$row["numd"]."</span></td>";
                }else{
                    $CW = "<td width=\"65\">".$objForm->ge("CW$i")."</td>";
                    $CD = "<td width=\"65\">".$objForm->ge("CD$i")."</td>";
                    $weeks .=$CW;
                    $days .=$CD;
                }
            }

            $row["CW"] = $weeks.$CW0;
            $row["CD"] = $days.$CD0;
            $arg["data"][] = $row;
            $c++;
        }

        //var_dump($model->RESERVATION);

        //学期名取得
        $query = knjz190Query::SemesterGet($model);
        $result = $db->query($query);
        $i=0;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $tmp[$i] = $row["SEMESTERNAME"];
            $i++;
            if($i == $model->ctrl["学期数"]){
                break;
            }
        }
        $result->free();
        Query::dbCheckIn($db);
        //事前処理が行われていない場合
        # if(!isset($tmp[0]) || !isset($row["CW"])){
        if(!isset($tmp[0])){
            $arg["close"] = "close_window(1);";
        }
        //CTRL_SEMESTERNAME
        $arg["gaki"]["GAKI"] = $tmp;

        //処理が完了していなければ閉じる。
        $arg["Closing"] = "if (document.forms[0].cntl_dt_year.value == \"\"){
                               closing_window();
                           }";
        //hiddenの作成
        $objForm->ae(array("type" =>"hidden",
                           "name" =>"gakki",
                           "value" =>$model->ctrl["学期数"]));

        //デバッグ------------------------
        //var_dump($model->reservation);

        //セキュリティチェック
        if ($model->sc != DEF_UPDATABLE){
            $arg["close"] = "close_window();";
        }

        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz190Form1.html", $arg);
    }
}
?>
