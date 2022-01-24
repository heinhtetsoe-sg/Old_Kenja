<?php

require_once('for_php7.php');
class knjl090yForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl090yindex.php", "", "main");
        $db = Query::dbCheckOut();

/****************************************** GET DATA!! ***************************************************************/        

        $Row = $db->getRow(knjl090yQuery::get_edit_data($model), DB_FETCHMODE_ASSOC);
        if (!is_array($Row)) {
            if ($model->cmd == "back2" || $model->cmd == "next2") {
                $model->setWarning("MSG303","更新しましたが、移動先のデータが存在しません。");
            }
            if ($model->cmd == 'back2' || $model->cmd == 'next2' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                $model->cmd = "main";
            }
            $Row = $db->getRow(knjl090yQuery::get_edit_data($model), DB_FETCHMODE_ASSOC);
        } else {
            $model->examno = $Row["EXAMNO"];
        }
//echo "(1)".$model->pay_money."<BR>";
        if ((!isset($model->warning))) {
            if ($model->cmd != 'change' && $model->cmd != 'change_testdiv2') {
                $model->judgement           = $Row["JUDGEMENT"];
                $model->special_measures    = $Row["SPECIAL_MEASURES"];
                $model->judge_kind          = $Row["JUDGE_KIND"];
                $model->procedurediv        = $Row["PROCEDUREDIV"];
                $model->proceduredate       = $Row["PROCEDUREDATE"];
                $model->pay_money           = $Row["PAY_MONEY"];
                $model->entdiv              = $Row["ENTDIV"];
            }
        }
//echo "(2)".$model->pay_money."<BR>";

        //データが無ければ更新ボタン等を無効
        if (!is_array($Row) && $model->cmd == 'reference') {
            $model->setWarning("MSG303");
        }

        if (isset($Row["EXAMNO"])) {
            $model->checkexam = $Row["EXAMNO"];
        }

        //入試区分の値が変わればそれをキーにして受付データ取得
        if ($model->cmd == 'change_testdiv2' || $model->cmd == 'change' || $model->cmd == 'update' || (isset($model->warning))) {
        } else {
            $model->testdiv2 = $db->getOne(knjl090yQuery::getMaxtestdiv($model, $Row["APPLICANTDIV"]));    //最大testdiv取得
        }
        $Row2 = $db->getRow(knjl090yQuery::getRecept($model, $Row["APPLICANTDIV"]), DB_FETCHMODE_ASSOC);

        if (!isset($model->warning) && ($model->cmd != 'change')) {
            $model->judgediv = $Row2["JUDGEDIV"];
        }

        //警告メッセージがある場合はフォームの値を参照する
        if (strlen($model->examno) && (isset($model->warning)) || $model->cmd == 'change' || $model->cmd == 'change_testdiv2') {
            $Row["PROCEDUREDATE"]       =& $model->field["PROCEDUREDATE"];
            $Row["PAY_MONEY"]           =& $model->field["PAY_MONEY"];
            $Row["REMARK1"]             =& $model->field["REMARK1"];
            $Row["REMARK2"]             =& $model->field["REMARK2"];
            if (strlen($model->examno) && (isset($model->warning))) {
                $Row["SUC_COURSECD"]        =& $model->field["SUC_COURSECD"];
                $Row["SUC_MAJORCD"]         =& $model->field["SUC_MAJORCD"];
                $Row["SUC_COURSECODE"]      =& $model->field["SUC_COURSECODE"];
            }
        }
/***
        //志願者データ取得
        $Row3 = $db->getRow(knjl090yQuery::getDesire($model), DB_FETCHMODE_ASSOC);

        //同じ入試区分で受験データにも登録されていれば志願、受験コンボ共に変更不可
        $exist = $db->getRow(knjl090yQuery::getRecept($model, $Row["APPLICANTDIV"]), DB_FETCHMODE_ASSOC);
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
***/
        //志願者得点データ
        $result = $db->query(knjl090yQuery::getScore($model, $Row["APPLICANTDIV"]));
        while($Row4 = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $subclasscd = $Row4["TESTSUBCLASSCD"];
            $arg["data"]["ATTEND".$subclasscd] = ($Row4["ATTEND_FLG"]=="1")? ○ : "";
            $arg["data"]["SCORE".$subclasscd]  = $Row4["SCORE"];
            $arg["data"]["STDSC".$subclasscd]  = $Row4["STD_SCORE"];
            $arg["data"]["RANK".$subclasscd]   = $Row4["RANK"];
        }
        $result->free();

        //受験科目 判定名称
        $testsubName = ($Row["APPLICANTDIV"] == "1") ? "NAME1" : "NAME2";
        $result = $db->query(knjl090yQuery::getName($model->year, array("L013","L009")));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($row["NAMECD1"]=="L009") $arg["data"]["TESTSUBCLASSCD".$row["NAMECD2"]]  = $row[$testsubName];
            if ($row["NAMECD1"]=="L013") $judgename[$row["NAMECD2"]] = htmlspecialchars($row["NAME1"]);
            if ($row["NAMECD1"]=="L013") $judgeNameSpare1[$row["NAMECD2"]] = $row["NAMESPARE1"];
        }

