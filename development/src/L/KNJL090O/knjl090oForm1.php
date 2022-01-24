<?php

require_once('for_php7.php');

class knjl090oForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl090oindex.php", "", "main");
        $db = Query::dbCheckOut();

/****************************************** GET DATA!! ***************************************************************/        

        $Row = $db->getRow(knjl090oQuery::get_edit_data($model), DB_FETCHMODE_ASSOC);

        if (!is_array($Row) && ($model->cmd == "back2" || $model->cmd == "next2")) {
            $model->setWarning("MSG303","更新しましたが、移動先のデータが存在しません。");
            $model->cmd = "main";
            $Row = $db->getRow(knjl090oQuery::get_edit_data($model), DB_FETCHMODE_ASSOC);
        } else {
            $model->examno = $Row["EXAMNO"];
        }

        if ((!isset($model->warning))) {
            if ($model->cmd != 'change' && $model->cmd != 'change_testdiv2') {
                $model->desirediv           = $Row["DESIREDIV"];
                $model->judgement           = $Row["JUDGEMENT"];
                $model->special_measures    = $Row["SPECIAL_MEASURES"];
                $model->procedurediv        = $Row["PROCEDUREDIV"];
                $model->entdiv              = $Row["ENTDIV"];
            }
        }

        //データが無ければ更新ボタン等を無効
        if (!is_array($Row) && $model->cmd == 'reference') {
            $model->setWarning("MSG303");
        }

        if (isset($Row["EXAMNO"])) {
            $model->checkexam = $Row["EXAMNO"];
        }

        //入試区分の値が変わればそれをキーにして受付データ取得
        if($model->cmd == 'change_testdiv2' || $model->cmd == 'change' || $model->cmd == 'update' || (isset($model->warning))) {
            $Row2 = $db->getRow(knjl090oQuery::getRecept($model), DB_FETCHMODE_ASSOC);
        }else{
            $model->testdiv2 = $db->getOne(knjl090oQuery::getMaxtestdiv($model));    //最大testdiv取得
            $Row2 = $db->getRow(knjl090oQuery::getRecept($model), DB_FETCHMODE_ASSOC);
        }

        if (!isset($model->warning) && ($model->cmd != 'change')) {
            $model->judgediv = $Row2["JUDGEDIV"];
        }

        //警告メッセージがある場合はフォームの値を参照する
        if (strlen($model->examno) && (isset($model->warning)) || $model->cmd == 'change' || $model->cmd == 'change_testdiv2') {
            $Row["APPLICANTDIV"]          =& $model->field["APPLICANTDIV"];
            $Row["TESTDIV"]               =& $model->field["TESTDIV"];
            $Row["SUC_COURSECD"]          =& $model->field["SUC_COURSECD"];
            $Row["SUC_MAJORCD"]           =& $model->field["SUC_MAJORCD"];
            $Row["SUC_COURSECODE"]        =& $model->field["SUC_COURSECODE"];
            $Row["HONORDIV"]              =& $model->field["HONORDIV1"];
            $Row["SUCCESS_NOTICENO"]      =& $model->field["SUCCESS_NOTICENO"];
            $Row["FAILURE_NOTICENO"]      =& $model->field["FAILURE_NOTICENO"];
            $Row["INTERVIEW_ATTEND_FLG"]  =& $model->field["INTERVIEW_ATTEND_FLG"];
            $Row["REMARK1"]               =& $model->field["REMARK1"];
            $Row["REMARK2"]               =& $model->field["REMARK2"];
            if ($model->cmd != 'change_testdiv2') {
                $Row2["HONORDIV"]         =& $model->field["HONORDIV2"];
                $Row2["ADJOURNMENTDIV"]   =& $model->field["ADJOURNMENTDIV"];
                $Row2["JUDGECLASS"]       =& $model->field["JUDGECLASS"];
            }
            $Row["SPECIAL_MEASURES3"]     =& $model->field["SPECIAL_MEASURES3"];
        }

        //志願者データ取得
        $Row3 = $db->getRow(knjl090oQuery::getDesire($model), DB_FETCHMODE_ASSOC);

        //同じ入試区分で受験データにも登録されていれば志願、受験コンボ共に変更不可
        $exist = $db->getRow(knjl090oQuery::getRecept($model), DB_FETCHMODE_ASSOC);
        if (is_array($exist) && is_array($Row3) ) {
            $disabled_a = "disabled";
        }

        //警告メッセージがある場合はフォームの値を参照する
        if ((isset($model->warning)) || $model->cmd == 'change') {
            if ((!isset($Row3["APPLICANT_DIV"]))) {
                $Row3["APPLICANT_DIV"]  =& $model->field["APPLICANT_DIV"];
            }
            if ((!isset($Row3["EXAMINEE_DIV"]))) {
                $Row3["EXAMINEE_DIV"]   =& $model->field["EXAMINEE_DIV"];
            }
        }

        //志願者得点データ
        $result = $db->query(knjl090oQuery::getScore($model));
        while($Row4 = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $subclasscd = $Row4["TESTSUBCLASSCD"];
            $arg["data"]["ATTEND".$subclasscd] = ($Row4["ATTEND_FLG"]=="1")? "○" : "";
            $arg["data"]["SCORE".$subclasscd]  = $Row4["SCORE"];
            $arg["data"]["STDSC".$subclasscd]  = $Row4["STD_SCORE"];
            $arg["data"]["RANK".$subclasscd]   = $Row4["RANK"];
        }
        $result->free();

        //受験科目 内申科目 判定名称
        $special_measures3_name = array();
        $result = $db->query(knjl090oQuery::getName($model->year, array("L013","L008","L009","L010")));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($row["NAMECD1"]=="L008") $arg["data"]["CONFIDENTIAL".$row["NAMECD2"]]    = $row["NAME1"];
            if ($row["NAMECD1"]=="L009") $arg["data"]["TESTSUBCLASSCD".$row["NAMECD2"]]  = $row["NAME1"];
            if ($row["NAMECD1"]=="L013") $judgename[$row["NAMECD2"]] = htmlspecialchars($row["NAME1"]);
            if ($row["NAMECD1"]=="L010") $special_measures3_name[$row["NAMECD2"]] = htmlspecialchars($row["NAME1"]);
        }

