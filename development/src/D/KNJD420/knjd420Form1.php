<?php

require_once('for_php7.php');

class knjd420Form1
{
    function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd420index.php", "", "edit");

        $db = Query::dbCheckOut();

        //印刷学期コンボ
        $query = knjd420Query::getD078($model->exp_year, "D".$model->schoolKind."78");
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->printGakki, "PRINT_GAKKI", $extra, 1);

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;
        //グループ情報
        $getGroupRow = array();
        $getGroupRow = $db->getRow(knjd420Query::getViewGradeKindSchreg($model, "set"), DB_FETCHMODE_ASSOC);
        if ($model->schregno) {
            $getGroupName = $db->getOne(knjd420Query::getGroupcd($model, $getGroupRow));
            if ($getGroupName) {
                $arg["GROUP_NAME"] = '履修科目グループ:'.$getGroupName;
            } else {
                $arg["GROUP_NAME"] = '履修科目グループ未設定';
            }
            $getConditionName = $db->getOne(knjd420Query::getConditionName($model, $getGroupRow["CONDITION"]));
            $arg["CONDITION_NAME"] = ($getConditionName) ? '('.$getConditionName.')' : "";
        }

        $query = knjd420Query::getGuidancePattern($model);
        $result = $db->query($query);
        $model->schregInfo = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->schregInfo = $row;
        }
        $result->free();

        if ($model->schregInfo["GUIDANCE_PATTERN"] == 'A') {
            $arg["PATTERN_A"] = "1";
            $arg["NOT_PATTERN_B"] = "1";
            $arg["setName"]["ITEM_REMARK_WIDTH"] = "75";
            $arg["setName"]["ITEM_REMARK_VALUE_WIDTH"] = "150";
        } else if ($model->schregInfo["GUIDANCE_PATTERN"] == 'B') {
            $arg["PATTERN_B"] = "1";
            $arg["NOT_PATTERN_A"] = "1";
            $arg["setName"]["ITEM_REMARK_WIDTH"] = "150";
            $arg["setName"]["ITEM_REMARK_VALUE_WIDTH"] = "75";
        } else {
            $arg["NOT_PATTERN_A"] = "1";
            $arg["NOT_PATTERN_B"] = "1";
            $arg["setName"]["ITEM_REMARK_WIDTH"] = "150";
            $arg["setName"]["ITEM_REMARK_VALUE_WIDTH"] = "75";
        }

        //入力項目タイトル等
        $model->itemName = array();
        $query = knjd420Query::getItemName($model);
        $model->itemName = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["ITEM_REMARK1"] = ($model->itemName["ITEM_REMARK1"]) ? "(".$model->itemName["ITEM_REMARK1"].")" : "";

        if ($model->schregInfo["GUIDANCE_PATTERN"] == 'B') {
            //前印刷学期からコピーボタン
            $name = "";
            for ($i = 1; $i <= 2; $i++) {
                if ($name) {
                    $name .= "・";
                }
                $name .= $model->itemName["ITEM_REMARK".$i];
            }
            $extra = "onclick=\"return btn_submit('copy');\"";
            $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前印刷学期から".$name."コピー", $extra);
        }

        //項目数の最大値
        $max_remark_cnt = $db->getOne(knjd420Query::getGroupRemarkCnt($model, $getGroupRow, "max"));
        $max_remark_cnt = ($max_remark_cnt) ? $max_remark_cnt : "1";

        /************/
        /* 履歴一覧 */
        /************/
        $rirekiCnt = makeList($arg, $db, $model, $max_remark_cnt);
        
        //科目コンボ
        $query = knjd420Query::getSubclass($model);
        $extra = "onChange=\"return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->subclasscd, "SUBCLASSCD", $extra, 1);

        //「指導のねらい」区分
        $get_div = $db->getOne(knjd420Query::getUnitAimDiv($model));
        $unit_aim_div = ($get_div == "1") ? "1" : "0";
        knjCreateHidden($objForm, "UNIT_AIM_DIV", $unit_aim_div);

        //単元コンボ
        $query = knjd420Query::getUnit($model);
        if ($unit_aim_div == "1") {
            $extra = "onChange=\"return btn_submit('edit');\"";
            makeCmb($objForm, $arg, $db, $query, $model->unitcd, "UNITCD", $extra, 1);
        } else {
            $unitcd = $unitname = "";
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $unitcd .= ($unitcd) ? ','.$row["VALUE"] : $row["VALUE"];
                $unitname .= ($unitname) ? ','.$row["LABEL"] : $row["LABEL"];
            }
            $result->free();

            knjCreateHidden($objForm, "UNITCD", $unitcd);
            $arg["UNITCD"] = $unitname;
        }

        //警告メッセージを表示しない場合
        $warning = false;
        if ((isset($model->schregno) && !isset($model->warning) && $model->cmd != "set") || !isset($model->schregno)) {
            $result = $db->query(knjd420Query::getHreportGuidanceSchregDat($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row["SEQ"] == "51") {
                    $name = "PROCEDURE";
                } else if ($row["SEQ"] == "52") {
                    $name = "VALUE_TEXT";
                } else if ($row["SEQ"] == "53") {
                    $name = "VALUE_TEXT2";
                } else {
                    $name = "REMARK".$row["SEQ"];
                }
                $remarkD["SCHREGNO"] = $row["SCHREGNO"];
                $remarkD[$name] = $row["REMARK"];
                $remarkD["REMARK_VALUE".$row["SEQ"]] = $row["REMARK_VALUE"];
            }
            $result->free();

            $remarkU = $db->getRow(knjd420Query::getHreportGuidanceSchregUnitDat($model), DB_FETCHMODE_ASSOC);
            $remarkUseq = knjd420Query::getHreportGuidanceSchregUnitSeqDat($db, $model);
            $remarkY = $db->getRow(knjd420Query::getHreportGuidanceSchregYdat($model), DB_FETCHMODE_ASSOC);
            $recordS = $db->getRow(knjd420Query::getRecordScoreDat($model), DB_FETCHMODE_ASSOC);
            $arg["NOT_WARNING"] = 1;
        } else {
            $remarkD =& $model->field;
            $remarkU =& $model->field2;
            $remarkUseq =& $model->field2seq;
            $remarkY =& $model->field3;
            $recordS =& $model->field4;
            if (isset($model->warning)) $warning = true;
        }

        //項目数
        $group_remark_cnt = $db->getOne(knjd420Query::getGroupRemarkCnt($model, $getGroupRow));
        $remark_cnt = ($group_remark_cnt) ? $group_remark_cnt : "1";
        knjCreateHidden($objForm, "GROUP_REMARK_CNT", $remark_cnt);

        $div_array = array("8", $unit_aim_div, "9");

        if ($model->schregInfo["GUIDANCE_PATTERN"] && $model->subclasscd) {

            list ($classCd, $schoolKind, $curriculumCd, $subclassCd) = preg_split("/-/", $model->subclasscd);

            $counter = 0;
            foreach ($div_array as $div) {
                if ($div == "0") {
                    if (get_count($model->paternInfo[$div][$model->schregInfo["GUIDANCE_PATTERN"]]) > 0) {
                        for ($i=1; $i<=$remark_cnt; $i++) {
                            foreach ($model->paternInfo[$div][$model->schregInfo["GUIDANCE_PATTERN"]] as $key => $val) {

                                $tmp1 = $tmp2 = array();
                                $seq = $val["ADD_SEQ"] + $i;
                                $upd_field = $val["UPD_FIELD"].$seq;
                                $value = $remarkD[$upd_field];

                                if ($val["GYOU"]) {
                                    if ($val["GYOU"] == "1") {
                                        //テキスト
                                        $extra = " onkeypress=\"return btn_keypress();\"";
                                        $tmp2["REMARK"] = knjCreateTextBox($objForm, $value, $upd_field, $val["MOJI"], $val["MOJI"], $extra);
                                        $tmp2["REMARK_SIZE"] = '(全角で '.$val["MOJI"].'文字)';
                                    } else {
                                        //テキストエリア
                                        $height = "height:73px;";
                                        $extra  = "style=\"{$height}\"";
                                        $tmp2["REMARK"] = knjCreateTextArea($objForm, $upd_field, $val["GYOU"], $val["MOJI"] * 2, "wrap", $extra, $value);
                                        $tmp2["REMARK_SIZE"] = "";
                                    }
                                } else if ($val["MOJI"]) {
                                    //テキスト
                                    $extra  = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
                                    $extra .= " onkeypress=\"return btn_keypress();\"";
                                    $tmp2["REMARK"] = knjCreateTextBox($objForm, $value, $upd_field, $val["MOJI"], $val["MOJI"], $extra);
                                    $tmp2["REMARK_SIZE"] = '(半角で '.$val["MOJI"].'文字)';
                                } else {
                                    //コンボ
                                    if ($model->schregInfo["GUIDANCE_PATTERN"] == "A") {
                                        $extra = "";
                                        $tmp2["REMARK"] = makeCmb3($objForm, $arg, $db, $value, $upd_field, $extra, 1, $model, "BLANK");
                                    } else {
                                        $query = knjd420Query::getGradeKindAssess($model, $schoolKind);
                                        $extra = "";
                                        $tmp2["REMARK"] = makeCmb2($objForm, $arg, $db, $query, $value, $upd_field, $extra, 1, "BLANK");
                                    }
                                }
                                $tmp1["REMARK_NAME"] = $model->itemName[$val["NAME"]].$i;

                                $arg["data1"][] = $tmp1;
                                $arg["data2"][] = $tmp2;
                                $counter++;
                            }
                        }
                    }
                } else {
                    if (get_count($model->paternInfo[$div][$model->schregInfo["GUIDANCE_PATTERN"]]) > 0) {
                        foreach ($model->paternInfo[$div][$model->schregInfo["GUIDANCE_PATTERN"]] as $key => $val) {
                            $tmp1 = $tmp2 = array();
                            $show_comment = true;
                            
                            //データセット
                            if ($val["UPD_FIELD"] == "VALUE") {
                                $name = $val["UPD_FIELD"];
                                $value = $recordS[$name];
                            } else if ($div == "1" && $val["SEQ"]) {
                                $name = $val["UPD_FIELD"].$val["SEQ"];
                                $value = $remarkUseq[$name];
                                $schregno = $remarkUseq["SCHREGNO"];
                            } else if ($div == "1") {
                                $name = $val["UPD_FIELD"];
                                $value = $remarkU[$name];
                                $schregno = $remarkU["SCHREGNO"];
                            } else if ($div == "8") {
                                $name = $val["UPD_FIELD"];
                                $value = $remarkY[$name];
                                $schregno = $remarkY["SCHREGNO"];
                            } else {
                                $name = $val["UPD_FIELD"];
                                $value = $remarkD[$name];
                                $schregno = $remarkD["SCHREGNO"];
                                $show_comment = false;
                            }

                            if ($val["GYOU"]) {
                                if ($val["GYOU"] == "1") {
                                    //テキスト
                                    $extra = " onkeypress=\"return btn_keypress();\"";
                                    $tmp2["REMARK"] = knjCreateTextBox($objForm, $value, $name, $val["MOJI"], $val["MOJI"], $extra);
                                    $tmp2["REMARK_SIZE"] = '(全角で '.$val["MOJI"].'文字)';
                                } else {
                                    //テキストエリア
                                    $height = ($val["UPD_FIELD"] == "PROCEDURE") ? "height:275px;" : "height:73px;";
                                    $extra  = "style=\"{$height}\"";
                                    $tmp2["REMARK"] = knjCreateTextArea($objForm, $name, $val["GYOU"], $val["MOJI"] * 2, "wrap", $extra, $value);
                                    $tmp2["REMARK_SIZE"] = ($show_comment) ? '(全角で '.$val["MOJI"].'文字X'.$val["GYOU"].'行)' : "";
                                }
                            } else if ($val["MOJI"]) {
                                //テキスト
                                $extra  = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
                                $extra .= " onkeypress=\"return btn_keypress();\"";
                                $tmp2["REMARK"] = knjCreateTextBox($objForm, $value, $name, $val["MOJI"], $val["MOJI"], $extra);
                                $tmp2["REMARK_SIZE"] = '(半角で '.$val["MOJI"].'文字)';
                            } else {
                                //コンボ
                                if ($model->schregInfo["GUIDANCE_PATTERN"] == "A") {
                                    $extra = "";
                                    $tmp2["REMARK"] = makeCmb3($objForm, $arg, $db, $value, $name, $extra, 1, $model, "BLANK");
                                } else {
                                    $query = knjd420Query::getGradeKindAssess($model, $schoolKind);
                                    $extra = "";
                                    $tmp2["REMARK"] = makeCmb2($objForm, $arg, $db, $query, $value, $name, $extra, 1, "BLANK");
                                }
                            }
                            $tmp1["REMARK_NAME"] = $model->itemName[$val["NAME"]];

                            if ($unit_aim_div == "1" && $model->paternInfo[$div][$model->schregInfo["GUIDANCE_PATTERN"]][0]["UPD_FIELD"] == "YEAR_TARGET") {
                                $arg["pattern7"] = "1";
                                $arg["REMARK_NAME"] = $tmp1["REMARK_NAME"];
                                $arg["ITEM_REMARK1"] = $tmp2["ITEM_REMARK1"];
                                $arg["REMARK"] = $tmp2["REMARK"];
                                $arg["REMARK_SIZE"] = $tmp2["REMARK_SIZE"];
                            } else {
                                $arg["data1"][] = $tmp1;
                                $arg["data2"][] = $tmp2;
                                $counter++;
                                $arg["pattern7Igai"] = $arg["pattern7"] == "1" ? "" : "1";
                            }
                        }
                    }
                }
            }
        }

        if ($arg["pattern7"] == "1") $counter++;
        $arg["COLSPAN"] = ($counter > 0) ? "colspan=\"{$counter}\"" : "";

        //列幅
        $width = ($counter > 0) ? 100 / $counter : 100;
        $arg["WIDTH"] = "style=\"width:{$width}%;\"";

        //ボタン設置
        if ($counter > 5) {
            $arg["BTN1"] = "";
            $arg["BTN2"] = "1";
        } else {
            $arg["BTN1"] = "1";
            $arg["BTN2"] = "";
        }

        //更新
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
        //取消
        $extra = "onclick=\"return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        if ($model->retprg != "") {
            $prgdivstr = substr($model->retprg, 3,1);
            $prglowstr = strtolower($model->retprg);
            if ($model->retprg == "KNJD425") {
                $link = REQUESTROOT."/{$prgdivstr}/{$model->retprg}/{$prglowstr}index.php?cmd=edit&mode=1&SEND_PRGID={$model->getPrgId}&SEND_AUTH={$model->auth}&SCHREGNO={$model->schregno}&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&GRADE={$model->gradeHrClass}&NAME={$model->name}";
            } else {
                $link = REQUESTROOT."/{$prgdivstr}/{$model->retprg}/{$prglowstr}index.php?cmd=edit&mode=1&SEND_PRGID={$model->getPrgId}&SEND_AUTH={$model->auth}&SCHREGNO={$model->schregno}&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&GRADE={$model->grade}&NAME={$model->name}";
            }
            $extra = "onclick=\"window.open('$link','_self');\"";
            $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);
        } else {
            $extra = "onclick=\"closeWin();\"";
            $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
        }

        if ($model->retprg != "KNJD425") {
            //更新後前の生徒へボタン
            $arg["button"]["btn_up_next"] = View::updateNext($model, $objForm, 'btn_update');
        }

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);

        //印刷用
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJD420");
        knjCreateHidden($objForm, "YEAR", $model->exp_year);
        knjCreateHidden($objForm, "SEMESTER", $model->exp_semester);
        knjCreateHidden($objForm, "useGradeKindCompGroupSemester", $model->Properties["useGradeKindCompGroupSemester"]);
        knjCreateHidden($objForm, "SELECT_GHR");

        if(get_count($model->warning)== 0 && $model->cmd !="clear"){
            $arg["next"] = "NextStudent(0);";
        }elseif($model->cmd =="clear"){
            $arg["next"] = "NextStudent(1);";
        }

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjd420Form1.html", $arg);
    }
}

