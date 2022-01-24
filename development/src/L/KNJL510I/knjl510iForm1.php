<?php
class knjl510iForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"] = $objForm->get_start("edit", "POST", "knjl510iindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();


        //一覧表示
        if (!isset($model->warning)) {
            //データを取得
            $query = knjl510iQuery::getSelectQuery($model, $model->examno);
            $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
            if ($model->cmd == 'back1' || $model->cmd == 'next1') {
                if (!is_array($Row)) {
                    if ($model->cmd == 'back1' || $model->cmd == 'next1') {
                        $model->setWarning("MSG303", "更新しましたが、移動先のデータが存在しません。");
                    }
                    $model->cmd = "main";
                    $query = knjl510iQuery::getSelectQuery($model, $model->examno);
                    $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                }
                $model->examno = $Row["EXAMNO"];
            }
            if (!is_array($Row)) {
                if ($model->cmd == 'reference') {
                    $model->setWarning("MSG303");
                }
                $Row["EXAMNO"] = $model->examno;
            }
        } else {
            $Row =& $model->field;
            $Row["EXAMNO"] = $model->examno;
        }

        //入試年度
        $arg["TOP"]["YEAR"] = $model->year;

        //入試制度
        $applicantRow = $db->getRow(knjl510iQuery::getNameMst($model, "L003", $model->applicantdiv), DB_FETCHMODE_ASSOC);
        $arg["data"]["APPLICANTDIV"] = $applicantRow["LABEL"];

        //学科
        $extra = "";
        $opt = array();
        $opt[] = array("label" => "", "value" => "");
        $opt[] = array("label" => "1:普通科", "value" => "1");
        $opt[] = array("label" => "2:工業科", "value" => "2");
        $arg["data"]["TESTDIV0"] = knjCreateCombo($objForm, "TESTDIV0", $Row["TESTDIV0"], $opt, $extra, $size);

        //入試区分
        $query = knjl510iQuery::getTestdivMst($model);
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["TESTDIV"], "TESTDIV", $extra, 1, "BLANK");

        //受験番号
        $extra = "";
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $Row["EXAMNO"], "EXAMNO", $model->examnoKeta, $model->examnoKeta, $extra);

        //重複チェックボックス
        $extra = " disabled ";
        for ($idx = 1; $idx <= $model->maxAltExamnoNum; $idx++) {
            if ($Row["ALT_EXAMNO".$idx] && $Row["ALT_EXAMNO".$idx] != $Row["EXAMNO"]) {
                $extra .= " checked='checked' "; //他受験番号が存在する場合、チェックon
            }
        }
        $arg["data"]["DUPLICATURE"] = knjCreateCheckBox($objForm, "DUPLICATURE", "1", $extra);

        //氏名
        $extra = "";
        $arg["data"]["NAME"] = knjCreateTextBox($objForm, $Row["NAME"], "NAME", $model->nameKeta, $model->nameKeta, $extra);

        //氏名かな
        $extra = "";
        $arg["data"]["NAME_KANA"] = knjCreateTextBox($objForm, $Row["NAME_KANA"], "NAME_KANA", $model->nameKanaKeta, $model->nameKanaKeta, $extra);

        //生年月日
        $disabled = "";
        $arg["data"]["BIRTHDAY"] = View::popUpCalendarAlp($objForm, "BIRTHDAY", str_replace("-", "/", $Row["BIRTHDAY"]), $disabled, "");
        
        //CSV_生年月日
        $arg["data"]["CSV_BIRTHDAY"] = $model->getSlashDate($Row["CSV_BIRTHDAY"]);

        //性別
        $query = knjl510iQuery::getNameMst($model, "Z002");
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["SEX"], "SEX", $extra, 1, "BLANK");

        //過年度チェックボックス
        $extra = " id=\"SEQ031_REMARK5\" ";
        if ($Row["SEQ031_REMARK5"] == "1") {
            $extra .= " checked='checked' ";
        }
        $arg["data"]["SEQ031_REMARK5"] = knjCreateCheckBox($objForm, "SEQ031_REMARK5", "1", $extra);

        //出身学校コード
        $extra = "onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\" id=\"FINSCHOOLCD_ID\" ";
        $arg["data"]["FINSCHOOLCD"] = knjCreateTextBox($objForm, $Row["FINSCHOOLCD"], "FINSCHOOLCD", $model->fsCdKeta, $model->fsCdKeta, $extra);

        //学校名
        $query = knjl510iQuery::getFinschoolName($Row["FINSCHOOLCD"]);
        $fsArray = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["FINSCHOOLNAME"] = $fsArray["FINSCHOOL_NAME"]."　".$fsArray["FINSCHOOL_DISTCD_NAME"];

        //かな検索ボタン（出身学校）
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=searchMain3&fscdname=FINSCHOOLCD_ID&fsname=FINSCHOOLNAME_ID&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["button"]["btn_fin_kana_reference"] = knjCreateBtn($objForm, "btn_fin_kana_reference", "検 索", $extra);

        //卒業年度
        $extra = "";
        $arg["data"]["FS_GRDYEAR"] = knjCreateTextBox($objForm, $Row["FS_GRDYEAR"], "FS_GRDYEAR", $model->yearKeta, $model->yearKeta, $extra);

        //保護者氏名
        $extra = "";
        $arg["data"]["GNAME"] = knjCreateTextBox($objForm, $Row["GNAME"], "GNAME", $model->nameKeta, $model->nameKeta, $extra);

        //保護者氏名かな
        $extra = "";
        $arg["data"]["GKANA"] = knjCreateTextBox($objForm, $Row["GKANA"], "GKANA", $model->nameKanaKeta, $model->nameKanaKeta, $extra);

        //郵便番号入力支援(保護者)
        global $sess;
        $extra = " STYLE=\"ime-mode: inactive;\" onblur=\"isZipcd(this), toCopytxt(0, this.value)\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GZIPCD"] = knjCreateTextBox($objForm, $Row["GZIPCD"], "GZIPCD", 10, "", $extra);

        //読込ボタンを作成する
        $extra = "style=\"width:140px\"onclick=\"loadwindow('" .REQUESTROOT ."/common/search_zipcd.php?cmd=search&addrname=GADDRESS1&zipname=GZIPCD&ZIP_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $arg["data"]["GZIPCD"] .= knjCreateBtn($objForm, "btn_zip", "郵便番号入力支援", $extra);

        //確定ボタンを作成する
        $extra = "onclick=\"hiddenWin('" .REQUESTROOT ."/common/search_zipcd.php?cmd=apply&addrname=GADDRESS1&zipname=GZIPCD&zip='+document.forms[0]['GZIPCD'].value+'&frame='+getFrameName(self))\"";
        $arg["data"]["GZIPCD"] .= knjCreateBtn($objForm, "btn_apply", "確定", $extra);

        //住所1
        $extra = "";
        $arg["data"]["GADDRESS1"] = knjCreateTextBox($objForm, $Row["GADDRESS1"], "GADDRESS1", $model->addrKeta, $model->addrKeta, $extra);

        //住所2
        $extra = "";
        $arg["data"]["GADDRESS2"] = knjCreateTextBox($objForm, $Row["GADDRESS2"], "GADDRESS2", $model->addrKeta, $model->addrKeta, $extra);

        //電話番号
        $extra = " STYLE=\"ime-mode: inactive;\" onblur=\"this.value=toTelNo(this.value)\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GTELNO"] = knjCreateTextBox($objForm, $Row["GTELNO"], "GTELNO", $model->telNoKeta, $model->telNoKeta, $extra);

        //志望コース
        $query = knjl510iQuery::getGeneralMst($model, "02");
        $extra = "";
        for ($idx = 1; $idx <= $model->maxHopeCourseNum; $idx++) {
            makeCmb($objForm, $arg, $db, $query, $Row["HOPE_COURSE".$idx], "HOPE_COURSE".$idx, $extra, 1, "BLANK");
            $generalRow = $db->getRow(knjl510iQuery::getGeneralMst($this, "02", $Row["HOPE_COURSE".$idx]), DB_FETCHMODE_ASSOC);
            knjCreateHidden($objForm, "HOPE_CLASSIFICATION".$idx, $generalRow["REMARK1"]);
        }

        //奨学希望チェックボックス
        $extra = " id=\"DORMITORY_FLG\" ";
        if ($Row["DORMITORY_FLG"] == "1") {
            $extra .= " checked='checked' ";
        }
        $arg["data"]["DORMITORY_FLG"] = knjCreateCheckBox($objForm, "DORMITORY_FLG", "1", $extra);

        //入寮希望チェックボックス
        $extra = " id=\"SEQ031_REMARK6\" ";
        if ($Row["SEQ031_REMARK6"] == "1") {
            $extra .= " checked='checked' ";
        }
        $arg["data"]["SEQ031_REMARK6"] = knjCreateCheckBox($objForm, "SEQ031_REMARK6", "1", $extra);

        //他受験番号
        $extra = "";
        for ($idx = 1; $idx <= $model->maxAltExamnoNum; $idx++) {
            $field = "ALT_EXAMNO".$idx;
            $arg["data"][$field] = knjCreateTextBox($objForm, $Row[$field], $field, $model->examnoKeta, $model->examnoKeta, $extra);
        }

        //ボタン作成
        makeBtn($objForm, $arg, $model, $sess);

        //hidden作成
        makeHidden($objForm, $model);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl510iForm1.html", $arg);
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
        if ($value === $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model, $sess)
{

    //検索ボタン
    $extra = "onclick=\"return btn_submit('reference');\"";
    $arg["button"]["btn_reference"] = knjCreateBtn($objForm, "btn_reference", "検 索", $extra);

    //受験生検索ボタン
    $extra = "style=\"width:90px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL510I/search_name.php?cmd=search&year={$model->year}&applicantdiv={$model->applicantdiv}&testdiv0='+document.forms[0]['TESTDIV0'].value+'&testdiv='+document.forms[0]['TESTDIV'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 420, 360)\"";
    $arg["button"]["btn_kana_reference"] = knjCreateBtn($objForm, "btn_kana_reference", "受験生検索", $extra);

    //前の志願者検索ボタン
    $extra = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onClick=\"btn_submit('back1');\"";
    $arg["button"]["btn_back_next"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);

    //次の志願者検索ボタン
    $extra = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onClick=\"btn_submit('next1');\"";
    $arg["button"]["btn_back_next"] .= knjCreateBtn($objForm, "btn_next", " >> ", $extra);

    //画面クリアボタン
    $extra = "style=\"width:90px\" onclick=\"return btn_submit('disp_clear');\"";
    $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "画面クリア", $extra);

    //追加ボタン
    $extra = "onclick=\"return btn_submit('add');\"";
    $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //更新ボタン(更新後前の志願者)
    $extra = "onclick=\"return btn_submit('back');\"";
    $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_pre", "更新後前の志願者", $extra);

    //更新ボタン(更新後次の志願者)
    $extra = "onclick=\"return btn_submit('next');\"";
    $arg["button"]["btn_up_next"] .= knjCreateBtn($objForm, "btn_up_next", "更新後次の志願者", $extra);

    //CSVボタン
    $extra = "onClick=\" wopen('".REQUESTROOT."/X/KNJX_L510I/knjx_l510iindex.php?YEAR={$model->year}&APPLICANTDIV={$model->applicantdiv}','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
    $arg["button"]["btn_csv"] = KnjCreateBtn($objForm, "btn_csv", "チェックリストCSV出力", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);
}

function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
}
