<?php

require_once('for_php7.php');

class knjxSearch5
{
    function main(&$model){
        $objForm = new form;
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("search", "POST", "index.php", "right_frame");

        //年度と学期
        $arg["CTRL_YEAR"] = CTRL_YEAR;
        $arg["CTRL_SEMESTER"] = CTRL_SEMESTERNAME;

        $db = Query::dbCheckOut();
        $opt = array();
        $opt2 = array();
                
        $result = $db->query(knjxexpkQuery::selectExpense_L_Cd($model));
#        var_dump($model->Expense["M"]);
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $opt[$row["EXPENSE_L_CD"]] = array("label"     => $row["EXPENSE_L_CD"] ."：" .htmlspecialchars($row["EXPENSE_L_CD2"]),
                           "value"     => $row["EXPENSE_L_CD"]);

            if (!$model->Expense["L"]) $model->Expense["L"] =  $row["EXPENSE_L_CD"];
            if ($row["EXPENSE_L_CD"] == $model->Expense["L"]){
                $cd = $row["EXPENSE_L_CD"] .$row["EXPENSE_M_CD"];
                $opt2[] = array("label"     => $cd ."：" .htmlspecialchars($row["EXPENSE_M_NAME"]),
                            "value"     => $cd);
                if (!$model->Expense["M"]) $model->Expense["M"] =  $cd;
            }
        }
        //費目大分類
        $objForm->ae( array("type"        => "select",
                            "name"        => "EXPENSE_L_CD",
                            "extrahtml"  => "onchange=\"chgCombo(this)\"",
                            "value"      => $model->Expense["L"],
                            "size"        => "1",
                            "options"     => $opt ));

        $arg["EXPENSE_L_CD"] = $objForm->ge("EXPENSE_L_CD");

        //費目中分類
        $objForm->ae( array("type"        => "select",
                            "name"        => "EXPENSE_M_CD",
                            "extrahtml"  => "onchange=\"chgCombo(this)\"",
                            "value"      => $model->Expense["M"],
                            "size"        => "1",
                            "options"     => $opt2 ));

        $opt = array();
        $opt2 = array();
        $opt[] = array("label"  => '',
                    "value" => '');
        $opt2[] = array("label"  => '',
                    "value" => '');

        $arg["EXPENSE_M_CD"] = $objForm->ge("EXPENSE_M_CD");
        
