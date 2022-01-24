<?php

require_once('for_php7.php');

class knjc033dForm2 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("knjc033dForm2", "POST", "knjc033dindex.php", "", "knjc033dForm2");

        //DB接続
        $db = Query::dbCheckOut();

        //日付表示
        $weekday = array( "日", "月", "火", "水", "木", "金", "土" );
        $w = $weekday[date("w", strtotime(str_replace("/","-",$model->cntl_dt_key)))]."曜日";
        list ($y, $m ,$d) = explode('-', str_replace("/","-",$model->cntl_dt_key));
        $arg["DATE"] =  $y.'年 '.$m.'月 '.$d.'日（'.$w.'）';

        //年組表示
        $arg["HR_NAME"] = $db->getOne(knjc033dQuery::getGradeHrClass($model->grade, $model->hr_class, $model));

        //使用する出欠コード
        $use_di_cd = array();
        foreach($model->use_di_cd as $key => $val) {
            $use_di_cd[] = $key;
        }

        //事前チェック（出欠コード）
        if ($db->getOne(knjc033dQuery::getC001("cnt", $use_di_cd, $model)) == 0) {
            $arg["jscript"] = "PreCheck('（出欠コード）');";
        }

        //出欠コードボタン
        $c001 = array();
        $query = knjc033dQuery::getC001("list", $use_di_cd, $model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $color = ($model->di_cd == $row["NAMECD2"]) ? "color:red;" : "";
            $bgcolor = ($model->use_di_cd[$row["NAMECD2"]][1] == "1") ? "background:#C0FFFF;" : (($model->use_di_cd[$row["NAMECD2"]][1] == "2") ? "background:#FFFF00;" : (($model->use_di_cd[$row["NAMECD2"]][1] == "3") ? "background:#FFC0CB;" : ""));
            $extra  = "onclick=\" setDICD(this, {$row["NAMECD2"]});\"";
            $extra .= " style=\"height:40px;width:120px;font-weight:bold;font-size:12pt;{$color}{$bgcolor}\"";
            $row["btn_di_cd"] = knjCreateBtn($objForm, "btn_di_cd".$row["NAMECD2"], $row["NAME1"], $extra);

            $arg["c001"][] = $row;

            $c001[$row["NAMECD2"]] = $row["ABBV1"];
            knjCreateHidden($objForm, "C001_".$row["NAMECD2"], $row["ABBV1"]);
        }
        $result->free();

        //クラス人数取得
        $sch_cnt = $db->getOne(knjc033dQuery::getStudent($model, "cnt"));

        //生徒ボタン
        $schregno = "";
        $sch = array();
        $cnt = 1;
        $cnt1 = $cnt2 = $cnt3 = 0;
        $query = knjc033dQuery::getStudent($model, "list");
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

            $schregno .= ($schregno) ? ','.$row["SCHREGNO"] : $row["SCHREGNO"];

            //出欠情報取得
            $di_cd1 = $di_cd2 = $di_cd3 = "";
            $query2 = knjc033dQuery::getDI_CD($row["SCHREGNO"], $use_di_cd, $model);
            $result2 = $db->query($query2);
            while ($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($model->use_di_cd[$row2["DI_CD"]][1] == "1") {
                    $di_cd1 = $row2["DI_CD"];
                } else if ($model->use_di_cd[$row2["DI_CD"]][1] == "2") {
                    $di_cd2 = $row2["DI_CD"];
                } else if ($model->use_di_cd[$row2["DI_CD"]][1] == "3") {
                    $di_cd3 = $row2["DI_CD"];
                }
            }
            $result2->free();

            $attend1 = ($model->cmd == "form2_chg") ? $model->field[$row["SCHREGNO"]]["ATTEND1"] : $di_cd1;
            $attend2 = ($model->cmd == "form2_chg") ? $model->field[$row["SCHREGNO"]]["ATTEND2"] : $di_cd2;
            $attend3 = ($model->cmd == "form2_chg") ? $model->field[$row["SCHREGNO"]]["ATTEND3"] : $di_cd3;

            //hidden作成（出欠情報）
            knjCreateHidden($objForm, "ATTEND1_".$row["SCHREGNO"], $attend1);
            knjCreateHidden($objForm, "ATTEND2_".$row["SCHREGNO"], $attend2);
            knjCreateHidden($objForm, "ATTEND3_".$row["SCHREGNO"], $attend3);

            //生徒ボタンの使用有無
            if ($model->di_cd) {
                if ($row["IDOU"] == "") {
                    $disabled = "";
                } else if ($model->auth == DEF_UPDATABLE) {
                    $disabled = "";
                } else {
                    $disabled = " disabled";
                }
            } else {
                $disabled = " disabled";
            }

            //extra
            $extraB  = "onclick=\" setATTEND(this, '{$row["SCHREGNO"]}');\"";
            $extraB .= " style=\"height:35px;width:150px;font-weight:bold;\"".$disabled;
            $extraT1 = "style=\"height:25px;width:50px;font-size:11pt;text-align:center;background-color:#C0FFFF;\" readonly";
            $extraT2 = "style=\"height:25px;width:50px;font-size:11pt;text-align:center;background-color:#FFFF00;\" readonly";
            $extraT3 = "style=\"height:25px;width:50px;font-size:11pt;text-align:center;background-color:#FFC0CB;\" readonly";

            if ((int)$sch_cnt / 3 > 15) {
                if ($cnt <= (int)$sch_cnt / 3) {
                    //左列
                    $sch[$cnt1]["ATTENDNO1"] = $row["ATTENDNO"].'番';
                    $sch[$cnt1]["btn_sch1"]  = knjCreateBtn($objForm, "SCH_".$row["SCHREGNO"], $row["NAME_SHOW"], $extraB);
                    $sch[$cnt1]["txt_sch11"] = knjCreateTextBox($objForm, $c001[$attend1], "MARK1_".$row["SCHREGNO"], 3, 3, $extraT1);
                    $sch[$cnt1]["txt_sch12"] = knjCreateTextBox($objForm, $c001[$attend2], "MARK2_".$row["SCHREGNO"], 3, 3, $extraT2);
                    $sch[$cnt1]["txt_sch13"] = knjCreateTextBox($objForm, $c001[$attend3], "MARK3_".$row["SCHREGNO"], 3, 3, $extraT3);
                    $cnt1++;
                } else if ($cnt <= (int)$sch_cnt / 3 * 2) {
                    //中央列
                    $sch[$cnt2]["ATTENDNO2"] = $row["ATTENDNO"].'番';
                    $sch[$cnt2]["btn_sch2"]  = knjCreateBtn($objForm, "SCH_".$row["SCHREGNO"], $row["NAME_SHOW"], $extraB);
                    $sch[$cnt2]["txt_sch21"] = knjCreateTextBox($objForm, $c001[$attend1], "MARK1_".$row["SCHREGNO"], 3, 3, $extraT1);
                    $sch[$cnt2]["txt_sch22"] = knjCreateTextBox($objForm, $c001[$attend2], "MARK2_".$row["SCHREGNO"], 3, 3, $extraT2);
                    $sch[$cnt2]["txt_sch23"] = knjCreateTextBox($objForm, $c001[$attend3], "MARK3_".$row["SCHREGNO"], 3, 3, $extraT3);
                    $cnt2++;
                } else {
                    //右列
                    $sch[$cnt3]["ATTENDNO3"] = $row["ATTENDNO"].'番';
                    $sch[$cnt3]["btn_sch3"]  = knjCreateBtn($objForm, "SCH_".$row["SCHREGNO"], $row["NAME_SHOW"], $extraB);
                    $sch[$cnt3]["txt_sch31"] = knjCreateTextBox($objForm, $c001[$attend1], "MARK1_".$row["SCHREGNO"], 3, 3, $extraT1);
                    $sch[$cnt3]["txt_sch32"] = knjCreateTextBox($objForm, $c001[$attend2], "MARK2_".$row["SCHREGNO"], 3, 3, $extraT2);
                    $sch[$cnt3]["txt_sch33"] = knjCreateTextBox($objForm, $c001[$attend3], "MARK3_".$row["SCHREGNO"], 3, 3, $extraT3);
                    $cnt3++;
                }
            } else {
                if ($cnt <= 15) {
                    //左列
                    $sch[$cnt1]["ATTENDNO1"] = $row["ATTENDNO"].'番';
                    $sch[$cnt1]["btn_sch1"]  = knjCreateBtn($objForm, "SCH_".$row["SCHREGNO"], $row["NAME_SHOW"], $extraB);
                    $sch[$cnt1]["txt_sch11"] = knjCreateTextBox($objForm, $c001[$attend1], "MARK1_".$row["SCHREGNO"], 3, 3, $extraT1);
                    $sch[$cnt1]["txt_sch12"] = knjCreateTextBox($objForm, $c001[$attend2], "MARK2_".$row["SCHREGNO"], 3, 3, $extraT2);
                    $sch[$cnt1]["txt_sch13"] = knjCreateTextBox($objForm, $c001[$attend3], "MARK3_".$row["SCHREGNO"], 3, 3, $extraT3);
                    $cnt1++;
                } else if ($cnt <= 30) {
                    //中央列
                    $sch[$cnt2]["ATTENDNO2"] = $row["ATTENDNO"].'番';
                    $sch[$cnt2]["btn_sch2"]  = knjCreateBtn($objForm, "SCH_".$row["SCHREGNO"], $row["NAME_SHOW"], $extraB);
                    $sch[$cnt2]["txt_sch21"] = knjCreateTextBox($objForm, $c001[$attend1], "MARK1_".$row["SCHREGNO"], 3, 3, $extraT1);
                    $sch[$cnt2]["txt_sch22"] = knjCreateTextBox($objForm, $c001[$attend2], "MARK2_".$row["SCHREGNO"], 3, 3, $extraT2);
                    $sch[$cnt2]["txt_sch23"] = knjCreateTextBox($objForm, $c001[$attend3], "MARK3_".$row["SCHREGNO"], 3, 3, $extraT3);
                    $cnt2++;
                } else {
                    //右列
                    $sch[$cnt3]["ATTENDNO3"] = $row["ATTENDNO"].'番';
                    $sch[$cnt3]["btn_sch3"]  = knjCreateBtn($objForm, "SCH_".$row["SCHREGNO"], $row["NAME_SHOW"], $extraB);
                    $sch[$cnt3]["txt_sch31"] = knjCreateTextBox($objForm, $c001[$attend1], "MARK1_".$row["SCHREGNO"], 3, 3, $extraT1);
                    $sch[$cnt3]["txt_sch32"] = knjCreateTextBox($objForm, $c001[$attend2], "MARK2_".$row["SCHREGNO"], 3, 3, $extraT2);
                    $sch[$cnt3]["txt_sch33"] = knjCreateTextBox($objForm, $c001[$attend3], "MARK3_".$row["SCHREGNO"], 3, 3, $extraT3);
                    $cnt3++;
                }
            }
            $cnt++;
        }
        $result->free();

        //データをセット
        for ($i=0; $i < $cnt1; $i++) {
            $arg["sch"][] = $sch[$i];
        }

        //全てクリアボタン（削除）
        $extra  = ($model->auth < DEF_UPDATE_RESTRICT) ? "disabled" : "onclick=\"return btn_submit('form2_delete');\"";
        $extra .= " style=\"height:40px;width:110px;font-weight:bold;font-size:12pt;\"";
        $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "全てクリア", $extra);

        //キャンセルボタン
        $extra  = "onclick=\"return btn_submit('form2_clear');\"";
        $extra .= " style=\"height:40px;width:110px;font-weight:bold;font-size:12pt;\"";
        $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "キャンセル", $extra);

        //確定ボタン
        $extra  = ($model->auth < DEF_UPDATE_RESTRICT) ? "disabled" : "onclick=\"return btn_submit('form2_update');\"";
        $extra .= " style=\"height:40px;width:110px;font-weight:bold;font-size:12pt;\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "確 定", $extra);

        //戻るボタン
        $extra  = "onclick=\"return btn_submit('main');\"";
        $extra .= " style=\"height:30px;width:70px;font-size:10pt;\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //管理者コントロール
        $admin_control = $db->getOne(knjc033dQuery::checkAdminControlDat($model, $model->grade));

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "DI_CD");
        knjCreateHidden($objForm, "DI_CD_SET", $model->di_cd);
        knjCreateHidden($objForm, "DI_CD_SET_POSITION", $model->use_di_cd[$model->di_cd][1]);
        knjCreateHidden($objForm, "SCHREGNO", $schregno);
        knjCreateHidden($objForm, "ADMIN_CONTROL", $admin_control);
        knjCreateHidden($objForm, "useDateText", $model->useDateText);
        knjCreateHidden($objForm, "DATE", $model->cntl_dt_key);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjc033dForm2.html", $arg);
    }
}
?>
