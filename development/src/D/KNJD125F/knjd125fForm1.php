<?php

require_once('for_php7.php');

// kanji=漢字
// $Id: knjd125fForm1.php 56581 2017-10-22 12:37:16Z maeshiro $

class knjd125fForm1
{
    function main(&$model)
    {
        $objForm = new form;

        $arg["start"]    = $objForm->get_start("main", "POST", "knjd125findex.php", "", "main");
        $arg["YEAR"]     = CTRL_YEAR;

        $db = Query::dbCheckOut();

        //学期コンボ
        $opt_semester = array();
        $opt_semester[] = array("label" => "", "value" => "");
        $result = $db->query(knjd125fQuery::getSemesterMst());
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_semester[] = array("label" => $row["SEMESTERNAME"],
                                    "value" => $row["SEMESTER"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "SEMESTER",
                            "size"        => "1",
                            "value"       => $model->field["SEMESTER"],
                            "options"     => $opt_semester,
                            "extrahtml"   => "onChange=\"btn_submit('')\";"));
        $arg["SEMESTER"] = $objForm->ge("SEMESTER");

        //テストコンボ
        $flg_test = true;
        $opt_test = array();
        $opt_test[] = array("label" => "", "value" => "");
        $result = $db->query(knjd125fQuery::getTestItemName($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_test[] = array("label" => $row["TESTITEMNAME"],
                                "value" => $row["TESTKINDCD"].$row["TESTITEMCD"]);
            if ($row["TESTKINDCD"].$row["TESTITEMCD"] == $model->field["TESTITEM"]) $flg_test = false;
        }
        if ($flg_test) $model->field["TESTITEM"] = $opt_test[0]["value"];
        $objForm->ae( array("type"        => "select",
                            "name"        => "TESTITEM",
                            "size"        => "1",
                            "value"       => $model->field["TESTITEM"],
                            "options"     => $opt_test,
                            "extrahtml"   => "onChange=\"btn_submit('')\";"));
        $arg["TESTITEM"] = $objForm->ge("TESTITEM");

        //科目コンボ
        $opt_sbuclass = array();
        $opt_sbuclass[] = array("label" => "", "value" => "");
        $result = $db->query(knjd125fQuery::selectSubclassQuery());
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_sbuclass[] = array("label" => $row["SUBCLASSCD"]." ".$row["SUBCLASSNAME"],"value" => $row["SUBCLASSCD"]);
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "SUBCLASSCD",
                            "size"        => "1",
                            "value"       => $model->field["SUBCLASSCD"],
                            "options"     => $opt_sbuclass,
                            "extrahtml"   => "onChange=\"btn_submit('')\";"));
        $arg["SUBCLASSCD"] = $objForm->ge("SUBCLASSCD");

        //講座コンボ
        $flg_chaircd = true;
        $opt_chair = $opt_chair_cmb = array();
        $opt_chair[] = array("label" => "", "value" => "");
        $result = $db->query(knjd125fQuery::selectChairQuery($model));
        while( $row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_chair_cmb[] = $row["CHAIRCD"];
            $opt_chair[] = array("label" => $row["CHAIRCD"]." ".$row["CHAIRNAME"],"value" => $row["CHAIRCD"]);
            if ($row["CHAIRCD"] == $model->field["CHAIRCD"]) $flg_chaircd = false;
        }
        if (0 < get_count($opt_chair_cmb)) $opt_chair[] = array("label" => "（全て）", "value" => "00");
        if ($flg_chaircd && $model->field["CHAIRCD"] != "0") $model->field["CHAIRCD"] = $opt_chair[0]["value"];
        $objForm->ae( array("type"        => "select",
                            "name"        => "CHAIRCD",
                            "size"        => "1",
                            "value"       => $model->field["CHAIRCD"],
                            "options"     => $opt_chair,
                            "extrahtml"   => "onChange=\"btn_submit('')\";"
                           ));
        $arg["CHAIRCD"] = $objForm->ge("CHAIRCD");

