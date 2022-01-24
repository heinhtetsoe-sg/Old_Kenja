<?php

require_once('for_php7.php');

class knjd425l_4Form1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg = array();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("subform", "POST", "knjd425l_4index.php", "", "subform");

        $db = Query::dbCheckOut();

        //画面タイトル
        $query = knjd425l_4Query::getHreportGuidanceKindNameHdat($model);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["TITLE"] = $row["KIND_NAME"];

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;

        //生徒の履修科目グループ取得
        if ($model->schregno) {
            $arg["GROUPNAME"] = '履修科目グループ未設定';
            if ($model->schregInfo["GROUPNAME"]) {
                $arg["GROUPNAME"] = '履修科目グループ：'.$model->schregInfo["GROUPNAME"];
            }
            $arg["CONDITION_NAME"] = '';
            if ($model->schregInfo["CONDITION_NAME"]) {
                $arg["CONDITION_NAME"] = '('.$model->schregInfo["CONDITION_NAME"].')';
            }
        }

        //テーブルヘッダ項目名取得(各教科等)
        $arg["title"]["ITEM_REMARK1"] = $model->remarkTitle["1"]["ITEM_REMARK"];
        $arg["title"]["ITEM_REMARK2"] = $model->remarkTitle["2"]["ITEM_REMARK"];
        $arg["title"]["ITEM_REMARK3"] = $model->remarkTitle["3"]["ITEM_REMARK"];

        //テーブルヘッダ項目名取得(総合所見)
        $arg["title"]["ITEM_REMARK"] = $model->itemRemarkTitle;

        //入力項目ラジオ
        $opt = array(1, 2);
        $model->field["INPUT_ITEMS"] = $model->field["INPUT_ITEMS"] ? $model->field["INPUT_ITEMS"] : 1;
        $extra = array("id=\"INPUT_ITEMS1\" aria-label=\"各教科等\" onclick=\"current_cursor('INPUT_ITEMS1');btn_submit('edit');\""
                     , "id=\"INPUT_ITEMS2\" aria-label=\"総合所見\" onclick=\"current_cursor('INPUT_ITEMS2');btn_submit('edit');\"");
        $radioArray =  knjCreateRadio($objForm, "INPUT_ITEMS", $model->field["INPUT_ITEMS"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //学期コンボ
        $query = knjd425l_4Query::getSemester($model);
        $extra = "id=\"SEMESTER\" aria-label=\"学期\" onChange=\"current_cursor('SEMESTER');return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1, "");

        //科目コンボ
        $query = knjd425l_4Query::getSubclass($model);
        $extra = "id=\"SUBCLASSCD\" aria-label=\"教科・科目\" onChange=\"current_cursor('SUBCLASSCD');return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->field["SUBCLASSCD"], "SUBCLASSCD", $extra, 1, "");

        //単元コンボ
        $query = knjd425l_4Query::getUnit($model, $model->field["SUBCLASSCD"], "");
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($row) {
            $extra = "id=\"UNITCD\" aria-label=\"単元\" onChange=\"current_cursor('UNITCD');return btn_submit('edit');\"";
            makeCmb($objForm, $arg, $db, $query, $model->field["UNITCD"], "UNITCD", $extra, 1, "");
        } else {
            $arg["data"]["UNITCD"] = "単元は設定されていません。";
            $model->field["UNITCD"] = "00";
            knjCreateHidden($objForm, "UNITCD", "00");
        }

        //各教科等のとき
        if ($model->field["INPUT_ITEMS"] == 1) {
            $arg["INPUT_ITEMS_1"] = "1";

            $list = array();

            $prevSemester = "";
            $prevSubclasscd = "";

            //科目リスト取得
            $subclassList = array();
            $query = knjd425l_4Query::getSubclass($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $subclassList[$row["VALUE"]] = $row;
            }
            $result->free();
            //単元リスト取得
            $unitList = array();
            $query = knjd425l_4Query::getUnit($model, "", "");
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $unitList[$row["SUBCLASSCD"]][$row["VALUE"]] = $row;
            }
            $result->free();

            //リスト取得
            $dataList = array();
            $query = knjd425l_4Query::getSubclassRemark($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $key = $row["SEMESTER"].'_'.$row["SUBCLASSCD"].'_'.$row["UNITCD"];
        
                $dataList[$key]["SEMESTER"]     = $row["SEMESTER"];
                $dataList[$key]["SEMESTERNAME"] = $row["SEMESTERNAME"];
                $dataList[$key]["SUBCLASSCD"]   = $row["SUBCLASSCD"];
                $dataList[$key]["SUBCLASSNAME"] = $row["SUBCLASSNAME"];
                $dataList[$key]["UNITCD"]       = $row["UNITCD"];
                $dataList[$key]["REMARK_".$row["SEQ"]] = $row["REMARK"];
            }
            $result->free();

            //データセット
            foreach ($dataList as $key => $row) {
                list($semester, $subclass, $unitCd) = preg_split("/_/", $key);

                //初期化
                $list = array();
                //削除用チェックボックス
                $extra = "";
                $list["DEL_CHECK"] = knjCreateCheckBox($objForm, "DEL_CHECK", $key, $extra, "");

                //単元名
                $unitName = "";
                if ($unitList[$row["SUBCLASSCD"]][$row["UNITCD"]]) {
                    $unitName = "(".$unitList[$row["SUBCLASSCD"]][$row["UNITCD"]]["LABEL"].")";
                }
                //リンク有無
                $isLink = false;
                if ($subclassList[$row["SUBCLASSCD"]]) {
                    if ($row["UNITCD"] == "00") {
                        if (get_count($unitList[$row["SUBCLASSCD"]]) == 0) {
                            $isLink = true;
                        }
                    } else {
                        if ($unitList[$row["SUBCLASSCD"]][$row["UNITCD"]]) {
                            $isLink = true;
                        }
                    }
                }
                if ($isLink) {
                    //リンクセット
                    $list["SUBCLASSNAME"] = View::alink(
                        "",
                        $row["SUBCLASSNAME"].$unitName,
                        "onclick =\"current_cursor('SUBCLASSCD');return listSet('{$row["SEMESTER"]}','{$row["SUBCLASSCD"]}','{$row["UNITCD"]}');\" ",
                        array()
                    );
                } else {
                    $list["SUBCLASSNAME"] = $row["SUBCLASSNAME"].$unitName;
                }

                $list["SEMESTER"] = $row["SEMESTER"];
                $list["SEMESTERNAME"] = $row["SEMESTERNAME"];

                //構成元教科を取得
                $query2 = knjd425l_4Query::getTargetClass($model, $row["SUBCLASSCD"]);
                $result2 = $db->query($query2);
                $sep = "";
                $list["TARGET_CLASSNAME"] = "";
                while ($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $list["TARGET_CLASSNAME"] .= $sep.$row2["CLASSNAME"];
                    $sep = "<br>";
                }

                //項目内容設定
                foreach ($model->textLimit as $seq => $textLimit) {
                    $list["REMARK_".$seq] = str_replace(array("\n", "\r\n"), "<br>", $row["REMARK_".$seq]);
                    //テキストボックス用
                    $remarkList[$key]["REMARK_".$seq] = $row["REMARK_".$seq];
                }

                $arg["list"][] = $list;
            }

            //学習内容、目標、指導の経過と評価
            if ($model->cmd != "check") {
                $key = $model->field["SEMESTER"].'_'.$model->field["SUBCLASSCD"].'_'.$model->field["UNITCD"];
                $Row = $remarkList[$key];
            } else {
                $Row = $model->field;
            }

            foreach ($model->textLimit as $seq => $textLimit) {
                $moji = $textLimit["moji"];
                $gyou = $textLimit["gyou"];

                $extra = "id=\"REMARK_".$seq."\" ";
                $arg["data"]["REMARK_".$seq] = knjCreateTextArea($objForm, "REMARK_".$seq, $gyou, ($moji * 2), "", $extra, $Row["REMARK_".$seq]);
                $arg["data"]["REMARK_SIZE_".$seq] .= "<font size=2, color=\"red\">(全角".$moji."文字X".$gyou."行まで)</font>";
            }

        //総合所見のとき
        } else {
            $arg["INPUT_ITEMS_2"] = "1";

            //リスト取得
            $query = knjd425l_4Query::getRemarkList($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //削除用チェックボックス
                $extra = "";
                $value = $row["SEMESTER"];
                $row["DEL_CHECK"] = knjCreateCheckBox($objForm, "DEL_CHECK", $value, $extra, "");

                //リンクセット
                $row["KIND_REMARK"] = View::alink(
                    "",
                    $row["KIND_REMARK"],
                    "onclick =\"current_cursor('SEMESTER');return listSet('{$row["SEMESTER"]}','','');\" ",
                    array()
                );

                $remark = $row["REMARK"];
                $row["REMARK"] = str_replace(array("\n", "\r\n"), "<br>", $row["REMARK"]);

                $arg["list"][] = $row;

                //テキストボックス用
                $remarkList[$row["SEMESTER"]]["REMARK"] = $remark;
            }

            //総合所見
            if ($model->cmd != "check") {
                $Row = $remarkList[$model->field["SEMESTER"]];
            } else {
                $Row = $model->field;
            }
            //総合所見
            $moji = 50;
            $gyou = 25;
            $extra = "id=\"REMARK\" ";
            $arg["data"]["REMARK"] = knjCreateTextArea($objForm, "REMARK".$seq, $gyou, ($moji * 2), "", $extra, $Row["REMARK"]);
            $arg["data"]["REMARK_SIZE"] .= "<font size=2, color=\"red\">(全角".$moji."文字X".$gyou."行まで)</font>";
        }

        /**********/
        /* ボタン */
        /**********/
        //リスト削除
        $extra = "onClick=\"return btn_submit('listdelete')\"";
        $arg["button"]["btn_listdelete"] = knjCreateBtn($objForm, "btn_listdelete", "削 除", $extra);

        //「合わせた指導」教科登録
        $extra = " onclick=\"loadwindow('knjd425l_4index.php?";
        $extra .= "cmd=targetClass&SUBCLASSCD={$model->field["SUBCLASSCD"]}'";
        $extra .= ", event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 550, 350);\" ";
        $arg["button"]["btn_target_class"] = knjCreateBtn($objForm, "btn_target_class", "「合わせた指導」教科登録", $extra);

        //合理的配慮参照
        $extra = " onclick=\"loadwindow('knjd425l_4index.php?";
        $extra .= "cmd=gouri'";
        $extra .= ", event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), (event.clientY - 450) + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 850, 430);\" ";
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
        $link = REQUESTROOT."/D/KNJD425L/knjd425lindex.php?cmd=edit&SEND_PRGID={$model->sendPrgId}&SEND_AUTH={$model->auth}&SCHREGNO={$model->schregno}&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&GRADE={$model->grade}&NAME={$model->name}";
        $extra = "onclick=\"window.open('$link','_self');\"";
        $arg["button"]["btn_end"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "DEL_LIST");

        Query::dbCheckIn($db);

        $arg["IFRAME"] = VIEW::setIframeJs();

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML($model, "knjd425l_4Form1.html", $arg);
    }
}

