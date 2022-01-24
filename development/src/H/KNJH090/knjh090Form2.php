<?php

require_once('for_php7.php');

class knjh090form2
{
    public function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjh090index.php", "", "edit");

        //表彰実績区分
        $opt_detaildiv = array();
        if (!$model->chktokiwagiflg) {
            $opt_detaildiv[] = array("label" => "賞データ","value" => "1");
        }
        $opt_detaildiv[] = array("label" => "罰データ","value" => "2");
        if ($model->chktokiwagiflg) {
            $opt_detaildiv[] = array("label" => "活動の記録","value" => "4");
        }

        if (!$model->detaildiv || !$model->schregno) {
            $model->detaildiv = 1;
        }
        if ($model->cmd == "clear") {
            $model->detaildiv = $model->org_detaildiv;
        }

        $extra = "onchange=\" return btn_submit('edit2')\"";
        $arg["data"]["DETAIL_DIV"] = knjCreateCombo($objForm, "DETAIL_DIV", $model->detaildiv, $opt_detaildiv, $extra, 1);

        //警告メッセージを表示しない場合 NO001
        if ($model->dtclick && !isset($model->warning)) {
            $Row = knjh090Query::getRow($model->detail_edate, $model->detail_sdate, $model->schregno, $model->detaildiv, $model->year);
            $temp_cd = $Row["SCHREGNO"];
        } else {
            if ($model->cmd == "clear") {
                $Row =& $model->clear;
            } else {
                $Row =& $model->field;
            }
        }

        $model->Properties["KNJH090DETAIL2PATAN"] = '1';

        if ($model->detaildiv == 2) {
            if ($model->Properties["KNJH090DETAIL2PATAN"] == '1') {
                $arg["PATAN2"] = '1';
                $arg["PATAN"]  = '';
            } else {
                $arg["PATAN"]  = '1';
                $arg["PATAN2"] = '';
            }
        } else {
            $arg["PATAN"]  = '1';
            $arg["PATAN2"] = '';
        }

        //取消押下時退避用 NO001
        if ($model->dtclick) {
            $model->clear = array("DETAIL_SDATE"  =>  $Row["DETAIL_SDATE"],        //登録日付
                                  "DETAIL_EDATE"  =>  $Row["DETAIL_EDATE"],        //終了日
                                  "DETAIL_DIV"    =>  $Row["DETAIL_DIV"],          //詳細区分
                                  "DETAILCD"      =>  $Row["DETAILCD"],            //罰則
                                  "CONTENT"       =>  $Row["CONTENT"],             //賞罰内容
                                  "REMARK"        =>  $Row["REMARK"],              //備考
                                  "BICYCLE_CD"    =>  $Row["BICYCLE_CD"],    //自転車許可番号
                                  "BICYCLE_NO"    =>  $Row["BICYCLE_NO"],    //駐輪所番号
                                  "OCCURRENCE_DATE"        =>  $Row["OCCURRENCE_DATE"],        //発生日付
                                  "INVESTIGATION_DATE"     =>  $Row["INVESTIGATION_DATE"],     //調査日付
                                  "STD_GUID_MTG_DATE"      =>  $Row["STD_GUID_MTG_DATE"],      //生徒指導部会議
                                  "ORIGINAL_PLAN_CD"       =>  $Row["ORIGINAL_PLAN_CD"],       //原案
                                  "STAFF_MTG_DATE"         =>  $Row["STAFF_MTG_DATE"],         //職員会議
                                  "PUNISH_CD"              =>  $Row["PUNISH_CD"],              //処分
                                  "OCCURRENCE_PLACE"       =>  $Row["OCCURRENCE_PLACE"],       //場所
                                  "DIARY_FLG"              =>  $Row["DIARY_FLG"],              //日誌
                                  "WRITTEN_OATH_FLG"       =>  $Row["WRITTEN_OATH_FLG"],       //誓約書
                                  "REPORT_FLG"             =>  $Row["REPORT_FLG"],             //調書
                                  "WRITTEN_STAFFCD"        =>  $Row["WRITTEN_STAFFCD"],        //資料作成者
                                  "INVESTIGATION_STAFFCD1" =>  $Row["INVESTIGATION_STAFFCD1"], //調査委員1
                                  "INVESTIGATION_STAFFCD2" =>  $Row["INVESTIGATION_STAFFCD2"], //調査委員2
                                  "INVESTIGATION_STAFFCD3" =>  $Row["INVESTIGATION_STAFFCD3"], //調査委員3
                                  "INVESTIGATION_STAFFCD4" =>  $Row["INVESTIGATION_STAFFCD4"]  //調査委員4
                                  );
        }

        //システム日付
        $today = getdate();
        $date  = mktime(0, 0, 0, $today["mon"], $today["mday"], $today["year"]);
        $date  = date("Y/m/d", $date);

        //登録日付 NO001
        $date_ymd = ($model->detail_sdate) ? strtr($model->detail_sdate, "-", "/") : $date;
        $arg["data"]["DETAIL_SDATE"] = View::popUpCalendar($objForm, "DETAIL_SDATE", $date_ymd);

