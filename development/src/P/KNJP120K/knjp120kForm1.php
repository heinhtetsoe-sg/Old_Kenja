<?php

require_once('for_php7.php');

class knjp120kForm1
{
    function main(&$model)
    {
        $objForm = new form;
        $db     = Query::dbCheckOut();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("list", "POST", "knjp120kindex.php", "", "edit");

        //生徒の名前を取得
        $row = $db->getRow(knjp120kQuery::getStudentName($model->schregno),DB_FETCHMODE_ASSOC);
        $arg["NAME"] = $row["SCHREGNO"]."　".$row["NAME_SHOW"];


        //リスト取得
        $result = $db->query(knjp120kQuery::getList($model));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            array_walk($row, "htmlspecialchars_array");

            if($row["EXPENSE_S_EXIST_FLG"] == "1") {
                $row["MONEY_DUE"] = $db->getOne(knjp120kQuery::getMoneyDue($model->schregno, $row["EXPENSE_M_CD"]));
                $sRepaiData = $db->getRow(knjp120kQuery::getRepayDiv($model->schregno, $row["EXPENSE_M_CD"]), DB_FETCHMODE_ASSOC);
                $row["REPAY_DEV"] = $sRepaiData["MIN_DIV"];
            }

            //リンク設定
            $row["EXPENSE_M_CD"] = View::alink("knjp120kindex.php", $row["EXPENSE_L_CD"].$row["EXPENSE_M_CD"], "target=bottom_frame",
                                    array("EXPENSE_M_CD"        => $row["EXPENSE_M_CD"],
                                          "EXPENSE_L_CD"        => $row["EXPENSE_L_CD"],
                                          "EXPENSE_M_NAME"      => $row["EXPENSE_M_NAME"],
                                          "MONEY_DUE"           => $row["MONEY_DUE"],
                                          "EXPENSE_S_EXIST_FLG" => $row["EXPENSE_S_EXIST_FLG"],
                                          "PAID_INPUT_FLG"      => $row["PAID_INPUT_FLG"],
                                          "INST_CD"             => $row["INST_CD"],
                                          "BANK_TRANS_SDATE"    => $row["BANK_TRANS_SDATE"],
                                          "cmd"                 => "edit"));

            //金額フォーマット
            $row["MONEY_DUE"] = (strlen($row["MONEY_DUE"])) ? number_format($row["MONEY_DUE"]): "";
            $row["PAID_MONEY"] = (strlen($row["PAID_MONEY"])) ? number_format($row["PAID_MONEY"]): "";
            $row["REPAY_MONEY"] = (strlen($row["REPAY_MONEY"])) ? number_format($row["REPAY_MONEY"]): "";

            //入金区分
            if (strlen($row["PAID_MONEY_DIV"])) {
                $row["PAID_MONEY_DIV"] = $db->getOne(knjp120kQuery::getName($model->year, "G205", $row["PAID_MONEY_DIV"]));
            }

            //返金区分 NO004
            if (strlen($row["REPAY_DEV"])) {
                $namecd2 = $row["EXPENSE_S_EXIST_FLG"] == "1" ? "G209" : "G212";
                $row["REPAY_DEV"] = $db->getOne(knjp120kQuery::getName($model->year, $namecd2, $row["REPAY_DEV"]));
                if($row["EXPENSE_S_EXIST_FLG"] == "1") {
                    $row["REPAY_DEV"] .= $sRepaiData["MIN_DIV"] == $sRepaiData["MAX_DIV"] ? "" : " 他";
                    $row["REPAY_DEV"] .= "({$sRepaiData["CNT"]})";
                }
            }

            //振替停止
            if (strlen($row["BANK_TRANS_STOP_RESON"])) {
                $row["BANK_TRANS_STOP_RESON"] = "停止";
#                $row["BANK_TRANS_STOP_RESON"] = $db->getOne(knjp120kQuery::getName($model->year, "G208", $row["BANK_TRANS_STOP_RESON"]));
            }

            //分納
            if (strlen($row["INST_CD"])) {
                $row["INST_CD"] = "分納";
            }

            //軽減 NO003
            if ($row["REDUC_DEC_FLG_1"] == 1 && $row["REDUC_DEC_FLG_2"] == 1) {
                $row["REDUC_DEC_FLG"] = "有";
            }else {
                $row["REDUC_DEC_FLG"] = "";
			}

            //日付変換
            $row["PAID_MONEY_DATE"] = str_replace("-", "/", $row["PAID_MONEY_DATE"]);

            $arg["data"][] = $row;
        }
        $result->free();
        Query::dbCheckIn($db);   

        //hiddenを作成する
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd"));
    
        //異動情報ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_form3",
                            "value"       => "異動情報",
                            "extrahtml"   => "onclick=\"dispInfo('form3')\"") );


        //交付情報ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_form4",
                            "value"       => "交付情報",
                            "extrahtml"   => "onclick=\"dispInfo('form4')\"") );

        //銀行情報ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_form5",
                            "value"       => "銀行情報",
                            "extrahtml"   => "onclick=\"dispInfo('form5')\"") );

        //申込情報ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_form6",
                            "value"       => "振込情報",
                            "extrahtml"   => "onclick=\"dispInfo('form6')\"") );

        //軽減情報ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_form7",
                            "value"       => "軽減情報",
                            "extrahtml"   => "onclick=\"dispInfo('form7')\"") );

        //費目中分類情報ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_form8",
                            "value"       => "費目中分類情報",
                            "extrahtml"   => "onclick=\"dispInfo('form8')\"") );

        //費目小分類情報ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_form9",
                            "value"       => "費目小分類情報",
                            "extrahtml"   => "onclick=\"dispInfo('form9')\"") );

        //分納情報ボタンを作成する
        $objForm->ae( array("type"        => "button",
                            "name"        => "btn_form10",
                            "value"       => "分納情報",
                            "extrahtml"   => "onclick=\"dispInfo('form10')\"") );

        //終了ボタンを作成
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_end",
                            "value"       => "終了",
                            "extrahtml"   => "style=\"display:none\" id=\"btn_end\" onclick=\"return closeWin();\"" ) );

        //戻るボタンを作成 NO002
        $objForm->ae( array("type" => "button",
                            "name"        => "btn_rtrn",
                            "value"       => "戻る",
                            "extrahtml"   => "style=\"display:none\" id=\"btn_rtrn\" onclick=\"dispInfo('form2')\"") );

        $arg["btn_form3"] = $objForm->ge("btn_form3");
        $arg["btn_form4"] = $objForm->ge("btn_form4");
        $arg["btn_form5"] = $objForm->ge("btn_form5");
        $arg["btn_form6"] = $objForm->ge("btn_form6");
        $arg["btn_form7"] = $objForm->ge("btn_form7");
        $arg["btn_form8"] = $objForm->ge("btn_form8");
        $arg["btn_form9"] = $objForm->ge("btn_form9");
        $arg["btn_form10"] = $objForm->ge("btn_form10");
        $arg["btn_end"] = $objForm->ge("btn_end");
        $arg["btn_rtrn"] = $objForm->ge("btn_rtrn");
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjp120kForm1.html", $arg);
    }
}
?>
