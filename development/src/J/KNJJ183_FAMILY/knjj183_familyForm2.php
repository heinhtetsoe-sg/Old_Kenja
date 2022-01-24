<?php

require_once('for_php7.php');

class knjj183_familyForm2 {
    function main(&$model) {
        //フォーム作成
        $objForm = new form;
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjj183_familyindex.php", "", "edit");
        $arg["reload"] = "";
        $db = Query::dbCheckOut();

        //家族番号
        $query = knjj183_familyQuery::getFamilyNo($model);
        $model->familyNo = $db->getOne($query);

        //家族情報の取得      (FAMILY_DAT)
        $row6 = $db->getRow(knjj183_familyQuery::getFamilyDat($model, $model->rela_no), DB_FETCHMODE_ASSOC);

        if (!$model->isWarning()) {
            $row  = array_merge((array)$row1,(array)$row3,(array)$row6);
        } else {
            $row =& $model->field;
        }

        if (!$model->rela_no) {
            $model->rela_no = $row["RELA_NO"];
        }

/********************************************************************************/
/********************************************************************************/
/*******        *****************************************************************/
/*******  家族  *****************************************************************/
/*******        *****************************************************************/
/********************************************************************************/
/********************************************************************************/
        global $sess;

        $guard_disabled = "";

        //コピー元学籍番号
        $model->stucd = $model->familyNo;
        $extra = "style=\"text-align:right; background-color:#999999\" readOnly ";
        $arg["data"]["STUCD"] = knjCreateTextBox($objForm, $model->stucd, "STUCD", 10, 10, $extra);

        //検索ボタンを作成する
        $extra = "style=\"width:140px\"onclick=\"loadwindow('./knjj183_familyFamilySearch.php?cmd=search&MODE=reflect&useGuardian2={$model->Properties["useGuardian2"]}&CD=$model->schregno&FAMILYNO='+document.forms[0]['STUCD'].value+'&STUCD_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 600, 350)\"";
        $arg["data"]["BTN_STUCD"] = knjCreateBtn($objForm, "btn_stucd", "家族番号検索", $extra);

        //連番
        $arg["data"]["RELA_NO"] = $row["RELA_NO"];
        knjCreateHidden($objForm, "RELA_NO", $row["RELA_NO"]);

        //生年月日
        $arg["data"]["RELA_BIRTHDAY"]   = str_replace("input ", "input {$guard_disabled} ", View::popUpCalendar($objForm, "RELA_BIRTHDAY", str_replace("-","/",$row["RELA_BIRTHDAY"]), ""));

        /********************/
        /* テキストボックス */
        /********************/
        //氏名
        $extra = "" . $guard_disabled;
        $arg["data"]["RELA_NAME"]       = knjCreateTextBox($objForm, $row["RELA_NAME"], "RELA_NAME", 40, 40, $extra);
        //氏名かな
        $extra = "" . $guard_disabled;
        $arg["data"]["RELA_KANA"]       = knjCreateTextBox($objForm, $row["RELA_KANA"], "RELA_KANA", 40, 80, $extra);
        //職業又は学校
        $extra = "" . $guard_disabled;
        $arg["data"]["RELA_OCCUPATION"] = knjCreateTextBox($objForm, $row["RELA_OCCUPATION"], "RELA_OCCUPATION", 40, 40, $extra);
        //備考
        $extra = "" . $guard_disabled;
        $arg["data"]["RELA_REMARK"]     = knjCreateTextBox($objForm, $row["RELA_REMARK"], "RELA_REMARK", 40, 30, $extra);
        //兄弟姉妹学籍番号
        $arg["data"]["RELA_SCHREGNO"]   = $row["RELA_SCHREGNO"];
        knjCreateHidden($objForm, "RELA_SCHREGNO", $row["RELA_SCHREGNO"]);

        /******************/
        /* コンボボックス */
        /******************/
        //名称マスタよりコンボボックスのデータを取得
        $opt = array();
        $opt["Z002"][] = array("label" => "","value" => "0");
        $opt["H201"][] = array("label" => "","value" => "00");
        $opt["H200"][] = array("label" => "","value" => "00");
        $query = knjj183_familyQuery::get_name_mst();
        $result = $db->query($query);
        while ($row3 = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt[$row3["NAMECD1"]][] = array("label" => $row3["NAMECD2"]."&nbsp;".$row3["NAME1"],
                                             "value" => $row3["NAMECD2"]);
        }
        //性別
        $extra = "" . $guard_disabled;
        $arg["data"]["RELA_SEX"] = knjCreateCombo($objForm, "RELA_SEX", $row["RELA_SEX"], $opt["Z002"], $extra, 1);
        //続柄
        $extra = "" . $guard_disabled;
        $arg["data"]["RELA_RELATIONSHIP"] = knjCreateCombo($objForm, "RELA_RELATIONSHIP", $row["RELA_RELATIONSHIP"], $opt["H201"], $extra, 1);
        //長子
        $extra = " id=\"TYOUSHI_FLG\" ";
        $tyoushi = $row["TYOUSHI_FLG"] == "1" ? " checked " : "";
        $arg["data"]["TYOUSHI_FLG"] = knjCreateCheckBox($objForm, "TYOUSHI_FLG", "1", $extra.$tyoushi);
        //同居区分
        $extra = "" . $guard_disabled;
        $arg["data"]["RELA_REGIDENTIALCD"] = knjCreateCombo($objForm, "RELA_REGIDENTIALCD", $row["RELA_REGIDENTIALCD"], $opt["H200"], $extra, 1);
        //在卒区分
        $opt["FLG"][] = array('label' => "",     'value' => "");
        $opt["FLG"][] = array('label' => "在学", 'value' => "1");
        $opt["FLG"][] = array('label' => "卒業", 'value' => "2");
        $extra = "" . $guard_disabled;
        $arg["data"]["REGD_GRD_FLG"] = knjCreateCombo($objForm, "REGD_GRD_FLG", $row["REGD_GRD_FLG"], $opt["FLG"], $extra, 1);
        //学年
        $opt["GRADE"][] = array("label" => "", "value" => "");
        $query = knjj183_familyQuery::getSchregRegdGdat($model);
        $result = $db->query($query);
        while ($rowG = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt["GRADE"][] = array("label" => $rowG["LABEL"],
                                    "value" => $rowG["VALUE"]);
        }
        $extra = "" . $guard_disabled;
        $arg["data"]["RELA_GRADE"] = knjCreateCombo($objForm, "RELA_GRADE", $row["RELA_GRADE"], $opt["GRADE"], $extra, 1);

        /**********/
        /* ボタン */
        /**********/
        //追加ボタン
        $add_cmd = "addFamily";
        $extra = "onclick=\"return btn_submit('".$add_cmd."');\"" . $guard_disabled;
        $arg["button"]["btn_add2"]     = knjCreateBtn($objForm, "btn_add2", "追加", $extra);
        //更新ボタン
        $upd_cmd = "updFamily";
        $extra = "onclick=\"return btn_submit('".$upd_cmd."');\"" . $guard_disabled;
        $arg["button"]["btn_update2"]  = knjCreateBtn($objForm, "btn_update2", "更新", $extra);
        //削除ボタン
        $del_cmd = "delFamily";
        $extra = "onclick=\"return btn_submit('".$del_cmd."');\"" . $guard_disabled;
        $arg["button"]["btn_del2"]     = knjCreateBtn($objForm, "btn_del2", "削除", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('clear');\"" . $guard_disabled;
        $arg["button"]["btn_reset2"]   = knjCreateBtn($objForm, "btn_reset2", "取消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"" . $guard_disabled;
        $arg["button"]["btn_end2"]     = knjCreateBtn($objForm, "btn_end2", "終了", $extra);
        //戻るボタンを作成する
        $link1 = REQUESTROOT."/J/KNJJ183/knjj183index.php?cmd=edit";
        $extra = "onclick=\" Page_jumper('".$link1."','".$model->schregno."');\"";
        $arg["button"]["btn_jump"] = knjCreateBtn($objForm, "btn_jump", "戻る", $extra);

        /**********/
        /* hidden */
        /**********/
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "UPDATED", $row["UPDATED"]);
        knjCreateHidden($objForm, "RELA_UPDATED", $row["RELA_UPDATED"]);
        knjCreateHidden($objForm, "REQUESTROOT", REQUESTROOT);
        //hidden
        knjCreateHidden($objForm, "searchVal");

        $result->free();
        Query::dbCheckIn($db);
        $arg["finish"] = $objForm->get_finish();

        if (VARS::get("cmd") != "edit"){
            $arg["reload"]  = "parent.top_frame.location.href='knjj183_familyindex.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。 
        View::toHTML($model, "knjj183_familyForm2.html", $arg);
    }
}
//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank="") {
    $opt = array();
    $value_flg = false;
    if ($blank) $opt[] = array('label' => "", 'value' => "");
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $result->free();

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
?>