        //詳細内容 NO001
        $opt_detailcd = array();
        $opt_detailcd[] = array("label" => "　","value" => "00");
        if ($model->detaildiv != 3) {
            $db = Query::dbCheckOut();

            if ($model->detaildiv == 1) {
                $query = knjh090Query::getName('H303');
            } elseif ($model->detaildiv == 2) {
                $query = knjh090Query::getName('H304');
            } elseif ($model->detaildiv == 4) {
                $query = knjh090Query::getName('H317');
            }
            $result = $db->query($query);

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt_detailcd[] = array("label" => $row["NAMECD2"]."&nbsp;".$row["NAME1"],
                                        "value" => $row["NAMECD2"]);
            }

            $result->free();
            Query::dbCheckIn($db);
        }

        //原案・処分
        $opt_detailcd2 = array();
        $opt_detailcd2[] = array("label" => "　","value" => "00");
        if ($model->detaildiv == 2) {
            $db = Query::dbCheckOut();

            $query = knjh090Query::getName('H318');
            $result = $db->query($query);

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt_detailcd2[] = array("label" => $row["NAMECD2"]."&nbsp;".$row["NAME1"],
                                        "value" => $row["NAMECD2"]);
            }

            $result->free();
            Query::dbCheckIn($db);
        }

        //資料作成者・調査教員
        $opt_staffcd = array();
        $opt_staffcd[] = array("label" => "　","value" => "");
        if ($model->detaildiv != 3) {
            $db = Query::dbCheckOut();

            $query = knjh090Query::getStaffName();
            $result = $db->query($query);

            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $opt_staffcd[] = array("label" => $row["STAFFCD"]."&nbsp;&nbsp;".$row["STAFFNAME"],
                                        "value" => $row["STAFFCD"]);
            }

            $result->free();
            Query::dbCheckIn($db);
        }


        //詳細種類・罰理由
        $extra = "";
        $arg["data"]["DETAILCD"] = knjCreateCombo($objForm, "DETAILCD", $Row["DETAILCD"], $opt_detailcd, $extra, 1);

        //発生日付
        $date_ymd = ($Row["OCCURRENCE_DATE"]) ? strtr($Row["OCCURRENCE_DATE"], "-", "/") : $date;
        $arg["data"]["OCCURRENCE_DATE"] = View::popUpCalendar($objForm, "OCCURRENCE_DATE", $date_ymd);
        //調査日付
        $date_ymd = strtr($Row["INVESTIGATION_DATE"], "-", "/");
        $arg["data"]["INVESTIGATION_DATE"] = View::popUpCalendar($objForm, "INVESTIGATION_DATE", $date_ymd);
        //生徒指導部会議
        $date_ymd = strtr($Row["STD_GUID_MTG_DATE"], "-", "/");
        $arg["data"]["STD_GUID_MTG_DATE"] = View::popUpCalendar($objForm, "STD_GUID_MTG_DATE", $date_ymd);
        //原案
        $extra = "";
        $arg["data"]["ORIGINAL_PLAN_CD"] = knjCreateCombo($objForm, "ORIGINAL_PLAN_CD", $Row["ORIGINAL_PLAN_CD"], $opt_detailcd2, $extra, 1);
        //職員会議
        $date_ymd = strtr($Row["STAFF_MTG_DATE"], "-", "/");
        $arg["data"]["STAFF_MTG_DATE"] = View::popUpCalendar($objForm, "STAFF_MTG_DATE", $date_ymd);
        //処分
        $extra = "";
        $arg["data"]["PUNISH_CD"] = knjCreateCombo($objForm, "PUNISH_CD", $Row["PUNISH_CD"], $opt_detailcd2, $extra, 1);
        //場所
        $extra = "";
        $arg["data"]["OCCURRENCE_PLACE"] = knjCreateTextArea($objForm, "OCCURRENCE_PLACE", 1, 40, "soft", $extra, $Row["OCCURRENCE_PLACE"]);
        //日誌
        $extra  = $Row["DIARY_FLG"] ? " checked " : "";
        $extra .= " id=\"DIARY_FLG\"";
        $arg["data"]["DIARY_FLG"] = knjCreateCheckBox($objForm, "DIARY_FLG", "1", $extra);
        //誓約書
        $extra  = $Row["WRITTEN_OATH_FLG"] ? " checked " : "";
        $extra .= " id=\"WRITTEN_OATH_FLG\"";
        $arg["data"]["WRITTEN_OATH_FLG"] = knjCreateCheckBox($objForm, "WRITTEN_OATH_FLG", "1", $extra);
        //調書
        $extra  = $Row["REPORT_FLG"] ? " checked " : "";
        $extra .= " id=\"REPORT_FLG\"";
        $arg["data"]["REPORT_FLG"] = knjCreateCheckBox($objForm, "REPORT_FLG", "1", $extra);
        //資料作成者
        $extra = "";
        $arg["data"]["WRITTEN_STAFFCD"] = knjCreateCombo($objForm, "WRITTEN_STAFFCD", $Row["WRITTEN_STAFFCD"], $opt_staffcd, $extra, 1);
        //調査教員
        $extra = " style=\"width:200px\" ";
        $arg["data"]["INVESTIGATION_STAFFCD1"] = knjCreateCombo($objForm, "INVESTIGATION_STAFFCD1", $Row["INVESTIGATION_STAFFCD1"], $opt_staffcd, $extra, 1);
        $arg["data"]["INVESTIGATION_STAFFCD2"] = knjCreateCombo($objForm, "INVESTIGATION_STAFFCD2", $Row["INVESTIGATION_STAFFCD2"], $opt_staffcd, $extra, 1);
        $arg["data"]["INVESTIGATION_STAFFCD3"] = knjCreateCombo($objForm, "INVESTIGATION_STAFFCD3", $Row["INVESTIGATION_STAFFCD3"], $opt_staffcd, $extra, 1);
        $arg["data"]["INVESTIGATION_STAFFCD4"] = knjCreateCombo($objForm, "INVESTIGATION_STAFFCD4", $Row["INVESTIGATION_STAFFCD4"], $opt_staffcd, $extra, 1);

        //詳細内容
        $extra = "";
        $arg["data"]["CONTENT"] = knjCreateTextArea($objForm, "CONTENT", 6, 100, "soft", $extra, $Row["CONTENT"]);

        //備考
        $extra = "";
        $arg["data"]["REMARK"] = knjCreateTextArea($objForm, "REMARK", 2, 50, "soft", $extra, $Row["REMARK"]);

        //追加ボタンを作成する
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //更新ボタンを作成する
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除ボタンを作成する
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);
        //クリアボタンを作成する
        $extra = "onclick=\"return Btn_reset('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
        //一括更新ボタンを作成する
        $link = REQUESTROOT."/H/KNJH090_2/knjh090_2index.php?GRADE=".$model->grade."&HR_CLASS=".$model->hr_class;
        $extra = "onclick=\"Page_jumper('{$link}');\"";
        $arg["button"]["btn_batch"] = knjCreateBtn($objForm, "btn_batch", "一括更新", $extra);

        //PDF取込
        if ($model->Properties["savePdfFolderH090"]) {
            $arg["PDF"] = '1';
            updownPDF($objForm, $arg, $model);
        }

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "org_detail_sdate", strtr($model->org_detail_sdate, "/", "-"));
        knjCreateHidden($objForm, "org_detail_edate", strtr($model->org_detail_edate, "/", "-"));
        knjCreateHidden($objForm, "org_detaildiv", $model->org_detaildiv);
        knjCreateHidden($objForm, "UPDATED", $Row["UPDATED"]);
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        if ($temp_cd=="") {
            $temp_cd = $model->field["temp_cd"];
        }
        knjCreateHidden($objForm, "temp_cd", $temp_cd);

        $cd_change = false;
        if ($temp_cd==$Row["SCHREGNO"]) {
            $cd_change = true;
        }

        $arg["finish"]  = $objForm->get_finish();
        if (VARS::get("cmd") != "edit" && VARS::get("cmd") != "clear" && ($cd_change==true || $model->isload != 1)) {
            $arg["reload"]  = "window.open('knjh090index.php?cmd=list&SCHREGNO=$model->schregno','right_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjh090Form2.html", $arg);
    }
}

