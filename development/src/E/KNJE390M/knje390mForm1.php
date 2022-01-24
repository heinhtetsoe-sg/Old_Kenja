<?php

require_once('for_php7.php');

class knje390mForm1
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knje390mindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報取得
        $info = $db->getRow(knje390mQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];

        //生徒詳細情報取得
        $infoshousai = $db->getRow(knje390mQuery::getSchInfoShousai($model), DB_FETCHMODE_ASSOC);
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
        $arg["data"]["GUARD_TELNO"] = $infoshousai["GUARD_TELNO"];
        $arg["data"]["EMERGENCYTELNO"] = $infoshousai["EMERGENCYTELNO"];
        $arg["data"]["EMERGENCYNAME"] = $infoshousai["EMERGENCYNAME"];
        $arg["data"]["EMERGENCYTELNO2"] = $infoshousai["EMERGENCYTELNO2"];
        $arg["data"]["EMERGENCYNAME2"] = $infoshousai["EMERGENCYNAME2"];
        $arg["data"]["EMERGENCYTELNO3"] = $infoshousai["EMERGENCYTELNO3"];
        $arg["data"]["EMERGENCYNAME3"] = $infoshousai["EMERGENCYNAME3"];
        
        //顔写真
        $arg["data"]["FACE_IMG"] = REQUESTROOT."/".$model->control_data["LargePhotoPath"]."/P".$infoshousai["SCHREGNO"].".".$model->control_data["Extension"];
        $arg["data"]["IMG_PATH"] = REQUESTROOT."/".$model->control_data["LargePhotoPath"]."/P".$infoshousai["SCHREGNO"].".".$model->control_data["Extension"];
        
        //[高校生][理療] D 移行支援計画を表示
        if ($infoshousai["SCHOOL_KIND"] === 'H' || $infoshousai["SCHOOL_KIND"] === 'A') {
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
        $cnt = get_count($db->getcol(knje390mQuery::selectQuery($model)));
        if ($model->schregno && $cnt) {
            $dataflg = array();
            $dataflg = array("1" => "", "2" => "", "3" => "", "4" => "");
            $result = $db->query(knje390mQuery::selectQuery($model));
            // var_dump(knje390mQuery::selectQuery($model));
            //表示用フラグ(同一テーブルは複数表示されるため)
            $hyoujiCFlg = false;
            $hyoujiDFlg = false;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //履歴コンボ作成
                //B　
                if ($row["KINDCD"] === '1') {
                    $extra = "onchange=\"return btn_submit('edit_change')\"";
                    $query = knje390mQuery::getRecordDate($model, "B");
                    $row["RECORD_DATE_SET"] = makeCmb($objForm, $arg, $db, $query, "RECORD_DATE_B", $model->mainfield["RECORD_DATE_B"], $extra, 1, "");
                    $row["KINDNAME"] = View::alink(
                        "knje390mindex.php",
                        $row["KINDNAME"],
                        "target=_self tabindex=\"-1\"",
                        array("SCHREGNO"        => $row["SCHREGNO"],
                                                          "cmd"             => "subform".$row["KINDCD"]."A",
                                                          "RECORD_DATE"     => $model->mainfield["RECORD_DATE_B"],
                                                          "TYPE"            => "list")
                    );
                    $setval = $row;
                    $arg["list"][] = $setval;
                //A　年度ごと
                } elseif ($row["KINDCD"] === '2') {
                    $extra = "onchange=\"return btn_submit('edit_change')\"";
                    $query = knje390mQuery::getRecordDate($model, "A");
                    $row["RECORD_DATE_SET"] = makeCmb($objForm, $arg, $db, $query, "RECORD_DATE_A", $model->mainfield["RECORD_DATE_A"], $extra, 1, "");

                    $row["KINDNAME"] = View::alink(
                        "knje390mindex.php",
                        $row["KINDNAME"],
                        "target=_self tabindex=\"-1\"",
                        array("SCHREGNO"        => $row["SCHREGNO"],
                                                          "cmd"             => "subform".$row["KINDCD"]."A",
                                                          "MAIN_YEAR"       => $model->mainfield["YEAR_A"],
                                                          "RECORD_DATE"     => $model->mainfield["RECORD_DATE_A"],
                                                          "TYPE"            => "list")
                    );
                    $setval = $row;
                    $arg["list"][] = $setval;
                //C　年度ごと
                } elseif ($row["KINDCD"] === '3') {
                    if (!$hyoujiCFlg) {
                        $extra = "onchange=\"return btn_submit('edit_change')\"";
                        $query = knje390mQuery::getRecordDate($model, "C");
                        // var_dump($query);
                        $row["RECORD_DATE_SET"] = makeCmb($objForm, $arg, $db, $query, "RECORD_DATE_C", $model->mainfield["RECORD_DATE_C"], $extra, 1, "");

                        $row["KINDNAME"] = View::alink(
                            "knje390mindex.php",
                            $row["KINDNAME"],
                            "target=_self tabindex=\"-1\"",
                            array("SCHREGNO"        => $row["SCHREGNO"],
                                                              "cmd"             => "subform".$row["KINDCD"]."A",
                                                              "MAIN_YEAR"      => $model->mainfield["YEAR_C"],
                                                              "RECORD_DATE"     => $model->mainfield["RECORD_DATE_C"],
                                                              "TYPE"            => "list")
                        );

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
                } elseif ($row["KINDCD"] === '4') {
                    if (!$hyoujiDFlg) {
                        $extra = "onchange=\"return btn_submit('edit_change')\"";
                        $query = knje390mQuery::getRecordDate($model, "D");
                        $row["RECORD_DATE_SET"] = makeCmb($objForm, $arg, $db, $query, "RECORD_DATE_D", $model->mainfield["RECORD_DATE_D"], $extra, 1, "");
                        $row["KINDNAME"] = View::alink(
                            "knje390mindex.php",
                            $row["KINDNAME"],
                            "target=_self tabindex=\"-1\"",
                            array("SCHREGNO"        => $row["SCHREGNO"],
                                                              "cmd"             => "subform".$row["KINDCD"]."A",
                                                              "RECORD_DATE"     => $model->mainfield["RECORD_DATE_D"],
                                                              "TYPE"            => "list")
                        );
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
                } elseif ($row["KINDCD"] === '5') {
                    $extra = "onchange=\"return btn_submit('edit_change')\"";
                    $query = knje390mQuery::getRecordDate($model, "E");
                    $row["RECORD_DATE_SET"] = makeCmb($objForm, $arg, $db, $query, "RECORD_DATE_E", $model->mainfield["RECORD_DATE_E"], $extra, 1, "");

                    $row["KINDNAME"] = View::alink(
                        "knje390mindex.php",
                        $row["KINDNAME"],
                        "target=_self tabindex=\"-1\"",
                        array("SCHREGNO"        => $row["SCHREGNO"],
                                                          "cmd"             => "subform".$row["KINDCD"]."A",
                                                          "MAIN_YEAR"       => $model->mainfield["YEAR_E"],
                                                          "RECORD_DATE"     => $model->mainfield["RECORD_DATE_E"],
                                                          "TYPE"            => "list")
                    );
                    $setval = $row;
                    $arg["list"][] = $setval;
                    
                //F　年度ごと
                } elseif ($row["KINDCD"] === '6') {
                    $extra = "onchange=\"return btn_submit('edit_change')\"";
                    $query = knje390mQuery::getRecordDate($model, "F");
                    $row["RECORD_DATE_SET"] = makeCmb($objForm, $arg, $db, $query, "RECORD_DATE_F", $model->mainfield["RECORD_DATE_F"], $extra, 1, "");

                    $row["KINDNAME"] = View::alink(
                        "knje390mindex.php",
                        $row["KINDNAME"],
                        "target=_self tabindex=\"-1\"",
                        array("SCHREGNO"        => $row["SCHREGNO"],
                                                          "cmd"             => "subform".$row["KINDCD"]."A",
                                                          "MAIN_YEAR"       => $model->mainfield["YEAR_F"],
                                                          "RECORD_DATE"     => $model->mainfield["RECORD_DATE_F"],
                                                          "TYPE"            => "list")
                    );
                    $setval = $row;
                    $arg["list"][] = $setval;
                    
                //G　年度ごと
                } elseif ($row["KINDCD"] === '7') {
                    $extra = "onchange=\"return btn_submit('edit_change')\"";
                    $query = knje390mQuery::getRecordDate($model, "G");
                    $row["RECORD_DATE_SET"] = makeCmb($objForm, $arg, $db, $query, "RECORD_DATE_G", $model->mainfield["RECORD_DATE_G"], $extra, 1, "");

                    $row["KINDNAME"] = View::alink(
                        "knje390mindex.php",
                        $row["KINDNAME"],
                        "target=_self tabindex=\"-1\"",
                        array("SCHREGNO"        => $row["SCHREGNO"],
                                                          "cmd"             => "subform".$row["KINDCD"]."A",
                                                          "MAIN_YEAR"       => $model->mainfield["YEAR_G"],
                                                          "RECORD_DATE"     => $model->mainfield["RECORD_DATE_G"],
                                                          "TYPE"            => "list")
                    );
                    $setval = $row;
                    $arg["list"][] = $setval;
                    
                //H　年度ごと
                } elseif ($row["KINDCD"] === '8') {
                    $extra = "onchange=\"return btn_submit('edit_change')\"";
                    $query = knje390mQuery::getRecordDate($model, "H");
                    $row["RECORD_DATE_SET"] = makeCmb($objForm, $arg, $db, $query, "RECORD_DATE_H", $model->mainfield["RECORD_DATE_H"], $extra, 1, "");

                    $row["KINDNAME"] = View::alink(
                        "knje390mindex.php",
                        $row["KINDNAME"],
                        "target=_self tabindex=\"-1\"",
                        array("SCHREGNO"        => $row["SCHREGNO"],
                                                          "cmd"             => "subform".$row["KINDCD"]."A",
                                                          "MAIN_YEAR"       => $model->mainfield["YEAR_H"],
                                                          "RECORD_DATE"     => $model->mainfield["RECORD_DATE_H"],
                                                          "TYPE"            => "list")
                    );
                    $setval = $row;
                    $arg["list"][] = $setval;
                } elseif ($row["KINDCD"] === '9') {
                    $extra = "onchange=\"return btn_submit('edit_change')\"";
                    $query = knje390mQuery::getMainYear($model, "I");

                    $extra = "onchange=\"return btn_submit('edit_change')\"";
                    $query = knje390mQuery::getRecordDate($model, "I");
                    $row["RECORD_DATE_SET"] = makeCmb($objForm, $arg, $db, $query, "RECORD_DATE_I", $model->mainfield["RECORD_DATE_I"], $extra, 1, "");

                    $row["KINDNAME"] = View::alink(
                        "knje390mindex.php",
                        $row["KINDNAME"],
                        "target=_self tabindex=\"-1\"",
                        array("SCHREGNO"        => $row["SCHREGNO"],
                                                          "cmd"             => "subformZittai",
                                                          "MAIN_YEAR"       => $model->mainfield["YEAR_I"],
                                                          "RECORD_DATE"     => $model->mainfield["RECORD_DATE_I"],
                                                          "TYPE"            => "list")
                    );
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
        // $arg["data"]["WRITING_DATE_MAIN"] = View::popUpCalendar($objForm, "WRITING_DATE_MAIN", str_replace("-", "/", $model->mainfield["WRITING_DATE_MAIN"]));
        knjCreateHidden($objForm, "WRITING_DATE_MAIN", $model->mainfield["WRITING_DATE_MAIN"]);

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
        View::toHTML($model, "knje390mForm1.html", $arg);
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
    //実態表ボタン
    $extra = "style=\"height:22px;width:130px;background:#FF7F27;color:#FFFFFF;font:bold\" onclick=\"return btn_submit('subformZittai');\"";
    $arg["button"]["btn_subform9"] = KnjCreateBtn($objForm, "btn_subform9", "実態表", $extra);

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
    $extra = "onclick=\"loadwindow('" .REQUESTROOT."/E/KNJE390M/knje390mindex.php?cmd=family&SCHREGNO=".$model->schregno."', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 700, 600)\"";
    $arg["button"]["btn_family"] = knjCreateBtn($objForm, "btn_family", "家族構成", $extra.$disabled);
    //終了ボタンを作成する
    $extra = "style=\"height:30px\" onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
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
