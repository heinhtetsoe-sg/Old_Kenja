<?php

require_once('for_php7.php');

class knjl011dForm1 {
    function main(&$model) {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl011dindex.php", "", "main");

        //一覧表示
        if ((!isset($model->warning)) && (!is_array($existdata))) {
            //データを取得
            $Row = knjl011dQuery::get_edit_data($model);
            if ($model->cmd == 'back' || $model->cmd == 'next' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                if (!is_array($Row)) {
                    if ($model->cmd == 'back' || $model->cmd == 'next') {
                        $model->setWarning("MSG303","更新しましたが、移動先のデータが存在しません。");
                    }
                    $model->cmd = "main";
                    $Row = knjl011dQuery::get_edit_data($model);
                }
                $model->examno = $Row["EXAMNO"];
                $model->applicantdiv = $Row["APPLICANTDIV"];
                $model->testdiv = $Row["TESTDIV"];
            }
            $disabled = "";
            if (!is_array($Row)) {
                $disabled = "disabled";
                if ($model->cmd == 'reference') {
                    $Row["TESTDIV"] = $model->field["TESTDIV"];
                    $model->setWarning("MSG303");
                }
            }
        } else {
            $Row =& $model->field;
        }

        $arg["TOP"]["YEAR"] = $model->year;

        if ($model->cmd == 'changeTest') {
            $Row["TESTDIV"] = $model->field["TESTDIV"];
        }

        if (isset($Row["EXAMNO"])) {
            $model->checkexam = $Row["EXAMNO"];
        }

        //DB接続
        $db = Query::dbCheckOut();
        //エンター押下時の移動対象一覧
        $setTextField = array();
        $setTextField[] = "NAME";
        $setTextField[] = "NAME_KANA";
        $setTextField[] = "SHDIV";
        $setTextField[] = "SEX";
        $setTextField[] = "ERACD";
        $setTextField[] = "BIRTH_Y";
        $setTextField[] = "BIRTH_M";
        $setTextField[] = "BIRTH_D";
        $setTextField[] = "DESIREDIV";
        $setTextField[] = "ZIPCD";
        $setTextField[] = "ADDRESS1";
        $setTextField[] = "ADDRESS2";
        $setTextField[] = "TELNO";
        $setTextField[] = "FINSCHOOLCD";
        $setTextField[] = "FS_GRDYEAR";
        $setTextField[] = "GNAME";
        $setTextField[] = "GKANA";
        $setTextField[] = "RELATIONSHIP";
        $setTextField[] = "GZIPCD";
        $setTextField[] = "GADDRESS1";
        $setTextField[] = "GADDRESS2";
        $setTextField[] = "GTELNO";
        $setTextField[] = "REMARK1";
        $setTextField[] = "REMARK2";
        $setTextField[] = "PRISCHOOLCD1";
        $setTextField[] = "PRISCHOOL_CLASS_CD1";
        $setTextField[] = "PRISCHOOLCD2";
        $setTextField[] = "PRISCHOOL_CLASS_CD2";
        knjCreateHidden($objForm, "setTextField", implode(',', $setTextField));

        //入試区分
        knjCreateHidden($objForm, "APPLICANTDIV", $model->applicantdiv);

        //受験種別
        $query = knjl011dQuery::get_name_cd($model->year, "L004");
        $extra = "onChange=\"change_flg(); return btn_submit('changeTest');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["TESTDIV"], "TESTDIV", $extra, 1, "");

        //受験番号
        $extra = " STYLE=\"ime-mode: inactive\" onChange=\"btn_disabled();\" onblur=\"this.value=toAlphaNumber(this.value);\"";
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $model->examno, "EXAMNO", 4, 4, $extra);

        //------------------------------志願者情報-------------------------------------
        //氏名(志願者)
        $extra = "STYLE=\"ime-mode: active\" onChange=\"change_flg();\" id=\"NAME\" onkeyup=\"keySet('NAME', 'NAME_KANA', 'H');\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["NAME"] = knjCreateTextBox($objForm, $Row["NAME"], "NAME", 40, 60, $extra);

