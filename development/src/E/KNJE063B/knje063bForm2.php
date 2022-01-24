<?php

require_once('for_php7.php');

class knje063bform2
{

    public function main(&$model)
    {

        $objForm = new form();

        /* Add by HPA textarea_cursor 2020-02-03 start */
        if ($model->message915 == "") {
            echo "<script>sessionStorage.removeItem(\"KNJE063BForm2_CurrentCursor915\");</script>";
        } else {
            echo "<script>var textArea= '".$model->message915."';
              sessionStorage.setItem(\"KNJE063BForm2_CurrentCursor915\", textArea);
              sessionStorage.removeItem(\"KNJE063BForm2_CurrentCursor\");</script>";
            $model->message915 = "";
        }
        /* Add by HPA for textarea_cursor 2020-02-20 end */

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knje063bindex.php", "", "edit");
        /* Add by HPA for PC-talker 読み start 2020/02/03 */
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = htmlspecialchars($model->name);
        if ($arg["NAME"] != "") {
            $htmlTitle = "\"".$arg["NAME"]."のロールクリック画面\"";
        } else {
            $htmlTitle = "'右結果画面のロールクリック画面'";
        }
        echo "<script>
        var title= ".$htmlTitle.";
        </script>";
        /*Add by HPA for title end 2020/02/20 */

        //DB接続
        $db = Query::dbCheckOut();

        //学校校種を取得
        $query = knje063bQuery::getSchoolKind($model);
        $getSchoolKind = $db->getOne($query);
        knjCreateHidden($objForm, "PRGID", "KNJA134".$getSchoolKind);

        //警告メッセージを表示しない場合
        $Row = array();
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            if ($model->cmd == "add_year") {
                $Row =& $model->field;
            } elseif ($model->cmd == "subclass") {
                $Row =& $model->field;
                $model->editYear = $Row["YEAR"];
                $model->subclasscd = $Row["SUBCLASSCD"];
                $row = $db->getRow(knje063bQuery::selectQuery($model, "1", $getSchoolKind), DB_FETCHMODE_ASSOC);
                if (!empty($row)) {
                    $Row = $row;
                }
            } elseif ($model->cmd == "class" || $model->cmd == "curriculum") {
                $Row =& $model->field;
                $Row["SUBCLASSCD"]  = "";
                $Row["REMARK1"]     = "";
            } else {
                $Row = $db->getRow(knje063bQuery::selectQuery($model, "1", $getSchoolKind), DB_FETCHMODE_ASSOC);
            }
        } else {
            $Row =& $model->field;
        }

        //年度コンボボックス
        /* Edit by HPA for PC-talker 読み and current_cursor start 2020/02/03 */
        $opt_year = makeYear($db, $model, $getSchoolKind);
        $extra = "aria-label = \"年度\" id = \"add_year\" onChange=\"current_cursor('add_year');return btn_submit('add_year');\"";
        $arg["year"]["YEAR"] = knjCreateCombo($objForm, "YEAR", $Row["YEAR"], $opt_year, $extra, 1);
        $year = $Row["YEAR"] ? $Row["YEAR"] : $opt_year[0]["value"];
        //年度追加テキスト
        $extra = " aria-label = \"年度追加\" id = \"year_add\" onblur=\"this.value=toInteger(this.value,'year_add');\"";
        $arg["year"]["year_add"] = knjCreateTextBox($objForm, "", "year_add", 5, 4, $extra);
        //年度追加ボタン
        $extra = "id = \"add_year_btn\" onclick=\"current_cursor('add_year_btn');return add('');\"";
        $arg["year"]["btn_year_add"] = knjCreateBtn($objForm, "btn_year_add", "年度追加", $extra);

        //教科コンボ
        $query = knje063bQuery::getClassMst($model, $year, $getSchoolKind);
        $extra = "aria-label = \"教科名\" id = \"class\" onChange=\"current_cursor('class');return btn_submit('class');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["CLASSCD"], "CLASSCD", $extra, 1, "BLANK");

        //教育課程コンボ
        $query = knje063bQuery::getCurriculum($model, $year, $Row["CLASSCD"]);
        $extra = "aria-label = \"教育課程\" id = \"curriculum\" onChange=\"current_cursor('curriculum');return btn_submit('curriculum');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["CURRICULUM_CD"], "CURRICULUM_CD", $extra, 1, "BLANK");

        //科目コンボ
        $query = knje063bQuery::getSubclassMst($model, $Row["CLASSCD"], $Row["CURRICULUM_CD"]);
        $extra = "aria-label = \"科目コード\" id = \"subclass\" onChange=\"current_cursor('subclass');return btn_submit('subclass');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SUBCLASSCD"], "SUBCLASSCD", $extra, 1, "BLANK");

        //文言評価
        $extra = " aria-label = \"文言評価 全角で {$model->moji}文字X{$model->gyo}行\" id=\"REMARK1\" ";
        $arg["REMARK1"] = knjCreateTextArea($objForm, "REMARK1", $model->gyo, $model->moji * 2, "wrap", $extra, $Row["REMARK1"]);
        $arg["REMARK1_SIZE"] = "(全角{$model->moji}文字✕{$model->gyo}行まで)";
        knjCreateHidden($objForm, "REMARK1_KETA", $model->moji * 2);
        knjCreateHidden($objForm, "REMARK1_GYO", $model->gyo);
        KnjCreateHidden($objForm, "REMARK1_STAT", "statusareaREMARK1");

