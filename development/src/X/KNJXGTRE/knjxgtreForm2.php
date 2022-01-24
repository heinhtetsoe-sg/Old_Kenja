<?php

require_once('for_php7.php');

//ビュー作成用クラス
class knjxgtreForm2
{
    function main(&$model)
    {
        $objForm = new form;

        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "index.php", "", "main");

        $db = Query::dbCheckOut();

        $objForm->ae(array("type"      => "checkbox",
                            "name"      => "chk_all",
                            "extrahtml"   => "onClick=\"return check_all(this);\"" ));
        
        //項目名
        $header = array($objForm->ge("chk_all"),"クラス","担任","正",
                        "講座名称","実施期間","講座","群ｺｰﾄﾞ","適用開始日付");
        //幅
        //$width  = array("15","50","50","15","100","160","50","*","*");    //2004/08/17 ARAKAKI
        $width  = array("15","45","80","15","140","155","50","*","*");
        $option  = array("nowrap","nowrap","nowrap","nowrap","","nowrap","nowrap","nowrap");

        if ($model->appd != "1") {
            $header = array_slice($header,0,8);
            $width  = array_slice($width,0,8);
            $option = array_slice($option,0,8);
        }

        if ($model->cmd == "left"){
            $header = array_slice($header, 1);
            $width  = array_slice($width, 1);
            $option = array_slice($option, 1);
        }
        
        $t = new Table($header, $width, $option);

        if (isset($model->subclasscd)){
            //受講クラスの一覧を表示
            $query = knjxgtreQuery::selectQueryChair($model);
            $result = $db->query($query);
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $objForm->ae(array("type"      => "checkbox",
                                    "name"     => "CHAIRCD",
                                    "value"    => ($row["CHAIRCD"].",".$row["APPDATE"]),
                                    "multiple"   => true ));

                if ($row["TRGTCOUNT"] > 1){
                    //対象クラスを求める
                    $result2 = $db->query(knjxgtreQuery::selectQueryChairClsDat($row["CHAIRCD"], $model->semester,$model->subclasscd));
                    $trgtabbv = $trgtclass = $sep = "";
                    while($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)){
                        $trgtabbv .= $sep .$row2["HR_NAMEABBV"];
                        $trgtclass .= sprintf("%d%02d", $row2["TRGTGRADE"], $row2["TRGTCLASS"]);
                        $sep = ",";
                    }
                    $ccd = View::alink("#",$row["HR_NAMEABBV"],
                                    "onmouseover=\"stm(['対象クラス','$trgtabbv'], Style)\" onmouseout=\"htm();\"");
                }else{
                    $ccd = $row["HR_NAMEABBV"];
                    $trgtclass = sprintf("%d%02d", $row["TRGTGRADE"], $row["TRGTCLASS"]);
                }
                if($model->cmd == "left" && $model->programid == "KNJD020"){ //テスト実施予定登録
                     $ccd = View::alink(REQUESTROOT ."/D/KNJD020/knjd020index.php", $row["HR_NAMEABBV"], "target=edit_frame",
                                                 array("cmd"               => "edit",
                                                       "CHAIRCD"           => $row["CHAIRCD"],
                                                       "SUBCLASSCD"        => $row["SUBCLASSCD"],
                                                       "GROUPCD"           => $row["GROUPCD"],
                                                       "TARGETCLASS"       => $trgtclass,
                                                       "SHOW_FLG"          => "show"
                                                       ));
                }
                
                $data = array($objForm->ge("CHAIRCD"),
                              $ccd,
                              htmlspecialchars($row["STAFFNAME_SHOW"]),
                              $row["CHARGEDIV"],
                              htmlspecialchars($row["CHAIRNAME"]),
                              str_replace("-","/",$row["SDATE"]) .'～' .str_replace("-","/",$row["EDATE"]),
                              $row["CHAIRCD"],
                              sprintf("%04d", $row["GROUPCD"]),
                              ($model->appd == "1") ? str_replace("-","/",$row["APPDATE"]) : "");
                
