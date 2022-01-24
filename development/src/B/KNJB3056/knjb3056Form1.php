<?php

require_once('for_php7.php');

class knjb3056Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjb3056Form1", "POST", "knjb3056index.php", "", "knjb3056Form1");

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        // $arg["data"]["YEAR"] = $model->ObjYear;

        //上行作成
        $query = knjb3056Query::getMainDataHead($model);
        $extra = '';
        $value=null;
        $arg["head"]["chairCd1"] = makeCmb($objForm, $arg, $db, $query, "CHAIRCD1", $value, $extra, 1);
        $arg["head"]["chairCd2"] = makeCmb($objForm, $arg, $db, $query, "CHAIRCD2", $value, $extra, 1);
        $result = $db->query($query);
        $cnt = 0;
        $staffMaxCount = 1;
        $model->chairJoudan = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->chairJoudan[] = $row['VALUE'];
            $data=array();
            $data['CHAIRCD'] = $row['VALUE'];
            $data['CHAIRNAME'] = $row['LABEL'];
            $data['SUBCLASSCD'] = $row['SUBCLASSCD'];
            $data['SUBCLASSNAME'] = $row['SUBCLASSNAME'];
            
            $staff = array();
            $result2 = $db->query(knjb3056Query::getMainDataHeadSTAFF($model, $data['CHAIRCD']));
            while ($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
                $staff[] = $row2['STAFFCD'].' '.$row2['STAFFNAME'];
            }
            $staffMaxCount = $staffMaxCount > get_count($staff) ? $staffMaxCount : get_count($staff);
            $data['STAFF_LIST'] = $staff;
            
            $query = knjb3056Query::getMainData($model,$row['VALUE']);
            $extra = ' style="width:100%;height:200px" multiple="multiple" data-chairCd="'.$row['VALUE'].'"';
            $value = null;
            $data['CHAIRLIST'] =  makeCmb2($objForm, $arg, $db, $query, "CHAIRLIST_".$cnt, $value, $extra, 15);
            knjCreateHidden($objForm, "CHAIRLIST_VALUE_".$cnt);
            $arg['data'][] = $data;
            $cnt++;
        }
        for ($j = 0; $j < get_count($arg['data']); $j++) {
            if (get_count($arg['data'][$j]["STAFF_LIST"]) == 0) {
                $arg['data'][$j]["STAFF"] = str_repeat('<br>　', ($staffMaxCount - 1) - get_count($arg['data'][$j]["STAFF_LIST"]));
            } else {
                $arg['data'][$j]["STAFF"] = implode('<br>', $arg['data'][$j]["STAFF_LIST"]).str_repeat('<br>　', $staffMaxCount - get_count($arg['data'][$j]["STAFF_LIST"]));
            }
        }
        $result->free();
        knjCreateHidden($objForm, "CHAIRLIST_MAX_CNT",$cnt);

        //下行作成
        $cnt = 0;
        $result = $db->query(knjb3056Query::getMainData2Head($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $data=array();
            $data['HR_NAME'] = $row['HR_NAME'];
            
            $query = knjb3056Query::getMainData2($model,$row['GRADE'],$row['HR_CLASS']);
            $extra = ' style="width:100%;height:200px" multiple="multiple" data-prop="'.$row['PROP'].'"';
            $value = null;
            $data['CHAIRLIST2'] =  makeCmb2($objForm, $arg, $db, $query, "CHAIRLIST2_".$cnt, $value, $extra, 15);
            knjCreateHidden($objForm, "CHAIRLIST2_VALUE_".$cnt);
            $arg['data2'][] = $data;
            $cnt++;
        }
        $result->free();
        knjCreateHidden($objForm, "CHAIRLIST2_MAX_CNT",$cnt);
        
        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjb3056Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg){
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }

    $result->free();
    //$value = ($value && $value_flg) ? $value : $opt[$default]["value"];

//    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//コンボ作成2
function makeCmb2(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    if ($blank) $opt[] = array("label" => "", "value" => "");
    $value_flg = false;
    $i = $default = 0;
    $default_flg = true;

    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"],
                       'prop' => $row["PROP"],
                       'prop2' => $row["PROP2"]);

        if ($value == $row["VALUE"]) $value_flg = true;

        if ($row["NAMESPARE2"] && $default_flg){
            $default = $i;
            $default_flg = false;
        } else {
            $i++;
        }
    }

    $result->free();
    $ret='<select name='.$name.' '.$extra.' size="'.$size.'">'."\n";
    for($i=0;$i<get_count($opt);$i++){
        $ret.='<option value="'.$opt[$i]['value'].'" data-prop="'.$opt[$i]['prop'].'" data-prop2="'.$opt[$i]['prop2'].'">'.$opt[$i]['label'].'</option>'."\n";
    }
    $ret.='</select>'."\n";
    return $ret;
}


//ボタン作成
function makeBtn(&$objForm, &$arg) {
    //HRに戻す
    $extra = " onclick=\"return retrunHr();\"";
    $arg["button"]["btn_backHr"] = knjCreateBtn($objForm, "btn_backHr", "ホームルームに戻す", $extra);
    //上段へ移動
    $extra = " onclick=\"return chairToChair1();\"";
    $arg["button"]["btn_ChairToChair1"] = knjCreateBtn($objForm, "btn_ChairToChair", "へ移動", $extra);
    //上段へ登録
    $extra = " onclick=\"return chairToChair2();\"";
    $arg["button"]["btn_ChairToChair2"] = knjCreateBtn($objForm, "btn_ChairToChair", "へ登録", $extra);

    //更新ボタン
    $extra = "onclick=\"return doPopup('".REQUESTROOT."');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('clear');\"";
    $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm, $model) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SELECT_DATE");
}
?>
