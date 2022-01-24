<?php

require_once('for_php7.php');

class knjd425n_3Form1 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd425n_3index.php", "", "edit");

        $db = Query::dbCheckOut();

        //画面タイトル
        $arg["TITLE"] = $db->getOne(knjd425n_3Query::getHreportGuidanceKindNameHdat($model));

        //生徒情報 //
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;
        //グループ情報
        $getGroupRow = array();
        $getGroupRow = $db->getRow(knjd425N_3Query::getViewGradeKindSchreg($model, "set"), DB_FETCHMODE_ASSOC);
        if ($model->schregno) {
            $getGroupName = $db->getOne(knjd425N_3Query::getGroupcd($model, $getGroupRow));
            if ($getGroupName) {
                $arg["GROUP_NAME"] = '履修科目グループ:'.$getGroupName;
            } else {
                $arg["GROUP_NAME"] = '履修科目グループ未設定';
            }
            $getConditionName = $db->getOne(knjd425N_3Query::getConditionName($model, $getGroupRow["CONDITION"]));
            $arg["CONDITION_NAME"] = ($getConditionName) ? '('.$getConditionName.')' : "";
        }

        //テーブルヘッダ（学習内容、目標、指導の経過と評価、総合所見）
        for ($i = 1; $i <= $model->remarkCnt; $i++) {
            $kindSeq = sprintf("%03d", $i);
            $arg["data"]["REMARK_TITLE-".$kindSeq] = $model->remarkTitle[$kindSeq];
        }

        //入力項目ラジオ
        $opt = array(1, 2);
        $model->field["INPUT_ITEMS"] = $model->field["INPUT_ITEMS"] ? $model->field["INPUT_ITEMS"] : 1;
        $extra = array("id=\"INPUT_ITEMS1\" onclick=\"btn_submit('changeRadio');\"", "id=\"INPUT_ITEMS2\" onclick=\"btn_submit('changeRadio');\"");
        $radioArray =  knjCreateRadio($objForm, "INPUT_ITEMS", $model->field["INPUT_ITEMS"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //学期コンボ
        if ($model->field["SEMESTER"] == "") $model->field["SEMESTER"] = $model->exp_semester;
        $query = knjd425n_3Query::getSemester($model);
        $extra = "onchange=\"return btn_submit('changeSemester');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1);

        //ソート設定
        setSortLink($arg, $model);

        //各教科等のとき
        if ($model->field["INPUT_ITEMS"] == 1) {
            $arg["KAKUKYOUKANADO"] = true;

            //リスト取得
            $query = knjd425n_3Query::getSubclassRemark($model);
            $result = $db->query($query);
            $list = array();
            $prevSemester = "";
            $prevSubclasscd = "";
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($prevSemester != $row["SEMESTER"] || $prevSubclasscd != $row["SUBCLASSCD"]) {
                    if ($prevSemester != "" && $prevSubclasscd != "") {
                        $arg["list"][] = $list;
                        $list = array();
                    }

                    //削除用チェックボックス
                    $extra = "";
                    $value = $row["SUBCLASSCD"]."_".$row["SEMESTER"];
                    $list["DEL_CHECK"] = knjCreateCheckBox($objForm, "DEL_CHECK", $value, $extra, $multi = "");

                    $list["SEMESTERNAME"] = $row["SEMESTERNAME"];
                    $list["SUBCLASSNAME"] = $row["SUBCLASSNAME"];

                    //構成元教科を取得
                    $query2 = knjd425n_3Query::getTargetClass($model, $row["SUBCLASSCD"]);
                    $result2 = $db->query($query2);
                    $sep = "";
                    $list["TARGET_CLASSNAME"] = "";
                    while ($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
                        $list["TARGET_CLASSNAME"] .= $sep.$row2["CLASSNAME"];
                        $sep = "<br>";
                    }

                    $list["SEMESTER"] = $row["SEMESTER"];

                    $prevSemester = $row["SEMESTER"];
                    $prevSubclasscd = $row["SUBCLASSCD"];
                }
                $list["REMARK-".$row["SEQ"]] = str_replace(array("\n", "\r\n"), "<br>", $row["REMARK"]);

                //テキストボックス用
                $remarkList[$row["SEMESTER"]][$row["SUBCLASSCD"]]["REMARK-".$row["SEQ"]] = $row["REMARK"];
            }
            if ($list) $arg["list"][] = $list;

            //教科・科目コンボ
            $query = knjd425n_3Query::getSubclasscdCombo($model);
            $extra = "onChange=\"btn_submit('changeSubclasscd');\"";
            makeCmb($objForm, $arg, $db, $query, $model->field["SUBCLASSCD"], "SUBCLASSCD", $extra, 1);

            //学習内容、目標、指導の経過と評価
            $Row = $model->isWarning() ? $model->field : $remarkList[$model->field["SEMESTER"]][$model->field["SUBCLASSCD"]];
            for ($i=1; $i <= 3; $i++) {
                setRemarkTextArea($objForm, $arg, $model, $Row["REMARK-".$i], $i);
            }

        //総合所見のとき
        } else {
            $arg["SOUGOUSYOKEN"] = true;

            //リスト取得
            $query = knjd425n_3Query::getRemarkList($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //削除用チェックボックス
                $extra = "";
                $value = $row["SEMESTER"];
                $row["DEL_CHECK"] = knjCreateCheckBox($objForm, "DEL_CHECK", $value, $extra, $multi = "");

                $row["KIND_REMARK"] = $row["KIND_REMARK"];
                $row["REMARK-".$row["SEQ"]] = $row["REMARK"];

                $arg["list"][] = $row;

                //テキストボックス用
                $remarkList[$row["SEMESTER"]]["REMARK-".$row["SEQ"]] = $row["REMARK"];
            }

            //総合所見
            $Row = $model->isWarning() ? $model->field : $remarkList[$model->field["SEMESTER"]];
            setRemarkTextArea($objForm, $arg, $model, $Row["REMARK-4"], 4);
        }

        /**********/
        /* ボタン */
        /**********/
        //リスト削除
        $extra = "onClick=\"return btn_submit('listdelete')\"";
        $arg["button"]["btn_listdelete"] = knjCreateBtn($objForm, "btn_listdelete", "削 除", $extra);

        //「合わせた指導」教科登録
        $extra = " onclick=\"loadwindow('knjd425n_3index.php?";
        $extra .= "cmd=targetClass&SUBCLASSCD={$model->field["SUBCLASSCD"]}'";
        $extra .= ", event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 550, 350);\" ";
        $arg["button"]["btn_target_class"] = knjCreateBtn($objForm, "btn_target_class", "「合わせた指導」教科登録", $extra);

        //合理的配慮参照
        $extra = " onclick=\"loadwindow('knjd425n_3index.php?";
        $extra .= "cmd=gouri'";
        $extra .= ", event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 850, 430);\" ";
        $arg["button"]["btn_gouri"] = knjCreateBtn($objForm, "btn_gouri", "合理的配慮参照", $extra);

        //更新
        $extra = "onClick=\"return btn_submit('update')\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        $extra = "onClick=\"return btn_submit('delete')\"";
        $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
        //取消
        $extra = "onClick=\"return btn_submit('reset')\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //戻る
        $link = REQUESTROOT."/D/KNJD425N/knjd425nindex.php?cmd=edit&SEND_PRGID={$model->getPrgId}&SEND_AUTH={$model->auth}&SCHREGNO={$model->schregno}&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&GRADE={$model->grade}&NAME={$model->name}";
        $extra = "onclick=\"window.open('$link','_self');\"";
        $arg["button"]["btn_end"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "DEL_LIST");

        Query::dbCheckIn($db);

        $arg["IFRAME"] = VIEW::setIframeJs();

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjd425n_3Form1.html", $arg);
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
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

function setRemarkTextArea(&$objForm, &$arg, $model, $remark, $seq) {
    $moji = $model->textLimit[$seq]["moji"];
    $gyou = $model->textLimit[$seq]["gyou"];

    $extra = "id=\"REMARK-".$seq."\"";
    $arg["data"]["REMARK-".$seq] = knjCreateTextArea($objForm, "REMARK-".$seq, $gyou, ($moji * 2), "", $extra, $remark);
    $arg["data"]["EXTFMT-".$seq] .= "<font size=2, color=\"red\">(全角".$moji."文字X".$gyou."行まで)</font>";
    knjCreateHidden($objForm, "REMARK-".$seq."_KETA", ($moji * 2));
    knjCreateHidden($objForm, "REMARK-".$seq."_GYO", $gyou);
    KnjCreateHidden($objForm, "REMARK-".$seq."_STAT", "statusarea_".$seq);
}

function setSortLink(&$arg, &$model) {
    //ソート表示文字作成
    $order[1] = "▲";
    $order[2] = "▼";
    $model->getSort = $model->getSort ? $model->getSort : "SEMESTER";
    //リストヘッダーソート作成
    $model->sort["SEMESTER"] = $model->sort["SEMESTER"] ? $model->sort["SEMESTER"] : 1;
    $setOrder = $model->getSort == "SEMESTER" ? $order[$model->sort["SEMESTER"]] : "";
    $arg["SRT_SEMESTER"] = "<a href=\"knjd425n_3index.php?cmd=sort&sort=SEMESTER&INPUT_ITEMS={$model->field["INPUT_ITEMS"]}&SUBCLASSCD={$model->field["SUBCLASSCD"]}&SEMESTER={$model->field["SEMESTER"]}\" target=\"_self\" style=\"color:white\">学期{$setOrder}</a>";
}
?>
