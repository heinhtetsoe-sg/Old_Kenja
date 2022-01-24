<?php

require_once('for_php7.php');


class knjp930_mainForm1 {
    function main(&$model) {
        //オブジェクト作成
        $objForm        = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjp930_mainindex.php", "", "main");
        
        if (!isset($model->warning) && $model->getAuth != "" && $model->cmd !== 'edit') {
            $Row = knjp930_mainQuery::getRow($model);
        } else {
            $Row =& $model->field;
        }
        
        //DB接続
        $db = Query::dbCheckOut();
        
        //本締めデータチェック
        $model->getHonjimeCount = "";
        $model->getHonjimeCount = $db->getOne(knjp930_mainQuery::getCloseFlgData($model));

        //支出伺の状況を取得(比較時に利用)
        if ($model->getRequestNo) {
            $model->getApproval = $db->getOne(knjp930_mainQuery::getOutgoData($model, "APPROVAL"));
            $model->getCancel   = $db->getOne(knjp930_mainQuery::getOutgoData($model, "CANCEL"));
            //支出伺画面から遷移してきていない場合
            if(!$model->getOutgoRequestNo) {
                $model->getOutgoRequestNo = $db->getOne(knjp930_mainQuery::getOutgoData($model, "REQUEST_NO"));
            }
        } else {
            $model->getApproval = "";
            $model->getCancel   = "";
        }
        
        //表示項目
        //伝票番号
        $arg["data"]["REQUEST_NO"] = $model->getRequestNo;
        
        //各項目
        //施行項目
        $extra = "onchange=\"return btn_submit('edit');\"";
        $query = knjp930_mainQuery::getLevyMDiv($model);
        makeCombo($objForm, $arg, $db, $query, $Row["SEKOU_L_M_CD"], "SEKOU_L_M_CD", $extra, 1, "BLANK", $model);
        
        //施行伺い日
        $arg["data"]["REQUEST_DATE"] = View::popUpCalendar($objForm, "REQUEST_DATE",str_replace("-","/",$Row["REQUEST_DATE"]),"");
        
        //起案者
        $extra = "";
        $query = knjp930_mainQuery::getRequestStaff($model);
        makeCombo($objForm, $arg, $db, $query, $Row["REQUEST_STAFF"], "REQUEST_STAFF", $extra, 1, "BLANK", $model);
        
        //事業名
        $extra = " STYLE=\"ime-mode: active;\"";
        $arg["data"]["SEKOU_JIGYOU_NAME"] = knjCreateTextBox($objForm, $Row["SEKOU_JIGYOU_NAME"], "SEKOU_JIGYOU_NAME", 45, 120, $extra);
        
        //施行内容
        $extra = " STYLE=\"ime-mode: active;\"";
        $arg["data"]["SEKOU_NAIYOU"] = knjCreateTextBox($objForm, $Row["SEKOU_NAIYOU"], "SEKOU_NAIYOU", 60, 120, $extra);

        //施行期間（期日）
        $arg["data"]["SEKOU_DATE_FROM"] = View::popUpCalendar($objForm, "SEKOU_DATE_FROM",str_replace("-","/",$Row["SEKOU_DATE_FROM"]),"");
        $arg["data"]["SEKOU_DATE_TO"] = View::popUpCalendar($objForm, "SEKOU_DATE_TO",str_replace("-","/",$Row["SEKOU_DATE_TO"]),"");
        
        //施行場所
        $extra = " STYLE=\"ime-mode: active;\"";
        $arg["data"]["SEKOU_PLACE"] = knjCreateTextBox($objForm, $Row["SEKOU_PLACE"], "SEKOU_PLACE", 60, 120, $extra);

        //予算額
        $extra = " STYLE=\"ime-mode: inactive;text-align:right;\" onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["REQUEST_GK"] = knjCreateTextBox($objForm, $Row["REQUEST_GK"], "REQUEST_GK", 7, 7, $extra);
        
        //契約方法
        $extra = " STYLE=\"ime-mode: active;\"";
        $arg["data"]["KEIYAKU_HOUHOU"] = knjCreateTextBox($objForm, $Row["KEIYAKU_HOUHOU"], "KEIYAKU_HOUHOU", 45, 120, $extra);
        
        //付記事項
        $extra = " STYLE=\"ime-mode: active;height:70px;\"";
        $arg["data"]["REMARK"] = KnjCreateTextArea($objForm, "REMARK", 4, 61, "soft", $extra, $Row["REMARK"]);

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
        View::toHTML($model, "knjp930_mainForm1.html", $arg); 
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
    if ($name == 'REQUEST_STAFF') {
        $value = ($value && $value_flg) ? $value : STAFFCD;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $Row) {
    //新規登録
    $flg = "1";
    $root = REQUESTROOT;
    $auth = AUTHORITY;
    
    //印刷ボタンを作成する
    if ($model->getRequestNo == "") {
        $extra = " onclick=\"btn_error('new');\"";
    } else {
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    }
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "プレビュー／印刷", $extra);
    
    //更 新
    if ($model->getApproval === '1' || $model->getCancel === '1' || $model->getHonjimeCount > 0) {
        $extra = " disabled";
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
    } else if ($model->cmd === 'edit' && $model->getSekouLMcd != "") {
        $extra = " onclick=\"btn_error('huriwake');\"";
    } else {
        $subdata  = "wopen('".REQUESTROOT."/P/KNJP905_MAIN/knjp905_mainindex.php?cmd=main&SEND_AUTH={$model->auth}";
        $subdata .= "&SEND_PRGID=KNJP930_MAIN&SEND_OUTGO_L_CD={$Row["SEKOU_L_CD"]}&SEND_OUTGO_M_CD={$Row["SEKOU_M_CD"]}";
        $subdata .= "&SEND_SCHOOL_KIND={$model->getSchoolKind}&SEND_REQUEST_NO={$model->getOutgoRequestNo}&SEND_YEAR={$Row["YEAR"]}";
        $subdata .= "&SEND_OUTGO_L_M_CD={$Row["SEKOU_L_M_CD"]}&SEND_SEKOU_REQUEST_NO={$model->getRequestNo}";
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
    knjCreateHidden($objForm, "PRGID", "KNJP954");
    knjCreateHidden($objForm, "YEAR", $model->year);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "REQUEST_NO", $model->getRequestNo);
    knjCreateHidden($objForm, "SCHOOLCD", sprintf("%012d", SCHOOLCD));
    knjCreateHidden($objForm, "SCHOOL_KIND", $model->getSchoolKind);
    knjCreateHidden($objForm, "DOCUMENTROOT", DOCUMENTROOT);
}
?>
