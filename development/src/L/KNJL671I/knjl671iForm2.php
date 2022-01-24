<?php

require_once('for_php7.php');

class knjl671iForm2
{
    public function main(&$model)
    {
        //権限チェック
        $jscriptStr = "";
        if (AUTHORITY != DEF_UPDATABLE) {
            $jscriptStr .= "OnAuthError();";
        }
        if ($model->sendCmd == "updateAll") {
            $jscriptStr .= " updateCheckBoxChanged()";
        }
        $arg["jscript"] = $jscriptStr;

        $objForm = new form();
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjl671iindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //警告メッセージを表示しない場合
        $Row = array();
        if (($model->examno != "") && !isset($model->warning) && $model->cmd != 'search') {
            $query = knjl671iQuery::getSelectQuery($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        } else {
            $Row =& $model->field;
            $Row["EXAMNO"] = $model->examno;
        }

        //表示項目の設定
        if ($model->sendCmd == "updateAll") {
            //更新画面の場合（一部項目専用の更新画面は除く）
            $arg["histRecord"] = "1";
        } elseif ($model->sendCmd == "search") {
            //参照画面の場合
            if ($model->examno != "") {
                $arg["histList"] = "1";
            }
        }
        if (array_key_exists("FINSCHOOLCD", $model->updItemArray)) {
            $arg["UPD_FINSCHOOLCD"] = "1";
        }
        if (array_key_exists("SEQ007_REMARK1", $model->updItemArray)) {
            $arg["UPD_PRISCHOOLCD"] = "1";
        }
        // 表示項目CSV機能（新規の場合CSV機能をレンダリング）
        if ($model->sendPrgid == "KNJL670I") {
            $arg["contentsCsv"] = "1";
        }

        //各種項目の設定
        foreach ($model->itemArray as $key => $val) {
            if (array_key_exists($key, $model->updItemArray)) {
                //更新対象項目
                $extra = "";
                if ($key == "FINSCHOOLCD") {
                    //出身学校コード
                    $keta = $model->ketaArray[$key];
                    $extra = "onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\" id=\"FINSCHOOLCD_ID\" ";
                    $arg["data"]["FINSCHOOLCD"] = knjCreateTextBox($objForm, $Row["FINSCHOOLCD"], "FINSCHOOLCD", $keta, $keta, $extra);

                    //かな検索ボタン（出身学校）
                    $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=searchMain3&fscdname=FINSCHOOLCD_ID&fsname=FINSCHOOLNAME_ID&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
                    $arg["button"]["btn_fin_kana_reference"] = knjCreateBtn($objForm, "btn_fin_kana_reference", "検 索", $extra);
                } elseif ($key == "SEQ007_REMARK1") {
                    //塾
                    $keta = $model->ketaArray[$key];
                    $extra = "onblur=\"this.value=toInteger(this.value)\";";
                    $arg["data"]["PRISCHOOLCD"] = knjCreateTextBox($objForm, $Row[$key], "PRISCHOOLCD", $keta, $keta, $extra);

                    //かな検索ボタン（塾）
                    $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_PRISCHOOL/knjwpri_searchindex.php?cmd=&pricdname=&priname=&priaddr=&prischool_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
                    $arg["button"]["btn_pri_kana_reference"] = knjCreateBtn($objForm, "btn_pri_kana_reference", "検 索", $extra);
                } else {
                    if ($val == "TEXT") {
                        //テキスト
                        $keta = $model->ketaArray[$key];
                        $arg["data"][$key] = knjCreateTextBox($objForm, $Row[$key], $key, $keta*2, $keta*2, $extra);
                        $arg["data"][$key."_KETA"] = "(全角{$keta}文字)";
                    } elseif ($val == "INTEGER") {
                        //テキスト(数字)
                        $extra = "onblur=\"this.value=toInteger(this.value)\"";
                        $keta = $model->ketaArray[$key];
                        $arg["data"][$key] = knjCreateTextBox($objForm, $Row[$key], $key, $keta, $keta, $extra);
                    } elseif ($val == "COMBO") {
                        //コンボ
                        $query = "";
                        if ($key == "SEQ001_REMARK1") {
                            $query = knjl671iQuery::getEntexamTestdivMst($model); //試験区分
                        } elseif ($key == "SEQ002_REMARK1") {
                            $query = knjl671iQuery::getEntexamGeneralMst($model, "02"); //相談コース
                        } elseif ($key == "SEQ004_REMARK1") {
                            $query = knjl671iQuery::getEntexamGeneralMst($model, "05"); //特待理由
                        } elseif ($key == "SEQ004_REMARK2") {
                            $query = knjl671iQuery::getEntexamGeneralMst($model, "04"); //特待記号
                        } elseif ($key == "SEQ005_REMARK1") {
                            $query = knjl671iQuery::getClubMst(); //部活動
                        }
                        makeCmb($objForm, $arg, $db, $query, $Row[$key], $key, $extra, 1, "BLANK");
                    } elseif ($val == "CHECK") {
                        //チェックボックス
                        $extra = " id=\"".$key."\" ";
                        if ($Row[$key] == "1") {
                            $extra .= " checked='checked' ";
                        }
                        $arg["data"][$key] = knjCreateCheckBox($objForm, $key, "1", $extra);
                    } elseif ($val == "TEXTAREA") {
                        //テキストエリア
                        $gyo  = $model->gyoArray[$key];
                        $keta = $model->ketaArray[$key];
                        $moji = $keta * $gyo; //全角文字数 = 桁数 * 行数
                        $width = $keta * 14;  //テキストエリアの横幅 = 桁数 * 14px
                        $extra = " style='width:".$width."px'";
                        $arg["data"][$key] = knjCreateTextArea($objForm, $key, $gyo, $keta, "soft", $extra, $Row[$key]);
                        $arg["data"][$key."_KETA"] = "(全角{$moji}文字)";
                    }
                }
            } else {
                //更新対象外
                if ($val == "COMBO" && $Row[$key] != "") {
                    //コンボの名称を取得
                    if ($key == "SEQ001_REMARK1") {
                        //試験区分
                        $query = knjl671iQuery::getEntexamTestdivMst($model, $Row[$key]);
                        $comboRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                        $Row[$key] = $comboRow["TESTDIV_NAME"];
                    } elseif ($key == "SEQ002_REMARK1") {
                        //相談コース
                        $query = knjl671iQuery::getEntexamGeneralMst($model, "02", $Row[$key]);
                        $comboRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                        $Row[$key] = $comboRow["GENERAL_NAME"];
                    } elseif ($key == "SEQ004_REMARK1") {
                        //特待理由
                        $query = knjl671iQuery::getEntexamGeneralMst($model, "05", $Row[$key]);
                        $comboRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                        $Row[$key] = $comboRow["GENERAL_NAME"];
                    } elseif ($key == "SEQ004_REMARK2") {
                        //特待記号
                        $query = knjl671iQuery::getEntexamGeneralMst($model, "04", $Row[$key]);
                        $comboRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                        $Row[$key] = $comboRow["GENERAL_NAME"];
                    } elseif ($key == "SEQ005_REMARK1") {
                        //部活動
                        $query = knjl671iQuery::getClubMst($Row[$key]);
                        $comboRow = $db->getRow($query, DB_FETCHMODE_ASSOC);
                        $Row[$key] = $comboRow["CLUBNAME"];
                    }
                }
                if ($key == "EXCLUSION") {
                    //除外フラグ
                    $Row[$key] = ($Row[$key] == "1") ? "済" : "";
                }
                $arg["data"][$key] = $Row[$key];
            }
        }

        /** 表示のみの項目 START **/
        if ($model->examno != "") {
            //担当者名・学校名・地区名
            $fsArray = [];
            if (trim($Row["FINSCHOOLCD"]) != '') {
                $query = knjl671iQuery::getFinschoolName($Row["FINSCHOOLCD"]);
                $fsArray = $db->getRow($query, DB_FETCHMODE_ASSOC);
            }
            $arg["data"]["STAFFNAME"] = ($fsArray["STAFFNAME"] ? $fsArray["STAFFNAME"] : ''); //担当者名
            $arg["data"]["FINSCHOOLNAME"] = ($fsArray["FINSCHOOL_NAME"] ? $fsArray["FINSCHOOL_NAME"] : ''); //学校名
            $arg["data"]["FINSCHOOL_DISTCD_NAME"] = ($fsArray["FINSCHOOL_DISTCD_NAME"] ? $fsArray["FINSCHOOL_DISTCD_NAME"] : ''); //地区名称
            knjCreateHidden($objForm, "FINSCHOOL_STAFFCD", ($fsArray["FINSCHOOL_STAFFCD"] ? $fsArray["FINSCHOOL_STAFFCD"] : '')); //担任
            knjCreateHidden($objForm, "FINSCHOOL_DISTCD", ($fsArray["FINSCHOOL_DISTCD"] ? $fsArray["FINSCHOOL_DISTCD"] : '')); //地区コード

            //塾名称
            $query = knjl671iQuery::getPriSchoolName($Row["SEQ007_REMARK1"]);
            $priArray = $db->getRow($query, DB_FETCHMODE_ASSOC);
            $arg["data"]["PRISCHOOL_NAME"] = ($priArray["PRISCHOOL_NAME"] ? $priArray["PRISCHOOL_NAME"] : '');
        }
        /** 表示のみの項目 END **/


        /** 変更履歴 START **/
        if ($model->sendCmd == "updateAll") {
            //更新画面の場合（一部項目専用の更新画面は除く）
            $histRow =& $model->histField;
            $changeExtra = "onClick=\"updateCheckBoxChanged()\"";
            //氏名チェックボックス
            $extra = " id=\"NAME_FLG\" ".$changeExtra;
            if ($histRow["NAME_FLG"] == "1") {
                $extra .= " checked='checked' ";
            }
            $arg["data"]["NAME_FLG"] = knjCreateCheckBox($objForm, "NAME_FLG", "1", $extra);

            //試験区分チェックボックス
            $extra = " id=\"TESTDIV_FLG\" ".$changeExtra;
            if ($histRow["TESTDIV_FLG"] == "1") {
                $extra .= " checked='checked' ";
            }
            $arg["data"]["TESTDIV_FLG"] = knjCreateCheckBox($objForm, "TESTDIV_FLG", "1", $extra);

            //相談コースチェックボックス
            $extra = " id=\"COURSE_FLG\" ".$changeExtra;
            if ($histRow["COURSE_FLG"] == "1") {
                $extra .= " checked='checked' ";
            }
            $arg["data"]["COURSE_FLG"] = knjCreateCheckBox($objForm, "COURSE_FLG", "1", $extra);

            //共通テストチェックボックス
            $extra = " id=\"STANDARD_EXAM_FLG\" ".$changeExtra;
            if ($histRow["STANDARD_EXAM_FLG"] == "1") {
                $extra .= " checked='checked' ";
            }
            $arg["data"]["STANDARD_EXAM_FLG"] = knjCreateCheckBox($objForm, "STANDARD_EXAM_FLG", "1", $extra);

            //特待チェックボックス
            $extra = " id=\"HONOR_FLG\" ".$changeExtra;
            if ($histRow["HONOR_FLG"] == "1") {
                $extra .= " checked='checked' ";
            }
            $arg["data"]["HONOR_FLG"] = knjCreateCheckBox($objForm, "HONOR_FLG", "1", $extra);

            //その他チェックボックス
            $extra = " id=\"OTHER_FLG\" ".$changeExtra;
            if ($histRow["OTHER_FLG"] == "1") {
                $extra .= " checked='checked' ";
            }
            $arg["data"]["OTHER_FLG"] = knjCreateCheckBox($objForm, "OTHER_FLG", "1", $extra);

            //テキストエリア
            $gyo  = $model->gyoArray["CHANGE_TEXT"];
            $keta = $model->ketaArray["CHANGE_TEXT"];
            $moji = $keta * $gyo; //全角文字数 = 桁数 * 行数
            $width = $keta * 14;  //テキストエリアの横幅 = 桁数 * 14px
            $extra = " style='width:".$width."px'";
            $arg["data"]["CHANGE_TEXT"] = knjCreateTextArea($objForm, "CHANGE_TEXT", $gyo, $keta, "soft", $extra, $histRow["CHANGE_TEXT"]);
            $arg["data"]["CHANGE_TEXT_KETA"] = "(全角{$moji}文字)";

            //依頼者
            $extra = "";
            $query = knjl671iQuery::getStaffMst();
            makeCmb($objForm, $arg, $db, $query, $histRow["CLIENT_STAFFCD"], "CLIENT_STAFFCD", $extra, 1, "BLANK");

            //編集者
            $extra = "";
            $query = knjl671iQuery::getStaffMst();
            makeCmb($objForm, $arg, $db, $query, $histRow["EDIT_STAFFCD"], "EDIT_STAFFCD", $extra, 1, "BLANK");

            //変更日時
            $arg["data"]["CHANGE_DATE"] = $Row["CHANGE_DATE"];
            knjCreateHidden($objForm, "CHANGE_DATE", $Row["CHANGE_DATE"]);
        } elseif ($model->sendCmd == "search" && $model->examno != "") {
            //参照画面の場合
            //各種チェックボックス
            $chkTextArray = array("NAME_FLG"            =>  "氏名",
                                  "TESTDIV_FLG"         =>  "試験区分",
                                  "COURSE_FLG"          =>  "相談コース",
                                  "STANDARD_EXAM_FLG"   =>  "共通テスト",
                                  "HONOR_FLG"           =>  "特待",
                                  "OTHER_FLG"           =>  "その他"
                            );

            //変更履歴一覧
            $query = knjl671iQuery::getHistQuery($model);
            $result = $db->query($query);
            while ($histRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //変更日時
                $histRow["CHANGE_DATE"] = $histRow["FORMAT_CHANGE_DATE"];
                //変更内容(各種チェックボックス)の作成
                $changeChkFlg = "";
                $comma = "";
                foreach ($chkTextArray as $key => $val) {
                    if ($histRow[$key] == "1") {
                        $changeChkFlg .= $comma.$val;
                        $comma = ", ";
                    }
                }
                $histRow["CHANGE_CHKFLG"] = $changeChkFlg;
                //レコードを連想配列のまま配列$arg[data]に追加していく。
                array_walk($histRow, "htmlspecialchars_array");
                $arg["histData"][] = $histRow;
            }
        }
        /** 変更履歴 END **/

        //CSVファイルアップロードコントロール(表示制御は contentsCsv を見てHTMLでレンダリング判定している)
        makeCsv($objForm, $arg, $model);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $model);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjl671iindex.php", "", "edit");
        $arg["finish"]  = $objForm->get_finish();
        Query::dbCheckIn($db);
        if (VARS::get("cmd") != "edit" && !$model->warning) {
            $model->year = CTRL_YEAR+1;
            $arg["reload"]  = "parent.left_frame.location.href='knjl671iindex.php?cmd=list&HID_SORT=".$model->sort."';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjl671iForm2.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//ボタン作成
function makeBtn(&$objForm, &$arg, &$model)
{
    //更新ボタン
    if ($model->sendCmd == "search") {
        $arg["isNotSearch"] = "";
    } else {
        $arg["isNotSearch"] = "1";
        $btnLabel = ($model->sendCmd == "insert") ? "登 録" : "更 新";
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", $btnLabel, $extra);

        // //削除ボタン
        // $extra = "onclick=\"return btn_submit('delete');\"";
        // $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        // //クリアボタン
        // $extra = "onclick=\"return btn_submit('reset');\"";
        // $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    }

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);
}

//CSV作成（新規画面:KNJL670I のみ）
function makeCsv(&$objForm, &$arg, $model)
{
    //出力取込種別ラジオボタン 1:ヘッダー出力 2:データ取込 3:データ出力 4:エラー出力
    $opt_shubetsu = array(1, 2, 3, 4);
    $model->field["OUTPUT"] = ($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : "1";
    $extra = array("id=\"OUTPUT1\"", "id=\"OUTPUT2\"", "id=\"OUTPUT3\"", "id=\"OUTPUT4\"");
    $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, count($opt_shubetsu));
    foreach ($radioArray as $key => $val) {
        $arg["data"][$key] = $val;
    }

    //ファイルからの取り込み
    $extra = "";
    $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);

    //ヘッダ有チェックボックス
    $check_header  = "checked id=\"HEADER\"";
    $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $check_header, "");

    //実行ボタン(CSV)
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
}

function makeHidden(&$objForm, &$model)
{
    knjCreateHidden($objForm, "PRGID", "KNJL671I");
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "SORT", $model->sort);
    knjCreateHidden($objForm, "HID_SORT", $model->sort);
    knjCreateHidden($objForm, "SEND_PRGID", $model->sendPrgid);
    knjCreateHidden($objForm, "SEND_CMD", $model->sendCmd);
    if ($model->sendCmd == "update") {
        knjCreateHidden($objForm, "EXAMNO", $model->examno);
    }
}