                //学籍処理日期間内
                if ($row["DATE_FLG"] == 1){
                    $bgcolor = "#ffffff";
                }else{
                    $bgcolor = "#ccffcc";
                }
                $option  = array("align='center' bgcolor=\"$bgcolor\" nowrap",
                                 "align='center' bgcolor=\"$bgcolor\" nowrap",
                                 "bgcolor=\"$bgcolor\" nowrap",
                                 "align='center' bgcolor=\"$bgcolor\" nowrap",
                                 "align='left' bgcolor=\"$bgcolor\"",
                                 "align='center' bgcolor=\"$bgcolor\" nowrap",
                                 "align='center' bgcolor=\"$bgcolor\" nowrap",
                                 "align='center' bgcolor='$bgcolor' nowrap",
                                 "align='center' bgcolor='$bgcolor' nowrap");

                if ($model->appd != "1") {
                    $data   = array_slice($data,0,8);
                    $option = array_slice($option,0,8);
                } 
                if ($model->cmd == "left"){
                    $data 	= array_slice($data, 1);
                    $option = array_slice($option, 1);
                }
                $t->addData($data, $option);
                $chaircd[] = $row["CHAIRCD"];
                $groupcd[] = $row["GROUPCD"];
                $appdate[] = ($model->appd == "1") ? $row["APPDATE"] : "";
            }
        }
        Query::dbCheckIn($db);

        if ($model->cmd == "main"){
            $t->setFrameHeight(400);
            //ボタンを作成する
            $objForm->ae( array("type" => "button",
                                "name"        => "btn_ok",
                                "value"       => " 選 択 ",
                                "extrahtml"   => "onClick=\"return opener_submit();\"" ));

            $arg["btn_ok"] = $objForm->ge("btn_ok");

            //ボタンを作成する
            $objForm->ae( array("type" => "button",
                                "name"        => "btn_can",
                                "value"       => "キャンセル",
                                "extrahtml"   => "onClick=\"closeWin();\"" ));

            $arg["btn_can"] = $objForm->ge("btn_can");
        }else if($model->cmd == "left"){
            if (isset($chaircd) && is_array($chaircd)){
                $data = array("YEAR" => CTRL_YEAR,                      //年度
                            "SEMESTER"  => $model->semester,        //学期
                            "SUBCLASSCD"    =>  $model->subclasscd, //科目コード
                            "TESTKINDCD"    =>  (isset($model->testkindcd))? $model->testkindcd : "",   //テスト種別コード
                            "TESTITEMCD"    =>  (isset($model->testitemcd))? $model->testitemcd : "",   //テスト項目コード
                            "CHAIRCD"       =>  $chaircd,           //受講クラスコード
                            "GROUPCD"       =>  $groupcd,            //郡コード
                            "APPDATE"       =>  $appdate);

                //right_frameに送信するGTREデータ
                $arg["GTREDATA"] = serialize($data);
            }else{
                //学習記録エクスプローラー
                $arg["ONLOAD"] = "wopen('" .REQUESTROOT ."/X/KNJXGTRE/index.php','KNJXGTRE',0,0,900,550);";
            }
            //学習記録エクスプローラー
            //読込ボタンを作成する
            $objForm->ae( array("type" => "button",
                            "name"        => "btn_popup",
                            "value"       => "･･･",
                            "extrahtml"   => "onclick=\"wopen('" .REQUESTROOT ."/X/KNJXGTRE/index.php?PROGRAMID=".$model->programid ."&APPD=".$model->appd."&DISP=" .$model->disp ."','KNJXGTRE',0,0,900,550)\"") );

            $arg["BTN_GTRE"] = $objForm->ge("btn_popup");
            //hiddenを作成する
            $objForm->ae( array("type"      => "hidden",
                                "name"      => "CHAIRCD") );

            $objForm->ae( array("type"      => "hidden",
                                "name"      => "APPDATE") );
        }
        
        $arg["REQUESTROOT"] = REQUESTROOT;
        $arg["TABLE"] = $t->toTable();
        //タイトル
        $arg["TITLE"] = (isset($model->title))? $model->title : "";

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjxgtreForm2.html", $arg);
    }
}
?>
