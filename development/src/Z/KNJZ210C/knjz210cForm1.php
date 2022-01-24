<?php

require_once('for_php7.php');

class knjz210cform1
{
    function main(&$model)
    {
        $objForm = new form;
        //フォーム作成
        $arg["start"]   = $objForm->get_start("main", "POST", "knjz210cindex.php", "", "main");
        //DB接続
        $db = Query::dbCheckOut();

        $query = knjz210cQuery::getZ010();
        $model->Z010 = $db->getOne($query);

        //処理年度
        $arg["YEAR"] = CTRL_YEAR;

        //評定区分作成
        $query = knjz210cQuery::getAssesscd($model);
        $extra = "onChange=\"btn_submit('main')\";";
        makeCmb($objForm, $arg, $db, $query, "ASSESSCD", $model->field["ASSESSCD"], $extra, 1);

        //校種コンボ作成
        $query = knjz210cQuery::getNameMstA023($model);
        $extra = "onChange=\"btn_submit('main')\";";
        makeCmb($objForm, $arg, $db, $query, "SCHOOL_KIND", $model->field["SCHOOL_KIND"], $extra, 1);
        $schoolKindName = $db->getOne(knjz210cQuery::getNameMstA023($model, "ABBV1"));

        //科目コンボ作成
        $query = knjz210cQuery::getSubclassMst($model, $schoolKindName);
        $extra = "onchange=\"return btn_submit('main')\"";
        makeCmb($objForm, $arg, $db, $query, "SUBCLASSCD", $model->field["SUBCLASSCD"], $extra, 1);

        //コピー科目コンボ作成
        $query = knjz210cQuery::getSubclassMst($model, $schoolKindName, "COPY");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, "COPY_SUBCLASSCD", $model->field["COPY_SUBCLASSCD"], $extra, 1);

        $schoolMstSchoolKind = knjz210cQuery::hasTableField($db, "SCHOOL_MST", "SCHOOL_KIND") ? $model->field["SCHOOL_KIND"] : "";
        $schoolMstSemesAssesscd = $db->getOne(knjz210cQuery::getSchoolMstSemesAssesscd($schoolMstSchoolKind));
        if ($schoolMstSemesAssesscd == "2") {
            $model->setMax = 10;
        } else {
            $model->setMax = 100;
        }

        $model->testCnt = $db->getOne(knjz210cQuery::getTestItemMstCountflgNewSdivCount($model->field["SCHOOL_KIND"]));
        if ($model->testCnt == 0) {
            $model->testCnt = 1;
        }

        //警告メッセージを表示しない場合
        if (!isset($model->warning)) {
            $row = array();
            for ($ti = 1; $ti <= $model->testCnt; $ti++) {
                for ($i = 1; $i <= $model->assessCnt; $i++) {
                    $query = knjz210cQuery::selectQuery($model, $ti, $i);
                    $row1 = $db->getRow($query, DB_FETCHMODE_ASSOC);
                    if (is_array($row1)) {
                        //レコードを連想配列のまま配列$arg[data]に追加していく。 
                        array_walk($row1, "htmlspecialchars_array");
                        $row["ASSESSMARK".$i] = $row1["ASSESSMARK"];
                        $row["ASSESSLOW".$i."_".$ti] = $row1["ASSESSLOW"];
                        $row["ASSESSHIGH".$i."_".$ti] = $row1["ASSESSHIGH"];
                        //小数点を取り除く
                        if ($row["MIN_ASSESSLOW_".$ti] == "") {
                            $ar = explode(".",$row1["ASSESSLOW"]);
                            $row["MIN_ASSESSLOW_".$ti] = $ar[0];
                        }
                        $ar = explode(".",$row1["ASSESSHIGH"]);
                        $row["MAX_ASSESSHIGH_".$ti] = $ar[0];
                    } else {
                        $row["ASSESSLOW".$i."_".$ti] = "";
                        $row["ASSESSHIGH".$i."_".$ti] = "";
                    }
                }
            }

        } else {
            $row =& $model->field;
            $result = "";
        }

        for ($ti = 1; $ti <= $model->testCnt; $ti++) {
            $arg["data"]["TESTCOUNT"][] = array("COUNT" => $ti, "TITLE" => "第".$ti."回");
        }
        $arg["data"]["FOOTER_SPAN"] = $model->testCnt * 2 + 2;

