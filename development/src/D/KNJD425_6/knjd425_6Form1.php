<?php

require_once('for_php7.php');

class knjd425_6Form1 {
    function main(&$model) {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd425_6index.php", "", "edit");

        $db = Query::dbCheckOut();

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;
        //グループ情報
        $getGroupRow = array();
        $getGroupRow = $db->getRow(knjd425_6Query::getViewGradeKindSchreg($model, "set"), DB_FETCHMODE_ASSOC);
        if ($model->schregno) {
            $getGroupName = $db->getOne(knjd425_6Query::getGroupcd($model, $getGroupRow));
            if ($getGroupName) {
                $arg["GROUP_NAME"] = '履修科目グループ:'.$getGroupName;
            } else {
                $arg["GROUP_NAME"] = '履修科目グループ未設定';
            }
            $getConditionName = $db->getOne(knjd425_6Query::getConditionName($model, $getGroupRow["CONDITION"]));
            $arg["CONDITION_NAME"] = ($getConditionName) ? '('.$getConditionName.')' : "";
        }

        // Add by HPA for title start 2020/02/03
        $htmlTitle = "\"".$arg["SCHREGNO"]."".$arg["NAME"]."".$arg["CONDITION_NAME"]."".$arg["GROUP_NAME"]."の情報画面\"";
        echo "<script>
        var title= $htmlTitle;
        </script>";
        // Add by HPA for title end 2020/02/20

        $query = knjd425_6Query::getGuidancePattern($model);
        $result = $db->query($query);
        $model->schregInfo = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->schregInfo = $row;
        }
        $result->free();

        //項目名称セット
        $model->itemNameArr = array();
        $query = knjd425_6Query::getItemName($model);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            for ($i=1; $i <= $model->maxRemarkCnt; $i++) {
                if ($row["ITEM_REMARK".$i] != '') {
                    $row["ITEM_REMARK"] = $row["ITEM_REMARK".$i];
                    $arg["koumoku"][] = $row;
                    $model->itemNameArr[$i] = $row["ITEM_REMARK"];
                }
            }
        }

        //Bパターンフラグ
        $query = knjd425_6Query::getNameMstPattern($model, $model->schregInfo["GUIDANCE_PATTERN"], 'NAMESPARE2');
        $model->patternB = $db->getOne($query) == '1' ? true: false;

        //学期コンボ
        if ($model->patternB) {
            $arg["PATTERN_B"] = '1';
            $query = knjd425_6Query::getSemester($model);
            $extra = "onChange=\"return btn_submit('edit');\"";
            makeCmb($objForm, $arg, $db, $query, $model->semester, "SEMESTER", $extra, 1, "");
        }

        //ソート表示文字作成
        $order[1] = "▲";
        $order[2] = "▼";
        $model->getSort = $model->getSort ? $model->getSort : "SRT_SEMESTER";

        //リストヘッダーソート作成
        $model->sort["SRT_SEMESTER"] = $model->sort["SRT_SEMESTER"] ? $model->sort["SRT_SEMESTER"] : 1;
        $setOrder = $model->getSort == "SRT_SEMESTER" ? $order[$model->sort["SRT_SEMESTER"]] : "";
        /* Edit by HPA for PC-talker 読み start 2020/02/03 */
        $arg["ID"] = $setOrder == "▲" ? "sortAsc" : "sortDes";
        $arg["LABEL"] = $setOrder == "▲" ? "リストを学期によって昇順で並べ替えました" : "リストを学期によって降順で並べ替えました";

        $SRT_SEMESTER = "<a href=\"knjd425_6index.php?cmd=sort&sort=SRT_SEMESTER\" onclick=\"current_cursor('{$arg["ID"]}');\" id =\"{$arg["ID"]}\" target=\"_self\" STYLE=\"color:white\">学期{$setOrder}</a>";
        $arg["SRT_SEMESTER"] = $SRT_SEMESTER;
        /* Edit by HPA for PC-talker 読み end 2020/02/20 */

        /************/
        /* 履歴一覧 */
        /************/
        makeList($arg, $db, $model);

        //科目コンボ
        $query = knjd425_6Query::getSubclass($model);
        /* Edit by HPA for current_cursor start 2020/02/03 */
        $extra = "id = \"SUBCLASSCD\" aria-label = \"教科・科目\" onChange=\"current_cursor('SUBCLASSCD');return btn_submit('edit');\"";
        makeCmb($objForm, $arg, $db, $query, $model->subclasscd, "SUBCLASSCD", $extra, 1);
        /* Edit by HPA for current_cursor end 2020/02/20 */

        //単元がセットされているか
        $get_div = $db->getOne(knjd425_6Query::getUnitAimDiv($model));
        $model->unit_aim_div = ($get_div == "1") ? "1" : "0";

        //単元コンボ
        $query = knjd425_6Query::getUnit($model);
        if ($model->unit_aim_div == "1") {
            $extra = "onChange=\"return btn_submit('edit');\"";
            makeCmb($objForm, $arg, $db, $query, $model->unitcd, "UNITCD", $extra, 1);
        }

        //警告メッセージを表示しない場合
        $warning = false;
        if ((isset($model->schregno) && !isset($model->warning) && $model->cmd != "set") || !isset($model->schregno)) {
            $result = $db->query(knjd425_6Query::getHreportGuidanceSchregSubclassDat($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $Row["REMARK_{$row["SEQ"]}"] = $row["REMARK"];
            }
        } else {
            $Row =& $model->field;
            if (isset($model->warning)) $warning = true;
        }

        //パターン取得
        $query = knjd425_6Query::getNameMstPattern($model, $model->schregInfo["GUIDANCE_PATTERN"], 'NAMESPARE1');
        $tmp = $db->getOne($query);
        $model->printPattern = substr($tmp, 1, 1);

        $outcnt = 0;
        //テキストセット
        if ($model->schregInfo["GUIDANCE_PATTERN"] && $model->subclasscd) {
            foreach($model->itemNameArr as $nameCd2 => $name1) {
                $tmpData = array();

                //textArea
                $setName = "REMARK_{$nameCd2}";
                /* Edit by HPA for PC-talker 読み start 2020/02/03 */
                $moji = $model->paternInfo[$model->printPattern][$nameCd2]["MOJI"];
                $gyou = $model->paternInfo[$model->printPattern][$nameCd2]["GYOU"];
                $extra = "aria-label = \"$name1 全角{$moji}文字X{$gyou}行まで\" id=\"{$setName}\"";
                $tmpData["REMARK"] = knjCreateTextArea($objForm, $setName, "10", $moji * 2, "", $extra, $Row[$setName]);
                $tmpData["EXTFMT"] = "<BR><font size=2, color=\"red\">(全角{$moji}文字X{$gyou}行まで)</font><span id=\"statusarea".$outcnt."\" style=\"color:blue\"></span>";
                /* Edit by HPA for PC-talker 読み end 2020/02/20 */
                knjCreateHidden($objForm, "{$setName}_KETA", ($moji * 2));
                knjCreateHidden($objForm, "{$setName}_GYO", $gyou);
                KnjCreateHidden($objForm, "{$setName}_STAT", "statusarea".$outcnt);
                $outcnt++;

                $arg["data2"][] = $tmpData;
            }
        }

        $cnt = get_count($model->itemNameArr) + 1;
        $arg["COLSPAN"] = ($cnt > 0) ? "colspan=\"{$cnt}\"" : "";

        /**********/
        /* ボタン */
        /**********/
        //更新
        /* Edit by HPA for PC-talker 読み start 2020/02/03 */
        $extra = "id = \"update\" aria-label = \"更新\" onclick=\"current_cursor('update');return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        $extra = "id = \"delete\" aria-label = \"削除\" onclick=\"current_cursor('delete');return btn_submit('delete');\"";
        $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
        //取消
        $extra = "id = \"clear\" aria-label = \"取消\" onclick=\"current_cursor('clear');return btn_submit('clear');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //戻る
        $link = REQUESTROOT."/D/KNJD425/knjd425index.php?cmd=edit&SEND_PRGID={$model->getPrgId}&SEND_AUTH={$model->auth}&SCHREGNO={$model->schregno}&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&GRADE={$model->grade}&NAME={$model->name}";
        $extra = "aria-label = \"戻る\" onclick=\"window.open('$link','_self');\"";
        /* Edit by HPA for PC-talker 読み end 2020/02/20 */
        $arg["button"]["btn_end"] = KnjCreateBtn($objForm, "btn_back", "戻 る", $extra);

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
//        $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "印 刷", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);

        //印刷用
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "KNJD425_6");
        knjCreateHidden($objForm, "YEAR", $model->exp_year);
        knjCreateHidden($objForm, "useGradeKindCompGroupSemester", $model->Properties["useGradeKindCompGroupSemester"]);
        knjCreateHidden($objForm, "SELECT_GHR");

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML5($model, "knjd425_6Form1.html", $arg);
    }
}

