<?php

require_once('for_php7.php');

//ファイルアップロードオブジェクト
require_once("csvfile.php");
class knjp090kForm1
{
    function main($model)
    {
        $objForm = new form;

        $db  = Query::dbCheckOut();

        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE){
            $arg["jscript"] = "OnAuthError();";
        }

        $arg["data"]["YEAR"] = CTRL_YEAR;
        //銀行入金データ取込処理
        $arg["data"]["PROCESS1_UPDATED"] = knjp090kQuery::GetMaxUpdate($db, "PROCESS1_UPDATED");                                //前回実行日付
        $arg["data"]["PROCESS1_STS"]     = number_format($db->getOne(knjp090kQuery::GetProcessCount("PROCESS1_UPDATED")));      //処理件数
        $arg["data"]["ERRCNT1"]          = number_format($db->getOne(knjp090kQuery::GetErrCount("1")));                         //エラー件数
        //銀行入金データ消込更新処理
        $arg["data"]["PROCESS2_UPDATED"] = knjp090kQuery::GetMaxUpdate($db, "PROCESS2_UPDATED");                                //前回実行日付
        $arg["data"]["PROCESS2_STS"]     = number_format($db->getOne(knjp090kQuery::GetProcessCount("PROCESS2_UPDATED")));      //処理件数
        $arg["data"]["ERRCNT2"]          = number_format($db->getOne(knjp090kQuery::GetErrCount("2")));                         //エラー件数
        //登録銀行データ取込更新処理
        $arg["data"]["PROCESS3_UPDATED"] = knjp090kQuery::GetMaxUpdate($db, "PROCESS3_UPDATED");                                //前回実行日付
        $arg["data"]["PROCESS3_STS"]     = number_format($db->getOne(knjp090kQuery::GetProcessCount("PROCESS3_UPDATED")));      //処理件数
        $arg["data"]["ERRCNT3"]          = number_format($db->getOne(knjp090kQuery::GetErrCount("3")));                         //エラー件数


        //入金月コンボ
		$monthcnt = 0;		//NO002
        $opt = array();
        $result = $db->query(knjp090kQuery::getPaidMonth());
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[] = array("label" => $row["PAID_MONTH"]."月", "value" => $row["PAID_MONTH"]);
			$monthcnt++;	//NO002
        }
        //起動時に設定
		$monthcnt = ($monthcnt > 0) ? $monthcnt - 1 : 0;		//NO002
        $model->paid_month = (!$model->paid_month) ? $opt[$monthcnt]["value"] : $model->paid_month;	//NO002

        $objForm->ae( array("type"        => "select",
                            "name"        => "PAID_MONTH",
                            "size"        => 1,
                            "value"       => $model->paid_month,
                            "extrahtml"   => "onchange=\"btn_submit('main')\"",
                            "options"     => $opt ));
        $arg["data"]["PAID_MONTH"] = $objForm->ge("PAID_MONTH");

        //入金日コンボ
/* NO003
        $opt = array();
        $opt[] = array("label" => "", "value" => "");         //空リストをセット
        if (strlen($model->paid_month)) {
            $result = $db->query(knjp090kQuery::getPaidDay($model->paid_month));
            while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
            {
                $opt[] = array("label" => $row["PAID_DAY"]."日", "value" => $row["PAID_DAY"]);
            }
        }
        $objForm->ae( array("type"        => "select",
                            "name"        => "PAID_DAY",
                            "size"        => 1,
                            "value"       => $model->paid_day,
                            "options"     => $opt ));
        $arg["data"]["PAID_DAY"] = $objForm->ge("PAID_DAY");
*/

        //銀行コードコンボ
//NO004-->
//		$opt = array();
//		$result = $db->query(knjp090kQuery::getBankcd());
		/***ADD 2005/12/02 by ameku***/
//		$opt[] = array("label" => "", "value" => "");         //空リストをセット
		/***ADD 2005/12/02 by ameku***/
//		while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
//		{
//			$opt[] = array("label" => $row["BANKCD"]."：".mb_convert_kana($row["BANKNAME_KANA"], "K"),
//							"value" => $row["BANKCD"]);
//		}
//		$objForm->ae( array("type"        => "select",
//							"name"        => "BANKCD",
//							"size"        => 1,
//							"value"       => $model->bankcd,
//							"extrahtml"   => "onchange=\"btn_submit('main')\"",
//							"options"     => $opt ));
//		$arg["data"]["BANKCD"] = $objForm->ge("BANKCD");

		//起動時に設定
//		$model->bankcd = (!strlen($model->bankcd)) ? $opt[0]["value"] : $model->bankcd;
//NO004<--

		//支店コードコンボ
//NO004-->
//		$opt = array();
//		$opt[] = array("label" => "", "value" => "");         //空リストをセット
//		$result = $db->query(knjp090kQuery::getBranchcd($model->bankcd));
//		while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
//		{
//			$opt[] = array("label" => $row["BRANCHCD"]."：".mb_convert_kana($row["BRANCHNAME_KANA"], "K"),
//							"value" => $row["BRANCHCD"]);
//		}
//		$objForm->ae( array("type"        => "select",
//							"name"        => "BRANCHCD",
//							"size"        => 1,
//							"value"       => $model->branchcd,
//							"options"     => $opt ));
//		$arg["data"]["BRANCHCD"] = $objForm->ge("BRANCHCD");
//NO004<--


        Query::dbCheckIn($db);

       //ファイルからの取り込み
        $objForm->add_element(array("type"      => "file",
                                    "name"      => "FILE",
#                                    "size"      => 2048000,
#                                    "size"      => 4096000,
                                    "size"      => 6144000,
                                    "extrahtml" => "" ));
        $arg["data"]["FILE"] = $objForm->ge("FILE");

        //振替日変更ボタン
        $link = REQUESTROOT."/P/KNJP091K/knjp091kindex.php?mode=1";
        $extra = "onclick=\"document.location.href='$link'\"";
        $arg["button"]["btn_p091k"] = knjCreateBtn($objForm, "btn_p091k", "振替日変更", $extra);

        //更新ボタン
        $extra = "onclick=\"return btn_submit('execute1');\"";
        $arg["button"]["BTN_OK1"] = knjCreateBtn($objForm, "btn_ok1", "更 新", $extra);

        $extra = "onclick=\"return btn_submit('execute2');\"";
        $arg["button"]["BTN_OK2"] = knjCreateBtn($objForm, "btn_ok2", "更 新", $extra);

        $extra = "onclick=\"return btn_submit('execute3');\"";
        $arg["button"]["BTN_OK3"] = knjCreateBtn($objForm, "btn_ok3", "更 新", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["BTN_CLEAR"] = knjCreateBtn($objForm, "btn_cancel", "終 了", $extra);

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );
       
        $arg["start"]   = $objForm->get_start("main", "POST", "knjp090kindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjp090kForm1.html", $arg); 
    }
}
?>