//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    $value_flg = false;
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"], 'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

function setRemarkTextArea(&$objForm, &$arg, $model, $remark, $seq)
{
    $moji = $model->textLimit[$seq]["moji"];
    $gyou = $model->textLimit[$seq]["gyou"];

    $extra = "id=\"REMARK_".$seq."\"";
    $arg["data"]["REMARK_".$seq] = knjCreateTextArea($objForm, "REMARK_".$seq, $gyou, ($moji * 2), "", $extra, $remark);
    $arg["data"]["REMARK_SIZE_".$seq] .= "<font size=2, color=\"red\">(全角".$moji."文字X".$gyou."行まで)</font>";
    knjCreateHidden($objForm, "REMARK-".$seq."_KETA", ($moji * 2));
    knjCreateHidden($objForm, "REMARK-".$seq."_GYO", $gyou);
    KnjCreateHidden($objForm, "REMARK-".$seq."_STAT", "statusarea_".$seq);
}

function setSortLink(&$arg, &$model)
{
    //ソート表示文字作成
    $order[1] = "▲";
    $order[2] = "▼";
    $model->getSort = $model->getSort ? $model->getSort : "SEMESTER";
    //リストヘッダーソート作成
    $model->sort["SEMESTER"] = $model->sort["SEMESTER"] ? $model->sort["SEMESTER"] : 1;
    $setOrder = $model->getSort == "SEMESTER" ? $order[$model->sort["SEMESTER"]] : "";
    $arg["SRT_SEMESTER"] = "<a href=\"knjd425l_4index.php?cmd=sort&sort=SEMESTER&INPUT_ITEMS={$model->field["INPUT_ITEMS"]}&SUBCLASSCD={$model->field["SUBCLASSCD"]}&SEMESTER={$model->field["SEMESTER"]}\" target=\"_self\" style=\"color:white\">学期{$setOrder}</a>";
}
