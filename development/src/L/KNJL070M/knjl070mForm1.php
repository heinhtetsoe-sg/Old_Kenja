<?php

require_once('for_php7.php');

class knjl070mForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl070mindex.php", "", "main");
        $db           = Query::dbCheckOut();

        //ヘッダ
        $result = $db->query(knjl070mQuery::getName(array("L003","L004","L005"),$model->year));
        $opt = array();
        $opt["L003"] = array();
        $opt["L004"] = array();
        $opt_testdiv = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            switch($row["NAMECD1"]){
            case "L003":    //試験区分
                $opt[$row["NAMECD1"]][] = array("label"  =>  $row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]), "value"  => $row["NAMECD2"]);
                if ($model->applicantdiv=="" && $row["NAMESPARE2"]=='1') $model->applicantdiv = $row["NAMECD2"];
                break;
            case "L004":    //入試区分
                $opt[$row["NAMECD1"]][] = array("label"  =>  $row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]), "value"  => $row["NAMECD2"]);
                if ($model->testdiv=="" && $row["NAMESPARE2"]=='1') $model->testdiv = $row["NAMECD2"];
                $opt_testdiv[] = $row["NAMECD2"];
                break;
            }
        }

        if ($model->cmd == "main"){
            $Row = array("ENTEXAMYEAR"  => $model->entexamyear,
                        "APPLICANTDIV"  => $model->applicantdiv,
                        "TESTDIV"       => $model->testdiv,
                        "SHDIV"         => $model->shdiv,
                        "COURSE"        => $model->course,
                        "BORDER_SCORE"  => $model->field["BORDER_SCORE"],
                        "BACK_RATE"     => $model->field["BACK_RATE"],
                        "BORDER_SCORE_CANDI"  => $model->field["BORDER_SCORE_CANDI"]
                        );
        }else{
            //合格点マスタ
            $query = knjl070mQuery::selectQueryPassingmark($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }
        $arg["CTRL_YEAR"] = $model->year ."年度";

        $objForm->ae( array("type"       => "select",
                            "name"       => "APPLICANTDIV",
                            "size"       => "1",
                            "extrahtml"  => "onchange=\"return btn_submit('main')\"",
                            "value"      => $Row["APPLICANTDIV"],
                            "options"    => $opt["L003"]));
        $arg["APPLICANTDIV"] = $objForm->ge("APPLICANTDIV");

        $objForm->ae( array("type"       => "select",
                            "name"       => "TESTDIV",
                            "size"       => "1",
                            "extrahtml"  => "onchange=\"return btn_submit('main')\"",
                            "value"      => $Row["TESTDIV"],
                            "options"    => $opt["L004"]));
        $arg["TESTDIV"] = $objForm->ge("TESTDIV");

        //学科コース取得
        $result = $db->query(knjl070mQuery::selectQueryCourse($model));
        $opt = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $cd = $row["COURSECD"] .$row["MAJORCD"].$row["EXAMCOURSECD"];
            $opt[] = array("label"  =>  $cd .":" .htmlspecialchars($row["EXAMCOURSE_NAME"]),
                           "value"  => $cd);
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

        //候補点
        $readonly = ($model->applicantdiv == '1' || $model->applicantdiv == '2') ? "" : " readonly";
        $objForm->ae( array("type"        => "text",
                            "name"        => "BORDER_SCORE_CANDI",
                            "size"        => 5,
                            "maxlength"   => 3,
                            "extrahtml"   => "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"" .$readonly,
                            "value"       => $Row["BORDER_SCORE_CANDI"]
                            ));

        $arg["BORDER_SCORE_CANDI"] = $objForm->ge("BORDER_SCORE_CANDI");

        if ($model->cmd == "main"){
            //合格者取得
            $query = knjl070mQuery::selectQuerySuccess_cnt($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        
            $arg["SUCCESS_CNT"] = is_numeric($row["SUCCESS_CNT"]) ? $row["SUCCESS_CNT"] : 0;
            if (is_numeric($row["SUCCESS_CNT"]) && $row["SUCCESS_CNT"] > 0){
                //収容人数
                $arg["CAPA_CNT"]  = floor((float) $model->field["BACK_RATE"] * (float) $row["SUCCESS_CNT"]/100);
            }else{
                $arg["CAPA_CNT"]  = 0;
            }
            $arg["SUCCESS_CNT_CANDI"] = is_numeric($row["SUCCESS_CNT_CANDI"]) ? $row["SUCCESS_CNT_CANDI"] : 0;
        }else{
            //合格者数
            $arg["SUCCESS_CNT"]  = $Row["SUCCESS_CNT"];
            //収容人数
            $arg["CAPA_CNT"]  = $Row["CAPA_CNT"];
            //候補者数
            $arg["SUCCESS_CNT_CANDI"]  = $Row["SUCCESS_CNT_CANDI"];
        }
        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SUCCESS_CNT",
                            "value"      => $arg["SUCCESS_CNT"]
                            ) );
        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "CAPA_CNT",
                            "value"      => $arg["CAPA_CNT"]
                            ) );
        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SUCCESS_CNT_CANDI",
                            "value"      => $arg["SUCCESS_CNT_CANDI"]
                            ) );
        //戻り率
        $objForm->ae( array("type"        => "text",
                            "name"        => "BACK_RATE",
                            "size"        => 5,
                            "maxlength"   => 3,
                            "extrahtml"   => "style=\"text-align:right\" onchange=\"chg_rate()\" onblur=\"this.value=toInteger(this.value);\"",
                            "value"       => ((isset($Row["BACK_RATE"]))? $Row["BACK_RATE"] : 100)
                            ));

        $arg["BACK_RATE"] = $objForm->ge("BACK_RATE");

        //合格点取得
        $result = $db->query(knjl070mQuery::selectQueryPassingmarkAll($model));
        $arg["data"] = array();
        $data = $rowspan = $capacnt = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $cd = $row["COURSECD"] .$row["MAJORCD"].$row["EXAMCOURSECD"];
            $data[] = $row;
            if (!isset($rowspan[$cd])){
                $rowspan[$cd] = 1;
            }else{
                $rowspan[$cd]++;
            }
            if (!isset($capacnt[$cd])){
                $capacnt[$cd] = 0;
            }
            $capacnt[$cd] += (int) $row["CAPA_CNT2"];
        }
        $i = 1;
        $precd = "";
        $sum = array();
        foreach ($data as $k => $row){
            $cd = $row["COURSECD"] .$row["MAJORCD"].$row["EXAMCOURSECD"];
            //入試区分がコンボに存在しない場合はリンクを省く
            if (in_array($row["TESTDIV"], $opt_testdiv)){
                $row["BORDER_SCORE2"] = View::alink("knjl070mindex.php" ,$row["BORDER_SCORE2"],"",
                                        array("cmd"         => "edit",
                                            "ENTEXAMYEAR"   => $row["ENTEXAMYEAR"],
                                            "APPLICANTDIV"  => $row["APPLICANTDIV"],
                                            "TESTDIV"       => $row["TESTDIV"],
                                            "SHDIV"         => $row["SHDIV"],
                                            "COURSE"        => $cd
                                    ));
            }

            $row["ID"]  = $i;
            if ($precd != $cd && $rowspan[$cd]){ 
                $row["rowspan"] = $rowspan[$cd];
            }
            $row["INCREASE"] = $row["CAPACITY"]-$capacnt[$cd];
            $row["COURSE"] = $cd;
            $arg["data"][] = $row;

            foreach(array("CAPACITY",
                        "SUCCESS_CNT2",
                        "CAPA_CNT2",
                        "SUCCESS_CNT_CANDI2",
                        "INCREASE") as $v){

                if (!isset($sum[$v])){
                    $sum[$v] = 0;
                }
                if ($precd == $cd && ($v == "CAPACITY" || $v == "INCREASE")){ 
                    continue;
                }
                $sum[$v] += (int) $row[$v];
            }

            $i++;
            $precd = $cd;
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
                                "extrahtml"   => "onclick=\"closeWin();\"" ) );

        $arg["btn_end"]  = $objForm->ge("btn_end");

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl070mForm1.html", $arg); 
    }
}
?>
