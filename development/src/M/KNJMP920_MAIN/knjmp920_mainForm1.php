<?php

require_once('for_php7.php');


class knjmp920_mainForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm        = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjmp920_mainindex.php", "", "main");
        
        if (!isset($model->warning) && $model->getAuth != "" && $model->cmd !== 'edit') {
            $Row = knjmp920_mainQuery::getRow($model);
        } else {
            $Row =& $model->field;
        }
        
        //DB接続
        $db = Query::dbCheckOut();
        
        //本締めデータチェック
        $model->getHonjimeCount = "";
        $model->getHonjimeCount = $db->getOne(knjmp920_mainQuery::getCloseFlgData($model));

        //支出伺の状況を取得(比較時に利用)
        if ($model->getRequestNo) {
            $model->getApproval = $db->getOne(knjmp920_mainQuery::getOutgoData($model, "APPROVAL"));
            $model->getCancel   = $db->getOne(knjmp920_mainQuery::getOutgoData($model, "CANCEL"));
            //支出伺画面から遷移してきていない場合
            if(!$model->getOutgoRequestNo) {
                $model->getOutgoRequestNo = $db->getOne(knjmp920_mainQuery::getOutgoData($model, "REQUEST_NO"));
            }
        } else {
            $model->getApproval = "";
            $model->getCancel   = "";
        }
        
        //表示項目
        //伝票番号
        $arg["data"]["REQUEST_NO"] = $model->getRequestNo;
        
        //各項目
        //購入項目
        $extra = "onchange=\"return btn_submit('edit');\"";
        $query = knjmp920_mainQuery::getLevyMDiv();
        makeCombo($objForm, $arg, $db, $query, $Row["KOUNYU_L_M_CD"], "KOUNYU_L_M_CD", $extra, 1, "BLANK", $model);
        
        //購入伺い日
        $arg["data"]["REQUEST_DATE"] = View::popUpCalendar($objForm, "REQUEST_DATE",str_replace("-","/",$Row["REQUEST_DATE"]),"");
        
        //起案者
        $extra = "";
        $query = knjmp920_mainQuery::getRequestStaff($model);
        makeCombo($objForm, $arg, $db, $query, $Row["REQUEST_STAFF"], "REQUEST_STAFF", $extra, 1, "BLANK", $model);
        
        //摘要内容
        $model->sumTotalPrice = "";
        $model->sumTotalTax = "";
        for ($i = 1; $i <= $model->koumoku; $i++) {
            //通常表示
            if ($model->cmd !== 'edit') {
                //品名等
                $arg["data"]["LEVY_S_NAME".$i] = $Row["LEVY_S_NAME".$i];
                //単価
                $arg["data"]["COMMODITY_PRICE".$i] = $Row["COMMODITY_PRICE".$i];
                //数量
                $arg["data"]["COMMODITY_CNT".$i] = $Row["COMMODITY_CNT".$i];
                //金額(税抜き)
                $arg["data"]["TOTAL_PRICE_ZEINUKI".$i] = $Row["TOTAL_PRICE_ZEINUKI".$i];
                //備考
                $arg["data"]["REMARK".$i] = $Row["REMARK".$i];
                
                //消費税
                $model->sumTotalTax += $Row["TOTAL_TAX".$i];
                //金額合計(税込み)
                $model->sumTotalPrice += $Row["TOTAL_PRICE".$i];
                //以下、サブミット時に表示するためhidden格納
                knjCreateHidden($objForm, "LEVY_S_NAME".$i, $Row["LEVY_S_NAME".$i]);
                knjCreateHidden($objForm, "COMMODITY_PRICE".$i, $Row["COMMODITY_PRICE".$i]);
                knjCreateHidden($objForm, "COMMODITY_CNT".$i, $Row["COMMODITY_CNT".$i]);
                knjCreateHidden($objForm, "TOTAL_PRICE_ZEINUKI".$i, $Row["TOTAL_PRICE_ZEINUKI".$i]);
                knjCreateHidden($objForm, "REMARK".$i, $Row["REMARK".$i]);
                knjCreateHidden($objForm, "TOTAL_TAX".$i, $Row["TOTAL_TAX".$i]);
                knjCreateHidden($objForm, "TOTAL_PRICE".$i, $Row["TOTAL_PRICE".$i]);
            //サブミット表示用
            } else {
                //品名等
                $arg["data"]["LEVY_S_NAME".$i] = $model->setRow["LEVY_S_NAME".$i];
                //単価
                $arg["data"]["COMMODITY_PRICE".$i] = $model->setRow["COMMODITY_PRICE".$i];
                //数量
                $arg["data"]["COMMODITY_CNT".$i] = $model->setRow["COMMODITY_CNT".$i];
                //金額(税抜き)
                $arg["data"]["TOTAL_PRICE_ZEINUKI".$i] = $model->setRow["TOTAL_PRICE_ZEINUKI".$i];
                //備考
                $arg["data"]["REMARK".$i] = $model->setRow["REMARK".$i];
                
                //消費税
                $model->sumTotalTax += $model->setRow["TOTAL_TAX".$i];
                //金額合計(税込み)
                $model->sumTotalPrice += $model->setRow["TOTAL_PRICE".$i];
                //以下、サブミット時に表示するためhidden格納
                knjCreateHidden($objForm, "LEVY_S_NAME".$i, $model->setRow["LEVY_S_NAME".$i]);
                knjCreateHidden($objForm, "COMMODITY_PRICE".$i, $model->setRow["COMMODITY_PRICE".$i]);
                knjCreateHidden($objForm, "COMMODITY_CNT".$i, $model->setRow["COMMODITY_CNT".$i]);
                knjCreateHidden($objForm, "TOTAL_PRICE_ZEINUKI".$i, $model->setRow["TOTAL_PRICE_ZEINUKI".$i]);
                knjCreateHidden($objForm, "REMARK".$i, $model->setRow["REMARK".$i]);
                knjCreateHidden($objForm, "TOTAL_TAX".$i, $model->setRow["TOTAL_TAX".$i]);
                knjCreateHidden($objForm, "TOTAL_PRICE".$i, $model->setRow["TOTAL_PRICE".$i]);
            }
        }
        //消費税
        $arg["data"]["SUM_TOTAL_TAX"] = $model->sumTotalTax;
        
        //振込手数料
        $extra = " STYLE=\"ime-mode: inactive;text-align:right;\" onblur=\"this.value=toInteger(this.value);keisanTotal(this.value);\"";
        $arg["data"]["REQUEST_TESUURYOU"] = knjCreateTextBox($objForm, $Row["REQUEST_TESUURYOU"], "REQUEST_TESUURYOU", 7, 7, $extra);
                
        //総支出額(振込手数料と合算)
        $arg["data"]["SUM_TOTAL_PRICE_ALL"] = $model->sumTotalPrice + $Row["REQUEST_TESUURYOU"];
        knjCreateHidden($objForm, "SUM_TOTAL_PRICE", $model->sumTotalPrice);
        
        //見積書徴収業者
        //マスタ、テキストより選択
        for ($i = 1;$i <= 4; $i++) {
            $extra = "";
            $query = knjmp920_mainQuery::getTraderMst($model);
            makeCombo($objForm, $arg, $db, $query, $Row["TRADER_CD".$i], "TRADER_CD".$i, $extra, 1, "BLANK", $model);
            
            $extra = " STYLE=\"ime-mode: active;\"";
            $arg["data"]["TRADER_NAME".$i] = knjCreateTextBox($objForm, $Row["TRADER_NAME".$i], "TRADER_NAME".$i, 45, 120, $extra);
            //決定フラグ
            $extra = " onclick=\"tradercheckFlg('".$i."');\"";
            if ($Row["TRADER_KAKUTEI".$i] == "1") {
                $extra .= "checked='checked' ";
            } else {
                $extra .= "";
            }
            $arg["data"]["TRADER_KAKUTEI".$i] = knjCreateCheckBox($objForm, "TRADER_KAKUTEI".$i, "1", $extra);
        }
        
        //見積合わせ日時
        $arg["data"]["KOUNYU_MITUMORI_DATE"] = View::popUpCalendar($objForm, "KOUNYU_MITUMORI_DATE",str_replace("-","/",$Row["KOUNYU_MITUMORI_DATE"]),"");
        
        //契約方法
        $extra = " STYLE=\"ime-mode: active;\"";
        $arg["data"]["KEIYAKU_HOUHOU"] = knjCreateTextBox($objForm, $Row["KEIYAKU_HOUHOU"], "KEIYAKU_HOUHOU", 45, 120, $extra);
        
        //納入期限
        $arg["data"]["NOUNYU_LIMIT_DATE"] = View::popUpCalendar($objForm, "NOUNYU_LIMIT_DATE",str_replace("-","/",$Row["NOUNYU_LIMIT_DATE"]),"");
        
        //納入場所
        $extra = " STYLE=\"ime-mode: active;\"";
        $arg["data"]["NOUNYU_PLACE"] = knjCreateTextBox($objForm, $Row["NOUNYU_PLACE"], "NOUNYU_PLACE", 45, 120, $extra);
        
        //その他
        $extra = " STYLE=\"ime-mode: active;\"";
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 60, 120, $extra);

        //支出伺 状態表示
        if ($model->getOutgoRequestNo) {
            if ($model->getApproval === '1' && $model->getCancel == "") {
                $arg["data"]["SET_STATUS"] = $model->getOutgoRequestNo.'<font color="red">(決裁 済み)</font>';
            } else if ($model->getCancel === '1') {
                $arg["data"]["SET_STATUS"] = $model->getOutgoRequestNo.'<font color="red">(キャンセル)</font>';
            } else {
                $arg["data"]["SET_STATUS"] = $model->getOutgoRequestNo.'<font>(伺い中)</font>';
            }
        } else {
            $arg["data"]["SET_STATUS"] = '未作成';
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $Row);

        //hidden作成
        makeHidden($objForm, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();
        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjmp920_mainForm1.html", $arg); 
    }
}
//コンボ作成
function makeCombo(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "", $model) {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array ("label" => "",
                        "value" => "");
    }
    $result = $db->query($query);

    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)){
        $opt[] = array ("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $Row) {
    //新規登録
    $flg = "1";
    $root = REQUESTROOT;
    $auth = AUTHORITY;
    
    //印刷ボタンを作成する
    if ($model->getKounyuLMcd == "") {
        $extra = " onclick=\"btn_error('new');\"";
    } else {
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    }
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    
    //振り分け設定
    for ($i = 1; $i <= $model->koumoku; $i++) {
        if ($model->getRequestNo == "") {
            $extra = " onclick=\"btn_error('new');\"";
        } else if ($model->cmd === 'edit' && $model->getKounyuLMcd != "") {
            $extra = " onclick=\"btn_error('huriwake');\"";
        } else {
            $subdata  = "wopen('".REQUESTROOT."/M/KNJMP920_MEISAI/knjmp920_meisaiindex.php?cmd=main&SEND_AUTH={$model->auth}";
            $subdata .= "&SEND_PRGID=KNJMP920_MAIN&SEND_KOUNYU_L_CD={$Row["KOUNYU_L_CD"]}&SEND_KOUNYU_M_CD={$Row["KOUNYU_M_CD"]}";
            $subdata .= "&SEND_REQUEST_NO={$Row["REQUEST_NO"]}&SEND_YEAR={$Row["YEAR"]}&SEND_LINE_NO=".$i."";
            $subdata .= "&SEND_KOUNYU_L_M_CD={$Row["KOUNYU_L_M_CD"]}&SEND_INCOME_L_CD={$Row["INCOME_L_CD"]}";
            $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
            $extra = " onclick=\"$subdata\"";
        }
        $arg["button"]["btn_schreg".$i] = knjCreateBtn($objForm, "btn_schreg".$i, "登録画面", $extra);
    }
    //更 新
    if ($model->getApproval === '1' || $model->getCancel === '1' || $model->getHonjimeCount > 0) {
        $extra = " disabled";
    } else if ($model->cmd === 'edit' && $model->getKounyuLMcd != "") {
        $extra = " onclick=\"return btn_submit('delete_update');\"";
    } else {
        $extra = " onclick=\"return btn_submit('update');\"";
    }
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削 除
    if ($model->getApproval === '1' || $model->getHonjimeCount > 0) {
        $extra = " disabled";
    } else {
        $extra = " onclick=\"return btn_submit('delete');\"";
    }
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    //取消
    $extra = "onclick=\"return btn_submit('cancel');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra, "reset");
    //支出伺画面
    if ($model->getRequestNo == "") {
        $extra = " onclick=\"btn_error('new');\"";
    } else if ($model->cmd === 'edit' && $model->getKounyuLMcd != "") {
        $extra = " onclick=\"btn_error('huriwake');\"";
    } else {
        $subdata  = "wopen('".REQUESTROOT."/M/KNJMP910_MAIN/knjmp910_mainindex.php?cmd=main&SEND_AUTH={$model->auth}";
        $subdata .= "&SEND_PRGID=KNJMP920_MAIN&SEND_OUTGO_L_CD={$Row["KOUNYU_L_CD"]}&SEND_OUTGO_M_CD={$Row["KOUNYU_M_CD"]}";
        $subdata .= "&SEND_REQUEST_NO={$model->getOutgoRequestNo}&SEND_YEAR={$Row["YEAR"]}";
        $subdata .= "&SEND_OUTGO_L_M_CD={$Row["KOUNYU_L_M_CD"]}&SEND_KOUNYU_REQUEST_NO={$model->getRequestNo}";
        $subdata .= "&SUBWIN=SUBWIN2','SUBWIN2',0,0,screen.availWidth,screen.availHeight);";
        $extra = "onclick=\"$subdata\"";
    }
    $arg["button"]["btn_kessai"] = knjCreateBtn($objForm, "btn_kessai", "支出伺画面", $extra);
    
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model, $Row) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "PRGID", "KNJMP953");
    knjCreateHidden($objForm, "YEAR", $model->year);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "REQUEST_NO", $model->getRequestNo);
    
}
?>