/*********************************************************** 表示 *************************************************************/

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        if (isset($model->examno)) {
            //何れかの入試区分で受験区分が「有り」であればラベルを表示
            $DESIRE_FLG["EXAMINEE_DIV"] = $db->getOne(knjl090oQuery::get_desire_flg($model));
            if(isset($DESIRE_FLG["EXAMINEE_DIV"])) {
                $arg["data"]["EXAMINEE"] = "【受験】";
            }

            $RECEPT_FLG["HONORDIV"] = $db->getOne(knjl090oQuery::get_recept_flg($model, "HONORDIV"));
            //何れかの入試区分で特待区分が「対象」であればラベルを表示
            if(isset($RECEPT_FLG["HONORDIV"])) {
                $arg["data"]["HONOR"] = "【特待】";
            }
            $RECEPT_FLG["ADJOURNMENTDIV"] = $db->getOne(knjl090oQuery::get_recept_flg($model, "ADJOURNMENTDIV"));
            //何れかの入試区分で延期区分が「延期あり」であればラベルを表示
            if(isset($RECEPT_FLG["ADJOURNMENTDIV"])) {
                $arg["data"]["ADJOURNMENT"] = "【延期】";
            }
        }

        //受験番号
        $objForm->ae( array("type"        => "text",
                            "name"        => "EXAMNO",
                            "size"        => 5,
                            "maxlength"   => 5,
                            "extrahtml"   => "onchange=\"btn_disabled();\" onblur=\"this.value=toInteger(this.value)\"",
                            "value"       => $model->examno ));
        $arg["data"]["EXAMNO"] = $objForm->ge("EXAMNO");

        //合格通知NO.
        $objForm->ae( array("type"        => "text",
                            "name"        => "SUCCESS_NOTICENO",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\", onChange=\"change_flg()\"",
                            "value"       => $Row["SUCCESS_NOTICENO"] ));
        $arg["data"]["SUCCESS_NOTICENO"] = $objForm->ge("SUCCESS_NOTICENO");

        //不合格通知NO.
        $objForm->ae( array("type"        => "text",
                            "name"        => "FAILURE_NOTICENO",
                            "size"        => 4,
                            "maxlength"   => 4,
                            "extrahtml"   => "onblur=\"this.value=toInteger(this.value)\", onChange=\"change_flg()\"",
                            "value"       => $Row["FAILURE_NOTICENO"] ));
        $arg["data"]["FAILURE_NOTICENO"] = $objForm->ge("FAILURE_NOTICENO");

        //判定
        $judge = $db->getRow(knjl090oQuery::getReceptJudge($model), DB_FETCHMODE_ASSOC); 

