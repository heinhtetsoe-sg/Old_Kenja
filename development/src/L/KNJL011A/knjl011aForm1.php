<?php

require_once('for_php7.php');

class knjl011aForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"] = $objForm->get_start("main", "POST", "knjl011aindex.php", "", "main");

        //一覧表示
        if ((!isset($model->warning)) && (!is_array($existdata)) && $model->cmd != 'changeFscd') {
            unset($model->honorDiv1);
            unset($model->honorDiv2);
            unset($model->honorDiv3);
            unset($model->clubCd);
            unset($model->seq005R1);
            //データを取得
            $Row = knjl011aQuery::getEditData($model);
            if ($model->cmd == 'back' || $model->cmd == 'next' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                if (!is_array($Row)) {
                    if ($model->cmd == 'back' || $model->cmd == 'next') {
                        $model->setWarning("MSG303", "更新しましたが、移動先のデータが存在しません。");
                    }
                    $model->cmd = "main";
                    $Row = knjl011aQuery::getEditData($model);
                }
                $model->search_examno = $Row["SEARCH_EXAMNO"];
                $model->examno = $Row["EXAMNO"];
                $model->applicantdiv = $Row["APPLICANTDIV"];
            }
            $disabled = "";
            if (!is_array($Row)) {
                $disabled = "disabled";
                if ($model->cmd == 'reference' || $model->cmd == 'reference2') {
                    $model->examno = "";
                    $model->search_examno = "";
                    $model->setWarning("MSG303");
                }
            } else {
                if ($model->cmd == 'reference' || $model->cmd == 'reference2') {
                    $model->examno = $Row["EXAMNO"];
                }
            }
        } else {
            $Row =& $model->field;
        }

        $arg["TOP"]["YEAR"] = $model->year;

        if ($model->cmd == 'changeApp') {
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
        $setTextField[] = "SEX";
        $setTextField[] = "ERACD";
        $setTextField[] = "BIRTH_Y";
        $setTextField[] = "BIRTH_M";
        $setTextField[] = "BIRTH_D";
        $setTextField[] = "FINSCHOOLCD";
        $setTextField[] = "FS_ERACD";
        $setTextField[] = "FS_Y";
        $setTextField[] = "FS_M";
        $setTextField[] = "FS_GRDDIV";
        $setTextField[] = "GNAME";
        $setTextField[] = "GKANA";
        $setTextField[] = "RELATIONSHIP";
        $setTextField[] = "ZIPCD";
        $setTextField[] = "ADDRESS1";
        $setTextField[] = "ADDRESS2";
        $setTextField[] = "TELNO";
        $setTextField[] = "EMERGENCYTELNO";
        $setTextField[] = "PRISCHOOLCD1";
        $setTextField[] = "PRISCHOOL_CLASS_CD1";
        $setTextField[] = "PRISCHOOLCD2";
        $setTextField[] = "PRISCHOOL_CLASS_CD2";
        $setTextField[] = "PRISCHOOLCD3";
        $setTextField[] = "PRISCHOOL_CLASS_CD3";
        knjCreateHidden($objForm, "setTextField", implode(',', $setTextField));

        //受験校種(1,2)
        $query = knjl011aQuery::getNameCd($model->year, "L003");
        $extra = "onChange=\"change_flg(); return btn_submit('changeApp');\"";
        makeCmb($objForm, $arg, $db, $query, $model->applicantdiv, "APPLICANTDIV", $extra, 1, "");
        //受験校種(J,H)
        $model->schoolKind = $db->getOne(knjl011aQuery::getSchoolKind($model));

        //志願者SEQ
        $extra = " STYLE=\"ime-mode: inactive; background:#cccccc;\" readonly onChange=\"btn_disabled();\" ";
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $model->examno, "EXAMNO", 10, 10, $extra);

        //検索受験番号
        $extra = " STYLE=\"ime-mode: inactive\" onChange=\"btn_disabled();\" ";
        $arg["data"]["SEARCH_EXAMNO"] = knjCreateTextBox($objForm, $model->search_examno, "SEARCH_EXAMNO", 7, 7, $extra);

        //------------------------------志願者情報-------------------------------------
        //氏名(志願者)
        $extra = "STYLE=\"ime-mode: active\" onChange=\"change_flg();\" id=\"NAME\" onkeyup=\"keySet('NAME', 'NAME_KANA', 'H');\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["NAME"] = knjCreateTextBox($objForm, $Row["NAME"], "NAME", 40, 60, $extra);

        //氏名かな(志願者)
        $extra = "STYLE=\"ime-mode: active\" onChange=\"change_flg();\" id=\"NAME_KANA\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["NAME_KANA"] = knjCreateTextBox($objForm, $Row["NAME_KANA"], "NAME_KANA", 40, 120, $extra);

        //特待区分コンボ
        //上から順に選択する。2番目、3番目からの選択はできないように非活性化しておく。
        $extra = " disabled ";
        $query = knjl011aQuery::getHonordiv($model);
        //特待区分1
        $model->honorDiv1 = $Row["HONORDIV1"] ? $Row["HONORDIV1"] : $model->honorDiv1;
        makeCmb($objForm, $arg, $db, $query, $model->honorDiv1, "HONORDIV1", $extra, 1, "BLANK");
        //特待区分2
        $model->honorDiv2 = $Row["HONORDIV2"] ? $Row["HONORDIV2"] : $model->honorDiv2;
        makeCmb($objForm, $arg, $db, $query, $model->honorDiv2, "HONORDIV2", $extra, 1, "BLANK");
        //特待区分3
        $model->honorDiv3 = $Row["HONORDIV3"] ? $Row["HONORDIV3"] : $model->honorDiv3;
        makeCmb($objForm, $arg, $db, $query, $model->honorDiv3, "HONORDIV3", $extra, 1, "BLANK");

        //クラブコンボ
        //特待区分がクラブ特待(CLUB_FLG=1)の時のみ活性化する。
        $query = knjl011aQuery::getClubcd($model);
        $model->clubCd = $Row["CLUB_CD"] ? $Row["CLUB_CD"] : $model->clubCd;
        makeCmb($objForm, $arg, $db, $query, $model->clubCd, "CLUB_CD", $extra, 1, "BLANK");

        //英語見なし得点（英検取得級）コンボ
        $query = knjl011aQuery::getEiken($model->year, $model->applicantdiv);
        $model->seq005R1 = $Row["SEQ005_R1"] ? $Row["SEQ005_R1"] : $model->seq005R1;
        makeCmb($objForm, $arg, $db, $query, $model->seq005R1, "SEQ005_R1", $extra, 1, "BLANK");

        //性別(志願者)
        $query = knjl011aQuery::getNameCd($model->year, "Z002");
        $extra = "onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SEX"], "SEX", $extra, 1, "");

        //元号
        $query = knjl011aQuery::getNameCd($model->year, "L007");
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

        //出身学校コード
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg(); return btn_submit('changeFscd');\" onkeydown=\"changeEnterToTab(this)\" id=\"FINSCHOOLCD_ID\" ";
        $arg["data"]["FINSCHOOLCD"] = knjCreateTextBox($objForm, $Row["FS_CD"], "FINSCHOOLCD", 7, 7, $extra);

        //学校名
        $query = knjl011aQuery::getFinschoolName($Row["FS_CD"]);
        $fsArray = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["FINSCHOOLNAME"] = $fsArray["FINSCHOOL_DISTCD_NAME"].$fsArray["FINSCHOOL_NAME"];

        $gengouCd = "";
        //名称マスタより和暦の元号を取得
        $result = $db->query(knjl011aQuery::getCalendarno($model->year));
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
            if ($row["NAMESPARE2"] <= $model->year."/03/01" && $model->year."/03/01" <= $row["NAMESPARE3"]) {
                // 卒業元号・卒業年の初期値
                $gengouCd = $row["NAMECD2"];
            }
            $arg["data2"][] = array("eracd" => $row["NAMECD2"], "wname" => $row["NAME1"]);
        }

        //卒業元号
        $query = knjl011aQuery::getNameCd($model->year, "L007");
        $extra = "onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $Row["FS_ERACD"] = ($Row["FS_ERACD"]) ? $Row["FS_ERACD"]: $gengouCd;
        makeCmb($objForm, $arg, $db, $query, $Row["FS_ERACD"], "FS_ERACD", $extra, 1, "");
        //卒業年
        $extra = "STYLE=\"ime-mode: inactive\" onchange=\"change_flg()\" style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["FS_Y"] = knjCreateTextBox($objForm, $Row["FS_Y"], "FS_Y", 2, 2, $extra);
        //卒業月
        $extra = "STYLE=\"ime-mode: inactive\" onchange=\"change_flg()\" style=\"text-align:right\" onblur=\"this.value=toInteger(this.value);\" onkeydown=\"changeEnterToTab(this)\"";
        $Row["FS_M"] = ($Row["FS_M"]) ? $Row["FS_M"]: "03";
        $arg["data"]["FS_M"] = knjCreateTextBox($objForm, $Row["FS_M"], "FS_M", 2, 2, $extra);
        //卒業区分
        $query = knjl011aQuery::getNameCd($model->year, "L016");
        $extra = "onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $Row["FS_GRDDIV"] = ($Row["FS_GRDDIV"]) ? $Row["FS_GRDDIV"]: "2";
        makeCmb($objForm, $arg, $db, $query, $Row["FS_GRDDIV"], "FS_GRDDIV", $extra, 1, "");

        //かな検索ボタン（出身学校）
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=searchMain&fscdname=FINSCHOOLCD_ID&fsname=FINSCHOOLNAME_ID&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["button"]["btn_fin_kana_reference"] = knjCreateBtn($objForm, "btn_fin_kana_reference", "検 索", $extra);

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
        $arg["data"]["PRISCHOOL_NAME1"] = $db->getOne(knjl011aQuery::getPriSchoolName($Row["PRISCHOOLCD1"]));
        //教室名１
        $arg["data"]["PRISCHOOL_CLASS_NAME1"] = $db->getOne(knjl011aQuery::getPriSchoolClassName($Row["PRISCHOOLCD1"], $Row["PRISCHOOL_CLASS_CD1"]));

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
        $arg["data"]["PRISCHOOL_NAME2"] = $db->getOne(knjl011aQuery::getPriSchoolName($Row["PRISCHOOLCD2"]));
        //教室名２
        $arg["data"]["PRISCHOOL_CLASS_NAME2"] = $db->getOne(knjl011aQuery::getPriSchoolClassName($Row["PRISCHOOLCD2"], $Row["PRISCHOOL_CLASS_CD2"]));

        //塾３
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"this.value=toInteger(this.value)\"; id=\"PRISCHOOLCD3_ID\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["PRISCHOOLCD3"] = knjCreateTextBox($objForm, $Row["PRISCHOOLCD3"], "PRISCHOOLCD3", 7, 7, $extra);
        //教室コード３
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"this.value=toInteger(this.value)\"; id=\"PRISCHOOL_CLASS_CD3_ID\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["PRISCHOOL_CLASS_CD3"] = knjCreateTextBox($objForm, $Row["PRISCHOOL_CLASS_CD3"], "PRISCHOOL_CLASS_CD3", 7, 7, $extra);
        //かな検索ボタン（塾）３
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_PRISCHOOL/knjwpri_searchindex.php?cmd=searchMain&pricd=PRISCHOOLCD3_ID&priname=label_priName3&priclasscd=PRISCHOOL_CLASS_CD3_ID&priclassname=label_priClassName3&priaddr=&prischool_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY - 200 + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 280)\"";
        $arg["button"]["btn_pri_kana_reference3"] = knjCreateBtn($objForm, "btn_pri_kana_reference3", "検 索", $extra);
        //塾名３
        $arg["data"]["PRISCHOOL_NAME3"] = $db->getOne(knjl011aQuery::getPriSchoolName($Row["PRISCHOOLCD3"]));
        //教室名３
        $arg["data"]["PRISCHOOL_CLASS_NAME3"] = $db->getOne(knjl011aQuery::getPriSchoolClassName($Row["PRISCHOOLCD3"], $Row["PRISCHOOL_CLASS_CD3"]));

        //備考
        $extra = "STYLE=\"ime-mode: active\" onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["REMARK1"] = knjCreateTextBox($objForm, $Row["REMARK1"], "REMARK1", 140, 240, $extra);

        //合格試験ID
        $arg["data"]["PASS_TESTDIV"] = $Row["PASS_TESTDIV"];
        //合格受験番号
        $arg["data"]["PASS_RECEPTNO"] = $Row["PASS_RECEPTNO"];
        //入学コース
        $arg["data"]["ENT_COURSE"] = $Row["ENT_COURSE"];
        //辞退フラグ
        $arg["data"]["JITAI_FLG"] = $Row["JITAI_FLG"];

        //------------------------------保護者情報-------------------------------------
        //氏名(保護者)
        $extra = "STYLE=\"ime-mode: active\" onChange=\"change_flg()\" id=\"GNAME\" onkeyup=\"keySet('GNAME', 'GKANA', 'H');\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["GNAME"] = knjCreateTextBox($objForm, $Row["GNAME"], "GNAME", 40, 60, $extra);

        //氏名かな(保護者)
        $extra = "STYLE=\"ime-mode: active\" onChange=\"change_flg();\" id=\"GKANA\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["GKANA"] = knjCreateTextBox($objForm, $Row["GKANA"], "GKANA", 40, 120, $extra);

        //続柄コンボ
        $query = knjl011aQuery::getNameCd($model->year, "H201");
        $extra = "onChange=\"change_flg()\" onkeydown=\"changeEnterToTab(this)\"";
        makeCmb($objForm, $arg, $db, $query, $Row["RELATIONSHIP"], "RELATIONSHIP", $extra, 1, "BLANK");

        global $sess;
        //郵便番号入力支援(志願者)
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"isZipcd(this)\" onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["ZIPCD"] = knjCreateTextBox($objForm, $Row["ZIPCD"], "ZIPCD", 10, "", $extra);

        //読込ボタンを作成する
        $extra = "style=\"width:140px\"onclick=\"loadwindow('" .REQUESTROOT ."/common/search_zipcd.php?cmd=search&addrname=ADDRESS1&zipname=ZIPCD&ZIP_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $arg["data"]["ZIPCD"] .= knjCreateBtn($objForm, "btn_zip", "郵便番号入力支援", $extra);

        //確定ボタンを作成する
        $extra = "onclick=\"hiddenWin('" .REQUESTROOT ."/common/search_zipcd.php?cmd=apply&addrname=ADDRESS1&zipname=ZIPCD&zip='+document.forms[0]['ZIPCD'].value+'&frame='+getFrameName(self))\"";
        $arg["data"]["ZIPCD"] .= knjCreateBtn($objForm, "btn_apply", "確定", $extra);

        //住所(志願者)
        $extra = "onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["ADDRESS1"] = knjCreateTextBox($objForm, $Row["ADDRESS1"], "ADDRESS1", 60, 150, $extra);

        //方書(志願者)
        $extra = "onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["ADDRESS2"] = knjCreateTextBox($objForm, $Row["ADDRESS2"], "ADDRESS2", 60, 150, $extra);

        //電話番号(志願者)
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"this.value=toTelNo(this.value)\" onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["TELNO"] = knjCreateTextBox($objForm, $Row["TELNO"], "TELNO", 14, 14, $extra);

        //緊急連絡先(保護者)
        $extra = "STYLE=\"ime-mode: inactive\" onblur=\"this.value=toTelNo(this.value)\" onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        $arg["data"]["EMERGENCYTELNO"] = knjCreateTextBox($objForm, $Row["EMERGENCYTELNO"], "EMERGENCYTELNO", 14, 14, $extra);

        //-------------------------------- ボタン作成 ------------------------------------
        $gzip = $Row["ZIPCD"];
        $gadd = $Row["ADDRESS1"];

        //新規ボタン
        $extra = "onclick=\"return btn_submit('addnew');\"";
        $arg["button"]["btn_addnew"] = knjCreateBtn($objForm, "btn_addnew", "新 規", $extra);

        //検索ボタン
        $extra = "onclick=\"return btn_submit('reference', '".$gzip."', '".$gadd."');\"";
        $arg["button"]["btn_reference"] = knjCreateBtn($objForm, "btn_reference", "検 索", $extra);

        //かな検索ボタン
        $extra = "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL011A/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&applicantdiv='+document.forms[0]['APPLICANTDIV'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
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

        //入学者の一覧画面からコールされた時
        if ($model->getPrgId == "KNJL013A") {
            $disSend = " disabled";
            //戻るボタン
            $extra = "onClick=\"closeFunc();\"";
            $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", $extra);
        } else {
            $disSend = "";
            //終了ボタン
            $extra = "onclick=\"closeWin();\"";
            $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);
        }

        //追加処理中のみ個人情報の追加ボタンは入力可
        //追加処理中は個人情報の更新・削除ボタンは入力不可
        //追加処理中は応募情報の追加～終了ボタンは入力不可
        if ($model->cmd == 'addnew' || $model->cmd == 'mainAdd' && isset($model->warning)) {
            $disAdd1 = "";
            $disBtn2 = " disabled";
        } else {
            $disAdd1 = " disabled";
            $disBtn2 = "";
        }

        //削除ボタンは、Hで始まる志願者SEQのデータのみ可能
        if (strlen($model->examno) && substr($model->examno, 0, 1) == 'H') {
            $disExamnoH = "";
        } else {
            $disExamnoH = " disabled";
        }

        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra.$disSend.$disAdd1);
        //更新ボタン
        $extra = "$disabled onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra.$disBtn2);
        //削除ボタン
        $extra = "$disabled onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra.$disSend.$disExamnoH.$disBtn2);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year", $model->year);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);

        //------------------------------表示処理（下画面）-------------------------------------

        //リスト
        $disAdd2 = $disUpdDel2 = "disabled";
        if ($model->examno) {
            $checkKey = "";
            $query = knjl011aQuery::getReceptList($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $setKey = $row["TESTDIV"]."_".$row["RECEPTNO"];
                if ($setKey == $model->field2["TESTDIV"]."_".$model->field2["RECEPTNO"]) {
                    $checkKey = $setKey;
                }
                $setParam = $row["DESIREDIV"]."_".$row["SHDIV"]."_".$row["SUBCLASS_TYPE"];
                $extra = "onclick=\"return link_select('{$setKey}', '{$setParam}');\"";
                $row["TEST_NAME"] = View::alink("#", $row["TEST_NAME"], $extra);

                $arg["data2List"][] = $row;
                $disUpdDel2 = "";
            }
            $result->free();
            $Row2 =& $model->field2;
            $disAdd2 = "";
            knjCreateHidden($objForm, "CHECK_KEY", $checkKey);//リストから選択したか
        }

        //受験種別
        $query = knjl011aQuery::getTestdivMst($model);
        $extra = "onChange=\"change_flg(); return btn_submit2('changeTest');\"";
        makeCmb($objForm, $arg, $db, $query, $Row2["TESTDIV"], "TESTDIV", $extra, 1, "");

        //受験番号
        $extra = " STYLE=\"ime-mode: inactive\" onChange=\"btn_disabled2();\" ";
        $arg["data"]["RECEPTNO"] = knjCreateTextBox($objForm, $Row2["RECEPTNO"], "RECEPTNO", 7, 7, $extra);

        //志望コース
        $query = knjl011aQuery::getCourseCmb($model);
        $extra = "onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        makeCmb($objForm, $arg, $db, $query, $Row2["DESIREDIV"], "DESIREDIV", $extra, 1, "");

        //専併区分
        $query = knjl011aQuery::getNameCd($model->year, "L006", "");
        $extra = "onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        makeCmb($objForm, $arg, $db, $query, $Row2["SHDIV"], "SHDIV", $extra, 1, "");

        //受験科目
        $query = knjl011aQuery::getExamType($model);
        $extra = "onChange=\"change_flg();\" onkeydown=\"changeEnterToTab(this)\"";
        makeCmb($objForm, $arg, $db, $query, $Row2["SUBCLASS_TYPE"], "SUBCLASS_TYPE", $extra, 1, "");

        //追加ボタン
        $extra = "$disAdd2 onclick=\"return btn_submit2('add2');\"";
        $arg["button"]["btn_add2"] = knjCreateBtn($objForm, "btn_add2", "追 加", $extra.$disSend.$disBtn2);
        //更新ボタン
        $extra = "$disUpdDel2 onclick=\"return btn_submit2('update2');\"";
        $arg["button"]["btn_update2"] = knjCreateBtn($objForm, "btn_update2", "更 新", $extra.$disSend.$disBtn2);
        //削除ボタン
        $extra = "$disUpdDel2 onclick=\"return btn_submit2('delete2');\"";
        $arg["button"]["btn_del2"] = knjCreateBtn($objForm, "btn_del2", "削 除", $extra.$disSend.$disBtn2);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset2"] = knjCreateBtn($objForm, "btn_reset2", "取 消", $extra.$disSend.$disBtn2);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end2"] = knjCreateBtn($objForm, "btn_end2", "終 了", $extra.$disSend.$disBtn2);


        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl011aForm1.html", $arg);
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
        if ($name == 'TESTDIV') {
            if ($value == "" && $row["NAMESPARE2"] == '1') {
                $value = $row["VALUE"];
            }
        }
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