        //専併区分
        if ($Row["TESTDIV"] == "1") {
            $query = knjl011dQuery::get_name_cd($model->year, "L006", "1");
        } else {
            $query = knjl011dQuery::get_name_cd($model->year, "L006");
        }
        $extra = "onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SHDIV"], "SHDIV", $extra, 1, "");

        //氏名かな(志願者)
        $extra = "STYLE=\"ime-mode: active\" onChange=\"change_flg();\" id=\"NAME_KANA\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["NAME_KANA"] = knjCreateTextBox($objForm, $Row["NAME_KANA"], "NAME_KANA", 40, 120, $extra);

        //特待生区分
        $arg["data"]["SUB_ORDER"] = ($Row["SUB_ORDER"]) ? $Row["SUB_ORDER"]: "";
        $arg["data"]["CLUB_NAME"] = ($Row["CLUB_NAME"]) ? "　クラブ名：".$Row["CLUB_NAME"]: "";
        //学園子女
        $checked = ($Row["CHILD"]) ? " checked": "";
        $extra = "id=\"CHILD\" onChange=\"change_flg();\"".$checked;
        $arg["data"]["CHILD"] = knjCreateCheckBox($objForm, "CHILD", "1", $extra);

        //性別(志願者)
        $query = knjl011dQuery::get_name_cd($model->year, "Z002");
        $extra = "onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SEX"], "SEX", $extra, 1, "");

        //元号
        $query = knjl011dQuery::get_name_cd($model->year, "L007");
        $extra = "onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $Row["ERACD"] = ($Row["ERACD"]) ? $Row["ERACD"]: "4";//デフォルト4:平成
        makeCmb($objForm, $arg, $db, $query, $Row["ERACD"], "ERACD", $extra, 1, "");
        //年
        $extra = "STYLE=\"ime-mode: inactive\" onchange=\"change_flg()\" style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["BIRTH_Y"] = knjCreateTextBox($objForm, $Row["BIRTH_Y"], "BIRTH_Y", 2, 2, $extra);
        //月
        $extra = "STYLE=\"ime-mode: inactive\" onchange=\"change_flg()\" style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["BIRTH_M"] = knjCreateTextBox($objForm, $Row["BIRTH_M"], "BIRTH_M", 2, 2, $extra);
        //日
        $extra = "STYLE=\"ime-mode: inactive\" onchange=\"change_flg()\" style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["BIRTH_D"] = knjCreateTextBox($objForm, $Row["BIRTH_D"], "BIRTH_D", 2, 2, $extra);

        //志望類型
        $query = knjl011dQuery::get_name_cd($model->year, "L058");
        $extra = "onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        makeCmb($objForm, $arg, $db, $query, $Row["DESIREDIV"], "DESIREDIV", $extra, 1, "");

        global $sess;
        //郵便番号入力支援(志願者)
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"isZipcd(this), toCopytxt(0, this.value)\" onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["ZIPCD"] = knjCreateTextBox($objForm, $Row["ZIPCD"], "ZIPCD", 10, "", $extra);

        //読込ボタンを作成する
        $extra = "style=\"width:140px\"onclick=\"loadwindow('" .REQUESTROOT ."/common/search_zipcd.php?cmd=search&addrname=ADDRESS1&addr2name=ADDRESS2&zipname=ZIPCD&ZIP_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $arg["data"]["ZIPCD"] .= knjCreateBtn($objForm, "btn_zip", "郵便番号入力支援", $extra);

        //確定ボタンを作成する
        $extra = "onclick=\"hiddenWin('" .REQUESTROOT ."/common/search_zipcd.php?cmd=apply&addrname=ADDRESS1&addr2name=ADDRESS2&zipname=ZIPCD&zip='+document.forms[0]['ZIPCD'].value+'&frame='+getFrameName(self))\"";
        $arg["data"]["ZIPCD"] .= knjCreateBtn($objForm, "btn_apply", "確定", $extra);

        //住所(志願者)
        $extra = "onblur=\"toCopytxt(1, this.value)\" onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["ADDRESS1"] = knjCreateTextBox($objForm, $Row["ADDRESS1"], "ADDRESS1", 60, 150, $extra);

        //方書(志願者)
        $extra = "onblur=\"toCopytxt(2, this.value)\" onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["ADDRESS2"] = knjCreateTextBox($objForm, $Row["ADDRESS2"], "ADDRESS2", 60, 150, $extra);

        //電話番号(志願者)
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"this.value=toTelNo(this.value),toCopytxt(3, this.value)\" onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["TELNO"] = knjCreateTextBox($objForm, $Row["TELNO"], "TELNO", 14, 14, $extra);

        //出身学校コード
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\" id=\"FINSCHOOLCD_ID\" ";
        $arg["data"]["FINSCHOOLCD"] = knjCreateTextBox($objForm, $Row["FS_CD"], "FINSCHOOLCD", 7, 7, $extra);

        //学校名
        $query = knjl011dQuery::getFinschoolName($Row["FS_CD"]);
        $fsArray = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["FINSCHOOLNAME"] = $fsArray["FINSCHOOL_DISTCD_NAME"].$fsArray["FINSCHOOL_NAME"];

        //卒業年
        $Row["FS_GRDYEAR"] = ($Row["FS_GRDYEAR"]) ? $Row["FS_GRDYEAR"]: $model->year;
        $extra = "STYLE=\"ime-mode: inactive\" onchange=\"change_flg()\" style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["FS_GRDYEAR"] = knjCreateTextBox($objForm, $Row["FS_GRDYEAR"], "FS_GRDYEAR", 4, 4, $extra);

        //かな検索ボタン（出身学校）
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=searchMain&fscdname=FINSCHOOLCD_ID&fsname=FINSCHOOLNAME_ID&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["button"]["btn_fin_kana_reference"] = knjCreateBtn($objForm, "btn_fin_kana_reference", "検 索", $extra);

        //備考1(全角で40文字)
        $extra = "STYLE=\"ime-mode: active\" onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["REMARK1"] = knjCreateTextBox($objForm, $Row["REMARK1"], "REMARK1", 41, 80, $extra);

        //備考2(全角で40文字)
        $extra = "STYLE=\"ime-mode: active\" onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["REMARK2"] = knjCreateTextBox($objForm, $Row["REMARK2"], "REMARK2", 41, 80, $extra);

        //塾１
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"this.value=toInteger(this.value)\"; id=\"PRISCHOOLCD1_ID\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["PRISCHOOLCD1"] = knjCreateTextBox($objForm, $Row["PRISCHOOLCD1"], "PRISCHOOLCD1", 7, 7, $extra);
        //教室コード１
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"this.value=toInteger(this.value)\"; id=\"PRISCHOOL_CLASS_CD1_ID\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["PRISCHOOL_CLASS_CD1"] = knjCreateTextBox($objForm, $Row["PRISCHOOL_CLASS_CD1"], "PRISCHOOL_CLASS_CD1", 7, 7, $extra);
        //かな検索ボタン（塾）１
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_PRISCHOOL/knjwpri_searchindex.php?cmd=searchMain&pricd=PRISCHOOLCD1_ID&priname=label_priName1&priclasscd=PRISCHOOL_CLASS_CD1_ID&priclassname=label_priClassName1&priaddr=&prischool_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY - 200 + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 280)\"";
        $arg["button"]["btn_pri_kana_reference1"] = knjCreateBtn($objForm, "btn_pri_kana_reference1", "検 索", $extra);
        //塾名１
        $query = knjl011dQuery::getPriSchoolName($Row["PRISCHOOLCD1"]);
        $setFin1 = $db->getOne($query);
        $arg["data"]["PRISCHOOL_NAME1"] = $setFin1;
        //教室名１
        $query = knjl011dQuery::getPriSchoolClassName($Row["PRISCHOOLCD1"], $Row["PRISCHOOL_CLASS_CD1"]);
        $setFin1 = $db->getOne($query);
        $arg["data"]["PRISCHOOL_CLASS_NAME1"] = $setFin1;

        //塾２
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"this.value=toInteger(this.value)\"; id=\"PRISCHOOLCD2_ID\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["PRISCHOOLCD2"] = knjCreateTextBox($objForm, $Row["PRISCHOOLCD2"], "PRISCHOOLCD2", 7, 7, $extra);
        //教室コード２
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"this.value=toInteger(this.value)\"; id=\"PRISCHOOL_CLASS_CD2_ID\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["PRISCHOOL_CLASS_CD2"] = knjCreateTextBox($objForm, $Row["PRISCHOOL_CLASS_CD2"], "PRISCHOOL_CLASS_CD2", 7, 7, $extra);
        //かな検索ボタン（塾）２
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_PRISCHOOL/knjwpri_searchindex.php?cmd=searchMain&pricd=PRISCHOOLCD2_ID&priname=label_priName2&priclasscd=PRISCHOOL_CLASS_CD2_ID&priclassname=label_priClassName2&priaddr=&prischool_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY - 200 + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 280)\"";
        $arg["button"]["btn_pri_kana_reference2"] = knjCreateBtn($objForm, "btn_pri_kana_reference2", "検 索", $extra);
        //塾名２
        $query = knjl011dQuery::getPriSchoolName($Row["PRISCHOOLCD2"]);
        $setFin2 = $db->getOne($query);
        $arg["data"]["PRISCHOOL_NAME2"] = $setFin2;
        //教室名２
        $query = knjl011dQuery::getPriSchoolClassName($Row["PRISCHOOLCD2"], $Row["PRISCHOOL_CLASS_CD2"]);
        $setFin2 = $db->getOne($query);
        $arg["data"]["PRISCHOOL_CLASS_NAME2"] = $setFin2;

        //名称マスタより和暦の元号を取得
        $result = $db->query(knjl011dQuery::get_calendarno($model->year));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            if ($calno == "") {
                $calno = $row["NAMECD2"];
                $spare = $row["NAMESPARE1"];
                $spare2 = $row["NAMESPARE2"];
                $spare3 = $row["NAMESPARE3"];
            } else {
                $calno.= "," . $row["NAMECD2"];
                $spare.= "," . $row["NAMESPARE1"];
                $spare2.= "," . $row["NAMESPARE2"];
                $spare3.= "," . $row["NAMESPARE3"];
            }
            $arg["data2"][] = array("eracd" => $row["NAMECD2"], "wname" => $row["NAME1"]);
        }

        //------------------------------保護者情報-------------------------------------
        //氏名(保護者)
        $extra = "STYLE=\"ime-mode: active\" onChange=\"change_flg()\" id=\"GNAME\" onkeyup=\"keySet('GNAME', 'GKANA', 'H');\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["GNAME"] = knjCreateTextBox($objForm, $Row["GNAME"], "GNAME", 40, 60, $extra);

        //氏名かな(保護者)
        $extra = "STYLE=\"ime-mode: active\" onChange=\"change_flg();\" id=\"GKANA\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["GKANA"] = knjCreateTextBox($objForm, $Row["GKANA"], "GKANA", 40, 120, $extra);

        //続柄コンボ
        $query = knjl011dQuery::get_name_cd($model->year, "H201");
        $extra = "onChange=\"change_flg()\" onkeydown=\"changeEnterToTab(this)\"";
        $relationship = (!isset($Row["EXAMNO"])) ? "01" : $Row["RELATIONSHIP"] ;
        makeCmb($objForm, $arg, $db, $query, $relationship, "RELATIONSHIP", $extra, 1, "BLANK");

        //郵便番号入力支援(保護者)
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"isZipcd(this)\" onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["GZIPCD"] = knjCreateTextBox($objForm, $Row["GZIPCD"], "GZIPCD", 10, "", $extra);

        //読込ボタンを作成する
        $extra = "style=\"width:140px\"onclick=\"loadwindow('" .REQUESTROOT ."/common/search_zipcd.php?cmd=search&addrname=GADDRESS1&addr2name=GADDRESS2&zipname=GZIPCD&ZIP_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $arg["data"]["GZIPCD"] .= knjCreateBtn($objForm, "btn_gzip", "郵便番号入力支援", $extra);

        //確定ボタンを作成する
        $extra = "onclick=\"hiddenWin('" .REQUESTROOT ."/common/search_zipcd.php?cmd=apply&addrname=GADDRESS1&addr2name=GADDRESS2&zipname=GZIPCD&zip='+document.forms[0]['GZIPCD'].value+'&frame='+getFrameName(self))\"";
        $arg["data"]["GZIPCD"] .= knjCreateBtn($objForm, "btn_apply", "確定", $extra);

        //住所(保護者)
        $extra = "onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["GADDRESS1"] = knjCreateTextBox($objForm, $Row["GADDRESS1"], "GADDRESS1", 60, 150, $extra);

        //方書(保護者)
        $extra = "onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["GADDRESS2"] = knjCreateTextBox($objForm, $Row["GADDRESS2"], "GADDRESS2", 60, 150, $extra);

        //電話番号(保護者)
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"this.value=toTelNo(this.value)\" onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["GTELNO"] = knjCreateTextBox($objForm, $Row["GTELNO"], "GTELNO", 14, 14, $extra);

        //-------------------------------- ボタン作成 ------------------------------------
        $gzip = $Row["GZIPCD"];
        $gadd = $Row["GADDRESS1"];

        //検索ボタン
        $extra = "onclick=\"return btn_submit('reference', '".$gzip."', '".$gadd."');\"";
        $arg["button"]["btn_reference"] = knjCreateBtn($objForm, "btn_reference", "検 索", $extra);

        //かな検索ボタン
        $extra = "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL011D/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&applicantdiv='+document.forms[0]['APPLICANTDIV'].value+'&testdiv='+document.forms[0]['TESTDIV'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $arg["button"]["btn_kana_reference"] = knjCreateBtn($objForm, "btn_kana_reference", "かな検索", $extra);

        //前の志願者検索ボタン
        $extra = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onClick=\"btn_submit('back1', '".$gzip."', '".$gadd."');\"";
        $arg["button"]["btn_back_next"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);

        //次の志願者検索ボタン
        $extra = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onClick=\"btn_submit('next1', '".$gzip."', '".$gadd."');\"";
        $arg["button"]["btn_back_next"] .= knjCreateBtn($objForm, "btn_next", " >> ", $extra);

        //画面クリアボタン
        $extra = "style=\"width:90px\" onclick=\"return btn_submit('disp_clear');\"";
        $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "画面クリア", $extra);

        //志願者よりコピーボタン
        $extra = "$disabled style=\"width:135px\" onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "志願者よりコピー", $extra);

        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);
        //更新ボタン
        $extra = "$disabled onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_udpate"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);
        //更新ボタン(更新後前の志願者)
        $extra = "$disabled style=\"width:150px\" onclick=\"return btn_submit('back');\"";
        $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_pre", "更新後前の志願者", $extra);
        //更新ボタン(更新後次の志願者)
        $extra = "$disabled style=\"width:150px\" onclick=\"return btn_submit('next');\"";
        $arg["button"]["btn_up_next"] .= knjCreateBtn($objForm, "btn_up_next", "更新後次の志願者", $extra);
        //削除ボタン
        $extra = "$disabled onclick=\"return btn_submit('delete');\"";
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
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl011dForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($name == 'TESTDIV') {
            if ($value == "" && $row["NAMESPARE2"] == '1') $value = $row["VALUE"];
        }
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>