        //勤怠コード
        $backcolor = array( "1"  => "#3399ff",
                            "2"  => "#66cc33",
                            "3"  => "#66cc33",
                            "4"  => "#ff0099",
                            "5"  => "#ff0099",
                            "6"  => "#ff0099",
                            "8"  => "#3399ff",
                            "9"  => "#66cc33",
                            "10" => "#66cc33",
                            "11" => "#ff0099",
                            "12" => "#ff0099",
                            "13" => "#ff0099",
                            "14" => "#ff0099");

        //試験名称
        $ctrl_name = array("101" => "SCORE101"
                          ,"102" => "SCORE102"
                          ,"201" => "SCORE201"
                          ,"202" => "SCORE202");

        //時間割講座テストより試験日を抽出
        $execute_date = CTRL_DATE;//初期値
        $result = $db->query(knjd125fQuery::selectExecuteDateQuery($model, $opt_chair_cmb));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $execute_date = $row["EXECUTEDATE"];
        }
//echo $execute_date;
        //遅刻何回で欠課とするかの指数取得
        $absent = array();
        $absent = $db->getRow(knjd125fQuery::getScAbsentCov($model),DB_FETCHMODE_ASSOC);

        //初期化
        $model->data=array();
        $counter=0;
        $btn_update_dis = "disabled";//ボタン disabled true

        //休学時の欠課をカウントするかどうかのフラグ(1 or NULL)を取得。1:欠課をカウントする
        $offdaysFlg = $db->getRow(knjd125fQuery::getOffdaysFlg(CTRL_YEAR),DB_FETCHMODE_ASSOC);
        //帳票パラメータ
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SUB_OFFDAYS",
                            "value"     => $offdaysFlg["SUB_OFFDAYS"] ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SUB_ABSENT",
                            "value"     => $offdaysFlg["SUB_ABSENT"] ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SUB_SUSPEND",
                            "value"     => $offdaysFlg["SUB_SUSPEND"] ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SUB_MOURNING",
                            "value"     => $offdaysFlg["SUB_MOURNING"] ) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "SUB_VIRUS",
                            "value"     => $offdaysFlg["SUB_VIRUS"] ) );

        //累積情報
        $attend = array();
        if ($absent["ABSENT_COV"] == "0" || $absent["ABSENT_COV"] == "2") { 
            $result = $db->query(knjd125fQuery::GetAttendData($model->field["SUBCLASSCD"],$absent["ABSENT_COV"],$absent["ABSENT_COV_LATE"], $offdaysFlg));
        } else {
            $result = $db->query(knjd125fQuery::GetAttendData2($model->field["SUBCLASSCD"],$absent["ABSENT_COV_LATE"], $offdaysFlg));
        }
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $attend[$row["SCHREGNO"]]["T_NOTICE"]    = $row["T_NOTICE"];
            $attend[$row["SCHREGNO"]]["T_LATEEARLY"] = $row["T_LATEEARLY"];
            $attend[$row["SCHREGNO"]]["NOTICE_LATE"] = $row["NOTICE_LATE"];
        }

        //合併先科目の単位固定／加算フラグ
        $model->creditflg = $db->getOne(knjd125fQuery::getCalculateCreditFlg($model->field["SUBCLASSCD"]));

        //成績入力対象科目の判別
        if ($db->getOne(knjd125fQuery::getSubjectFlg("D003", $model->field["SUBCLASSCD"]))) {
            $model->subjectflg = "D"; //中学・英語（素点・平常・会話）
        } else if ($db->getOne(knjd125fQuery::getSubjectFlg("D002", $model->field["SUBCLASSCD"]))) {
            $model->subjectflg = "C"; //高校・英語数学（共通・ﾚｯｽﾝ・平常）
        } else {
            $model->subjectflg = "B"; //基本（素点・平常）
        }

        //一覧表示
