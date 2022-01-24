<?php

require_once('for_php7.php');

class knjm110dForm1 {

    function main(&$model) {

        //セキュリティーチェック
        if(AUTHORITY != DEF_UPDATABLE && AUTHORITY != DEF_UPDATE_RESTRICT) {
            $arg["jscript"] = "OnAuthError();";
        }

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("list", "POST", "knjm110dindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //年度コンボボックス 今年度・今年度+1
        $opt_year = array();
        $opt_year[0] = array("label" => CTRL_YEAR, "value" => CTRL_YEAR);
        $opt_year[1] = array("label" => CTRL_YEAR+1, "value" => CTRL_YEAR+1);

        if (!$model->Year)  $model->Year = CTRL_YEAR;
        $extra = "onChange=\"btn_submit('init');\"";
        $arg["YEAR"] = knjCreateCombo($objForm, "YEAR", $model->Year, $opt_year, $extra, 1);

        //学期
        $opt = array();
        $query = knjm110dQuery::getSemesterMst($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[] = array('label' => $row["SEMESTERNAME"],
                           'value' => $row["SEMESTER"]);
        }
        $model->Select_Semester = ($model->Select_Semester) ? $model->Select_Semester : CTRL_SEMESTER;
        $extra = "onChange=\"return btn_submit('list');\"";
        $arg["SELECT_SEMESTER"] = knjCreateCombo($objForm, "SELECT_SEMESTER", $model->Select_Semester, $opt, $extra, 1);

        //表示区分ラジオボタン 1:講座 2:実施日付
        $opt_div = array(1, 2);
        if (!$model->Div)  $model->Div = "1";
        $click = " onClick=\"btn_submit('init');\"";
        $extra = array("id=\"DIV1\"".$click, "id=\"DIV2\"".$click);
        $radioArray = knjCreateRadio($objForm, "DIV", $model->Div, $extra, $opt_div, get_count($opt_div));
        foreach($radioArray as $key => $val) $arg[$key] = $val;

        //表示切替
        if ($model->Div == 2) {
            $arg["hizuke"] = 1;
        } else {
            $arg["kouza"] = 1;
        }

        /************/
        /*  講  座  */
        /************/
        if ($model->Div == 1) {
            //講座コンボ
            $query = knjm110dQuery::getChairList($model);
            $extra = "onChange=\"btn_submit('kch');\"";
            makeCmb($objForm, $arg, $db, $query, "CHAIRCD", $model->Chair, $extra, 1);

            //担当者表示
            $chair_stf = "";
            $result = $db->query(knjm110dQuery::getStaffname($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($chair_stf != "") $chair_stf .= "、";
                $chair_stf .= $row["STAFFNAME_SHOW"];
            }
            $arg["ChaiShow"] = $chair_stf;
            $result->free();

            //スクーリング一覧
            $syoki = 0;
            $result = $db->query(knjm110dQuery::ReadQuery($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($model->cmd == "kch" && $syoki == 0) {
                    $model->Exedate       = $row["EXECUTEDATE"];
                    $model->Periodcd      = $row["PERIODCD"];
                }
                array_walk($row, "htmlspecialchars_array");
                $exedate = str_replace("-", "/", $row["EXECUTEDATE"]);
                if($row["EXECUTED"] == 1) {
                    $row["EXEDATE"] = $exedate;
                } else {
                    $row["EXEDATE"] = View::alink("knjm110dindex.php",
                                                  $exedate,
                                                  "target=\"right_frame\"",
                                                  array("cmd"           => "edit",
                                                        "PERIODCD"      => $row["PERIODCD"],
                                                        "EXEDATE"       => $row["EXECUTEDATE"],
                                                        "SCHOOLING_SEQ" => $row["SCHOOLING_SEQ"]
                                                  ));
                }
                $arg["data"][] = $row;
                $syoki++;
            }
            if ($model->cmd == "kch" && $syoki == 0){
                $model->Exedate     = "";
                $model->Periodcd    = 0;
            }
            $result->free();
        }

        /**************/
        /*  実施日付  */
        /**************/
        if ($model->Div == 2) {
            $sdate='';
            $edate='';
            $result = $db->query(knjm110dQuery::getSemesterMst($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["SEMESTER"] == $model->Select_Semester){
                    $sdate = $row["SDATE"];
                    $edate = $row["EDATE"];
                }
            }
            
            //実施日付コンボ
            $model->Exedate = ($model->Exedate) ? str_replace("/","-",$model->Exedate) : "";
            $query = knjm110dQuery::getExecutedateList($model, $sdate, $edate);
            $extra = "onChange=\"btn_submit('kch');\"";
            makeCmb($objForm, $arg, $db, $query, "EXEDATE", $model->Exedate, $extra, 1);
        
            //スクーリング一覧
            $result = $db->query(knjm110dQuery::ReadQuery($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                array_walk($row, "htmlspecialchars_array");
                if($row["EXECUTED"] == 1) {
                    $row["CHAIRNAME"] = $row["CHAIRNAME"];
                } else {
                    $row["CHAIRNAME"] = View::alink("knjm110dindex.php",
                                                  $row["CHAIRNAME"],
                                                  "target=\"right_frame\"",
                                                  array("cmd"           => "edit",
                                                        "PERIODCD"      => $row["PERIODCD"],
                                                        "CHAIRCD"       => $row["CHAIRCD"],
                                                        "SCHOOLING_SEQ" => $row["SCHOOLING_SEQ"]
                                                  ));
                }
                $arg["data"][] = $row;
            }
            $result->free();
        }

        //hidden作成
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        if (($model->cmd == "list" || $model->cmd== "kch" || $model->cmd== "init") && VARS::get("ed") != "1")
            $arg["reload"] = "window.open('knjm110dindex.php?cmd=edit&init=1','right_frame');";

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjm110dForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size) {
    $opt = array();
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($name == "EXEDATE") $row["LABEL"] =  str_replace("-", "/", $row["LABEL"]);
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
