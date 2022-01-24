<?php

require_once('for_php7.php');

class knje390nForm1
{
    function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knje390nindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報取得
        $info = $db->getRow(knje390nQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];

        //生徒詳細情報取得
        $infoshousai = $db->getRow(knje390nQuery::getSchInfoShousai($model), DB_FETCHMODE_ASSOC);
        $arg["data"]["SCHREGNO"] = $infoshousai["SCHREGNO"];
        $arg["data"]["GAKUBU_NAME"] = $infoshousai["GAKUBU_NAME"];
        $arg["data"]["COURSE_MAJOR_NAME"] = $infoshousai["COURSE_MAJOR_NAME"];
        $arg["data"]["COURSECODENAME"] = $infoshousai["COURSECODENAME"];
        $arg["data"]["HR_NAME"] = $infoshousai["HR_NAME"];
        $arg["data"]["GHR_NAME"] = $infoshousai["GHR_NAME"];
        $arg["data"]["NAME_SHOW"] = $infoshousai["NAME_SHOW"];
        $arg["data"]["NAME_KANA"] = $infoshousai["NAME_KANA"];
        $arg["data"]["BIRTHDAY"] = $infoshousai["BIRTHDAY"];
        $arg["data"]["SEX_NAME"] = $infoshousai["SEX_NAME"];
        $arg["data"]["ZIPCD"] = '〒'.$infoshousai["ZIPCD"];
        $arg["data"]["ADDR1"] = $infoshousai["ADDR1"];
        $arg["data"]["ADDR2"] = $infoshousai["ADDR2"];
        $arg["data"]["TELNO"] = $infoshousai["TELNO"];
        $arg["data"]["EMERGENCYTELNO"] = $infoshousai["EMERGENCYTELNO"];
        $arg["data"]["EMERGENCYNAME"] = $infoshousai["EMERGENCYNAME"];
        $arg["data"]["EMERGENCYTELNO2"] = $infoshousai["EMERGENCYTELNO2"];
        $arg["data"]["EMERGENCYNAME2"] = $infoshousai["EMERGENCYNAME2"];
        $arg["data"]["EMERGENCYTELNO3"] = $infoshousai["EMERGENCYTELNO3"];
        $arg["data"]["EMERGENCYNAME3"] = $infoshousai["EMERGENCYNAME3"];
        
        //顔写真
        $arg["data"]["FACE_IMG"] = REQUESTROOT."/".$model->control_data["LargePhotoPath"]."/P".$infoshousai["SCHREGNO"].".".$model->control_data["Extension"];
        $arg["data"]["IMG_PATH"] = REQUESTROOT."/".$model->control_data["LargePhotoPath"]."/P".$infoshousai["SCHREGNO"].".".$model->control_data["Extension"];
        
        //高校生のみ D 移行支援計画を表示
        if ($infoshousai["SCHOOL_KIND"] === 'H') {
            $arg["D_BUTTON"] = '1';
        }

        //最初の画面に戻った時は年度、レコード日付をアンセット
        if ($model->cmd == "edit") {
            unset($model->main_year);
            unset($model->record_date);
            unset($model->type);
        }

