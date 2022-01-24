<?php
class knjo151Form1
{
    function main(&$model) {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjo151index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        //extraセット
        $extraInt = "onblur=\"this.value=toInteger(this.value)\";";

        //生徒データ表示
        makeStudentInfo($objForm, $arg, $db, $model);

        //明細
        makeMeisai($objForm, $arg, $db, $model);
        
        //SCHREG_STUYREC_DATに更新済のデータがあるか確認
        $query = knjo151Query::getStudyRecCnt($model->schregno);
        $cnt = $db->getOne($query);
        if($cnt > 0){
            $model->dataCnt = 1;
        }else{
            $model->dataCnt = 0;
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);
        
        switch (VARS::get("cmd")) {
            case "top_update" :
            case "top_delete" :
                $arg["jscript"] = "window.open('knjo151index.php?cmd=right_list','right_frame');";
                break;
            case "edit" :
                $arg["jscript"] = "window.open('knjo151index.php?cmd=edit_src','edit_frame');";
                break;
        }

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjo151Form1.html", $arg);
    }
}

//生徒データ表示
function makeStudentInfo(&$objForm, &$arg, $db, &$model)
{
    $info = $db->getRow(knjo151Query::getStudentInfoData($model), DB_FETCHMODE_ASSOC);
    $grdYoteiAdd = floor($info["TOKKATU_JISU"] / 10) + 1;
    if (is_array($info)) {
        foreach ($info as $key => $val) {
            if (in_array($key, array("ENT_DATE", "GRD_DATE"))) {
                $splitVal = split("-", $val);
                $val = $splitVal[0]."年".$splitVal[1]."月";
                if ($key == "ENT_DATE") {
                    $setRow["GRD_SCHEDULE_DATE"] = $splitVal[0] + $grdYoteiAdd."年3月";
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

    $query = knjo151Query::getAnother($db, $model);
    $result = $db->query($query);
    $rowCnt = 0;

    $hidden = "";
    $hcnm = "";
    
    $model->dubble = 0;
    $before = "";
    
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //hiddenに入れるデータ用
        $hidden .= $hcnm.$row["TYPE"]."-".$row["DATA_ROW"];
        $hcnm = ",";
        
        //重複チェック
        if($model->dubble != 1){
            $subclass = $row["REP_SUBCLASSCD"];
            if($before == $subclass){
                $model->dubble = 1;
            }
        }
        $before = $row["REP_SUBCLASSCD"];
        
        //学設教科だったら
        if($row["SCHOOL_CLASS_NAME"] != ""){    //もしくはCLASS_NAMEが学校設定教科コードだったら、にする
            $row["APP_CLASSNAME"] = $row["SCHOOL_CLASS_NAME"];
        }
        if($row["SCHOOL_SUBCLASS_NAME"] != ""){
            $row["APP_SUBCLASSNAME"] = $row["SCHOOL_SUBCLASS_NAME"];
        }
        if($row["GET_CREDIT"] == ""){
            $row["GET_CREDIT"] = $row["SCHOOL_SUBCLASS_TANNI"];
        }
        
        
        $rowCnt++;

        $arg["data2"][] = $row;
    }
    knjCreateHidden($objForm, "ROWDATA", $hidden);
    
    $result->free();
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //確定ボタン
    $extra = "onclick=\"return btn_submit2('top_update', '{$model->dataCnt}', '{$model->dubble}');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "確 定", $extra);
    //終了
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");

    //学籍基礎
    $extra  = " onClick=\" wopen('".REQUESTROOT."/A/KNJWA130S1/knjwa130s1index.php?";
    $extra .= "SCHREGNO=".$model->schregno."&YEAR=".CTRL_YEAR."&cmd=main&PROGRAMID=KNJo151&CALLID=KNJo151";
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