//        if ($model->judgediv == "1") $model->special_measures = "";

        //変更無し
        if ($model->cmd != "change")  {
            $model->judgement         = $Row["JUDGEMENT"];
            $arg["data"]["JUDGEMENT"] = $Row["JUDGEMENT_NAME"];
            $model->special_measures  = $Row["SPECIAL_MEASURES"];

        //特別措置
        } elseif  ($model->special_measures != "") {
            $model->judgement = "1";
            $arg["data"]["JUDGEMENT"] = $judgename["1"];

        //合格(受付データに1件（画面上も含む）でも合格がある場合)
        } elseif ($model->judgediv == "1" || (int)$judge["PASS"] > 0) {
            $model->judgement = "1";
            $arg["data"]["JUDGEMENT"] = $judgename["1"];
//            $model->special_measures  = "";

        //すべて未設定
        } elseif ($model->judgediv == "" && ((int)$judge["UNKNOWN"] == (int)$judge["CNT"])) {
            $model->judgement         = "";
            $arg["data"]["JUDGEMENT"] = "";

        //不合格(受付データに1件も合格がない場合)
        } elseif ($model->judgediv != "1" && (int)$judge["PASS"] == 0 ) {
            $model->judgement = "2";
            $arg["data"]["JUDGEMENT"] = $judgename["2"];
        }

        //3:特別アップ合格
        $arg["data"]["SPECIAL_MEASURES3_NAME"] = $special_measures3_name[$Row["SPECIAL_MEASURES3"]];
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SPECIAL_MEASURES3",
                            "value"     => $Row["SPECIAL_MEASURES3"]) );

        //氏名(判定で合格ならを赤、措置は青、その他黒)
        if ($model->judgement == '1' && $model->special_measures == '' && $Row["SPECIAL_MEASURES3"] ==''){
            $arg["data"]["NAME"]      = "<font color=\"red\">".htmlspecialchars($Row["NAME"])."</font>";
            $arg["data"]["JUDGEMENT"] = "<font color=\"red\">".htmlspecialchars($arg["data"]["JUDGEMENT"])."</font>";
        } elseif ($model->special_measures !='' || $Row["SPECIAL_MEASURES3"] !='') {
            $arg["data"]["NAME"]      = "<font color=\"blue\">".htmlspecialchars($Row["NAME"])."</font>";
            $arg["data"]["JUDGEMENT"] = "<font color=\"blue\">".htmlspecialchars($arg["data"]["JUDGEMENT"])."</font>";
            $arg["data"]["SPECIAL_MEASURES3_NAME"] = "<font color=\"blue\">".htmlspecialchars($arg["data"]["SPECIAL_MEASURES3_NAME"])."</font>";
        } else {
            $arg["data"]["NAME"] = htmlspecialchars($Row["NAME"]);
        }

