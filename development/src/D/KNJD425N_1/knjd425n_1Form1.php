<?php

require_once('for_php7.php');

class knjd425n_1Form1 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd425n_1index.php", "", "edit");

        $db = Query::dbCheckOut();

        //画面タイトル
        $arg["TITLE"] = $db->getOne(knjd425n_1Query::getHreportGuidanceKindNameHdat($model));

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //自立活動区分
        $query = knjd425n_1Query::getZirituKatudouList($model);
        $result = $db->query($query);
        $preSelfTarget = "";
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($preSelfTarget != $row["SELF_TARGET"]) {
                $preSelfTarget = $row["SELF_TARGET"];
                $row["SELF_TARGET_NAME"] = $model->target[$row["SELF_TARGET"]];
                $row["SELF_TARGET_ROWSPAN"] = $row["SELF_TARGET_ROWSPAN"];
            }

            $arg["list1"][] = $row;
        }
        $result->free();

        //テーブルヘッダ
        if (is_array($model->remarkTitle)) {
            foreach ($model->remarkTitle as $kindSeq => $kindRemark) {
                $arg["REMARK_TITLE-".$kindSeq] = $kindRemark;
            }
        }

        //学期コンボ
        if ($model->field["SEMESTER"] == "") $model->field["SEMESTER"] = $model->exp_semester;
        $query = knjd425n_1Query::getSemester($model);
        $extra = "onchange=\"return btn_submit('changeSemester');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1);

        if ($model->isWarning()) {
            $Row = $model->field;
        } else {
            //データ取得
            $query = knjd425n_1Query::getHreportGuidanceSchregRemarkDat($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $Row["REMARK-".$row["SEQ"]] = $row["REMARK"];
            }
            $result->free();
        }

        //重点目標
        setRemarkTextArea($objForm, $arg, $model, $Row, "1");
        makeRemarkButton($objForm, $arg, "1");
        makeLastYearDataBtn($objForm, $arg, "1");

        //課題を設定した理由・実態
        setRemarkTextArea($objForm, $arg, $model, $Row, "2");
        makeRemarkButton($objForm, $arg, "2");
        makeLastYearDataBtn($objForm, $arg, "2");

        //指導内容、支援、評価
        setRemarkTextArea($objForm, $arg, $model, $Row, "3");
        setRemarkTextArea($objForm, $arg, $model, $Row, "4");
        setRemarkTextArea($objForm, $arg, $model, $Row, "5");
        makeRemarkButton($objForm, $arg, "3:4:5");
        makeLastYearDataBtn($objForm, $arg, "3");
        //ソート設定
        setSortLink($arg, $model);
        //指導内容、支援、評価（一覧）
        setRemarkList($arg, $db, $model);

        //次年度の目標（年間まとめ）
        setRemarkTextArea($objForm, $arg, $model, $Row, "7");
        makeRemarkButton($objForm, $arg, "7");

        //引継ぎ事項
        setRemarkTextArea($objForm, $arg, $model, $Row, "8");
        makeRemarkButton($objForm, $arg, "8");

        /**********/
        /* ボタン */
        /**********/
        //「自立活動」区分登録
        $disabled = ($Row["REMARK-1"] == "") ? " disabled" : "";
        $extra = " onclick=\"loadwindow('knjd425n_1index.php?";
        $extra .= "cmd=ziritu'";
        $extra .= ", event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 850, 350);\" ";
        $arg["btn_ziritu"] = knjCreateBtn($objForm, "btn_ziritu", "「自立活動」区分登録", $extra.$disabled);

        //戻る
        $link = REQUESTROOT."/D/KNJD425N/knjd425nindex.php?cmd=edit&SEND_PRGID={$model->getPrgId}&SEND_AUTH={$model->auth}&SCHREGNO={$model->schregno}&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&GRADE={$model->grade}&NAME={$model->name}";
        $extra = "onclick=\"window.open('$link','_self');\"";
        $arg["button"]["btn_end"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "DELETE_SEQ");
        knjCreateHidden($objForm, "UPDATE_SEQ");

        //取消用にREMARKの初期値を用意
        if ($model->isWarning) {
            for ($i = 1; $i <= $model->remarkCnt; $i++) {
                knjCreateHidden($objForm, "INIT_REMARK-".$i, $model->initRow["REMARK-".$i]);
            }
        } else {
            for ($i = 1; $i <= $model->remarkCnt; $i++) {
                knjCreateHidden($objForm, "INIT_REMARK-".$i, $Row["REMARK-".$i]);
            }
        }

        Query::dbCheckIn($db);

        $arg["IFRAME"] = VIEW::setIframeJs();

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjd425n_1Form1.html", $arg);
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

