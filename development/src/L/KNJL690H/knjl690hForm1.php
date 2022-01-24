<?php
class knjl690hForm1
{
    public function main(&$model)
    {
        $objForm = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl690hindex.php", "", "main");
        
        //DB接続
        $db = Query::dbCheckOut();

        if ($model->cmd == "change" || $model->cmd == "changeTestDiv") {
            $model->field["RECEPTNO"] = "";
        }

        //生徒表示
        if ((!isset($model->warning))) {
            //データを取得
            $query = knjl690hQuery::getSelectQuery($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($model->cmd == 'back' || $model->cmd == 'next' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                if (!is_array($Row)) {
                    if ($model->cmd == 'back' || $model->cmd == 'next') {
                        $model->setWarning("MSG303", "更新しましたが、移動先のデータが存在しません。");
                    }
                    $model->cmd = "main";
                    $query = knjl690hQuery::getSelectQuery($model);
                    $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                }
                $model->field["RECEPTNO"] = $Row["RECEPTNO"];
            }
            $disabled = "";
            if (!is_array($Row)) {
                $disabled = "disabled";
                if ($model->cmd == 'reference') {
                    $model->setWarning("MSG303");
                }
            }
        } else {
            $Row =& $model->field;
        }

        //入試年度
        $arg["data"]["YEAR"] = $model->ObjYear;

        //入試日程
        $arg["data"]["TESTDIV"] = $Row["TESTDIV"];

        //受験番号
        $extra = "onChange=\"btn_disabled();\" onblur=\"this.value=toAlphaNumber(this.value);\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["RECEPTNO"] = knjCreateTextBox($objForm, $Row["RECEPTNO"], "RECEPTNO", 8, 8, $extra);

        //氏名
        $arg["data"]["NAME"] = $Row["NAME"];

        //氏名カナ
        $arg["data"]["NAME_KANA"] = $Row["NAME_KANA"];

        //類別
        $arg["data"]["CLASSIFY_NAME"] = $Row["CLASSIFY_NAME"];

        //性別
        $arg["data"]["SEX"] = $Row["SEX"];

        //合否
        $extra = "";
        $query = knjl690hQuery::getEntexamSettingMst($model, "L013");
        makeCmb($objForm, $arg, $db, $query, $Row["JUDGEMENT"], "JUDGEMENT", $extra, 1, "blank");

        //手続終了
        $extra = "";
        $query = knjl690hQuery::getEntexamSettingMst($model, "L011");
        makeCmb($objForm, $arg, $db, $query, $Row["PROCEDUREDIV"], "PROCEDUREDIV", $extra, 1, "blank");

        //手続日
        $Row["PROCEDUREDATE"] = str_replace("-", "/", $Row["PROCEDUREDATE"]);
        $arg["data"]["PROCEDUREDATE"] = View::popUpCalendar2($objForm, "PROCEDUREDATE", $Row["PROCEDUREDATE"], "", "", "");

        //入学辞退
        $extra = "";
        $query = knjl690hQuery::getEntexamSettingMst($model, "L012");
        makeCmb($objForm, $arg, $db, $query, $Row["ENTDIV"], "ENTDIV", $extra, 1, "blank");
        
        //学籍番号
        $extra = "";
        $arg["data"]["STUDENTNO"] = knjCreateTextBox($objForm, $Row["STUDENTNO"], "STUDENTNO", 8, 8, $extra);

        //ボタン作成
        makeBtn($objForm, $arg, $model, $disabled);

        //hidden作成
        makeHidden($objForm);

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();

        View::toHTML($model, "knjl690hForm1.html", $arg);
    }
}

//makeCmb
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
        if ($name == "APPLICANTDIV") {
            if ($value == "" && $row["NAMESPARE2"] == '1') {
                $value = $row["VALUE"];
            }
        }
    }
    $value = ($value != "") ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
    $result->free();
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $disabled)
{
    //検索周りの制御
    $disabled2 = ($model->field["APPLICANTDIV"]) ? "" : " disabled ";

    //検索ボタン
    $extra = "onclick=\"return btn_submit('reference');\"";
    $extra = $extra.$disabled2;
    $arg["button"]["btn_reference"] = knjCreateBtn($objForm, "btn_reference", "検 索", $extra);

    //かな検索ボタン
    $extra = "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL690H/search_name.php?cmd=search&year={$model->ObjYear}&applicantdiv={$model->field["APPLICANTDIV"]}&examtype={$model->field["EXAM_TYPE"]}&receptno='+document.forms[0]['RECEPTNO'].value+'&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
    $extra = $extra.$disabled2;
    $arg["button"]["btn_kana_reference"] = knjCreateBtn($objForm, "btn_kana_reference", "かな検索", $extra);

    //前の志願者検索ボタン
    $extra = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onClick=\"btn_submit('back1');\"";
    $extra = $extra.$disabled2;
    $arg["button"]["btn_back_next"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);

    //次の志願者検索ボタン
    $extra = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onClick=\"btn_submit('next1');\"";
    $extra = $extra.$disabled2;
    $arg["button"]["btn_back_next"] .= knjCreateBtn($objForm, "btn_next", " >> ", $extra);

    //更新ボタン
    $extra = $disabled." onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_udpate"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);

    //更新ボタン(更新後前の志願者)
    $extra = $disabled." style=\"width:150px\" onclick=\"return btn_submit('back');\"";
    $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_pre", "更新後前の志願者", $extra);

    //更新ボタン(更新後次の志願者)
    $extra = $disabled." style=\"width:150px\" onclick=\"return btn_submit('next');\"";
    $arg["button"]["btn_up_next"] .= knjCreateBtn($objForm, "btn_up_next", "更新後次の志願者", $extra);

    //取消ボタン
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}

//hidden作成
function makeHidden(&$objForm)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectLeft");
    knjCreateHidden($objForm, "selectLeftText");
    knjCreateHidden($objForm, "selectRight");
    knjCreateHidden($objForm, "selectRightText");
}
?>

