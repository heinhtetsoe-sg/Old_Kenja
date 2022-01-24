<?php

require_once('for_php7.php');


class knjb1230Form1
{
    function main(&$model)
    {
        $objForm = new form;

        $arg["start"]    = $objForm->get_start("main", "POST", "knjb1230index.php", "", "main");

        //DB接続
        $db = Query::dbCheckOut();

        //処理年度コンボ
        $query = knjb1230Query::getExeYear($model);
        $extra = "onChange=\"return btn_submit('main');\"";
        makeCombo($objForm, $arg, $db, $query, $model->exe_year, "EXE_YEAR", $extra, 1);

        //卒業単位数
        $query = knjb1230Query::getGradCredits($model);
        $model->gradCredits = $db->getOne($query);
        $model->gradCredits = strlen($model->gradCredits) > 0 ? $model->gradCredits : 999;
        knjCreateHidden($objForm, "GRAD_CREDITS", $model->gradCredits);

        //処理年度パターン
        $model->exeNendoPatern = CTRL_YEAR == $model->exe_year ? "1" : "2";
        $arg["data"]["EXE_NENDO"] = $model->exeNendoPatern == "1" ? "(今)" : "(次)";
        $arg["KONNENDO"] = $model->exeNendoPatern == "1" ? "1" : "";
        knjCreateHidden($objForm, "EXE_NENDO_PATERN", $model->exeNendoPatern);

        //履修パターンコンボ
        $query = knjb1230Query::getPattern($model);
        makeCombo($objForm, $arg, $db, $query, $model->pattern_cd, "PATTERN_CD", "", 1, "BLANK");

        //履修期間コンボ
        $query = knjb1230Query::getTakesemes($model);
        $extra = "onChange=\"return btn_submit('main');\"";
        makeCombo($objForm, $arg, $db, $query, $model->takesemes, "TAKESEMES", $extra, 1, "ALL");

        //履修コースコンボ
        $query = knjb1230Query::getRisyuuCourse($model);
        $extra = "onChange=\"return btn_submit('main');\"";
        $model->b022 = makeCombo($objForm, $arg, $db, $query, $model->risyuuCourse, "RISYUU_COURSE", $extra, 1, "ALL");
        $arg["useChairDatRisyuuCourse"] = ($model->Properties["useChairDatRisyuuCourse"] == '1') ? 1 : "";

        //生徒データ表示
        makeStudentInfo($objForm, $arg, $db, $model);

        //明細
        makeMeisai($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $db);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjb1230Form1.html", $arg);
    }
}

//生徒データ表示
function makeStudentInfo(&$objForm, &$arg, $db, &$model)
{
    $info = $db->getRow(knjb1230Query::getStudentInfoData($model), DB_FETCHMODE_ASSOC);
    $model->gradeHr = $info["CTRL_GRADE"]."-".$info["CTRL_HR_CLASS"];
    $grdYoteiAdd = floor((int)$info["TOKKATU_JISU"] / 10) + 1;
    if (is_array($info)) {
        foreach ($info as $key => $val) {
            if (in_array($key, array("ENT_DATE", "GRD_DATE"))) {
                $splitVal = preg_split("/-/", $val);
                $val = $splitVal[0]."年".$splitVal[1]."月";
                if ($key == "ENT_DATE") {
                    $setRow["GRD_SCHEDULE_DATE"] = $splitVal[0] + $grdYoteiAdd."年3月";
                }
            }
            $setRow[$key] = $val;
        }
    }

    $query = knjb1230Query::getRisyuuCnt($model);
    $risyuuCnt = $db->getOne($query);
    $zanMusyouKaisu = $info["MUSYOU_KAISU"] < $risyuuCnt ? 0 : $info["MUSYOU_KAISU"] - $risyuuCnt;
    $arg["data"]["MUSYOU_KAISU"] = $zanMusyouKaisu;

    $model->sateiTanni = $info["SATEI_TANNI"] && $info["JIKOUGAI_NYUURYOKU"] != "1" ? $info["SATEI_TANNI"] : 0;
    if ($info["JIKOUGAI_NYUURYOKU"] != "1") {
        $arg["data"]["SATEI_TANNI"] = "査定単位数：".$model->sateiTanni;
    } else {
        $arg["data"]["SATEI_TANNI"] = "前籍校入力完了";
    }
    knjCreateHidden($objForm, "SATEI_TANNI", $model->sateiTanni);

    $extra = $model->grdYotei == "1" ? " checked " : "";
    if ($model->cmd == "main" || $model->cmd == "prev" || $model->cmd == "next") {
        $extra = $info["GRD_YOTEI"] == "1" ? " checked " : "";
    }
    $setRow["GRD_YOTEI"] = knjCreateCheckBox($objForm, "GRD_YOTEI", "1", $extra);

    $setRow["ADDR"] = $setRow["ADDR1"].$setRow["ADDR2"];
    $arg["info"] = $setRow;

    $model->schregno = $setRow["SCHREGNO"];
    $model->newStudent = $setRow["ENT_YEAR"] == CTRL_YEAR ? true : false;
    $model->name = $setRow["NAME"];
}

