<?php

require_once('for_php7.php');

class knjl011gForm1
{
    public function main(&$model)
    {
        $objForm      = new form();
        $arg["start"] = $objForm->get_start("main", "POST", "knjl011gindex.php", "", "main");

        //学校名取得
        if ($model->cmd == "getSchoolName") {
            $db = Query::dbCheckOut();
            $query = knjl011gQuery::getFinschoolName($model->field["FS_CD"]);
            $response = $db->getRow($query, DB_FETCHMODE_ASSOC);
            Query::dbCheckIn($db);
            echo json_encode($response);
            die();
        }

        //一覧表示
        if ((!isset($model->warning)) && (!is_array($existdata)) && $model->cmd != 'addnew') {
            //データを取得
            $Row = knjl011gQuery::getEditData($model);
            if ($model->cmd == 'back' || $model->cmd == 'next' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                if (!is_array($Row)) {
                    if ($model->cmd == 'back' || $model->cmd == 'next') {
                        $model->setWarning("MSG303", "更新しましたが、移動先のデータが存在しません。");
                    }
                    $model->cmd = "main";
                    $Row = knjl011gQuery::getEditData($model);
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
            $Row["SHDIV"] = $model->field["SHDIV"];
            $Row["EXAMCOURSE"] = $model->field["EXAMCOURSE"];
            $Row["RECEPTDATE"] = $model->field["RECEPTDATE"];
        }
        if ($model->cmd == 'changeTest') {
            $Row =& $model->field;
        }

        if (isset($Row["EXAMNO"])) {
            $model->checkexam = $Row["EXAMNO"];
        }

        //DB接続
        $db = Query::dbCheckOut();

        //柏原のみ表示する項目
        if ($model->isKasiwara == "1") {
            $arg["isKasiwara"] = 1;
        }
        knjCreateHidden($objForm, "isKasiwara", $model->isKasiwara);
        knjCreateHidden($objForm, "isKeiai", $model->isKeiai);

        //入試制度コンボ
        $query = knjl011gQuery::getNameCd($model->year, "L003", "2");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["APPLICANTDIV"], "APPLICANTDIV", $extra, 1, "");

        //受験番号
        $extra = "onChange=\"btn_disabled();\" onblur=\"this.value=toAlphaNumber(this.value);\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $model->examno, "EXAMNO", 4, 4, $extra);

        //受付日付
        $extra = " onchange=\"change_flg()\"";
        $arg["data"]["RECEPTDATE"] = View::popUpCalendar2($objForm, "RECEPTDATE", str_replace("-", "/", $Row["RECEPTDATE"]), "", "", $extra);

        //入試区分
        $query = knjl011gQuery::getNameCd($model->year, "L004");
        $extra = "onChange=\"change_flg(); btn_submit('changeTest', '', '', '', '');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["TESTDIV"], "TESTDIV", $extra, 1, "BLANK");

        //第１志望コンボ
        $query = knjl011gQuery::getExamcourse($model->year, $Row["APPLICANTDIV"], $Row["TESTDIV"]);
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["EXAMCOURSE"], "EXAMCOURSE", $extra, 1, "BLANK");
        //専併区分コンボ
        $query = knjl011gQuery::getNameCd($model->year, "L006");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SHDIV"], "SHDIV", $extra, 1, "BLANK");

        //第２志望コンボ
        $query = knjl011gQuery::getExamcourse($model->year, $Row["APPLICANTDIV"], $Row["TESTDIV"]);
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["EXAMCOURSE2"], "EXAMCOURSE2", $extra, 1, "BLANK");
        //専併区分コンボ　TODO:柏原のみ
        $query = knjl011gQuery::getNameCd($model->year, "L006");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SHDIV2"], "SHDIV2", $extra, 1, "BLANK");

