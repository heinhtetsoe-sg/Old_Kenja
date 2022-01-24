<?php

require_once('for_php7.php');

class knjb3060Form1
{
    function main(&$model)
    {
        //フォーム作成
        $objForm = new form;

        $arg["start"]   = $objForm->get_start("main", "POST", "knjb3060index.php", "", "main");

        //データベース接続
        $db = Query::dbCheckOut();

		$week = array('日','月','火','水','木','金','土');
		$semeRow = $db->getRow(knjb3060Query::getSemester($model),DB_FETCHMODE_ASSOC);
		
		//年度、学期
		$arg['year'] = $model->year;
		$arg['semester'] = $semeRow['SEMESTERNAME'];
		
		//適用可能期間
	    $rengeStart = $db->getOne(knjb3060Query::getRengeStert($model, $semeRow['SDATE'], $semeRow['EDATE']));
	    $rengeStart =date('Y-m-d' ,strtotime($rengeStart)+24*60*60);
	    if(strtotime($semeRow['SDATE'])>strtotime($rengeStart)){
	    	$rengeStart = $semeRow['SDATE'];
	    }

    	$semeSData = $rengeStart.'('.$week[date('w', strtotime($rengeStart))].')';
    	$semeEData = $semeRow['EDATE'].'('.$week[date('w', strtotime($semeRow['EDATE']))].')';

		$arg['renge'] = $semeSData.'～'.$semeEData;
		
		$model->rengeStart=str_replace('-','/',$rengeStart);
		$model->rengeEnd=str_replace('-','/',$semeRow['EDATE']);
		$model->rengeSData=$semeSData;
		$model->rengeEData=$semeEData;

    	knjCreateHidden($objForm, "RENGE_START", $model->rengeStart);
    	knjCreateHidden($objForm, "RENGE_END", $model->rengeEnd);
    	
    	//基本時間割
        $opt = array();
	    $query = knjb3060Query::getTemplateData($model);
	    $result = $db->query($query);
	    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
	    	
	    	list($day, $time) = explode(' ',$row['UPDATED']);
	    	list($time) = explode('.',$time);
	    	$updated = $day.'('.$week[date('w', strtotime($day))].')'.$time;
	        $opt[] = array("label" => $row["BSCSEQ"].','.$updated.','.$row["TITLE"],
	                       "value" => $row["BSCSEQ"]);
	    }
	    $result->free();

	    $extra = "";
	    $arg['BSCSEQ'] = knjCreateCombo($objForm, 'BSCSEQ', $model->bscSeq, $opt, $extra, 1);
        
        //開始日時
        $arg["START_DATE"] = View::popUpCalendar($objForm, "START_DATE", str_replace("-", "/", ($model->startDate)?$model->startDate:$rengeStart));
        
        //終了日時
        $arg["END_DATE"] = View::popUpCalendar($objForm, "END_DATE", str_replace("-", "/", ($model->endDate)?$model->endDate:$rengeStart));
        
        //ダイアログ内選択用日時
        $arg["SELECT_DATE"] = View::popUpCalendar($objForm, "SELECT_DATE", str_replace("-", "/", $rengeStart));
        
        //更新種別
    	$opt = array(array('label'=>'指定期間全て','value'=>0),array('label'=>'1週間おき','value'=>1),array('label'=>'2週間おき','value'=>2),array('label'=>'曜日指定','value'=>3));

    	$value = ($model->reflectDiv) ? $model->reflectDiv : $opt[0]["value"];
    	$arg['REFLECTDIV'] = knjCreateCombo($objForm, 'REFLECTDIV', $value, $opt, ' onchange="refrectDivChange()"', 1);
	

		//適用履歴
		$reflectDiv = array('指定期間全て','１週間おき　','２週間おき　','曜日指定');
		$dayWeek = array(1=>'月',2=>'火',3=>'水',4=>'木',5=>'金',6=>'土',7=>'日');
		$opt = array();
	    $query = knjb3060Query::getRirekiData($model);
	    $result = $db->query($query);
	    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
	    	
	    	list($day, $time) = explode(' ',$row['REFLECTDATE']);
	    	list($time) = explode('.',$time);
	    	$reflectDate = $day.'('.$week[date('w', strtotime($day))].')'.$time;
	    	$sDate = $row["SDATE"].'('.$week[date('w', strtotime($row["SDATE"]))].')';
	    	$eDate = $row["EDATE"].'('.$week[date('w', strtotime($row["EDATE"]))].')';
	    	if($row["REFLECTDIV"] == 3){
	    		$seData = $row['DAYS'];
	    	} else {
	    		$seData = $sDate.'～'.$eDate;
	    	}
	    	list($day, $time) = explode(' ',$row['UPDATED']);
	    	list($time) = explode('.',$time);
	    	$updated = $day.'('.$week[date('w', strtotime($day))].')'.$time;
	    	$daycd=($row["REFLECTDIV"] == 3)?('('.$dayWeek[$row['DAYCD']].')'):'';
	    	$dataList = array();
	    	$dataList['REFLECTDATE'] = $reflectDate;
	    	$dataList['REFLECTDIV'] = $reflectDiv[$row["REFLECTDIV"]].$daycd;
	    	$dataList['RENGE'] = $seData;
	    	$dataList['TITLE'] = $row["BSCSEQ"].','.$updated.','.$row["TITLE"];
	    	$arg['data'][] = $dataList;
	    }
	    $result->free();

        //曜日ラジオ
        $opt = array(1, 2, 3, 4, 5, 6, 7);
        $extra = array();
        foreach ($opt as $key => $val) {
            array_push($extra, " id=\"WEEK_RADIO{$val}\" ");
        }
        $radioArray = knjCreateRadio($objForm, "WEEK_RADIO", $model->weekRadio, $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg[$key] = $val;
        
        if(!empty($model->weekTableDays)){
        	$weekDays = explode(',', $model->weekTableDays);
        	for($i=0;$i<12;$i++){
        		$arg['week']['WEEKTABLE'.($i+1)] = isset($weekDays[$i])?$weekDays[$i]:''; 
        	}
		}
	    //更新ボタン
	    $extra = "onclick=\"return btn_submit('update');\"";
	    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "反 映", $extra);
	    //終了
	    $extra = "onclick=\"closeWin();\"";
	    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
    	knjCreateHidden($objForm, "cmd");
    	knjCreateHidden($objForm, "WEEK_TABLE_DAYS");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML5($model, "knjb3060Form1.html", $arg); 
    }
}
?>