//echo implode("','", $opt_chair_cmb);
        $result = $db->query(knjd125fQuery::selectQuery($model, $execute_date, $opt_chair_cmb));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            //表示切替
            $seme_kind = $model->field["SEMESTER"] .substr($model->field["TESTITEM"],0,2);
            if ($model->show_all != "on") {
                if ($row["SCORE" .$seme_kind ."_FLG"] == "3") continue;
            }

            //学籍番号を配列で取得
            $model->data["SCHREGNO"][] = $row["SCHREGNO"];

            //クラス-出席番(表示)
            if($row["HR_NAME"] != "" && $row["ATTENDNO"] != ""){
                $row["ATTENDNO"] = sprintf("%s-%02d", $row["HR_NAME"], $row["ATTENDNO"]);
            }

            //累積データ
            $row["T_NOTICE"]    = strlen($attend[$row["SCHREGNO"]]["T_NOTICE"]) ? $attend[$row["SCHREGNO"]]["T_NOTICE"] : "0";
            $row["T_LATEEARLY"] = strlen($attend[$row["SCHREGNO"]]["T_LATEEARLY"]) ? $attend[$row["SCHREGNO"]]["T_LATEEARLY"] : "0";
            $row["NOTICE_LATE"] = strlen($attend[$row["SCHREGNO"]]["NOTICE_LATE"]) ? $attend[$row["SCHREGNO"]]["NOTICE_LATE"] : "0";

            //単位情報を配列で取得
            $model->data["NOTICE_LATE"][] = $row["NOTICE_LATE"]."-".$row["CREDITS"]."-".$row["ABSENCE_HIGH"]."-".$row["AUTHORIZE_FLG"];

            //合併先科目の単位を抽出（2:加算タイプ）
            $model->data["COMBINED_CREDIT_SUM"][] = $row["COMP_CREDIT_SUM"]."-".$row["GET_CREDIT_SUM"]."-".$row["GET_CREDIT_SUM_Y"];

            //各項目を作成
            foreach ($ctrl_name as $code => $col)
            {
                $sem = substr($code, 0, 1);
                $kind = substr($code, 1);
                $row[$col."_COLOR"] = "#ffffff";

                //各データを取得
                $model->data[$col."-".$counter]        = $row[$col];

                //異動情報
                if (strlen($row["TRANSFER_SDATE"]) || strlen($row["TRANSFER_EDATE"])) {

                    //学期期間中すべて異動期間の場合
                    if (strtotime($row["TRANSFER_SDATE"]) <= strtotime($model->control["学期開始日付"][$sem]) && 
                        strtotime($row["TRANSFER_EDATE"]) >= strtotime($model->control["学期終了日付"][$sem])) {
                        $row[$col."_COLOR"]="#ffff00";
                    //一部
                    } elseif ((strtotime($row["TRANSFER_SDATE"]) >= strtotime($model->control["学期開始日付"][$sem]))
                           && (strtotime($row["TRANSFER_SDATE"]) <= strtotime($model->control["学期終了日付"][$sem]))) {
                        $row[$col."_COLOR"]="#ffff00";
                    } elseif ((strtotime($row["TRANSFER_EDATE"]) >= strtotime($model->control["学期開始日付"][$sem]))
                           && (strtotime($row["TRANSFER_EDATE"]) <= strtotime($model->control["学期終了日付"][$sem]))) {
                        $row[$col."_COLOR"]="#ffff00";
                    }
                //卒業日付
                } elseif (strlen($row["GRD_DATE"])) {
                    //学期期間中すべて卒業の場合(学期開始日付以前に卒業している場合）
                    if (strtotime($row["GRD_DATE"]) <= strtotime($model->control["学期開始日付"][$sem])) {
                        $row[$col."_COLOR"]="#ffff00";
                    //一部
                    } elseif (strtotime($row["GRD_DATE"]) > strtotime($model->control["学期開始日付"][$sem])
                             && strtotime($row["GRD_DATE"]) <= strtotime($model->control["学期終了日付"][$sem])) {
                        $row[$col."_COLOR"]="#ffff00";
                    }
                }

                //在籍情報がない場合
                if (!strlen($row["CHAIR_SEM".$sem])) {
                    if ($sem <= CTRL_SEMESTER) $row[$col."_COLOR"]="#ffff00";
                }

                //出欠情報
                if (strlen($row[$col."_ATTEND"])) {
                    $row[$col."_COLOR"] = $backcolor[$row[$col."_ATTEND"]];
                }

                //指定学期テスト
                if ($sem == $model->field["SEMESTER"] && $kind."01" == $model->field["TESTITEM"]) {

                        //チェックボックス
                        $flg_check = (strlen($row[$col."_FLG"])) ? "checked" : "";
                        $objForm->ae( array("type"      => "checkbox",
                                            "name"      => "chk_box"."-".$counter,
                                            "value"     => "on",
                                            "extrahtml" => $flg_check ));
                        $row["CHK_BOX"] = $objForm->ge("chk_box"."-".$counter);

                        //テキストボックスを作成
                        $bgcolor = $row[$col."_COLOR"];
                        $value1 = $row[$col];
                        $value2 = ($row[$col."_FLG"] == "3") ? $row[$col."_SUPP"] : "";
                        $name2 = $col ."-" .$counter;
                        $extrahtml = "STYLE=\"text-align: right\" onChange=\"this.style.background='#ccffcc'\" onblur=\"calc(this);\"";
                        $row[$col]  = "<td align=\"center\" bgcolor=\"" .$bgcolor ."\">" ."<font color=\"#000000\">" .$value1 ."</font>" ."</td>";
                        $row[$col] .= "<td align=\"center\" bgcolor=\"" .$bgcolor ."\">" ."<input name='".$name2."' value=\"".$value2."\" type='text' maxlength='3' size='3' " .$extrahtml .">" ."</td>";

                        //更新ボタン disabled false
                        $btn_update_dis = "";

                //ラベルのみ
                } else {

                    //再試験
                    if ($row[$col."_FLG"] == "3") {

                        $bgcolor = $row[$col."_COLOR"];
                        $value1 = $row[$col];
                        $value2 = ($row[$col."_FLG"] == "3") ? $row[$col."_SUPP"] : "";
                        $row[$col]  = "<td align=\"center\" bgcolor=\"" .$bgcolor ."\">" ."<font color=\"#000000\">" .$value1 ."</font>" ."</td>";
                        $row[$col] .= "<td align=\"center\" bgcolor=\"" .$bgcolor ."\">" ."<font color=\"#000000\">" .$value2 ."</font>" ."</td>";

                    //再試験以外
                    } else {

                        $bgcolor = $row[$col."_COLOR"];
                        $value = ($row[$col."_FLG"] == "1" || $row[$col."_FLG"] == "2") ? $row[$col."_PASS"] : $row[$col];
                        $row[$col]  = "<td colspan=\"2\" align=\"center\" bgcolor=\"" .$bgcolor ."\">" ."<font color=\"#000000\">" .$value ."</font>" ."</td>";

                    }

                }
            }

            $row["COLOR"]="#ffffff";

            $counter++;
            $arg["data"][] = $row;
        }

        Query::dbCheckIn($db);


        //ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_update",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return btn_submit('update');\" " .$btn_update_dis ) );
        $arg["btn_update"] = $objForm->ge("btn_update");

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_reset",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('reset');\"" ) );
        $arg["btn_reset"] = $objForm->ge("btn_reset");

        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ) );
        $arg["btn_end"] = $objForm->ge("btn_end");

        //表示切替
        $chg_val = ($model->show_all == "on")?  "しない" : "" ;

        //処理済表示ボタン
        $objForm->ae( array("type"      => "button",
                            "name"      => "btn_show",
                            "value"     => "処理済みを表示".$chg_val,
                            "extrahtml" => " onClick=\"return btn_submit('show_all');\"" ));
        $arg["btn_show"] = $objForm->ge("btn_show");

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "shw_flg",
                            "value"     => $model->show_all ));
        //更新権限チェック
        knjCreateHidden($objForm, "USER_AUTH", AUTHORITY);
        knjCreateHidden($objForm, "UPDATE_AUTH", DEF_UPDATE_RESTRICT);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjd125fForm1.html", $arg);
    }
}
?>
