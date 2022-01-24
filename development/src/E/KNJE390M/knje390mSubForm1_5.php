<?php

require_once('for_php7.php');
class knje390mSubForm1_5
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform1_5", "POST", "knje390mindex.php", "", "subform1_5");
        
        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = VIEW::setIframeJs();

        //DB接続
        $db = Query::dbCheckOut();
        
        //生徒情報
        $info = $db->getRow(knje390mQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];


        //補装具項目名取得
        $prostheticsNameArray = array();
        $query = knje390mQuery::getItemcdName("1");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $prostheticsNameArray[$row["NAMECD"]] = $row["NAME"];
        }
        $result->free();

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
        if ($model->cmd == "subform1_welfare_set") {
            if (isset($model->schregno) && !isset($model->warning) && $model->cmd != "subform1_welfare2") {
                $Row = $db->getRow(knje390mQuery::getSubQuery1WelfareGetData($model), DB_FETCHMODE_ASSOC);

                if ($Row["RECORD_DIV"] === '1') {
                    if ($Row["RECORD_NO"] == '2') {
                        $query = knje390mQuery::getSubQuery1WelfareGetItemcdData($model);
                        $result = $db->query($query);
                        while ($rowItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                            for ($i = 1; $i <= 6; $i++) {
                                $Row["EQUIP2_ITEMCD".$i."_".$rowItem["ITEMDIV"]] = $rowItem["ITEMCD".$i];
                            }
                        }
                        $Row["EQUIP2_SUPPLY_DATE"] = $Row["SUPPLY_DATE"];
                        $Row["EQUIP2_SERVICE_CENTER_TEXT"] = $Row["SERVICE_CENTER_TEXT"];
                    } else {
                        $query = knje390mQuery::getSubQuery1WelfareGetItemcdData($model);
                        $result = $db->query($query);
                        while ($rowItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                            for ($i = 1; $i <= 3; $i++) {
                                $Row["EQUIP3_ITEMCD".$i."_".$rowItem["ITEMDIV"]] = $rowItem["ITEMCD".$i];
                            }
                        }
                        $Row["EQUIP3_WELFARE_REMARK"] = $Row["WELFARE_REMARK"];
                    }
                } elseif ($Row["RECORD_DIV"] === '2') {
                    $Row["ADVICE_SERVICE_CENTER_TEXT"] = $Row["SERVICE_CENTER_TEXT"];
                    $Row["ADVICE_SERVICE_CHARGE"] = $Row["SERVICE_CHARGE"];
                } elseif ($Row["RECORD_DIV"] === '3') {
                    $Row["WELFARE_SERVICE_CENTER_TEXT"] = $Row["SERVICE_CENTER_TEXT"];
                    $query = knje390mQuery::getSubQuery1WelfareGetItemcdData($model);
                    $result = $db->query($query);
                    while ($rowItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        for ($i = 1; $i <= 3; $i++) {
                            $Row["WELFARE_ITEMCD".$i."_".$rowItem["ITEMDIV"]] = $rowItem["ITEMCD".$i];
                        }
                    }
                    $Row["WELFARE_ITEMCD"] = $Row["ITEMCD"];
                    $Row["WELFARE_ITEMCD2"] = $Row["ITEMCD2"];
                    $Row["WELFARE_WELFARE_REMARK"] = $Row["WELFARE_REMARK"];
                }
            } else {
                $Row =& $model->field;
            }
        } else {
            $Row =& $model->field;
        }

        knjCreateHidden($objForm, "RECORD_DIV", $Row["RECORD_DIV"]);
        knjCreateHidden($objForm, "RECORD_NO", $Row["RECORD_NO"]);

        //===========================
        //補装具等の給付（補装具）
        //===========================
        $query = knje390mQuery::getItemdivComboName("1");
        $result = $db->query($query);
        while ($rowItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $rowItem["EQUIP2_ITEMDIV_NAME"] = $rowItem["LABEL"];
            $itemcdList = $space = "";
            for ($i = 1; $i <= 6; $i++) {
                $query = knje390mQuery::getItemcdComboName("1", $rowItem["VALUE"]);
                $name = "EQUIP2_ITEMCD".$i."_".$rowItem["VALUE"];
                $itemcdList .= $space.makeCmbReturn($objForm, $arg, $db, $query, $name, $Row[$name], "", 1, 1);
                $space = ' ';
            }
            $rowItem["EQUIP2_ITEMCD_SHOW"] = $itemcdList;
            $arg["item12"][] = $rowItem;
        }
        //申請年月
        $extra = "";
        $dispwk = $Row["EQUIP2_SUPPLY_DATE"];
        $cutstr = array();
        $cutstr = explode("-", $dispwk);
        if (get_count($cutstr) > 2) {
            $dispwk = $cutstr[0]."/".$cutstr[1];
        }
        $arg["data"]["EQUIP2_SUPPLY_DATE"] = knjCreateTextBox($objForm, $dispwk, "EQUIP2_SUPPLY_DATE", 7, 7, $extra);
        $arg["data"]["EQUIP2_SUPPLY_DATE_SIZE"] = '<font size="2" color="red">(半角7文字まで)</font>';
        $arg["data"]["EQUIP2_SUPPLY_DATE_SAMPLE"] = '<font size="2" color="red">（例）2020/07</font>';
        //製薬会社
        $extra = "";
        $arg["data"]["EQUIP2_SERVICE_CENTER_TEXT"] = getTextOrArea($objForm, "EQUIP2_SERVICE_CENTER_TEXT", 10, 1, $Row["EQUIP2_SERVICE_CENTER_TEXT"]);
        $arg["data"]["EQUIP2_SERVICE_CENTER_TEXT_SIZE"] = '<font size="2" color="red">(全角10文字まで)</font>';

        //===========================
        //補装具等の給付（日常生活用具）
        //===========================
        $query = knje390mQuery::getItemdivComboName("2");
        $result = $db->query($query);
        while ($rowItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $rowItem["EQUIP3_ITEMDIV_NAME"] = $rowItem["LABEL"];
            $itemcdList = $space = "";
            for ($i = 1; $i <= 3; $i++) {
                $query = knje390mQuery::getItemcdComboName("2", $rowItem["VALUE"]);
                $name = "EQUIP3_ITEMCD".$i."_".$rowItem["VALUE"];
                $itemcdList .= $space.makeCmbReturn($objForm, $arg, $db, $query, $name, $Row[$name], "", 1, 1);
                $space = ' ';
            }
            $rowItem["EQUIP3_ITEMCD_SHOW"] = $itemcdList;
            $arg["item13"][] = $rowItem;
        }

        //備考
        $extra = "";
        $arg["data"]["EQUIP3_WELFARE_REMARK"] = getTextOrArea($objForm, "EQUIP3_WELFARE_REMARK", 30, 1, $Row["EQUIP3_WELFARE_REMARK"]);
        $arg["data"]["EQUIP3_WELFARE_REMARK_SIZE"] = '<font size="2" color="red">(全角30文字まで)</font>';

        //===========================
        //相談支援事業所
        //===========================
        //相談支援事業所
        $extra = "";
        $arg["data"]["ADVICE_SERVICE_CENTER_TEXT"] = getTextOrArea($objForm, "ADVICE_SERVICE_CENTER_TEXT", 50, 1, $Row["ADVICE_SERVICE_CENTER_TEXT"]);
        $arg["data"]["ADVICE_SERVICE_CENTER_TEXT_SIZE"] = '<font size="2" color="red">(全角50文字まで)</font>';
        //担当者
        $arg["data"]["ADVICE_SERVICE_CHARGE"] = getTextOrArea($objForm, "ADVICE_SERVICE_CHARGE", 4, 1, $Row["ADVICE_SERVICE_CHARGE"]);
        $arg["data"]["ADVICE_SERVICE_CHARGE_SIZE"] = '<font size="2" color="red">(全角4文字まで)</font>';

        //===========================
        //事業所サービスの利用
        //===========================
        //福祉サービス施設
        $extra = "style=\"overflow:auto;\"";
        // $arg["data"]["WELFARE_SERVICE_CENTER_TEXT"] = knjCreateTextArea($objForm, "WELFARE_SERVICE_CENTER_TEXT", 2, 101, "soft", $extra, $Row["WELFARE_SERVICE_CENTER_TEXT"]);
        $arg["data"]["WELFARE_SERVICE_CENTER_TEXT"] = getTextOrArea($objForm, "WELFARE_SERVICE_CENTER_TEXT", 50, 2, $Row["WELFARE_SERVICE_CENTER_TEXT"]);
        $arg["data"]["WELFARE_SERVICE_CENTER_TEXT_SIZE"] = '<font size="2" color="red">(全角50文字X2行まで)</font>';
        //サービス内容
        $query = knje390mQuery::getItemdivComboName("3");
        $result = $db->query($query);
        while ($rowItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $rowItem["WELFARE_ITEMDIV_NAME"] = $rowItem["LABEL"];
            $itemcdList = $space = "";
            for ($i = 1; $i <= 3; $i++) {
                $query = knje390mQuery::getItemcdComboName("3", $rowItem["VALUE"]);
                $name = "WELFARE_ITEMCD".$i."_".$rowItem["VALUE"];
                $itemcdList .= $space.makeCmbReturn($objForm, $arg, $db, $query, $name, $Row[$name], "", 1, 1);
                $space = ' ';
            }
            $rowItem["WELFARE_ITEMCD_SHOW"] = $itemcdList;
            $arg["item3"][] = $rowItem;
        }
        $result->free();
        //支給量
        $arg["data"]["WELFARE_ITEMCD"] = knjCreateTextBox($objForm, $Row["WELFARE_ITEMCD"], "WELFARE_ITEMCD", 2, 2, $extra);
        $arg["data"]["WELFARE_ITEMCD_SIZE"] = '<font size="2" color="red">(半角数字2文字まで)</font>';
        //1:月/日、2:時間/週
        $opt = array(1, 2);
        $Row["WELFARE_ITEMCD2"] = ($Row["WELFARE_ITEMCD2"] == "") ? "1" : $Row["WELFARE_ITEMCD2"];
        $extra = array("id=\"WELFARE_ITEMCD21\"", "id=\"WELFARE_ITEMCD22\"");
        $radioArray = knjCreateRadio($objForm, "WELFARE_ITEMCD2", $Row["WELFARE_ITEMCD2"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }
        //事業者(頻度)
        $arg["data"]["WELFARE_WELFARE_REMARK"] = getTextOrArea($objForm, "WELFARE_WELFARE_REMARK", 30, 1, $Row["WELFARE_WELFARE_REMARK"]);
        $arg["data"]["WELFARE_WELFARE_REMARK_SIZE"] = '<font size="2" color="red">(全角30文字まで)</font>';

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje390mSubForm1_5.html", $arg);
    }
}