        //データを取得
        $setval = array();
        $firstflg = true;   //初回フラグ
        $cnt = get_count($db->getcol(knje390nQuery::selectQuery($model)));
        if ($model->schregno && $cnt) {
            $dataflg = array();
            $dataflg = array("1" => "", "2" => "", "3" => "", "4" => "");
            $result = $db->query(knje390nQuery::selectQuery($model));
            //表示用フラグ(同一テーブルは複数表示されるため)
            $hyoujiCFlg = false;
            $hyoujiDFlg = false;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //履歴コンボ作成
                //B　
                if ($row["KINDCD"] === '1') {
                    $extra = "onchange=\"return btn_submit('edit_change')\"";
                    $query = knje390nQuery::getRecordDate($model, "B");
                    $row["RECORD_DATE_SET"] = makeCmb($objForm, $arg, $db, $query, "RECORD_DATE_B", $model->mainfield["RECORD_DATE_B"], $extra, 1, "");
                    $row["KINDNAME"] = View::alink("knje390nindex.php", $row["KINDNAME"], "target=_self tabindex=\"-1\"",
                                                    array("SCHREGNO"        => $row["SCHREGNO"],
                                                          "cmd"             => "subform".$row["KINDCD"]."A",
                                                          "RECORD_DATE"     => $model->mainfield["RECORD_DATE_B"],
                                                          "TYPE"            => "list"));
                    $setval = $row;
                    $arg["list"][] = $setval;
                //A　年度ごと
                } else if ($row["KINDCD"] === '2') {
                    $extra = "onchange=\"return btn_submit('edit_change')\"";
                    $query = knje390nQuery::getMainYear($model, "A");
                    $row["YEAR_SET"] = makeCmb($objForm, $arg, $db, $query, "YEAR_A", $model->mainfield["YEAR_A"], $extra, 1, "");
                    
                    $extra = "onchange=\"return btn_submit('edit_change')\"";
                    $query = knje390nQuery::getRecordDate($model, "A");
                    $row["RECORD_DATE_SET"] = makeCmb($objForm, $arg, $db, $query, "RECORD_DATE_A", $model->mainfield["RECORD_DATE_A"], $extra, 1, "");
                    
                    //新規ボタンを配置
                    $setData = array();
                    $setData = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    if ($setData["VALUE"] != "") {
                        $extra = " onclick=\"return btn_submit('subform2_new');\"";
                        $row["NEW_SET"] = KnjCreateBtn($objForm, "NEW_SET", "コピー新規", $extra.$disabled);
                    }
                    $row["KINDNAME"] = View::alink("knje390nindex.php", $row["KINDNAME"], "target=_self tabindex=\"-1\"",
                                                    array("SCHREGNO"        => $row["SCHREGNO"],
                                                          "cmd"             => "subform".$row["KINDCD"]."A",
                                                          "MAIN_YEAR"       => $model->mainfield["YEAR_A"],
                                                          "RECORD_DATE"     => $model->mainfield["RECORD_DATE_A"],
                                                          "TYPE"            => "list"));
                    $setval = $row;
                    $arg["list"][] = $setval;
                //C　年度ごと
                } else if ($row["KINDCD"] === '3') {
                    if (!$hyoujiCFlg) {
                        $extra = "onchange=\"return btn_submit('edit_change')\"";
                        $query = knje390nQuery::getMainYear($model, "C");
                        $row["YEAR_SET"] = makeCmb($objForm, $arg, $db, $query, "YEAR_C", $model->mainfield["YEAR_C"], $extra, 1, "");

                        if ($model->Properties["TokushiShienPlanPatern"] != "1") {
                            $extra = "onchange=\"return btn_submit('edit_change')\"";
                            $query = knje390nQuery::getRecordDate($model, "C");
                            $row["RECORD_DATE_SET"] = makeCmb($objForm, $arg, $db, $query, "RECORD_DATE_C", $model->mainfield["RECORD_DATE_C"], $extra, 1, "");
                            
                            //新規ボタンを配置
                            $setData = array();
                            $setData = $db->getRow($query, DB_FETCHMODE_ASSOC);
                            if ($setData["VALUE"] != "") {
                                $extra = " onclick=\"return btn_submit('subform3_new');\"";
                                $row["NEW_SET"] = KnjCreateBtn($objForm, "NEW_SET", "コピー新規", $extra.$disabled);
                            }
                        }

                        $row["KINDNAME"] = View::alink("knje390nindex.php", $row["KINDNAME"], "target=_self tabindex=\"-1\"",
                                                        array("SCHREGNO"        => $row["SCHREGNO"],
                                                              "cmd"             => "subform".$row["KINDCD"]."A",
                                                              "MAIN_YEAR"      => $model->mainfield["YEAR_C"],
                                                              "RECORD_DATE"     => $model->mainfield["RECORD_DATE_C"],
                                                              "TYPE"            => "list"));

                        if ($row["SORT"] === 'C-1') {
                            $hyoujiCFlg = true;
                        }
                        $setval = $row;
                        $arg["list"][] = $setval;
                    //重複時は表示させない
                    } else {
                        $row = array();
                    }
                //D
                } else if ($row["KINDCD"] === '4') {
                    if (!$hyoujiDFlg) {
                        $extra = "onchange=\"return btn_submit('edit_change')\"";
                        $query = knje390nQuery::getRecordDate($model, "D");
                        $row["RECORD_DATE_SET"] = makeCmb($objForm, $arg, $db, $query, "RECORD_DATE_D", $model->mainfield["RECORD_DATE_D"], $extra, 1, "");
                        $row["KINDNAME"] = View::alink("knje390nindex.php", $row["KINDNAME"], "target=_self tabindex=\"-1\"",
                                                        array("SCHREGNO"        => $row["SCHREGNO"],
                                                              "cmd"             => "subform".$row["KINDCD"]."A",
                                                              "RECORD_DATE"     => $model->mainfield["RECORD_DATE_D"],
                                                              "TYPE"            => "list"));
                        if ($row["SORT"] === 'D-1') {
                            $hyoujiDFlg = true;
                        }
                        $setval = $row;
                        $arg["list"][] = $setval;
                    //重複時は表示させない
                    } else {
                        $row = array();
                    }
                    
                //E　年度ごと
                } else if ($row["KINDCD"] === '5') {
                    $extra = "onchange=\"return btn_submit('edit_change')\"";
                    $query = knje390nQuery::getMainYear($model, "E");
                    $row["YEAR_SET"] = makeCmb($objForm, $arg, $db, $query, "YEAR_E", $model->mainfield["YEAR_E"], $extra, 1, "");
                    
                    $extra = "onchange=\"return btn_submit('edit_change')\"";
                    $query = knje390nQuery::getRecordDate($model, "E");
                    $row["RECORD_DATE_SET"] = makeCmb($objForm, $arg, $db, $query, "RECORD_DATE_E", $model->mainfield["RECORD_DATE_E"], $extra, 1, "");
                    
                    //新規ボタンを配置
                    $setData = array();
                    $setData = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    if ($setData["VALUE"] != "") {
                        $extra = " onclick=\"return btn_submit('subform5_new');\"";
                        $row["NEW_SET"] = KnjCreateBtn($objForm, "NEW_SET", "コピー新規", $extra.$disabled);
                    }
                    $row["KINDNAME"] = View::alink("knje390nindex.php", $row["KINDNAME"], "target=_self tabindex=\"-1\"",
                                                    array("SCHREGNO"        => $row["SCHREGNO"],
                                                          "cmd"             => "subform".$row["KINDCD"]."A",
                                                          "MAIN_YEAR"       => $model->mainfield["YEAR_E"],
                                                          "RECORD_DATE"     => $model->mainfield["RECORD_DATE_E"],
                                                          "TYPE"            => "list"));
                    $setval = $row;
                    $arg["list"][] = $setval;
                    
                //F　年度ごと
                } else if ($row["KINDCD"] === '6') {
                    $extra = "onchange=\"return btn_submit('edit_change')\"";
                    $query = knje390nQuery::getMainYear($model, "F");
                    $row["YEAR_SET"] = makeCmb($objForm, $arg, $db, $query, "YEAR_F", $model->mainfield["YEAR_F"], $extra, 1, "");
                    
                    $extra = "onchange=\"return btn_submit('edit_change')\"";
                    $query = knje390nQuery::getRecordDate($model, "F");
                    $row["RECORD_DATE_SET"] = makeCmb($objForm, $arg, $db, $query, "RECORD_DATE_F", $model->mainfield["RECORD_DATE_F"], $extra, 1, "");
                    
                    //新規ボタンを配置
                    $setData = array();
                    $setData = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    if ($setData["VALUE"] != "") {
                        $extra = " onclick=\"return btn_submit('subform6_new');\"";
                        $row["NEW_SET"] = KnjCreateBtn($objForm, "NEW_SET", "コピー新規", $extra.$disabled);
                    }
                    $row["KINDNAME"] = View::alink("knje390nindex.php", $row["KINDNAME"], "target=_self tabindex=\"-1\"",
                                                    array("SCHREGNO"        => $row["SCHREGNO"],
                                                          "cmd"             => "subform".$row["KINDCD"]."A",
                                                          "MAIN_YEAR"       => $model->mainfield["YEAR_F"],
                                                          "RECORD_DATE"     => $model->mainfield["RECORD_DATE_F"],
                                                          "TYPE"            => "list"));
                    $setval = $row;
                    $arg["list"][] = $setval;
                    
                //G　年度ごと
                } else if ($row["KINDCD"] === '7') {
                    $extra = "onchange=\"return btn_submit('edit_change')\"";
                    $query = knje390nQuery::getMainYear($model, "G");
                    $row["YEAR_SET"] = makeCmb($objForm, $arg, $db, $query, "YEAR_G", $model->mainfield["YEAR_G"], $extra, 1, "");
                    
                    $extra = "onchange=\"return btn_submit('edit_change')\"";
                    $query = knje390nQuery::getRecordDate($model, "G");
                    $row["RECORD_DATE_SET"] = makeCmb($objForm, $arg, $db, $query, "RECORD_DATE_G", $model->mainfield["RECORD_DATE_G"], $extra, 1, "");
                    
                    //新規ボタンを配置
                    $setData = array();
                    $setData = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    if ($setData["VALUE"] != "") {
                        $extra = " onclick=\"return btn_submit('subform7_new');\"";
                        $row["NEW_SET"] = KnjCreateBtn($objForm, "NEW_SET", "コピー新規", $extra.$disabled);
                    }
                    $row["KINDNAME"] = View::alink("knje390nindex.php", $row["KINDNAME"], "target=_self tabindex=\"-1\"",
                                                    array("SCHREGNO"        => $row["SCHREGNO"],
                                                          "cmd"             => "subform".$row["KINDCD"]."A",
                                                          "MAIN_YEAR"       => $model->mainfield["YEAR_G"],
                                                          "RECORD_DATE"     => $model->mainfield["RECORD_DATE_G"],
                                                          "TYPE"            => "list"));
                    $setval = $row;
                    $arg["list"][] = $setval;
                    
                //H　年度ごと
                } else if ($row["KINDCD"] === '8') {
                    $extra = "onchange=\"return btn_submit('edit_change')\"";
                    $query = knje390nQuery::getMainYear($model, "H");
                    $row["YEAR_SET"] = makeCmb($objForm, $arg, $db, $query, "YEAR_H", $model->mainfield["YEAR_H"], $extra, 1, "");
                    
                    $extra = "onchange=\"return btn_submit('edit_change')\"";
                    $query = knje390nQuery::getRecordDate($model, "H");
                    $row["RECORD_DATE_SET"] = makeCmb($objForm, $arg, $db, $query, "RECORD_DATE_H", $model->mainfield["RECORD_DATE_H"], $extra, 1, "");
                    
                    //新規ボタンを配置
                    $setData = array();
                    $setData = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    if ($setData["VALUE"] != "") {
                        $extra = " onclick=\"return btn_submit('subform8_new');\"";
                        $row["NEW_SET"] = KnjCreateBtn($objForm, "NEW_SET", "コピー新規", $extra.$disabled);
                    }
                    $row["KINDNAME"] = View::alink("knje390nindex.php", $row["KINDNAME"], "target=_self tabindex=\"-1\"",
                                                    array("SCHREGNO"        => $row["SCHREGNO"],
                                                          "cmd"             => "subform".$row["KINDCD"]."A",
                                                          "MAIN_YEAR"       => $model->mainfield["YEAR_H"],
                                                          "RECORD_DATE"     => $model->mainfield["RECORD_DATE_H"],
                                                          "TYPE"            => "list"));
                    $setval = $row;
                    $arg["list"][] = $setval;
                }
                
                //$setval = $row;
                //$arg["list"][] = $setval;
                //データチェック
                $dataflg[$row["KINDCD"]] = "1";
            }
        }
        
        //作成年月日
        $set1_3monthYear = CTRL_YEAR+1;
        knjCreateHidden($objForm, "SDATE", CTRL_YEAR.'/04/01');
        knjCreateHidden($objForm, "EDATE", $set1_3monthYear.'/03/31');
        
        //作成日付
        if (!$model->mainfield["WRITING_DATE_MAIN"]) {
            $model->mainfield["WRITING_DATE_MAIN"] = str_replace("-", "/", CTRL_DATE);
        }
        $arg["data"]["WRITING_DATE_MAIN"] = View::popUpCalendar($objForm, "WRITING_DATE_MAIN", str_replace("-", "/", $model->mainfield["WRITING_DATE_MAIN"]));
        
        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["IFRAME"] = VIEW::setIframeJs();

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knje390nForm1.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    $disabled = ($model->schregno) ? "": " disabled";
    //基本情報ボタン
    $extra = "style=\"height:22px;width:130px;background:#00FFFF;color:#000080;font:bold\" onclick=\"return btn_submit('subform1');\"";
    $arg["button"]["btn_subform1"] = KnjCreateBtn($objForm, "btn_subform1", "基本情報", $extra);
    //アセスメント表ボタン
    $extra = "style=\"height:22px;width:130px;background:#ADFF2F;color:#006400;font:bold\" onclick=\"return btn_submit('subform2');\"";
    $arg["button"]["btn_subform2"] = KnjCreateBtn($objForm, "btn_subform2", "アセスメント表", $extra);
    //支援計画ボタン
    $extra = "style=\"height:22px;width:130px;background:#FFFF00;color:#FF8C00;font:bold\" onclick=\"return btn_submit('subform3');\"";
    $arg["button"]["btn_subform3"] = KnjCreateBtn($objForm, "btn_subform3", "支援計画", $extra);
    //移行支援計画ボタン
    $extra = "style=\"height:22px;width:130px;background:#FFE4E1;color:#FF0000;font:bold\" onclick=\"return btn_submit('subform4');\"";
    $arg["button"]["btn_subform4"] = KnjCreateBtn($objForm, "btn_subform4", "移行支援計画", $extra);
    
    //サポートブック
    $extra = "style=\"height:22px;width:130px;background:#F0E68C;color:#660000;font:bold\" onclick=\"return btn_submit('subform5');\"";
    $arg["button"]["btn_subform5"] = KnjCreateBtn($objForm, "btn_subform5", "サポートブック", $extra);
    //引継資料(担任)
    $extra = "style=\"height:22px;width:130px;background:#D0B0FF;color:#191970;font:bold\" onclick=\"return btn_submit('subform6');\"";
    $arg["button"]["btn_subform6"] = KnjCreateBtn($objForm, "btn_subform6", "引継資料(担任)", $extra);
    //引継資料(事業者)
    $extra = "style=\"height:22px;width:130px;background:#FFAD90;color:#B22222;font:bold\" onclick=\"return btn_submit('subform7');\"";
    $arg["button"]["btn_subform7"] = KnjCreateBtn($objForm, "btn_subform7", "引継資料(事業者)", $extra);
    //関係者間資料
    $extra = "style=\"height:22px;width:130px;background:#A4C6FF;color:#00008B;font:bold\" onclick=\"return btn_submit('subform8');\"";
    $arg["button"]["btn_subform8"] = KnjCreateBtn($objForm, "btn_subform8", "関係者間資料", $extra);

    //家族構成ボタンを作成する
    $extra = "onclick=\"loadwindow('" .REQUESTROOT."/E/KNJE390N/knje390nindex.php?cmd=family&SCHREGNO=".$model->schregno."', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 650, 600)\"";
    $arg["button"]["btn_family"] = knjCreateBtn($objForm, "btn_family", "家族構成", $extra.$disabled);
    //終了ボタンを作成する
    $extra = "style=\"height:30px\" onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
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

   return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
    knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
    knjCreateHidden($objForm, "cmd");
}
?>
