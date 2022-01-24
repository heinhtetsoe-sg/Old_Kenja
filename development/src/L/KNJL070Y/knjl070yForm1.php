<?php

require_once('for_php7.php');

class knjl070yForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl070yindex.php", "", "main");
        $db           = Query::dbCheckOut();

        //ヘッダ
        $result = $db->query(knjl070yQuery::getName(array("L003","L004","L006"),$model->year));
        $opt = array();
        $opt["L004"] = array();
        $opt_testdiv = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            switch ($row["NAMECD1"]) {
            case "L003":    //入試制度
                if ($row["NAMECD2"] == "1") break; //1:中学は表示しない
                $opt[$row["NAMECD1"]][] = array("label"  =>  $row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]), "value"  => $row["NAMECD2"]);
                if ($model->applicantdiv=="" && $row["NAMESPARE2"]=='1') $model->applicantdiv = $row["NAMECD2"];
                break;
            case "L004":    //入試区分
                if ($row["NAMECD2"] == "2") break; //2:推薦は表示しない
                $opt[$row["NAMECD1"]][] = array("label"  =>  $row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]), "value"  => $row["NAMECD2"]);
                if ($model->testdiv=="" && $row["NAMESPARE2"]=='1') $model->testdiv = $row["NAMECD2"];
                $opt_testdiv[] = $row["NAMECD2"];
                break;
            case "L006":    //専併区分
                $opt[$row["NAMECD1"]][] = array("label"  =>  $row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]), "value"  => $row["NAMECD2"]);
                break;
            }
        }
        if ($model->applicantdiv=="") $model->applicantdiv = $opt["L003"][0]["value"];
        if ($model->testdiv=="")      $model->testdiv      = $opt["L004"][0]["value"];
        if ($model->shdiv=="")        $model->shdiv        = $opt["L006"][0]["value"];
        if ($model->kikoku=="")       $model->kikoku       = "1";

        if ($model->cmd == "main"){
            $Row = array("ENTEXAMYEAR"  => $model->entexamyear,
                        "APPLICANTDIV"  => $model->applicantdiv,
                        "TESTDIV"       => $model->testdiv,
                        "SHDIV"         => $model->shdiv,
                        "COURSE"        => $model->course,
                        "KIKOKU"        => $model->kikoku,
                        "BORDER_SCORE"  => $model->field["BORDER_SCORE"],
                        "BORDER_DEVIATION"  => $model->field["BORDER_DEVIATION"],
                        "BACK_RATE"     => $model->field["BACK_RATE"],
                        "BORDER_SCORE_CANDI"  => $model->field["BORDER_SCORE_CANDI"]
                        );
        }else{
            //合格点マスタ
            $query = knjl070yQuery::selectQueryPassingmark($model);
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

        //スライドは「1:学特」のみ表示する
        if ($Row["TESTDIV"] == "1") {
            $arg["isSlide"] = 1;
        } else {
            $arg["isSlideNo"] = 1;
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "SHDIV",
                            "size"       => "1",
                            "extrahtml"  => "onchange=\"return btn_submit('main')\"",
                            "value"      => $Row["SHDIV"],
                            "options"    => $opt["L006"]));
        $arg["SHDIV"] = $objForm->ge("SHDIV");

        //学科コース取得
        $result = $db->query(knjl070yQuery::selectQueryCourse($model));
        $opt = array();
        $optCapacity = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $cd = $row["COURSECD"] .$row["MAJORCD"].$row["EXAMCOURSECD"];
            $opt[] = array("label"  =>  $cd .":" .htmlspecialchars($row["EXAMCOURSE_NAME"]),
                           "value"  => $cd);
            $optCapacity[$cd] = $row["CAPACITY"];//定員
        }
        $objForm->ae( array("type"       => "select",
                            "name"       => "COURSE",
                            "size"       => "1",
                            "extrahtml"  => "onchange=\"return btn_submit('main')\"",
                            "value"      => $Row["COURSE"],
                            "options"    => $opt));

        $arg["COURSE"] = $objForm->ge("COURSE");

        //対象者(帰国生)ラジオボタン 1:帰国生除く 2:帰国生のみ
        //$Row["KIKOKU"] = ($Row["KIKOKU"]) ? $Row["KIKOKU"] : "1";
        $opt_kikoku = array(1, 2);
        $extra = array("id=\"KIKOKU1\" onclick=\"btn_submit('main');\"", "id=\"KIKOKU2\" onclick=\"btn_submit('main');\"");
        $radioArray = knjCreateRadio($objForm, "KIKOKU", $Row["KIKOKU"], $extra, $opt_kikoku, get_count($opt_kikoku));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

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

        //評定合計
        $objForm->ae( array("type"        => "text",
                            "name"        => "BORDER_DEVIATION",
                            "size"        => 5,
                            "maxlength"   => 3,
                            "extrahtml"   => "style=\"text-align:right\" onblur=\"this.value=toFloat(this.value);\"",
                            "value"       => $Row["BORDER_DEVIATION"]
                            ));
        $arg["BORDER_DEVIATION"] = $objForm->ge("BORDER_DEVIATION");

        if ($model->cmd == "main"){
            //合格者取得
            $query = knjl070yQuery::selectQuerySuccess_cnt($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        
            $arg["SUCCESS_CNT"] = is_numeric($row["SUCCESS_CNT"]) ? $row["SUCCESS_CNT"] : 0;
            if (is_numeric($row["SUCCESS_CNT"]) && $row["SUCCESS_CNT"] > 0){
                //収容人数
                $arg["CAPA_CNT"]  = floor((float) $model->field["BACK_RATE"] * (float) $row["SUCCESS_CNT"]/100);
            }else{
                $arg["CAPA_CNT"]  = 0;
            }
            $arg["SUCCESS_CNT_CANDI"] = is_numeric($row["SUCCESS_CNT_CANDI"]) ? $row["SUCCESS_CNT_CANDI"] : 0;
            $arg["SUCCESS_CNT_SPECIAL"] = is_numeric($row["SUCCESS_CNT_SPECIAL"]) ? $row["SUCCESS_CNT_SPECIAL"] : 0;
            $arg["SUCCESS_CNT_SPECIAL2"] = is_numeric($row["SUCCESS_CNT_SPECIAL2"]) ? $row["SUCCESS_CNT_SPECIAL2"] : 0;
            $arg["SUCCESS_CNT_CHALLENGE"] = is_numeric($row["SUCCESS_CNT_CHALLENGE"]) ? $row["SUCCESS_CNT_CHALLENGE"] : 0;
        }else{
            //合格者数
            $arg["SUCCESS_CNT"]  = $Row["SUCCESS_CNT"];
            //収容人数
            $arg["CAPA_CNT"]  = $Row["CAPA_CNT"];
            //スライド合格者数
            $arg["SUCCESS_CNT_CANDI"] = is_numeric($Row["SUCCESS_CNT_CANDI"]) ? $Row["SUCCESS_CNT_CANDI"] : 0;
            //特別判定合格者数
            $arg["SUCCESS_CNT_SPECIAL"] = is_numeric($Row["SUCCESS_CNT_SPECIAL"]) ? $Row["SUCCESS_CNT_SPECIAL"] : 0;
            $arg["SUCCESS_CNT_SPECIAL2"] = is_numeric($Row["SUCCESS_CNT_SPECIAL2"]) ? $Row["SUCCESS_CNT_SPECIAL2"] : 0;
            //特進チャレンジ合格者数
            $arg["SUCCESS_CNT_CHALLENGE"] = is_numeric($Row["SUCCESS_CNT_CHALLENGE"]) ? $Row["SUCCESS_CNT_CHALLENGE"] : 0;
        }
        /**************/
        /* 入学予想数 */
        /**************/
        //入学予想数=合格者数*定着率/100
        if (is_numeric($arg["SUCCESS_CNT"]) && 0 < $arg["SUCCESS_CNT"]) {
            $percent = (float) $arg["SUCCESS_CNT"] * (float) $Row["BACK_RATE"] / 100;
            $arg["PERCENT"] = sprintf("%01.1f", round($percent, 1));
        } else {
            $percent = 0.0;
            $arg["PERCENT"] = sprintf("%01.1f", round($percent, 1));
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
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SUCCESS_CNT_SPECIAL",
                            "value"      => $arg["SUCCESS_CNT_SPECIAL"]
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SUCCESS_CNT_SPECIAL2",
                            "value"      => $arg["SUCCESS_CNT_SPECIAL2"]
                            ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SUCCESS_CNT_CHALLENGE",
                            "value"      => $arg["SUCCESS_CNT_CHALLENGE"]
                            ) );
        //戻り率
        $objForm->ae( array("type"        => "text",
                            "name"        => "BACK_RATE",
                            "size"        => 5,
                            "maxlength"   => 3,
//                            "extrahtml"   => "style=\"text-align:right\" onchange=\"chg_rate()\" onblur=\"this.value=toInteger(this.value);\"",
                            "extrahtml"   => "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"",
                            "value"       => ((isset($Row["BACK_RATE"]))? $Row["BACK_RATE"] : 100)
                            ));

        $arg["BACK_RATE"] = $objForm->ge("BACK_RATE");

        //合格点取得
        $result = $db->query(knjl070yQuery::selectQueryPassingmarkAll($model));
        $arg["data"] = array();
        $data = $rowspan = $capacnt = $sum = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $cd = $row["COURSECD"] .$row["MAJORCD"].$row["EXAMCOURSECD"];
            //入試区分がコンボに存在しない場合はリンクを省く
            if (in_array($row["TESTDIV"], $opt_testdiv)){
                $row["BORDER_SCORE2_SHDIV1"] = View::alink("knjl070yindex.php" ,$row["BORDER_SCORE2_SHDIV1"],"",
                                        array("cmd"         => "edit",
                                            "ENTEXAMYEAR"   => $row["ENTEXAMYEAR"],
                                            "APPLICANTDIV"  => $row["APPLICANTDIV"],
                                            "TESTDIV"       => $row["TESTDIV"],
                                            "SHDIV"         => $row["SHDIV1"],
                                            "COURSE"        => $cd,
                                            "KIKOKU"        => $row["KIKOKU"]
                                    ));
                $row["BORDER_SCORE2_SHDIV2"] = View::alink("knjl070yindex.php" ,$row["BORDER_SCORE2_SHDIV2"],"",
                                        array("cmd"         => "edit",
                                            "ENTEXAMYEAR"   => $row["ENTEXAMYEAR"],
                                            "APPLICANTDIV"  => $row["APPLICANTDIV"],
                                            "TESTDIV"       => $row["TESTDIV"],
                                            "SHDIV"         => $row["SHDIV2"],
                                            "COURSE"        => $cd,
                                            "KIKOKU"        => $row["KIKOKU"]
                                    ));
            }
            $row["COURSE"] = $cd;
            $row["KIKOKU_NAME"] = ($row["KIKOKU"] == "2") ? "(帰国生)" : "";
            $success_cnt2_shdiv1 = strlen($row["SUCCESS_CNT2_SHDIV1"]) ? $row["SUCCESS_CNT2_SHDIV1"] : 0;
            $success_cnt2_shdiv2 = strlen($row["SUCCESS_CNT2_SHDIV2"]) ? $row["SUCCESS_CNT2_SHDIV2"] : 0;
            $row["SUCCESS_CNT2_TOTAL"] = $success_cnt2_shdiv1 + $success_cnt2_shdiv2;
            //スライド合格者数
            $success_cnt_candi2_shdiv1 = strlen($row["SUCCESS_CNT_CANDI2_SHDIV1"]) ? $row["SUCCESS_CNT_CANDI2_SHDIV1"] : 0;
            $success_cnt_candi2_shdiv2 = strlen($row["SUCCESS_CNT_CANDI2_SHDIV2"]) ? $row["SUCCESS_CNT_CANDI2_SHDIV2"] : 0;
            $row["SUCCESS_CNT_CANDI2_TOTAL"] = $success_cnt_candi2_shdiv1 + $success_cnt_candi2_shdiv2;
            //特別判定合格者数
            $success_cnt_special_shdiv1 = strlen($row["SUCCESS_CNT_SPECIAL_SHDIV1"]) ? $row["SUCCESS_CNT_SPECIAL_SHDIV1"] : 0;
            $success_cnt_special_shdiv2 = strlen($row["SUCCESS_CNT_SPECIAL_SHDIV2"]) ? $row["SUCCESS_CNT_SPECIAL_SHDIV2"] : 0;
            $row["SUCCESS_CNT_SPECIAL_TOTAL"] = $success_cnt_special_shdiv1 + $success_cnt_special_shdiv2;
            //特進チャレンジ合格者数
            $row["SUCCESS_CNT_CHALLENGE"] = strlen($row["SUCCESS_CNT_CHALLENGE"]) ? $row["SUCCESS_CNT_CHALLENGE"] : 0;
            /**************/
            /* 入学予想数 */
            /**************/
            //入学予想数=合格者数*定着率/100
            //1:専願
            if ($row["SHDIV1"] == "1") {
                $percent = 0.0;
                $row["PERCENT_SHDIV1"] = sprintf("%01.1f", round($percent, 1));
            }
            if (is_numeric($row["SUCCESS_CNT2_SHDIV1"]) && 0 < $row["SUCCESS_CNT2_SHDIV1"]) {
                $percent = (float) $row["SUCCESS_CNT2_SHDIV1"] * (float) $row["BACK_RATE_SHDIV1"] / 100;
                $row["PERCENT_SHDIV1"] = sprintf("%01.1f", round($percent, 1));
            }
            //2:併願
            if ($row["SHDIV2"] == "2") {
                $percent = 0.0;
                $row["PERCENT_SHDIV2"] = sprintf("%01.1f", round($percent, 1));
            }
            if (is_numeric($row["SUCCESS_CNT2_SHDIV2"]) && 0 < $row["SUCCESS_CNT2_SHDIV2"]) {
                $percent = (float) $row["SUCCESS_CNT2_SHDIV2"] * (float) $row["BACK_RATE_SHDIV2"] / 100;
                $row["PERCENT_SHDIV2"] = sprintf("%01.1f", round($percent, 1));
            }
            $arg["data"][] = $row;

            foreach (array("SUCCESS_CNT2_SHDIV1",
                           "SUCCESS_CNT2_SHDIV2",
                           "SUCCESS_CNT2_TOTAL",
                           "SUCCESS_CNT_CANDI2_TOTAL",
                           "SUCCESS_CNT_SPECIAL_TOTAL",
                           "SUCCESS_CNT_CHALLENGE") as $v) {

                if (!isset($sum[$v])) {
                    $sum[$v] = 0;
                }

                $sum[$v] += (int) $row[$v];
            }
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
        View::toHTML($model, "knjl070yForm1.html", $arg); 
    }
}
?>