//履歴一覧
function makeList(&$arg, $db, $model)
{

    //項目名取得
    $itemNameArray = array();
    for ($flg = 1; $flg <= 3; $flg++) {
        $query = knje390mQuery::getItemcdName("{$flg}");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $itemNameArray[$flg][$row["NAMECD"]] = $row["NAME"];
        }
        $result->free();
    }
    $e039ItemNameArray = array();
    $query = knje390mQuery::getItemcdComboName("3", "1");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $e039ItemNameArray["1"][$row["VALUE"]] = $row["NAME1"];
    }
    $result->free();
    $query = knje390mQuery::getItemcdComboName("3", "2");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $e039ItemNameArray["2"][$row["VALUE"]] = $row["NAME1"];
    }
    $result->free();

    $retCnt = 0;
    $query = knje390mQuery::getSubQuery1WelfareRecordList($model, "", "");
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
                $rowlist["RECORD_DIV_NAME"] = View::alink(
                    "knje390mindex.php",
                    $rowlist["RECORD_DIV_NAME"],
                    "",
                    array("cmd"         => "subform1_welfare_set"
                                                        , "YEAR"        => $rowlist["PATTERN_CD"]
                                                        , "SCHREGNO"    => $rowlist["SCHREGNO"]
                                                        , "RECORD_DIV"  => $rowlist["RECORD_DIV"]
                                                        , "RECORD_NO"   => $rowlist["RECORD_NO"]
                                                        , "RECORD_SEQ"  => $rowlist["RECORD_SEQ"]
                                                    )
                );
                $rowlist["CONTENTS_NAME"] = 'サービス';
                $rowlist["CONTENTS_NAIYOU"] = $rowlist["SERVICE_NAME"];
                $divCount1++;
                $arg["data11"][] = $rowlist;
            } elseif ($rowlist["RECORD_NO"] === '2') {
                $rowlist["RECORD_DIV_NAME"] = $rowlist["RECORD_DIV_NAME"].'(補装具)'.$divCount2;
                $rowlist["RECORD_DIV_NAME"] = View::alink(
                    "knje390mindex.php",
                    $rowlist["RECORD_DIV_NAME"],
                    "",
                    array("cmd"         => "subform1_welfare_set"
                                                        , "YEAR"        => $rowlist["PATTERN_CD"]
                                                        , "SCHREGNO"    => $rowlist["SCHREGNO"]
                                                        , "RECORD_DIV"  => $rowlist["RECORD_DIV"]
                                                        , "RECORD_NO"   => $rowlist["RECORD_NO"]
                                                        , "RECORD_SEQ"  => $rowlist["RECORD_SEQ"]
                                                    )
                );

                $itemcdArray = array();
                $resultItem = $db->query(knje390mQuery::getSubQuery1WelfareRecordItemcdList($model, $rowlist["RECORD_DIV"], $rowlist["RECORD_NO"], $rowlist["RECORD_SEQ"]));
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
                $temp = explode("-", $rowlist["SUPPLY_DATE"]);
                $supply = (get_count($temp) >= 2) ? $temp[0]."/".$temp[1] : $rowlist["SUPPLY_DATE"];
                $rowlist["SUPPLY_DATE"] = $supply;
                $rowlist["CONTENTS"] = $rowlist["SERVICE_CENTER_TEXT"];

                $divCount2++;
                $arg["data12"][] = $rowlist;
            } elseif ($rowlist["RECORD_NO"] === '3') {
                $rowlist["RECORD_DIV_NAME"] = $rowlist["RECORD_DIV_NAME"].'(日常生活用具)'.$divCount3;
                $rowlist["RECORD_DIV_NAME"] = View::alink(
                    "knje390mindex.php",
                    $rowlist["RECORD_DIV_NAME"],
                    "",
                    array("cmd"         => "subform1_welfare_set"
                                                        , "YEAR"        => $rowlist["PATTERN_CD"]
                                                        , "SCHREGNO"    => $rowlist["SCHREGNO"]
                                                        , "RECORD_DIV"  => $rowlist["RECORD_DIV"]
                                                        , "RECORD_NO"   => $rowlist["RECORD_NO"]
                                                        , "RECORD_SEQ"  => $rowlist["RECORD_SEQ"]
                                                    )
                );

                $itemcdArray = array();
                $resultItem = $db->query(knje390mQuery::getSubQuery1WelfareRecordItemcdList($model, $rowlist["RECORD_DIV"], $rowlist["RECORD_NO"], $rowlist["RECORD_SEQ"]));
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
                $arg["data13"][] = $rowlist;
            }
        } elseif ($rowlist["RECORD_DIV"] === '2') {
            $rowlist["RECORD_DIV_NAME"] = '相談支援事業所'.$divCount4;
            $rowlist["RECORD_DIV_NAME"] = View::alink(
                "knje390mindex.php",
                $rowlist["RECORD_DIV_NAME"],
                "",
                array("cmd"         => "subform1_welfare_set"
                                                    , "YEAR"        => $rowlist["PATTERN_CD"]
                                                    , "SCHREGNO"    => $rowlist["SCHREGNO"]
                                                    , "RECORD_DIV"  => $rowlist["RECORD_DIV"]
                                                    , "RECORD_NO"   => $rowlist["RECORD_NO"]
                                                    , "RECORD_SEQ"  => $rowlist["RECORD_SEQ"]
                                                )
            );

            // $centerName = $db->getOne(knje390mQuery::getWelfareCentercdName($rowlist["CENTERCD"]));
            $rowlist["CONTENTS_NAME"] = $rowlist["SERVICE_CENTER_TEXT"];
            $rowlist["CONTENTS_NAIYOU"] = $rowlist["SERVICE_CHARGE"];
            $divCount4++;
            $arg["data2"][] = $rowlist;
        } elseif ($rowlist["RECORD_DIV"] === '3') {
            $rowlist["RECORD_DIV_NAME"] = 'サービス内容'.$divCount5;
            $rowlist["RECORD_DIV_NAME"] = View::alink(
                "knje390mindex.php",
                $rowlist["RECORD_DIV_NAME"],
                "",
                array("cmd"         => "subform1_welfare_set"
                                                    , "YEAR"        => $rowlist["PATTERN_CD"]
                                                    , "SCHREGNO"    => $rowlist["SCHREGNO"]
                                                    , "RECORD_DIV"  => $rowlist["RECORD_DIV"]
                                                    , "RECORD_NO"   => $rowlist["RECORD_NO"]
                                                    , "RECORD_SEQ"  => $rowlist["RECORD_SEQ"]
                                                )
            );

            $rowlist["CONTENTS_NAME"] = $rowlist["SERVICE_CENTER_TEXT"];

            $resultItem = $db->query(knje390mQuery::getSubQuery1WelfareRecordItemcdList($model, $rowlist["RECORD_DIV"], $rowlist["RECORD_NO"], $rowlist["RECORD_SEQ"]));
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
                    } elseif ($rowItem["ITEMDIV"] == "2") {
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
 
            $contents = $rowlist["ITEMCD"];
            if ($rowlist["ITEMCD2"] == '1') {
                $contents .= '(日/月)';
            } elseif ($rowlist["ITEMCD2"] == '2') {
                $contents .= '(時間/週)';
            }
            $rowlist["CONTENTS"] = $contents;
            $rowlist["CONTENTS2"] = $rowlist["WELFARE_REMARK"];

            $divCount5++;
            $arg["data3"][] = $rowlist;
        }

        $retCnt++;
    }
    $result->free();
    return $retCnt;
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space = "")
{
    $opt = array();
    if ($space) {
        $opt[] = array('label' => "", 'value' => "");
    }
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) {
            $value_flg = true;
        }
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//コンボ作成
function makeCmbReturn(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space = "")
{
    $opt = array();
    if ($space) {
        $opt[] = array('label' => "", 'value' => "");
    }
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) {
            $value_flg = true;
        }
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg)
{
    //相談支援事業所マスタ参照
    $extra = "onclick=\"loadwindow('" .REQUESTROOT."/E/KNJE390M/knje390mindex.php?cmd=welfare_advice_center_search&SCHREGNO=".$model->schregno."', function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 700, 600)\"";
    $arg["button"]["btn_advice_center_search"] = knjCreateBtn($objForm, "btn_advice_center_search", "相談支援事業所参照", $extra.$disabled);

    //福祉サービスマスタ参照
    $extra = "onclick=\"loadwindow('" .REQUESTROOT."/E/KNJE390M/knje390mindex.php?cmd=welfare_useservice_search&SCHREGNO=".$model->schregno."', function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 700, 600)\"";
    $arg["button"]["btn_service_search"] = knjCreateBtn($objForm, "btn_service_search", "福祉サービス施設参照", $extra.$disabled);

    //===========================
    //補装具等の給付（補装具）
    //===========================
    //追加ボタン
    $extra = "onclick=\"return submitCheck('welfare1_insert', '1', '2');\"";
    $arg["button"]["btn_insertEquip2"] = knjCreateBtn($objForm, "btn_insertEquip2", "追 加", $extra);
    //更新ボタン
    $extra = "onclick=\"return submitCheck('welfare1_update', '1', '2');\"";
    $arg["button"]["btn_updateEquip2"] = knjCreateBtn($objForm, "btn_updateEquip2", "更 新", $extra);
    //削除ボタン
    $extra = "onclick=\"return submitCheck('welfare1_delete', '1', '2');\"";
    $arg["button"]["btn_deleteEquip2"] = knjCreateBtn($objForm, "btn_deleteEquip2", "削 除", $extra);

    //===========================
    //補装具等の給付（日常生活用具）
    //===========================
    //追加ボタン
    $extra = "onclick=\"return submitCheck('welfare1_insert', '1', '3');\"";
    $arg["button"]["btn_insertEquip3"] = knjCreateBtn($objForm, "btn_insertEquip3", "追 加", $extra);
    //更新ボタン
    $extra = "onclick=\"return submitCheck('welfare1_update', '1', '3');\"";
    $arg["button"]["btn_updateEquip3"] = knjCreateBtn($objForm, "btn_updateEquip3", "更 新", $extra);
    //削除ボタン
    $extra = "onclick=\"return submitCheck('welfare1_delete', '1', '3');\"";
    $arg["button"]["btn_deleteEquip3"] = knjCreateBtn($objForm, "btn_deleteEquip3", "削 除", $extra);

    //===========================
    //相談支援事業所
    //===========================
    //追加ボタン
    $extra = "onclick=\"return submitCheck('welfare1_insert', '2', '1');\"";
    $arg["button"]["btn_insertAdvice"] = knjCreateBtn($objForm, "btn_insertAdvice", "追 加", $extra);
    //更新ボタン
    $extra = "onclick=\"return submitCheck('welfare1_update', '2', '1');\"";
    $arg["button"]["btn_updateAdvice"] = knjCreateBtn($objForm, "btn_updateAdvice", "更 新", $extra);
    //削除ボタン
    $extra = "onclick=\"return submitCheck('welfare1_delete', '2', '1');\"";
    $arg["button"]["btn_deleteAdvice"] = knjCreateBtn($objForm, "btn_deleteAdvice", "削 除", $extra);

    //===========================
    //事業所サービスの利用
    //===========================
    //追加ボタン
    $extra = "onclick=\"return submitCheck('welfare1_insert', '3', '1');\"";
    $arg["button"]["btn_insertWelfare"] = knjCreateBtn($objForm, "btn_insertWelfare", "追 加", $extra);
    //更新ボタン
    $extra = "onclick=\"return submitCheck('welfare1_update', '3', '1');\"";
    $arg["button"]["btn_updateWelfare"] = knjCreateBtn($objForm, "btn_updateWelfare", "更 新", $extra);
    //削除ボタン
    $extra = "onclick=\"return submitCheck('welfare1_delete', '3', '1');\"";
    $arg["button"]["btn_deleteWelfare"] = knjCreateBtn($objForm, "btn_deleteWelfare", "削 除", $extra);

    //戻るボタン
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", "onclick=\"return btn_submit('subform1A');\"");
}

//テキストボックス or テキストエリア作成
function getTextOrArea(&$objForm, $name, $moji, $gyou, $val)
{
    $retArg = "";
    if ($gyou > 1) {
        //textArea
        $extra = "style=\"overflow-y:scroll\" id=\"".$name."\"";
        $retArg = knjCreateTextArea($objForm, $name, $gyou, ($moji * 2), "soft", $extra, $val);
    } else {
        //textbox
        $extra = "onkeypress=\"btn_keypress();\" id=\"".$name."\"";
        $retArg = knjCreateTextBox($objForm, $val, $name, ($moji * 2), $moji, $extra);
    }
    return $retArg;
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
}
?>