/**************************************** CREATE 15 COMBOS ! ************************************************************/

        //入試制度コンボ
        $opt = knjl090oForm1::GetOpt($db, $model->year, array("L003"), 0);
        $arg["data"]["APPLICANTDIV"] = knjl090oForm1::CreateCombo($objForm, "APPLICANTDIV", $Row["APPLICANTDIV"], "150", $opt, "onChange=\"change_flg()\"");

        //出願区分コンボ
        $testdiv = $seq = "";
        $result = $db->query(knjl090oQuery::getTestdivMst($model->year));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if (strlen($Row["TESTDIV" .$row["NAMECD2"]])) {
                $testdiv .= $seq .$row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]);
                $seq = " ／ ";
            }
        }
        $arg["data"]["TESTDIV"] = $testdiv;

        //出願コースコンボ
        $opt = array();
        $result     = $db->query(knjl090oQuery::get_desirediv($model->year));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["DESIREDIV"].":".$row["EXAMCOURSE_NAME"], "value" => $row["DESIREDIV"]);
        }
        $arg["data"]["DESIREDIV"] = knjl090oForm1::CreateCombo($objForm, "DESIREDIV", $model->desirediv, "150", $opt, "onChange=\"btn_submit('change')\";");

        if (!isset($model->desirediv))
            $model->desirediv = $opt[0]["value"];

        knjl090oForm1::FormatOpt($opt_cmcd);
        knjl090oForm1::FormatOpt($opt["L010"]);
        knjl090oForm1::FormatOpt($opt["L011"]);
        knjl090oForm1::FormatOpt($opt["L012"]);
        knjl090oForm1::FormatOpt($opt_honor1);
        knjl090oForm1::FormatOpt($opt_inflg);

        //判定で合格各コンボに値を追加
        if ($model->judgement == '1' || $model->special_measures !='') {

            //面接出欠に値を追加
            $opt_inflg[] = array("label"  =>  '1:出欠', "value"  => 1);
            $opt_inflg[] = array("label"  =>  '2:欠席', "value"  => 2);

            //志望学科に追加
            knjl090oForm1::FormatOpt($opt_cmcd,0);
            $result     = $db->query(knjl090oQuery::get_coursemajor($model));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt_cmcd[] = array("label" => $row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"].":".$row["COURSENAME"],
                                    "value" => $row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"]);
            }
            $Row["COURSEMAJOR"] = $Row["SUC_COURSECD"].$Row["SUC_MAJORCD"].$Row["SUC_COURSECODE"];

            //手続区分に追加
            $opt["L011"] = knjl090oForm1::GetOpt($db, $model->year, array("L011"));

            //手続区分が「済み」なら入学区分に値を追加
            if($model->procedurediv == "1") {
                
                $opt["L012"] = knjl090oForm1::GetOpt($db, $model->year, array("L012"));

                //手続区分が「済み」なら特待入学に値を追加
                if($model->entdiv == "1") {
                    $opt_honor1[] = array("label"  => '1:特待入学', "value"  => 1);
                }
            }
        }

        //志望学科コンボ
        $arg["data"]["COURSEMAJOR"] = knjl090oForm1::CreateCombo($objForm, "COURSEMAJOR", $Row["COURSEMAJOR"], "180", $opt_cmcd, "onChange=\"change_flg()\"");

        //特別措置区分(合格が一件もない場合）
//        if ((int)$judge["PASS"] == 0 && $model->judgediv != "1") {
            $opt["L010"] = knjl090oForm1::GetOpt($db, $model->year, array("L010"));
