<?php

require_once('for_php7.php');

class knjl031hForm1
{
    function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjl031hForm1", "POST", "knjl031hindex.php", "", "knjl031hForm1");

	    //年度
	    $arg["TOP"]["YEAR"] = $model->ObjYear;

	    //入試制度コンボの設定
	    $db = Query::dbCheckOut();
	    $opt_apdv_typ = array();
	    $result = $db->query(knjl031hQuery::get_apct_div("L003",$model->ObjYear));

	    while($Rowtyp = $result->fetchRow(DB_FETCHMODE_ASSOC)){
		    $opt_apdv_typ[]= array("label" => $Rowtyp["NAME1"], 
			                       "value" => $Rowtyp["NAMECD2"]);
	    }

	    $result->free();
	    Query::dbCheckIn($db);

	    if (!isset($model->field["APDIV"])) {
		    $model->field["APDIV"] = $opt_apdv_typ[0]["value"];
	    }

	    $objForm->ae( array("type"       => "select",
    	                    "name"       => "APDIV",
        	                "size"       => "1",
            	            "value"      => $model->field["APDIV"],
						    "extrahtml"  => " onChange=\"return btn_submit('knjl031hForm1'),AllClearList();\"",
                    	    "options"    => $opt_apdv_typ));

	    $arg["data"]["APDIV"] = $objForm->ge("APDIV");

	    //試験科目選択コンボボックスを作成する
        $db = Query::dbCheckOut();
	    $result = $db->query(knjl031hQuery::select_subclass_div("L009",$model));
        $opt_left_id = $opt_left = array();
	    while($Rowlist = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    	    $opt_left[]= array('label' => $Rowlist["TESTSUBCLASSCD"]." ".$Rowlist["NAME1"],
    		                   'value' => $Rowlist["TESTSUBCLASSCD"]);
            $opt_left_id[] = $Rowlist["NAMECD2"];
        }

	    //試験科目一覧コンボボックスを作成する
        $opt_right = array();
        if (is_array($opt_left_id)){
	        $result = $db->query(knjl031hQuery::get_subclasslist_div("L009",$model));
	        while($Rowlist = $result->fetchRow(DB_FETCHMODE_ASSOC)){
    	        $opt_right[]= array('label' => $Rowlist["NAMECD2"]." ".$Rowlist["NAME1"],
    					            'value' => $Rowlist["NAMECD2"]);
            }
        }

        $result->free();
        Query::dbCheckIn($db);

        //試験科目選択
        $objForm->ae( array("type"        => "select",
                            "name"        => "category_name",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('right','category_name','category_selected',1)\"",
                            "options"     => $opt_left)); 

        //試験科目一覧
        $objForm->ae( array("type"        => "select",
                            "name"        => "category_selected",
                            "size"        => "20",
                            "value"       => "left",
                            "extrahtml"   => "multiple STYLE=\"WIDTH:100%\" WIDTH=\"100%\" ondblclick=\"move('left','category_name','category_selected','')\"",
                            "options"     => $opt_right));  

        //追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add_all",
                            "value"       => "≪",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"return move('sel_add_all','category_name','category_selected','');\"" ) );

        //追加ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_add",
                            "value"       => "＜",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"return move('left','category_name','category_selected','');\"" ) );

        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del",
                            "value"       => "＞",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"return move('right','category_name','category_selected',1);\"" ) );

        //削除ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "sel_del_all",
                            "value"       => "≫",
                            "extrahtml"   => "style=\"height:20px;width:40px\" onclick=\"return move('sel_del_all','category_name','category_selected',1);\"" ) ); 

        $arg["main_part"] = array( "LEFT_PART"   => $objForm->ge("category_name"),
                                   "RIGHT_PART"  => $objForm->ge("category_selected"),
                                   "SEL_ADD_ALL" => $objForm->ge("sel_add_all"),
                                   "SEL_ADD"     => $objForm->ge("sel_add"),
                                   "SEL_DEL"     => $objForm->ge("sel_del"),
                                   "SEL_DEL_ALL" => $objForm->ge("sel_del_all"));                    

        //保存ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_keep",
                            "value"       => "更 新",
                            "extrahtml"   => "onclick=\"return doSubmit();\"" ) );

        //取消ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_clear",
                            "value"       => "取 消",
                            "extrahtml"   => "onclick=\"return btn_submit('clear');\"" ) );

    	//終了ボタンを作成する
    	$objForm->ae( array("type"        => "button",
                        	"name"        => "btn_end",
                        	"value"       => "終 了",
                        	"extrahtml"   => "onclick=\"closeWin();\"" ) );


        $arg["button"] = array("BTN_OK"     =>$objForm->ge("btn_keep"),
                               "BTN_CLEAR"  =>$objForm->ge("btn_clear"),
                               "BTN_END"    =>$objForm->ge("btn_end"));  

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"
                            ) );  

        $objForm->ae( array("type"      => "hidden",
                            "name"      => "selectdata"
                            ) );  

	    $objForm->ae( array("type"      => "hidden",
    	                    "name"      => "DBNAME",
        	                "value"     => DB_DATABASE
             		        ) );

		//年度
		$objForm->ae( array("type"      => "hidden",
                    		"name"      => "YEAR",
        	                "value"     => $model->ObjYear
    						) );

        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
		View::toHTML($model, "knjl031hForm1.html", $arg); 
    }
}
?>
