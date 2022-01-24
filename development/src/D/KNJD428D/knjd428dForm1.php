<?php

require_once('for_php7.php');
class knjd428dForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd428dindex.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        // 設定する項目。「x学期制_校種」がキー。
        //項目番号 => 1:道徳、2:外国語活動、3:総合的な学習の時間, 4:特別活動、5:自立活動、6:総合所見, 7:行動の記録、8:出欠の備考、9:学校より

        $useSectionChk = array();
        $useSectionChk["P_12"]  = array("1", "4", "5", "6", "7", "8", "9");
        $useSectionChk["P_34"]  = array("1", "2", "3", "4", "5", "6", "7", "8", "9");
        $useSectionChk["P_56"]  = array("1", "3", "4", "5", "6", "7", "8", "9");
        $useSectionChk["J"]     = array("1", "3", "4", "5", "6", "7", "8", "9");
        $useSectionChk["H"]     = array("3", "4", "5", "6", "7", "8", "9");
        $useSectionChk["A"]     = array("6", "8");

        //生徒情報
        $arg["data"]["SCHREGNO"] = $model->schregno;
        $arg["data"]["NAME"]     = $model->name;

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            if ($model->schregno != "") {
                //学期コンボ
                $query = knjd428dQuery::getSemester($model);
                $extra = "onChange=\"return btn_submit('gakki');\"";
                makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $extra, 1, "");

                //(今年度今学期の)選択生徒の校種を割り出す。
                $schkind = $db->getOne(knjd428dQuery::getSchKindFromGrade($model));
                $grade = $db->getOne(knjd428dQuery::getGrade($model));
                if (strlen($schkind) == 0) {
                    //取得できなかったら、以降の処理に影響が出るのでエラーにする。
                    $model->setWarning("MSG303", '生徒に対応する校種が取得できませんでした。');
                }

                if (strlen($schkind) > 0) {
                    //表示部分設定
                    if ($schkind == "P"){
                        if ($grade == "1" || $grade == "2"){
                            $chkKey = $schkind."_12";
                        }else if ($grade == "3" || $grade == "4"){
                            $chkKey = $schkind."_34";
                        }else if ($grade == "5" || $grade == "6"){
                            $chkKey = $schkind."_56";
                        }
                    }else{
                        $chkKey = $schkind;
                    }

                    //各種変数初期化
                    $linerowtbl = array();
                    //学期コンボを指定
                    $arg["DISPSEMESTER"] = 1;
                    $linerowtbl_A = array("P"=>3, "J"=>6, "H"=>3);  //「道徳」用行数
                    $linerowtbl_B = array("P"=>3, "J"=>6, "H"=>6);  //「総合的な学習の時間」用行数
                    $mojicntval = 35;

                    //総合所見・行動の記録・学校よりの表示切替フラグ取得
                    $query = knjd428dQuery::getDispFlg($model, $schkind);
                    $reuslt = $db->query($query);
                    while ($row = $reuslt->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $seqToItemNoArray = array("210" => "6", "208" => "7", "211" => "9"); // SEQと項目番号の対応
                        $itemNo = $seqToItemNoArray[$row["SEQ"]];
                        $itemNoDispFlg[$itemNo] = $row["DISP_FLG"];
                    }

                    //取消時、生徒選択時、及び学期変更時にはDBからデータを取得する。
                    if ($model->cmd == "gakki" || $model->cmd == "edit" || $model->cmd == "clear") {
                        $query = knjd428dQuery::getRow($model);
                        $result = $db->query($query);
                        $row = array();
                        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                            $model->field["REMARK1"] = $row["MORALEDUCATION"];
                            $model->field["REMARK2"] = $row["FOREIGNLANGACT"];
                            $model->field["REMARK3"] = $row["TOTALSTUDYTIME"];
                            $model->field["REMARK4"] = $row["SPECIALACTREMARK"];
                            $model->field["REMARK5"] = $row["SELFRELIANCEACT"];
                            $model->field["REMARK6"] = $row["TOTALREMARK"];
                            $model->field["REMARK8"] = $row["ATTENDREC_REMARK"];
                            $model->field["REMARK9"] = $row["COMMUNICATION"];
                        }
                        $model->staffField = array();
                        $query = knjd428dQuery::getHreportStaff($model);
                        $result = $db->query($query);
                        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                            $model->staffField[$row["SEQ"]]   = $row["STAFFCD"];
                        }
                        //HREPORT_STAFF_DATに担当が登録されていない場合はSCHREG_REGD_HDATに登録された担任をデフォルトにする
                        if (get_count($model->staffField) < 1) {
                            $query = knjd428dQuery::getRegdhStaff($model);
                            $staffRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                            for ($i = 1; $i <= 6; $i++) {
                                $model->staffField[$i]   = $staffRow["STAFFCD{$i}"];
                            }
                        }

                    }

                    //教師コンボ作成
                    $query = knjd428dQuery::getStaff($model);
                    for($i = 1; $i <= 6; $i++) {
                        $extra = " style=\"width:120px\" ";
                        makeCmb($objForm, $arg, $db, $query, "STAFFCD{$i}", $model->staffField[$i], $extra, 1, "BLANK");
                    }

                    //以下、各項目を設定するが、$useSectionChkの設定で、処理をするか制御しているので注意。
                    if (in_array("1", $useSectionChk[$chkKey])) {
                        //道徳
                        $arg["DISP_ITEM1"] = "1";
                        $rowlineval = $linerowtbl_A[$schkind];
                        setUniformInputBox($objForm, $arg, "REMARK1", $model->field["REMARK1"], $mojicntval, $rowlineval);
                    }
                    if (in_array("2", $useSectionChk[$chkKey])) {
                        //外国語活動
                        $arg["DISP_ITEM2"] = "1";
                        $rowlineval = "3";
                        setUniformInputBox($objForm, $arg, "REMARK2", $model->field["REMARK2"], $mojicntval, $rowlineval);
                    }
                    if (in_array("3", $useSectionChk[$chkKey])) {
                        //総合的な学習の時間
                        $arg["DISP_ITEM3"] = "1";
                        $rowlineval = $linerowtbl_B[$schkind];
                        setUniformInputBox($objForm, $arg, "REMARK3", $model->field["REMARK3"], $mojicntval, $rowlineval);
                    }
                    if (in_array("4", $useSectionChk[$chkKey])) {
                        //特別活動
                        $arg["DISP_ITEM4"] = "1";
                        $rowlineval = "6";
                        setUniformInputBox($objForm, $arg, "REMARK4", $model->field["REMARK4"], $mojicntval, $rowlineval);
                    }
                    if (in_array("5", $useSectionChk[$chkKey])) {
                        //自立活動
                        $arg["DISP_ITEM5"] = "1";
                        $rowlineval = "6";
                        setUniformInputBox($objForm, $arg, "REMARK5", $model->field["REMARK5"], $mojicntval, $rowlineval);
                    }
                    if (in_array("6", $useSectionChk[$chkKey]) && $itemNoDispFlg["6"] != "1") {
                        $totalRemarkName = $db->getOne(knjd428dQuery::getTotalRemarkName($schkind));
                            //総合所見
                            $arg["DISP_ITEM6"] = "1";
                            $rowlineval = "6";
                            $arg["data"]["TOTALREMARK_NAME"] = ($totalRemarkName) ? $totalRemarkName : "総合所見";
                            setUniformInputBox($objForm, $arg, "REMARK6", $model->field["REMARK6"], $mojicntval, $rowlineval);
                    }
                    if (in_array("7", $useSectionChk[$chkKey]) && $itemNoDispFlg["7"] != "1") {
                        //行動の記録
                        $arg["DISP_ITEM7"] = "1";
                        //行動の記録ボタン
                        $extra = "onclick=\"loadwindow('".REQUESTROOT."/D/KNJD_BEHAVIOR_SD/knjd_behavior_sdindex.php?CALL_PRG=".KNJD428D."&EXP_YEAR=".$model->exp_year."&EXP_SEMESTER=".$model->semester."&SCHOOL_KIND=".$schkind."&SCHREGNO=".$model->schregno."&GRADE=".$model->grade."&send_knjdBehaviorsd_UseText=".$model->Properties["knjdBehaviorsd_UseText"]."&send_knjdBehaviorsd_DispViewName=".$model->Properties["knjdBehaviorsd_DispViewName"]."',0,0,800,500);\"";

                        $arg["button"]["btn_form1"] = KnjCreateBtn($objForm, "btn_form1", "行動の記録", $extra);
                    }
                    if (in_array("8", $useSectionChk[$chkKey])) {
                        //出欠の備考欄
                        $arg["DISP_ITEM8"] = "1";
                        $rowlineval = 2;
                        $mojicntval = 10;
                        setUniformInputBox($objForm, $arg, "REMARK8", $model->field["REMARK8"], $mojicntval, $rowlineval);

                        //出欠の記録参照ボタン
                        $arg["btn_syukketsu_sansyo"] = makeSelectBtn($objForm, $model, "syukketsukiroku", "btn_syukketsu_sansyo", "出欠の記録参照", "REMARK8", $disabled);
                    }
                    if (in_array("9", $useSectionChk[$chkKey]) && $itemNoDispFlg["9"] != "1") {
                        //学校より
                        $arg["DISP_ITEM9"] = "1";
                        $rowlineval = 6;
                        $mojicntval = 45;
                        setUniformInputBox($objForm, $arg, "REMARK9", $model->field["REMARK9"], $mojicntval, $rowlineval);
                    }
                }
            }
            $arg["NOT_WARNING"] = 1;
        } else {
            $arg["data"] = 1;
        }

        //実際に表示された項目を記録する配列(更新の時に参照)
        $model->displayItemNo = array();
        for($i = 1; $i <= 9; $i++) {
            $model->displayItemNo[$i] = $arg["DISP_ITEM{$i}"];
        }

        //更新ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //更新後前の生徒へボタン
        $arg["button"]["btn_up_next"] = View::updateNext($model, $objForm, 'btn_update');

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = KnjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "PRGID", "KNJD428D");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRINT_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "PRINT_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "GRADE_HR_CLASS");

        $arg["IFRAME"] = VIEW::setIframeJs();

        if (get_count($model->warning) == 0 && $model->cmd != "clear") {
            $arg["next"] = "NextStudent(0);";
        } else if ($model->cmd == "clear") {
            $arg["next"] = "NextStudent(1);";
        }

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        View::toHTML5($model, "knjd428dForm1.html", $arg);
    }
}