        $result = $db->query(knjxexpkQuery::selectMoney_Due_M_Dat($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($row["REDUCTION_REASON"] != ""){
                $opt[$row["REDUCTION_REASON"]] = array("label"     => $row["REDUCTION_REASON"] ."：" .htmlspecialchars($row["REDUCTION_REASON2"]),
                               "value"     => $row["REDUCTION_REASON"]);
            }
            if ($row["BANK_TRANS_STOP_RESON"] != ""){
                $opt2[$row["BANK_TRANS_STOP_RESON"]] = array("label"     => $row["BANK_TRANS_STOP_RESON"] ."：" .htmlspecialchars($row["BANK_TRANS_STOP_RESON2"]),
                               "value"     => $row["BANK_TRANS_STOP_RESON"]);
            }
        }
        //納入必要金額
        $objForm->ae( array("type"        => "text",
                            "size"        => 10,
                            "maxlength"   => 8,
                            "extrahtml"  => "onblur=\"this.value=toInteger(this.value)\" style=\"text-align:right\"",
                            "name"        => "MONEY_DUE"));

        $arg["MONEY_DUE"] = $objForm->ge("MONEY_DUE");

        ksort($opt);
        //減免事由
        $objForm->ae( array("type"        => "select",
                            "name"        => "REDUCTION_REASON",
                            "size"        => "1",
                            "options"     => $opt ));

        $arg["REDUCTION_REASON"] = $objForm->ge("REDUCTION_REASON");

        $opt = array();
        $opt[] = array("label"  => '',
                    "value" => '');
        $opt[] = array("label"  => '分納設定なし',
                    "value" => '1');
        $opt[] = array("label"  => '分納設定あり',
                    "value" => '2');
        //分納
        $objForm->ae( array("type"        => "select",
                            "name"        => "INST_CD",
                            "size"        => "1",
                            "options"     => $opt ));

        $arg["INST_CD"] = $objForm->ge("INST_CD");

        ksort($opt2);
        //銀行振替停止事由
        $objForm->ae( array("type"        => "select",
                            "name"        => "BANK_TRANS_STOP_REASON",
                            "size"        => "1",
                            "options"     => $opt2 ));

        $arg["BANK_TRANS_STOP_REASON"] = $objForm->ge("BANK_TRANS_STOP_REASON");

        $opt = array();
        $opt[] = array("label"  => '',
                    "value" => '');
        $opt[] = array("label"  => '1:銀行処理結果CSVより入金',
                    "value" => '1');
        $opt[] = array("label"  => '2:校納金システムより入金',
                    "value" => '2');

        //入金データ区分
        $objForm->ae( array("type"        => "select",
                            "name"        => "PAID_INPUT_FLG",
                            "size"        => "1",
                            "options"     => $opt ));

        $arg["PAID_INPUT_FLG"] = $objForm->ge("PAID_INPUT_FLG");
        //入金日
        $arg["PAID_MONEY_DATE"] = View::popUpCalendar($objForm, "PAID_MONEY_DATE");
        //入金額
        $objForm->ae( array("type"        => "text",
                            "size"        => 10,
                            "maxlength"   => 10,
                            "extrahtml"  => "onblur=\"this.value=toInteger(this.value)\" style=\"text-align:right\"",
                            "name"        => "PAID_MONEY"));

        $arg["PAID_MONEY"] = $objForm->ge("PAID_MONEY");
        $opt = array();
        $opt2 = array();
        $opt[] = array("label"  => '',
                    "value" => '');
        $opt2[] = array("label"  => '',
                    "value" => '');
                
        $result = $db->query(knjxexpkQuery::selectMoney_Paid_M_Dat($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            if ($row["PAID_MONEY_DIV"] != ""){
                $opt[$row["PAID_MONEY_DIV"]] = array("label"     => $row["PAID_MONEY_DIV"] ."：" .htmlspecialchars($row["PAID_MONEY_DIV2"]),
                               "value"     => $row["PAID_MONEY_DIV"]);
            }
            if ($row["REPAY_DEV"] != ""){
                $opt2[$row["REPAY_DEV"]] = array("label"     => $row["REPAY_DEV"] ."：" .htmlspecialchars($row["REPAY_DEV2"]),
                               "value"     => $row["REPAY_DEV"]);
            }

        }
        ksort($opt);
        //入金区分
        $objForm->ae( array("type"        => "select",
                            "name"        => "PAID_MONEY_DIV",
                            "size"        => "1",
                            "options"     => $opt ));

        $arg["PAID_MONEY_DIV"] = $objForm->ge("PAID_MONEY_DIV");        
        //返金日
        $arg["REPAY_DATE"] = View::popUpCalendar($objForm, "REPAY_DATE");
        //返金額
        $objForm->ae( array("type"        => "text",
                            "size"        => 10,
                            "maxlength"   => 8,
                            "extrahtml"  => "onblur=\"this.value=toInteger(this.value)\" style=\"text-align:right\"",
                            "name"        => "REPAY_MONEY"));

        $arg["REPAY_MONEY"] = $objForm->ge("REPAY_MONEY");

        ksort($opt2);
        //返金区分
        $objForm->ae( array("type"        => "select",
                            "name"        => "REPAY_DEV",
                            "size"        => "1",
                            "options"     => $opt ));

        $arg["REPAY_DEV"] = $objForm->ge("REPAY_DEV");

        //実行ボタン
        $objForm->ae( array("type" 		  => "button",
                            "name"        => "btn_search",
                            "value"       => "検索",
                            "extrahtml"   => "onclick=\"return search_submit('search5');\"" ));

        $arg["btn_search"] = $objForm->ge("btn_search");

        //閉じるボタン
        $objForm->ae( array("type" 		  => "button",
                            "name"        => "btn_end",
                            "value"       => "戻る",
                            "extrahtml"   => "onclick=\"return btn_back();\"" ));

        $arg["btn_end"] = $objForm->ge("btn_end");
        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd" ) );

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjxSearch5.html", $arg);
    }
}
?>