function setRemarkTextArea(&$objForm, &$arg, $model, $Row, $seq) {
    $moji = $model->textLimit[$seq]["moji"];
    $gyou = $model->textLimit[$seq]["gyou"];

    //「自立活動」区分登録にデータがなければ指導内容、支援、評価は非活性
    $disabled = "";
    if ($seq == "3" || $seq == "4" || $seq == "5") {
        if (!isset($arg["list1"])) {
            $disabled = " disabled";
        }
    }

    $extra = "id=\"REMARK-".$seq."\"";
    $arg["REMARK-".$seq] = knjCreateTextArea($objForm, "REMARK-".$seq, $gyou, ($moji * 2), "", $extra.$disabled, $Row["REMARK-".$seq]);
    $arg["EXTFMT-".$seq] .= "<font size=2, color=\"red\">(全角".$moji."文字X".$gyou."行まで)</font>";
    knjCreateHidden($objForm, "REMARK-".$seq."_KETA", ($moji * 2));
    knjCreateHidden($objForm, "REMARK-".$seq."_GYO", $gyou);
    KnjCreateHidden($objForm, "REMARK-".$seq."_STAT", "statusarea_".$seq);
}

function makeRemarkButton(&$objForm, &$arg, $seq) {
    //更新
    $extra = "onClick=\"return btn_submit('update', '".$seq."')\"";
    $arg["btn_update-".$seq] = knjCreateBtn($objForm, "btn_update-".$seq, "更 新", $extra);

    //削除
    $extra = "onClick=\"return btn_submit('delete', '".$seq."')\"";
    $arg["btn_delete-".$seq] = knjCreateBtn($objForm, "btn_reset-".$seq, "削 除", $extra);

    //取消
    $extra = "onClick=\"return resetRemark('".$seq."')\"";
    $arg["btn_reset-".$seq] = knjCreateBtn($objForm, "btn_reset-".$seq, "取 消", $extra);
}

function makeLastYearDataBtn(&$objForm, &$arg, $seq) {
    $extra = " onclick=\"loadwindow('knjd425n_1index.php?";
    $extra .= "cmd=lastYearData".$seq."'";
    $extra .= ", event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 850, 350);\" ";
    $arg["btn_lastYearData-".$seq] = knjCreateBtn($objForm, "btn_lastYearData-".$seq, "前年度データ参照", $extra);
}

//指導内容、支援、評価の一覧を設定
function setRemarkList (&$arg, &$db, $model) {
    $query = knjd425n_1Query::getRemarkList($model);
    $result = $db->query($query);
    $list = array();
    $prevSemester = "";
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (!isset($list["SEMESTERNAME"])) {
            $list["SEMESTERNAME"] = $row["SEMESTERNAME"];
        }
        if ($row["SEMESTER"] != $prevSemester) {
            if ($prevSemester != "") {
                $arg["list2"][] = $list;
                $list = array();
            }
            $prevSemester = $row["SEMESTER"];
        }
        $list["REMARK-".$row["SEQ"]] = str_replace(array("\r", "\r\n"), "<br>", $row["REMARK"]);
    }
    $arg["list2"][] = $list;
}

function setSortLink(&$arg, &$model) {
    //ソート表示文字作成
    $order[1] = "▲";
    $order[2] = "▼";
    $model->getSort = $model->getSort ? $model->getSort : "SEMESTER";
    //リストヘッダーソート作成
    $model->sort["SEMESTER"] = $model->sort["SEMESTER"] ? $model->sort["SEMESTER"] : 1;
    $setOrder = $model->getSort == "SEMESTER" ? $order[$model->sort["SEMESTER"]] : "";
    $arg["SRT_SEMESTER"] = "<a href=\"knjd425n_1index.php?cmd=sort&sort=SEMESTER\" target=\"_self\" style=\"color:white\">学期{$setOrder}</a>";
}
?>
