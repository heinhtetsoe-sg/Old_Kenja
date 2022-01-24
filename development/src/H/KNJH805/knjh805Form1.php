<?php
class knjh805Form1
{
    function main(&$model) {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjh805index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();
        
        //年度と学期
        if (!isset($model->exp_year)) $model->exp_year = CTRL_YEAR ."-" .CTRL_SEMESTER;
        //年度切り替えられるようにしたい
        $extra = " onChange=\"btn_submit('')\";";
        $query = knjh805Query::getYear();
        makeCmb($objForm, $arg, $db, $query, "EXP_YEAR", $model->topfield["EXP_YEAR"], $extra, 1, "");
        //学期も
        $extra = "onChange=\"btn_submit('')\";";
        $query = knjh805Query::getSemester($model->topfield["EXP_YEAR"]);
        makeCmb($objForm, $arg, $db, $query, "SEMESTER", $model->topfield["SEMESTER"], $extra, 1, "");

        //科目コンボ
        $extra = "onChange=\"btn_submit('subclasscd')\";";
        $query = knjh805Query::getSubclassMst($model);
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->topfield["SUBCLASSCD"], $extra, 1, "blank");

        //講座コンボ
        $extra = "onChange=\"btn_submit('chaircd')\";";
        $query = knjh805Query::selectChairQuery($model);
        makeCmb($objForm, $arg, $db, $query, "CHAIRCD", $model->topfield["CHAIRCD"], $extra, 1, "blank");

        
        
        //表示内容
        //教科
        $extra = " onchange=\"btn_submit('change');\"";
        $query = knjh805Query::getKyouka();
        $kresult = $db->query($query);
        $opt = array();
        //「すべて」はなし　エラーチェック有
        $opt[0] = array("label" => "",
                        "value" => "");
        while($row = $kresult->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt[] = array("label"  => $row["LABEL"],
                           "value"  => $row["VALUE"]);
        }
        $arg["data"]["KYOKA"] = knjCreateCombo($objForm, "KYOKA", $model->field["KYOKA"], $opt, $extra, "1");
        
        //科目
        $extra = " onchange=\"btn_submit('change');\"";
        $opt2 = array();
        $opt2[0] = array("label" => "",
                         "value" => "");
        if($model->field["KYOKA"] != ""){
            //Queryで中身取得
            $query = knjh805Query::getKamoku($model->topfield["EXP_YEAR"], $model->field["KYOKA"]);
            $Result = $db->query($query);
            while($row = $Result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt2[] = array("label"  => $row["LABEL"],
                                "value"  => $row["VALUE"]);
            }
        }
        $arg["data"]["KAMOKU"] = knjCreateCombo($objForm, "KAMOKU", $model->field["KAMOKU"], $opt2, $extra, "1");
        
        //テスト種類
        $extra = "";
        $opt3 = array();
        $opt3[0] = array("label" => "",
                         "value" => "");
        $query = knjh805Query::getTestLevel($model->topfield["EXP_YEAR"], $model->field["KYOKA"], $model->field["KAMOKU"]);
        $Result = $db->query($query);
        if(!empty($Result)){
            while($row = $Result->fetchRow(DB_FETCHMODE_ASSOC)){
                $opt3[] = array("label" => $row["LABEL"],
                                "value" => $row["VALUE"]);
            }
        }
        /*$kind[0] = "すべて";
        $kind[1] = "易";
        $kind[2] = "やや易";
        $kind[3] = "標準レベル";
        $kind[4] = "やや難";
        $kind[5] = "難";*/
        $arg["data"]["TEST_KIND"] = knjCreateCombo($objForm, "TEST_KIND", $model->field["TEST_KIND"], $opt3, $extra, "1");
        
        //受験日時
        $extra = "";
        $arg["data"]["DATE"] = View::popUpCalendar2($objForm, "DATE", str_replace("-", "/", $model->field["DATE"]), "", "", "");
        
        $hour = range(0, 23);
        $opt4 = array();
        $opt4[] = array("label"  => "",
                        "value"  => "");
        foreach($hour as $key => $val){
            $opt4[] = array("label"  => $val,
                            "value"  => "{$key}");
        }
        $arg["data"]["F_HOUR"] = knjCreateCombo($objForm, "F_HOUR", $model->field["F_HOUR"], $opt4, $extra, "1");
        $arg["data"]["T_HOUR"] = knjCreateCombo($objForm, "T_HOUR", $model->field["T_HOUR"], $opt4, $extra, "1");
        