        if ($model->schoolName == "naraken") {
            $text = array();
            $text[] = "理科と社会の授業を行っている場合の入力上の注意";
            $text[] = "";
            $text[] = "小学3から6年生の理科は生活の欄の上半分に表示されます";
            $text[] = "小学3から6年生の社会は生活の欄の下半分に表示されます";
            $text[] = "";
            $text[] = "このため、表示可能行数が理科と社会は4行になります";
            $text[] = "必ず4行以内の入力としてください";
            $text[] = "超過した場合は、後半が表示されません";
            $arg["NARAKEN_HYOUKA_TEXT"] = implode("<br>", $text);
        }

        // 指導計画参照の科目指定
        if ($model->Properties["shidouKeikakuSansyoTable"] == "HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT" && $model->schoolName != "naraken") {
            $arg["SUBCLASSCD_SEQ001"] = "1";
            //科目コンボ(合わせた指導)
            $query = knje063bQuery::getSubclassMstClassSeq001($model, $getSchoolKind, $year);
            $extra = " id=\"SUBCLASSCD_SEQ001\" onchange=\"current_cursor('SUBCLASSCD_SEQ001');subclasscdSeq001Changed();\" ";
            makeCmb($objForm, $arg, $db, $query, $Row["SUBCLASSCD_SEQ001"], "SUBCLASSCD_SEQ001", $extra, 1, "BLANK");
        }
        /* Edit by HPA for PC-talker 読み and current_cursor end 2020/02/20 */

        //ボタン作成
        makeBtn($objForm, $arg, $model, $Row);

        //hidden作成
        makeHidden($objForm, $model);
        knjCreateHidden($objForm, "shidouKeikakuSansyoTable", $model->Properties["shidouKeikakuSansyoTable"]);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        $arg["IFRAME"] = VIEW::setIframeJs();

        if (isset($model->message)) {
            $arg["reload"] = "window.open('knje063bindex.php?cmd=list&SCHREGNO=$model->schregno','top_frame');";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML5($model, "knje063bForm2.html", $arg);
    }
}

//年度作成
function makeYear($db, &$model, $getSchoolKind)
{
    //年度取得
    $query = knje063bQuery::selectQueryYear($model, $getSchoolKind);
    $result = $db->query($query);
    $make_year = array();
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $model->year[] = $row["YEAR"];
    }

    //年度追加された値を保持
    $year_arr = array_unique($model->year);
    foreach ($year_arr as $val) {
        $make_year[] = array("label" => $val, "value" => $val);
    }
    rsort($make_year);
    return $make_year;
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank) {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

/* Edit by HPA for PC-talker 読み and current_cursor start 2020/02/03 */
//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $Row)
{
    //ボタン無効
    $disabled = ($model->schregno && $Row["YEAR"] && $Row["SUBCLASSCD"]) ? "" : " disabled";

    //更新ボタン
    $extra = "id = \"update\" aria-label= \"更新\" onclick=\"current_cursor('update');return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);
    //削除ボタン
    $extra = "id = \"delete2\" aria-label= \"削除\"  onclick=\"current_cursor('delete2');return btn_submit('delete2');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra.$disabled);
    //取消ボタン
    $extra = "id = \"reset\" aria-label= \"取消\" onclick=\"current_cursor('reset');return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了ボタン
    $extra = "aria-label= \"終了\" onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
    //印刷ボタン
    $extra = "onclick=\"return newwin('" . SERVLET_URL . "');\"";
    $arg["button"]["btn_print"] = knjCreateBtn($objForm, "btn_print", "印刷(確認用)", $extra);

    if ($model->schoolName != "naraken") {
        //指導計画参照ボタン
        $extra = "id = \"btn_subform1\" onclick=\"current_cursor('btn_subform1');return btn_submit('subform1');\"";
        $arg["button"]["btn_subform1"] = KnjCreateBtn($objForm, "btn_subform1", "指導計画参照", $extra.$disabled);
        if ($model->Properties["shidouKeikakuSansyoTable"] == "HREPORT_GUIDANCE_SCHREG_SUBCLASS_DAT") {
            //指導計画参照ボタン
            $disabled = ($Row["SUBCLASSCD_SEQ001"]) ? "" : " disabled";
            $extra = "id = \"subform1_2\" onclick=\"current_cursor('subform1_2');return btn_submit('subform1_2');\"";
            $arg["button"]["btn_subform1_2"] = KnjCreateBtn($objForm, "btn_subform1_2", "合わせた指導参照", $extra.$disabled);
        }
    }
}
/* Edit by HPA for PC-talker 読み and current_cursor end 2020/02/20 */

//hidden作成
function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "chkSCHREGNO", $model->schregno);
    //帳票印刷用
    knjCreateHidden($objForm, "category_selected", $model->grade.$model->hrClass."-".$model->schregno);
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "GAKKI", CTRL_SEMESTER);
    knjCreateHidden($objForm, "gakushu", "1");
    knjCreateHidden($objForm, "CHITEKI", "1");
    knjCreateHidden($objForm, "OUTPUT", "1");
    knjCreateHidden($objForm, "useSchregRegdHdat", $model->Properties["useSchregRegdHdat"]);
}
?>