function getMaxInputMsg($mojicnt, $maxline){
    return "(全角".$mojicnt."文字X".$maxline."行まで)";
}

function setUniformInputBox(&$objForm, &$arg, $ids, $val, $mojicntval, $rowlineval){
    $ext = "id=\"".$ids."\"";
    $arg["data"][$ids] = getTextOrArea($objForm, $ids, $mojicntval, $rowlineval, $val, $model, $ext);
    $arg["data"][$ids."_COMMENT"] = getMaxInputMsg($mojicntval, $rowlineval);
}

//テキストボックスorテキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val, $model, $ext_outstyle) {
    $retArg = "";
    if ($gyou > 1) {
        //textArea
        $minusHasu = 0;
        $minus = 0;
        if ($gyou >= 5) {
            $minusHasu = $gyou % 5;
            $minus = ($gyou / 5) > 1 ? ($gyou / 5) * 6 : 5;
        }
        $height = $gyou * 13.5 + ($gyou -1) * 3 + (5 - ($minus + $minusHasu));
        $extra = "style=\"height:".$height."px;\" ".$ext_outstyle;
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2) + 1, "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    $defValue = '';
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($row["DEF_VALUE_FLG"] == '1') {
            $defValue = $row["VALUE"];
        }
    }

    $result->free();
    if ($name == "SEMESTER") {
        $value = ($value == "" && $defValue) ? $defValue : ($value ? $value : $opt[0]["value"]);
    } else {
        $value = ($value == "") ? $opt[0]["value"] : $value;
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//選択ボタン
function makeSelectBtn(&$objForm, $model, $div, $name, $label, $target, $disabled="") {
    if (!$div || !$name || !$label || !$target) {
        return;
    } else {
        if ($div == "syukketsukiroku") {   //出欠の記録参照
            $extra = $disabled." onclick=\"loadwindow('".REQUESTROOT."/X/KNJX_SYUKKETSUKIROKU/knjx_syukketsukirokuindex.php?program_id=".PROGRAMID."&SEND_PRGID=".PROGRAMID."&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->semester}&SCHREGNO={$model->schregno}&NAME={$model->name}&TARGET={$target}&DIV=1',0,document.documentElement.scrollTop || document.body.scrollTop,800,350);\"";
        }
        return knjCreateBtn($objForm, $name, $label, $extra);
    }
}
?>
