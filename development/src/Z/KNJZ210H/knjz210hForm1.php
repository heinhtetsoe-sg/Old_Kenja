<?php

require_once('for_php7.php');

class knjz210hform1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjz210hindex.php", "", "main");

        //処理年度
        $arg["sepa"]["YEAR"] = CTRL_YEAR . "年度";

        //オプション設定
        $opt[] = array("label" => "1：１０段階  ", "value" =>"1");
        $opt[] = array("label" => "2：５段階    ", "value" =>"2");

        //リスト表示
        if(!isset($model->sepa)) $model->sepa = 1;

        //評定区分コンボボックス
        $objForm->ae( array("type"      => "select",
                            "name"      => "sepa",
                            "size"      => "1",
                            "value"     => $model->sepa,
                            "extrahtml" => "onChange=\"return btn_submit('change');\"",
                            "options"   => $opt) );

        $arg["sepa"]["VAL"] = $objForm->ge("sepa");

        $db = Query::dbCheckOut();

        $cnt = ($model->sepa == 1) ? 10 : 5 ;

        //SQL文発行
        $ar[] = array();
        
        //警告メッセージを表示しない場合
        if (isset($model->sepa) && !isset($model->warning)){
            $query = knjz210hQuery::selectQuery($model);
            $result = $db->query($query);
        }else{
            $row =& $model->field;
            $result = "";
        }

        $rate_sum = 0;
        for ($i=$cnt; $i>0; $i--){
            if($result != ""){
                if($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                    //レコードを連想配列のまま配列$arg[data]に追加していく。 
                    array_walk($row, "htmlspecialchars_array");
                    $row["RATE"] = $row["RATE"];
                    $row["ASSESSLEVEL5"] = $row["ASSESSLEVEL5"];
                    $up = $row["UPDATED"];
                }
            }
            //評定段階
            $row["ASSESSLEVEL"] = $i;

            //評定率合計
            if ($row["RATE"] != "") $rate_sum = $rate_sum + $row["RATE"];

            //評定率
            $objForm->ae( array("type"      => "text",
                                "name"      => "RATE".$i,
                                "size"      => "6",
                                "maxlength" => "3",
                                "value"     => $row["RATE"],
                                "extrahtml" => "STYLE=\"text-align: right\" onblur=\"isNumb(this,".$cnt.");\" "));
            $row["RATE"] = $objForm->ge("RATE".$i);

            //評定5段階
            $objForm->ae( array("type"      => "text",
                                "name"      => "ASSESSLEVEL5".$i,
                                "size"      => "6",
                                "maxlength" => "1",
                                "value"     => $row["ASSESSLEVEL5"],
                                "extrahtml" => "STYLE=\"text-align: right\" onblur=\"isNumb2(this,".$cnt.");\" "));
            $row["ASSESSLEVEL5"] = $objForm->ge("ASSESSLEVEL5".$i);


            $arg["data"][] = $row;
        }

        Query::dbCheckIn($db);

        $arg["data2"]["RATE_SUM"] = ($rate_sum > 0) ? $rate_sum : "";



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
        View::toHTML($model, "knjz210hForm1.html", $arg);
    }       
}       
?>
