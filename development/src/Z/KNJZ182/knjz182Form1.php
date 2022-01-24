<?php

require_once('for_php7.php');

class knjz182form1 {
    function main(&$model) {
        $objForm = new form;
        //フォーム作成
        $arg["start"] = $objForm->get_start("main", "POST", "knjz182index.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        /**********/
        /* ラジオ */
        /**********/
        //区分ラジオ 1:学年 2:クラス 3:コース
        $model->field["DIV"] = $model->field["DIV"] ? $model->field["DIV"] : '1';
        $opt_div = array(1, 2, 3);
        $extra = "onClick=\"return btn_submit('change')\"";
        $label = array($extra." id=\"DIV1\"", $extra." id=\"DIV2\"", $extra." id=\"DIV3\"");
        $radioArray = knjCreateRadio($objForm, "DIV", $model->field["DIV"], $label, $opt_div, get_count($opt_div));
        foreach($radioArray as $key => $val) $arg["sepa"][$key] = $val;
        //表示切替(1:学年 2:クラス 3:コース)
        if ($model->field["DIV"] == '2') {
            $arg["show2"] = $model->field["DIV"];
        } else if ($model->field["DIV"] == '3') {
            $arg["show3"] = $model->field["DIV"];
        } else {
        }

        /**********/
        /* コンボ */
        /**********/
        //参照
        //学期コンボ
        $extra = "style=\"width:70px\" onChange=\"return btn_submit('change');\" ";
        $query = knjz182Query::getSemester();
        makeCmb($objForm, $arg, $db, $query, $model->field["PRE_SEMESTER"], "PRE_SEMESTER", $extra, 1);
        //成績種別コンボ
        $extra = "style=\"width:120px\" onChange=\"return btn_submit('change');\" ";
        $query = knjz182Query::getTestkindcd($model->field["PRE_SEMESTER"]);
        makeCmb($objForm, $arg, $db, $query, $model->field["PRE_TESTKINDCD"], "PRE_TESTKINDCD", $extra, 1);
        //対象
        //学期コンボ
        $extra = "style=\"width:70px\" onChange=\"return btn_submit('change');\" ";
        $query = knjz182Query::getSemester();
        makeCmb($objForm, $arg, $db, $query, $model->field["SEMESTER"], "SEMESTER", $extra, 1);
        //成績種別コンボ
        $extra = "style=\"width:120px\" onChange=\"return btn_submit('change');\" ";
        $query = knjz182Query::getTestkindcd($model->field["SEMESTER"]);
        makeCmb($objForm, $arg, $db, $query, $model->field["TESTKINDCD"], "TESTKINDCD", $extra, 1);
        //学年コンボ
        $extra = "onChange=\"return btn_submit('change');\" ";
        $query = knjz182Query::getGrade();
        makeCmb($objForm, $arg, $db, $query, $model->field["GRADE"], "GRADE", $extra, 1);
        //クラスコンボ
        $query = knjz182Query::getHrClass($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["HR_CLASS"], "HR_CLASS", $extra, 1);
        //課程学科コースコンボ
        $query = knjz182Query::getCourse($model);
        makeCmb($objForm, $arg, $db, $query, $model->field["COURSE"], "COURSE", $extra, 1);
        //科目コンボ
        if ($model->Properties["useCurriculumcd"] == '1') {
            $query = knjz182Query::getSchoolKind($model);
            $model->schoolkind = $db->getOne($query);
        }
        $query = knjz182Query::getSubclasscd($model, $model->schoolkind);
        makeCmb($objForm, $arg, $db, $query, $model->field["SUBCLASSCD"], "SUBCLASSCD", $extra, 1, "BLANK");

        /********************/
        /* テキストボックス */
        /********************/
        //評定段階数の取得
        $assesslevelcnt = $db->getOne(knjz182Query::getAssessLevelCnt($model)); //評定段階数の初期値
        if ($model->cmd == 'level') {
            $assesslevelcnt = $model->field["ASSESSLEVELCNT"];
        }
        //評定段階数
        $extra = " STYLE=\"text-align: right\"; onblur=\"this.value=toInteger(this.value)\"";
        $arg["sepa"]["ASSESSLEVELCNT"] = knjCreateTextBox($objForm, $assesslevelcnt, "ASSESSLEVELCNT", 2, 2, $extra);
        //平均点・最低点・最高点を取得(計算用)
        $recordAverage = array();
        if (0 < $assesslevelcnt) {
            $recordAverage = $db->getRow(knjz182Query::getRecordAverage($model), DB_FETCHMODE_ASSOC);
        }
        //満点(考査)
        $perfect = "";
        if (0 < $assesslevelcnt) {
            $perfect = $db->getOne(knjz182Query::getPerfect($model));
        }
        if ($perfect == "") $perfect = 100;

        /**********/
        /* リスト */
        /**********/
        makeList($objForm, $arg, $db, $model, $assesslevelcnt);

        /**********/
        /* ボタン */
        /**********/
        makeBtn($objForm, $arg, $model, $assesslevelcnt);

        /**********/
        /* hidden */
        /**********/
        makeHidden($objForm, $model, $recordAverage, $perfect, $db);

        //DB切断
        Query::dbCheckIn($db);
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz182Form1.html", $arg);
    }
}
/********************************************** 以下関数 **********************************************/
//リスト作成
function makeList(&$objForm, &$arg, $db, $model, $assesslevelcnt) {
    //警告メッセージを表示しない場合
    if (!isset($model->warning)) {
        $query = knjz182Query::getList($model);
        $result = $db->query($query);
    } else {
        $row =& $model->field;
        $result = "";
    }

    for ($i = $assesslevelcnt; $i > 0; $i--) {
        if ($result != "") {
            if ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //レコードを連想配列のまま配列$arg[data]に追加していく。 
                array_walk($row, "htmlspecialchars_array");

                //評定区分1と2は小数点を取り除く。
                $ar =explode(".",$row["ASSESSLOW"]);
                $row["ASSESSLOW"] = $ar[0];
                $ar =explode(".",$row["ASSESSHIGH"]);
                $row["ASSESSHIGH"] = $ar[0];
            }
        }
        $row["ASSESSLEVEL"] = $i; //TODO

        //下限
        $lowVal = "";
        if ($model->cmd !="level") {
            if ($result != "") {
                $lowVal = $row["ASSESSLOW"];
            } else {
                $lowVal = $row["ASSESSLOW".$i];
            }
        }
        if ($row["ASSESSLEVEL"] == "1") {
            $extra = "onblur=\"this.value=toInteger(this.value);\" STYLE=\"text-align: right\"";
        } else {
            $extra = "onblur=\"this.value=toInteger(this.value);isNumb(this,".($row["ASSESSLEVEL"] - 1).",'ELSE');\" STYLE=\"text-align: right\"";
        }
        $row["ASSESSLOWTEXT"] = knjCreateTextBox($objForm, $lowVal, "ASSESSLOW".$row["ASSESSLEVEL"], 4, 3, $extra);

        //上限
        if ($row["ASSESSLEVEL"] == $assesslevelcnt) {
            $highVal = "";
            if ($model->cmd != "level") {
                if ($result != "") {
                    $highVal = $row["ASSESSHIGH"];
                } else {
                    $highVal = $row["ASSESSHIGH".$i];
                }
            }
            $extra = "onblur=\"this.value=toInteger(this.value);\" STYLE=\"text-align: right\"";
            $row["ASSESSHIGHTEXT"] = knjCreateTextBox($objForm, $highVal, "ASSESSHIGH".$row["ASSESSLEVEL"], 4, 3, $extra);
            /**********
            $row["ASSESSHIGHTEXT"]  = "<span id=\"strID";
            $row["ASSESSHIGHTEXT"] .= $row["ASSESSLEVEL"];
            $row["ASSESSHIGHTEXT"] .= "\">";
            $row["ASSESSHIGHTEXT"] .= 100;
            $row["ASSESSHIGHTEXT"] .= "</span>";
            **********/

        } else {
            $row["ASSESSHIGHTEXT"]  = "<span id=\"strID";
            $row["ASSESSHIGHTEXT"] .= $row["ASSESSLEVEL"];
            $row["ASSESSHIGHTEXT"] .= "\">";
            if ($result != "") {
                if ($model->cmd != "level") {
                    $row["ASSESSHIGHTEXT"] .= $row["ASSESSHIGH"];
                }
            } else {
                $row["ASSESSHIGHTEXT"] .= ($row["ASSESSLOW".($i + 1)] - 1);
            }
            $row["ASSESSHIGHTEXT"] .= "</span>";
        }

        $arg["data"][] = $row;
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "",
                       "value" => "");
    }

    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array("label" => $row["LABEL"],
                       "value" => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    if ($name == "SEMESTER") {
        $value = ($value && $value_flg) ? $value : CTRL_SEMESTER;
    } else {
        $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    }

    $arg["sepa"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $assesslevelcnt) {
    //コピーボタン
    $extra = "style=\"width:150px\" onclick=\"return btn_submit('copy');\" ";
    $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "左のデータをコピー", $extra);
    //照会ボタン
    $url = REQUESTROOT."/Z/KNJZ182/knjz182index.php?cmd=inquiry&SEMESTER={$model->field["SEMESTER"]}&TESTKINDCD={$model->field["TESTKINDCD"]}&DIV={$model->field["DIV"]}&GRADE={$model->field["GRADE"]}";
    $extra = "onClick=\" wopen('{$url}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["button"]["btn_inquiry"] = knjCreateBtn($objForm, "btn_inquiry", "照 会", $extra);
    //確定ボタン
    $extra = "onclick=\"return level({$assesslevelcnt});\" ";
    $arg["button"]["btn_kakutei"] = knjCreateBtn($objForm, "btn_kakutei", "確 定", $extra);
    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\" ";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //取消ボタン
    $extra = "onclick=\"return btn_submit('clear');\" ";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "onclick=\"return closeWin();\" ";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
    //計算ボタン
    $extra = "onclick=\"return keisan({$assesslevelcnt});\" ";
    $arg["button"]["btn_keisan"] = knjCreateBtn($objForm, "btn_keisan", "計 算", $extra);
}
//hidden作成
function makeHidden(&$objForm, $model, $recordAverage, $perfect, $db) {
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "AVG",       $recordAverage["AVG"]);
    knjCreateHidden($objForm, "LOWSCORE",  $recordAverage["LOWSCORE"]);
    knjCreateHidden($objForm, "HIGHSCORE", $recordAverage["HIGHSCORE"]);
    knjCreateHidden($objForm, "PERFECT",   $perfect);
    $queryMoto = knjz182Query::getCntCopyQuery($model, $model->field["PRE_SEMESTER"].$model->field["PRE_TESTKINDCD"]);
    $querySaki = knjz182Query::getCntCopyQuery($model, $model->field["SEMESTER"].$model->field["TESTKINDCD"]);
    knjCreateHidden($objForm, "COPY_MOTO_CNT", $db->getOne($queryMoto));
    knjCreateHidden($objForm, "COPY_SAKI_CNT", $db->getOne($querySaki));
}
?>