//        }
        $arg["data"]["SPECIAL_MEASURES"] = knjl090oForm1::CreateCombo($objForm, "SPECIAL_MEASURES", $model->special_measures, "100", $opt["L010"], "onChange=\"btn_submit('change')\";");

        //入学クラスコンボ
        $opt_entclass = knjl090oForm1::GetOpt($db, $model->year, array("L017")) ;
        $arg["data"]["ENTCLASS"] = knjl090oForm1::CreateCombo($objForm, "ENTCLASS", $Row["ENTCLASS"], "140", $opt_entclass, "onChange=\"change_flg()\"");

        //手続区分コンボ
        $arg["data"]["PROCEDUREDIV"] = knjl090oForm1::CreateCombo($objForm, "PROCEDUREDIV", $model->procedurediv, "100", $opt["L011"], "onChange=\"btn_submit('change')\";");

        //入学区分コンボ
        $arg["data"]["ENTDIV"] = knjl090oForm1::CreateCombo($objForm, "ENTDIV", $model->entdiv, "100", $opt["L012"], "onChange=\"btn_submit('change')\";");

        //特待入学コンボ
        $arg["data"]["HONORDIV1"] = knjl090oForm1::CreateCombo($objForm, "HONORDIV1", $Row["HONORDIV"], "100", $opt_honor1, "onChange=\"change_flg()\"");

        //面接出欠コンボ
        $arg["data"]["INTERVIEW_ATTEND_FLG"] = knjl090oForm1::CreateCombo($objForm, "INTERVIEW_ATTEND_FLG", $Row["INTERVIEW_ATTEND_FLG"], "100", $opt_inflg, "onChange=\"change_flg()\"");

        //入試区分コンボ
        $opt = knjl090oForm1::getTestdivMstOpt($db, $model->year, 0) ;
        $arg["data"]["TESTDIV2"] = knjl090oForm1::CreateCombo($objForm, "TESTDIV2", $model->testdiv2, "155", $opt, "onChange=\"change_flg(), btn_submit('change_testdiv2')\";");

        //合否区分コンボ(受付データがなければ合否区分コンボに空セット)
        $opt = (!isset($Row2["EXAMNO"]) ? array() : knjl090oForm1::GetOpt($db, $model->year, array("L013")));
        $arg["data"]["JUDGEDIV"] = knjl090oForm1::CreateCombo($objForm, "JUDGEDIV", $model->judgediv, "100", $opt, "onChange=\"btn_submit('change')\";");

        //合否区分が「合格」なら特待区分コンボと延期区分コンボに値を追加
        knjl090oForm1::FormatOpt($opt);
        knjl090oForm1::FormatOpt($opt2);
        knjl090oForm1::FormatOpt($opt_judgeclass);
        if ($model->judgediv == "1") {
            $opt[]   = array("label"  => '1:対象',       "value"  => 1);
            $opt2[]  = array("label"  => '1:延期あり',   "value"  => 1);
            $opt_judgeclass = knjl090oForm1::GetOpt($db, $model->year, array("L016")) ;
        }

        //特待区分コンボ
        $arg["data"]["HONORDIV2"] = knjl090oForm1::CreateCombo($objForm, "HONORDIV2", $Row2["HONORDIV"], "100", $opt, "onChange=\"change_flg()\"");

        //延期区分コンボ
        $arg["data"]["ADJOURNMENTDIV"] = knjl090oForm1::CreateCombo($objForm, "ADJOURNMENTDIV", $Row2["ADJOURNMENTDIV"], "100", $opt2, "onChange=\"change_flg()\"");

        //合格クラスコンボ
        $arg["data"]["JUDGECLASS"] = knjl090oForm1::CreateCombo($objForm, "JUDGECLASS", $Row2["JUDGECLASS"], "140", $opt_judgeclass, "onChange=\"change_flg()\"");

        //志願区分コンボ
        knjl090oForm1::FormatOpt($opt);
        $opt[] = array("label"  =>  '1:有り', "value"  => 1);
        $opt[] = array("label"  =>  '2:無し', "value"  => 2);
        $arg["data"]["APPLICANT_DIV"] = knjl090oForm1::CreateCombo($objForm, "APPLICANT_DIV", $Row3["APPLICANT_DIV"], "100", $opt, $disabled_a." onChange=\"change_flg()\"");

        //受験区分コンボ
        knjl090oForm1::FormatOpt($opt);
        $opt[] = array("label"  =>  '1:有り', "value"  => 1);
        $opt[] = array("label"  =>  '2:欠席', "value"  => 2);
        $arg["data"]["EXAMINEE_DIV"] = knjl090oForm1::CreateCombo($objForm, "EXAMINEE_DIV", $Row3["EXAMINEE_DIV"], "100", $opt, $disabled_a." onChange=\"change_flg()\"");

