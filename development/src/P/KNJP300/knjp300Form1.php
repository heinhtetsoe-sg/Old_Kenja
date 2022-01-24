<?php

require_once('for_php7.php');

/********************************************************************/
/* 変更履歴 校納金振込み用紙印刷                    山城 2005/05/31 */
/*                                                                  */
/* ･NO001：                                         name yyyy/mm/dd */
/********************************************************************/

class knjp300Form1
{
    function main(&$model){

    //オブジェクト作成
    $objForm = new form;

    //フォーム作成
    $arg["start"]   = $objForm->get_start("knjp300Form1", "POST", "knjp300index.php", "", "knjp300Form1");

    /********************/
    /* 年度コンボの設定 */
    /********************/
    $opt_year = array();
    $db = Query::dbCheckOut();
    $query = knjp300Query::getyear();
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_year[] = array('label' => $row["YEAR"],
                            'value' => $row["YEAR"]);
    }
    $result->free();
    Query::dbCheckIn($db);

    if ($model->field["YEAR"] == "") $model->field["YEAR"] = $opt_year[0]["value"];

    $objForm->ae( array("type"       => "select",
                        "name"       => "YEAR",
                        "size"       => "1",
                        "value"      => $model->field["YEAR"],
                        "extrahtml"	 => "onchange=\"return btn_submit('knjp300'),AllClearList();\"",
                        "options"    => $opt_year ) );

