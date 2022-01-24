<?php

require_once('for_php7.php');

class knjl070oForm1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjl070oindex.php", "", "main");
        $db = Query::dbCheckOut();

        //ヘッダ
        $result = $db->query(knjl070oQuery::getName(array("L003", "L005", "L064"),$model->year));
        $opt = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            switch($row["NAMECD1"]){
            case "L003":    //試験区分
                $opt[$row["NAMECD1"]][] = array("label"  =>  $row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]), "value"  => $row["NAMECD2"]);
                if (!strlen($model->applicantdiv)) {
                    $model->applicantdiv = $row["NAMECD2"];
                }
                break;
            case "L005":    //判定対象
                if ($row["NAMECD2"] == '2') {
                    $opt[$row["NAMECD1"]][] = array("label"  =>  $row["NAMECD2"] .":" .htmlspecialchars($row["NAME2"]), "value"  => $row["NAMECD2"]);
                    if (!strlen($model->exam_type)) {
                        $model->exam_type = $row["NAMECD2"];
                    }
                }
                break;
            case "L064":    //試験区分
                $opt[$row["NAMECD1"]][] = array("label"  =>  $row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]), "value"  => $row["NAMECD2"]);
                if (!strlen($model->shDiv)) {
                    $model->shDiv = $row["NAMECD2"];
                }
                break;
            }
        }

        //入試区分
        $opt["L004"] = array();
        $opt_testdiv = array();
        $result = $db->query(knjl070oQuery::getTestdivMst($model->year));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if($row["NAMESPARE2"]=='1') {
                $opt["L004"][] = array("label"  =>  $row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]), "value"  => $row["NAMECD2"]);
                if (!strlen($model->testdiv)){
                    $model->testdiv = $row["NAMECD2"];
                }
                $opt_testdiv[] = $row["NAMECD2"];
            }
        }

        if ($model->cmd == "main"){
            $Row = array("ENTEXAMYEAR"  => $model->entexamyear,
                        "APPLICANTDIV"  => $model->applicantdiv,
                        "TESTDIV"       => $model->testdiv,
                        "EXAM_TYPE"     => $model->exam_type,
                        "SHDIV"         => $model->shdiv,
                        "COURSE"        => $model->course,
                        "BORDER_SCORE"  => $model->field["BORDER_SCORE"],
                        "BACK_RATE"     => $model->field["BACK_RATE"]
                        );
        } else {
            //合格点マスタ
            $query = knjl070oQuery::selectQueryPassingmark($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        }
        $arg["CTRL_YEAR"] = $model->year ."年度";

        //combobox
        $extra = "onchange=\"return btn_submit('main')\"";
        $arg["APPLICANTDIV"] = knjCreateCombo($objForm, "APPLICANTDIV", $Row["APPLICANTDIV"], $opt["L003"], $extra, 1);

        $extra = "onchange=\"return btn_submit('main')\"";
        $arg["TESTDIV"] = knjCreateCombo($objForm, "TESTDIV", $Row["TESTDIV"], $opt["L004"], $extra, 1);

        $extra = "onchange=\"return btn_submit('main')\"";
        $arg["EXAM_TYPE"] = knjCreateCombo($objForm, "EXAM_TYPE", $Row["EXAM_TYPE"], $opt["L005"], $extra, 1);

        $extra = "onchange=\"return btn_submit('main')\"";
        $arg["SHDIV"] = knjCreateCombo($objForm, "SHDIV", $Row["SHDIV"], $opt["L064"], $extra, 1);

        //学科コース取得
        $result = $db->query(knjl070oQuery::selectQueryCourse($model));
        $opt = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $cd = $row["COURSECD"] .$row["MAJORCD"].$row["EXAMCOURSECD"];
            $opt[] = array("label"  =>  $cd .":" .htmlspecialchars($row["EXAMCOURSE_NAME"]),
                           "value"  => $cd);
        }
        $extra = "onchange=\"return btn_submit('main')\"";
        $arg["COURSE"] = knjCreateCombo($objForm, "COURSE", $Row["COURSE"], $opt, $extra, 1);

        //合格点
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["BORDER_SCORE"] = knjCreateTextBox($objForm, $Row["BORDER_SCORE"], "BORDER_SCORE", 5, 3, $extra);

        if ($model->cmd == "main") {
            //合格者取得
            $query = knjl070oQuery::selectQuerySuccess_cnt($model);
            $row = $db->getRow($query, DB_FETCHMODE_ASSOC);

            $arg["SUCCESS_CNT"] = $row["SUCCESS_CNT"];
            if (is_numeric($row["SUCCESS_CNT"]) && $row["SUCCESS_CNT"] > 0) {
                //収容人数
                $arg["CAPA_CNT"]  = floor((float) $model->field["BACK_RATE"] * (float) $row["SUCCESS_CNT"]/100);
            } else {
                $arg["CAPA_CNT"]  = 0;
            }
        } else {
            //合格者数
            $arg["SUCCESS_CNT"]  = $Row["SUCCESS_CNT"];
            //収容人数
            $arg["CAPA_CNT"]  = $Row["CAPA_CNT"];
        }

        //hiddenを作成する
        knjCreateHidden($objForm, "SUCCESS_CNT", $arg["SUCCESS_CNT"]);
        knjCreateHidden($objForm, "CAPA_CNT", $arg["CAPA_CNT"]);

        //戻り率
        $extra = "style=\"text-align:right\" onchange=\"chg_rate()\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["BACK_RATE"] = knjCreateTextBox($objForm, ((isset($Row["BACK_RATE"]))? $Row["BACK_RATE"] : 100), "BACK_RATE", 5, 3, $extra);

        //合格点取得
        $result = $db->query(knjl070oQuery::selectQueryPassingmarkAll($model));
        $arg["data"] = array();
        $data = $rowspan = $capacnt = $capacity = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $cd = $row["COURSECD"] .$row["MAJORCD"].$row["EXAMCOURSECD"];
            $data[] = $row;
            if (!isset($rowspan[$cd])) {
                $rowspan[$cd] = 1;
            } else {
                $rowspan[$cd]++;
            }
            if (!isset($capacnt[$cd])) {
                $capacnt[$cd] = 0;
            }
            $capacnt[$cd] += (int) $row["CAPA_CNT1"]+ (int) $row["CAPA_CNT2"];
            if (!isset($capacity[$cd])) {
                $capacity[$cd] = 0;
            }
            $capacity[$cd] += (int) $row["TESTDIV_CAPACITY"];
        }
        $i = 1;
        $precd = "";
        $sum = array();
        foreach ($data as $k => $row){
            $cd = $row["COURSECD"] .$row["MAJORCD"].$row["EXAMCOURSECD"];
            //入試区分がコンボに存在しない場合はリンクを省く
            if (in_array($row["TESTDIV"], $opt_testdiv)){
                $row["BORDER_SCORE2"] = View::alink("knjl070oindex.php" ,$row["BORDER_SCORE2"],"",
                                        array("cmd"     => "edit",
                                            "ENTEXAMYEAR" => $row["ENTEXAMYEAR"],
                                            "APPLICANTDIV"  => $row["APPLICANTDIV"],
                                            "TESTDIV"       => $row["TESTDIV"],
                                            "EXAM_TYPE"     => '2',
                                            "SHDIV"         => $row["SHDIV"],
                                            "COURSE"        => $cd
                                    ));
            }

            $row["ID"] = $i;
            if ($precd != $cd && $rowspan[$cd]) { 
                $row["rowspan"] = $rowspan[$cd];
            }
            $row["CAPACITY"] = $capacity[$cd];
            $row["INCREASE"] = $row["CAPACITY"]-$capacnt[$cd];
            $row["COURSE"] = $cd;
            $arg["data"][] = $row;

            foreach (array("CAPACITY",
                        "SUCCESS_CNT1",
                        "CAPA_CNT1",
                        "SUCCESS_CNT2",
                        "CAPA_CNT2",
                        "INCREASE") as $v) {

                if (!isset($sum[$v])) {
                    $sum[$v] = 0;
                }
                if ($precd == $cd && ($v == "CAPACITY" || $v == "INCREASE")) {
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
        $extra = "onclick=\"return btn_submit('sim')\"";
        $arg["btn_sim"] = knjCreateBtn($objForm, "btn_sim", "シミュレーション", $extra);

        //確定ボタン作成
        $extra = "onclick=\"return btn_submit('decision')\"";
        $arg["btn_decision"] = knjCreateBtn($objForm, "btn_decision", "確 定", $extra);

        //終了ボタン作成
        $extra = "onclick=\"closeWin();\"";
        $arg["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl070oForm1.html", $arg); 
    }
}
?>