/********************************************************* SET DATA ********************************************************/

        Query::dbCheckIn($db);

        $arg["data"]["NAME_KANA"]  = htmlspecialchars($Row["NAME_KANA"]);
        $arg["data"]["SEX"]        = $Row["SEX"]? $Row["SEX"]."：".$Row["SEXNAME"] : "";
        $arg["data"]["FS_GRDYEAR"] = $Row["FS_GRDYEAR"]? $Row["FS_GRDYEAR"]."年" : "";
        $arg["data"]["BIRTHDAY"]   = $Row["ERA_NAME"]? $Row["ERA_NAME"].$Row["BIRTH_Y"]."/".$Row["BIRTH_M"]."/".$Row["BIRTH_D"] : "";

        //試験会場
        $arg["data"]["EXAMHALL_NAME"]   = $Row2["EXAMHALL_NAME"];
        //受付№（座席番号）
        $arg["data"]["RECEPTNO"]        = $Row2["RECEPTNO"];
        //受験型
        $arg["data"]["EXAM_TYPE"] = $Row2["EXAM_TYPE"]? $Row2["EXAM_TYPE"]."：".$Row2["NAME1"] : "";

        $arg["data"]["TOTAL2"]      = $Row2["TOTAL2"];
        $arg["data"]["TOTAL_RANK2"] = $Row2["TOTAL_RANK2"];
        $arg["data"]["DIV_RANK2"]   = $Row2["DIV_RANK2"];
        $arg["data"]["TOTAL4"]      = $Row2["TOTAL4"];
        $arg["data"]["TOTAL_RANK4"] = $Row2["TOTAL_RANK4"];
        $arg["data"]["DIV_RANK4"]   = $Row2["DIV_RANK4"];
        $arg["data"]["AVARAGE2"]    = $Row2["AVARAGE2"];
        $arg["data"]["AVARAGE4"]    = $Row2["AVARAGE4"];
        $arg["data"]["KATEN"]       = $Row2["KATEN"];

        //志願者内申データ
        $arg["data"]["RPT01"] = $Row["CONFIDENTIAL_RPT01"];
        $arg["data"]["RPT02"] = $Row["CONFIDENTIAL_RPT02"];
        $arg["data"]["RPT03"] = $Row["CONFIDENTIAL_RPT03"];
        $arg["data"]["RPT04"] = $Row["CONFIDENTIAL_RPT04"];
        $arg["data"]["RPT05"] = $Row["CONFIDENTIAL_RPT05"];
        $arg["data"]["RPT06"] = $Row["CONFIDENTIAL_RPT06"];
        $arg["data"]["RPT07"] = $Row["CONFIDENTIAL_RPT07"];
        $arg["data"]["RPT08"] = $Row["CONFIDENTIAL_RPT08"];
        $arg["data"]["RPT09"] = $Row["CONFIDENTIAL_RPT09"];
        $arg["data"]["RPT10"] = $Row["CONFIDENTIAL_RPT10"];
        $arg["data"]["RPT11"] = $Row["CONFIDENTIAL_RPT11"];
        $arg["data"]["RPT12"] = $Row["CONFIDENTIAL_RPT12"];
        $arg["data"]["AVERAGE5"]      = $Row["AVERAGE5"];
        $arg["data"]["AVERAGE_ALL"]   = $Row["AVERAGE_ALL"];
        $arg["data"]["ABSENCE_DAYS"] = $Row["ABSENCE_DAYS"];

        //備考１
        $objForm->ae( array("type"      => "text",
                            "name"      => "REMARK1",
                            "size"      => 40,
                            "maxlength" => 60,
                            "extrahtml" => "onChange=\"change_flg()\"",
                            "value"     => $Row["REMARK1"] ));
        $arg["data"]["REMARK1"] = $objForm->ge("REMARK1");

        //備考２
        $objForm->ae( array("type"      => "text",
                            "name"      => "REMARK2",
                            "size"      => 40,
                            "maxlength" => 60,
                            "extrahtml" => "onChange=\"change_flg()\"",
                            "value"     => $Row["REMARK2"] ));
        $arg["data"]["REMARK2"] = $objForm->ge("REMARK2");