//履歴一覧
function makeList(&$arg, $db, $model, $remark_cnt) {
    $retCnt = 0;
    $bifKey = "";

    $array = array();
    for ($i = 1; $i <= 10; $i++) $array[$i] = array("REMARK".$i, "REMARK_VALUE".$i);
    $array[51] = array("PROCEDURE", "");
    $array[52] = array("VALUE_TEXT", "");
    $array[53] = array("VALUE_TEXT2", "");
    for ($i = 61; $i <= 70; $i++) $array[$i] = array("REMARK".$i, "REMARK_VALUE".$i);
    for ($i = 71; $i <= 73; $i++) $array[$i] = array("REMARK".$i, "REMARK_VALUE".$i, "UNIT_REMARK".$i);

    $query = knjd420Query::getList($model);
    $result = $db->query($query);
    while ($rowlist = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        foreach ($array as $seq => $val) {
            $query = knjd420Query::getHreportGuidanceSchregDat2($model, $rowlist["SET_SUBCLASSCD"], $seq);
            $tmpData = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $rowlist[$val[0]] = str_replace(array("\r\n","\n","\r"), '<br>', $tmpData["REMARK"]);
            if ($val[1] != "") $rowlist[$val[1]] = $tmpData["REMARK_VALUE"];
            if ($val[2] != "" && $rowlist["UNITCD"]) {
                $query = knjd420Query::getHreportGuidanceSchregUnitSeqDat2($model, $rowlist["SET_SUBCLASSCD"], $rowlist["UNITCD"], $seq);
                $tmpData2 = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $rowlist[$val[2]] = $tmpData2["REMARK"];
            }
        }

        //「指導のねらい」区分を取得(単元あり、なしの判断)
        $query = knjd420Query::getUnitAimDiv($model, $rowlist["SET_SUBCLASSCD"]);
        $get_div = $db->getOne($query);
        $unit_aim_div = ($get_div == "1") ? "1" : "0";
        
        //単元あり
        if ($unit_aim_div == 1) {
            $arg["UNIT_ARI"] = "1";
            
            //各項目名をセット
            $arg["setName"]["SET_ITEM_REMARK1"] = $model->itemName["ITEM_REMARK1"];
            $arg["setName"]["SET_ITEM_REMARK2"] = $model->itemName["ITEM_REMARK2"];
            $arg["setName"]["SET_ITEM_REMARK3"] = $model->itemName["ITEM_REMARK3"];
            
            //単元名をセット
            $getUnitName = $db->getOne(knjd420Query::getUnit($model, $rowlist["SET_SUBCLASSCD"], $rowlist["UNITCD"]));
            $rowlist["SET_UNITNAME"] = $getUnitName;
            if ($model->schregInfo["GUIDANCE_PATTERN"] == "1" || $model->schregInfo["GUIDANCE_PATTERN"] == "5"
                || $model->schregInfo["GUIDANCE_PATTERN"] == "6" || $model->schregInfo["GUIDANCE_PATTERN"] == "7"
                || $model->schregInfo["GUIDANCE_PATTERN"] == "8" || $model->schregInfo["GUIDANCE_PATTERN"] == "A") {
                $arg["UNIT_ARI_SET_VALUE"] = "1";
            }
            //重複科目はまとめる
            if ($bifKey !== $rowlist["SET_SUBCLASSCD"]) {
                $cnt = $db->getOne(knjd420Query::getUnitSubclassCnt($model, $rowlist["SET_SUBCLASSCD"]));
                $rowlist["ROWSPAN"] = $cnt > 0 ? $cnt : 1;
            }
            $bifKey = $rowlist["SET_SUBCLASSCD"];
            //科目表示
            $rowlist["SUBCLASS_SHOW"] = "";

        //単元なし
        } else {
            $arg["UNIT_NASHI"] = "1";
            $rowlist["ROWSPAN"] = "1";
            //各項目名をセット
            for ($i = 1; $i <= 10; $i++) {
                if ($remark_cnt >= $i) {
                    $arg["UNIT_NASHI_SET".$i] = "1";
                } 
                $arg["setName"]["ITEM_REMARK".$i]  = $model->itemName["ITEM_REMARK2"].$i;
            }
            
            if ($model->schregInfo["GUIDANCE_PATTERN"] == "1" || $model->schregInfo["GUIDANCE_PATTERN"] == "5"
                || $model->schregInfo["GUIDANCE_PATTERN"] == "6" || $model->schregInfo["GUIDANCE_PATTERN"] == "7"
                || $model->schregInfo["GUIDANCE_PATTERN"] == "8" || $model->schregInfo["GUIDANCE_PATTERN"] == "A") {

                $arg["UNIT_NASHI_SET_VALUE"] = "1";
                
                for ($i = 1; $i <= 10; $i++) {
                    if ($remark_cnt >= $i) {
                        $arg["setName"]["ITEM_REMARK".$i."_VALUENAME"]  = $model->itemName["ITEM_REMARK3"].$i;
                    } 
                }
                if ($model->schregInfo["GUIDANCE_PATTERN"] == "A") {
                    for ($i = 1; $i <= 10; $i++) {
                        if ($remark_cnt >= $i) {
                            $arg["setName"]["ITEM_REMARK".$i."_2"]  = $model->itemName["ITEM_REMARK4"].$i;
                        } 
                    }
                }
            }
            //科目表示
            $rowlist["SUBCLASS_SHOW"] = "1";
        }
        
        //各パターンごとの表示項目
        //文言評価、所見(パターン8のみ)
        if ($model->schregInfo["GUIDANCE_PATTERN"] == "1" || $model->schregInfo["GUIDANCE_PATTERN"] == "2"
            || $model->schregInfo["GUIDANCE_PATTERN"] == "3" || $model->schregInfo["GUIDANCE_PATTERN"] == "4"
            || $model->schregInfo["GUIDANCE_PATTERN"] == "6" || $model->schregInfo["GUIDANCE_PATTERN"] == "7"
            || $model->schregInfo["GUIDANCE_PATTERN"] == "8") {
            $arg["SET_VALUE_TEXT"] = "1";
            $arg["setName"]["SET_ITEM_REMARK4"]  = $model->itemName["ITEM_REMARK4"];
        } 
        if ($model->schregInfo["GUIDANCE_PATTERN"] == "A") {
            $arg["setName"]["SET_ITEM_REMARK4"]  = $model->itemName["ITEM_REMARK4"];
        }
        //手立て
        if ($model->schregInfo["GUIDANCE_PATTERN"] == "2" || $model->schregInfo["GUIDANCE_PATTERN"] == "4") {
            $arg["SET_PROCEDURE"] = "1";
            $arg["setName"]["SET_ITEM_REMARK5"]  = $model->itemName["ITEM_REMARK5"];
        }
        //備考
        if ($model->schregInfo["GUIDANCE_PATTERN"] == "4") {
            $arg["SET_VALUE_TEXT2"] = "1";
            $arg["setName"]["SET_ITEM_REMARK8"]  = $model->itemName["ITEM_REMARK8"];
        }
        //年間目標
        if ($model->schregInfo["GUIDANCE_PATTERN"] == "7") {
            $arg["SET_YEAR_TARGET"] = "1";
            $arg["setName"]["SET_ITEM_REMARK7"]  = $model->itemName["ITEM_REMARK7"];
        }
        //評定
        if ($model->schregInfo["GUIDANCE_PATTERN"] == "6") {
            $arg["SET_VALUE"] = "1";
            $arg["setName"]["SET_ITEM_REMARK6"]  = $model->itemName["ITEM_REMARK6"];
        }

        //パターンB
        if ($model->schregInfo["GUIDANCE_PATTERN"] == 'B') {
            if ($unit_aim_div == 1)  {
                $rowlist["PATTERN_B_SET_SUBCLASSNAME"] = $rowlist["SET_SUBCLASSNAME"];
                $rowlist["PATTERN_B_SET_UNITNAME"] = "<a href=\"knjd420index.php?cmd=list_set&SUBCLASSCD={$rowlist["SET_SUBCLASSCD"]}&UNITCD={$rowlist["UNITCD"]}\">{$rowlist["SET_UNITNAME"]}</a>";
                for ($i = 71; $i <= 73; $i++) $rowlist["PATTERN_B_REMARK".$i] = $rowlist["UNIT_REMARK".$i];
            } else {
                $rowlist["PATTERN_B_SET_SUBCLASSNAME"] = "<a href=\"knjd420index.php?cmd=list_set&SUBCLASSCD={$rowlist["SET_SUBCLASSCD"]}\">{$rowlist["SET_SUBCLASSNAME"]}</a>";
                $rowlist["PATTERN_B_SET_UNITNAME"] = "";
                for ($i = 71; $i <= 73; $i++) $rowlist["PATTERN_B_REMARK".$i] = $rowlist["REMARK".$i];
            }
        }

        $arg["list"][] = $rowlist;
        $retCnt++;
    }
    $result->free();
    return $retCnt;
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
        if ($name == "PRINT_GAKKI" && $row["DEF_VALUE_FLG"] == '1') {
            $defValue = $row["VALUE"];
        }
    }
    if ($name == "PRINT_GAKKI") {
        $value = $defvalue ? $defValue : (($value && $value_flg) ? $value : $opt[0]["value"]);
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//makeCmb
function makeCmb2(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $combo = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
    return $combo;
}

//makeCmb
function makeCmb3(&$objForm, &$arg, $db, &$value, $name, $extra, $size, $model, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") $opt[] = array("label" => "", "value" => "");
    $row = $db->getRow(knjd420Query::getHreportGuidanceSchregImpptDat($model), DB_FETCHMODE_ASSOC);
    for ($i = 1; $i <= 3; $i++) {
        $opt[] = array('label' => $i.':'.$row["REMARK".$i],
                       'value' => $i);
        if ($value == $i) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $combo = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    return $combo;
}
?>
