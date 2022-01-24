<?php

require_once('for_php7.php');

class knjl211rForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl211rindex.php", "", "main");

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //DB接続
        $db = Query::dbCheckOut();

        //一覧表示
        if ((!isset($model->warning)) && (!is_array($existdata)) || $model->cmd != "changeData") {
            //データを取得
            $query = knjl211rQuery::get_edit_data($model);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if (!is_array($Row)) {
                if ($model->cmd == 'reference') {
                    $model->setWarning("MSG303");
                }
            }
        } else {
            $Row =& $model->field;
        }

        $arg["TOP"]["YEAR"] = $model->year;
        if ($model->cmd == 'changeData' || isset($model->warning)) {
            $Row =& $model->field;
        }

        if (isset($Row["EXAMNO"])) {
            $model->checkexam = $Row["EXAMNO"];
        }

        //------------------------------志願者情報-------------------------------------
        //入試制度コンボ
        $query = knjl211rQuery::get_name_cd($model, $model->year, "L003");
        $extra = "onChange=\"change_flg()\" disabled ";
        makeCmb($objForm, $arg, $db, $query, $Row["APPLICANTDIV"], "APPLICANTDIV1", $extra, 1, "");
        knjCreateHidden($objForm, "APPLICANTDIV", $Row["APPLICANTDIV"]);

        //入試区分
        $query = knjl211rQuery::get_name_cd($model, $model->year, "L004", "ABBV1");
        $extra = "onChange=\"change_flg(); return btn_submit('changeData');\" disabled ";
        makeCmb($objForm, $arg, $db, $query, $Row["TESTDIV"], "TESTDIV1", $extra, 1, "BLANK");
        knjCreateHidden($objForm, "TESTDIV", $Row["TESTDIV"]);

        //事前頁
        $extra = " style=\"background:#cccccc;\" readOnly";
        $arg["data"]["BEFORE_PAGE"] = knjCreateTextBox($objForm, $Row["BEFORE_PAGE"], "BEFORE_PAGE", 3, 3, $extra);

        //事前連番
        $extra = " style=\"background:#cccccc;\" readOnly";
        $arg["data"]["BEFORE_SEQ"] = knjCreateTextBox($objForm, $Row["BEFORE_SEQ"], "BEFORE_SEQ", 3, 3, $extra);

        //受験番号
        $getExamno = array();
        $setExamno = "";
        $query = knjl211rQuery::getExamno($model, $Row);
        $result = $db->query($query);
        $i = 0;
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $getExamno[$i] = $row["EXAMNO"];
            if ($i == 0 && $row["EXAMNO"] != "") {
                $setExamno .= $getExamno[$i];
            } else if ($i != 0 && $row["EXAMNO"] != "") {
                $setExamno .= ','.$getExamno[$i];
            }
            $i++;
        }
        $result->free();
        $arg["data"]["EXAMNO"] = $setExamno;

        //氏名
        $extra = "onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["NAME"] = knjCreateTextBox($objForm, $Row["NAME"], "NAME", 40, 60, $extra);

        //氏名かな
        $extra = "onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["NAME_KANA"] = knjCreateTextBox($objForm, $Row["NAME_KANA"], "NAME_KANA", 40, 120, $extra);

        //性別コンボ
        $query = knjl211rQuery::get_name_cd($model, $model->year, "Z002");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SEX"], "SEX", $extra, 1, "BLANK");

        //内諾コンボ
        $query = knjl211rQuery::getNaidaku($model);
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["EXAMCOURSECD"], "EXAMCOURSECD", $extra, 1, "BLANK");

        global $sess;

        //出身学校コード
        $extra = "onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["FINSCHOOLCD"] = knjCreateTextBox($objForm, $Row["FS_CD"], "FINSCHOOLCD", 7, 7, $extra);

        //学校名
        $query = knjl211rQuery::getFinschoolName($Row["FS_CD"]);
        $fsArray = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["FINSCHOOLNAME"] = $fsArray["FINSCHOOL_NAME"];

        //検索ボタン（出身学校）
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=&fscdname=&fsname=&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["button"]["btn_fin_kana_reference"] = knjCreateBtn($objForm, "btn_fin_kana_reference", "検 索", $extra);

        //出身学校名
        $extra = "onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["FS_NAME"] = knjCreateTextBox($objForm, $Row["FS_NAME"], "FS_NAME", 30, 30, $extra);

        $ippanDisabled = $Row["TESTDIV"] == "2" ? " style=\"background=#bbbbbb\" disabled " : "";
        $suisenDisabled = $Row["TESTDIV"] == "1" ? " style=\"background=#bbbbbb\" disabled " : "";
        //内申１
        $extra = "onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["NAISIN1"] = knjCreateTextBox($objForm, $Row["NAISIN1"], "NAISIN1", 3, 3, $extra.$suisenDisabled);

        //内申２
        $extra = "onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["NAISIN2"] = knjCreateTextBox($objForm, $Row["NAISIN2"], "NAISIN2", 3, 3, $extra);

        //内申３
        $extra = "onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["NAISIN3"] = knjCreateTextBox($objForm, $Row["NAISIN3"], "NAISIN3", 3, 3, $extra);

        //出欠１
        $extra = "onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["ATTEND1"] = knjCreateTextBox($objForm, $Row["ATTEND1"], "ATTEND1", 3, 3, $extra);

        //出欠２
        $extra = "onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["ATTEND2"] = knjCreateTextBox($objForm, $Row["ATTEND2"], "ATTEND2", 3, 3, $extra);

        //出欠３
        $extra = "onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["ATTEND3"] = knjCreateTextBox($objForm, $Row["ATTEND3"], "ATTEND3", 3, 3, $extra);

        //出欠合計
        $extra = "onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["ATTEND_TOTAL"] = knjCreateTextBox($objForm, $Row["ATTEND_TOTAL"], "ATTEND_TOTAL", 3, 3, $extra);

        //選抜１
        $extra = "onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["SENBATU1_SCHOOL"] = knjCreateTextBox($objForm, $Row["SENBATU1_SCHOOL"], "SENBATU1_SCHOOL", 16, 16, $extra.$suisenDisabled);

        $extra = "onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["SENBATU1_MAJOR"] = knjCreateTextBox($objForm, $Row["SENBATU1_MAJOR"], "SENBATU1_MAJOR", 16, 16, $extra.$suisenDisabled);

        //選抜２
        $extra = "onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["SENBATU2_SCHOOL"] = knjCreateTextBox($objForm, $Row["SENBATU2_SCHOOL"], "SENBATU2_SCHOOL", 16, 16, $extra.$suisenDisabled);

        $extra = "onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["SENBATU2_MAJOR"] = knjCreateTextBox($objForm, $Row["SENBATU2_MAJOR"], "SENBATU2_MAJOR", 16, 16, $extra.$suisenDisabled);

        //奨学生
        $query = knjl211rQuery::get_name_cd($model, $model->year, "L031");
        $extra = "onChange=\"change_flg();\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SCHOLARSHIP"], "SCHOLARSHIP", $extra, 1, "BLANK");

        //難関コース希望
        $checked = $Row["NANKAN_FLG"] == "1" ? "checked" : "";
        $extra = "onChange=\"change_flg()\" id=NANKAN_FLG " .$checked;
        $arg["data"]["NANKAN_FLG"] = knjCreateCheckBox($objForm, "NANKAN_FLG", "1", $extra);

        //クラブ
        $query = knjl211rQuery::get_name_cd($model, $model->year, "L032");
        $extra = "onChange=\"change_flg();\"";
        makeCmb($objForm, $arg, $db, $query, $Row["RECOM_FLG"], "RECOM_FLG", $extra.$ippanDisabled, 1, "BLANK");

        //クラブ備考
        $extra = "onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["RECOM_REMARK"] = knjCreateTextBox($objForm, $Row["RECOM_REMARK"], "RECOM_REMARK", 100, 100, $extra.$ippanDisabled);

        //備考
        $extra = "onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 100, 100, $extra);

        //-------------------------------- ボタン作成 ------------------------------------

        //事前検索ボタン
        $extra = "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL211R/search_name.php?cmd=jizen_search&year='+document.forms[0]['year'].value+'&BEFORE_PAGE='+document.forms[0]['BEFORE_PAGE'].value+'&BEFORE_SEQ='+document.forms[0]['BEFORE_SEQ'].value+'&applicantDiv='+document.forms[0]['APPLICANTDIV'].value+'&testDiv='+document.forms[0]['TESTDIV'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 400, 300)\"";
        $arg["button"]["btn_kana_reference"] = knjCreateBtn($objForm, "btn_kana_reference", "事前検索", $extra);

        //更新ボタン
        $extra = " onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_udpate"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);

        //削除ボタン
        $extra = " onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year", $model->year);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl211rForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "")
{
    $opt = array();
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

?>