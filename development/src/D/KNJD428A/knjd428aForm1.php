<?php

require_once('for_php7.php');

class knjd428aForm1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd428aindex.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        // 設定する項目。「x学期制_校種」がキー。1:道徳、2:外国語活動、3:自立活動、4:総合的な学習の時間・特別活動、
        // 5:委員会・部活動、6:学校より/家庭より
        
        $useSectionChk = array();
        $useSectionChk["3_P"] = array("1", "2", "3", "4", "5");
        $useSectionChk["3_J"] = array("1", "3", "4", "5");
        $useSectionChk["3_H"] = array("3", "5");
        $useSectionChk["2_P"] = array("1", "2", "3", "4");
        $useSectionChk["2_J"] = array("1", "3", "4");
        $useSectionChk["2_H"] = array("3", "4");

        //生徒情報
        $arg["data"]["SCHREGNO"] = $model->schregno;
        $arg["data"]["NAME"]     = $model->name;

        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            if ($model->schregno != "") {
                //学期コンボ
                $query = knjd428aQuery::getSemester($model);
                $extra = "onChange=\"return btn_submit('gakki');\"";
                makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->semester, $extra, 1);

                //(今年度今学期の)選択生徒の校種を割り出す。
                $schkind = $db->getOne(knjd428aQuery::getSchKindFromGrade($model));
                $grade = $db->getOne(knjd428aQuery::getGrade($model));
                if (strlen($schkind) == 0) {
                    //取得できなかったら、以降の処理に影響が出るのでエラーにする。
                    $model->setWarning("MSG303", '生徒に対応する校種が取得できませんでした。');
                } else {
                    //2学期制/3学期制をDBから取得。
                    $semesdiv = $db->getOne(knjd428aQuery::getSemesterDiv($model, $schkind));
                    if (strlen($semesdiv) == 0) {
                        //取得できなかったら、以降の処理に影響が出るのでエラーにする。
                        $model->setWarning("MSG303", '学校マスタの学期制が取得できませんでした。');
                    }
                }
                $chkkey = $semesdiv."_".$schkind;

                if (strlen($schkind) > 0 && strlen($semesdiv) > 0) {
                    //表示部分設定
                    if ($schkind == "P"){
                        if ($grade == "1" || $grade == "2"){
                            $arg["DISP_P12"] = "1";
                            $arg["DISPSELDAT"] = "1";
                        }else if ($grade == "3" || $grade == "4"){
                            $arg["DISP_P34"] = "1";
                            $arg["DISPSELDAT"] = "1";
                        }else if ($grade == "5" || $grade == "6"){
                            $arg["DISP_P56"] = "1";
                            $arg["DISPSELDAT"] = "1";
                        }else{
                            $arg["DISP".$chkkey] = "1";
                            $arg["DISPSELDAT"] = "1";
                        }
                    }else{
                        $arg["DISP".$chkkey] = "1";
                        $arg["DISPSELDAT"] = "1";
                    }

                    //各種変数初期化
                    $linerowtbl = array();
                    if ($semesdiv == 3) {
                        $linerowtbl_A = array("P"=>5, "J"=>5, "H"=>12);
                    } else {
                        //学期コンボを指定
                        $arg["DISPSEMESTER"] = 1;
                        $linerowtbl_A = array("P"=>4, "J"=>4, "H"=>4);
                        $mojicntval = 35;
                    }

                    //取消時、生徒選択時、及び学期変更時にはDBからデータを取得する。
                    if ($model->cmd != "gakki" || $model->cmd != "edit" || $model->cmd != "clear") {
                        $query = knjd428aQuery::getRow($model);
                        $result = $db->query($query);
                        $row = array();
                        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                            $model->field["REMARK1_1"] = $row["REMARK2_1"];
                            $model->field["REMARK2_1"] = $row["FOREIGNLANGACT2_1"];
                            if ($semesdiv == 3) {
                                //3学期制では各学期毎
                                $model->field["REMARK3_1"] = $row["REMARK2_3"];
                                //3学期制では通年
                                $model->field["REMARK4_1"] = $row["SPECIALACTREMARKF9"];
                                $model->field["REMARK4_2"] = $row["TOTALSTUDYTIMEF9"];
                                //格納場所は固定だが、2学期制とは取得時のDIV/CODEが違う。
                                $model->field["REMARK5_1"] = $row["REMARKF3_1"];
                                $model->field["REMARK5_2"] = $row["REMARKF3_2"];
                                $model->field["REMARK5_3"] = $row["REMARKF3_3"];
                            } else {
                                //格納場所は固定だが、3学期制とは取得時のDIV/CODEが違う。
                                $model->field["REMARK3_1"] = $row["REMARK4_1"];
                                $model->field["REMARK3_2"] = $row["REMARK4_2"];
                                //2学期制では各学期毎
                                $model->field["REMARK4_1"] = $row["SPECIALACTREMARK2_1"];
                                $model->field["REMARK4_2"] = $row["TOTALSTUDYTIME2_1"];
                                //"5"は2学期制では通らない。
                            }
                            $model->field["ATTENDREC_REMARK"] = $row["ATTENDREC_REMARK"];
                            $model->field["REMARK6_1"] = $row["COMMUNICATION2_1"];
                        }
                    }

                    //以下、各項目を設定するが、$useSectionChkの設定で、処理をするか制御しているので注意。
                    if (in_array("1", $useSectionChk[$chkkey])) {
                        //道徳
                        $rowlineval = $linerowtbl_A[$schkind];
                        if ($semesdiv == 3) {
                            $mojicntval = 15;
                            setUniformInputBox($objForm, $arg, "REMARK1_1", $model->field["REMARK1_1"], $mojicntval, $rowlineval);
                        } else {
                            setUniformInputBox($objForm, $arg, "REMARK1_1", $model->field["REMARK1_1"], $mojicntval, $rowlineval);
                        }
                    }
                    if (in_array("2", $useSectionChk[$chkkey])) {
                        //外国語活動
                        $rowlineval = $linerowtbl_A[$schkind];
                        if ($semesdiv == 3) {
                            $mojicntval = 15;
                            setUniformInputBox($objForm, $arg, "REMARK2_1", $model->field["REMARK2_1"], $mojicntval, $rowlineval);
                        } else {
                            setUniformInputBox($objForm, $arg, "REMARK2_1", $model->field["REMARK2_1"], $mojicntval, $rowlineval);
                        }
                    }
                    if (in_array("3", $useSectionChk[$chkkey])) {
                        //自立活動
                        if ($semesdiv == 3) {
                            $rowlineval = $linerowtbl_A[$schkind];
                            $mojicntval = 15;
                            setUniformInputBox($objForm, $arg, "REMARK3_1", $model->field["REMARK3_1"], $mojicntval, $rowlineval);
                        } else {
                            $rowlineval = 8;
                            setUniformInputBox($objForm, $arg, "REMARK3_1", $model->field["REMARK3_1"], $mojicntval, $rowlineval);
                            setUniformInputBox($objForm, $arg, "REMARK3_2", $model->field["REMARK3_2"], $mojicntval, $rowlineval);
                        }
                    }
                    if (in_array("4", $useSectionChk[$chkkey])) {
                        $rowlineval = $linerowtbl_A[$schkind];
                        //総合的な学習の時間・特別活動
                        if ($semesdiv == 3) {
                            $mojicntval = 22;
                            //学習活動
                            setUniformInputBox($objForm, $arg, "REMARK4_1", $model->field["REMARK4_1"], $mojicntval, $rowlineval);
                            //評価
                            setUniformInputBox($objForm, $arg, "REMARK4_2", $model->field["REMARK4_2"], $mojicntval, $rowlineval);
                        } else {
                            //総合的な学習の時間
                            setUniformInputBox($objForm, $arg, "REMARK4_1", $model->field["REMARK4_1"], $mojicntval, $rowlineval);
                            //特別活動
                            setUniformInputBox($objForm, $arg, "REMARK4_2", $model->field["REMARK4_2"], $mojicntval, $rowlineval);
                        }
                    }
                    if (in_array("5", $useSectionChk[$chkkey])) {
                        //委員会・部活動
                        if ($semesdiv == 3) {
                            //委員会・部活動(3学期制のみ)
                            $rowlineval = 12;
                            $mojicntval = 15;
                            setUniformInputBox($objForm, $arg, "REMARK5_1", $model->field["REMARK5_1"], $mojicntval, $rowlineval);
                            setUniformInputBox($objForm, $arg, "REMARK5_2", $model->field["REMARK5_2"], $mojicntval, $rowlineval);
                            if ($schkind == "H") {
                                setUniformInputBox($objForm, $arg, "REMARK5_3", $model->field["REMARK5_3"], $mojicntval, $rowlineval);
                            }
                        }
                    }
                    
                    //校種
                    $query = knjd428aQuery::getSchoolKind($model);
                    $schoolKind = $db->getOne($query);

                    //行動の記録
                    $query = knjd428aQuery::getReportCondition($model, $schoolKind, "108");
                    $remark1 = $db->getOne($query);
                    if ($remark1 != '1') {
                        $arg["remark_show"] = '1';
                        //行動の記録ボタン
                        $extra = "onclick=\"loadwindow('".REQUESTROOT."/D/KNJD_BEHAVIOR_SD/knjd_behavior_sdindex.php?CALL_PRG=".KNJD428."&EXP_YEAR=".$model->exp_year."&EXP_SEMESTER=".$model->semester."&SCHREGNO=".$model->schregno."&send_knjdBehaviorsd_UseText=".$model->Properties["knjdBehaviorsd_UseText"]."',0,0,600,500);\"";
                        $arg["button"]["btn_form1"] = KnjCreateBtn($objForm, "btn_form1", "行動の記録", $extra);
                    } else {
                        $arg["remark_show"] = '';
                    }

                    //出欠の備考欄
                    $rowlineval = 2;
                    $mojicntval = 10;
                    setUniformInputBox($objForm, $arg, "ATTENDREC_REMARK", $model->field["ATTENDREC_REMARK"], $mojicntval, $rowlineval);
                    
                    //出欠の記録参照ボタン
                    $arg["btn_syukketsu_sansyo"] = makeSelectBtn($objForm, $model, "syukketsukiroku", "btn_syukketsu_sansyo", "出欠の記録参照", "ATTENDREC_REMARK", $disabled);

                    //学校より/家庭より
                    $rowlineval = 8;
                    $mojicntval = 33;
                    setUniformInputBox($objForm, $arg, "REMARK6_1", $model->field["REMARK6_1"], $mojicntval, $rowlineval);
                }
            }
            $arg["NOT_WARNING"] = 1;
        } else {
            $arg["data"] = 1;
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
        knjCreateHidden($objForm, "PRGID", "KNJD428A");
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

        View::toHTML5($model, "knjd428aForm1.html", $arg);
    }
}

function getMaxInputMsg($mojicnt, $maxline){
    return "(全角".$mojicnt."文字X".$maxline."行まで)";
}

function setUniformInputBox(&$objForm, &$arg, $ids, $val, $mojicntval, $rowlineval){
    $ext = "id=\"".$ids."\"";
    $arg["data"][$ids] = getTextOrArea($objForm, $ids, $mojicntval, $rowlineval, $val, $model, $ext);
    $arg["data"][$ids."_COMMENT"] = getMaxInputMsg($mojicntval, $rowlineval);
    knjCreateHidden($objForm, $ids."_KETA", $mojicntval * 2);
    knjCreateHidden($objForm, $ids."_GYO", $rowlineval);
    KnjCreateHidden($objForm, $ids."_STAT", $ids."area");
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
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size)
{
    $opt = array();
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
