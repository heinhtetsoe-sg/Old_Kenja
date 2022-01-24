<?php

require_once('for_php7.php');

class knje301Form1
{
    function main(&$model)
    {
    //権限チェック
    if (AUTHORITY != DEF_UPDATABLE){
        $arg["jscript"] = "OnAuthError();";
    }
    
    $objForm = new form;
    
    //フォーム作成
    $arg["start"]   = $objForm->get_start("sel", "POST", "knje301index.php", "", "sel");
    $db             = Query::dbCheckOut();
    $no_year        = 0;
    //年度設定
    $result    = $db->query(knje301Query::selectYearQuery());   
    $opt       = array();
    
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
    {
        $opt[] = array("label" => $row["YEAR"], 
                       "value" => $row["YEAR"]);
        if ($row["YEAR"] == $model->year)
            $no_year = 1;
    }
    if ($no_year == 0)
        $model->year = $opt[0]["value"];
        
    //年度科目一覧取得
    $opt_right = array();
    if($model->field['kubun'] == 2){
        $result      = $db->query(knje301Query::selectQueryPrischool($model));
	    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
	    {
	    	$label=$row['NAME1'];
	    	if ($row['NAME2']) {
	    		$label .= '(' . $row['NAME2'] . ')';
	    	}
	        $opt_right[]    = array("label" =>$label,
	                               "value" => $row["VALUE"]);
	    }
    } else {
	    $result      = $db->query(knje301Query::selectQuery($model));
	    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
	    {
	        $opt_right[]    = array("label" =>(($row["NAME1"] == '')?'　':substr($row["NAME1"], 0, 3))."：".$row["FINSCHOOL_NAME"],
	                               "value" => $row["FINSCHOOLCD"]);
	    }
    }
    $result->free();
    
    $opt_left = array();
    if($model->field['kubun'] == 2){
        $result      = $db->query(knje301Query::selectQueryPrischool2($model));
	    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
	    {
	    	$label=$row['NAME1'];
	    	if ($row['NAME2']) {
	    		$label .= '(' . $row['NAME2'] . ')';
	    	}
	        $opt_left[]    = array("label" =>$label,
	                               "value" => $row["VALUE"]);
	    }
    } else {
	    $result      = $db->query(knje301Query::selectQuery2($model));
	    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
	    {
	        $opt_left[]    = array("label" =>(($row["NAME1"] == '')?'　':substr($row["NAME1"], 0, 3))."：".$row["FINSCHOOL_NAME"],
	                               "value" => $row["FINSCHOOLCD"]);
	    }
    }
    $result->free();


    //帳票出力ラジオボタン 1:A 2:B 3:C
    $optRadio = array(1, 2);
    $model->field["kubun"] = ($model->field["kubun"]) ? $model->field["kubun"] : 1;
    $extra = array("id=\"kubun1\" onchange=\"return btn_submit('main')\"", "id=\"kubun2\" onchange=\"return btn_submit('main')\"");
    $radioArray = knjCreateRadio($objForm, "kubun", $model->field["kubun"], $extra, $optRadio, get_count($optRadio));
    foreach($radioArray as $key => $val) $arg[$key] = $val;
    
    $query = knje301Query::getStaffList($model);
    $extra = " onchange=\"return btn_submit('main')\"";
    $arg['STAFFCD'] = makeCmb($objForm, $arg, $db, $query, 'STAFFCD', $model->field['STAFFCD'], $extra, 1,'BLANK');
    
    if($model->field['kubun'] != 2){
        $query = knje301Query::getKousyuList($model);
        $extra = " onchange=\"return btn_submit('')\"";
        $arg['kousyu'] = makeCmb($objForm, $arg, $db, $query, 'kousyu', $model->field['kousyu'], $extra, 1, 'BLANK');
    }

    //年度コンボボックスを作成する
    $objForm->ae( array("type"        => "select",
                        "name"        => "year",
                        "size"        => "1",
                        "extrahtml"   => "onchange=\"return btn_submit('');\"",
                        "value"       => $model->year,
                        "options"     => $opt));    

    $objForm->ae( array("type"        => "text",
                        "name"        => "year_add",
                        "size"        => 5,
                        "maxlength"   => 4,
                        "extrahtml"   => "onblur=\"this.value=toInteger(this.value);\"",
                         )); 

    $objForm->ae( array("type"        => "button",
                        "name"        => "btn_year_add",
                        "value"       => "年度追加",
                        "extrahtml"   => "onclick=\"return add('');\"" ));                           
                                          
    $arg["year"] = array( "VAL"       => $objForm->ge("year")."&nbsp;&nbsp;".
                                         $objForm->ge("year_add")."&nbsp;".$objForm->ge("btn_year_add"));

    //出身学校年度
    $objForm->ae( array("type"        => "select",
                        "name"        => "finschoolyear",
                        "size"        => "20",
                        "value"       => "left",
                        "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','finschoolyear','finschoolmaster',1)\"",
                        "options"     => $opt_left)); 

    //出身学校マスタ
    $objForm->ae( array("type"        => "select",
                        "name"        => "finschoolmaster",
                        "size"        => "20",
                        "value"       => "left",
                        "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','finschoolyear','finschoolmaster',1)\"",
                        "options"     => $opt_right));  

    //追加ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "sel_add_all",
                        "value"       => "≪",
                        "extrahtml"   => "onclick=\"return move('sel_add_all','finschoolyear','finschoolmaster',1);\"" ) );

    //追加ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "sel_add",
                        "value"       => "＜",
                        "extrahtml"   => "onclick=\"return move('left','finschoolyear','finschoolmaster',1);\"" ) );

    //削除ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "sel_del",
                        "value"       => "＞",
                        "extrahtml"   => "onclick=\"set_selectdata_del(); move('right','finschoolyear','finschoolmaster',1); return false;\"" ) );

    //削除ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "sel_del_all",
                        "value"       => "≫",
                        "extrahtml"   => "onclick=\"return move('sel_del_all','finschoolyear','finschoolmaster',1);\"" ) ); 
                                        
    $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("finschoolyear"),
                               "RIGHT_PART"  => $objForm->ge("finschoolmaster"),
                               "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                               "SEL_ADD"     => $objForm->ge("sel_add"),
                               "SEL_DEL"     => $objForm->ge("sel_del"),
                               "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));
                          
    //保存ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "btn_keep",
                        "value"       => "更新",
                        "extrahtml"   => "onclick=\"return doSubmit();\"" ) );

    //取消ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "btn_clear",
                        "value"       => "取消",
                        "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ) );

    //終了ボタンを作成する
    $objForm->ae( array("type"        => "button",
                        "name"        => "btn_end",
                        "value"       => "終了",
                        "extrahtml"   => "onclick=\"closeWin();\"" ) );

    $arg["button"] = array("BTN_OK"     =>$objForm->ge("btn_keep"),
                           "BTN_CLEAR"  =>$objForm->ge("btn_clear"),
                           "BTN_END"    =>$objForm->ge("btn_end"));  

    //hiddenを作成する
    $objForm->ae( array("type"      => "hidden",
                        "name"      => "cmd"
                        ) );  

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "selectdata",
                        "value"       => $model->selectdata,
                        ) );  
                        
    $objForm->ae( array("type"      => "hidden",
                        "name"      => "selectdata_del",
                        "value"       => $model->selectdata_del,
                        ) );  

    $arg["info"]    = array("TOP"        => "対象年度",
                            "LEFT_LIST"  => "出身学校年度一覧",
                            "RIGHT_LIST" => "出身学校一覧");

    $arg["TITLE"]   = "マスタメンテナンス - 出身学校マスタ";
    $arg["finish"]  = $objForm->get_finish();

    //前年度からコピーボタン
    $extra = "onclick=\"return btn_submit('copy');\"";
    $arg["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度からコピー", $extra);
    
    $arg['year'] = $model->year;
    
    Query::dbCheckIn($db);
    
    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knje301Form1.html", $arg); 
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $result->free();
    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

}
?>