        $minutes = range(0, 45, 15);
        $opt5 = array();
        $opt5[] = array("label"  => "",
                        "value"  => "");
        foreach($minutes as $key => $val){
            $opt5[] = array("label"  => sprintf("%02d",$val),
                            "value"  => "{$val}");
        }
        $arg["data"]["F_MIN"] = knjCreateCombo($objForm, "F_MIN", $model->field["F_MIN"], $opt5, $extra, "1");
        $arg["data"]["T_MIN"] = knjCreateCombo($objForm, "T_MIN", $model->field["T_MIN"], $opt5, $extra, "1");
        
        if($model->mode != 0){
            
            //テストの実施回取得    回数の個数取得SQL
            $query = knjh805Query::getKnjid($model->topfield, $model->field, "2");
            $cCnt = $db->getOne($query);
            if($cCnt < 1){
                $model->setMessage("対象のデータがありません。");
                $model->mode = 0;
            }
        }
        
        if($model->mode != 0){
            $arg["HYOUZI"] = 1;

            //テストの実施回取得    回数のみ取得できるSQL
            $query = knjh805Query::getKnjid($model->topfield, $model->field, "1");
            $cResult = $db->query($query);
            
            //適当なデータ
            $right_h = "";
            $sql = "";
            $cnm = "";
            $takeTurn = array();
            $takeCnt = 0;
            while($cRow = $cResult->fetchRow(DB_FETCHMODE_ASSOC)){
                $right_h .= "<th class=\"no_search\">第".$cRow["TAKE_TURN"]."回<BR>".$cRow["TEST_KIND_NAME"]."</th>";
                $sql .= $cnm."'".$cRow["TEST_ID"]."'";
                $cnm = ",";
                $takeTurn[$takeCnt] = $cRow["TEST_ID"];
                $takeCnt++;
            }

            //対象生徒取得
            //$query = knjh805Query::getKnjid($model->topfield,$model->field);
            $query = knjh805Query::getAllKnjid($model->topfield);      //対象のクラス全員を表示する
            $sResult = $db->query($query);
            while($sRow = $sResult->fetchRow(DB_FETCHMODE_ASSOC)){
                $resu["left_b"] = "<td nowrap>".$sRow["GRADE"]."-".$sRow["HR_CLASS"]."-".$sRow["ATTENDNO"]."</td><td nowrap>".$sRow["NAME"]."</td>";
                
                //対象の生徒の対象回データ取得
                $query = knjh805Query::getScore($model->topfield, $model->field,$sRow["KNJID"],$sql);
                $tResult = $db->query($query);
                $count = 0;
                $resu["right_b"] = "";
                $create = 0;
                while($tRow = $tResult->fetchRow(DB_FETCHMODE_ASSOC)){
                    $key = array_search($tRow["TEST_ID"], $takeTurn);
                    for($t=$count;$t<$key;$t++){
                        $resu["right_b"] .= "<td nowrap>-</td>";
                    }
                    $count = $t;
                    $resu["right_b"] .= "<td nowrap><span style=\"font-weight:bold;font-size:large;\">".$tRow["GET_POINT"]."/".$tRow["POINT"]."</span><span style=\"font-size:small;\">(".$tRow["TAKEDATE"].")</span></td>";
                    
                    $count++;
                    $create = 1;
                
                }
                if($count < $takeCnt){
                    for($a = $count; $a < $takeCnt; $a++){
                        $resu["right_b"] .= "<td nowrap>-</td>";
                        $create = 1;
                    }
                }
                if($create != 0){
                    $arg["data2"][] = $resu;
                }
            }
            //#holizonの代わりに直接値決めて入れる
            $width = 200*$takeCnt;
            $arg["width"] = "style=\"width:".$width."px;\"";
            $arg["right_h"] = $right_h;
        }
        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjh805Form1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //表示
    $extra = " onclick=\"btn_submit('hyouzi');\"";
    $arg["button"]["btn_search"] = createBtn($objForm, "btn_search", "表 示", $extra);
    //クリア
    $extra = " onclick=\"btn_submit('clear');\"";
    $arg["button"]["btn_clear"] = createBtn($objForm, "btn_clear", "クリア", $extra);
    //CSV
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
