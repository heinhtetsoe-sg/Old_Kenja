<?php

require_once('for_php7.php');

class knjd420bForm1 {
    function main(&$model) {
        $objForm = new form;
        // Add by HPA for title 2020-01-20 start
        if($model->schregno != "" && $model->name != ""){
            $arg["TITLE"] = "".$model->schregno."". $model->name."の情報画面";
        }else{
            $arg["TITLE"] = "右情報画面";
        }
        // Add by HPA for title 2020-01-31 end
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd420bindex.php", "", "edit");

        $db = Query::dbCheckOut();

        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;
        //グループ情報
        $getGroupRow = array();
        $getGroupRow = $db->getRow(knjd420bQuery::getViewGradeKindSchreg($model, "set"), DB_FETCHMODE_ASSOC);
        if ($model->schregno) {
            $getGroupName = $db->getOne(knjd420bQuery::getGroupcd($model, $getGroupRow));
            if ($getGroupName) {
                $arg["GROUP_NAME"] = '履修科目グループ:'.$getGroupName;
            } else {
                $arg["GROUP_NAME"] = '履修科目グループ未設定';
            }
            $getConditionName = $db->getOne(knjd420bQuery::getConditionName($model, $getGroupRow["CONDITION"]));
            $arg["CONDITION_NAME"] = ($getConditionName) ? '('.$getConditionName.')' : "";
        }

        $query = knjd420bQuery::getGuidancePattern($model);
        $result = $db->query($query);
        $model->schregInfo = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->schregInfo = $row;
        }
        $result->free();

        //項目名称セット
        $model->itemNameArr = array();
        $query = knjd420bQuery::getItemName($model);
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
        $query = knjd420bQuery::getNameMstPattern($model, $model->schregInfo["GUIDANCE_PATTERN"], 'NAMESPARE2');
        $model->patternB = $db->getOne($query) == '1' ? true: false;

        //学期コンボ
        if ($model->patternB) {
            $arg["PATTERN_B"] = '1';
            $query = knjd420bQuery::getSemester($model);
            $extra = "onChange=\"return btn_submit('edit');\"";
            makeCmb($objForm, $arg, $db, $query, $model->semester, "SEMESTER", $extra, 1, "");
        }

        //科目コンボ
        $query = knjd420bQuery::getSubclass($model);
        /* Edit by HPA for PC-talker 読み and current_cursor start 2020/01/20 */
        $extra = "id = \"SUBCLASSCD\" aria-label = \"教科・科目\" onChange=\"current_cursor('SUBCLASSCD');return btn_submit('edit');\"";
        /* Edit by HPA for PC-talker 読み and current_cursor end 2020/01/31 */
        makeCmb($objForm, $arg, $db, $query, $model->subclasscd, "SUBCLASSCD", $extra, 1);

        // ソート順変更の場合、ソートを入れ替える
        if ($model->cmd == "sort") {
            if ($model->sort == "SUBCLASSCD") {
                $model->sort = "SEMESTER";
            } else {
                $model->sort = "SUBCLASSCD";
            }
        }
        // 学期 ソート順
        /* Edit by HPA for current_cursor start 2020/01/20 */
        if ($model->sort == "SUBCLASSCD") {
           $arg["LABEL"] = "リストを学期によって降順で並べ替えました";
            $arg["SEMESTER_TITLE"] = "<a style=\"color: white;\" href=\"#\" id = \"sortDes\" onclick=\"current_cursor('sortDes');return btn_submit('sort')\">学期 ▼</a>";
        } else {
          $arg["LABEL"] = "リストを学期によって昇順で並べ替えました";
            $arg["SEMESTER_TITLE"] = "<a style=\"color: white;\" href=\"#\" id = \"sortAsc\" onclick=\"current_cursor('sortAsc');return btn_submit('sort')\">学期 ▲</a>";
        }
        /* Edit by HPA for current_cursor end 2020/01/31 */
        knjCreateHidden($objForm, "SORT", $model->sort);

        /************/
        /* 履歴一覧 */
        /************/
        makeList($arg, $db, $model);

        //単元がセットされているか
        $get_div = $db->getOne(knjd420bQuery::getUnitAimDiv($model));
        $model->unit_aim_div = ($get_div == "1") ? "1" : "0";

        //単元コンボ
        $query = knjd420bQuery::getUnit($model);
        if ($model->unit_aim_div == "1") {
            /* Edit by HPA for PC-talker 読み and current_cursor start 2020/01/20 */
            $extra = "id = \"UNITCD\" aria-label = \"単元\" onChange=\"current_cursor('UNITCD');return btn_submit('edit');\"";
            /* Edit by HPA for PC-talker 読み and current_cursor end 2020/01/31 */
            makeCmb($objForm, $arg, $db, $query, $model->unitcd, "UNITCD", $extra, 1);
        }

