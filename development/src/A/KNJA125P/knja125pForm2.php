<?php

require_once('for_php7.php');

class knja125pForm2
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("form2", "POST", "knja125pindex.php", "", "form2");

        $arg["fep"] = $model->Properties["FEP"];

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $arg["NAME_SHOW"] = $model->schregno."　".$model->name;

        //学年の取得
        $query = knja125pQuery::getGradecd($model);
        $model->grade_cd = $db->getOne($query);

        //特別活動の記録出力項目取得
        $query = knja125pQuery::getNameMst($model, "D034");
        $result = $db->query($query);
        $model->itemArray = array();
        while ($setItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->itemArray[$setItem["NAMECD2"]] = $setItem;
        }

        //記録の取得
        $Row = $row = array();
        $result = $db->query(knja125pQuery::getBehavior($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $scd = $row["DIV"] .$row["CODE"];
            $Row["RECORD"][$scd] = $row["RECORD"];
        }
        $result->free();

        //特別活動の記録の観点取得
        $row = $db->getRow(knja125pQuery::getTrainRow($model), DB_FETCHMODE_ASSOC);

        //警告メッセージがある時と、更新の際はモデルの値を参照する
        if (isset($model->warning)) {
            $Row =& $model->record;
            $row =& $model->field;
        }

        //【通知票読込】行動の記録取得
        if ($model->cmd == "behavior_semes") {
            for ($h=1; $h<11; $h++) {
                $rcd = sprintf("%02d", $h);
                $ival = "3" . $rcd;
                $Row["RECORD"][$ival] = "";
                for ($i=1; $i<=$model->control["学期数"]; $i++) {
                    $result = $db->query(knja125pQuery::getBehaviorSemesDat($model, $i, $rcd));
                    while ($bsRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                        if ($bsRow["RECORD"] == "1") {
                            $Row["RECORD"][$ival] = $bsRow["RECORD"];
                        }
                    }
                    $result->free();
                }
            }
        }

        //行動の記録チェックボックス
        for ($i=1; $i<11; $i++) {
            $ival = "3" . sprintf("%02d", $i);
            $check1 = ($Row["RECORD"][$ival] == "1") ? "checked" : "";
            $extra = $check1." id=\"RECORD".$ival."\"";
            $arg["RECORD".$ival]= knjCreateCheckBox($objForm, "RECORD".$ival, "1", $extra, "");
        }

        //特別活動の記録名前表示
        $setData = array();
        foreach ($model->itemArray as $key => $val) {
            $setData["RECORD_NAME".$key] = $val["NAME1"];
        }
        $arg["data"][] = $setData;

        //特別活動の記録チェックボックス
        for ($i=1; $i < get_count($model->itemArray) + 1; $i++) {
            $ival = "4" . sprintf("%02d", $i);
            $count = sprintf("%02d", $i);
            $check1 = ($Row["RECORD"][$ival] == "1") ? "checked" : "";
            if ($model->grade_cd == '01') {
                if ($model->itemArray[$count]["ABBV1"] == '1') {
                    $extra = "disabled ";
                    $extra.= $check1." id=\"RECORD".$ival."\"";
                } else {
                    $extra = $check1." id=\"RECORD".$ival."\"";
                }
            } elseif ($model->grade_cd == '02') {
                if ($model->itemArray[$count]["ABBV2"] == '1') {
                    $extra = "disabled ";
                    $extra.= $check1." id=\"RECORD".$ival."\"";
                } else {
                    $extra = $check1." id=\"RECORD".$ival."\"";
                }
            } elseif ($model->grade_cd == '03') {
                if ($model->itemArray[$count]["ABBV3"] == '1') {
                    $extra = "disabled ";
                    $extra.= $check1." id=\"RECORD".$ival."\"";
                } else {
                    $extra = $check1." id=\"RECORD".$ival."\"";
                }
            } elseif ($model->grade_cd == '04') {
                if ($model->itemArray[$count]["NAMESPARE1"] == '1') {
                    $extra = "disabled ";
                    $extra.= $check1." id=\"RECORD".$ival."\"";
                } else {
                    $extra = $check1." id=\"RECORD".$ival."\"";
                }
            } elseif ($model->grade_cd == '05') {
                if ($model->itemArray[$count]["NAMESPARE2"] == '1') {
                    $extra = "disabled ";
                    $extra.= $check1." id=\"RECORD".$ival."\"";
                } else {
                    $extra = $check1." id=\"RECORD".$ival."\"";
                }
            } elseif ($model->grade_cd == '06') {
                if ($model->itemArray[$count]["NAMESPARE3"] == '1') {
                    $extra = "disabled ";
                    $extra.= $check1." id=\"RECORD".$ival."\"";
                } else {
                    $extra = $check1." id=\"RECORD".$ival."\"";
                }
            } elseif ($model->grade_cd == '') {
                $extra = $check1." id=\"RECORD".$ival."\"";
            }
            $arg["RECORD".$ival]= knjCreateCheckBox($objForm, "RECORD".$ival, "1", $extra, "");
        }

        //特別活動の記録の観点
        $extra = "id=\"SPECIALACTREMARK\" style=\"height:145px;\"";
        $arg["SPECIALACTREMARK"] = knjCreateTextArea($objForm, "SPECIALACTREMARK", 10, 35, "soft", $extra, $row["SPECIALACTREMARK"]);
        knjCreateHidden($objForm, "SPECIALACTREMARK_KETA", 34);
        knjCreateHidden($objForm, "SPECIALACTREMARK_GYO", 10);
        KnjCreateHidden($objForm, "SPECIALACTREMARK_STAT", "statusarea9");

        /****************/
        /*  通知表参照  */
        /****************/
        //プロパティ（un_sanshouReport_P = 1）の時、非表示とする。
        $arg["un_sanshouReport_P"] = ($model->Properties["un_sanshouReport_P"] == '1') ? "" : "on";

        //学期名表示
        for ($i=1; $i<=$model->control["学期数"]; $i++) {
            $arg["SEM_NAME".$i] = $model->control["学期名"][$i];
        }

        if ($model->control["学期数"] == "3") {
            $arg["semester"] = 1;
        }
        $arg["setCol"] = (int)$model->control["学期数"] + 1;

        //行動の記録項目名取得
        if ($model->Properties["useKnja125pBehaviorSemesMst"] == '1') {
            $arg["RECORD_TITLE"] = '生　活　の　記　録';
            $arg["REMARK_TITLE"] = '特 別 活 動 の 記 録';
            $query = knja125pQuery::getBehaviorSemesMst($model);
        } elseif ($model->Properties["useKnja125pBehaviorSemesMst"] == '2') {
            $arg["RECORD_TITLE"] = '行　動　の　記　録';
            $arg["REMARK_TITLE"] = '特別活動・クラブ活動';
            $query = knja125pQuery::getBehaviorSemesMst($model);
        } else {
            $arg["RECORD_TITLE"] = '行　動　の　記　録';
            $arg["REMARK_TITLE"] = '生活･特別活動のようす所見';
            $query = knja125pQuery::getNameMst($model, "D035");
        }
        $result = $db->query($query);
        $itemCnt = 1;
        $rfActItem = array();
        while ($rowItem = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $rfActItem[$rowItem["NAMECD2"]] = $rowItem["NAME1"];
            $itemCnt++;
        }
        if ($model->Properties["useKnja125pBehaviorSemesMst"] == '2') {
            $arg["dis_remark"] = '';
            if ($model->grade_cd > 4) {
                $arg["grade_cd56"] = '1';
            }
        } else {
            $arg["dis_remark"] = '1';
            $itemCnt = 11;
        }

        //行動の記録取得
        $rfActRow = array();
        for ($h=1; $h<$itemCnt; $h++) {
            $rcd = sprintf("%02d", $h);
            for ($i=1; $i<=$model->control["学期数"]; $i++) {
                $result = $db->query(knja125pQuery::getBehaviorSemesDat($model, $i, $rcd));
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $rfActRow["RECORD".$i][$rcd] = $row["RECORD"];
                }
                $result->free();
            }
        }

        //行動の記録
        $referData = array();
        for ($h=1; $h<$itemCnt; $h++) {
            $rcd = sprintf("%02d", $h);
            $referData["REFER_ITEMNAME"] = $rfActItem[$rcd];

            for ($i=1; $i<=$model->control["学期数"]; $i++) {
                if ($model->Properties["knjdBehaviorsd_UseText"] == "1") {
                    $referData["RECORD_VAL".$i] = $rfActRow["RECORD".$i][$rcd];
                } else {
                    $check1 = ($rfActRow["RECORD".$i][$rcd] == "1") ? "checked" : "";
                    $extra = $check1." disabled";
                    $referData["RECORD_VAL".$i] = knjCreateCheckBox($objForm, "RECORD".$i.$rcd, "1", $extra, "");
                }
            }
            $arg["rdata"][] = $referData;
        }

        //通知表所見表示
        $query = knja125pQuery::getHreportRemarkDat($model);
        $result = $db->query($query);
        while ($remarkRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //生活・特別活動のようす所見
            if ($model->specialactremark_gyou == "1") {
                $remarkRow["HREPORTSPECIALACTREMARK"] = knjCreateTextBox($objForm, $remarkRow["HREPORTSPECIALACTREMARK"], "HREPORTSPECIALACTREMARK", (int)$model->specialactremark_moji*2, (int)$model->specialactremark_moji*2, "");
            } else {
                $height = (int)$model->specialactremark_gyou * 13.5 + ((int)$model->specialactremark_gyou -1 ) * 3 + 5;
                $extra = "style=\"height:{$height}px;\" readonly";
                $remarkRow["HREPORTSPECIALACTREMARK"] = KnjCreateTextArea($objForm, "HREPORTSPECIALACTREMARK", $model->specialactremark_gyou, ((int)$model->specialactremark_moji * 2 + 1), "soft", $extra, $remarkRow["HREPORTSPECIALACTREMARK"]);
            }
            $arg["remark"][] = $remarkRow;
        }
        $result->free();

        //特別活動・クラブ活動  ※プロパティだが、雲雀丘専用の意味で作成
        if ($model->Properties["useKnja125pBehaviorSemesMst"] == '2') {
            $query = knja125pQuery::getHreportRemarkDetailDat($model);
            $result = $db->query($query);
            while ($remarkRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $remarkRow["REMARK1_02_01"] = knjCreateTextBox($objForm, $remarkRow["REMARK1_02_01"], "REMARK1_02_01", 23, 22, "");
                $remarkRow["REMARK2_02_01"] = knjCreateTextBox($objForm, $remarkRow["REMARK2_02_01"], "REMARK2_02_01", 23, 22, "");
                $remarkRow["REMARK3_02_01"] = knjCreateTextBox($objForm, $remarkRow["REMARK3_02_01"], "REMARK3_02_01", 27, 26, "");

                $arg["detail_R"] = $remarkRow;
            }
        }

        //学校種別
        $schoolkind = $db->getOne(knja125pQuery::getSchoolKind($model));

        //通知票より読込ボタン
        if ($model->isMusashinohigashi) {
            $extra = "onclick=\"return btn_submit('behavior_semes')\"";
            $arg["button"]["btn_behavior_semes"] = KnjCreateBtn($objForm, "btn_behavior_semes", "通知票より読込", $extra);
        }

        //更新ボタン
        $extra = (AUTHORITY < DEF_UPDATE_RESTRICT || $schoolkind != 'P') ? "disabled" : "onclick=\"return btn_submit('update2')\"";
        $arg["button"]["btn_update"] = KnjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear2')\"";
        $arg["button"]["btn_reset"] = KnjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //戻るボタン
        $extra = "onclick=\"return top.main_frame.right_frame.closeit()\"";
        $arg["button"]["btn_back"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knja125pForm2.html", $arg);
    }
}
