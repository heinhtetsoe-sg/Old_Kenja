<?php

require_once('for_php7.php');

//ファイルアップロードオブジェクト
require_once("csvfile.php");
class knjd210Form1{
    function main(&$model){
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjd210index.php", "", "main");

        $db = Query::dbCheckOut();

        $objForm->ae(array("type"   => "checkbox",
                        "name"      => "CHECKALL",
                        "extrahtml" => "onClick=\"return check_all(this);\"" ));

        $order[1] = "▲";
        $order[-1] = "▼";

        $arg["CHECKALL"] = $objForm->ge("CHECKALL");
        $arg["ATTENDNO"] = View::alink("knjd210index.php", "<font color=\"white\">年-組-番</font>", "",
                        array("cmd"=>"sort", "sort"=>"ATTENDNO")) .$order[$model->sort["ATTENDNO"]];
                        
        $arg["RANK"] = View::alink("knjd210index.php", "<font color=\"white\">席次</font>", "", array("cmd"=>"sort", "sort"=>"RANK")).$order[$model->sort["RANK"]];
        //学期毎の学期末評価
        if (is_numeric($model->control["学期数"])){
            $arg["SEMESTER"] = array();
            for ($i = 0; $i < $model->control["学期数"]; $i++){
                $arg["SEMESTER"][] = $model->control["学期名"][$i+1];
            }
            $arg["COLSPAN"] = $model->control["学期数"];
        }
        if (isset($model->field["SUBCLASSCD"])){
            $array = $new_tmpval = $avgval = $valuation = $get_credit = $add_credit = array();
            //SQL文発行
            $query = knjd210Query::selectQuery($model);
            $result = $db->query($query);
            while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $array[] = $row;
                $new_tmpval[] = $row["NEW_TMPVAL"];
                $avgval[] = $row["AVGVAL"];
                $valuation[] = $row["VALUATION"];
                $get_credit[] = $row["GET_CREDIT"];
                $add_credit[] = $row["ADD_CREDIT"];
            }
            //席次を求める
            array_multisort($valuation, SORT_NUMERIC, SORT_DESC);
        }
        $model->data = $model->org = array();
        //オブジェクト作成
        $objUp = new csvFile();
        $objUp->setFileName("学年末成績処理.csv");
        //ボタンの押下不可
        $disabled = "disabled";
        if (isset($array) && is_array($array)){
           foreach($array as $key => $row){
                $rank = "";
                if (is_numeric($row["VALUATION"])){
                    $arr = array_keys($valuation, $row["VALUATION"]);
                    $rank = $arr[0] + 1;
                }
                $csv = array($row["SCHREGNO"],
                             trim(sprintf("%s%d番", $row["HR_NAME"], $row["ATTENDNO"])),
                             $row["NAME_SHOW"],
                             $row["SEX"]);
                             
                $row["ATTENDNO"] = sprintf("%s-%d", $row["HR_NAMEABBV"], $row["ATTENDNO"]);

                if (is_numeric($model->control["学期数"])){
                    $row["VAL"] = array();
                    for ($i = 0; $i < $model->control["学期数"]; $i++){
                        $row["VAL"][] = $row["VAL" .($i+1)];
                        $csv[]  = $row["VAL" .($i+1)];
                    }
                }
                array_push($csv, $row["GRADINGCLASSCD"],
                                     $row["AVGVAL"],
                                     $row["NEW_TMPVAL"],
                                     $row["VALUATION"],
                                     $row["GET_CREDIT"],
                                     $row["ADD_CREDIT"],
                                     $rank,
                                     $row["REMARK"]);

                //書き出し用CSVデータ
                $objUp->addCsvValue($csv);

                $model->data["SCHREGNO"][] = $row["SCHREGNO"];
                $model->data["TAKESEMES"][] = $row["TAKESEMES"];
                $model->data["GRADINGCLASSCD"][] = $row["GRADINGCLASSCD"];
                $model->org["REMARK"][] = $row["REMARK"];

                $objForm->ae(array("type"      => "checkbox",
                                 "name"     => "CHECKED",
                                 "value"    => $row["GRADINGCLASSCD"] ."-" .$row["SCHREGNO"],
                                "extrahtml"   => "tabindex=\"-1\"",
                                "multiple"   => "1" ));
                //評価/５
                $objForm->ae( array("type"        => "text",
                                    "name"        => "VALUATION",
                                    "size"        => 3,
                                    "maxlength"   => 4,
                                    "multiple"    => 1,
                                    "extrahtml"   => "tabindex=\"".($key+1) ."\" STYLE=\"text-align: right\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\"",
                                    "value"       => $row["VALUATION"] ));
                //修得単位
                $objForm->ae( array("type"        => "text",
                                    "name"        => "GET_CREDIT",
                                    "size"        => 3,
                                    "maxlength"   => 4,
                                    "multiple"    => 1,
                                    "extrahtml"   => "tabindex=\"".(get_count($valuation)+$key+1) ."\" STYLE=\"text-align: right\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\"",
                                    "value"       => $row["GET_CREDIT"] ));

                //増加区分
                $objForm->ae( array("type"        => "text",
                                    "name"        => "ADD_CREDIT",
                                    "size"        => 3,
                                    "maxlength"   => 4,
                                    "multiple"    => 1,
                                    "extrahtml"   => "tabindex=\"".(get_count($valuation)*2+$key+1) ."\" STYLE=\"text-align: right\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\"",
                                    "value"       => $row["ADD_CREDIT"] ));

                //備考
                $objForm->ae( array("type"        => "text",
                                    "name"        => "REMARK",
                                    "size"        => 17,
                                    "maxlength"   => 40,
                                    "multiple"    => 1,
                                    "extrahtml"   => "tabindex=\"".(get_count($valuation)*3+$key+1) ."\" onChange=\"this.style.background='#ccffcc';\"",
                                    "value"       => $row["REMARK"] ));
/*

                //評価/100
                $objForm->ae( array("type"      => "hidden",
                                    "name"      => "HIDDEN_AVGVAL",
                                    "multiple"    => 1,
                                    "value"     => $row["AVGVAL"]));

                $row["HIDDEN_AVGVAL"]      = $objForm->ge("HIDDEN_AVGVAL"); 
*/

               $data = array($objForm->ge("CHECKED"),
                              $row["ATTENDNO"],
                              $row["NAME_SHOW"]);

                $row["CHECKED"]     = $objForm->ge("CHECKED");
                $row["VALUATION"]   = $objForm->ge("VALUATION");
                $row["GET_CREDIT"]  = $objForm->ge("GET_CREDIT");
                $row["ADD_CREDIT"]  = $objForm->ge("ADD_CREDIT");
                $row["REMARK"]      = $objForm->ge("REMARK");
                $row["RANK"]        = $rank;



                $arg["data"][] = $row;

                $disabled = "";

                $key = array("学籍番号"         =>$row["SCHREGNO"],
                             "評価科目コード"   =>$row["GRADINGCLASSCD"]);

                //入力エリアとキーをセットする
                $objUp->setElementsValue("VALUATION[]","評価／5" , $key);
                //入力エリアとキーをセットする
                $objUp->setElementsValue("GET_CREDIT[]","修得単位" , $key);
                //入力エリアとキーをセットする
                $objUp->setElementsValue("ADD_CREDIT[]","増加単位" , $key);
                //入力エリアとキーをセットする
                $objUp->setElementsValue("REMARK[]","備考" , $key);


                //ゼロ埋めフラグ
                $flg = array("学籍番号"       => array(true,8),
				             "評価科目コード" => array(true,6));
				$objUp->setEmbed_flg($flg);           
                
            }
        }
        $arr_h = array("学籍番号","年組番","氏名","性別");
        //学期毎の学期末評価
        if (is_numeric($model->control["学期数"])){
            for ($i = 0; $i < $model->control["学期数"]; $i++){
                $arr_h[] = $model->control["学期名"][$i+1] ."評価";
            }
        }
        array_push($arr_h,
                    "評価科目コード",
                    "評価／100",
                    "仮評価",
                    "評価／5",
                    "修得単位",
                    "増加単位",
                    "席次",
                    "備考");

