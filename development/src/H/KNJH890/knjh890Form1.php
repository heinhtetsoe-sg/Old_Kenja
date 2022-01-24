<?php
class knjh890Form1
{
    function main(&$model) {
        $objForm      = new form;

        //DB接続
        $db = Query::dbCheckOut();

        
        //年度と学期
        if (!isset($model->exp_year)) $model->exp_year = CTRL_YEAR ."-" .CTRL_SEMESTER;
        $arg["EXP_YEAR"] = CTRL_YEAR;
        
        //対象者コンボ
        $opt = array();
        $opt[0] = array("value" => "",
                        "label" => "");
        $opt[1] = array("value" => "0",
                        "label" => "生徒用");
        $opt[2] = array("value" => "1",
                        "label" => "職員用");
        $extra = "onChange=\"btn_submit('change')\";";
        if(!empty($model->csvImport)){
            $extra .= " disabled ";
            $model->Kubun = $model->field["KUBUN"];
        }
        $arg["KUBUN"] = knjCreateCombo($objForm, "KUBUN", $model->field["KUBUN"], $opt, $extra, "1");
        
        //生徒＞学年・クラスコンボ
        if($model->field["KUBUN"] != "1"){
            $opt = array();
            $opt[0] = array("value" => "",
                            "label" => "");
            $opt[1] = array("value" => "1",
                            "label" => "学年");
            $opt[2] = array("value" => "2",
                            "label" => "クラス");
            $extra = "onChange=\"btn_submit('change')\";";
            if(!empty($model->csvImport)){
                $extra .= " disabled ";
            }
            $arg["G_HR"] = knjCreateCombo($objForm, "G_HR", $model->field["G_HR"], $opt, $extra, "1");
            
            if($model->field["G_HR"] != ""){
                //生徒＞学年コンボ
                $optS = array();
                $optS[] = array("value" => "",
                                "label" => "");
                $query = knjh890Query::getGrade($model, $model->field["G_HR"]);
                $result = $db->query($query);
                if($model->field["G_HR"] != "2"){
                    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                        $optS[] = array("value" => $row["GRADE"],
                                        "label" => $row["GRADE_NAME"]);
                    }
                }else{
                    //生徒＞クラスコンボ
                    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                        $optS[] = array("value" => $row["GRADE"]."-".$row["HR_CLASS"],
                                        "label" => $row["HR_NAME"]);
                    }
                }
                if($model->field["G_HR"] != ""){
                    //$extra = "onChange=\"btn_submit('change_sec')\";";
                    $extra = "";
                    if(!empty($model->csvImport)){
                        $extra .= " disabled ";
                    }
                    $arg["GHR_CHOICE"] = knjCreateCombo($objForm, "GHR_CHOICE", $model->field["GHR_CHOICE"], $optS, $extra, "1");
                }
            }
        }
        
        //ファイル
        $arg["FILE"] = knjCreateFile($objForm, "FILE", "", 1024000);

        
        $errSchregData = array();
        $errKnjidData = array();
        $errLoginData = array();
        //データ取得
        if(!empty($model->csvImport)){
            $arg["HYOUZI"] = 1;
            
            //表示するデータのSCHREGNOまたはSTAFFCD
            $sch_staffNo = implode("','", $model->csvImport);
            
            
            if($model->field["KUBUN"] == "1"){
                //職員のとき
                $query = knjh890Query::getStaffData("", $sch_staffNo);
            }else{
                //生徒のとき
                $query = knjh890Query::getSchregData("", $sch_staffNo);
            }
            $result = $db->query($query);
            
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                if($model->field["KUBUN"] == "1"){
                    $row["SCHREG_STAFFCD"] = $row["STAFFCD"];
                    $row["GRADE"] = "";
                    $row["NAME"] = $row["STAFFNAME"];
                }else{
                    $row["SCHREG_STAFFCD"] = $row["SCHREGNO"];
                    if($row["GRADE"] != ""){
                        $row["GRADE"] = $row["GRADE"]."-".$row["HR_CLASS"]."-".$row["ATTENDNO"];
                    }else{
                        $row["GRADE"] = "";
                    }
                }
                
                if(!empty($model->csvData[$row["SCHREG_STAFFCD"]])){
                    //取り込んだデータから会員情報を取得する
                    $query = knjh890Query::getMemberData($model->field["KUBUN"], $row["SCHREG_STAFFCD"], $model->csvData[$row["SCHREG_STAFFCD"]]);
                    $mData = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    
                    if(!in_array($row["SCHREG_STAFFCD"], $model->errSchregno) 
                        && !in_array($model->csvData[$row["SCHREG_STAFFCD"]]["KNJID"], $model->errKnjid) 
                        && !in_array($model->csvData[$row["SCHREG_STAFFCD"]]["LOGINID"], $model->errSchregno) 
                        && $mData["KNJID"] != "" && $mData["KNJCNT"] < 1  && $mData["LOGINCNT"] < 1 && $mData["SCHREGCNT"] < 1
                        && $mData["USER_TYPE"] == $model->field["KUBUN"]){
                        $color = "blue";
                    }else{
                        $color = "red";
                        
                        if($mData["KNJID"] == ""){
                            $notExist[] = $row["SCHREG_STAFFCD"];
                        }
                        
                        if($mData["SCHREGCNT"] > 0){
                            $errSchregData[] = $row["SCHREG_STAFFCD"];
                        }
                        if($mData["KNJCNT"] > 0){
                            if($model->csvData[$row["SCHREG_STAFFCD"]]["KNJID"] != ""){
                                $errKnjidData[] = $model->csvData[$row["SCHREG_STAFFCD"]]["KNJID"];
                            }
                        }
                        if($mData["LOGINCNT"] > 0){
                            if($model->csvData[$row["SCHREG_STAFFCD"]]["LOGINID"] != ""){
                                $errLoginData[] = $model->csvData[$row["SCHREG_STAFFCD"]]["LOGINID"];
                            }
                        }
                        if($mData["USER_TYPE"] != $model->field["KUBUN"]){
                            $errType[] = $row["SCHREG_STAFFCD"];
                        }
                    }
                    
                    $knjid = $model->csvData[$row["SCHREG_STAFFCD"]]["KNJID"] != "" ? $model->csvData[$row["SCHREG_STAFFCD"]]["KNJID"] : $mData["KNJID"];
                    $loginid = $model->csvData[$row["SCHREG_STAFFCD"]]["LOGINID"] != "" ? $model->csvData[$row["SCHREG_STAFFCD"]]["LOGINID"] : $mData["LOGINID"];
                    
                    $row["KNJID"] = "<span style=\"color:".$color.";\">".$knjid."</span>";
                    $row["LOGINID"] = "<span style=\"color:".$color.";\">".$loginid."</span>";
                    $row["INITIAL_PASS"] = "<span style=\"color:".$color.";\">".$mData["INITIAL_PASS"]."</span>";
                    if($mData["USEFLG"] == "T"){
                        $row["USEFLG"] = "<span style=\"color:".$color.";\">"."有効"."</span>";
                    }else if($mData["USEFLG"] == "F"){
                        $row["USEFLG"] = "<span style=\"color:".$color.";\">"."無効"."</span>";
                    }
                    $row["START_DATE"] = "<span style=\"color:".$color.";\">".$mData["START_DATE"]."</span>";
                    $row["END_DATE"] = "<span style=\"color:".$color.";\">".$mData["END_DATE"]."</span>";
                    
                    
                    //配列にデータを追加する
                    $model->csvData[$row["SCHREG_STAFFCD"]]["KNJID"] = $knjid;
                    $model->csvData[$row["SCHREG_STAFFCD"]]["LOGINID"] = $loginid;
                    
                    if($color == "red"){
                        //配列からデータを削除する
                        $model->csvImport = array_diff($model->csvImport, array($row["SCHREG_STAFFCD"]));
                    }
                }
                
                $arg["data"][] = $row;
            }
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        knjCreateHidden($objForm, "SELECTID", $selectid);
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["start"] = $objForm->get_start("edit", "POST", "knjh890index.php", "", "edit");
        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjh890Form1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //割り当て
    if(!empty($model->csvImport) && !empty($arg["data"])){
        //確定
        $extra = " onclick=\"btn_submit('update');\"";
        if($model->errorFlg != 0){
            $extra .= " disabled ";
        }
        $arg["button"]["btn_update"] = createBtn($objForm, "btn_update", "割り当て", $extra);
    }
    if(!empty($model->csvData)){
        //取込データリセット
        $extra = " onclick=\"btn_submit('');\"";
        $arg["button"]["btn_reset"] = createBtn($objForm, "btn_reset", "取込データリセット", $extra);
    }
    
    //CSV出力
    $extra = " onclick=\"btn_submit('csv');\"";
    if($model->errorFlg != 0){
        $extra .= " disabled ";
    }
    $arg["button"]["btn_csv"] = createBtn($objForm, "btn_csv", "CSV出力", $extra);
    //CSV取込
    $extra = " onclick=\"btn_submit('import');\"";
    if($model->errorFlg != 0){
        $extra .= " disabled ";
    }
    $arg["button"]["btn_import"] = createBtn($objForm, "btn_import", "CSV取込", $extra);
    //終了
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    $objForm->ae(createHiddenAe("cmd"));
    $objForm->ae(createHiddenAe("SCHREGNO", $model->schregno));
    $objForm->ae(createHiddenAe("sort", $model->sort));
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae( array("type"      => "button",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra));
    return $objForm->ge($name);
}

//チェックボックス作成
function createCheckBox(&$objForm, $name, $value, $extra, $multi)
{

    $objForm->ae( array("type"      => "checkbox",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "multiple"  => $multi));

    return $objForm->ge($name);
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $cnt = 0;
    if ($blank == "blank") $opt[] = array('label' => "", 'value' => "");
    if ($blank == "blank") $cnt++;
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
        if ($name == "CHAIRCD") {
            knjCreateHidden($objForm, "LIST_CHAIRCD" . $row["VALUE"], $cnt);
            $cnt++;
        }
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//Hidden作成ae
function createHiddenAe($name, $value = "")
{
    $opt_hidden = array();
    $opt_hidden = array("type"      => "hidden",
                        "name"      => $name,
                        "value"     => $value);
    return $opt_hidden;
}

?>
