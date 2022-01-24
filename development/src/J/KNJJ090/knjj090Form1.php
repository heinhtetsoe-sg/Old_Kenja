<?php

require_once('for_php7.php');


class knjj090Form1
{
    function main(&$model){

	$objForm = new form;
	//フォーム作成
	$arg["start"]   = $objForm->get_start("list", "POST", "knjj090index.php", "", "edit");

//echo "rightlist=".$model->grade;

	$db = Query::dbCheckOut();
	
	//学籍基礎マスタより学籍番号と名前を取得
	$query = knjj090Query::getSchregno_name($model->schregno);
	$Row 		 = $db->getRow($query,DB_FETCHMODE_ASSOC);
	$arg["SCHREGNO"] = $Row["SCHREGNO"];
	$arg["NAME"] = $Row["NAME"];

	//HR学籍委員会履歴データよりデータを取得
	if($model->schregno)
	{	
		$result = $db->query(knjj090Query::getCouseling($model, $model->control_data["年度"], $model->schregno));
		while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
		{
            $sep = ($row["COMMITTEENAME"] && $row["CHARGENAME"]) ? "／" : "";
			if($row["YEAR"]==$model->control_data["年度"]){
				$row["COMMI_CHARGE"] = "<a href=\"knjj090index.php?cmd=edit&SCHREGNO=".$row["SCHREGNO"]."&SEQ=".$row["SEQ"]."\" target=\"edit_frame\">".$row["COMMITTEENAME"].$sep.$row["CHARGENAME"]."</a>";
			} else {
				$row["COMMI_CHARGE"] = $row["COMMITTEENAME"].$sep.$row["CHARGENAME"];
			}

			$arg["data"][] = $row;
		}
	}
	Query::dbCheckIn($db);


	
	//hiddenを作成する
	$objForm->ae( array("type"      => "hidden",
	                    "name"      => "cmd"
	                    ) );
	//hiddenを作成する
	$objForm->ae( array("type"      => "hidden",
	                    "name"      => "clear",
						"value"     => "0"
	                    ) );

	$arg["finish"]  = $objForm->get_finish();
	
//	if (VARS::get("cmd") == "from_list"){ 
	if (VARS::get("cmd") == "right_list"){ 
		$arg["reload"]  = "window.open('knjj090index.php?cmd=edit&SCHREGNO=$model->schregno','edit_frame');";
	}
	//テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
	View::toHTML($model, "knjj090Form1.html", $arg); 
}
}
?>
