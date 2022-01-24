<?php

require_once('for_php7.php');

class knjl100kForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl100kindex.php", "", "main");
        $db           = Query::dbCheckOut();

        //ヘッダ
        $result = $db->query(knjl100kQuery::getName(array("L003","L006"),$model->year));
        $opt = array();
        $opt["L003"] = array();
        $opt["L006"] = array();

        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[$row["NAMECD1"]][] = array("label"  =>  $row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]), "value"  => $row["NAMECD2"]);

            switch($row["NAMECD1"]){
            case "L003":    //試験区分
                if (!$model->testdiv){
                    $model->testdiv = $row["NAMECD2"];
                }
                break;
            case "L006":    //専
                if (!$model->shdiv){
                    $model->shdiv = $row["NAMECD2"];
                }
                break;
            }
        }
        if ($model->cmd == "main"){
            $Row = array("ENTEXAMYEAR"      => $model->entexamyear,
                        "TESTDIV"           => $model->testdiv,
                        "SHDIV"             => $model->shdiv,
                        "COURSE"            => $model->course,
                        "BORDER_SCORE"      => $model->field["BORDER_SCORE"],
                        "A_BORDER_SCORE"    => $model->field["A_BORDER_SCORE"],
                        "B_BORDER_SCORE"    => $model->field["B_BORDER_SCORE"],
                        "BACK_RATE"         => $model->field["BACK_RATE"]
                        );
        }else{
            //合格点マスタ
            $query = knjl100kQuery::selectQueryPassingmark($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }
        $arg["CTRL_YEAR"] = $model->year ."年度";

        //試験区分
        $objForm->ae( array("type"       => "select",
                            "name"       => "TESTDIV",
                            "size"       => "1",
                            "extrahtml"  => "onchange=\"return btn_submit('main')\"",
                            "value"      => $Row["TESTDIV"],
                            "options"    => $opt["L003"]));

        $arg["TESTDIV"] = $objForm->ge("TESTDIV");

        //専併区分
        $objForm->ae( array("type"       => "select",
                            "name"       => "SHDIV",
                            "size"       => "1",
                            "extrahtml"  => "onchange=\"return btn_submit('main')\"",
                            "value"      => $Row["SHDIV"],
                            "options"    => $opt["L006"]));

        $arg["SHDIV"] = $objForm->ge("SHDIV");

        //学科コース取得
        $result = $db->query(knjl100kQuery::selectQueryCourse($model));
        $opt = array();
        $model->opt_course = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $cd = $row["COURSECD"] .$row["MAJORCD"].$row["EXAMCOURSECD"];
            $opt[] = array("label"  =>  $cd .":" .htmlspecialchars($row["EXAMCOURSE_NAME"]),
                           "value"  => $cd);
            $model->opt_course[] = $cd;
        }
        $objForm->ae( array("type"       => "select",
                            "name"       => "COURSE",
                            "size"       => "1",
                            "extrahtml"  => "onchange=\"return btn_submit('main')\"",
                            "value"      => $Row["COURSE"],
                            "options"    => $opt));

        $arg["COURSE"] = $objForm->ge("COURSE");

        //合格点
        $objForm->ae( array("type"        => "text",
                            "name"        => "BORDER_SCORE",
                            "size"        => 5,
                            "maxlength"   => 3,
                            "extrahtml"   => "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"",
                            "value"       => $Row["BORDER_SCORE"]
                            ));

        $arg["BORDER_SCORE"] = $objForm->ge("BORDER_SCORE");

        //Ａ判定
        $objForm->ae( array("type"        => "text",
                            "name"        => "A_BORDER_SCORE",
                            "size"        => 5,
                            "maxlength"   => 3,
                            "extrahtml"   => "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"",
                            "value"       => $Row["A_BORDER_SCORE"]
                            ));

        $arg["A_BORDER_SCORE"] = $objForm->ge("A_BORDER_SCORE");

        //Ｂ判定
        $objForm->ae( array("type"        => "text",
                            "name"        => "B_BORDER_SCORE",
                            "size"        => 5,
                            "maxlength"   => 3,
                            "extrahtml"   => "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"",
                            "value"       => $Row["B_BORDER_SCORE"]
                            ));

        $arg["B_BORDER_SCORE"] = $objForm->ge("B_BORDER_SCORE");

        if ($model->cmd == "main"){

            //合格者取得
            $query = knjl100kQuery::selectQuerySuccess_cnt($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            $arg["SUCCESS_CNT"]   = (int) $row["SUCCESS_CNT"];                 //合計      #2005/09/12 arakaki
            $arg["R_SUCCESS_CNT"] = (int) $row["R_SUCCESS_CNT"];               //基本      #2005/09/12 arakaki
            $arg["A_SUCCESS_CNT"] = (int) $row["A_SUCCESS_CNT"];               //A判定     #2005/09/12 arakaki
            $arg["B_SUCCESS_CNT"] = (int) $row["B_SUCCESS_CNT"];               //B判定     #2005/09/12 arakaki

            $arg["R_SUCCESS_SEX1_CNT"] = (int) $row["R_SUCCESS_SEX1_CNT"];     //基本男    #2005/12/27 arakaki
            $arg["R_SUCCESS_SEX2_CNT"] = (int) $row["R_SUCCESS_SEX2_CNT"];     //基本女    #2005/12/27 arakaki
            $arg["A_SUCCESS_SEX1_CNT"] = (int) $row["A_SUCCESS_SEX1_CNT"];     //A判定男   #2005/12/27 arakaki
            $arg["A_SUCCESS_SEX2_CNT"] = (int) $row["A_SUCCESS_SEX2_CNT"];     //A判定女   #2005/12/27 arakaki
            $arg["B_SUCCESS_SEX1_CNT"] = (int) $row["B_SUCCESS_SEX1_CNT"];     //B判定男   #2005/12/27 arakaki
            $arg["B_SUCCESS_SEX2_CNT"] = (int) $row["B_SUCCESS_SEX2_CNT"];     //B判定女   #2005/12/27 arakaki

            if (is_numeric($row["SUCCESS_CNT"]) && $row["SUCCESS_CNT"] > 0){
                //収容人数
#                $arg["CAPA_CNT"]  = floor((float) $model->field["BACK_RATE"] * (float) $row["SUCCESS_CNT"]/100);
                $arg["CAPA_CNT"]  = round((float) $model->field["BACK_RATE"] * (float) $row["SUCCESS_CNT"]/100);    #2005/11/10 arakaki

            }else{
                $arg["CAPA_CNT"]  = 0;
            }
        }else{
            //合格者数
            $arg["SUCCESS_CNT"]   = (int) $Row["A_SUCCESS_CNT"]+(int) $Row["B_SUCCESS_CNT"]+(int) $Row["SUCCESS_CNT"];
            $arg["R_SUCCESS_CNT"] = (int) $Row["SUCCESS_CNT"];       //基本      #2005/09/12 arakaki
            $arg["A_SUCCESS_CNT"] = (int) $Row["A_SUCCESS_CNT"];     //A判定     #2005/09/12 arakaki
            $arg["B_SUCCESS_CNT"] = (int) $Row["B_SUCCESS_CNT"];     //B判定     #2005/09/12 arakaki

            $arg["R_SUCCESS_SEX1_CNT"] = (int) $Row["SUCCESS_SEX1_CNT"];       //基本男    #2005/12/27 arakaki
            $arg["R_SUCCESS_SEX2_CNT"] = (int) $Row["SUCCESS_SEX2_CNT"];       //基本女    #2005/12/27 arakaki
            $arg["A_SUCCESS_SEX1_CNT"] = (int) $Row["A_SUCCESS_SEX1_CNT"];     //A判定男   #2005/12/27 arakaki
            $arg["A_SUCCESS_SEX2_CNT"] = (int) $Row["A_SUCCESS_SEX2_CNT"];     //A判定女   #2005/12/27 arakaki
            $arg["B_SUCCESS_SEX1_CNT"] = (int) $Row["B_SUCCESS_SEX1_CNT"];     //B判定男   #2005/12/27 arakaki
            $arg["B_SUCCESS_SEX2_CNT"] = (int) $Row["B_SUCCESS_SEX2_CNT"];     //B判定女   #2005/12/27 arakaki

            //収容人数
            $arg["CAPA_CNT"]  = $Row["CAPA_CNT"];
        }
        //合格者数hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SUCCESS_CNT",
                            "value"     => $arg["SUCCESS_CNT"]) );
        //収容人数hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "CAPA_CNT",
                            "value"     => $arg["CAPA_CNT"]) );


        //判定項目ラジオボタン
        $objForm->ae( array("type"        => "radio",
                            "name"        => "JUDGE_COL",
                            "value"       => $model->judge_col ));

        $arg["A_JUDGE_COL"] = $objForm->ge("JUDGE_COL","1");
        $arg["B_JUDGE_COL"] = $objForm->ge("JUDGE_COL","2");

        //戻り率
        $objForm->ae( array("type"        => "text",
                            "name"        => "BACK_RATE",
                            "size"        => 5,
                            "maxlength"   => 3,
                            "extrahtml"   => "style=\"text-align:right\" onchange=\"chg_rate()\" onblur=\"this.value=toInteger(this.value);\"",
                            "value"       => ((isset($Row["BACK_RATE"]))? $Row["BACK_RATE"] : 100)
                            ));

        $arg["BACK_RATE"] = $objForm->ge("BACK_RATE");

        /* 2005/02/07 reprocess_flgが1の場合,その背景をピンクにする */
        //合格点取得
        
        $result = $db->query(knjl100kQuery::selectQueryPassingmarkAll($model));
        $arg["data"] = array();
        $sum = array();
        $reprocess_flg = "";  //再計算フラグ
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($row["REPROCESS_FLG1"] == "1" || $row["REPROCESS_FLG2"] == "1")
                 $reprocess_flg = "1";

            $row["BGCOLOR1"] = ($row["REPROCESS_FLG1"] == "1" ? "pink" : "#FFFFFF");
            $row["BGCOLOR2"] = ($row["REPROCESS_FLG2"] == "1" ? "pink" : "#FFFFFF");

            $cd = $row["COURSECD"] .$row["MAJORCD"].$row["EXAMCOURSECD"];
            $row["COURSE"] = $cd;
            $row["BORDER_SCORE1"] = View::alink("knjl100kindex.php" ,$row["BORDER_SCORE1"],"",
                                    array("cmd"         => "edit",
                                        "ENTEXAMYEAR"   => $row["ENTEXAMYEAR"],
                                        "TESTDIV"       => $row["TESTDIV"],
                                        "SHDIV"         => '1',
                                        "JUDGE_COL"     => $row["JUDGE_COL1"],
                                        "COURSE"        => $cd
                                ));

            $row["BORDER_SCORE2"] = View::alink("knjl100kindex.php" ,$row["BORDER_SCORE2"],"",
                                    array("cmd"         => "edit",
                                        "ENTEXAMYEAR"   => $row["ENTEXAMYEAR"],
                                        "TESTDIV"       => $row["TESTDIV"],
                                        "SHDIV"         => '2',
                                        "JUDGE_COL"     => $row["JUDGE_COL2"],
                                        "COURSE"        => $cd
                                ));

            $row["SUCCESS_CNT1"] = (int) $row["A_SUCCESS_CNT1"]+(int) $row["B_SUCCESS_CNT1"]+(int) $row["SUCCESS_CNT1"];
            $row["SUCCESS_CNT2"] = (int) $row["A_SUCCESS_CNT2"]+(int) $row["B_SUCCESS_CNT2"]+(int) $row["SUCCESS_CNT2"];
            #$row["INCREASE"] = (int) $row["CAPACITY"] - ((int) $row["S_SUCCESS_CNT"]+(int) $row["CAPA_CNT1"]+(int) $row["CAPA_CNT2"]); //2005/11/01 by ameku