    $arg["data"]["YEAR"] = $objForm->ge("YEAR");
    /********************/
    /* 費目コンボの設定 */
    /********************/
    $opt_appli = array();
    $db = Query::dbCheckOut();
    $query = knjp300Query::getApplication($model->field["YEAR"]);
    $result = $db->query($query);
    while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt_appli[] = array('label' => $row["APPLICATIONNAME"],
                             'value' => $row["APPLICATIONCD"]);
    }
    $result->free();
    Query::dbCheckIn($db);

    if ($model->field["APPLICATION"] == "") $model->field["APPLICATION"] = $opt_appli[0]["value"];

    $objForm->ae( array("type"       => "select",
                        "name"       => "APPLICATION",
                        "size"       => "1",
                        "value"      => $model->field["APPLICATION"],
                        "extrahtml"	 => " onchange=\" return btn_submit('himoku');\"",
                        "options"    => $opt_appli));

    $arg["data"]["APPLICATION"] = $objForm->ge("APPLICATION");

    /********************/
    /* 納入期限日の設定 */
    /********************/
    $arg["data"]["DATE"] = View::popUpCalendar($objForm	,"DATE"	,$model->field["DATE"]);

    /******************************/
    /* 用紙選択ラジオボタンの設定 */
    /******************************/
    $opt_yousi[0]=1;
    $opt_yousi[1]=2;
    $opt_yousi[2]=3;

    if (!$model->field["OUTPUT2"]) $model->field["OUTPUT2"] = 1;

    $objForm->ae( array("type"       => "radio",
                        "name"       => "OUTPUT2",
                        "value"      => isset($model->field["OUTPUT2"])?$model->field["OUTPUT2"]:"1",
                        "extrahtml"	 => "",
                        "multiple"   => $opt_yousi));

    $arg["data"]["OUTPUT3"] = $objForm->ge("OUTPUT2",1);
    $arg["data"]["OUTPUT4"] = $objForm->ge("OUTPUT2",2);
    $arg["data"]["OUTPUT5"] = $objForm->ge("OUTPUT2",3);

    /****************************/
    /* 対象者ラジオボタンの設定 */
    /****************************/
    $opt_taisyo[0]=1;
    $opt_taisyo[1]=2;

    if (!$model->field["OUTPUT2"]) $model->field["OUTPUT2"] = 1;

    $objForm->ae( array("type"       => "radio",
                        "name"       => "TAISYOSYA",
                        "value"      => isset($model->field["TAISYOSYA"])?$model->field["TAISYOSYA"]:"1",
                        "extrahtml"	 => " onclick =\" return btn_submit('clickchengTaisyoSya');\"",
                        "multiple"   => $opt_taisyo));

    $arg["data"]["TAISYOSYA1"] = $objForm->ge("TAISYOSYA",1);
    $arg["data"]["TAISYOSYA2"] = $objForm->ge("TAISYOSYA",2);

    /****************************/
    /* 出力順ラジオボタンの設定 */
    /****************************/
    $opt[0]=1;
    $opt[1]=2;
    $disable = 0;
    if (!$model->field["OUTPUT"]) $model->field["OUTPUT"] = 1;
    if ($model->field["OUTPUT"] == 1) $disable = 1;
    $objForm->ae( array("type"       => "radio",
                        "name"       => "OUTPUT",
                        "value"      => isset($model->field["OUTPUT"])?$model->field["OUTPUT"]:"1",
                        "extrahtml"	 => " onclick =\" return btn_submit('clickcheng'),AllClearList();\"",
                        "multiple"   => $opt));

    $arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
    $arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);

    if ($disable == 1){
        $arg["kojin"] = 1;
    }else {
        $arg["kurasu"] = 2;
    }

    /**********************************/
    /* クラス選択コンボボックスの設定 */
    /**********************************/
    $db = Query::dbCheckOut();
    if ($disable == 1){
        $query = knjp300Query::getclass($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $row1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
        $result->free();
        Query::dbCheckIn($db);

        if(!isset($model->field["GRADE_HR_CLASS"])) {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
        }

        if($model->cmd == 'himoku' || $model->cmd == 'clickcheng') {
            $model->field["GRADE_HR_CLASS"] = $row1[0]["value"];
            $model->cmd = 'knjp300';
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "GRADE_HR_CLASS",
                            "size"       => "1",
                            "value"      => $model->field["GRADE_HR_CLASS"],
                            "extrahtml"	 => "onchange=\"return btn_submit('change_class');\"",
                            "options"    => isset($row1)?$row1:array()));

        $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");
    }

    /**********************/
    /* 対象者リストの設定 */
    /**********************/
    $db = Query::dbCheckOut();
    $opt1 = array();
    $opt_left = array();
    if ($disable == 1){
        //生徒単位
        $selectleft = explode(",", $model->selectleft);
        $db = Query::dbCheckOut();
        $query = knjp300Query::getsch($model);

        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $model->select_opt[$row["SCHREGNO"]] = array("label" => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"], 
                                                         "value" => $row["SCHREGNO"].":".$row["GRADE"].":".$row["HR_CLASS"].":".$row["ATTENDNO"].":");

            if($model->cmd == 'change_class' ) {
                if (!in_array($row["SCHREGNO"].":".$row["GRADE"].":".$row["HR_CLASS"].":".$row["ATTENDNO"].":", $selectleft)){
                    $opt1[] = array('label' => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                    'value' => $row["SCHREGNO"].":".$row["GRADE"].":".$row["HR_CLASS"].":".$row["ATTENDNO"].":");
                }
            }else {
                $opt1[] = array('label' => $row["HR_NAME"]."　".$row["ATTENDNO"]."番　".$row["NAME_SHOW"],
                                'value' => $row["SCHREGNO"].":".$row["GRADE"].":".$row["HR_CLASS"].":".$row["ATTENDNO"].":");
            }
        }
        //左リストで選択されたものを再セット
        if($model->cmd == 'change_class' ) {
            foreach ($model->select_opt as $key => $val){
                if (in_array($key, $selectleft)) {
                    $opt_left[] = $val;
                }
            }
        }
    }else {
        //クラス単位
        $query = knjp300Query::getclass($model);
        $result = $db->query($query);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
            $opt1[]= array('label' => $row["LABEL"],
                           'value' => $row["VALUE"]);
        }
    }
    $result->free();
    Query::dbCheckIn($db);

    $objForm->ae( array("type"       => "select",
                        "name"       => "category_name",
                        "extrahtml"  => "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('left',$disable)\"",
                        "size"       => "20",
                        "options"    => isset($opt1)?$opt1:array()));

    $arg["data"]["CATEGORY_NAME"] = $objForm->ge("category_name");

    /************************/
    /* 生徒一覧リストの設定 */
    /************************/
    $objForm->ae( array("type"       => "select",
                        "name"       => "category_selected",
                        "extrahtml"  => "multiple style=\"width:230px\" width:\"230px\" ondblclick=\"move1('right',$disable)\"",
                        "size"       => "20",
                        "options"    => $opt_left));

    $arg["data"]["CATEGORY_SELECTED"] = $objForm->ge("category_selected");

    /************************/
    /* 対象選択ボタンの設定 */
    /************************/
    $objForm->ae( array("type" => "button",
                        "name"        => "btn_rights",
                        "value"       => ">>",
                        "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('right',$disable);\"" ) );

    $arg["button"]["btn_rights"] = $objForm->ge("btn_rights");

    /************************/
    /* 対象取消ボタンの設定 */
    /************************/
    $objForm->ae( array("type" => "button",
                        "name"        => "btn_lefts",
                        "value"       => "<<",
                        "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"moves('left',$disable);\"" ) );

    $arg["button"]["btn_lefts"] = $objForm->ge("btn_lefts");

    /************************/
    /* 対象選択ボタンの設定 */
    /************************/
    $objForm->ae( array("type" => "button",
                        "name"        => "btn_right1",
                        "value"       => "＞",
                        "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('right',$disable);\"" ) );

    $arg["button"]["btn_right1"] = $objForm->ge("btn_right1");

    /************************/
    /* 対象取消ボタンの設定 */
    /************************/
    $objForm->ae( array("type" => "button",
                        "name"        => "btn_left1",
                        "value"       => "＜",
                        "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"move1('left',$disable);\"" ) );

    $arg["button"]["btn_left1"] = $objForm->ge("btn_left1");

    /********************/
    /* 印刷ボタンの設定 */
    /********************/
    $objForm->ae( array("type"		   => "button",
                        "name"        => "btn_print",
                        "value"       => "プレビュー／印刷",
                        "extrahtml"   => "onclick=\"return newwin('" . SERVLET_URL . "');\"" ) );

    $arg["button"]["btn_print"] = $objForm->ge("btn_print");

    /*******************/
    /* CSVボタンの設定 */
    /*******************/
    $objForm->ae( array("type" 		  => "button",
                        "name"        => "btn_csv",
                        "value"       => "ＣＳＶ出力",
                        "extrahtml"   => "onclick=\"return btn_submit('csv');\"" ) );

    $arg["button"]["btn_csv"] = $objForm->ge("btn_csv");

    /********************/
    /* 終了ボタンの設定 */
    /********************/
    $objForm->ae( array("type" 		  => "button",
                        "name"        => "btn_end",
                        "value"       => "終 了",
                        "extrahtml"   => "onclick=\"closeWin();\"" ) );

    $arg["button"]["btn_end"] = $objForm->ge("btn_end");

    /****************/
    /* hiddenを作成 */
    /****************/
    $objForm->ae( array("type"      => "hidden",
                        "name"      => "DBNAME",
                        "value"      => DB_DATABASE
                        ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "PRGID",
                        "value"     => "KNJP300"
                        ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "SEMESTER",
                        "value"     => CTRL_SEMESTER
                        ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "cmd"
                        ) );

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "selectdata") );  

    $objForm->ae( array("type"      => "hidden",
                        "name"      => "selectleft") );  

    //フォーム終わり
    $arg["finish"]  = $objForm->get_finish();

    //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
    View::toHTML($model, "knjp300Form1.html", $arg); 
    }
}
?>