/***************************************************** CREATE BUTTONS ********************************************************/

        //検索ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_reference",
                            "value"     => "検 索",
                            "extrahtml" => "onclick=\"btn_submit('reference');\"" ) );
        $arg["button"]["btn_reference"] = $objForm->ge("btn_reference");

        global $sess;
        //かな検索ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_kana_reference",
                            "value"     => "かな検索",
                            "extrahtml" => "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL090O/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"") );
        $arg["button"]["btn_kana_reference"] = $objForm->ge("btn_kana_reference");

        //前の志願者検索ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_back",
                            "value"     => " << ",
                            "extrahtml" => "onClick=\"btn_submit('back1');\"" ) );
        
        //次の志願者検索ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_next",
                            "value"     => " >> ",
                            "extrahtml" => "onClick=\"btn_submit('next1');\"" ) );
        $arg["button"]["btn_back_next"] = $objForm->ge("btn_back").$objForm->ge("btn_next");

        //更新ボタン
        $objForm->ae( array("type"      =>  "button",
                            "name"      =>  "btn_udpate",
                            "value"     =>  "更 新",
                            "extrahtml" =>  "onclick=\"btn_submit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_udpate");

        //更新ボタン(更新後前の志願者)
        $objForm->ae( array("type"      =>  "button",
                            "name"      =>  "btn_up_pre",
                            "value"     =>  "更新後前の志願者",
                            "extrahtml" =>  "style=\"width:150px\" onclick=\"btn_submit('back2');\"" ) );
        //更新ボタン(更新後次の志願者)
        $objForm->ae( array("type"      =>  "button",
                            "name"      =>  "btn_up_next",
                            "value"     =>  "更新後次の志願者",
                            "extrahtml" =>  "style=\"width:150px\" onclick=\"btn_submit('next2');\"" ) );
        $arg["button"]["btn_up_next"] = $objForm->ge("btn_up_pre") . $objForm->ge("btn_up_next");

        //取消ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_reset",
                            "value"     => "取 消",
                            "extrahtml" => "onclick=\"btn_submit('reset');\""  ) );

        $arg["button"]["btn_reset"] = $objForm->ge("btn_reset");

        //終了ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_end",
                            "value"     => "終 了",
                            "extrahtml" => "onclick=\"OnClosing();\"" ) );

        $arg["button"]["btn_end"] = $objForm->ge("btn_end");

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "auth_check",
                            "value"     => (AUTHORITY == DEF_UPDATABLE && is_array($Row)) ? "2" : (AUTHORITY == DEF_UPDATABLE && !is_array($Row) ? "1" : "0") ) );
        //入試年度
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "year",
                            "value"     => $model->year) );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cflg",
                            "value"     => $model->cflg) );

        $arg["IFRAME"] = View::setIframeJs();

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl090oForm1.html", $arg);
    }

    function CreateCombo(&$objForm, $name, $value, $width, $opt, $extra)
    {
        $objForm->ae( array("type"        => "select",
                            "name"        => $name,
                            "size"        => "1",
                            "extrahtml"   => $extra." style=\"width:".$width."\"",
                            "value"       => $value,
                            "options"     => $opt ) );
        return $objForm->ge($name);
    }
    
    function FormatOpt(&$opt, $flg=1){
        
        $opt = array();
        if ($flg == "1")
            $opt[] = array("label" => "", "value" => "");
    }

    function GetOpt(&$db, $year, $namecd, $flg=1)
    {
        $opt = array();
        if ($flg == "1")
            $opt[] = array("label" => "", "value" => "");

        if (is_array($namecd)) {
            $result = $db->query(knjl090oQuery::getName($year, $namecd));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt[] = array("label"  =>  $row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]),
                               "value"  =>  $row["NAMECD2"]);
            }
            $result->free();
        }
        return $opt;
    }

    function getTestdivMstOpt(&$db, $year, $flg=1)
    {
        $opt = array();
        if ($flg == "1")
            $opt[] = array("label" => "", "value" => "");

            $result = $db->query(knjl090oQuery::getTestdivMst($year));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt[] = array("label"  =>  $row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]),
                               "value"  =>  $row["NAMECD2"]);
            }
            $result->free();

        return $opt;
    }
}
?>