/*********************************************************** 表示 *************************************************************/

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        if (isset($model->examno)) {
            if (strlen($Row2["RECEPTNO"]) && $Row2["JUDGEDIV"] != "4") {
                $arg["data"]["EXAMINEE"] = "【受験】";
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
/***
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
***/
        //判定
        $judge = $db->getRow(knjl090yQuery::getReceptJudge($model), DB_FETCHMODE_ASSOC); 

        if ($judgeNameSpare1[$model->judgediv] == "1") $model->special_measures = "";

        $RowJudgeCourse = array();

        //変更無し
        if ($model->cmd != "change")  {
            $model->judgement         = $Row["JUDGEMENT"];
            $arg["data"]["JUDGEMENT"] = $Row["JUDGEMENT_NAME"];
            $model->special_measures  = $Row["SPECIAL_MEASURES"];

//        //特別措置
//        } elseif  ($model->special_measures != "") {
//            $model->judgement = "1";
//            $arg["data"]["JUDGEMENT"] = $judgename["1"];

        //3:一般入試において、特進チャレンジ者が不合格の場合、基礎データは、1:学特入試での進学コース合格に戻す。
        //理由：特進チャレンジ者は、1:学特入試で進学コース合格して、合格を保有したまま3:一般入試の特進コースを再受験しています。
        } else if ($Row["APPLICANTDIV"] == "2" && $Row2["TESTDIV2"] == "3" && $Row["SELECT_SUBCLASS_DIV"] == "1" && $judgeNameSpare1[$model->judgediv] != "1") {
            $RowJudgeCourse = $db->getRow(knjl090yQuery::getJudgeCourse($model, $Row["APPLICANTDIV"]), DB_FETCHMODE_ASSOC);
            $model->judgement         = $RowJudgeCourse["JUDGEDIV"];
            $arg["data"]["JUDGEMENT"] = $judgename[$RowJudgeCourse["JUDGEDIV"]];

        //合格(受付データに1件（画面上も含む）でも合格がある場合)
        } elseif ($judgeNameSpare1[$model->judgediv] == "1" || (int)$judge["PASS"] > 0) {
            $model->judgement         = $model->judgediv;
            $arg["data"]["JUDGEMENT"] = $judgename[$model->judgediv];
            $model->special_measures  = "";

        //すべて未設定
        } elseif ($model->judgediv == "" && ((int)$judge["UNKNOWN"] == (int)$judge["CNT"])) {
            $model->judgement         = "";
            $arg["data"]["JUDGEMENT"] = "";

        //不合格(受付データに1件も合格がない場合)
        } elseif ($judgeNameSpare1[$model->judgediv] != "1" && (int)$judge["PASS"] == 0 ) {
            $model->judgement         = $model->judgediv;
            $arg["data"]["JUDGEMENT"] = $judgename[$model->judgediv];
        }

        //氏名(判定で合格ならを赤、措置は青、その他黒)
        if ($judgeNameSpare1[$model->judgement] == '1' && $model->special_measures == ''){
            $arg["data"]["NAME"]      = "<font color=\"red\">".htmlspecialchars($Row["NAME"])."</font>";
            $arg["data"]["JUDGEMENT"] = "<font color=\"red\">".htmlspecialchars($arg["data"]["JUDGEMENT"])."</font>";
        } elseif ($model->special_measures !='') {
            $arg["data"]["NAME"]      = "<font color=\"blue\">".htmlspecialchars($Row["NAME"])."</font>";
            $arg["data"]["JUDGEMENT"] = "<font color=\"blue\">".htmlspecialchars($arg["data"]["JUDGEMENT"])."</font>";
        } else {
            $arg["data"]["NAME"] = htmlspecialchars($Row["NAME"]);
        }

/**************************************** CREATE 15 COMBOS ! ************************************************************/

        //入試制度
        $arg["data"]["APPLICANTDIV"] = $Row["APPLICANTDIV"]? $Row["APPLICANTDIV"]."：".$Row["APPLICANTDIVNAME"] : "";

        //出願区分
        $arg["data"]["TESTDIV"] = $Row["TESTDIV"]? $Row["TESTDIV"]."：".$Row["TESTDIVNAME"] : "";

        //専併区分
        $arg["data"]["SHDIV"] = $Row["SHDIV"]? $Row["SHDIV"]."：".$Row["SHDIVNAME"] : "";

        //希望
        $arg["data"]["GENERAL_FLG"] = strlen($Row["GENERAL_FLG"]) ? "（一般入試）" : "";
        $arg["data"]["SLIDE_FLG"]   = strlen($Row["SLIDE_FLG"])   ? "（スライド）" : "";
        $arg["data"]["SPORTS_FLG"]  = strlen($Row["SPORTS_FLG"])  ? "（スポ優秀）" : "";
        $arg["data"]["SHIFT_DESIRE_FLG"]   = strlen($Row["SHIFT_DESIRE_FLG"])   ? "（特別判定）" : "";
        $arg["data"]["SELECT_SUBCLASS_DIV"]   = strlen($Row["SELECT_SUBCLASS_DIV"])   ? "（特進チャ）" : "";
        $arg["data"]["SPORTS_FLG2"]  = strlen($Row["SPORTS_FLG2"])  ? "（Ｔ特奨）" : "";


        knjl090yForm1::FormatOpt($opt_cmcd);
        knjl090yForm1::FormatOpt($opt["L010"]);
        knjl090yForm1::FormatOpt($opt["L011"]);
        knjl090yForm1::FormatOpt($opt["L012"]);
        knjl090yForm1::FormatOpt($opt["L025"]);
//        knjl090yForm1::FormatOpt($opt_honor1);
//        knjl090yForm1::FormatOpt($opt_inflg);
        $disabled_date = "disabled";//デフォルト：入金日を編集不可
//        $disabled_date = "";

        //判定で合格各コンボに値を追加
        if ($judgeNameSpare1[$model->judgement] == '1' || $model->special_measures !='') {

            //面接出欠に値を追加
//            $opt_inflg[] = array("label"  =>  '1:出欠', "value"  => 1);
//            $opt_inflg[] = array("label"  =>  '2:欠席', "value"  => 2);

            //3:一般入試において、特進チャレンジ者が不合格の場合、基礎データは、1:学特入試での進学コース合格に戻す。
            //理由：特進チャレンジ者は、1:学特入試で進学コース合格して、合格を保有したまま3:一般入試の特進コースを再受験しています。
            if (0 < get_count($RowJudgeCourse) && $Row["APPLICANTDIV"] == "2" && $Row2["TESTDIV2"] == "3" && $Row["SELECT_SUBCLASS_DIV"] == "1" && $judgeNameSpare1[$model->judgediv] != "1") {
                //志望学科に追加
                $Row["COURSEMAJOR"] = $RowJudgeCourse["COURSEMAJOR"];
                knjl090yForm1::FormatOpt($opt_cmcd,0);
                $opt_cmcd[] = array("label" => $RowJudgeCourse["COURSEMAJOR"].":".$RowJudgeCourse["EXAMCOURSE_NAME"],
                                    "value" => $RowJudgeCourse["COURSEMAJOR"]);
            //下段の合否区分を変更した場合(特別判定)
            } else if ($model->judgement == $model->judgediv && $model->cmd == 'change' && $model->judgediv == '5') {
                $testdiv2 = $Row2["TESTDIV2"];
                $Row["COURSEMAJOR"] = $Row["SUC_COURSECD"].$Row["SUC_MAJORCD"].$Row["SUC_COURSECODE"];
                knjl090yForm1::FormatOpt($opt_cmcd,0);
                $opt_cmcd[] = array("label" => $Row2["COURSEMAJOR1"].":".$Row2["EXAMCOURSE_NAME1"],
                                    "value" => $Row2["COURSEMAJOR1"]);
                if (strlen($Row2["COURSEMAJOR2"]) && strlen($Row["SLIDE_FLG"])) {
                    $opt_cmcd[] = array("label" => $Row2["COURSEMAJOR2"].":".$Row2["EXAMCOURSE_NAME2"],
                                        "value" => $Row2["COURSEMAJOR2"]);
                }
            } else {
                //志望学科に追加
                $Row["COURSEMAJOR"] = $Row["SUC_COURSECD"].$Row["SUC_MAJORCD"].$Row["SUC_COURSECODE"];
                //下段の合否区分を変更した場合
                if ($model->judgement == $model->judgediv && $model->cmd == 'change') {
                    $Row["COURSEMAJOR"] = ($model->judgediv == '3') ? $Row2["COURSEMAJOR2"] : $Row2["COURSEMAJOR1"];
                    $testdiv2     = $Row2["TESTDIV2"];
                } else {
                    $testdiv2     = $model->testdiv2;
                }
                knjl090yForm1::FormatOpt($opt_cmcd,0);
                $result     = $db->query(knjl090yQuery::get_coursemajor($model->year, $Row["APPLICANTDIV"], $testdiv2));
                while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
                {
                    $opt_cmcd[] = array("label" => $row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"].":".$row["COURSENAME"],
                                        "value" => $row["COURSECD"].$row["MAJORCD"].$row["EXAMCOURSECD"]);
                }
            }

            //特奨に追加
            $namecd2 = (strlen($Row["SPORTS_FLG"]) || strlen($Row["SPORTS_FLG2"])) ? "" : "6";
            $opt["L025"] = knjl090yForm1::GetOpt($db, $model->year, array("L025"), 1, $namecd2);

            //手続区分に追加
            $opt["L011"] = knjl090yForm1::GetOpt($db, $model->year, array("L011"));

            //合格なら入金日を編集可能
            $disabled_date = "";
            $value_date = str_replace("-","/",$model->proceduredate);
            $value_pay_money = strlen($model->pay_money) ? number_format($model->pay_money) : "";
//echo "(3)".$value_pay_money."<BR>";

            //手続区分が「済み」なら入学区分に値を追加
            if ($model->procedurediv == "1") {
                $opt["L012"] = knjl090yForm1::GetOpt($db, $model->year, array("L012"));

                //入学区分が「済み」なら特待入学に値を追加
                if ($model->entdiv == "1") {
//                    $opt_honor1[] = array("label"  => '1:特待入学', "value"  => 1);
                }
            }
        }

        //志望学科コンボ
        $arg["data"]["COURSEMAJOR"] = knjl090yForm1::CreateCombo($objForm, "COURSEMAJOR", $Row["COURSEMAJOR"], "250", $opt_cmcd, "onChange=\"change_flg()\"");

        //特別措置区分(合格が一件もない場合）
        if ((int)$judge["PASS"] == 0 && $judgeNameSpare1[$model->judgediv] != "1") {
            $opt["L010"] = knjl090yForm1::GetOpt($db, $model->year, array("L010"));
        }
        $arg["data"]["SPECIAL_MEASURES"] = knjl090yForm1::CreateCombo($objForm, "SPECIAL_MEASURES", $model->special_measures, "100", $opt["L010"], "onChange=\"btn_submit('change')\";");

        //特奨コンボ
        $arg["data"]["JUDGE_KIND"] = knjl090yForm1::CreateCombo($objForm, "JUDGE_KIND", $model->judge_kind, "100", $opt["L025"], "onChange=\"btn_submit('change')\";");

        //手続区分コンボ
        $arg["data"]["PROCEDUREDIV"] = knjl090yForm1::CreateCombo($objForm, "PROCEDUREDIV", $model->procedurediv, "100", $opt["L011"], "onChange=\"btn_submit('change')\";");

        //入金日
        //$value_date = str_replace("-", "/", $Row["PROCEDUREDATE"]);
        $arg["data"]["PROCEDUREDATE"] = View::popUpCalendar2($objForm, "PROCEDUREDATE", $value_date, "", "", $disabled_date);

        //入金額
        //$value_pay_money = strlen($Row["PAY_MONEY"]) ? number_format($Row["PAY_MONEY"]) : "";
        $objForm->ae( array("type"        => "text",
                            "name"        => "PAY_MONEY",
                            "size"        => 9,
                            "maxlength"   => 9,
                            "extrahtml"   => "style=\"text-align:right;\" onblur=\"this.value=toNumber(this.value); this.value=addFigure(this.value)\", onChange=\"change_flg()\" " .$disabled_date,
                            "value"       => $value_pay_money ));
        $arg["data"]["PAY_MONEY"] = $objForm->ge("PAY_MONEY");

        //入学区分コンボ
        $arg["data"]["ENTDIV"] = knjl090yForm1::CreateCombo($objForm, "ENTDIV", $model->entdiv, "100", $opt["L012"], "onChange=\"btn_submit('change')\";");

        //特待入学コンボ
//        $arg["data"]["HONORDIV1"] = knjl090yForm1::CreateCombo($objForm, "HONORDIV1", $Row["HONORDIV"], "100", $opt_honor1, "onChange=\"change_flg()\"");

        //面接出欠コンボ
//        $arg["data"]["INTERVIEW_ATTEND_FLG"] = knjl090yForm1::CreateCombo($objForm, "INTERVIEW_ATTEND_FLG", $Row["INTERVIEW_ATTEND_FLG"], "100", $opt_inflg, "onChange=\"change_flg()\"");

        //入試区分コンボ
        $namecd1 = ($Row["APPLICANTDIV"] == "1") ? "L024" : "L004";
        $opt = knjl090yForm1::GetOpt($db, $model->year, array($namecd1), 0) ;
        $arg["data"]["TESTDIV2"] = knjl090yForm1::CreateCombo($objForm, "TESTDIV2", $model->testdiv2, "150", $opt, "onChange=\"change_flg(), btn_submit('change_testdiv2')\";");

        //合否区分コンボ(受付データがなければ合否区分コンボに空セット)
        //スライド合格はスライド希望者で高校学特入試のみ表示する
        $namecd2 = (strlen($Row["SLIDE_FLG"]) && $Row["TESTDIV"] == $Row2["TESTDIV2"]) ? "" : "3";
        $namecd3 = (strlen($Row["SHIFT_DESIRE_FLG"]) && $Row["APPLICANTDIV"] == "2" && $Row2["TESTDIV2"] == "1") ? "" : "5";
        $opt = (!isset($Row2["EXAMNO"]) ? array() : knjl090yForm1::GetOpt($db, $model->year, array("L013"), 1, $namecd2, $namecd3));
        $arg["data"]["JUDGEDIV"] = knjl090yForm1::CreateCombo($objForm, "JUDGEDIV", $model->judgediv, "150", $opt, "onChange=\"btn_submit('change')\";");
/***
        //合否区分が「合格」なら特待区分コンボと延期区分コンボに値を追加
        knjl090yForm1::FormatOpt($opt);
        knjl090yForm1::FormatOpt($opt2);
        if ($judgeNameSpare1[$model->judgediv] == "1") {
            $opt[]   = array("label"  => '1:対象',       "value"  => 1);
            $opt2[]  = array("label"  => '1:延期あり',   "value"  => 1);
        }

        //特待区分コンボ
        $arg["data"]["HONORDIV2"] = knjl090yForm1::CreateCombo($objForm, "HONORDIV2", $Row2["HONORDIV"], "100", $opt, "onChange=\"change_flg()\"");

        //延期区分コンボ
        $arg["data"]["ADJOURNMENTDIV"] = knjl090yForm1::CreateCombo($objForm, "ADJOURNMENTDIV", $Row2["ADJOURNMENTDIV"], "100", $opt2, "onChange=\"change_flg()\"");

        //志願区分コンボ
        knjl090yForm1::FormatOpt($opt);
        $opt[] = array("label"  =>  '1:有り', "value"  => 1);
        $opt[] = array("label"  =>  '2:無し', "value"  => 2);
        $arg["data"]["APPLICANT_DIV"] = knjl090yForm1::CreateCombo($objForm, "APPLICANT_DIV", $Row3["APPLICANT_DIV"], "100", $opt, $disabled_a." onChange=\"change_flg()\"");

        //受験区分コンボ
        knjl090yForm1::FormatOpt($opt);
        $opt[] = array("label"  =>  '1:有り', "value"  => 1);
        $opt[] = array("label"  =>  '2:欠席', "value"  => 2);
        $arg["data"]["EXAMINEE_DIV"] = knjl090yForm1::CreateCombo($objForm, "EXAMINEE_DIV", $Row3["EXAMINEE_DIV"], "100", $opt, $disabled_a." onChange=\"change_flg()\"");
***/
/********************************************************* SET DATA ********************************************************/

        Query::dbCheckIn($db);

        $arg["data"]["NAME_KANA"]  = htmlspecialchars($Row["NAME_KANA"]);
        $arg["data"]["SEX"]        = $Row["SEX"]? $Row["SEX"]."：".$Row["SEXNAME"] : "";
//        $arg["data"]["BIRTHDAY"]   = $Row["ERA_NAME"]? $Row["ERA_NAME"].$Row["BIRTH_Y"]."/".$Row["BIRTH_M"]."/".$Row["BIRTH_D"] : "";
        $arg["data"]["BIRTHDAY"]   = $Row["BIRTHDAY"]? str_replace("-","/",$Row["BIRTHDAY"]) : "";

        //試験会場
        $arg["data"]["EXAMHALL_NAME"]   = $Row2["EXAMHALL_NAME"];
        //受付№（座席番号）
        $arg["data"]["RECEPTNO"]        = $Row2["RECEPTNO"];
        //受験型
//        $arg["data"]["EXAM_TYPE"] = $Row2["EXAM_TYPE"]? $Row2["EXAM_TYPE"]."：".$Row2["NAME1"] : "";

        //推薦区分1
        $arg["data"]["RECOM_KIND_NAME1"] = $Row2["RECOM_KIND_NAME1"];
        //志望コース1
        $arg["data"]["EXAMCOURSE_NAME1"] = $Row2["EXAMCOURSE_NAME1"];
        //志望コース2
        $arg["data"]["EXAMCOURSE_NAME2"] = strlen($Row["SLIDE_FLG"]) && $Row["TESTDIV"] == $Row2["TESTDIV2"] ? $Row2["EXAMCOURSE_NAME2"] : "";
//echo "SLIDE_FLG=" . $Row["SLIDE_FLG"] . ", TESTDIV=" . $Row["TESTDIV"] . ", TESTDIV2=" . $Row2["TESTDIV2"] . "<BR>";

        for ($i = 1; $i <= 4; $i++) {
            $arg["data"]["TOTAL".$i]      = $Row2["TOTAL".$i];
            $arg["data"]["TOTAL_RANK".$i] = $Row2["TOTAL_RANK".$i];
            $arg["data"]["DIV_RANK".$i]   = $Row2["DIV_RANK".$i];
            $arg["data"]["AVARAGE".$i]    = $Row2["AVARAGE".$i];
        }
/***
        //備考１
        $objForm->ae( array("type"      => "text",
                            "name"      => "REMARK1",
                            "size"      => 40,
                            "maxlength" => 160,
                            "extrahtml" => "onChange=\"change_flg()\"",
                            "value"     => $Row["REMARK1"] ));
        $arg["data"]["REMARK1"] = $objForm->ge("REMARK1");

        //備考２
        $objForm->ae( array("type"      => "text",
                            "name"      => "REMARK2",
                            "size"      => 40,
                            "maxlength" => 80,
                            "extrahtml" => "onChange=\"change_flg()\"",
                            "value"     => $Row["REMARK2"] ));
        $arg["data"]["REMARK2"] = $objForm->ge("REMARK2");
***/
        //size   = 文字数 * 2 + 1
        //height = 行数 * 13.5 + (行数 -1) * 3 + 5
        //備考１
        $arg["data"]["REMARK1"] = KnjCreateTextArea($objForm, "REMARK1", 4, 41, "soft", "style=\"height:68px;\" onchange=\"change_flg()\"", $Row["REMARK1"]);
        //備考２
        $arg["data"]["REMARK2"] = KnjCreateTextArea($objForm, "REMARK2", 2, 41, "soft", "style=\"height:35px;\" onchange=\"change_flg()\"", $Row["REMARK2"]);

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
                            "extrahtml" => "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL090Y/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"") );
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
                            "name"      =>  "btn_update",
                            "value"     =>  "更 新",
                            "extrahtml" =>  "onclick=\"btn_submit('update');\"" ) );
        $arg["button"]["btn_update"] = $objForm->ge("btn_update");

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
        View::toHTML($model, "knjl090yForm1.html", $arg);
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

    function GetOpt(&$db, $year, $namecd, $flg=1, $namecd2="", $namecd3="")
    {
        $opt = array();
        if ($flg == "1")
            $opt[] = array("label" => "", "value" => "");

        if (is_array($namecd)) {
            $result = $db->query(knjl090yQuery::getName($year, $namecd));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                if ($namecd3 == "5" && $namecd3 == $row["NAMECD2"]) continue;//特別判定
                if ($namecd2 == "3" && $namecd2 == $row["NAMECD2"]) continue;//スライド
                if ($namecd2 == "6" && $namecd2 <= $row["NAMECD2"]) continue;//スポーツ
                $opt[] = array("label"  =>  $row["NAMECD2"] .":" .htmlspecialchars($row["NAME1"]),
                               "value"  =>  $row["NAMECD2"]);
            }
            $result->free();
        }
        return $opt;
    }
}
?>