        $objUp->setHeader($arr_h);
        $objUp->setType(array(7+$model->control["学期数"]=>'N',
                              8+$model->control["学期数"]=>'N',
                              9+$model->control["学期数"]=>'N',
                              11+$model->control["学期数"]=>'S'));

        $objUp->setSize(array(11+$model->control["学期数"]=>60));
        //CSVファイルアップロードコントロール
        $arg["FILE"] = $objUp->toFileHtml($objForm);

        //評価の最大値
        $query = knjd210Query::selectMaxAssessLevelQuery($model);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["ASSESSLEVEL"] = $model->assesslevel = $row["ASSESSLEVELCNT"];
        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ASSESSLEVEL",
                            "value"     => $row["ASSESSLEVELCNT"]
                            ) );
        
        Query::dbCheckIn($db);
        
        //仮評価再処理ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_estimate",
                            "value"       => "仮評価処理",
                            "extrahtml"   => "onclick=\"return btn_submit('estimate');\"" ) );

        $arg["btn_estimate"] = $objForm->ge("btn_estimate");

        //平均点補正処理ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_avg",
                            "value"       => "平均点補正処理",
                            "extrahtml"   => (($model->semes_assesscd == 2)? "disabled" : $disabled) ." onclick=\"return btn_submit('avg');\"" ) );

        $arg["btn_avg"] = $objForm->ge("btn_avg");

        //相対評価処理ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_assess",
                            "value"       => "相対評価処理",
                            "extrahtml"   => $disabled ." onclick=\"return btn_submit('assess');\"" ) );

        $arg["btn_assess"] = $objForm->ge("btn_assess");

        //削除ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_del",
                            "value"       => "削 除",
                            "extrahtml"   => $disabled ." onclick=\"return btn_submit('delete');\"" ) );

        $arg["btn_del"] = $objForm->ge("btn_del");

        //更新ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_udpate",
                            "value"       => "更 新",
                            "extrahtml"   => $disabled ." onclick=\"return btn_submit('update');\"" ) );

        $arg["btn_update"] = $objForm->ge("btn_udpate");

        //取消ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_can",
                            "value"       => "取 消",
                            "extrahtml"   => $disabled ." onclick=\"return btn_submit('cancel');\"" ) );

        $arg["btn_can"] = $objForm->ge("btn_can");

        //終了ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["btn_end"] = $objForm->ge("btn_end");

        //終了ボタンを作成する
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["btn_end"] = $objForm->ge("btn_end");
        
        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "GTREDATA"
                            ) );
        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "AVGMOD_FLG"
                            ) );

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "RELATIVED_FLG"
                            ) );

        $arg["finish"]  = $objForm->get_finish();

        $subject = get_count($valuation);
        if ($subject > 0){
            $arg["AVG"] = array("SUBJECT"       => $subject,                                    //対象人
                                "NEW_TMPVAL"    => round(array_sum($new_tmpval)/$subject, 1),   //評価平均
                                "AVGVAL"    => round(array_sum($avgval)/$subject, 1),           //評価平均
                                "VALUATION"    => round(array_sum($valuation)/$subject, 1),     //評価平均
                                "GET_CREDIT"    => round(array_sum($get_credit)/$subject, 1),   //習得単位
                                "ADD_CREDIT"    => round(array_sum($add_credit)/$subject, 1)    //増加区分

            );
            $model->average["CLASS"] = round(array_sum($valuation)/$subject, 1);                //修正点平均
        }
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();        
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。

        View::toHTML($model, "knjd210Form1.html", $arg);
    }
}
?>
