<?php

require_once('for_php7.php');

class knjl210cForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl210cindex.php", "", "main");

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        //一覧表示
        if ((!isset($model->warning)) && (!is_array($existdata))) {
            //データを取得
            $Row = knjl210cQuery::get_edit_data($model);
            if ($model->cmd == 'back' || $model->cmd == 'next' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                if (!is_array($Row)) {
                    if ($model->cmd == 'back' || $model->cmd == 'next') {
                        $model->setWarning("MSG303","更新しましたが、移動先のデータが存在しません。");
                    }
                    $model->cmd = "main";
                    $Row = knjl210cQuery::get_edit_data($model);
                }
                $model->receptno = $Row["PRE_RECEPTNO"];
                $model->applicantdiv = $Row["APPLICANTDIV"];
            }
            $disabled = "";
            if (!is_array($Row)) {
                $disabled = " disabled";
                if ($model->cmd == 'reference') {
                    $model->setWarning("MSG303");
                }
            }
            if (is_array($Row)) {
                $model->preTestdiv = $Row["PRE_TESTDIV"];
            }
        } else {
            $Row =& $model->field;
        }
        if (isset($Row["PRE_RECEPTNO"])) {
            $model->checkrecept = $Row["PRE_RECEPTNO"];
        }

        //DB接続
        $db = Query::dbCheckOut();

        //------------------------------受付者情報-------------------------------------

        //入試制度コンボ
        $query = knjl210cQuery::getApplicantdiv($model);
        $extra = "onchange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, "APPLICANTDIV", $Row["APPLICANTDIV"], $extra, 1, "");

        //受付番号
        $extra = "onchange=\"btn_disabled();\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["PRE_RECEPTNO"] = knjCreateTextBox($objForm, $model->receptno, "PRE_RECEPTNO", 5, 5, $extra);

        //プレテスト区分
        $query = knjl210cQuery::getVnameMst($model->year, "L104");
        $extra = "onchange=\"change_flg()\"";
        $model->L104nmsp1 = makeCmb($objForm, $arg, $db, $query, "PRE_TESTDIV", $model->preTestdiv, $extra, 1, "BLANK");

        //氏名(受付者)
        //$extra = "onchange=\"change_flg()\" onkeyup=\" keySet('NAME', 'NAME_KANA', 'H')\"";
        $extra = "onchange=\"change_flg()\"";
        $arg["data"]["NAME"] = knjCreateTextBox($objForm, $Row["NAME"], "NAME", 40, 60, $extra);

        //氏名かな(受付者)
        $extra = "onchange=\"change_flg()\"";
        $arg["data"]["NAME_KANA"] = knjCreateTextBox($objForm, $Row["NAME_KANA"], "NAME_KANA", 40, 120, $extra);

        //性別コンボ
        $query = knjl210cQuery::getSex($model);
        $extra = "onchange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, "SEX", $Row["SEX"], $extra, 1, "");

        global $sess;
        //郵便番号
        $extra = "onblur=\"isZipcd(this)\" onchange=\"change_flg()\"";
        $arg["data"]["ZIPCD"] = knjCreateTextBox($objForm, $Row["ZIPCD"], "ZIPCD", 10, 8, $extra);
        //郵便番号入力支援
        $extra = "style=\"width:140px\"onclick=\"loadwindow('" .REQUESTROOT ."/common/search_zipcd.php?cmd=search&addrname=ADDRESS1&zipname=ZIPCD&ZIP_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $arg["button"]["btn_zip"] = knjCreateBtn($objForm, "btn_zip", "郵便番号入力支援", $extra);
        //確定
        $extra = "onclick=\"hiddenWin('" .REQUESTROOT ."/common/search_zipcd.php?cmd=apply&addrname=ADDRESS1&zipname=ZIPCD&zip='+document.forms[0]['ZIPCD'].value+'&frame='+getFrameName(self))\"";
        $arg["button"]["btn_apply"] = knjCreateBtn($objForm, "btn_apply", "確定", $extra);

        //住所
        $extra = "onchange=\"change_flg()\"";
        $arg["data"]["ADDRESS1"] = knjCreateTextBox($objForm, $Row["ADDRESS1"], "ADDRESS1", 50, 50, $extra);

        //方書
        $extra = "onchange=\"change_flg()\"";
        $arg["data"]["ADDRESS2"] = knjCreateTextBox($objForm, $Row["ADDRESS2"], "ADDRESS2", 50, 50, $extra);

        //電話番号
        $extra = "onblur=\"this.value=toTelNo(this.value)\" onchange=\"change_flg()\"";
        $arg["data"]["TELNO"] = knjCreateTextBox($objForm, $Row["TELNO"], "TELNO", 14, 14, $extra);

        //出身学校
        $extra = "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=&fscdname=&fsname=&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}()-150, 500, 380)\"";
        $arg["button"]["btn_searchfs"] = knjCreateBtn($objForm, "btn_searchfs", "出身校検索", $extra);
        $extra = "";
        $arg["data"]["FINSCHOOLCD"] = knjCreateTextBox($objForm, $Row["FS_CD"], "FINSCHOOLCD", 7, 7, $extra);
        $finschoolname = $db->getOne(knjl210cQuery::getFinschoolName($Row["FS_CD"]));
        $arg["data"]["FINSCHOOLNAME"] = $Row["FINSCHOOLNAME"] ? $Row["FINSCHOOLNAME"] : $finschoolname;

        //塾
        $PsName     = array();
        $result     = $db->query(knjl210cQuery::getPrischoolcd($model->year, $Row["PS_CD"]));
        while($row = $result->fetchRow(DB_FETCHMODE_ASSOC))
        {
            $PsName[$row["PRISCHOOLCD"]] = $row["PRISCHOOL_NAME"];
        }
        $result->free();
        //コード
        $extra = "onchange=\"change_flg()\"";
        $arg["data"]["PS_CD"] = knjCreateTextBox($objForm, $Row["PS_CD"], "PS_CD", 7, 7, $extra);
        //塾名
        $arg["data"]["PS_NAME"] = $PsName[$Row["PS_CD"]];


        //------------------------------保護者情報-------------------------------------

        //氏名(保護者)
        //$extra = "onchange=\"change_flg()\" onkeyup=\" keySet('GNAME', 'GKANA', 'H')\"";
        $extra = "onchange=\"change_flg()\"";
        $arg["data"]["GNAME"] = knjCreateTextBox($objForm, $Row["GNAME"], "GNAME", 40, 60, $extra);

        //氏名かな(保護者)
        $extra = "onchange=\"change_flg()\"";
        $arg["data"]["GKANA"] = knjCreateTextBox($objForm, $Row["GKANA"], "GKANA", 40, 120, $extra);

        //受験型
        $query = knjl210cQuery::getVnameMst($model->year, "L105");
        $result = $db->query($query);
        $model->L105nmsp1 = array();
        $model->L105nm1 = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->L105nmsp1[$row["VALUE"]] = $row["NAMESPARE1"];
            $model->L105nm1[$row["VALUE"]] = $row["NAME1"];
            $arg["data"]["TYPE_NAME".$row["VALUE"]] = $row["NAME1"];
        }
        $result->free();

        $opt = array(1, 2, 3);
        $defType = $model->preTestdiv == "2" ? "3" : "1";
        $Row["PRE_EXAM_TYPE"] = strlen($Row["PRE_EXAM_TYPE"]) ? $Row["PRE_EXAM_TYPE"] : $defType;
        $extra = array();
        foreach($opt as $key => $val) {
            array_push($extra, " id=\"PRE_EXAM_TYPE{$val}\" onchange=\"change_flg()\"");
        }
        $radioArray = knjCreateRadio($objForm, "PRE_EXAM_TYPE", $Row["PRE_EXAM_TYPE"], $extra, $opt, get_count($opt));
        foreach($radioArray as $key => $val) $arg["data"][$key] = $val;

        //申込方法 1:FAX 2:郵送 3:インターネット
        $opt = array(1, 2, 3);
        $extra = array("id=\"PRE_RECEPTDIV1\" onchange=\"change_flg()\"", "id=\"PRE_RECEPTDIV2\" onchange=\"change_flg()\"", "id=\"PRE_RECEPTDIV3\" onchange=\"change_flg()\"");
        $Row["PRE_RECEPTDIV"] = strlen($Row["PRE_RECEPTDIV"]) ? $Row["PRE_RECEPTDIV"] : "1";
        $radioArray = knjCreateRadio($objForm, "PRE_RECEPTDIV", $Row["PRE_RECEPTDIV"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg["data"][$key] = $val;

        //塾への連絡希望 1:する 2:しない
        $opt = array(1, 2);
        $extra = array("id=\"PS_CONTACT1\" onchange=\"change_flg()\"", "id=\"PS_CONTACT2\" onchange=\"change_flg()\"");
        $Row["PS_CONTACT"] = strlen($Row["PS_CONTACT"]) ? $Row["PS_CONTACT"] : "1";
        $radioArray = knjCreateRadio($objForm, "PS_CONTACT", $Row["PS_CONTACT"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg["data"][$key] = $val;

        //受付日付
        $Row["PRE_RECEPTDATE"] = strlen($Row["PRE_RECEPTDATE"]) ? $Row["PRE_RECEPTDATE"] : CTRL_DATE;
        $arg["data"]["PRE_RECEPTDATE"] = View::popUpCalendar($objForm, "PRE_RECEPTDATE", str_replace("-", "/", $Row["PRE_RECEPTDATE"]));

        //学園バス利用
        $extra  = "id=\"BUS_USE\" onchange=\"change_flg()\" onClick=\"disBusUse(this);\"";
        $extra .= strlen($Row["BUS_USE"]) ? " checked" : "";
        $arg["data"]["BUS_USE"] = knjCreateCheckBox($objForm, "BUS_USE", "1", $extra, "");
        //以下は、学園バス利用する場合、有効。
        $disBusUse = strlen($Row["BUS_USE"]) ? "" : " disabled";
        //乗降地 1:林間田園都市駅 2:福神駅 3:JR五条駅
        $opt = array(1, 2, 3);
        $extra = array("id=\"STATIONDIV1\" onchange=\"change_flg()\"".$disBusUse, "id=\"STATIONDIV2\" onchange=\"change_flg()\"".$disBusUse, "id=\"STATIONDIV3\" onchange=\"change_flg()\"".$disBusUse);
        $Row["STATIONDIV"] = strlen($Row["STATIONDIV"]) ? $Row["STATIONDIV"] : "3";
        $radioArray = knjCreateRadio($objForm, "STATIONDIV", $Row["STATIONDIV"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) $arg["data"][$key] = $val;
        //ご利用人数
        $extra = "onchange=\"change_flg()\" onblur=\"this.value=toInteger(this.value)\"".$disBusUse;
        $arg["data"]["BUS_USER_COUNT"] = knjCreateTextBox($objForm, $Row["BUS_USER_COUNT"], "BUS_USER_COUNT", 4, 2, $extra);

        //バス情報表示
        if ($model->Properties["Pretest_bus_Not_Hyouji"] != "1") {
            $arg["Pretest_bus_Hyouji"] = 1;
        }

        //備考
        $extra = "onchange=\"change_flg()\"";
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 30, 30, $extra);

        //重複受験番号
        $extra = "onchange=\"change_flg()\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["RECOM_EXAMNO"] = knjCreateTextBox($objForm, $Row["RECOM_EXAMNO"], "RECOM_EXAMNO", 5, 5, $extra);


        //DB切断
        Query::dbCheckIn($db);

        /********/
        /* 検索 */
        /********/
        //新規（受付者）
        $extra = "onclick=\"return btn_submit('addnew');\"";
        $arg["button"]["btn_addnew"] = knjCreateBtn($objForm, "btn_addnew", "新 規", $extra);
        //検索（受付者）
        $extra = "onclick=\"return btn_submit('reference');\"";
        $arg["button"]["btn_reference"] = knjCreateBtn($objForm, "btn_reference", "検 索", $extra);
        //かな検索（受付者）
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL210C/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&receptno='+document.forms[0]['PRE_RECEPTNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $arg["button"]["btn_kana_reference"] = knjCreateBtn($objForm, "btn_kana_reference", "かな検索", $extra);
        //前の受付者検索
        $extra = "style=\"width:32px\" onClick=\"btn_submit('back1');\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);
        //次の受付者検索
        $extra = "style=\"width:32px\" onClick=\"btn_submit('next1');\"";
        $arg["button"]["btn_next"] = knjCreateBtn($objForm, "btn_next", " >> ", $extra);
        //画面クリア
        $extra = "style=\"width:80px\" onclick=\"return btn_submit('disp_clear');\"";
        $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "画面クリア", $extra);

        //かな検索（塾）
        $extra = "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL210C/search_pri_name.php?cmd=search&year='+document.forms[0]['year'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}()-180, 320, 370)\"";
        $arg["button"]["btn_pri_kana_reference"] = knjCreateBtn($objForm, "btn_pri_kana_reference", "塾検索", $extra);

        //ボタン作成
        makeButton($objForm, $arg, $disabled);

        //hidden
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "cmd") );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "year",
                            "value"     => $model->year) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ctrl_year",
                            "value"     => CTRL_YEAR) );
        $objForm->ae( array("type"      => "hidden",
                            "name"      => "ctrl_semester",
                            "value"     => CTRL_SEMESTER) );

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl210cForm1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "") {
    $value_flg = false;
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $retArray = array();
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) $value_flg = true;
        if ($name == "PRE_TESTDIV") {
            $retArray[$row["VALUE"]] = $row["NAMESPARE1"];
        }
    }
    $result->free();
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    return $retArray;
}

//ボタン作成
function makeButton(&$objForm, &$arg, $disabled) {
    /********/
    /* 編集 */
    /********/
    //追加
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
    //更新
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disabled);
    //更新後前の受付者
    $extra = "style=\"width:150px\" onclick=\"return btn_submit('back');\"";
    $arg["button"]["btn_up_pre"] = knjCreateBtn($objForm, "btn_up_pre", "更新後前の受付者", $extra.$disabled);
    //更新後次の受付者
    $extra = "style=\"width:150px\" onclick=\"return btn_submit('next');\"";
    $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_next", "更新後次の受付者", $extra.$disabled);
    //削除
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra.$disabled);
    //取消
    $extra = "onclick=\"return btn_submit('reset');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //終了
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
}
?>