        //警告メッセージを表示しない場合
        $warning = false;
        if ((isset($model->schregno) && !isset($model->warning) && $model->cmd != "set") || !isset($model->schregno)) {
            $result = $db->query(knjd420bQuery::getHreportTokushiSchregSubclassDat($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $Row["REMARK_{$row["SEQ"]}"] = $row["REMARK"];
            }
        } else {
            $Row =& $model->field;
            if (isset($model->warning)) $warning = true;
        }

        //パターン取得
        $query = knjd420bQuery::getNameMstPattern($model, $model->schregInfo["GUIDANCE_PATTERN"], 'NAMESPARE1');
        $tmp = $db->getOne($query);
        $model->printPattern = substr($tmp, 1, 1);

        $outcnt = 0;
        //テキストセット
        if ($model->schregInfo["GUIDANCE_PATTERN"] && $model->subclasscd) {
            foreach($model->itemNameArr as $nameCd2 => $name1) {
                $tmpData = array();

                //textArea
                $setName = "REMARK_{$nameCd2}";
                $moji = $model->paternInfo[$model->printPattern][$nameCd2]["MOJI"];
                $gyou = $model->paternInfo[$model->printPattern][$nameCd2]["GYOU"];
                /* Edit by HPA for PC-talker 読み start 2020/01/20 */
                $extra = "aria-label = \"$name1 全角{$moji}文字X{$gyou}行まで\" id =\"{$setName}\"";
                /* Edit by HPA for PC-talker 読み end 2020/01/31 */
                $tmpData["REMARK"] = knjCreateTextArea($objForm, $setName, "10", $moji * 2, "", $extra, $Row[$setName]);
                $tmpData["EXTFMT"] = "<BR><font size=2, color=\"red\">(全角{$moji}文字X{$gyou}行まで)</font><span id= \"statusarea".$outcnt."\" style=\"color:blue\">残り文字数</span>";
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
        //全科目取込
        /* Edit by HPA for current_cursor start 2020/01/20 */
        $extra = "id= \"allcopy\" onclick=\"current_cursor('allcopy');return btn_submit('allcopy');\"";
        /* Edit by HPA for current_cursor end 2020/01/31 */
        $arg["button"]["btn_allcopy"] = knjCreateBtn($objForm, "btn_update", "全科目取込", $extra);
        //指定科目取込
        /* Edit by HPA for current_cursor start 2020/01/20 */
        $extra = "id = \"copy\" onclick=\"current_cursor('copy');return btn_submit('copy');\"";
        /* Edit by HPA for current_cursor end 2020/01/31 */
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_update", "指定科目取込", $extra);

        //更新
        /* Edit by HPA for PC-talker 読み and current_cursor start 2020/01/20 */
        $extra = "id = \"update\" aria-label = \"更新\" onclick=\"current_cursor('update');return btn_submit('update');\"";
        /* Edit by HPA for PC-talker 読み and current_cursor end 2020/01/31 */
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
        //削除
        /* Edit by HPA for PC-talker 読み start and current_cursor 2020/01/20 */
        $extra = "id = \"delete\" aria-label = \"削除\" onclick=\"current_cursor('delete');return btn_submit('delete');\"";
        /* Edit by HPA for PC-talker 読み and current_cursor end 2020/01/31 */
        $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
        //取消
        /* Edit by HPA for PC-talker 読み and current_cursor start 2020/01/20 */
        $extra = "id = \"clear\" aria-label = \"取消\" onclick=\"current_cursor('clear');return btn_submit('clear');\"";
        /* Edit by HPA for PC-talker 読み and current_cursor end 2020/01/31 */
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了
        /* Edit by HPA for PC-talker 読み start 2020/01/20 */
        $extra = " aria-label = \"終了\" onclick=\"closeWin();\"";
        /* Edit by HPA for PC-talker 読み end 2020/01/31 */
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //印刷ボタン
        $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "SEMESTERCOPY", $model->semestercopy);
        knjCreateHidden($objForm, "SUBCLASSCOPY", $model->subclasscdcopy);

        //印刷用
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);
        knjCreateHidden($objForm, "CTRL_DATE", CTRL_DATE);
        knjCreateHidden($objForm, "PRGID", "knjd420b");
        knjCreateHidden($objForm, "YEAR", $model->exp_year);
        knjCreateHidden($objForm, "useGradeKindCompGroupSemester", $model->Properties["useGradeKindCompGroupSemester"]);
        knjCreateHidden($objForm, "SELECT_GHR");

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        View::toHTML5($model, "knjd420bForm1.html", $arg);
    }
}

//履歴一覧
function makeList(&$arg, $db, &$model) {

    $model->semestercopy = "";
    $model->subclasscdcopy = "";

    $setArr = $remarkArr = array();
    $query = knjd420bQuery::getList($model);
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $setKey = $row["SEMESTER"].'-'.$row["SET_SUBCLASSCD"].'-'.$row["UNITCD"];

        $remarkArr["SEMESTERNAME"]     = $row["SEMESTERNAME"];
        $remarkArr[$row["SEQ"]]        = $row["REMARK"];
        $remarkArr["UNITCD"]           = $row["UNITCD"];
        $remarkArr["SET_SUBCLASSCD"]   = $row["SET_SUBCLASSCD"];
        $remarkArr["SET_SUBCLASSNAME"] = $row["SET_SUBCLASSNAME"];
        $setArr[$setKey] = $remarkArr;

        // データ登録済みフラグ設定
        if ($model->semester == $row["SEMESTER"]) {
            $model->semestercopy = "1";
            if ($model->subclasscd == $row["SET_SUBCLASSCD"]) {
                $model->subclasscdcopy = "1";
            }
        }
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
        /* Edit by HPA for current_cursor start 2020/01/20 */
        $setData["SET_SUBCLASSNAME"] = "<a href=\"knjd420bindex.php?cmd=list_set&SEMESTER={$semester}&SUBCLASSCD={$remarkArr["SET_SUBCLASSCD"]}&UNITCD={$remarkArr["UNITCD"]}&SORT={$model->sort}\" onclick =\"current_cursor('SUBCLASSCD');\" >{$remarkArr["SET_SUBCLASSNAME"]}</a>";
        /* Edit by HPA for current_cursor end 2020/01/31 */

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
