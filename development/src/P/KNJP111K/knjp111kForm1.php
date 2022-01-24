<?php

require_once('for_php7.php');

//ファイルアップロードオブジェクト
require_once("csvfile.php");
class knjp111kForm1
{
    function main($model)
    {
        $objForm = new form;

        $db  = Query::dbCheckOut();

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

		//中高判別フラグを作成する NO001
		$jhflg = 0;
		$row = $db->getOne(knjp111kQuery::GetJorH());
		if ($row == 1){
			$jhflg = 1;
			$arg["jflg"] = $jhflg;
		}else {
			$jhflg = 2;
			$arg["hflg"] = $jhflg;
		}
		$objForm->ae( array("type" => "hidden",
							"name" => "JHFLG",
							"value"=> $jhflg ) );

		//中高カウント NO001
		$arg["data"]["NOCNT1"] = $model->nocnt;

        $arg["data"]["YEAR"] = CTRL_YEAR;

        //銀行入金データ取込処理
        $arg["data"]["PROCESS1_UPDATED"] = knjp111kQuery::GetMaxUpdate($db, "PROCESS1_UPDATED");                                //前回実行日付
        $arg["data"]["PROCESS1_STS"]     = number_format($db->getOne(knjp111kQuery::GetProcessCount("PROCESS1_UPDATED")));      //処理件数
        $arg["data"]["ERRCNT1"]          = number_format($db->getOne(knjp111kQuery::GetErrCount("1")));                         //エラー件数
        //銀行入金データ消込更新処理
        $arg["data"]["PROCESS2_UPDATED"] = knjp111kQuery::GetMaxUpdate($db, "PROCESS2_UPDATED");                                //前回実行日付
        $arg["data"]["PROCESS2_STS"]     = number_format($db->getOne(knjp111kQuery::GetProcessCount("PROCESS2_UPDATED")));      //処理件数
        $arg["data"]["ERRCNT2"]          = number_format($db->getOne(knjp111kQuery::GetErrCount("2")));                         //エラー件数

        //申込コード
        $result = $db->query(knjp111kQuery::selectQueryAppli($model));
        $opt = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label"  => $row["APPLICATIONCD"] ."　" .htmlspecialchars($row["APPLICATIONNAME"]),
                           "value"  => $row["APPLICATIONCD"]
                           );
        }

        $objForm->ae( array("type"       => "select",
                            "name"       => "APPLICATIONCD",
                            "size"       => "1",
                            "extrahtml"  => "",
                            "value"      => $model->applicationcd,
                            "options"    => $opt));

        $arg["data"]["APPLICATIONCD"]  = $objForm->ge("APPLICATIONCD");

        Query::dbCheckIn($db);


        //ファイルからの取り込み
        $objForm->add_element(array("type"      => "file",
                                    "name"      => "FILE",
#                                    "size"      => 2048000,
                                    "size"      => 4096000,
                                    "extrahtml" => "" ));
        $arg["data"]["FILE"] = $objForm->ge("FILE");



        //実行ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_ok1",
                            "value"       => "更　新",
                            "extrahtml"   => "onclick=\"return btn_submit('execute1');\"" ));

        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_ok2",
                            "value"       => "更　新",
                            "extrahtml"   => "onclick=\"return btn_submit('execute2');\"" ));

        //終了ボタン
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_cancel",
                            "value"       => "終 了",
                            "extrahtml"   => "onclick=\"closeWin();\"" ));

        $arg["button"] = array("BTN_OK1"     => $objForm->ge("btn_ok1"),
                               "BTN_OK2"     => $objForm->ge("btn_ok2"),
                               "BTN_CLEAR"  => $objForm->ge("btn_cancel"));  
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );
       
        $arg["start"]   = $objForm->get_start("main", "POST", "knjp111kindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjp111kForm1.html", $arg); 
    }
}
?>
