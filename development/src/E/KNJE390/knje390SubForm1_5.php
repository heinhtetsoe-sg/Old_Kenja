<?php

require_once('for_php7.php');

class knje390SubForm1_5
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1_5", "POST", "knje390index.php", "", "subform1_5");
        
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = VIEW::setIframeJs();

        //DB接続
        $db = Query::dbCheckOut();
        
        //表示日付をセット
        if ($model->record_date === 'NEW') {
            $setHyoujiDate = '';
        } else {
            $setHyoujiDate = '　　<font color="RED"><B>'.str_replace("-", "/", $model->record_date).' 履歴データ 参照中</B></font>';
        }

        //生徒情報
        $info = $db->getRow(knje390Query::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"].$setHyoujiDate;
         // Add by PP for Title 2020-02-03 start
        if($info["NAME_SHOW"] != ""){
            $arg["TITLE"] = "B プロフィールの福祉画面";
            echo "<script>var TITLE= '".$arg["TITLE"]."';
              </script>";
        }
        // for 915 error
        if($model->message915 == ""){
            echo "<script>sessionStorage.removeItem(\"KNJE390SubFrom1_5_CurrentCursor915\");</script>";
        } else {
          echo "<script>var error195= '".$model->message915."';
              sessionStorage.setItem(\"KNJE390SubFrom1_5_CurrentCursor915\", error195);
              sessionStorage.removeItem(\"KNJE390SubFrom1_5_CurrentCursor\");
              </script>";
            $model->message915 = "";
        }
        // Add by PP for Title 2020-02-20 end

        /************/
        /* 履歴一覧 */
        /************/
        $rirekiCnt = makeList($arg, $db, $model);

        /************/
        /* テキスト */
        /************/
        //初期画面または画面サブミット時は、GET取得の変数を初期化する
        if ($model->cmd == "subform1_welfare") {
            unset($model->getYear);
            unset($model->getRecordDiv);
            unset($model->getRecordNo);
            unset($model->getRecordSeq);
            
            $model->field["SERVICE_NAME"] = "";
            $model->field["CENTERCD"] = "";
            $model->field["SERVICE_CENTERCD"] = "";
            $model->field["WELFARE_REMARK"] = "";

            for ($flg = 1; $flg <= 3; $flg++) {
                if (get_count($model->itemdiv[$flg])) {
                    foreach ($model->itemdiv[$flg] as $ikey => $ival) {
                        $cnt = ($flg == "1") ? 6 : 3;
                        for ($i = 1; $i <= $cnt; $i++) {
                            $model->field["ITEMCD".$i."_".$ival] = "";
                        }
                    }
                }
            }

            //事前チェック
            if ($model->field["RECORD_DIV"] == "1" || $model->field["RECORD_DIV"] == "3") {
                $flg = ($model->field["RECORD_DIV"] == "3") ? 3 : (($model->field["RECORD_NO"] == "") ? 2 : $model->field["RECORD_NO"]) - 1;
                if (!get_count($model->itemdiv[$flg])) {
                    $msg = ($flg == 1) ? '補装具区分' : '日常生活用具区分';
                    $arg["jscript"] = "preCheck('".$msg."');";
                }
            }
        }
        //医療情報取得
        if ($model->cmd == "subform1_welfare_set"){
            if (isset($model->schregno) && !isset($model->warning) && $model->cmd != "subform1_welfare2") {
                $Row = $db->getRow(knje390Query::getSubQuery1WelfareGetData($model), DB_FETCHMODE_ASSOC);
                //補装具等の給付
                if ($Row["RECORD_DIV"] === '1' || $Row["RECORD_DIV"] === '3') {
                    $query = knje390Query::getSubQuery1WelfareGetItemcdData($model);
                    $result = $db->query($query);
                    while ($rowItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $cnt = ($Row["RECORD_DIV"] == '1' && $rowItem["RECORD_NO"] == '2') ? 6 : 3;
                        for ($i = 1; $i <= $cnt; $i++) {
                            $Row["ITEMCD".$i."_".$rowItem["ITEMDIV"]] = $rowItem["ITEMCD".$i];
                        }
                    }
                }
            } else {
                $Row =& $model->field;
            }
        } else {
            $Row =& $model->field;
        }
        
        //補装具等の給付、相談支援事業所、サービス利用の切換
        // Add by PP for PC-Talker 2020-02-03 start
        $extra = "onchange=\"current_cursor('RECORD_DIV');return btn_submit('subform1_welfare')\" id=\"RECORD_DIV\" aria-label=\"種別\"";
        // Add by PP for PC-Talker 2020-02-20 end
        $query = knje390Query::getWelfareRecordDiv();
        makeCmb($objForm, $arg, $db, $query, "RECORD_DIV", $Row["RECORD_DIV"], $extra, 1, 1);
        if ($Row["RECORD_DIV"] == "3") {
            $arg["SERVICE_NAIYOU"] = "1";
        } else {
            $arg["NOT_SERVICE_NAIYOU"] = "1";
        }

        //補装具等の給付の①サービス、②補装具、③日常生活用具の切換
        $opt = array(1, 2, 3);
        $Row["RECORD_NO"] = ($Row["RECORD_NO"] == "") ? "2" : $Row["RECORD_NO"];
        // Add by PP for PC-Talker 2020-02-03 start
        $label = "支援内容項目の";
        $extra = array("id=\"RECORD_NO1\" onclick=\"current_cursor('RECORD_NO1'); return btn_submit('subform1_welfare')\" aria-label=\"{$label}画面切換\"", "id=\"RECORD_NO2\" onclick=\"current_cursor('RECORD_NO2'); return btn_submit('subform1_welfare')\" aria-label=\"{$label}補装具\"", "id=\"RECORD_NO3\" onclick=\"current_cursor('RECORD_NO3'); return btn_submit('subform1_welfare')\" aria-label=\"{$label}日常生活用具\"");
        $radioArray = knjCreateRadio($objForm, "RECORD_NO", $Row["RECORD_NO"], $extra, $opt, get_count($opt));
        // Add by PP for PC-Talker 2020-02-20 end
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //画面切換
        if ($Row["RECORD_DIV"] === '1') {
            $arg["RECORD_DIV1"] = "1";
            //サービス
            if ($Row["RECORD_NO"] === '1') {
                $arg["RECORD_DIV1_1"] = "1";
                $extra = "";
                $arg["data"]["SERVICE_NAME"] = knjCreateTextBox($objForm, $Row["SERVICE_NAME"], "SERVICE_NAME", 70, 70, $extra);
            //補装具
            } else if ($Row["RECORD_NO"] === '2') {
                $arg["RECORD_DIV1_23"] = "1";
                $arg["data"]["COMBO_NAME"] = '補装具';

                $query = knje390Query::getItemdivComboName("1");
                $result = $db->query($query);
                while ($rowItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $rowItem["ITEMDIV_NAME"] = $rowItem["LABEL"];
                    $itemcdList = $space = "";
                    for ($i = 1; $i <= 6; $i++) {
                        $query = knje390Query::getItemcdComboName("1", $rowItem["VALUE"]);
                        $name = "ITEMCD".$i."_".$rowItem["VALUE"];
                        // Add by PP for PC-Talker 2020-02-03 start
                        $extra = "aria-label=\"補装具の{$rowItem["LABEL"]}{$i}\"";
                        // Add by PP for PC-Talker 2020-02-20 end
                        $itemcdList .= $space.makeCmbReturn($objForm, $arg, $db, $query, $name, $Row[$name], $extra, 1, 1);
                        $space = ' ';
                    }
                    $rowItem["ITEMCD_SHOW"] = $itemcdList;
                    $arg["item"][] = $rowItem;
                }
                $result->free();
            //日常生活用具
            } else if ($Row["RECORD_NO"] === '3') {
                $arg["RECORD_DIV1_23"] = "1";
                $arg["data"]["COMBO_NAME"] = '日常生活用具';

                $query = knje390Query::getItemdivComboName("2");
                $result = $db->query($query);
                while ($rowItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $rowItem["ITEMDIV_NAME"] = $rowItem["LABEL"];
                    $itemcdList = $space = "";
                    for ($i = 1; $i <= 3; $i++) {
                        $query = knje390Query::getItemcdComboName("2", $rowItem["VALUE"]);
                        $name = "ITEMCD".$i."_".$rowItem["VALUE"];
                        // Add by PP for PC-Talker 2020-02-03 start
                        $extra = "aria-label=\"日常生活用具の{$rowItem["LABEL"]}{$i}\"";
                        // Add by PP for PC-Talker 2020-02-20 end
                        $itemcdList .= $space.makeCmbReturn($objForm, $arg, $db, $query, $name, $Row[$name], $extra, 1, 1);
                        $space = ' ';
                    }
                    $rowItem["ITEMCD_SHOW"] = $itemcdList;
                    $arg["item"][] = $rowItem;
                }
                $result->free();
            }
            //備考
            // Add by PP for PC-Talker 2020-02-03 start
            $label = "備考";
            $extra = "aria-label=\"{$label}全角30文字まで\"";
            // Add by PP for PC-Talker 2020-02-20 end
            $arg["data"]["WELFARE_REMARK"] = knjCreateTextBox($objForm, $Row["WELFARE_REMARK"], "WELFARE_REMARK", 60, 60, $extra);
            $arg["data"]["WELFARE_REMARK_SIZE"] = '<font size="1" color="red">(全角30文字まで)</font>';
            $arg["data"]["WELFARE_REMARK_NAME"] = '備考';
        } else if ($Row["RECORD_DIV"] === '2' || $Row["RECORD_DIV"] === '3') {
            // Add by PP for PC-Talker 2020-02-03 start
            $label = "備考";
            $extra = "aria-label=\"{$label}全角4文字まで\"";
            // Add by PP for PC-Talker 2020-02-20 end
            $arg["RECORD_DIV23"] = "1";
            if ($Row["RECORD_DIV"] === '2') {
                $arg["RECORD_DIV2"] = "1";
                $arg["data"]["WELFARE_REMARK_NAME"] = '備考';
                //圏域
                // Add by PP for PC-Talker 2020-02-03 start
                $extra = "id=\"AREACD\" onChange=\"current_cursor('AREACD'); return btn_submit('subform1_welfare2')\" aria-label=\"圏域\"";
                // Add by PP for PC-Talker 2020-02-20 end
                $query = knje390Query::getNameMst("E040");
                makeCmb($objForm, $arg, $db, $query, "AREACD", $Row["AREACD"], $extra, 1, 1);
                //相談・支援事業所
                $query = knje390Query::getWelfareCentercdComboName($Row["AREACD"]);
                makeCmb($objForm, $arg, $db, $query, "CENTERCD", $Row["CENTERCD"], "aria-label=\"事業所\"", 1, 1);
                //担当者
                // Add by PP for PC-Talker 2020-02-03 start
                $extra = "aria-label=\"担当者全角4文字まで\"";
                // Add by PP for PC-Talker 2020-02-20 end
                $arg["data"]["SERVICE_CHARGE"] = knjCreateTextBox($objForm, $Row["SERVICE_CHARGE"], "SERVICE_CHARGE", 8, 8, $extra);
                $arg["data"]["SERVICE_CHARGE_SIZE"] = '<font size="1" color="red">(全角4文字まで)</font>';
            } else {
                // Add by PP for PC-Talker 2020-02-03 start
                $label = "備考";
                $extra = "aria-label=\"{$label}全角50文字2行まで\"";
                // Add by PP for PC-Talker 2020-02-20 end
                $arg["RECORD_DIV3"] = "1";
                $arg["data"]["WELFARE_REMARK_NAME"] = '備考';
                //サービス利用事業所
                // Add by PP for PC-Talker 2020-02-03 start
                $extra = "style=\"overflow:auto;\" aria-label=\"事業所全角50文字2行まで\" id=\"SERVICE_CENTER_TEXT\"";
                // Add by PP for PC-Talker 2020-02-20 end
                $arg["data"]["SERVICE_CENTER_TEXT"] = knjCreateTextArea($objForm, "SERVICE_CENTER_TEXT", 2, 101, "soft", $extra, $Row["SERVICE_CENTER_TEXT"]);
                $arg["data"]["SERVICE_CENTER_TEXT_SIZE"] = '<font size="1" color="red">(全角50文字2行まで)</font>';
                //サービス内容
                $arg["data"]["COMBO_NAME"] = 'サービス内容';
                $query = knje390Query::getItemdivComboName("3");
                $result = $db->query($query);
                while ($rowItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $rowItem["ITEMDIV_NAME"] = $rowItem["LABEL"];
                    $itemcdList = $space = "";
                    for ($i = 1; $i <= 3; $i++) {
                        $query = knje390Query::getItemcdComboName("3", $rowItem["VALUE"]);
                        $name = "ITEMCD".$i."_".$rowItem["VALUE"];
                        $itemcdList .= $space.makeCmbReturn($objForm, $arg, $db, $query, $name, $Row[$name], "", 1, 1);
                        $space = ' ';
                    }
                    $rowItem["ITEMCD_SHOW"] = $itemcdList;
                    $arg["item"][] = $rowItem;
                }
                $result->free();
            }
            //通院の状況・特記事項
            // Add by PP for PC-Talker 2020-02-03 start
            $extra = "style=\"height:35px; overflow:auto;\" aria-label=\"備考全角20文字2行まで\"";
            // Add by PP for PC-Talker 2020-02-20 end
            $arg["data"]["WELFARE_REMARK"] = knjCreateTextArea($objForm, "WELFARE_REMARK", 2, 41, "soft", $extra, $Row["WELFARE_REMARK"]);
            $arg["data"]["WELFARE_REMARK_SIZE"] = '<font size="1" color="red">(全角20文字2行まで)</font>';
        }

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje390SubForm1_5.html", $arg); 
    }
}

//履歴一覧
function makeList(&$arg, $db, $model) {
    //項目名取得
    $itemNameArray = array();
    for ($flg = 1; $flg <= 3; $flg++) {
        $query = knje390Query::getItemcdName("{$flg}");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $itemNameArray[$flg][$row["NAMECD"]] = $row["NAME"];
        }
        $result->free();
    }
    $e039ItemNameArray = array();
    $query = knje390Query::getItemcdComboName("3", "1");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $e039ItemNameArray["1"][$row["VALUE"]] = $row["NAME1"];
    }
    $result->free();
    $query = knje390Query::getItemcdComboName("3", "2");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $e039ItemNameArray["2"][$row["VALUE"]] = $row["NAME1"];
    }
    $result->free();

    $retCnt = 0;
    $query = knje390Query::getSubQuery1WelfareRecordList($model);
    $result = $db->query($query);
    $divCount1 = 1;
    $divCount2 = 1;
    $divCount3 = 1;
    $divCount4 = 1;
    $divCount5 = 1;
    $centerName = "";
    while ($rowlist = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($rowlist["RECORD_DIV"] === '1') {
            $rowlist["RECORD_DIV_NAME"] = '補装具等の給付';
            if ($rowlist["RECORD_NO"] === '1') {
                $rowlist["RECORD_DIV_NAME"] = $rowlist["RECORD_DIV_NAME"].'(サービス)'.$divCount1;
                $rowlist["CONTENTS_NAME"] = 'サービス';
                $rowlist["CONTENTS_NAIYOU"] = $rowlist["SERVICE_NAME"];
                $divCount1++;
            } else if ($rowlist["RECORD_NO"] === '2') {
                $rowlist["RECORD_DIV_NAME"] = $rowlist["RECORD_DIV_NAME"].'(補装具)'.$divCount2;

                $itemcdArray = array();
                $resultItem = $db->query(knje390Query::getSubQuery1WelfareRecordItemcdList($model, $rowlist["RECORD_DIV"], $rowlist["RECORD_NO"], $rowlist["RECORD_SEQ"]));
                while ($rowItem = $resultItem->fetchRow(DB_FETCHMODE_ASSOC)) {
                    for ($i = 1; $i <= 6; $i++) {
                        if ($rowItem["ITEMCD".$i]) {
                            $itemName = $itemNameArray[1][$rowItem["ITEMCD".$i]];
                            if (strlen($itemName) && !in_array($itemName, $itemcdArray)) {
                                $itemcdArray[] = $itemName;
                            }
                        }
                    }
                }
                $resultItem->free();
                $rowlist["CONTENTS_NAME"] = ($itemcdArray[0]) ? implode("<BR>", $itemcdArray) : "";

                $rowlist["CONTENTS_NAIYOU"] = $rowlist["WELFARE_REMARK"];
                $divCount2++;
            } else if ($rowlist["RECORD_NO"] === '3') {
                $rowlist["RECORD_DIV_NAME"] = $rowlist["RECORD_DIV_NAME"].'(日常生活用具)'.$divCount3;

                $itemcdArray = array();
                $resultItem = $db->query(knje390Query::getSubQuery1WelfareRecordItemcdList($model, $rowlist["RECORD_DIV"], $rowlist["RECORD_NO"], $rowlist["RECORD_SEQ"]));
                while ($rowItem = $resultItem->fetchRow(DB_FETCHMODE_ASSOC)) {
                    for ($i = 1; $i <= 3; $i++) {
                        if ($rowItem["ITEMCD".$i]) {
                            $itemName = $itemNameArray[2][$rowItem["ITEMCD".$i]];
                            if (strlen($itemName) && !in_array($itemName, $itemcdArray)) {
                                $itemcdArray[] = $itemName;
                            }
                        }
                    }
                }
                $resultItem->free();
                $rowlist["CONTENTS_NAME"] = ($itemcdArray[0]) ? implode("<BR>", $itemcdArray) : "";

                $rowlist["CONTENTS_NAIYOU"] = $rowlist["WELFARE_REMARK"];
                $divCount3++;
            }
        } else if ($rowlist["RECORD_DIV"] === '2') {
            $rowlist["RECORD_DIV_NAME"] = '相談支援事業所'.$divCount4;
            $centerName = $db->getOne(knje390Query::getWelfareCentercdName($rowlist["CENTERCD"]));
            $rowlist["CONTENTS_NAME"] = $centerName;
            $rowlist["CONTENTS_NAIYOU"] = $rowlist["WELFARE_REMARK"];
            $divCount4++;
        } else if ($rowlist["RECORD_DIV"] === '3') {
            $rowlist["RECORD_DIV_NAME"] = 'サービス内容'.$divCount5;
            $centerName = $rowlist["SERVICE_CENTER_TEXT"];
            $rowlist["CONTENTS_NAME"] = $centerName;

            $resultItem = $db->query(knje390Query::getSubQuery1WelfareRecordItemcdList($model, $rowlist["RECORD_DIV"], $rowlist["RECORD_NO"], $rowlist["RECORD_SEQ"]));
            $itemcdArray = array();
            $itemcdArray1 = array();
            $itemcdArray2 = array();
            while ($rowItem = $resultItem->fetchRow(DB_FETCHMODE_ASSOC)) {
                for ($i = 1; $i <= 3; $i++) {
                    if ($rowItem["ITEMDIV"] == "1") {
                        if ($rowItem["ITEMCD".$i]) {
                            $itemName = $e039ItemNameArray["1"][$rowItem["ITEMCD".$i]];
                            if (strlen($itemName) && !in_array($itemName, $itemcdArray1)) {
                                $itemcdArray1[] = $itemName;
                                $itemcdArray[] = $itemName;
                            }
                        }
                    } else if ($rowItem["ITEMDIV"] == "2") {
                        if ($rowItem["ITEMCD".$i]) {
                            $itemName = $e039ItemNameArray["2"][$rowItem["ITEMCD".$i]];
                            if (strlen($itemName) && !in_array($itemName, $itemcdArray2)) {
                                $itemcdArray2[] = $itemName;
                                $itemcdArray[] = $itemName;
                            }
                        }
                    }
                }
            }
 
            $rowlist["CONTENTS_NAIYOU"] = ($itemcdArray[0]) ? implode("<BR>", $itemcdArray) : "";
            $divCount5++;
        }
        
        $arg["data2"][] = $rowlist;
        $retCnt++;
    }
    $result->free();
    return $retCnt;
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space="")
{
    $opt = array();
    if($space) $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) $value_flg = true;
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//コンボ作成
function makeCmbReturn(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space="")
{
    $opt = array();
    if($space) $opt[] = array('label' => "", 'value' => "");
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) $value_flg = true;
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //福祉サービスマスタ参照
    // Add by PP for PC-Talker 2020-02-03 start
    $extra = "id=\"btn_service_search\" onclick=\"current_cursor('btn_service_search'); loadwindow('" .REQUESTROOT."/E/KNJE390/knje390index.php?cmd=welfare_useservice_search&SCHREGNO=".$model->schregno."', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 700, 600)\"";
    $arg["button"]["btn_service_search"] = knjCreateBtn($objForm, "btn_service_search", "福祉サービス施設", $extra.$disabled);
    // Add by PP for PC-Talker 2020-02-20 end

    //追加ボタン
    // Add by PP for PC-Talker 2020-02-03 start
    $extra = "id=\"btn_insert\" onclick=\"current_cursor('btn_insert'); return btn_submit('welfare1_insert');\" aria-label=\"追加\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);
    // Add by PP for PC-Talker 2020-02-20 end
    //更新ボタン
    // Add by PP for PC-Talker 2020-02-03 start
    $extra = "id=\"btn_update\" onclick=\"current_cursor('btn_update'); return btn_submit('welfare1_update');\" aria-label=\"更新\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    // Add by PP for PC-Talker 2020-02-20 end
    //削除ボタン
    // Add by PP for PC-Talker 2020-02-03 start
    $extra = "id=\"btn_delete\" onclick=\"current_cursor('btn_delete'); return btn_submit('welfare1_delete');\" aria-label=\"削除\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    // Add by PP for PC-Talker 2020-02-20 end
    //戻るボタン
    // Add by PP for PC-Talker 2020-02-03 start
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", "onclick=\"return btn_submit('subform1A');\" aria-label=\"戻る\"");
    // Add by PP for PC-Talker 2020-02-20 end
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
}
?>

