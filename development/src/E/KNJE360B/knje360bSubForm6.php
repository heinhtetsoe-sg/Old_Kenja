<?php

require_once('for_php7.php');

class knje360bSubForm6
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form();

        //フォーム作成
        $arg["start"] = $objForm->get_start("subform6", "POST", "knje360bindex.php", "", "subform6");

        //DB接続
        $db = Query::dbCheckOut();

        // 入試カレンダーの使用フラグ
        $arg["useCollegeExamCalendar"] = "";
        if ($model->Properties["useCollegeExamCalendar"] === '1') {
            $arg["useCollegeExamCalendar"] = "1";
        }
        knjCreateHidden($objForm, "useCollegeExamCalendar", $arg["useCollegeExamCalendar"]);

        //生徒情報
        $info = $db->getRow(knje360bQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];

        //エラー時のセット用データ
        if (isset($model->warning)) {
            $dataTmp = $dataItem = array();
            $seq_array = preg_split("/,/", $model->seq_list);

            foreach ($model->fields as $key => $val) {
                $dataItem[] = $key;

                foreach ($seq_array as $skey) {
                    $dataTmp[$skey][$key] = $model->fields[$key][$skey];
                }
            }
        } else {
            $arg["NOT_WARNING"] = 1;
        }

        //データを取得
        $setval = array();
        $tmpSeq = "";
        if ($model->schregno != "") {
            $query = knje360bQuery::getSubQuery6($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $setval[$row["SEQ"]] = $row;

                //エラー時、データ置き換え
                if (isset($model->warning)) {
                    foreach ($dataItem as $key) {
                        $setval[$row["SEQ"]][$key] = $dataTmp[$row["SEQ"]][$key];
                    }
                }
                $tmpSeq .= ($tmpSeq == "") ? $row["SEQ"] : ",".$row["SEQ"];
            }
            $result->free();
        }
        knjCreateHidden($objForm, "SEQ_LIST", $tmpSeq);

        //調査書発行チェックボックス表示
        if ($model->Properties["KNJE360B_DEF_ISSUE"] == 1) {
            $arg["useISSUE"] = 1;
        }

        //E002,NAMESPARE1に1の項目を配列にセット
        $setShDivArr = array();
        $query       = knje360bQuery::getNameMstNamecd2('E002');
        $result      = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setShDivArr[] = $row["NAMECD2"];
        }
        knjCreateHidden($objForm, "SH_ARR", implode(",", $setShDivArr));

        //データ一覧を表示
        $setData = array();
        $counter = 0;
        foreach ($setval as $seq => $Row) {
            //登録日
            $Row["TOROKU_DATE"]     = str_replace("-", "/", $Row["TOROKU_DATE"]);
            $setData["TOROKU_DATE"] = View::popUpCalendar($objForm, "TOROKU_DATE"."-".$seq, $Row["TOROKU_DATE"]);

            //学校コード
            $setData["SCHOOL_CD"] = $Row["SCHOOL_CD"];
            knjCreateHidden($objForm, "SCHOOL_CD"."-".$seq, $Row["SCHOOL_CD"]);
            //学校名
            $setData["SCHOOL_NAME"] = $Row["SCHOOL_NAME"];

            //学部コード
            $setData["FACULTYCD"] = $Row["FACULTYCD"];
            knjCreateHidden($objForm, "FACULTYCD"."-".$seq, $Row["FACULTYCD"]);
            //学部名
            $setData["FACULTYNAME"] = $Row["FACULTYNAME"];

            //学科コード
            $setData["DEPARTMENTCD"] = $Row["DEPARTMENTCD"];
            knjCreateHidden($objForm, "DEPARTMENTCD"."-".$seq, $Row["DEPARTMENTCD"]);
            //学科名
            $setData["DEPARTMENTNAME"] = $Row["DEPARTMENTNAME"];

            //受験方式
            $extra = "style=\"width:100%;\" onChange=\"changeDispSh(this);\"";
            $query = knje360bQuery::getNameMst('E002');
            $setData["HOWTOEXAM"] = makeCmbReturn($objForm, $arg, $db, $query, "HOWTOEXAM"."-".$seq, $Row["HOWTOEXAM"], $extra, 1, "BLANK");
            $setData["DISP_SEQ"]  = $seq;

            //専併区分コンボ
            if (in_array($Row["HOWTOEXAM"], $setShDivArr)) {
                $setData["SH_DISP"] = " style=\"display:block;\" ";
            } else {
                $setData["SH_DISP"] = " style=\"display:none;\" ";
            }
            $query = knje360bQuery::getNameMst('L006');
            $extra = "";
            $setData["SHDIV"] = makeCmbReturn($objForm, $arg, $db, $query, "SHDIV"."-".$seq, $Row["SHDIV"], $extra, 1, "BLANK");

            //受験結果
            $extra = "style=\"width:100%;\"";
            $query = knje360bQuery::getNameMst('E005');
            $setData["DECISION"] = makeCmbReturn($objForm, $arg, $db, $query, "DECISION"."-".$seq, $Row["DECISION"], $extra, 1, "BLANK");

            //合格短冊匿名希望チェックボックス
            if ($model->Properties["knje360bShowTokumeiCheck"] == "1") {
                $arg["knje360bShowTokumeiCheck"] = "1";
                $extra  = "id=\"TOKUMEI\"";
                $extra .= $Row["TOKUMEI"] == "1" ? " checked " : "";
                $setData["TOKUMEI"] = knjCreateCheckBox($objForm, "TOKUMEI"."-".$seq, "1", $extra);
            }

            //証明書番号取得
            $certif_no = "";
            if (isset($seq)) {
                $query = knje360bQuery::getCertifNo($model, $seq);
                $certif_no = $db->getOne($query);
            }
            //調査書発行チェックボックス
            $extra   = " id=\"ISSUE\" onClick=\"issueControl(this);\"";
            $checked = $Row["ISSUE"] == "1" ? " checked " : "";
            $setData["ISSUE"] = knjCreateCheckBox($objForm, "ISSUE"."-".$seq, "1", $extra.$checked);

            knjCreateHidden($objForm, "ORIGINAL_ISSUE"."_".$seq, $Row["ISSUE"]);

            //進路状況
            $extra = "style=\"width:100%;\"";
            $query = knje360bQuery::getNameMst('E006');
            $setData["PLANSTAT"] = makeCmbReturn($objForm, $arg, $db, $query, "PLANSTAT"."-".$seq, $Row["PLANSTAT"], $extra, 1, "BLANK");

            //募集区分
            $setData["ADVERTISE_DIV"] = $Row["ADVERTISE_DIV"];
            knjCreateHidden($objForm, "ADVERTISE_DIV"."-".$seq, $Row["ADVERTISE_DIV"]);
            //募集区分名称
            $setData["ADVERTISE_NAME"] = $Row["ADVERTISE_NAME"];

            //日程
            $setData["PROGRAM_CD"] = $Row["PROGRAM_CD"];
            knjCreateHidden($objForm, "PROGRAM_CD"."-".$seq, $Row["PROGRAM_CD"]);
            //日程名称
            $setData["PROGRAM_NAME"] = $Row["PROGRAM_NAME"];

            //方式
            $setData["FORM_CD"] = $Row["FORM_CD"];
            knjCreateHidden($objForm, "FORM_CD"."-".$seq, $Row["FORM_CD"]);
            //方式名称
            $setData["FORM_NAME"] = $Row["FORM_NAME"];

            //大分類
            $setData["L_CD"] = $Row["L_CD"];
            knjCreateHidden($objForm, "L_CD"."-".$seq, $Row["L_CD"]);
            //大分類名称
            $setData["L_NAME"] = $Row["L_NAME"];

            //小分類
            $setData["S_CD"] = $Row["S_CD"];
            knjCreateHidden($objForm, "S_CD"."-".$seq, $Row["S_CD"]);
            //小分類名称
            $setData["S_NAME"] = $Row["S_NAME"];

            //締切日（窓口）
            $Row["LIMIT_DATE_WINDOW"]     = str_replace("-", "/", $Row["LIMIT_DATE_WINDOW"]);
            $setData["LIMIT_DATE_WINDOW"] = View::popUpCalendar($objForm, "LIMIT_DATE_WINDOW"."-".$seq, $Row["LIMIT_DATE_WINDOW"]);

            //締切日（郵送）
            $Row["LIMIT_DATE_MAIL"]     = str_replace("-", "/", $Row["LIMIT_DATE_MAIL"]);
            $setData["LIMIT_DATE_MAIL"] = View::popUpCalendar($objForm, "LIMIT_DATE_MAIL"."-".$seq, $Row["LIMIT_DATE_MAIL"]);

            //郵送区分
            $opt   = array();
            $opt[] = array('label' => "",                'value' => "");
            $opt[] = array('label' => "0：未定・その他", 'value' => "0");
            $opt[] = array('label' => "1：消印有効",     'value' => "1");
            $opt[] = array('label' => "2：必着",         'value' => "2");
            $Row["LIMIT_MAIL_DIV"] = ($Row["LIMIT_MAIL_DIV"] == "") ? $opt[0]["value"] : $Row["LIMIT_MAIL_DIV"];
            $extra = "";
            $setData["LIMIT_MAIL_DIV"] = knjCreateCombo($objForm, "LIMIT_MAIL_DIV"."-".$seq, $Row["LIMIT_MAIL_DIV"], $opt, $extra, 1);

            //入試日
            $Row["STAT_DATE1"]     = str_replace("-", "/", $Row["STAT_DATE1"]);
            $setData["STAT_DATE1"] = View::popUpCalendar($objForm, "STAT_DATE1"."-".$seq, $Row["STAT_DATE1"]);

            //合格発表日
            $Row["STAT_DATE3"]     = str_replace("-", "/", $Row["STAT_DATE3"]);
            $setData["STAT_DATE3"] = View::popUpCalendar($objForm, "STAT_DATE3"."-".$seq, $Row["STAT_DATE3"]);

            //受験番号
            $extra = "";
            $setData["EXAMNO"] = knjCreateTextBox($objForm, $Row["EXAMNO"], "EXAMNO"."-".$seq, 10, 10, $extra);

            //備考1
            $extra = "style=\"width:100%;\"";
            $setData["CONTENTEXAM"] = knjCreateTextBox($objForm, $Row["CONTENTEXAM"], "CONTENTEXAM"."-".$seq, 80, 40, $extra);
            //備考2
            $extra = "style=\"height:35px;\"";
            $setData["REASONEXAM"]  = knjCreateTextArea($objForm, "REASONEXAM"."-".$seq, 2, 75, "soft", $extra, $Row["REASONEXAM"]);

            //背景色
            $setData["BGCOLOR"] = ($counter % 2 == 0) ? "#ffffff" : "#cccccc";

            $arg["data"][] = $setData;
            $counter++;
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $tmpSeq);

        //hidden作成
        makeHidden($objForm, $db, $model);

        //DB切断
        Query::dbCheckIn($db);

        if (get_count($model->warning) == 0 && $model->cmd != "subform6_clear") {
            $arg["next"] = "NextStudent(0);";
        } elseif ($model->cmd == "subform6_clear") {
            $arg["next"] = "NextStudent(1);";
        }

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje360bSubForm6.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $tmpSeq)
{
    $disabled = ($model->mode == "grd") ? " disabled" : "";

    //更新ボタンを作成する
    $extra = ($tmpSeq == "") ? "disabled" : "onclick=\"return btn_submit('subform6_update');\"";
    $arg["button"]["btn_update"]  = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //更新後前の生徒へを作成する
    $extra = ($tmpSeq == "") ? "disabled" : "onclick=\"return updateNextStudent('".$model->schregno."', 1);\" style=\"width:130px\"";
    $arg["button"]["btn_up_pre"]  = knjCreateBtn($objForm, "btn_up_pre", "更新後前の{$model->sch_label}へ", $extra.$disabled);
    //更新後次の生徒へを作成する
    $extra = ($tmpSeq == "") ? "disabled" : "onclick=\"return updateNextStudent('".$model->schregno."', 0);\" style=\"width:130px\"";
    $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_next", "更新後次の{$model->sch_label}へ", $extra.$disabled);
    //クリアボタンを作成する
    $extra = "onclick=\"return btn_submit('subform6_clear');\"";
    $arg["button"]["btn_reset"]   = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタン
    $extra = "onclick=\"return btn_submit('edit');\"";
    $arg["button"]["btn_end"]     = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);
    //進路先選択ボタン
    $link  = REQUESTROOT."/E/KNJE360B/knje360bindex.php?cmd=replace6&SCHREGNO=".$model->schregno;
    $extra = "style=\"height:30px;font:bold;\" onclick=\"window.open('$link','_self');\"";
    $arg["button"]["btn_replace"] = KnjCreateBtn($objForm, "btn_replace", "進路先選択", $extra);
}

//hidden作成
function makeHidden(&$objForm, $db, $model)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "cmd");

    $semes = $db->getRow(knje360bQuery::getSemesterMst(), DB_FETCHMODE_ASSOC);
    knjCreateHidden($objForm, "SDATE", str_replace("-", "/", $semes["SDATE"]));
    knjCreateHidden($objForm, "EDATE", str_replace("-", "/", $semes["EDATE"]));
}

//コンボ作成（表内）
function makeCmbReturn(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                        "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $result->free();

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
