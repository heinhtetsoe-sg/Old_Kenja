<?php

require_once('for_php7.php');

class knjl011rForm1
{
    function main(&$model)
    {
        $objForm      = new form;
        $arg["start"] = $objForm->get_start("main", "POST", "knjl011rindex.php", "", "main");

        //一覧表示
        if ((!isset($model->warning)) && (!is_array($existdata)) && $model->cmd != 'addnew') {
            //データを取得
            $Row = knjl011rQuery::get_edit_data($model);
            if ($model->cmd == 'back' || $model->cmd == 'next' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                if (!is_array($Row)) {
                    if ($model->cmd == 'back' || $model->cmd == 'next') {
                        $model->setWarning("MSG303","更新しましたが、移動先のデータが存在しません。");
                    }
                    $model->cmd = "main";
                    $Row = knjl011rQuery::get_edit_data($model);
                }
                $model->examno = $Row["EXAMNO"];
                $model->applicantdiv = $Row["APPLICANTDIV"];
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

        $arg["TOP"]["YEAR"] = $model->year;
        if ($model->cmd == 'showdivAdd') {
            $Row["APPLICANTDIV"] = $model->field["APPLICANTDIV"];
            $Row["TESTDIV"] = $model->field["TESTDIV"];
            $Row["FS_CD"] = $model->field["FS_CD"];
            for ($code = 1; $code <= 4; $code++) {
                $Row["TESTDIV".$code] = $model->field["TESTDIV".$code];
            }
            $arg["moveCursor"] = "moveCursor();";
        }

        if (isset($Row["EXAMNO"])) {
            $model->checkexam = $Row["EXAMNO"];
        }

        //DB接続
        $db = Query::dbCheckOut();

        //------------------------------志願者情報-------------------------------------
        //入試制度コンボ
        $query = knjl011rQuery::get_name_cd($model, "L003", $model->fixApplicantDiv);
        $extra = "onChange=\"return btn_submit('main');\"";
        makeCmb($objForm, $arg, $db, $query, $model->applicantdiv, "APPLICANTDIV", $extra, 1, "");

        //受験番号
        $extra = "onChange=\"btn_disabled();\" onblur=\"this.value=toAlphaNumber(this.value);\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $model->examno, "EXAMNO", 5, 5, $extra);

        //入試区分
        $query = knjl011rQuery::get_name_cd($model, "L004");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["TESTDIV"], "TESTDIV", $extra, 1, "BLANK");

        //事前
        $arg["data"]["JIZEN"] = $Row["JIZEN"];
        //事前データの入試区分と受験コースを取得
        $jizen_data = array();
        $jizen_data = $db->getRow(knjl011rQuery::getEntexamApplicantBeforeDat($model, $model->year, $model->applicantdiv, $Row["TESTDIV"], $Row["JIZEN_PAGE"], $Row["JIZEN_SEQ"]), DB_FETCHMODE_ASSOC);
        if ($jizen_data["JIZEN_TEST_CD"] != "" || $jizen_data["EXAMCOURSE_MARK"] != "") {
            $arg["data"]["JIZEN_SET_DATA"] = '('.$jizen_data["JIZEN_TEST_CD"].'-'.$jizen_data["EXAMCOURSE_MARK"].')';
        }

        //志望区分チェックボックス
        $arg["data"]["DESIREDIV"] = $Row["DESIREDIV"] ? "({$Row["DESIREDIV"]})" : "";
        $model->all_testdiv = array();
        $result = $db->query(knjl011rQuery::getCourseMst($model->year));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $row["EXAMCOURSECD"] = $row["EXAMCOURSECD"] * 1;
            $fieldname = "TESTDIV".$row["EXAMCOURSECD"];
            $checked = strlen($Row[$fieldname]) ? "checked" : "";
            $extra = "onChange=\"change_flg()\" id=\"$fieldname\" " .$checked;
            $arg["data"][$fieldname] = knjCreateCheckBox($objForm, $fieldname, $row["EXAMCOURSECD"], $extra);
            $arg["data"][$fieldname."_ID"] = $fieldname;
            $arg["data"][$fieldname."_NAME"] = $row["EXAMCOURSE_NAME"];
            $model->all_testdiv[$row["EXAMCOURSECD"]] = $row["EXAMCOURSE_NAME"];
        }
        $result->free();

        //氏名(志願者)
        $extra = "style=\"background-color: pink;\" onChange=\"change_flg()\" onkeyup=\" keySet('NAME', 'NAME_KANA', 'K');\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["NAME"] = knjCreateTextBox($objForm, $Row["NAME"], "NAME", 40, 60, $extra);

        //氏名かな(志願者)
        $extra = "style=\"background-color: pink;\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["NAME_KANA"] = knjCreateTextBox($objForm, $Row["NAME_KANA"], "NAME_KANA", 40, 120, $extra);

        //性別コンボ
        $query = knjl011rQuery::get_name_cd($model, "Z002");
        $extra = "style=\"background-color: pink;\" onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SEX"], "SEX", $extra, 1, "BLANK");

        $gengouCd = "";
        $gengouName = "";
        $birthComent = "";
        //名称マスタより和暦の元号を取得
        $result = $db->query(knjl011rQuery::get_calendarno($model->year));
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
            if ($row["NAMESPARE2"] <= $model->year."/03/01" && $model->year."/03/01" <= $row["NAMESPARE3"]) {
                $gengouCd = $row["NAMECD2"];
                $gengouName = $row["NAME1"];
            }
            $birthComent .= $row["NAMECD2"].":".$row["NAME1"]." ";
        }

        //生年月日元号
        $extra = "STYLE=\"ime-mode: inactive; text-align: center; background-color: pink;\" onblur=\" toDatecheck(0, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."'), setName(this);\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["ERACD"] = knjCreateTextBox($objForm, strlen($Row["ERACD"]) ? $Row["ERACD"] : "4", "ERACD", 1, 1, $extra);

        if (isset($Row["NAME1"])) {
            $name1 = $Row["NAME1"];
        } else if(isset($Row["WNAME"])) {
            $name1 = str_replace("&nbsp;", "", $Row["WNAME"]);
        } else {
            $name1 = "平成";
        }

        $arg["data"]["WNAME"] = $name1;

        //和暦名
        knjCreateHidden($objForm, "WNAME", $name1);

        //生年月日年
        $extra = "STYLE=\"ime-mode: inactive; text-align: center; background-color: pink;\" onblur=\" toDatecheck(1, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["BIRTH_Y"] = knjCreateTextBox($objForm, $Row["BIRTH_Y"], "BIRTH_Y", 2, 2, $extra);

        //生年月日月
        $extra = "STYLE=\"ime-mode: inactive; text-align: center; background-color: pink;\" onblur=\" toDatecheck(2, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["BIRTH_M"] = knjCreateTextBox($objForm, $Row["BIRTH_M"], "BIRTH_M", 2, 2, $extra);

        //生年月日日
        $extra = "STYLE=\"ime-mode: inactive; text-align: center; background-color: pink;\" onblur=\" toDatecheck(3, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["BIRTH_D"] = knjCreateTextBox($objForm, $Row["BIRTH_D"], "BIRTH_D", 2, 2, $extra);

        //生年月日コメント
        $arg["data"]["BIRTH_COMENT"] = $birthComent;

        global $sess;
        //郵便番号入力支援(志願者)
        $extra = "onblur=\"isZipcd(this), toCopytxt(0, this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["ZIPCD"] = knjCreateTextBox($objForm, $Row["ZIPCD"], "ZIPCD", 10, "", $extra);

        //読込ボタンを作成する
        $extra = "style=\"width:140px\"onclick=\"loadwindow('" .REQUESTROOT ."/common/search_zipcd.php?cmd=search&addrname=ADDRESS1&zipname=ZIPCD&ZIP_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $arg["data"]["ZIPCD"] .= knjCreateBtn($objForm, "btn_zip", "郵便番号入力支援", $extra);

        //確定ボタンを作成する
        $extra = "onclick=\"hiddenWin('" .REQUESTROOT ."/common/search_zipcd.php?cmd=apply&addrname=ADDRESS1&zipname=ZIPCD&zip='+document.forms[0]['ZIPCD'].value+'&frame='+getFrameName(self))\"";
        $arg["data"]["ZIPCD"] .= knjCreateBtn($objForm, "btn_apply", "確定", $extra);

        //住所(志願者)
        $extra = "onblur=\"toCopytxt(1, this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["ADDRESS1"] = knjCreateTextBox($objForm, $Row["ADDRESS1"], "ADDRESS1", 50, 50, $extra);

        //方書(志願者)
        $extra = "onblur=\"toCopytxt(2, this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["ADDRESS2"] = knjCreateTextBox($objForm, $Row["ADDRESS2"], "ADDRESS2", 50, 50, $extra);

        //電話番号(志願者)
        $extra = "onblur=\"this.value=toTelNo(this.value), toCopytxt(3, this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["TELNO"] = knjCreateTextBox($objForm, $Row["TELNO"], "TELNO", 14, 14, $extra);

        //出身学校コード
        $extra = "style=\"background-color: pink;\" onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["FINSCHOOLCD"] = knjCreateTextBox($objForm, $Row["FS_CD"], "FINSCHOOLCD", 7, 7, $extra);

        //学校名
        $query = knjl011rQuery::getFinschoolName($Row["FS_CD"]);
        $fsArray = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["FINSCHOOLNAME"] = $fsArray["FINSCHOOL_NAME"];

        //かな検索ボタン（出身学校）
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=&fscdname=&fsname=&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["button"]["btn_fin_kana_reference"] = knjCreateBtn($objForm, "btn_fin_kana_reference", "検 索", $extra);

        //出身学校名
        $extra = "onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["FS_NAME"] = knjCreateTextBox($objForm, $Row["FS_NAME"], "FS_NAME", 30, 30, $extra);

        //卒業元号
        $extra = "STYLE=\"ime-mode: inactive; text-align: center;\" onblur=\" toDatecheck(0, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."'), setName(this);\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["FS_ERACD"] = knjCreateTextBox($objForm, strlen($Row["FS_ERACD"]) ? $Row["FS_ERACD"] : $gengouCd, "FS_ERACD", 1, 1, $extra);

        //卒業和暦名
        $fs_wname = isset($Row["FS_WNAME"]) ? str_replace("&nbsp;", "", $Row["FS_WNAME"]) : $gengouName;
        knjCreateHidden($objForm, "FS_WNAME", $fs_wname);
        $arg["data"]["FS_WNAME"] = $fs_wname;

        //卒業年
        $extra = "STYLE=\"ime-mode: inactive; text-align: center;\" onblur=\" toDatecheck(1, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["FS_Y"] = knjCreateTextBox($objForm, $Row["FS_Y"], "FS_Y", 2, 2, $extra);

        //卒業月
        $defGrdmon = $model->cmd == 'showdivAdd' ? "03" : "";
        $extra = "STYLE=\"ime-mode: inactive; text-align: center;\" onblur=\" toDatecheck(2, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["FS_M"] = knjCreateTextBox($objForm, strlen($Row["FS_M"]) ? $Row["FS_M"] : $defGrdmon, "FS_M", 2, 2, $extra);

        //------------------------------保護者情報-------------------------------------
        //氏名(保護者)
        $extra = "onChange=\"change_flg()\" onkeyup=\" keySet('GNAME', 'GKANA', 'K');\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GNAME"] = knjCreateTextBox($objForm, $Row["GNAME"], "GNAME", 40, 60, $extra);

        //続柄コンボ
        $query = knjl011rQuery::get_name_cd($model, "H201");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["RELATIONSHIP"], "RELATIONSHIP", $extra, 1, "BLANK");

        //氏名かな(保護者)
        $extra = "onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GKANA"] = knjCreateTextBox($objForm, $Row["GKANA"], "GKANA", 80, 120, $extra);

        //郵便番号入力支援(保護者)
        $arg["data"]["GZIPCD"] = View::popUpZipCode($objForm, "GZIPCD", $Row["GZIPCD"], "GADDRESS1");

        //住所(保護者)
        $extra = "onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GADDRESS1"] = knjCreateTextBox($objForm, $Row["GADDRESS1"], "GADDRESS1", 50, 50, $extra);

        //方書(保護者)
        $extra = "onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GADDRESS2"] = knjCreateTextBox($objForm, $Row["GADDRESS2"], "GADDRESS2", 50, 50, $extra);

        //電話番号(保護者)
        $extra = "onblur=\"this.value=toTelNo(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GTELNO"] = knjCreateTextBox($objForm, $Row["GTELNO"], "GTELNO", 14, 14, $extra);

        //------------------------------内申-------------------------------------
        $extra = "style=\"background-color: pink;\" onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["TOTAL_ALL"] = knjCreateTextBox($objForm, $Row["TOTAL_ALL"], "TOTAL_ALL", 4, 4, $extra);

        $arg["data"]["KASANTEN_ALL"] = $Row["KASANTEN_ALL"];

        $extra = "style=\"background-color: pink;\" onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["ABSENCE_DAYS3"] = knjCreateTextBox($objForm, $Row["ABSENCE_DAYS3"], "ABSENCE_DAYS3", 4, 4, $extra);

        //-------------------------------- ボタン作成 ------------------------------------
        $zip = $Row["ZIPCD"];
        $gzip = $Row["GZIPCD"];
        $zadd = $Row["ADDRESS1"];
        $gadd = $Row["GADDRESS1"];

        //検索ボタン
        $extra = "onclick=\"return btn_submit('reference', '".$zip."', '".$gzip."', '".$zadd."', '".$gadd."');\"";
        $arg["button"]["btn_reference"] = knjCreateBtn($objForm, "btn_reference", "検 索", $extra);

        //かな検索ボタン
        $extra = "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL011R/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&applicantdiv='+document.forms[0]['APPLICANTDIV'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $arg["button"]["btn_kana_reference"] = knjCreateBtn($objForm, "btn_kana_reference", "カナ検索", $extra);

        //前の志願者検索ボタン
        $extra = "style=\"width:32px\" onClick=\"btn_submit('back1', '".$zip."', '".$gzip."', '".$zadd."', '".$gadd."');\"";
        $arg["button"]["btn_back_next"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);

        //次の志願者検索ボタン
        $extra = "style=\"width:32px\" onClick=\"btn_submit('next1', '".$zip."', '".$gzip."', '".$zadd."', '".$gadd."');\"";
        $arg["button"]["btn_back_next"] .= knjCreateBtn($objForm, "btn_next", " >> ", $extra);

        //画面クリアボタン
        $extra = "style=\"width:90px\" onclick=\"return btn_submit('disp_clear');\"";
        $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "画面クリア", $extra);

        //志願者よりコピーボタン
        $extra = "$disabled style=\"width:135px\" onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "志願者よりコピー", $extra);

        //新規ボタン
        $extra = "onclick=\"return btn_submit('addnew');\"";
        $arg["button"]["btn_addnew"] = knjCreateBtn($objForm, "btn_addnew", "新 規", $extra);

        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //更新ボタン
        $extra = "$disabled onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_udpate"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);

        //更新ボタン(更新後前の志願者)
        $extra = "$disabled style=\"width:150px\" onclick=\"return btn_submit('back');\"";
        $arg["button"]["btn_up_pre"] = knjCreateBtn($objForm, "btn_up_pre", "更新後前の志願者", $extra);

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

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl011rForm1.html", $arg);
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
        if ($name === 'TESTDIV') {
            if ($value == "" && $row["NAMESPARE2"] == '1') $value = $row["VALUE"];
        }
        if ($value == $row["VALUE"]) $value_flg = true;
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

?>