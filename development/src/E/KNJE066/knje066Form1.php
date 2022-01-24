<?php

require_once('for_php7.php');

class knje066Form1
{
    function main(&$model) {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knje066index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        //extraセット
        $extraInt = "onblur=\"this.value=toInteger(this.value)\";";

        //生徒データ表示
        makeStudentInfo($objForm, $arg, $db, $model);

        $allCol = 6;
        if ($model->Properties["useProvFlg"] == '1') {
            $arg["useProvFlg"] = "1";
            $allCol = 7;
        }
        $arg["ALL_COL"] = $allCol;

        //ALLチェック
        $extra  = " id=\"CHECKALL\" ";
        $extra .= "onClick=\"return check_all(this);\"";
        $arg["data"]["CHECKALL"] = knjCreateCheckBox($objForm, "CHECKALL", "", $extra, "");

        //明細
        makeMeisai($objForm, $arg, $db, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);
        
        switch (VARS::get("cmd")) {
            case "top_update" :
            case "top_delete" :
                $arg["jscript"] = "window.open('knje066index.php?cmd=right_list','right_frame');";
                break;
            case "edit" :
                $arg["jscript"] = "window.open('knje066index.php?cmd=edit_src','edit_frame');";
                break;
        }

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knje066Form1.html", $arg);
    }
}

//生徒データ表示
function makeStudentInfo(&$objForm, &$arg, $db, &$model)
{
    $info = $db->getRow(knje066Query::getStudentInfoData($model), DB_FETCHMODE_ASSOC);
    $grdYoteiAdd = floor((int)$info["TOKKATU_JISU"] / 10) + 1;
    if (is_array($info)) {
        $model->schoolKind = $info["SCHOOL_KIND"];
        foreach ($info as $key => $val) {
            if (in_array($key, array("ENT_DATE", "GRD_DATE"))) {
                $splitVal = preg_split("/-/", $val);
                $val = $splitVal[0]."年".$splitVal[1]."月";
                if ($key == "ENT_DATE") {
                    $setRow["GRD_SCHEDULE_DATE"] = (int)$splitVal[0] + (int)$grdYoteiAdd."年3月";
                }
            }
            $setRow[$key] = $val;
        }
    }
    $arg["info"] = $setRow;
    //チェックボックス
    $checked = $setRow["EDUCATION_REC_PUT_FLG"] == "1" ? "checked" : "";
    $extra = " id=\"EDUCATION_REC_PUT_FLG[]\" ";
    $arg["EDUCATION_REC_PUT_FLG"] = knjCreateCheckBox($objForm, "EDUCATION_REC_PUT_FLG", "1", $checked." ".$extra, "1");
    $model->schregno = $info["SCHREGNO"];
}

//明細
function makeMeisai(&$objForm, &$arg, $db, $model)
{

    $time = array();

    $query = knje066Query::getAnother($db, $model);
    $result = $db->query($query);
    $rowCnt = 0;
    $totalCredit = 0;
    $totalZensekiCredit = 0;
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //チェックボックス
        $subclassCd = $row["CLASSCD"]."-".$row["SCHOOL_KIND"]."-".$row["CURRICULUM_CD"]."-".$row["SUBCLASSCD"];
        $rep_subclassCd = $row["REP_CLASSCD"]."-".$row["REP_SCHOOL_KIND"]."-".$row["REP_CURRICULUM_CD"]."-".$row["REP_SUBCLASSCD"];
        $checkVal = $row["YEAR"].":".$row["SCHREGNO"].":".$row["SCHOOLCD"].":";
        $checkVal .= $subclassCd.":";
        $checkVal .= $rep_subclassCd.":";
        $checkVal .= $row["FORMER_REG_SCHOOLCD"].":";
        $checkVal .= $row["GET_DIV"];
        $row["DELCHK"] = knjCreateCheckBox($objForm, "DELCHK", $checkVal, "", "1");

        if ($row["SCHOOLCD"] == '0') {
            $row["GET_DIV_NAME"] = '在籍中';
        } else if ($row["SCHOOLCD"] == '1') {
            $row["GET_DIV_NAME"] = '前籍校';
        } else {
            $row["GET_DIV_NAME"] = '高校認定';
        }
        //仮評定（在籍中のみ）
        if ($row["PROV_FLG"]) {
            $row["PROV_FLG"] = 'レ';
        }
        $row["ANOTHER_NAME"] = $row["CURRICULUM_CD_NAME"]."　".$row["GET_METHOD_NAME"];
        $rowCnt++;
        $totalCredit += (int)$row["GET_CREDIT"];
        $totalZensekiCredit += (int)$row["REP_GET_CREDIT"];

        $arg["data2"][] = $row;
    }
    $arg["TOTAL_CREDIT"] = $totalCredit;
    $arg["REP_TOTAL_CREDIT"] = $totalZensekiCredit;
    $result->free();
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //削除
    if (AUTHORITY >= DEF_UPDATE_RESTRICT) {
        $extra = "onclick=\"return btn_submit('top_delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
    }
    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

    //学籍基礎
    $extra  = " onClick=\" wopen('".REQUESTROOT."/A/KNJWA130S1/knjwa130s1index.php?";
    $extra .= "SCHREGNO=".$model->schregno."&YEAR=".CTRL_YEAR."&cmd=main&PROGRAMID=KNJE066&CALLID=KNJE066";
    $extra .= "&AUTH=".AUTHORITY;
    
    if ($model->subwin) {
        $extra .= "&SUBWIN=".$model->subwin."','".$model->subwin."',0,0,screen.availWidth,screen.availheight);\"";
        
    }
    else {
        $extra .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availheight);\"";
        
    }
    if (!$model->schregno) $extra .= " disabled";
    $arg["button"]["btn_schreg"] = knjCreateBtn($objForm, "btn_schreg", VARS::request("SUBWIN")."学籍基礎", $extra);
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "APPLICANTNO", $model->applicantno);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    
    knjCreateHidden($objForm, "SUBWIN", $model->subwin);
}

?>
