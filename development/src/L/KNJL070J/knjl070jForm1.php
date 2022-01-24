<?php

require_once('for_php7.php');

class knjl070jForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl070jindex.php", "", "main");
        $db           = Query::dbCheckOut();

        //ヘッダ
        $opt = array();
        $opt["L004"] = array();
        $opt_testdiv = $opt_testdiv_name = array();
        $result = $db->query(knjl070jQuery::getName(array("L004"),$model->year));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            switch($row["NAMECD1"]){
            case "L004":    //入試区分
                $opt[$row["NAMECD1"]][] = array("label"  =>  $row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]), "value"  => $row["NAMECD2"]);
                if ($model->testdiv=="" && $row["NAMESPARE2"]=='1') $model->testdiv = $row["NAMECD2"];
                $opt_testdiv[] = $row["NAMECD2"];
                $opt_testdiv_name[$row["NAMECD2"]] = $row["NAME1"];
                break;
            }
        }

        if ($model->cmd == "main"){
            $Row = array("ENTEXAMYEAR"      => $model->entexamyear,
                         "TESTDIV"          => $model->testdiv,
                         "COURSE"           => $model->course,
                         "BORDER_DEVIATION" => $model->field["BORDER_DEVIATION"],
                         "BACK_RATE"        => $model->field["BACK_RATE"]
                        );
        }else{
            //合格点マスタ
            $query = knjl070jQuery::selectQueryPassingmark($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }
        $arg["CTRL_YEAR"] = $model->year ."年度";

        $objForm->ae( array("type"       => "select",
                            "name"       => "TESTDIV",
                            "size"       => "1",
                            "extrahtml"  => "onchange=\"return btn_submit('main')\"",
                            "value"      => $Row["TESTDIV"],
                            "options"    => $opt["L004"]));
        $arg["TESTDIV"] = $objForm->ge("TESTDIV");

        //学科コース取得
        $opt = array();
        $result = $db->query(knjl070jQuery::selectQueryCourse($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $cd = $row["COURSECD"] .$row["MAJORCD"].$row["EXAMCOURSECD"];
            $opt[] = array("label"  =>  $cd .":" .htmlspecialchars($row["EXAMCOURSE_NAME"]), "value"  => $cd);
        }
        $objForm->ae( array("type"       => "select",
                            "name"       => "COURSE",
                            "size"       => "1",
                            "extrahtml"  => "onchange=\"return btn_submit('main')\"",
                            "value"      => $Row["COURSE"],
                            "options"    => $opt));
        $arg["COURSE"] = $objForm->ge("COURSE");

        //合格点（判定偏差値）
        $objForm->ae( array("type"        => "text",
                            "name"        => "BORDER_DEVIATION",
                            "size"        => 5,
                            "maxlength"   => 4,
                            "extrahtml"   => "style=\"text-align:right\" onblur=\"this.value=toFloat(this.value);\"",
                            "value"       => $Row["BORDER_DEVIATION"]
                            ));
        $arg["BORDER_DEVIATION"] = $objForm->ge("BORDER_DEVIATION");

        if ($model->cmd == "main"){
            //合格者取得
            $query = knjl070jQuery::selectQuerySuccess_cnt($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            $arg["SUCCESS_CNT"] = is_numeric($row["SUCCESS_CNT"]) ? $row["SUCCESS_CNT"] : 0;
            if (is_numeric($row["SUCCESS_CNT"]) && $row["SUCCESS_CNT"] > 0){
                //収容人数
                $arg["CAPA_CNT"]  = floor((float) $model->field["BACK_RATE"] * (float) $row["SUCCESS_CNT"]/100);
            }else{
                $arg["CAPA_CNT"]  = 0;
            }
        }else{
            //合格者数
            $arg["SUCCESS_CNT"]  = $Row["SUCCESS_CNT"];
            //収容人数
            $arg["CAPA_CNT"]  = $Row["CAPA_CNT"];
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
        $result = $db->query(knjl070jQuery::selectQueryPassingmarkAll($model));
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
            $capacnt[$cd] += (int) $row["CAPA_CNT1"]+ (int) $row["CAPA_CNT2"];
        }
        $i = 1;
        $precd = "";
        $sum = array();
        foreach ($data as $k => $row){
            $cd = $row["COURSECD"] .$row["MAJORCD"].$row["EXAMCOURSECD"];
            //入試区分がコンボに存在しない場合はリンクを省く
            if (in_array($row["TESTDIV"], $opt_testdiv)){
                $row["BORDER_DEVIATION1"] = View::alink("knjl070jindex.php" ,$row["BORDER_DEVIATION1"],"",
                                      array("cmd"           => "edit",
                                            "ENTEXAMYEAR"   => $row["ENTEXAMYEAR"],
                                            "APPLICANTDIV"  => $row["APPLICANTDIV"],
                                            "TESTDIV"       => $row["TESTDIV"],
                                            "EXAM_TYPE"     => '1',
                                            "SHDIV"         => $row["SHDIV"],
                                            "COURSE"        => $cd
                                    ));

                $row["BORDER_DEVIATION2"] = View::alink("knjl070jindex.php" ,$row["BORDER_DEVIATION2"],"",
                                      array("cmd"           => "edit",
                                            "ENTEXAMYEAR"   => $row["ENTEXAMYEAR"],
                                            "APPLICANTDIV"  => $row["APPLICANTDIV"],
                                            "TESTDIV"       => $row["TESTDIV"],
                                            "EXAM_TYPE"     => '1',
                                            "SHDIV"         => $row["SHDIV"],
                                            "COURSE"        => $cd
                                    ));
            }

            $row["ID"]  = $i;
            if ($precd != $cd && $rowspan[$cd]){ 
                $row["rowspan"] = $rowspan[$cd];
            }
//            $row["INCREASE"] = $row["CAPACITY"]-$capacnt[$cd];
            $row["INCREASE"] = $row["CAPACITY"]-$row["CAPA_CNT2"];
            $row["COURSE"] = $cd;
            $row["TESTDIV_NAME"] = $opt_testdiv_name[$row["TESTDIV"]];
            $arg["data"][] = $row;

            foreach(array("CAPACITY",
                          "SUCCESS_CNT1",
                          "CAPA_CNT1",
                          "SUCCESS_CNT2",
                          "CAPA_CNT2",
                          "INCREASE") as $v){

                if (!isset($sum[$v])){
                    $sum[$v] = 0;
                }
//                if ($precd == $cd && ($v == "CAPACITY" || $v == "INCREASE")){ 
//                    continue;
//                }
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
        View::toHTML($model, "knjl070jForm1.html", $arg); 
    }
}
?>
