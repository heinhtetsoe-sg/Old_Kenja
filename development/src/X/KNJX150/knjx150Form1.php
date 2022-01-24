<?php

require_once('for_php7.php');

class knjx150Form1
{
    function main(&$model)
    {
		//権限チェック
		if (AUTHORITY != DEF_UPDATABLE){
			$arg["jscript"] = "OnAuthError();";
		}

		$objForm = new form;

        //今年度・今学期名及びタイトルの表示
        $arg["data"]["YEAR_SEMESTER"] = CTRL_YEAR."年度　" .CTRL_SEMESTERNAME ."　ＣＳＶ出力／取込";

        //処理名コンボボックス
        $opt_shori  	= array();
        $opt_shori[]    = array("label" => "更新","value" => "1");
        $opt_shori[]    = array("label" => "削除","value" => "2");

        $objForm->ae( array("type"        => "select",
                            "name"        => "SHORI_MEI",
                            "size"        => "1",
                            "extrahtml"   => "style=\"width:60px;\"",
                            "value"       => $model->field["SHORI_MEI"],
                            "options"     => $opt_shori ));

        $arg["data"]["SHORI_MEI"] = $objForm->ge("SHORI_MEI");

        //年度一覧コンボボックス
        $db         = Query::dbCheckOut();
        $optnull    = array("label" => "(全て出力)","value" => "");   //初期値：空白項目
        $result     = $db->query(knjx150query::getSelectFieldSQL());   
        $opt_year  = array();
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_year[] = array("label" => $row["YEAR"]."年度 ".$row["SEMESTERNAME"], 
                           		 "value" => $row["YEAR"].$row["SEMESTER"]);
        }
		if($model->field["YEAR"]=="") $model->field["YEAR"] = CTRL_YEAR.CTRL_SEMESTER;

        //年組一覧コンボボックス
        $result     = $db->query(knjx150query::getSelectFieldSQL2($model));   
        $opt_gr_hr  = array();
        $opt_gr_hr[] = $optnull;
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt_gr_hr[] = array("label" => $row["HR_NAME"], 
                           		 "value" => $row["GRADE"].$row["HR_CLASS"]);
        }

        $result->free();
        Query::dbCheckIn($db);

        //年度一覧コンボボックス
        $objForm->ae( array("type"        => "select",
                            "name"        => "YEAR",
                            "size"        => "1",
                            "extrahtml"   => "onchange=\"btn_submit('');\"",
                            "value"       => $model->field["YEAR"],
                            "options"     => $opt_year ));

        $arg["data"]["YEAR"] = $objForm->ge("YEAR");

        //年組一覧コンボボックス
        $objForm->ae( array("type"        => "select",
                            "name"        => "GRADE_HR_CLASS",
                            "size"        => "1",
                            "extrahtml"   => "",
                            "value"       => $model->field["GRADE_HR_CLASS"],
                            "options"     => $opt_gr_hr ));

        $arg["data"]["GRADE_HR_CLASS"] = $objForm->ge("GRADE_HR_CLASS");

        //ヘッダ有チェックボックス
		if($model->field["HEADER"] == "on")
		{
			$check_header = "checked";
		}
		else
		{
	        if($model->cmd == "")
			{
				$check_header = "checked";
			}
			else
			{
				$check_header = "";
			}
		}

		$objForm->ae( array("type" 		=> "checkbox",
		                    "name"      => "HEADER",
							"value"		=> "on",
							"extrahtml"	=>$check_header ) );

		$arg["data"]["HEADER"] = $objForm->ge("HEADER");

		//出力取込種別ラジオボタン
		$opt_shubetsu[0]=1;		//ヘッダ出力
		$opt_shubetsu[1]=2;		//データ取込
		$opt_shubetsu[2]=3;		//エラー出力
		$opt_shubetsu[3]=4;		//データ出力

		if($model->field["OUTPUT"]=="") $model->field["OUTPUT"] = "1";

		$objForm->ae( array("type"       => "radio",
		                    "name"       => "OUTPUT",
							"value"      => $model->field["OUTPUT"],
		                    "extrahtml"   => "",
							"multiple"   => $opt_shubetsu));

		$arg["data"]["OUTPUT1"] = $objForm->ge("OUTPUT",1);
		$arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);
		$arg["data"]["OUTPUT3"] = $objForm->ge("OUTPUT",3);
		$arg["data"]["OUTPUT4"] = $objForm->ge("OUTPUT",4);

		//ファイルからの取り込み
		$objForm->add_element(array("type"      => "file",
		                            "name"      => "FILE",
		                            "size"      => 1024000,
		                            "extrahtml" => "" ));

		$arg["FILE"] = $objForm->ge("FILE");

		//実行ボタン
		$objForm->ae( array("type" 		=> "button",
		                    "name"      => "btn_exec",
		                    "value"     => "実 行",
		                    "extrahtml" => "onclick=\"return btn_submit('exec');\"" ));

		$arg["btn_exec"] = $objForm->ge("btn_exec");

		//終了ボタンを作成する
		$objForm->ae( array("type" 		=> "button",
		                    "name"      => "btn_end",
		                    "value"     => "終 了",
		                    "extrahtml" => "onclick=\"closeWin();\"" ) );

		$arg["btn_end"] = $objForm->ge("btn_end");

		//hiddenを作成する
		$objForm->ae( array("type"      => "hidden",
		                    "name"      => "cmd"
		                    ) );

		//フォーム作成
		$arg["start"]   = $objForm->get_start("main", "POST", "knjx150index.php", "", "main");
		$arg["finish"]  = $objForm->get_finish();

		//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
		View::toHTML($model, "knjx150Form1.html", $arg);
    }
}
?>