//履歴一覧
function makeList(&$arg, $db, $model) {

    $setArr = $remarkArr = array();
    $query = knjd425_6Query::getList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $setKey = $row["SEMESTER"].'-'.$row["SET_SUBCLASSCD"].'-'.$row["UNITCD"];

        $remarkArr["SEMESTERNAME"]     = $row["SEMESTERNAME"];
        $remarkArr[$row["SEQ"]]        = $row["REMARK"];
        $remarkArr["UNITCD"]           = $row["UNITCD"];
        $remarkArr["SET_SUBCLASSCD"]   = $row["SET_SUBCLASSCD"];
        $remarkArr["SET_SUBCLASSNAME"] = $row["SET_SUBCLASSNAME"];
        $setArr[$setKey] = $remarkArr;
    }

    //データセット
    $setData = array();
    foreach ($setArr as $key => $remarkArr) {
        list($semester, $classCd, $schoolKind, $curriculumCd, $subclassCd, $unitCd) = preg_split("/-/", $key);

        //初期化
        $setData = array();

        //学期名
        $setData["SEMESTERNAME"] = $remarkArr["SEMESTERNAME"];

        //リンクセット
        /* Edit by HPA for current_cursor start 2020/02/03 */
        $setData["SET_SUBCLASSNAME"] = "<a href=\"knjd425_6index.php?cmd=list_set&SEMESTER={$semester}&SUBCLASSCD={$remarkArr["SET_SUBCLASSCD"]}&UNITCD={$remarkArr["UNITCD"]}\" onclick =\"current_cursor('SUBCLASSCD');\">{$remarkArr["SET_SUBCLASSNAME"]}</a>";
        /* Edit by HPA for current_cursor end 2020/02/20 */

        //項目セット
        $miniData = array();
        foreach ($model->itemNameArr as $key2 => $val2) {
            $miniData["REMARK"] = $remarkArr[$key2];
            $setData["koumoku"][] = $miniData;
        }

        $arg["list"][] = $setData;
    }
    return;
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
?>
