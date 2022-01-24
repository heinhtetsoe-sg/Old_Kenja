<?php

require_once('for_php7.php');

class knjl999Form1
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
		$opt_shubetsu[1]=2;		//データ取込
		$opt_shubetsu[2]=3;		//エラー出力

		if($model->field["OUTPUT"]=="") $model->field["OUTPUT"] = "2";

		$objForm->ae( array("type"       => "radio",
		                    "name"       => "OUTPUT",
							"value"      => $model->field["OUTPUT"],
		                    "extrahtml"   => "",
							"multiple"   => $opt_shubetsu));

		$arg["data"]["OUTPUT2"] = $objForm->ge("OUTPUT",2);
		$arg["data"]["OUTPUT3"] = $objForm->ge("OUTPUT",3);

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
		$arg["start"]   = $objForm->get_start("main", "POST", "knjl999index.php", "", "main");
		$arg["finish"]  = $objForm->get_finish();

		//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
		View::toHTML($model, "knjl999Form1.html", $arg);
    }
}
?>
