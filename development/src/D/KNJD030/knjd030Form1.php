<?php

require_once('for_php7.php');

//ファイルアップロードオブジェクト
require_once("csvfile.php");
//ビュー作成用クラス
class knjd030Form1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objUp = new csvFile();
        $objUp->setFileName("テスト得点データ.csv");

        $err_flg = 0;   //複数テスト選択時の満点不揃いエラー
        $disabled = ""; //更新、取消ボタンの無効フラグ
        $objForm = new form;
        
        $err_msg = "alert('".$model->errorMessage("MSG916","満点の値が不揃いです")."');";
        $reload  = "top.main_frame.left_frame.document.forms[0].btn_popup.click();";  //エクスプローラ再表示

//var_dump($model->field["SCORE"]);
        $db = Query::dbCheckOut();


        //ヘッダデータ読込
        $result = $db->query(knjd030Query::getAvgScoreQuery($model));

        //それぞれの満点が異なる場合はエラー(SQL文で満点でGROUPBYしてるので異なる場合は複数行になる）
        if ($result->numRows() > 1) {
            $arg["err_msg"] = $err_msg;
            $arg["reload"] = $reload;
            $err_flg = 1;
            $disabled = "disabled";
        }

        $row = $result->fetchRow(DB_FETCHMODE_ASSOC);
        $result->free();

        if (!is_array($row)){
            $perfect = 0;
        }else{
            $perfect = $row["PERFECT"];
        }

        $order[1]  = "▲";
        $order[-1] = "▼";

        //ソート用のURL作成
        $header= array("連番",
                       "学籍番号",
                        View::alink("knjd030index.php", "<font color=\"white\">年-組-番</font>", "", array("cmd"=>"sort", "sort"=>"ATTENDNO")) .$order[$model->sort["ATTENDNO"]],
                       "氏名",
                       "性別",
                       "異動<br>情報",
                       "得点/" .$perfect,
                        View::alink("knjd030index.php", "<font color=\"white\">席次</font>", "", array("cmd"=>"sort", "sort"=>"SCORE")).$order[$model->sort["SCORE"]]
                        );
        //幅
        $width  = array("50","100","100","150","50","50","100","*");
        $option = array("nowrap","nowrap","nowrap","nowrap","nowrap","nowrap","nowrap","nowrap");

        $t = new Table($header, $width, $option);

        $arg["EXAMINEE"]  = (is_numeric($row["EXAMINEE"]))? $row["EXAMINEE"] : 0;    //受験者人数
        $arg["AVG_SCORE"] = (is_numeric($row["AVG_SCORE"]))? round($row["AVG_SCORE"], 1) : 0;    //平均

        //SQL文発行
        $query = knjd030Query::selectQuery($model);
        $result = $db->query($query);
        $i = 0;
        $ii=0;
        $array = $score = $model->score = $model->schregno = $model->chaircd = array();
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            if ($err_flg == 0) {
                $array[$i] = $row;
                $score[$i] = $row["SCORE"];
                $model->score[$i] = $row["SCORE"];
                $model->schregno[$i] = $row["SCHREGNO"];
                $model->chaircd[$i]  = $row["CHAIRCD"];
                $i++;
            }
        }
        //異動情報
        $query = knjd030Query::Get_NameQuery($schregno, $opration_d);

        $arg["SUBJECT"] = $i;                  //対象人数
        $result->free();

        //席次を求める
        array_multisort($score, SORT_NUMERIC, SORT_DESC);
        foreach($array as $row){

            if ($row["ATTEND_FLG"] == 1){
                $arr = array_keys($score, $row["SCORE"]);
                $rank = $arr[0] + 1;
            }else{
                $rank = '';
            }

            $numbers +=1;
            $schregno = $row["SCHREGNO"];
            $opration_d = $row["OPERATION_DATE"];

            //hiddenを作成する
            $objForm->ae( array("type"      => "hidden",
                                "name"      => "OPERATION_DATE",
                                "value"     => $row["OPERATION_DATE"]
                                ) );

            if(isset($model->warning)){
                $row["SCORE"] =& $model->field["SCORE"][$ii];
                $ii++;
            }


            //異動情報
            $query = knjd030Query::Get_NameQuery($schregno, $opration_d);
            $transfer = $db->getOne($query);
            if(!$transfer){
                $font="<font color=\"#000000\">";
                $fontcl="</font>";
                $extrahtml="STYLE=\"text-align: right\" TABINDEX=\"$numbers\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this,$perfect); \" ";
                $scores = (($row["ATTEND_FLG"] == 1)? $row["SCORE"] : "");

            }else{
                $font="<font color=\"#CCCCCC\">";
                $fontcl="</font>";
                //得点
                $scores = (($row["ATTEND_FLG"] == 1)? $row["SCORE"] : "");
                $extrahtml="STYLE=\"text-align: right; background-color:#CCCCCC\"onFocus=\"this.blur();\" ";

            }
            //CSVを書き出すデータをレコード毎セットする
            $csv = array($numbers,
                         trim(sprintf("%s%d番", $row["HR_NAME"], $row["ATTENDNO"])),
                         $schregno,
                         htmlspecialchars($row["NAME_SHOW"]),
                         substr($row["SEX"],0,3),
                         $transfer,
                         $row["TESTKINDCD"],
                         $row["TESTKINDNAME"],
                         $row["TESTITEMCD"],
                         $row["TESTITEMNAME"],
                         $row["SUBCLASSCD"],
                         $row["SUBCLASSNAME"],
                         $scores,
                         $rank);

            //書き出し用CSVデータ
            $objUp->addCsvValue($csv);

            //CSVを取り込むフォームの入力エリアの名前とキーをセットする
            $key = array("学籍番号"         => $row["SCHREGNO"],
                         "テスト種別コード" => $row["TESTKINDCD"],
                         "テスト項目コード" => $row["TESTITEMCD"],
                         "科目コード"       => $row["SUBCLASSCD"]);
            //入力エリアとキーをセットする
            $objUp->setElementsValue("SCORE[]", "得点", $key);

	        //ゼロ埋めフラグ
            $flg = array("学籍番号"         => array(true,8),
	                     "テスト種別コード" => array(true,2),
	                     "テスト項目コード" => array(true,2),
	                     "科目コード"       => array(true,6));
	       	$objUp->setEmbed_flg($flg);
         
            //得点
            $objForm->ae( array("type"        => "text",
                                "name"        => "SCORE",
                                "size"        => 4,
                                "maxlength"   => 4,
                                "multiple"    => 1,
                                "extrahtml"   => $extrahtml,
                                "value"       => $scores ));

            $SCORE=$objForm->ge("SCORE");
 
            $data = array($font.$numbers.$fontcl,
                          $font.$row["SCHREGNO"].$fontcl,
                          $font.sprintf("%s-%d", $row["HR_NAMEABBV"], $row["ATTENDNO"]).$fontcl,
                          $font.htmlspecialchars($row["NAME_SHOW"]).$fontcl, 
                          $font.substr($row["SEX"],0,3).$fontcl,
                          $font.$transfer.$fontcl,
                          $font.$SCORE.$fontcl,
                          $font.$rank.$fontcl
                          );

            $option  = array("align='center' nowrap",
                             "align='center' nowrap",
                             "align='center' nowrap",
                             "align='left'   nowrap",
                             "align='center' nowrap",
                             "align='center' nowrap",
                             "align='center' nowrap",
                             "align='center' nowrap"
                             );

            $arg["op_date"]=str_replace("-","/",$row["OPERATION_DATE"]);

            $t->addData($data, $option);

        }
        Query::dbCheckIn($db);

        $t->setFrameHeight(600);
        //テーブル作成
        $arg["TABLE"] = $t->toTable();

        $arg["ctrl_date"]= str_replace("-","/",CTRL_DATE);

        $objUp->setHeader(array("連番",
                                "年組番",
                                "学籍番号",
                                "生徒氏名",
                                "性別",
                                "異動情報",
                                "テスト種別コード",
                                "テスト種別名",
                                "テスト項目コード",
                                "テスト項目名",
                                "科目コード",
                                "科目名",
                                "得点",
                                "席次"
                                ));


        $objUp->setType(array(12 => 'N'));
        $objUp->setSize(array(12 => 3));
 
//        $objUp->setType(array("N","N","N","N","N","N","S","N","S","N","S","N","N","N"));
//        $objUp->setSize(array(3,8,8,20,3,6,2,15,2,30,2,60,3,3));

        //CSVファイルアップロードコントロール
        $arg["FILE"] = $objUp->toFileHtml($objForm);

        //ファイル操作実行ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_ok",
                            "value"       => "実  行",
                            "extrahtml"   => "onclick=\"return btn_submit('execute');\"" ));

        $arg["btn_ok"] = $objForm->ge("btn_ok");

        
        //更新ボタン
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_udpate",
                            "value"       => "更 新",
                            "extrahtml"   => $disabled." onclick=\"return btn_submit('update');\"" ) );

        $arg["btn_update"] = $objForm->ge("btn_udpate");

        //取消ボタン
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_can",
                            "value"       => "取 消",
                            "extrahtml"   => $disabled." onclick=\"return btn_submit('cancel');\"" ) );

        $arg["btn_can"] = $objForm->ge("btn_can");

        //終了ボタン
        $objForm->ae( array("type"        => "button",
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
                            "name"      => "perfect",
                            "value"      => $perfect
                            ) );


        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjd030index.php", "", "main");

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjd030Form1.html", $arg);
    }
}
?>