        //------------------------------志願者情報-------------------------------------
        //特別措置者(インフルエンザ)
        $extra  = "onChange=\"change_flg()\" id=\"SPECIAL_REASON_DIV\" ";
        $extra .= strlen($Row["SPECIAL_REASON_DIV"]) ? "checked" : "";
        $arg["data"]["SPECIAL_REASON_DIV"] = knjCreateCheckBox($objForm, "SPECIAL_REASON_DIV", "1", $extra);

        //氏名(志願者)
        $extra = "onChange=\"change_flg();\" id=\"NAME\" onkeyup=\"keySet('NAME', 'NAME_KANA', 'H');\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["NAME"] = knjCreateTextBox($objForm, $Row["NAME"], "NAME", 40, 60, $extra);

        //氏名かな(志願者)
        $extra = "onChange=\"change_flg();\" id=\"NAME_KANA\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["NAME_KANA"] = knjCreateTextBox($objForm, $Row["NAME_KANA"], "NAME_KANA", 40, 120, $extra);

        //性別コンボ
        //東大阪柏原高校の場合は初期値を男性にする
        if ($model->isKasiwara == "1") {
            $Row["SEX"] = ($Row["SEX"] == "") ? 1 : $Row["SEX"];
        }
        $query = knjl011gQuery::getNameCd($model->year, "Z002");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SEX"], "SEX", $extra, 1, "BLANK");

        //生年月日（西暦）
        $extra = " onchange=\"change_flg()\"";
        $arg["data"]["BIRTHDAY"] = View::popUpCalendar2($objForm, "BIRTHDAY", str_replace("-", "/", $Row["BIRTHDAY"]), "", "", $extra);

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
        $arg["data"]["ADDRESS1"] = knjCreateTextBox($objForm, $Row["ADDRESS1"], "ADDRESS1", 60, 60, $extra);

        //方書(志願者)
        $extra = "onblur=\"toCopytxt(2, this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["ADDRESS2"] = knjCreateTextBox($objForm, $Row["ADDRESS2"], "ADDRESS2", 60, 60, $extra);

        //電話番号(志願者)
        $extra = "onblur=\"this.value=toTelNo(this.value), toCopytxt(3, this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["TELNO"] = knjCreateTextBox($objForm, $Row["TELNO"], "TELNO", 14, 14, $extra);

        //出身学校コード
        $extra = "onblur=\"this.value=toInteger(this.value)\" onChange=\"change_flg();getFinschoolName(this);\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["FINSCHOOLCD"] = knjCreateTextBox($objForm, $Row["FS_CD"], "FINSCHOOLCD", 7, 7, $extra);

        //学校名
        $query = knjl011gQuery::getFinschoolName($Row["FS_CD"]);
        $fsArray = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["FINSCHOOLNAME"] = $fsArray["FINSCHOOL_NAME"];

        //かな検索ボタン（出身学校）
        $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=&fscdname=&fsname=&fsaddr=&school_div=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
        $arg["button"]["btn_fin_kana_reference"] = knjCreateBtn($objForm, "btn_fin_kana_reference", "検 索", $extra);