        $chan = array();
        for ($i = 1; $i <= $model->assessCnt; $i++) {

            $tr = array();
            $tr[] = $i;
                
            //記号部分作成
            $name = "ASSESSMARK".$i;
            $idx = $i;
            $t = "";
            $t .= "<input type=\"text\" name=\"".$name."\" value=\"".$row[$name]."\" ";
            $t .= " size=\"8\" maxlength=\"6\" tabindex=\"".$idx."\" style=\"text-align: right\"> ";
            $tr[] = $t;
            $chan[$idx] = $name;

            for ($ti = 1; $ti <= $model->testCnt; $ti++) {

                //textの有無設定(下限部分)
                $name = "ASSESSLOW".$i."_".$ti;
                $idx = $ti * $model->assessCnt + $i;
                $t = "";
                $t .= "<input type=\"text\" name=\"".$name."\" value=\"".$row[$name]."\" ";
                $ext = " size=\"4\" maxlength=\"3\" tabindex=\"".$idx."\" onblur=\"isNumb(this,".$i.",".$ti.",'ELSE');\" style=\"text-align: right\"> ";
                $chan[$idx] = $name;
                $t .= $ext;
                $tr[] = $t;

                //上限部分作成
                //上限値の時
                $t = "";
                if ($i == $model->assessCnt) {
                    $t = $model->setMax * $ti;
                } else {
                    $t .= "<span id=\"assessHigh".$i."_".$ti."\">";
                    if ($result != "") {
                        $t .= $row["MAX_ASSESSHIGH"];
                    } else if ($result == ""){
                        if ($row["ASSESSLOW".($i + 1)."_".$ti] - 1 > 0) {
                            $t .= $row["ASSESSLOW".($i + 1)."_".$ti] - 1;
                        } else {
                            $t .= "";
                        }
                    }
                    $t .= "</span>";
                }
                $tr[] = $t;
            }
                        
            $arg["data"]["ROWS"][] = array("ITEM" => $tr);
        }
        ksort($chan);
        knjCreateHidden($objForm, "enterChan", implode(",", $chan));
        
        //ボタン作成
        makeBtn($objForm, $arg, $db, $model);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        
        //DB切断
        Query::dbCheckIn($db);
        
        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjz210cForm1.html", $arg);
    }       
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if($blank != "") $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {

        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg[$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $db, &$model) {
    //コピーボタンを作成する
    $extra = "onclick=\"return btn_submit('copy');\"";
    if (0 == knjz210cQuery::cntYearOrder($db, $model, CTRL_YEAR - 1)) {
        $extra .= " disabled=\"disabled\" ";
        $arg["BTN_COPY_TOOLTIP"] = " class=\"tooltip\" data-tooltip=\"前年度データがありません\"";
    } else if (1 <= knjz210cQuery::cntYearOrder($db, $model, CTRL_YEAR)) {
        $extra .= " disabled=\"disabled\" ";
        $arg["BTN_COPY_TOOLTIP"] = " class=\"tooltip\" data-tooltip=\"今年度データが存在します\"";
    }
    $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "前年度コピー", $extra);
    //科目コピーボタンを作成する
    $extra = "onclick=\"return btn_submit('copy_subclass');\"";
    if ($model->field["COPY_SUBCLASSCD"] == "") {
        $extra = " disabled=\"disabled\"";
        $arg["BTN_COPY_SUBCLASS_TOOLTIP"] = " class=\"tooltip\" data-tooltip=\"設定済み科目がありません\"";
    }
    $arg["button"]["btn_copy_subclass"] = knjCreateBtn($objForm, "btn_copy_subclass", "左の科目をコピー", $extra);
    $tabidx = $model->assessCnt * (1 + $model->testCnt);
    $tabidx++;
    //更新ボタンを作成する
    $extra = "onclick=\"return btn_submit('update');\" tabindex=\"".$tabidx."\" ";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    $tabidx++;
    //削除ボタンを作成する
    $extra = "onclick=\"return btn_submit('delete');\" tabindex=\"".$tabidx."\" ";
    $arg["button"]["btn_delete"] = knjCreateBtn($objForm, "btn_delete", "削 除", $extra);
    $tabidx++;
    //取消ボタンを作成する
    $extra = "onclick=\"return btn_submit('clear');\" tabindex=\"".$tabidx."\" ";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    $tabidx++;
    //終了ボタンを作成する
    $extra = "onclick=\"return closeWin();\" tabindex=\"".$tabidx."\" ";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
    $tabidx++;
}

?>