//            $row["INCREASE"] = ((int) $row["S_SUCCESS_CNT"]+(int) $row["CAPA_CNT1"]+(int) $row["CAPA_CNT2"])-(int) $row["CAPACITY"];   #2005/12/27
            $row["INCREASE"] = ((int) $row["CAPA_CNT1"]+(int) $row["CAPA_CNT2"])-(int) $row["CAPACITY"];   //2006.02.11 alp m-yama
            $row["JUDGE_COL1"] = ($row["JUDGE_COL1"] == "1")? "配点A":(($row["JUDGE_COL1"] == "2")? "配点B":"");
            $row["JUDGE_COL2"] = ($row["JUDGE_COL2"] == "1")? "配点A":(($row["JUDGE_COL2"] == "2")? "配点B":"");
            foreach(array("CAPACITY",
                        "S_SUCCESS_CNT",
                        "SUCCESS_CNT1",
                        "CAPA_CNT1",
                        "SUCCESS_CNT2",
                        "CAPA_CNT2",
                        "INCREASE") as $v){

                if (!isset($sum[$v])){
                    $sum[$v] = 0;
                }
                $sum[$v] += (int) $row[$v];
            }
            $arg["data"][] = $row;
        }
        //合計出力
        $arg["sum"] = $sum;

        Query::dbCheckIn($db);

        //シミュレーションボタン作成
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_sim",
                            "value"       => "シミュレーション",
                            "extrahtml"   => "onclick=\"return btn_submit('sim')\"" ) );

        $arg["btn_sim"]  = $objForm->ge("btn_sim");

        //確定ボタン作成
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_decision",
                            "value"       => "確 定",
                            "extrahtml"   => "onclick=\"return btn_submit('decision')\"" ) );

        $arg["btn_decision"]  = $objForm->ge("btn_decision");

        //終了ボタン作成
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"return btn_submit('close')\"" ) );

        $arg["btn_end"]  = $objForm->ge("btn_end");

        //チェックリストボタン  alp m-yama 2005/08/28
        $db = Query::dbCheckOut();
        $row = $db->getOne(knjl100kQuery::GetJorH());
        if ($row == 0){
            $objForm->ae( array("type"      => "button",
                                "name"      => "btn_check",
                                "value"     => "得点分布表出力",
                                "size"      => "",
                                "extrahtml" => " onClick=\" wopen('../KNJL324K/knjl324kindex.php?,','SUBWIN2',0,0,screen.availWidth,screen.availheight);\"" ));

            $arg["button"]["BTN_CHECK"] = $objForm->ge("btn_check");
        }

        Query::dbCheckIn($db);

        //結果一覧  alp m-yama 2006/02/04
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_result",
                            "value"     => "合否判定表",
                            "size"      => "",
                            "extrahtml" => " onClick=\" wopen('../KNJL362K/knjl362kindex.php?,','',0,0,screen.availWidth,screen.availheight);\"" ));

        $arg["button"]["BTN_RESULT"] = $objForm->ge("btn_result");

        //通知発行Ｎｏ採番
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_notice",
                            "value"       => "通知発行No.採番",
                            "extrahtml"   => "onclick=\"return btn_submit('notice')\"" ) );

        $arg["btn_notice"]  = $objForm->ge("btn_notice");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        //終了時の再計算必要メッセージ
        if (VARS::post("cmd") == "close") {

            $value = "";
            if ($model->msg != "") {
                $value = "\n未処理のデータが存在します。\n\nバックカラーが赤の学科・コースは再処理が必要です。\n\n＜対象＞\n\n".$model->msg;
            }
            $objForm->ae( array("type"      => "hidden",
                                "name"      => "msg",
                                "value"     => $value ) );
        }

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl100kForm1.html", $arg); 
    }
}
?>
