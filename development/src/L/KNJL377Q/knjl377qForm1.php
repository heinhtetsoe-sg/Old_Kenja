<?php

require_once('for_php7.php');

class knjl377qForm1
{
    function main(&$model)
    {
        //権限チェック
        if ($model->auth != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;

        
        $extraInt   = "onblur=\"this.value=toInteger(this.value)\" ";   //数字に
        
        $db = Query::dbCheckOut();
        
        //SAT_HOPE_WRK_DATのデータ数カウント
        $query = knjl377qQuery::getHopeWrkCnt();
        $hopeWrk = $db->getOne($query);
        $data["ALL_CNT"] = $hopeWrk;
        
        if($hopeWrk > 0){
            $errorCnt = 0;
        }else{
            $errorCnt = 1;
        }
        
        if($data["ALL_CNT"] > 0){
            //エラーデータチェック
            //受験番号重複
            $query = knjl377qQuery::checkSatno();
            $satCnt = $db->getOne($query);
            $data["EXAM_CNT"] = $satCnt;
            if($satCnt > 0){
                //修正ボタン作成
                $extra = "onclick=\"btn_submit('sat');\"";
                $data["EXAM_BTN"] = knjCreateBtn($objForm, "EXAM_BTN", "修 正", $extra);
                $errorCnt++;
            }else if($model->mode == "1"){
                $model->mode = "";
            }
            //受験番号不一致
            $query = knjl377qQuery::checkSatConnect();
            $satConCnt = $db->getOne($query);
            $data["SAT_CNT"] = $satConCnt;
            if($satConCnt > 0){
                //修正ボタン作成
                $extra = "onclick=\"btn_submit('sat_connect');\"";
                $data["SAT_BTN"] = knjCreateBtn($objForm, "SAT_BTN", "修 正", $extra);
                $errorCnt++;
            }else if($model->mode == "2"){
                $model->mode = "";
            }
            
            if($satCnt == 0 && $satConCnt == 0){
                //生年月日不一致
                $query = knjl377qQuery::checkError("BIRTHDAY");
                $birthCnt = $db->getOne($query);
                $data["BIRTH_CNT"] = $birthCnt;
                if($birthCnt > 0){
                    //修正ボタン作成
                    $extra = "onclick=\"btn_submit('birth');\"";
                    if($satCnt > 0 || $satConCnt > 0){
                        $extra .= " disabled ";
                    }
                    $data["BIRTH_BTN"] = knjCreateBtn($objForm, "BIRTH_BTN", "修 正", $extra);
                    $errorCnt++;
                }else if($model->mode == "3"){
                    $model->mode = "";
                }
                
                //性別不一致
                $query = knjl377qQuery::checkError("SEX");
                $sexCnt = $db->getOne($query);
                $data["SEX_CNT"] = $sexCnt;
                if($sexCnt > 0){
                    //修正ボタン作成
                    $extra = "onclick=\"btn_submit('sex');\"";
                    if($satCnt > 0 || $satConCnt > 0){
                        $extra .= " disabled ";
                    }
                    $data["SEX_BTN"] = knjCreateBtn($objForm, "SEX_BTN", "修 正", $extra);
                    $errorCnt++;
                }else if($model->mode == "4"){
                    $model->mode = "";
                }
                
                //出身学校不一致
                $query = knjl377qQuery::checkError("FINSCHOOL");
                $schCnt = $db->getOne($query);
                $data["SCH_CNT"] = $schCnt;
                if($schCnt > 0){
                    //修正ボタン作成
                    $extra = "onclick=\"btn_submit('sch');\"";
                    if($satCnt > 0 || $satConCnt > 0){
                        $extra .= " disabled ";
                    }
                    $data["SCH_BTN"] = knjCreateBtn($objForm, "SCH_BTN", "修 正", $extra);
                    $errorCnt++;
                }else if($model->mode == "5"){
                    $model->mode = "";
                }
            }
            
        }
        $arg["data"] = $data;
        
        //更新ボタンの作成有無
        if($errorCnt > 0){
            $model->createUpdate = 0;
        }else{
            $model->createUpdate = 1;
        }
        

        //ファイルからの取り込み
        $arg["FILE"] = knjCreateFile($objForm, "FILE", "", 1024000);
        
        //更新用にRECNOを配列に保存
        $model->recNo = array();
        $recCnt = 0;
        
        //修正画面
        if($model->mode != "1" && $model->mode != "2" && $model->mode != ""){
            if($model->mode == "3"){
                $arg["ERROR"] = "生年月日";
                $field = "BIRTHDAY";
            }else if($model->mode == "4"){
                $arg["ERROR"] = "性別";
                $field = "SEX";
            }else{
                $arg["ERROR"] = "出身学校";
                $field = "FINSCHOOL";
            }
            //エラーデータのみ抽出
            $query = knjl377qQuery::getError($field);
            $result = $db->query($query);
            
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $model->recNo[$recCnt] = $row["RECNO"];
                
                $otr["RECNO"] = $row["RECNO"];
                $otr["SAT_NO"] = $row["SAT_NO"];
                
                if($model->mode == "3"){
                    
                    $otr["ERROR"] = View::popUpCalendar($objForm, "ERROR{$row["RECNO"]}", str_replace("-", "/", $row["BIRTHDAY"]),"");
                    
                }else if($model->mode == "4"){
                    $opt = array();
                    $opt[0] = array("value" => "",
                                    "label" => "");
                    $opt[1] = array("value" => "1",
                                    "label" => "1：男");
                    $opt[2] = array("value" => "2",
                                    "label" => "2：女");
                    $extra = "";
                    $otr["ERROR"] = knjCreateCombo($objForm, "ERROR{$row["RECNO"]}", $row["SEX"], $opt, $extra, "1");
                }else{
                    
                    $extra = $extraInt;
                    $row["FINSCHOOL"] = sprintf("%05d", $row["FINSCHOOL"]);
                    $otr["ERROR"] = "20".knjCreateTextBox($objForm, $row["FINSCHOOL"], "ERROR{$row["RECNO"]}", 7, 5, $extra);
                }
                
                $otr["BIRTHDAY"] = str_replace("-","/",$row["APP_BIRTHDAY"]);
                $otr["SEX"] = $row["APP_SEX"];
                $otr["FINSCHOOL"] = $row["APP_FINSCHOOL"]."：".$row["APP_FINSCHOOLNAME"];
                
                $arg["otr"][] = $otr;
                $recCnt++;
            }
            
            
        }else if($model->mode == "1"){
            //受験番号エラー
            //重複している受験番号を先に取得して対象の列だけ色を付けたい
            $query = knjl377qQuery::getDubSatNo();
            $result = $db->query($query);
            $dubSat = array();
            $dubCnt = 0;
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $dubSat[$dubCnt] = $row["SAT_NO"];
                $dubCnt++;
            }
            //SAT_HOPE_WRK_DATの全データを取得
            $query = knjl377qQuery::getSatError();
            $result = $db->query($query);
            
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $model->recNo[$recCnt] = $row["RECNO"];
                
                $exam["RECNO"] = $row["RECNO"];
                
                $extra = "";
                $exam["SAT_NO"] = knjCreateTextBox($objForm, $row["SAT_NO"], "ERROR{$row["RECNO"]}", 7, 5, $extra);
                
                $exam["BIRTHDAY"] = str_replace("-", "/", $row["BIRTHDAY"]);
                $exam["SEX"] = $row["SEX"];
                $exam["FINSCHOOLCD"] = $row["FINSCHOOLCD"];
                $exam["FINSCHOOLNAME"] = $row["FINSCHOOL_NAME"];
                
                //重複しているデータだったら色付けたい
                if(in_array($row["SAT_NO"], $dubSat)){
                    $exam["color"] = "#fcffcc";
                }else{
                    $exam["color"] = "#ffffff";
                }
                
                $arg["exam"][] = $exam;
                $recCnt++;
            }
            
        }else if($model->mode == "2"){
            //受験番号が一致しないSAT_HOPE_WRK_DATデータ取得
            $query = knjl377qQuery::getWrkData();
            $result = $db->query($query);
            
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $model->recNo[$recCnt] = $row["RECNO"];
                
                $sat["RECNO"] = $row["RECNO"];
                
                $extra = "";
                $sat["SAT_NO"] = knjCreateTextBox($objForm, $row["SAT_NO"], "ERROR{$row["RECNO"]}", 7, 5, $extra);
                
                $sat["BIRTHDAY"] = str_replace("-", "/", $row["BIRTHDAY"]);
                $sat["SEX"] = $row["SEX"];
                $sat["FINSCHOOLCD"] = $row["FINSCHOOL"];
                $sat["FINSCHOOLNAME"] = $row["FINSCHOOL_NAME"];
                
                $arg["sat"][] = $sat;
                $recCnt++;
            }
            
            //受験番号が一致したSAT_HOPE_WRK_DATがないSAT_APP_FORM_MSTデータ
            $query = knjl377qQuery::getAppData();
            $result = $db->query($query);
            
            $rowCnt = 1;
            
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
                $app["RECNO"] = $rowCnt;
                
                $app["SAT_NO"] = $row["SAT_NO"];
                $app["BIRTHDAY"] = str_replace("-", "/", $row["BIRTHDAY"]);
                $app["SEX"] = $row["SEX"];
                $app["FINSCHOOLCD"] = $row["SCHOOLCD"];
                $app["FINSCHOOLNAME"] = $row["FINSCHOOL_NAME"];
                
                $arg["app"][] = $app;
                $rowCnt++;
            }
        }
        
        if($model->mode != ""){
            //修正完了ボタン
            $extra = "onclick=\"btn_submit('wrkUpdate');\"";
            $arg["btn_wrkUpdate"] = knjCreateBtn($objForm, "btn_wrkUpdate", "修正完了", $extra);
        }
        
        //ボタン作成
        makeButton($objForm, $arg, $db, $model);

        Query::dbCheckIn($db);

        //hiddenを作成する
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "PRGID", "KNJl377q");
        knjCreateHidden($objForm, "TEMPLATE_PATH");

        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjl377qindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl377qForm1.html", $arg);
    }

}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    if ($name == "YEAR") {
        $value = ($value != "" && $value_flg) ? $value : CTRL_YEAR;
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//ボタン作成
function makeButton(&$objForm, &$arg, $db, $model)
{
    //実行ボタン
    $extra = "onclick=\"btn_submit('import');\"";
    $arg["button"]["btn_import"] = knjCreateBtn($objForm, "btn_import", "実 行", $extra);
    
    //CSV出力ボタン
    $extra = "onclick=\"btn_submit('exec');\"";
    $arg["button"]["btn_export"] = knjCreateBtn($objForm, "btn_export", "CSV出力", $extra);
    
    //更新ボタン
    if($model->createUpdate != 0){
        $extra = "onclick=\"btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "志望校データ作成", $extra);
    }

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