//PDF取込
function updownPDF(&$objForm, &$arg, $model)
{

    //移動後のファイルパス単位
    if ($model->schregno) {
        $dir = "/pdf/{$model->schregno}/{$model->Properties["savePdfFolderH090"]}/";
        $dataDir = DOCUMENTROOT . $dir;
        $searchFileName = $model->year.$model->detaildiv.str_replace("/", "", str_replace("-", "", $model->detail_sdate));
        if (!is_dir($dataDir)) {
            //echo "ディレクトリがありません。";
        } elseif ($aa = opendir($dataDir)) {
            $cnt = 0;
            while (false !== ($filename = readdir($aa))) {
                $filedir = REQUESTROOT . $dir . $filename;
                $info = pathinfo($filedir);
                //拡張子
                if ($info["extension"] == "pdf" && preg_match("/".$searchFileName."/", $info["basename"]) && $cnt < 5) {
                    $setFilename = mb_convert_encoding($filename, "UTF-8", "SJIS-win");
                    $setFiles = array();
                    $setFiles["PDF_FILE_NAME"] = $setFilename;
                    $setFiles["PDF_URL"] = REQUESTROOT . $dir . $setFilename;
                    $arg["down"][] = $setFiles;
                    $cnt++;
                }
            }
            closedir($aa);
        }
    }
    //ファイルからの取り込み
    $arg["up"]["FILE"] = knjCreateFile($objForm, "FILE", "", 10240000);
    //実行
    $extra = ($model->schregno) ? "onclick=\"return btn_submit('execute');\"" : "disabled";
    $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "btn_ok", "実 行", $extra);
}
