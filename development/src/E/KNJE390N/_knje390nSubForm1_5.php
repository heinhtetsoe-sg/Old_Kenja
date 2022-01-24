<?php

require_once('for_php7.php');

class knje390nSubForm1_5
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1_5", "POST", "knje390nindex.php", "", "subform1_5");
        
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
        $info = $db->getRow(knje390nQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"].$setHyoujiDate;

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
            $model->field["SERVICE_CENTER_TEXT"] = "";
            $model->field["SUPPLY_DATE"] = "";

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
                $Row = $db->getRow(knje390nQuery::getSubQuery1WelfareGetData($model), DB_FETCHMODE_ASSOC);
                //補装具等の給付
                if ($Row["RECORD_DIV"] === '1' || $Row["RECORD_DIV"] === '3') {
                    $query = knje390nQuery::getSubQuery1WelfareGetItemcdData($model);
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
        $extra = "onchange=\"return btn_submit('subform1_welfare')\"";
        $query = knje390nQuery::getWelfareRecordDiv();
        makeCmb($objForm, $arg, $db, $query, "RECORD_DIV", $Row["RECORD_DIV"], $extra, 1, 1);
        if ($Row["RECORD_DIV"] == "3") {
            $arg["SERVICE_NAIYOU"] = "1";
        } else {
            $arg["NOT_SERVICE_NAIYOU"] = "1";
        }

        //補装具等の給付の①サービス、②補装具、③日常生活用具の切換
        $opt = array(1, 2, 3);
        $Row["RECORD_NO"] = ($Row["RECORD_NO"] == "") ? "2" : $Row["RECORD_NO"];
        $extra = array("id=\"RECORD_NO1\" onclick=\"return btn_submit('subform1_welfare')\"", "id=\"RECORD_NO2\" onclick=\"return btn_submit('subform1_welfare')\"", "id=\"RECORD_NO3\" onclick=\"return btn_submit('subform1_welfare')\"");
        $radioArray = knjCreateRadio($objForm, "RECORD_NO", $Row["RECORD_NO"], $extra, $opt, get_count($opt));
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

                $query = knje390nQuery::getItemdivComboName("1");
                $result = $db->query($query);
                while ($rowItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $rowItem["ITEMDIV_NAME"] = $rowItem["LABEL"];
                    $itemcdList = $space = "";
                    for ($i = 1; $i <= 6; $i++) {
                        $query = knje390nQuery::getItemcdComboName("1", $rowItem["VALUE"]);
                        $name = "ITEMCD".$i."_".$rowItem["VALUE"];
                        $itemcdList .= $space.makeCmbReturn($objForm, $arg, $db, $query, $name, $Row[$name], "", 1, 1);
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

                $query = knje390nQuery::getItemdivComboName("2");
                $result = $db->query($query);
                while ($rowItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $rowItem["ITEMDIV_NAME"] = $rowItem["LABEL"];
                    $itemcdList = $space = "";
                    for ($i = 1; $i <= 3; $i++) {
                        $query = knje390nQuery::getItemcdComboName("2", $rowItem["VALUE"]);
                        $name = "ITEMCD".$i."_".$rowItem["VALUE"];
                        $itemcdList .= $space.makeCmbReturn($objForm, $arg, $db, $query, $name, $Row[$name], "", 1, 1);
                        $space = ' ';
                    }
                    $rowItem["ITEMCD_SHOW"] = $itemcdList;
                    $arg["item"][] = $rowItem;
                }
                $result->free();
            }
            //備考
            $extra = "";
            $dispwk = $Row["SUPPLY_DATE"];
            $cutstr = array();
            $cutstr = explode("-", $dispwk);
            if (get_count($cutstr) > 2) $dispwk = $cutstr[0]."/".$cutstr[1];
            $arg["data"]["SUPPLY_DATE"] = knjCreateTextBox($objForm, $dispwk, "SUPPLY_DATE", 7, 7, $extra);
            $arg["data"]["SUPPLY_DATE_SIZE"] = '<font size="1" color="red">(半角7文字まで)</font>';
            $arg["data"]["SUPPLY_DATE_NAME"] = '申請年月';
            $extra = "";
            $arg["data"]["SERVICE_CENTER_TEXT"] = knjCreateTextBox($objForm, $Row["SERVICE_CENTER_TEXT"], "SERVICE_CENTER_TEXT", 10, 20, $extra);
            $arg["data"]["SERVICE_CENTER_TEXT_SIZE"] = '<font size="1" color="red">(全角10文字まで)</font>';
            $arg["data"]["SERVICE_CENTER_TEXT_NAME"] = '製造会社';
        } else if ($Row["RECORD_DIV"] === '2' || $Row["RECORD_DIV"] === '3') {
            $arg["RECORD_DIV23"] = "1";
            if ($Row["RECORD_DIV"] === '2') {
                $arg["RECORD_DIV2"] = "1";
                $arg["data"]["WELFARE_REMARK_NAME"] = '備考';
                //圏域
                $extra = "onChange=\"return btn_submit('subform1_welfare2')\"";
                $query = knje390nQuery::getNameMst("E040");
                makeCmb($objForm, $arg, $db, $query, "AREACD", $Row["AREACD"], $extra, 1, 1);
                //相談・支援事業所
                $query = knje390nQuery::getWelfareCentercdComboName($Row["AREACD"]);
                makeCmb($objForm, $arg, $db, $query, "CENTERCD", $Row["CENTERCD"], "", 1, 1);
                //担当者
                $arg["data"]["SERVICE_CHARGE"] = knjCreateTextBox($objForm, $Row["SERVICE_CHARGE"], "SERVICE_CHARGE", 8, 8, $extra);
                $arg["data"]["SERVICE_CHARGE_SIZE"] = '<font size="1" color="red">(全角4文字まで)</font>';
                //通院の状況・特記事項
                $extra = "style=\"height:35px; overflow:auto;\"";
                $arg["data"]["WELFARE_REMARK"] = knjCreateTextArea($objForm, "WELFARE_REMARK", 2, 41, "soft", $extra, $Row["WELFARE_REMARK"]);
                $arg["data"]["WELFARE_REMARK_SIZE"] = '<font size="1" color="red">(全角20文字2行まで)</font>';
            } else {
                $arg["RECORD_DIV3"] = "1";
                //サービス利用事業所
                $extra = "style=\"overflow:auto;\"";
                $arg["data"]["SERVICE_CENTER_TEXT"] = knjCreateTextArea($objForm, "SERVICE_CENTER_TEXT", 2, 101, "soft", $extra, $Row["SERVICE_CENTER_TEXT"]);
                $arg["data"]["SERVICE_CENTER_TEXT_SIZE"] = '<font size="1" color="red">(全角50文字2行まで)</font>';
                //サービス内容
                $arg["data"]["COMBO_NAME"] = 'サービス内容';
                $query = knje390nQuery::getItemdivComboName("3");
                $result = $db->query($query);
                while ($rowItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $rowItem["ITEMDIV_NAME"] = $rowItem["LABEL"];
                    $itemcdList = $space = "";
                    for ($i = 1; $i <= 3; $i++) {
                        $query = knje390nQuery::getItemcdComboName("3", $rowItem["VALUE"]);
                        $name = "ITEMCD".$i."_".$rowItem["VALUE"];
                        $itemcdList .= $space.makeCmbReturn($objForm, $arg, $db, $query, $name, $Row[$name], "", 1, 1);
                        $space = ' ';
                    }
                    $rowItem["ITEMCD_SHOW"] = $itemcdList;
                    $arg["item"][] = $rowItem;
                }
                $result->free();
                //支給量
                $arg["data"]["ITEMCD"] = knjCreateTextBox($objForm, $Row["ITEMCD"], "ITEMCD", 2, 2, $extra);
                $arg["data"]["ITEMCD_SIZE"] = '<font size="1" color="red">(半角7文字まで)</font>';
                //1:月/日、2:時間/週
                $opt = array(1, 2);
                $Row["ITEMCD2"] = ($Row["ITEMCD2"] == "") ? "1" : $Row["ITEMCD2"];
                $extra = array("id=\"ITEMCD21\"", "id=\"ITEMCD22\"");
                $radioArray = knjCreateRadio($objForm, "ITEMCD2", $Row["ITEMCD2"], $extra, $opt, get_count($opt));
                foreach($radioArray as $key => $val) $arg["data"][$key] = $val;
                //事業者(頻度)
                $arg["data"]["WELFARE_REMARK"] = knjCreateTextBox($objForm, $Row["WELFARE_REMARK"], "WELFARE_REMARK", 8, 8, $extra);
                $arg["data"]["WELFARE_REMARK_SIZE"] = '<font size="1" color="red">(全角30文字まで)</font>';
            }
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
        View::toHTML($model, "knje390nSubForm1_5.html", $arg); 
    }
}

//履歴一覧
function makeList(&$arg, $db, $model) {
    //項目名取得
    $itemNameArray = array();
    for ($flg = 1; $flg <= 3; $flg++) {
        $query = knje390nQuery::getItemcdName("{$flg}");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $itemNameArray[$flg][$row["NAMECD"]] = $row["NAME"];
        }
        $result->free();
    }
    $e039ItemNameArray = array();
    $query = knje390nQuery::getItemcdComboName("3", "1");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $e039ItemNameArray["1"][$row["VALUE"]] = $row["NAME1"];
    }
    $result->free();
    $query = knje390nQuery::getItemcdComboName("3", "2");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $e039ItemNameArray["2"][$row["VALUE"]] = $row["NAME1"];
    }
    $result->free();

    $retCnt = 0;
    $query = knje390nQuery::getSubQuery1WelfareRecordList($model);
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
                $resultItem = $db->query(knje390nQuery::getSubQuery1WelfareRecordItemcdList($model, $rowlist["RECORD_DIV"], $rowlist["RECORD_NO"], $rowlist["RECORD_SEQ"]));
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
                $resultItem = $db->query(knje390nQuery::getSubQuery1WelfareRecordItemcdList($model, $rowlist["RECORD_DIV"], $rowlist["RECORD_NO"], $rowlist["RECORD_SEQ"]));
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
            $centerName = $db->getOne(knje390nQuery::getWelfareCentercdName($rowlist["CENTERCD"]));
            $rowlist["CONTENTS_NAME"] = $centerName;
            $rowlist["CONTENTS_NAIYOU"] = $rowlist["WELFARE_REMARK"];
            $divCount4++;
        } else if ($rowlist["RECORD_DIV"] === '3') {
            $rowlist["RECORD_DIV_NAME"] = 'サービス内容'.$divCount5;
            $centerName = $rowlist["SERVICE_CENTER_TEXT"];
            $rowlist["CONTENTS_NAME"] = $centerName;

            $resultItem = $db->query(knje390nQuery::getSubQuery1WelfareRecordItemcdList($model, $rowlist["RECORD_DIV"], $rowlist["RECORD_NO"], $rowlist["RECORD_SEQ"]));
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
    $extra = "onclick=\"loadwindow('" .REQUESTROOT."/E/KNJE390N/knje390nindex.php?cmd=welfare_useservice_search&SCHREGNO=".$model->schregno."', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 700, 600)\"";
    $arg["button"]["btn_service_search"] = knjCreateBtn($objForm, "btn_service_search", "福祉サービス施設", $extra.$disabled);

    //追加ボタン
    $extra = "onclick=\"return btn_submit('welfare1_insert');\"";
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", $extra);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('welfare1_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //削除ボタン
    $extra = "onclick=\"return btn_submit('welfare1_delete');\"";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    //戻るボタン
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", "onclick=\"return btn_submit('subform1A');\"");
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
}
?>