//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "", "value" => "");
    } else if ($blank == "ALL") {
        $opt[] = array ("label" => "全て", "value" => "");
    }
    $result = $db->query($query);

    $retArray = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);

        if ($value === $row["VALUE"]) $value_flg = true;
        if ($name == 'RISYUU_COURSE') {
            $retArray[$row["VALUE"]][$row["VALUE"]] = 1;
            if ($row["NAMESPARE1"]) {
                $setValArray = explode(",", $row["NAMESPARE1"]);
                foreach ($setValArray as $key => $val) {
                    $retArray[$row["VALUE"]][$val] = 1;
                }
            }
        }
    }
    $result->free();

    $value = (strlen($value) > 0 && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    return $retArray;
}

//明細
function makeMeisai(&$objForm, &$arg, $db, &$model)
{
    $totalComp = 0;
    $totalGet  = 0;
    $totalJikougai  = 0;
    $tmpdata = array();
    $selectdata = is_array($model->addchk) ? $model->addchk: array();
    $selectKounin = is_array($model->kounin) ? $model->kounin: array();
    $className = "";
    $classNameBack = "";
    $CHECKEDarr = array();
    $KOUNINarr = array();
    $ZOUTANarr = array();
    $CHECKBOXarr = array();
    $SUBCLASSCDarr = array();
    $CLASSCDarr = array();
    $SORTarr = array();
    $SORT2arr = array();
    $PATTERNarr = array();

    $query = knjb1230Query::getSubclassChairCnt($model);
    $result = $db->query($query);
    $model->subclassChairCnt = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $setSubclass = $row["CURRICULUM_CD"].":".$row["CLASSCD"].":".$row["SCHOOL_KIND"].":".$row["SUBCLASSCD"];
        $model->subclassChairCnt[$setSubclass] = 1;
    }
    $result->free();

    $query = knjb1230Query::getChairStd($model);
    $result = $db->query($query);
    $model->chairStd = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $model->chairStd[$row["CHAIRCD"]] = 1;
    }
    $result->free();

    if ($model->Properties["useKeizokuRisyuu"] == "1") {
        $arg["useKeizokuRisyuu"] = "1";
    }
    $query = knjb1230Query::getSubclassName($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $valName = $row["CURRICULUM_CD"].":".$row["CLASSCD"].":".$row["SCHOOL_KIND"].":".$row["SUBCLASSCD"].":".$row["CHAIRCD"];
        $row["ZOUTAN"] = get_count($model->zoutan) > 0 ? $model->zoutan[$valName] : $row["ZOUTAN"];
        $zoutanCheck = strlen($row["ZOUTAN"]) > 0 ? "1" : "0";
        if ($model->exeNendoPatern != "1" && $row["CTLCHECKED"] == "1" && $row["OLD_FLG"] != "1" && $row["STUDYREC_CNT"] == "0") {
            $row["RISYUTYU_CREDIT"] = $row["SET_CREDIT2"];
        }
        if ($row["CHECKBOX"] == "1") {
            $chkval = $row["CURRICULUM_CD"].":".$row["CLASSCD"].":".$row["SCHOOL_KIND"].":".$row["SUBCLASSCD"].":".$row["SET_CREDIT"].":".$row["CHAIRCD"];
            if ($model->cmd == "select") {
                foreach ($selectdata as $val) {
                    if ($val == $chkval) {
                        $row["CHECKED"] = "1";
                        $zoutanCheck = "0";
                        break;
                    } else {
                        $row["CHECKED"] = "";
                    }
                }
                foreach ($selectKounin as $val) {
                    if ($val == $chkval) {
                        $row["KOUNIN"] = "1";
                        $zoutanCheck = "0";
                        break;
                    } else {
                        $row["KOUNIN"] = "";
                    }
                }
            } else {
                if ($row["CHECKED"] != "1") {
                    foreach ($selectdata as $val) {
                        if ($val == $chkval) {
                            $row["CHECKED"] = "1";
                            $zoutanCheck = "0";
                            break;
                        } else {
                            $row["CHECKED"] = "";
                        }
                    }
                }
                if ($row["KOUNIN"] != "1") {
                    foreach ($selectKounin as $val) {
                        if ($val == $chkval) {
                            $row["KOUNIN"] = "1";
                            $zoutanCheck = "0";
                            break;
                        } else {
                            $row["KOUNIN"] = "";
                        }
                    }
                }
            }
        }
        $row["COMP_CONTINUE"] = $row["COMP_CONTINUE"] == "1" ? "レ" : "";
        $tmpdata[] = $row;
        $CHECKEDarr[] = $row["PATTERN_FLG"] == "1" ? "0" : $row["CHECKED"];
        $KOUNINarr[] = $row["PATTERN_FLG"] == "1" ? "0" : $row["KOUNIN"];
        $ZOUTANarr[] = $row["PATTERN_FLG"] == "1" ? "0" : $zoutanCheck;
        $CHECKBOXarr[] = $row["CHECKBOX"];
        $CLASSCDarr[] = $row["CLASSCD"];
        $SUBCLASSCDarr[] = $row["SUBCLASSCD"];
        $SORTarr[] = $row["SORT"];
        $SORT2arr[] = $row["PATTERN_FLG"] == "1" ? "2" : $row["SORT2"];
        $PATTERNarr[] = $row["PATTERN_FLG"];
    }
    
    if ($model->disp == 2) {
        array_multisort($SORT2arr, SORT_ASC,
                        $CHECKEDarr, SORT_DESC,
                        $KOUNINarr, SORT_DESC,
                        $ZOUTANarr, SORT_DESC,
                        $PATTERNarr, SORT_DESC,
                        $CHECKBOXarr, SORT_DESC,
                        $CLASSCDarr, SORT_ASC,
                        $SORTarr, SORT_ASC,
                        $SUBCLASSCDarr, SORT_ASC,
                        $tmpdata); //配列を並び替える
    }
    
    $model->subclassArray = array();
    foreach ($tmpdata as $row) {
        //再履修科目は行の色を変更
        $color = $row["AGAIN_COMP_FLG"] == "1" ? "#ffffcc" : "#ffffff";
        $addDis = "";
        $kouninDis = "";
        $zoutanDis = "";
        $zoutanFlg = false;

        //チェックボックス
        $jinendoSetColor = false;
        if ($row["CHECKBOX"] == "1") {
            $checkSubclass = $row["CURRICULUM_CD"].":".$row["CLASSCD"].":".$row["SCHOOL_KIND"].":".$row["SUBCLASSCD"];
            $model->subclassArray[] = $row["CURRICULUM_CD"].":".$row["CLASSCD"].":".$row["SCHOOL_KIND"].":".$row["SUBCLASSCD"].":".$row["CHAIRCD"];
            $checkVal = $row["CURRICULUM_CD"].":".$row["CLASSCD"].":".$row["SCHOOL_KIND"].":".$row["SUBCLASSCD"].":".$row["SET_CREDIT"].":".$row["CHAIRCD"];
            $extraCredit = "onClick=\"credit_total(this, {$row["NOT_CHANGE"]}, '{$zoutanFlg}');\"";
            //履修
            $extraRisyu = "";
            $extraKounin = "";
            $subRityu  = 0;
            if ($row["CHECKED"] == "1") {
                $extraRisyu = " checked ";
                if ($model->cmd == "pattern" && $model->subclassChairCnt[$checkSubclass] == "1") {
                    $extraRisyu = "";
                    $row["RISYUTYU_CREDIT"] = "";
                    $row["ANOTHER_CREDITS"] = "0";
                    $row["RECORD_CREDITS"] = "0";
                } else if ($model->cmd != "pattern" && $model->chairStd[$row["CHAIRCD"]] != "1") {
                    $extraRisyu = "";
                    $row["RISYUTYU_CREDIT"] = "";
                    $row["ANOTHER_CREDITS"] = "0";
                    $row["RECORD_CREDITS"] = "0";
                }
                if ($model->exeNendoPatern == "1") {
                    $totalGet  += $row["SET_CREDIT"];
                } else if ($model->exeNendoPatern != "1") {
                    $totalGet  += $row["SET_CREDIT3"];
                }
                $kouninDis = " disabled ";
                $zoutanDis = " disabled ";
                $jinendoSetColor = true;
            } else if ($row["KOUNIN"] == "1") {
                $addDis = " disabled ";
                $zoutanDis = " disabled ";
                $extraKounin = " checked";
                $totalJikougai  += $row["SET_CREDIT"];
                $subRityu  = $row["SET_CREDIT"];
                $jinendoSetColor = true;
            } else if ($row["ZOUTAN"]) {
                $addDis = " disabled ";
                $kouninDis = " disabled ";
                $totalJikougai  += $row["ZOUTAN"];
                $subRityu  = $row["ZOUTAN"];
                $jinendoSetColor = true;
            }
            $row["ADDCHK"] = knjCreateCheckBox($objForm, "ADDCHK", $checkVal, $extraCredit.$extraRisyu.$addDis, "1");
            //高認
            $row["KOUNIN"] = knjCreateCheckBox($objForm, "KOUNIN", $checkVal, $extraCredit.$extraKounin.$kouninDis, "1");
            //増単
            $zoutanDis = $zoutanFlg ? " disabled " : $zoutanDis;
            $valName = $row["CURRICULUM_CD"].":".$row["CLASSCD"].":".$row["SCHOOL_KIND"].":".$row["SUBCLASSCD"].":".$row["CHAIRCD"];
            $extraZou = " STYLE=\"text-align:right;\" onblur=\"this.value=toInteger(this.value); credit_total(this, {$row["NOT_CHANGE"]}, '{$zoutanFlg}');\"";
            $row["ZOUTAN"] = knjCreateTextBox($objForm, $row["ZOUTAN"], "ZOUTAN".$valName, 2, 2, $extraZou.$zoutanDis);

        }
        $row["TOTAL_COMP"] = $row["RISYUTYU_CREDIT"] + $row["ANOTHER_CREDITS"] + $row["RECORD_CREDITS"];

        //hidden
        knjCreateHidden($objForm, "VAL_TOTAL_COMP_{$valName}", $row["ANOTHER_CREDITS"] + $row["RECORD_CREDITS"]);

        $row["RISYUTYU_CREDIT"] = "<span id=\"ID_RISYUTYU_CREDIT_{$valName}\">{$row["RISYUTYU_CREDIT"]}</span>";
        if ($model->exeNendoPatern == "1" && $row["SET_CREDIT"] && intval($row["TOTAL_COMP"]) >= intval($row["SET_CREDIT"]) ||
            $model->exeNendoPatern != "1" && $row["SET_CREDIT2"] && intval($row["TOTAL_COMP"]) >= intval($row["SET_CREDIT2"])) {
            $color = "#ccffff";
        }

        if ($model->exeNendoPatern == "2" && $jinendoSetColor) {
            $color = "#ffcccc";
        }

        $row["ANOTHER_CREDITS"] = $row["ANOTHER_CREDITS"] ? $row["ANOTHER_CREDITS"] : "";
        
        $totalComp += $row["TOTAL_COMP"];
        $classNameBack = $row["CLASSNAME"];
        $row["CLASSNAME"] = $className == $row["CLASSNAME"] ? "" : $row["CLASSNAME"];

        $row["TOTAL_COMP"] = "<span id=\"ID_TOTAL_COMP_{$valName}\">{$row["TOTAL_COMP"]}</span>";

        $row["BGCOLOR"] = $color;

        //非表示
        $row["DISPLAY"] = "";
        if ((strlen($model->risyuuCourse) > 0 && $model->b022[$model->risyuuCourse][$row["RISYUU_COURSE"]] != 1) || (strlen($model->takesemes) > 0 && $model->takesemes != $row["TAKESEMES"])) {
            $row["DISPLAY"] = " style=\"display:none;\" ";
        }
        $arg["testdata"][] = $row;

        $className = $classNameBack;
    }

    $arg["total"]["COMP"] = $totalComp;
    $arg["total"]["GET"]  = $totalGet;
    $arg["total"]["JIKOUGAI"]  = $totalJikougai;
    if ($model->exeNendoPatern == "1") {
        $arg["total"]["COMP_GET"]  = $model->sateiTanni + $totalComp + $totalJikougai;
    } else {
        $arg["total"]["COMP_GET"]  = $model->sateiTanni + $totalGet + $totalComp + $totalJikougai;
    }

    if ($model->cmd == "pattern") {
        $extra = $model->gradCredits <= $arg["total"]["COMP_GET"] ? " checked " : "";
        $arg["info"]["GRD_YOTEI"] = knjCreateCheckBox($objForm, "GRD_YOTEI", "1", $extra);
    }

    //選択ラジオ 1:全表示 2:選択
    $opt_disp = array(1, 2);
    $model->disp = $model->disp ? $model->disp : "1";
    $extra = array("id=\"DISP1\" onClick=\"return btn_submit('select');\"", "id=\"DISP2\" onClick=\"return btn_submit('select');\"");
    $radioArray = knjCreateRadio($objForm, "DISP", $model->disp, $extra, $opt_disp, get_count($opt_disp));
    foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

    $result->free();
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $db)
{
    //読込
    $extra = "onclick=\"return btn_submit('pattern');\"";
    $arg["button"]["btn_read"] = knjCreateBtn($objForm, "btn_read", "読込", $extra);

    //生徒情報登録
    $extra  = " onClick=\" wopen('".REQUESTROOT."/X/KNJX_RISHU_KOJIN/knjx_rishu_kojinindex.php?";
    $extra .= "SEND_PRGRID=KNJB1230";
    $extra .= "&SCHREGNO=".$model->schregno."&cmd=main";
    $extra .= "&SEARCH_DIV=".$model->search_div;
    $extra .= "&AUTH=".AUTHORITY;
    $extra .= "&EXE_YEAR=".$model->exe_year;
    $extra .= "&NAME={$model->name}";
    $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\" style=\"width:90px;\"";
    $arg["button"]["btn_schInfo"] = knjCreateBtn($objForm, "btn_schInfo", "生徒情報登録", $extra);

    //単位履修修得表
    $extra =  "onclick=\"newwin('" . SERVLET_URL . "');\" style=\"width:75px;\"";
    $arg["button"]["btn_print1"] = knjCreateBtn($objForm, "btn_print1", "点 票", $extra);

    //更新
    $chk_update = $db->getOne(knjb1230Query::getSchregTextbookDatChk($model));
    if ($chk_update > 0) {
        $arg["info"]["TEXT_BUY"] = "<span style=\"color: #9ff;font-weight: bold;\">教科書登録済</span>";
    }
    if (AUTHORITY >= DEF_UPDATE_RESTRICT) {
        //更新
        $extra  = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //更新前へ
        $extra  = "onclick=\"return btn_submit('updatePrev');\"";
        $arg["button"]["btn_update_prev"] = knjCreateBtn($objForm, "btn_update_prev", "更新後前へ", $extra);
        //更新次へ
        $extra  = "onclick=\"return btn_submit('updateNext');\"";
        $arg["button"]["btn_update_next"] = knjCreateBtn($objForm, "btn_update", "更新後次へ", $extra);

    }
    //取消
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "PROGRAMID", "KNJB1230");
    knjCreateHidden($objForm, "PRGID", "KNJB1230");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "LOGIN_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "SEARCH_DIV", $model->search_div);
    knjCreateHidden($objForm, "knjb1210SubclassOrder", $model->Properties["knjb1210SubclassOrder"]);

}

?>