        //名称マスタより和暦の元号を取得
        $spareYear = 1989;
        $gengouCd = "";
        $gengouName = "";
        $result = $db->query(knjl011gQuery::getCalendarno($model->year));
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
            if ($row["NAMESPARE1"] < $model->year) {
                // 卒業元号の年度を引く
                $spareYear = $row["NAMESPARE1"];
                $gengouCd = $row["NAMECD2"];
                $gengouName = $row["NAME1"];
            }
            $arg["data2"][] = array("eracd" => $row["NAMECD2"], "wname" => $row["NAME1"]);
        }

        //卒業元号
        $extra = "STYLE=\"ime-mode: inactive; text-align: center;\" onblur=\" toDatecheck(0, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."'), setName(this);\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["FS_ERACD"] = knjCreateTextBox($objForm, strlen($Row["FS_ERACD"]) ? $Row["FS_ERACD"] : $gengouCd, "FS_ERACD", 1, 1, $extra);

        //卒業和暦名
        $fs_wname = isset($Row["FS_WNAME"]) ? str_replace("&nbsp;", "", $Row["FS_WNAME"]) : $gengouName;
        knjCreateHidden($objForm, "FS_WNAME", $fs_wname);
        $arg["data"]["FS_WNAME"] = $fs_wname;

        //卒業年
        // $defGrdY = $model->cmd == 'showdivAdd' ? (int)$model->year + 1 - 1989 : "";
        $defGrdY = $model->cmd == 'showdivAdd' ? (int)$model->year + 1 - $spareYear : "";
        $extra = "STYLE=\"ime-mode: inactive; text-align: center;\" onblur=\" toDatecheck(1, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["FS_Y"] = knjCreateTextBox($objForm, strlen($Row["FS_Y"]) ? $Row["FS_Y"] : $defGrdY, "FS_Y", 2, 2, $extra);

        //卒業月
        $defGrdmon = $model->cmd == 'showdivAdd' ? "03" : "";
        $extra = "STYLE=\"ime-mode: inactive; text-align: center;\" onblur=\" toDatecheck(2, this, '".$calno."', '".$spare."', '".$spare2."', '".$spare3."')\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["FS_M"] = knjCreateTextBox($objForm, strlen($Row["FS_M"]) ? $Row["FS_M"] : $defGrdmon, "FS_M", 2, 2, $extra);

        //卒業区分（1:見込み,2:卒業）
        $defGrddiv = $model->cmd == 'showdivAdd' ? "1" : "";
        $Row["FS_GRDDIV"] = strlen($Row["FS_GRDDIV"]) ? $Row["FS_GRDDIV"] : $defGrddiv;
        $query = knjl011gQuery::getNameCd($model->year, "L016");
        $extra = "onChange=\"change_flg(); btn_submit('changeTest', '', '', '', '');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["FS_GRDDIV"], "FS_GRDDIV", $extra, 1, "BLANK");

        //------------------------------保護者情報-------------------------------------
        //氏名(保護者)
        $extra = "onChange=\"change_flg()\" id=\"GNAME\" onkeyup=\"keySet('GNAME', 'GKANA', 'H');\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GNAME"] = knjCreateTextBox($objForm, $Row["GNAME"], "GNAME", 40, 60, $extra);

        //続柄コンボ
        $query = knjl011gQuery::getNameCd($model->year, "H201");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["RELATIONSHIP"], "RELATIONSHIP", $extra, 1, "BLANK");

        //氏名かな(保護者)
        $extra = "onChange=\"change_flg();\" id=\"GKANA\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GKANA"] = knjCreateTextBox($objForm, $Row["GKANA"], "GKANA", 40, 120, $extra);

        //郵便番号入力支援(保護者)
        $arg["data"]["GZIPCD"] = View::popUpZipCode($objForm, "GZIPCD", $Row["GZIPCD"], "GADDRESS1");

        //住所(保護者)
        $extra = "onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GADDRESS1"] = knjCreateTextBox($objForm, $Row["GADDRESS1"], "GADDRESS1", 60, 60, $extra);

        //方書(保護者)
        $extra = "onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GADDRESS2"] = knjCreateTextBox($objForm, $Row["GADDRESS2"], "GADDRESS2", 60, 60, $extra);

        //電話番号(保護者)
        $extra = "onblur=\"this.value=toTelNo(this.value)\" onChange=\"change_flg();\" onkeydown=\"keyChangeEntToTab(this)\"";
        $arg["data"]["GTELNO"] = knjCreateTextBox($objForm, $Row["GTELNO"], "GTELNO", 14, 14, $extra);

        //------------------------------情報-------------------------------------
        //事前相談情報
        //事前相談コンボ
        $query = knjl011gQuery::getNameCd($model->year, "L032");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["JIZEN_SOUDAN_CD"], "JIZEN_SOUDAN_CD", $extra, 1, "BLANK");
        //その他テキスト　全角５文字
        $extra = "onChange=\"change_flg();\"";
        $arg["data"]["JIZEN_SOUDAN_TEXT"] = knjCreateTextBox($objForm, $Row["JIZEN_SOUDAN_TEXT"], "JIZEN_SOUDAN_TEXT", 10, 15, $extra);

        //クラブ推薦情報
        //クラブ名コンボ
        $query = knjl011gQuery::getNameCd($model->year, "L037");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["CLUB_CD"], "CLUB_CD", $extra, 1, "BLANK");
        //クラブランクコンボ
        $query = knjl011gQuery::getNameCd($model->year, "L025");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["CLUB_RANK"], "CLUB_RANK", $extra, 1, "BLANK");
        //入学後の志望クラブ名コンボ　TODO:柏原のみ
        $query = knjl011gQuery::getNameCd($model->year, "L037");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["HOPE_CLUB_CD"], "HOPE_CLUB_CD", $extra, 1, "BLANK");

        //親族情報
        //氏名テキスト
        $extra = "onChange=\"change_flg();\"";
        $arg["data"]["SINZOKU_NAME"] = knjCreateTextBox($objForm, $Row["SINZOKU_NAME"], "SINZOKU_NAME", 20, 40, $extra);
        //旧姓テキスト
        $extra = "onChange=\"change_flg();\"";
        $arg["data"]["SINZOKU_KYUSEI"] = knjCreateTextBox($objForm, $Row["SINZOKU_KYUSEI"], "SINZOKU_KYUSEI", 10, 40, $extra);
        //続柄コンボ
        $query = knjl011gQuery::getNameCd($model->year, "H201");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SINZOKU_ZOKUGARA"], "SINZOKU_ZOKUGARA", $extra, 1, "BLANK");
        //学校名テキスト
        $extra = "onChange=\"change_flg();\"";
        $arg["data"]["SINZOKU_GAKKOUMEI"] = knjCreateTextBox($objForm, $Row["SINZOKU_GAKKOUMEI"], "SINZOKU_GAKKOUMEI", 30, 40, $extra);
        //学科・科テキスト
        $extra = "onChange=\"change_flg();\"";
        $arg["data"]["SINZOKU_GAKKA"] = knjCreateTextBox($objForm, $Row["SINZOKU_GAKKA"], "SINZOKU_GAKKA", 20, 40, $extra);
        //在学・卒業年度テキスト
        $extra = "onChange=\"change_flg();\"";
        $arg["data"]["SINZOKU_ZAI_GRD_YEAR"] = knjCreateTextBox($objForm, $Row["SINZOKU_ZAI_GRD_YEAR"], "SINZOKU_ZAI_GRD_YEAR", 8, 40, $extra);
        //在学・卒業フラグコンボ
        $query = knjl011gQuery::getNameCd($model->year, "L038");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SINZOKU_ZAI_GRD_FLG"], "SINZOKU_ZAI_GRD_FLG", $extra, 1, "BLANK");

        //双生児情報
        //氏名テキスト
        $extra = "onChange=\"change_flg();\"";
        $arg["data"]["SINZOKU_FUTAGO_NAME"] = knjCreateTextBox($objForm, $Row["SINZOKU_FUTAGO_NAME"], "SINZOKU_FUTAGO_NAME", 20, 40, $extra);
        //続柄コンボ
        $query = knjl011gQuery::getNameCd($model->year, "H201");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["SINZOKU_FUTAGO_ZOKUGARA"], "SINZOKU_FUTAGO_ZOKUGARA", $extra, 1, "BLANK");

        //面接情報
        //面接評価コンボ
        $query = knjl011gQuery::getNameCd($model->year, "L027");
        $extra = "onChange=\"change_flg()\"";
        makeCmb($objForm, $arg, $db, $query, $Row["INTERVIEW_VALUE"], "INTERVIEW_VALUE", $extra, 1, "BLANK");
        //面接テキスト　全角３０文字
        $extra = "onChange=\"change_flg();\"";
        $arg["data"]["INTERVIEW_REMARK"] = knjCreateTextBox($objForm, $Row["INTERVIEW_REMARK"], "INTERVIEW_REMARK", 60, 90, $extra);

        //監督者情報
        //監督者テキスト　全角３０文字
        $extra = "onChange=\"change_flg();\"";
        $arg["data"]["REMARK1"] = knjCreateTextBox($objForm, $Row["REMARK1"], "REMARK1", 60, 90, $extra);

        //------------------------------欠席-------------------------------------
        //欠席登録
        $link = REQUESTROOT."/L/KNJL023G/knjl023gindex.php?cmd=&SEND_PRGID=KNJL011G&SEND_AUTH=".$model->auth."&SEND_APPLICANTDIV=".$model->applicantdiv."&SEND_EXAMNO=".$model->examno;
        $extra = "$disabled onClick=\" Page_jumper('{$link}');\"";
        $arg["button"]["btn_kesseki"] = knjCreateBtn($objForm, "btn_kesseki", "欠席登録", $extra);
        $arg["data"]["JUDGEMENT_INFO"] = $Row["JUDGEMENT_INFO"];

        //------------------------------内申-------------------------------------
        //調査書登録
        $link = REQUESTROOT."/L/KNJL021G/knjl021gindex.php?cmd=&SEND_PRGID=KNJL011G&SEND_AUTH=".$model->auth."&SEND_APPLICANTDIV=".$model->applicantdiv."&SEND_EXAMNO=".$model->examno;
        $extra = "$disabled onClick=\" Page_jumper('{$link}');\"";
        $arg["button"]["btn_chousasho"] = knjCreateBtn($objForm, "btn_chousasho", "調査書登録", $extra);

        //-------------------------------- ボタン作成 ------------------------------------
        $zip = $Row["ZIPCD"];
        $gzip = $Row["GZIPCD"];
        $zadd = $Row["ADDRESS1"];
        $gadd = $Row["GADDRESS1"];

        //検索ボタン
        $extra = "onclick=\"return btn_submit('reference', '".$zip."', '".$gzip."', '".$zadd."', '".$gadd."');\"";
        $arg["button"]["btn_reference"] = knjCreateBtn($objForm, "btn_reference", "検 索", $extra);

        //かな検索ボタン
        $extra = "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL011G/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $arg["button"]["btn_kana_reference"] = knjCreateBtn($objForm, "btn_kana_reference", "かな検索", $extra);

        //前の志願者検索ボタン
        $extra = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onClick=\"btn_submit('back1', '".$zip."', '".$gzip."', '".$zadd."', '".$gadd."');\"";
        $arg["button"]["btn_back_next"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);

        //次の志願者検索ボタン
        $extra = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onClick=\"btn_submit('next1', '".$zip."', '".$gzip."', '".$zadd."', '".$gadd."');\"";
        $arg["button"]["btn_back_next"] .= knjCreateBtn($objForm, "btn_next", " >> ", $extra);

        //画面クリアボタン
        $extra = "style=\"width:90px\" onclick=\"return btn_submit('disp_clear');\"";
        $arg["button"]["btn_clear"] = knjCreateBtn($objForm, "btn_clear", "画面クリア", $extra);

        //志願者よりコピーボタン
        $extra = "$disabled style=\"width:135px\" onclick=\"return btn_submit('copy');\"";
        $arg["button"]["btn_copy"] = knjCreateBtn($objForm, "btn_copy", "志願者よりコピー", $extra);

        //名字をコピーボタン
        $extra = "$disabled style=\"width:100px\" onclick=\"return btn_submit('copysei');\"";
        $arg["button"]["btn_copysei"] = knjCreateBtn($objForm, "btn_copysei", "名字をコピー", $extra);

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

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML5($model, "knjl011gForm1.html", $arg);
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
        if ($name == 'APPLICANTDIV' || $name == 'TESTDIV') {
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
