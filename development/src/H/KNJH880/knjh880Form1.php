<?php
class knjh880Form1
{
    function main(&$model) {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjh880index.php", "", "edit");
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
        
        //表示対象コンボ
        $opt = array();
        $opt[0] = array("value" => "",
                        "label" => "すべて");
        $opt[1] = array("value" => "1",
                        "label" => "リンク済");
        $opt[2] = array("value" => "2",
                        "label" => "リンク未");
        $extra = "onChange=\"btn_submit('change')\";";
        $arg["LINK"] = knjCreateCombo($objForm, "LINK", $model->field["LINK"], $opt, $extra, "1");
        
        //並び替え用
        if($model->order != "KNJID"){
            $knjidOrder = "";
            $gradeOrder = "class=\"choice\"";
        }else{
            $knjidOrder = "class=\"choice\"";
            $gradeOrder = "";
        }
        $arg["TH_KNJID"] = "<a onclick=\"orderBy('KNJID');\"".$knjidOrder.">会員番号▼</a>";
        if($model->field["KUBUN"] != "1"){
            $arg["TH_GRADE"] = "年組番";
            $arg["TH_SCHEST"] = "<a onclick=\"orderBy('GRADE');\"".$gradeOrder.">学籍／職員番号▼</a>";
        }else{
            $arg["TH_GRADE"] = "<a onclick=\"orderBy('GRADE');\"".$gradeOrder.">年組番▼</a>";
            $arg["TH_SCHEST"] = "学籍／職員番号";
        }
        
        if($model->field["KUBUN"] != ""){
            
            //会員数とリンク済人数取得
            $query = knjh880Query::getCnt("PV_CBT_USER_DAT", $model->field["KUBUN"]);
            $allCnt = $db->getOne($query);
            //リンク済人数
            if($model->field["KUBUN"] == "1"){
                $table = "PV_STAFF_MST";
            }else{
                $table = "PV_SCHREG_MST";
            }
            $query = knjh880Query::getCnt($table);
            $linkCnt = $db->getOne($query);
            //リンクしてない人数
            $leave = $allCnt - $linkCnt;
            
            $arg["MEMBERCNT"] = $allCnt;
            $arg["LINKCNT"] = $linkCnt;
            $arg["NOTLINKCNT"] = $leave;
            
            //データカウント
            $query = knjh880Query::getData($model->field,"", "1");
            $cnt = $db->getOne($query);
            
            if($cnt > 0){
                $flg = 1;
            }else{
                $flg = 0;
                $model->setMessage("対象のデータがありません。");
            }
            
        }

        //データ取得
        if($flg != 0){
            $arg["HYOUZI"] = 1;
            
            $query = knjh880Query::getData($model->field, $model->order);
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
                if($row["USEFLG"] == "T"){
                    $row["USEFLG"] = "有効";
                }else{
                    $row["USEFLG"] = "無効";
                }
                
                $arg["data"][] = $row;
            }
        }
        
        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjh880Form1.html", $arg);
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
    $objForm->ae(createHiddenAe("ORDER", $model->order));
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
