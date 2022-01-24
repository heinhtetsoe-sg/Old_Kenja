<?php
class knjh885Form1
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
        $arg["KUBUN"] = knjCreateCombo($objForm, "KUBUN", $model->field["KUBUN"], $opt, $extra, "1");
        
        //生徒＞学年・クラスコンボ
        if($model->field["KUBUN"] == "0"){
            $opt = array();
            $opt[0] = array("value" => "",
                            "label" => "");
            $opt[1] = array("value" => "1",
                            "label" => "学年");
            $opt[2] = array("value" => "2",
                            "label" => "クラス");
            $extra = "onChange=\"btn_submit('change')\";";
            $arg["G_HR"] = knjCreateCombo($objForm, "G_HR", $model->field["G_HR"], $opt, $extra, "1");
            
            if($model->field["G_HR"] != ""){
                //生徒＞学年コンボ
                $optS = array();
                $optS[] = array("value" => "",
                                "label" => "");
                $query = knjh885Query::getGrade($model, $model->field["G_HR"]);
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
                    $extra = "onChange=\"btn_submit('change_sec')\";";
                    //$extra = "";
                    $arg["GHR_CHOICE"] = knjCreateCombo($objForm, "GHR_CHOICE", $model->field["GHR_CHOICE"], $optS, $extra, "1");
                }
            }
        }
        
        //全チェック
        $extra = " onclick=\"check_all(this);\"";
        if($model->field["CHECKALL"] == "1"){
            $extra .= " checked ";
        }
        $arg["CHECK_ALL"] = knjCreateCheckBox($objForm, "CHECKALL", $model->field["CHECKALL"], $extra, "");
        
        
        //データチェック
        if($model->field["KUBUN"] != ""){
            if($model->field["KUBUN"] == "1"){
                //職員のとき
                $query = knjh885Query::getStaffData($model->field);
            }else{
                //生徒のとき
                $query = knjh885Query::getSchregData($model->field);
            }
            $cnt = $db->getOne($query);
            if($cnt > 0){
                $flg = 1;
            }else{
                $flg = 0;
            }
        }

        //データ取得
        if($flg != 0){
            $arg["HYOUZI"] = 1;
            
            $selectid = "";
            $cnm = "";
            
            //割り当てられるKNJID取得
            $query = knjh885Query::getKnjid($model->field);
            $result = $db->query($query);
            
            $knjCnt = 0;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $knjid[$knjCnt] = $row;
                $knjCnt++;
            }
            $count = count($model->field["CHECK"]);
            if($count > $knjCnt){
                $model->setMessage("割り当てるデータが不足しています。\\n確認してください。");
            }
            
            if($model->field["KUBUN"] == "1"){
                //職員のとき
                $query = knjh885Query::getStaffData($model->field);
            }else{
                //生徒のとき
                $query = knjh885Query::getSchregData($model->field);
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
                        //$row["GRADE"] = number_format($row["GRADE"])."-".number_format($row["HR_CLASS"])."-".$row["ATTENDNO"];
                        $row["GRADE"] = $row["GRADE"]."-".$row["HR_CLASS"]."-".$row["ATTENDNO"];
                    }else{
                        $row["GRADE"] = "";
                    }
                }
                
                if($row["KNJID"] == ""){
                    //チェックボックス
                    $extra = "";
                    if(!empty($model->field["CHECK"]) && in_array($row["SCHREG_STAFFCD"], $model->field["CHECK"])){
                        $extra = " checked";
                    }
                    $row["CHECK"] = knjCreateCheckBox($objForm, "CHECK[]", $row["SCHREG_STAFFCD"], $extra, "");
                }
                
                if($row["KNJID"] == "" && in_array($row["SCHREG_STAFFCD"], $model->field["CHECK"])){
                    $key = array_search($row["SCHREG_STAFFCD"], $model->field["CHECK"]);
                    
                    $row["KNJID"] = "<span style=\"color:blue;\">".$knjid[$key]["KNJID"]."</span>";
                    $row["LOGINID"] = "<span style=\"color:blue;\">".$knjid[$key]["LOGINID"]."</span>";
                    $row["INITIAL_PASS"] = "<span style=\"color:blue;\">".$knjid[$key]["INITIAL_PASS"]."</span>";
                    if($knjid[$key]["USEFLG"] == "T"){
                        $row["USEFLG"] = "<span style=\"color:blue;\">"."有効"."</span>";
                    }else if($knjid[$key]["USEFLG"] == "F"){
                        $row["USEFLG"] = "<span style=\"color:blue;\">"."無効"."</span>";
                    }
                    $row["START_DATE"] = "<span style=\"color:blue;\">".$knjid[$key]["START_DATE"]."</span>";
                    $row["END_DATE"] = "<span style=\"color:blue;\">".$knjid[$key]["END_DATE"]."</span>";
                    
                    $selectid .= $cnm.$knjid[$key]["KNJID"];
                    $cnm = ",";
                }else{
                    if($row["USEFLG"] == "T"){
                        $row["USEFLG"] = "有効";
                    }else if($row["USEFLG"] == "F"){
                        $row["USEFLG"] = "無効";
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

        $arg["start"] = $objForm->get_start("edit", "POST", "knjh885index.php", "", "edit");
        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjh885Form1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //割り当て
    $extra = " onclick=\"btn_submit('select');\"";
    $arg["button"]["btn_select"] = createBtn($objForm, "btn_select", "割り当て", $extra);
    if(!empty($model->field["CHECK"])){
        //確定
        $extra = " onclick=\"btn_submit('update');\"";
        $arg["button"]["btn_update"] = createBtn($objForm, "btn_update", "確 定", $extra);
    }
    //CSV出力
    $extra = " onclick=\"btn_submit('csv');\"";
    $arg["button"]["btn_csv"] = createBtn($objForm, "btn_csv", "CSV出力", $extra);

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
