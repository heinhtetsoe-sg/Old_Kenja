<?php

require_once 'for_php7.php';

// kanji=漢字

class knjg010Form1
{
    public function main(&$model)
    {

        //権限チェック
        if (common::SecurityCheck(STAFFCD, PROGRAMID) != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form();
        //フォーム作成
        $arg["start"]   = $objForm->get_start("knjg010Form1", "POST", "knjg010index.php", "", "knjg010Form1");

        //警告メッセージを表示しない場合

        //
        if (isset($model->field["SCHREGNO"])) {
            $Row1 = knjg010Query::getSchregBaseMstData($model);
            $Row2 = knjg010Query::getSchregregdData($model);
            $Row3 = knjg010Query::getSchregTransferData($model);
        }

        $db = Query::dbCheckOut();

        //学籍番号///////////////////////////////////////////////////////////////////////////////////////////////////////
        $arg["data"]["SCHREGNO"] = knjCreateTextBox($objForm, $model->field["SCHREGNO"], "SCHREGNO", 9, 8, "onChange=\"gakusekichg()\"");

        //氏名///////////////////////////////////////////////////////////////////////////////////////////////////////////
        $arg["data"]["NAME"] = knjCreateTextBox($objForm, $Row1["NAME_SHOW"], "NAME", 42, 42, "");

        //発行番号
        $extra  = $model->Properties["certifNoSyudou"] == "1" ? "" : "disabled ";
        $extra .= "onblur=\"this.value=toInteger(this.value)\";";
        $arg["data"]["REMARK1"] = knjCreateTextBox($objForm, $model->field["REMARK1"], "REMARK1", 8, 8, $extra);

        //自動付番発行番号表示のみ
        $arg["data"]["DISP_CERTIF_NO"] = $model->field["CERTIF_NO"];

        //生年月日///////////////////////////////////////////////////////////////////////////////////////////////////////
        $arg["data"]["BN_DATE"] = knjCreateTextBox($objForm, str_replace("-", "/", $Row1["BIRTHDAY"]), "BN_DATE", 12, 10, "");

        //卒業年月日///////////////////////////////////////////////////////////////////////////////////////////////////////
        $arg["data"]["SOTUGYOU"] = knjCreateTextBox($objForm, str_replace("-", "/", $Row3["TRANSFER_SDATE"]), "SOTUGYOU", 12, 10, "");

        //年組///////////////////////////////////////////////////////////////////////////////////////////////////////////
        $arg["data"]["HR_CLASS"] = knjCreateTextBox($objForm, (isset($model->field["SCHREGNO"]) ? ($Row2["HR_NAME"]." ") : ""), "HR_CLASS", 12, 10, "");

        //学科///////////////////////////////////////////////////////////////////////////////////////////////////////////
        $arg["data"]["GAKKA"] = knjCreateTextBox($objForm, $Row2["MAJORNAME"], "GAKKA", 12, 10, "");

        //課程///////////////////////////////////////////////////////////////////////////////////////////////////////////
        $arg["data"]["KATEI"] = knjCreateTextBox($objForm, $Row2["COURSENAME"], "KATEI", 12, 10, "");

        //学級担任///////////////////////////////////////////////////////////////////////////////////////////////////////
        $arg["data"]["HR_TEARCH"] = knjCreateTextBox($objForm, $Row2["HR_TEARCH"], "HR_TEARCH", 20, 20, "");

        //申請年月日
        if (!$model->field["TK_DATE"]) {
            $model->field["TK_DATE"] = CTRL_DATE;
        }
        $arg["data"]["TK_DATE"]=View::popUpCalendar($objForm, "TK_DATE", str_replace("-", "/", $model->field["TK_DATE"]), "");

        //処理年月日
        if (!$model->field["PR_DATE"]) {
            $model->field["PR_DATE"] = CTRL_DATE;
        }
        $arg["data"]["PR_DATE"]=View::popUpCalendar($objForm, "PR_DATE", str_replace("-", "/", $model->field["PR_DATE"]), "");

        //証明書種類
        $query  = knjg010Query::getCertifKind($model->year);
        $result = $db->query($query);
        $opt_e = array();
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $opt_e[] = array("label" => $row["CERTIF_KINDCD"]."：".htmlspecialchars($row["KINDNAME"])
                            ,"value" => $row["CERTIF_KINDCD"] ."," .$row["ISSUECD"] ."," .$row["STUDENTCD"] ."," .$row["GRADUATECD"] ."," .$row["DROPOUTCD"]);
        }

        // 記載責任者を使用する証明書種別
        $kisaiSekiEnabledCkArray = array();
        $kisaiSekiEnabledCkArray[] = "006"; // 成績証明書
        $kisaiSekiEnabledCkArray[] = "007"; // 成績証明書英文
        if ($model->Properties["tyousasyoPrintHomeRoomStaff"] != '1') {
            $kisaiSekiEnabledCkArray[] = "008"; // 調査書進学用
            $kisaiSekiEnabledCkArray[] = "009"; // 調査書就職用
            $kisaiSekiEnabledCkArray[] = "025"; // 調査書進学用 卒業生用
            $kisaiSekiEnabledCkArray[] = "026"; // 調査書就職用 卒業生用
            $kisaiSekiEnabledCkArray[] = "058"; // 調査書進学用 諸事項6分割
            $kisaiSekiEnabledCkArray[] = "059"; // 調査書進学用 諸事項6分割 卒業生用
        }
        if ($model->Properties["tannishutokushoumeishoKisaisekininsha"] == '1') {
            $kisaiSekiEnabledCkArray[] = "011"; // 単位修得証明書
            $kisaiSekiEnabledCkArray[] = "029"; // 単位修得証明書 卒業生用
        }
        $kisaiSekiEnabledCks .= '["'.implode('","', $kisaiSekiEnabledCkArray).'"]';

        // 証明書発行手数料
        $pricesJs = "{";
        $query = knjg010Query::getFieldCheck("CERTIF_KIND_MST", "CURRENT_PRICE");
        $hasPriceField = $db->getOne($query) > 0;
        if ($hasPriceField) {
            $arg["showPrice"] = "1";
            $query = knjg010Query::getCertifKindMstPrice($db);
            $result = $db->query($query);
            $comma = "";
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $pricesJs .= $comma." '".$row["CERTIF_KINDCD"] ."-0': '".$row["CURRENT_PRICE"]."'";
                $comma = ",";
                $pricesJs .= $comma." '".$row["CERTIF_KINDCD"] ."-1': '".$row["GRADUATED_PRICE"]."'";
            }
            $result->free();
        }
        $pricesJs.= "}";
        $arg["check2"] = "\nfunction getPrice() {";
        $arg["check2"] .= "\n var pricejs = {$pricesJs};";
        $arg["check2"] .= "\n return pricejs;";
        $arg["check2"] .= "\n}";
        $arg["check2"] .= "\nfunction getKisaiSekiEnabledCertifKindCds() {";
        $arg["check2"] .= "\n var cks = {$kisaiSekiEnabledCks};";
        $arg["check2"] .= "\n return cks;";
        $arg["check2"] .= "\n}";

        $objForm->ae(
            array("type"        => "select",
                            "name"        => "CERTIF_KD",
                            "size"        => 1,
                            "maxlength"   => 10,
                            "extrahtml"   => "onChange=\"checkCertifKind();\"",
                            "value"       => $model->field["CERTIF_KD"],
                            "options"     => $opt_e
            )
        );

        $arg["data"]["CERTIF_KD"] = $objForm->ge("CERTIF_KD");

        //調査書漢字出力ラジオボタンを作成する////////////////////////////////////////////////////////////////////////////////////
        $opt = array(1, 2);
        if ($model->field["KJ_OUT"] == '') {
            $model->field["KJ_OUT"] = "1";
        }
        $extra  = array("id=\"KJ_OUT1\"", "id=\"KJ_OUT2\"");
        $radioArray = knjCreateRadio($objForm, "KJ_OUT", $model->field["KJ_OUT"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //近大チェック
        $schoolname = strtoupper($model->school_name);
        if (!$schoolname || !($schoolname == "KINDAI" || $schoolname == "KINJUNIOR")) {
            $arg["notKindai"] = '1';
        } else {
            $arg["Kindai"] = '1';
        }

        //未履修科目出力ラジオボタンを作成する
        $opt = array(1, 2);
        if ($model->field["MIRISYU"] == '') {
            $model->field["MIRISYU"] = "2";
        }
        $extra  = array("id=\"MIRISYU1\" onclick=\"checkRisyu();\"", "id=\"MIRISYU2\" onclick=\"checkRisyu();\"");
        $radioArray = knjCreateRadio($objForm, "MIRISYU", $model->field["MIRISYU"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //履修のみ科目出力ラジオボタンを作成する
        $opt = array(1, 2);
        if ($model->field["RISYU"] == '') {
            $model->field["RISYU"] = "1";
        }
        $extra  = array("id=\"RISYU1\" onclick=\"checkRisyu();\"", "id=\"RISYU2\" onclick=\"checkRisyu();\"");
        $radioArray = knjCreateRadio($objForm, "RISYU", $model->field["RISYU"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //評定読替を表示するかどうかのフラグ
        if (in_array($model->Properties["hyoteiYomikae"], array('1', '1_off'))) {
            $arg["showHyoteiYomikae"] = '1';
            $arg["colspan"] = '2';

            //評定の処理設定チェックボックス
            if ($model->Properties["hyoteiYomikaeRadio"] == "1") {
                $arg["data"]["HYOUTEI_TITLE"] = "評定読替<BR>(調査書のみ)<BR>評定表示切替<BR>(成績証明書・<BR>単位修得証明書のみ)";
                if ($model->cmd == "edit" && $model->field["HYOUTEI"] == '') {
                    $model->field["HYOUTEI"] = "notPrint1";
                }
                $opt = array(1, 2, 3);
                $extra  = array("id=\"HYOUTEI1\"", "id=\"HYOUTEI2\"", "id=\"HYOUTEI3\"");
                $labels = array(
                    "HYOUTEI1" => "評定読替・切替しない",
                    "HYOUTEI2" => "評定１を２に読替",
                    "HYOUTEI3" => "評定１を非表示");
                $replace_vals = array(
                    "HYOUTEI1" => "0",
                    "HYOUTEI2" => "1",
                    "HYOUTEI3" => "notPrint1");
                $radioArray = knjCreateRadio($objForm, "HYOUTEI", $model->field["HYOUTEI"], $extra, $opt, get_count($opt));
                $hyoutei = "";
                foreach ($radioArray as $key => $val) {
                    if ($hyoutei) {
                        $hyoutei .= "<BR>";
                    }
                    if (preg_match("/ checked/", $val)) {
                        $val = preg_replace("/ checked/", "", $val);
                    }
                    $checked = $replace_vals[$key] == $model->field["HYOUTEI"] ? " checked " : "";
                    $val = preg_replace("/'\d+'/", "'".$replace_vals[$key]."'".$checked, $val); // radioの値を1,2,3から変更する
                    $hyoutei .= $val."<LABEL for='".$key."'>".$labels[$key]."</LABEL>";
                }
                $arg["data"]["HYOUTEI"] = $hyoutei;
                // 注意:KNJE070、KNJI060では値は"on"。KNJG010は"1"(KNJG010.classがパラメータ"on"で渡す)。
            } else {
                $arg["data"]["HYOUTEI_TITLE"] = "評定読替<BR>(調査書のみ)";
                $extra = " id='HYOUTEI'";
                if ($model->field["HYOUTEI"] == "1" || ($model->cmd == "edit" && $model->Properties["hyoteiYomikae"] == '1')) {
                    $extra .= " checked='checked' ";
                }
                $hyoutei = knjCreateCheckBox($objForm, "HYOUTEI", "1", $extra); // 注意:KNJE070、KNJI060では値は"on"。KNJG010は"1"(KNJG010.classがパラメータ"on"で渡す)。
                $hyoutei .= '<LABEL for="HYOUTEI">評定１を２に読替</LABEL></td>';
                $arg["data"]["HYOUTEI"] = $hyoutei;
            }
        } else {
            $arg["colspan"] = '4';
        }

        //hidden
        knjCreateHidden($objForm, "hyoteiYomikae", $model->Properties["hyoteiYomikae"]);

        //その他住所を優先するチェックボックス
        $query = knjg010Query::getSchoolDiv();
        $schooldiv = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($model->field["SONOTAJUUSYO"] == "1" || $model->cmd == "edit" && $schooldiv["IS_TUSIN"] == '1') {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $extra .= " id=\"SONOTAJUUSYO\"";
        $arg["data"]["SONOTAJUUSYO"] = knjCreateCheckBox($objForm, "SONOTAJUUSYO", "1", $extra);

        //出欠の前籍校を含まないチェックボックス
        if ($model->field["tyousasyoNotPrintAnotherAttendrec"] == "1" ||  $model->cmd == "edit" && "1" == $model->Properties["tyousasyoSetNotPrintAnotherAttendrec"]) {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $extra .= " id=\"tyousasyoNotPrintAnotherAttendrec\"";
        $arg["data"]["tyousasyoNotPrintAnotherAttendrec"] = knjCreateCheckBox($objForm, "tyousasyoNotPrintAnotherAttendrec", "1", $extra);

        //入学・卒業日付は年月で表示する
        if ($model->field["ENT_GRD_DATE_FORMAT"]) {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $extra .= " id=\"ENT_GRD_DATE_FORMAT\"";
        $arg["data"]["ENT_GRD_DATE_FORMAT"] = knjCreateCheckBox($objForm, "ENT_GRD_DATE_FORMAT", "1", $extra);

        //記載責任者コンボボックスを作成
        $query = knjg010Query::getStaffcd($model);
        $result = $db->query($query);
        $row2[0] = array();
        list($simo, $fuseji) = explode(" | ", $model->Properties["showMaskStaffCd"]);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $ume = "" ;
            for ($umecnt = 1; $umecnt <= strlen($row["VALUE"]) - (int)$simo; $umecnt++) {
                $ume .= $fuseji;
            }
            if ($fuseji) {
                $SET_VALUE = $ume.substr($row["VALUE"], (strlen($row["VALUE"]) - (int)$simo), (int)$simo);
            } else {
                $SET_VALUE = $row["VALUE"];
            }
            $row["LABEL"] = str_replace($row["VALUE"], $SET_VALUE, $row["LABEL"]);
            $row2[]= array('label' => $row["LABEL"],
                            'value' => $row["VALUE"]);
        }
        $result->free();

        if ($model->field["KISAI_SEKI"]) {
            $model->staffcd = $model->field["KISAI_SEKI"];
        }
        $objForm->ae(
            array("type"       => "select",
                            "name"       => "KISAI_SEKI",
                            "size"       => "1",
                            "value"      => $model->staffcd,
            "options"    => $row2)
        );

        $arg["data"]["KISAI_SEKI"] = $objForm->ge("KISAI_SEKI");

        //概評の処理設定チェックボックスを作成する
        if ($model->cmd == 'edit') {
            if ($model->Properties["tyousasyoNotCheckGaihyoComment"] == '1') {
                $model->field["GAIHYOU"] = "";
            } else {
                $model->field["GAIHYOU"] = "1";
            }
        }
        if ($model->field["GAIHYOU"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $extra .= " id='GAIHYOU' ";
        $arg["data"]["GAIHYOU"] = knjCreateCheckBox($objForm, "GAIHYOU", "1", $extra);

        if ($model->Properties["useShuryoShoumeisho"] == '1') {
            $arg["sotsugyo"] = "修了";
        } else {
            $arg["sotsugyo"] = "卒業";
        }

        $query = knjg010Query::getSchoolDiv();
        $schooldiv = $db->getRow($query, DB_FETCHMODE_ASSOC);

        $opts = array();

        //評定平均算出
        if ("1" == $model->Properties["tyousasho2020GvalCalcCheck"]) {
            //評定平均算出
            if ($model->field["GVAL_CALC_CHECK"] == "") {
                if ($schooldiv["GVAL_CALC"] == "1") { // GVAL_CALCは0:平均値 1:単位による重み付け
                    $model->field["GVAL_CALC_CHECK"] = "2";
                } else {
                    $model->field["GVAL_CALC_CHECK"] = "1";
                }
            }
            $text = "評定平均算出<BR>(調査書のみ)";
            $text = '<font color="#336666" size="2">'.$text.'</font>';
            $opts[] = createRadioOpt($objForm, $model, "GVAL_CALC_CHECK", $text, array(1 => "単純平均", 2 => "加重平均"), "onclick=\"return gvalCalcChecked();\"");
        }

        if ("1" == $model->Properties["tyousasho2020PrintAvgRank"]) {
            //評定平均順位を表示
            $opt = array(1, 2); // 1:する 2:しない
            if ($model->field["PRINT_AVG_RANK"] == "") {
                $model->field["PRINT_AVG_RANK"] = "2";
            }
            $text = "評定平均席次出力<BR>(調査書のみ)";
            $text = '<font color="#336666" size="2">'.$text.'</font>';
            $opts[] = createRadioOpt($objForm, $model, "PRINT_AVG_RANK", $text, array(1 => "する", 2 => "しない"));
        }

        // 印影選択チェックボックス
        if ($model->Properties["knjg010usePrintStamp"] == '1') {
            $ckcds = array();
            if ($model->school_name == "aoyama") {
                $ckcds = array(
                    "001", "002", "022", "023", "050", "051", // 卒業証明書
                    "003", // 卒業見込証明書
                    "013", "041", // 在籍証明書
                    "004", "012", "024", "040" // 在学証明書
                );
            } elseif ($model->school_name == "matsudo") {
                $ckcds = array(
                    "008", "009", "025", "026", "058", "059", // 調査書
                    "006", "007", "033", "034", // 成績証明書
                    "001", "002", "022", "023", "050", "051", // 卒業証明書
                    "003", // 卒業見込証明書
                    "004", "005", "012", "024", "040", // 在学証明書
                    "011", "027" // 単位修得証明書
                );
            } elseif ($model->school_name == "ryukei") {
                $ckcds = array(
                    "006", "007", // 成績証明書
                    "001", "002", // 卒業証明書
                    "003", "019", // 卒業見込証明書
                    "004", "005", // 在学証明書
                    "011", "027", // 単位修得証明書
                    "018"   // 単位修得見込証明書
                );
            } else {
                // 賢者
                $ckcds = array(
                    "008", "009", "025", "026", "058", "059", // 調査書
                    "006", // 成績証明書
                    "001", "022", "023", "050", "051", // 卒業証明書
                    "004", "012", "024", "040"  // 在学証明書
                );
            }

            $checks = array(array("TITLE" => "調査書",                      "CKS" => array("008", "009", "025", "026", "058", "059")),
                            array("TITLE" => "成績証明書",                  "CKS" => array("006", "007", "033", "034")),
                            array("TITLE" => $arg["sotsugyo"]."証明書",     "CKS" => array("001", "002", "022", "023", "050", "051")),
                            array("TITLE" => $arg["sotsugyo"]."見込証明書", "CKS" => array("003", "019")),
                            array("TITLE" => "在学証明書",                  "CKS" => array("004", "005", "012", "024", "040")),
                            array("TITLE" => "単位修得証明書",              "CKS" => array("011", "027")),
                            array("TITLE" => "在籍証明書",                  "CKS" => array("013", "041")),
                            array("TITLE" => "単位修得見込証明書",          "CKS" => array("018"))
                            );
            $cksnames = array();
            foreach ($checks as $i => $arr) {
                $target = false;
                foreach ($arr["CKS"] as $checkcd) {
                    if (in_array($checkcd, $ckcds)) {
                        $target = true;
                        break;
                    }
                }
                if ($target) {
                    $cksnames[] = $arr["TITLE"];
                }
            }
            $max = get_count($cksnames);
            $cksname = "";
            for ($c = 0; $c < $max; $c++) {
                $cksname .= $cksnames[$c];
                if ($c != $max - 1) {
                    $cksname .= "、";
                }
                if ($c % 2 == 1 && $c != $max - 1) {
                    $cksname .= "<BR>";
                }
            }
            knjCreateHidden($objForm, "printStampCertifKindcds", "[\"".implode("\",\"", $ckcds)."\"]");
            $opt = array();
            $text = '<font color="#336666" size="2">印影出力</font><BR><font color="#336666" size="1.8">('.$cksname.'のみ)</font>';
            $opts[] = createCheckOpt($objForm, $model, "PRINT_STAMP", $text, "印影出力する");
        }

        // 半期認定フォーム
        if ($model->Properties["tyousasyoHankiNintei"] == '1') {
            if ($model->cmd == 'edit') {
                if ($model->Properties["tyousasyoHankiNinteiDefault"] == '1') {
                    $model->field["HANKI_NINTEI_FORM"] = "1";
                }
            }
            $text = '半期認定<BR>(調査書のみ)';
            $text = '<font color="#336666" size="2">'.$text.'</font>';
            $opts[] = createCheckOpt($objForm, $model, "HANKI_NINTEI_FORM", $text, "出力する");
        }

        // 留学の単位数を0表示
        if ($model->field["RYUGAKU_CREDIT"] == '') {
            $model->field["RYUGAKU_CREDIT"] = "2";
        }
        if ("kumamoto" == $model->school_name) {
            $text = '留学の単位数を0表示<BR>(成績証明書のみ)';
        } else {
            $text = '留学の単位数を0表示<BR>(調査書、成績証明書のみ)';
        }
        $text = '<font color="#336666" size="2">'.$text.'</font>';
        $opts[] = createRadioOpt($objForm, $model, "RYUGAKU_CREDIT", $text, array(1 => "する", 2 => "しない"));

        // 総合的な学習の時間の単位数を0表示
        if ($model->field["SOGAKU_CREDIT"] == '') {
            $model->field["SOGAKU_CREDIT"] = "2";
        }
        if ("kumamoto" == $model->school_name) {
            $text = '総合的な学習の時間の単位数を0表示<BR>(成績証明書のみ)';
        } else {
            $text = '総合的な学習の時間の単位数を0表示<BR>(調査書、成績証明書のみ)';
        }
        $text = '<font color="#336666" size="2">'.$text.'</font>';
        $opts[] = createRadioOpt($objForm, $model, "SOGAKU_CREDIT", $text, array(1 => "する", 2 => "しない"));

        $arr = preg_split("/\s*,\s*/", $model->Properties["tyousasho2020PrintHeaderName"]);
        if (in_array("check", $arr)) {
            //ラジオボタンを作成
            $opt = array(1, 2);
            if (!$model->field["KNJE070D_PRINTHEADERNAME"]) {
                if (in_array("on", $arr)) {
                    $model->field["KNJE070D_PRINTHEADERNAME"] = "1";
                } else {
                    $model->field["KNJE070D_PRINTHEADERNAME"] = "2";
                }
            }
            $text = "偶数頁の氏名出力<BR>(調査書のみ)";
            $text = '<font color="#336666" size="2">'.$text.'</font>';
            $opts[] = createRadioOpt($objForm, $model, "KNJE070D_PRINTHEADERNAME", $text, array(1 => "する", 2 => "しない"));
        }

        //校長印
        $arr = preg_split("/\s*,\s*/", $model->Properties["tyousashoPrintStampSelect"]);
        $tyousashoPrintStampSelect = "";
        if (in_array("1", $arr)) {
            $tyousashoPrintStampSelect = "1";
        } elseif (in_array("2", $arr)) {
            $tyousashoPrintStampSelect = "2";
        }

        if (in_array($tyousashoPrintStampSelect, array("1", "2"))) {
            //校長印出力ラジオボタン 1:する 2:しない
            if (!$model->field["KNJE070_CHECK_PRINT_STAMP_PRINCIPAL"]) {
                if ($model->cmd == "edit" && in_array("on", $arr)) {
                    $model->field["KNJE070_CHECK_PRINT_STAMP_PRINCIPAL"] = "1";
                } else {
                    $model->field["KNJE070_CHECK_PRINT_STAMP_PRINCIPAL"] = "2";
                }
            }
            $text = '校長印出力<BR>(調査書のみ)';
            $text = '<font color="#336666" size="2">'.$text.'</font>';
            $opts[] = createRadioOpt($objForm, $model, "KNJE070_CHECK_PRINT_STAMP_PRINCIPAL", $text, array(1 => "する", 2 => "しない"));
        }
        //記載者印
        if (in_array($tyousashoPrintStampSelect, array("1"))) {
            if (!$model->field["KNJE070_CHECK_PRINT_STAMP_HR_STAFF"]) {
                if ($model->cmd == "edit" && in_array("on", $arr)) {
                    $model->field["KNJE070_CHECK_PRINT_STAMP_HR_STAFF"] = "1";
                } else {
                    $model->field["KNJE070_CHECK_PRINT_STAMP_HR_STAFF"] = "2";
                }
            }
            if ($model->Properties["tyousasyoPrintHomeRoomStaff"] == "1") {
                $text = '担任印出力<BR>(調査書のみ)';
            } else {
                $text = '記載責任者印出力<BR>(調査書のみ)';
            }
            $text = '<font color="#336666" size="2">'.$text.'</font>';
            $opts[] = createRadioOpt($objForm, $model, "KNJE070_CHECK_PRINT_STAMP_HR_STAFF", $text, array(1 => "する", 2 => "しない"));
        }

        //調査書進学用6分割 出力ページ選択ラジオボタン
        $shojikouExtendsOthers = array();
        foreach (array("tyousasyo2020EshojikouExtends", "tyousasyo2020GshojikouExtends") as $key => $propName) {
            $val = $model->Properties[$propName];
            if (in_array($val, array("2", "2_2"))) {
                $shojikouExtendsOthers[$propName] = $val;
            } elseif (in_array($val, array("2page!", "2page"))) {
                $shojikouExtendsOthers[$propName] = $val;
            }
        }
        if (count($shojikouExtendsOthers)) { // KNJE070E、KNJE070G用プロパティーが設定済みの場合のみ表示する
            $arg["show_pageselect"] = "1";
            $shojikouExtendsArray = array();
            $ext = $model->Properties["tyousasyo2020shojikouExtends"];
            if (in_array($ext, array("2", "2_2"))) {  // 3ページ枠拡張、3ページ備考欄出力
                $shojikouExtendsArray["tyousasyo2020shojikouExtends"] = $ext;
            } elseif (in_array($ext, array("2page!", "2page"))) { // 2ページ、2ページ（枠あふれ分は備考欄出力）
                $shojikouExtendsArray["tyousasyo2020shojikouExtends"] = $ext;
            } else {
                $shojikouExtendsArray["tyousasyo2020shojikouExtends"] = "def";
            }
            $shojikouExtendsArray = array_merge($shojikouExtendsArray, $shojikouExtendsOthers);
            $opt_page = array();
            $extra = array();
            $replace_vals = array();
            $i = 1;
            foreach ($shojikouExtendsArray as $name => $val) {
                $opt_page[] = $i;
                $extra[] = "id=\"tyousasyo2020shojikouExtendsSelect{$i}\"";
                $replace_vals["tyousasyo2020shojikouExtendsSelect{$i}"] = $val;
                $i++;
            }

            if ($model->field["tyousasyo2020shojikouExtendsSelect"] == "") {
                $model->field["tyousasyo2020shojikouExtendsSelect"] = "def";
            }
            $radioArray = knjCreateRadio($objForm, "tyousasyo2020shojikouExtendsSelect", $model->field["tyousasyo2020shojikouExtendsSelect"], $extra, $opt_page, get_count($opt_page));
            $pageSelect = array();
            foreach ($radioArray as $key => $val) {
                if (preg_match("/ checked/", $val)) {
                    $val = preg_replace("/ checked/", "", $val);
                }

                $checked = $replace_vals[$key] == $model->field["tyousasyo2020shojikouExtendsSelect"] ? " checked " : "";
                $val = preg_replace("/'\d+'/", "'".$replace_vals[$key]."'".$checked, $val); // radioの値を1,2,3から変更する
                $label = "";
                if (in_array($replace_vals[$key], array("def"))) {
                    $label = "4ページ";
                } elseif (in_array($replace_vals[$key], array("2", "2_2"))) {
                    $label = "3ページ";
                } elseif (in_array($replace_vals[$key], array("2page!", "2page"))) {
                    $label = "2ページ";
                }
                $pageSelect[] = $val."<LABEL for='".$key."'>".$label."</LABEL>";
            }
            $text = '出力ページ選択<BR>(調査書進学用6分割のみ)';
            $text = '<font color="#336666" size="2">'.$text.'</font>';
            $opts[] = array("TEXT" => $text, "VAL" => implode("&nbsp;", $pageSelect));
        }

        //
        $arg["data"]["OPTS"] = array();
        for ($l = 0; $l < get_count($opts); $l+=3) {
            $row = array();
            $row["TEXT1"] = $opts[$l]["TEXT"];
            $row["VAL1"] = $opts[$l]["VAL"];
            if ($l + 1 < get_count($opts)) {
                $row["TEXT2"] = $opts[$l + 1]["TEXT"];
                $row["VAL2"] = $opts[$l + 1]["VAL"];
                if ($l + 2 < get_count($opts)) {
                    $row["TEXT3"] = $opts[$l + 2]["TEXT"];
                    $row["VAL3"] = $opts[$l + 2]["VAL"];
                } else {
                    $row["rest1Col"] = 1 * 2;
                }
            } else {
                $row["rest1Col"] = 2 * 2;
            }
            $arg["data"]["OPTS"][] = $row;
        }

        // ６年用フォーム選択チェックボックス
        if ($model->field["FORM6"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $extra .= " id='FORM6'";
        $arg["data"]["FORM6"] = knjCreateCheckBox($objForm, "FORM6", "1", $extra);

        $arg["data"]["FORM6_KIND"] = "";
        if ($model->Properties["tyousasyoNoSelectNenYoForm"] != '1') {
            $arg["data"]["FORM6_KIND"] .= "調査書";
        }
        if ($model->control["学校区分"] == "1") {
            if ($arg["data"]["FORM6_KIND"] != '') {
                $arg["data"]["FORM6_KIND"] .= "・";
            }
            $arg["data"]["FORM6_KIND"] .= "成績証明書";
        }
        if ($arg["data"]["FORM6_KIND"]) {
            $arg["form_select"] = "1";
            // ○年用フォーム
            $arg["data"]["FORM6NAME"] = "{$model->Properties["nenYoForm"]}年用フォーム";
        } else {
            $arg["no_form_select"] = "1";
        }

        //何年用のフォームを使うのかの初期値を判断する
        $shokiti = "";
        if ($schooldiv["SCHOOLDIV"] == '0') {
            if ($schooldiv["NEN"] == '0') {
                $shokiti = "3";
            } else {
                $shokiti = $schooldiv["NEN"];
            }
        } else {
            if ($schooldiv["NEN"] == '0') {
                $shokiti = "4";
            } else {
                $shokiti = $schooldiv["NEN"];
            }
        }
        knjCreateHidden($objForm, "NENYOFORM_SYOKITI", $shokiti);
        knjCreateHidden($objForm, "NENYOFORM_CHECK", $model->Properties["nenYoForm"]);

        //*******************************************************************************************************************

        //入力番号検索ボタンを作成する
        $extra = "onclick=\"btn_submit('search');\"";
        $arg["button"]["btn_input"] = knjCreateBtn($objForm, "btn_input", "番号確定", $extra);

        //在学生検索ボタンを作成する
        $extra = "onclick=\"wopen('../../X/KNJXSEARCH2/index.php?PATH=/G/KNJG010/knjg010index.php&cmd=&target=KNJG010','search',0,0,700,500);\"";
        $arg["button"]["btn_zaigaku"] = knjCreateBtn($objForm, "btn_zaigaku", "在校生検索", $extra);

        //卒業生検索ボタンを作成する
        $extra = "onclick=\"wopen('../../X/KNJXSEARCH7/index.php?PATH=/G/KNJG010/knjg010index.php&cmd=&target=KNJG010','search',0,0,700,500);\"";
        $arg["button"]["btn_sotugyou"] = knjCreateBtn($objForm, "btn_sotugyou", "在籍生以外", $extra);

        //キャンセルボタンを作成する
        $extra = " onclick=\"return btn_submit('cancel');\" ";
        $arg["button"]["btn_cancel"] = knjCreateBtn($objForm, "btn_cancel", "キャンセル", $extra);

        //追加ボタンを作成する
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追　加", $extra);

        //更新ボタンを作成する
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更　新", $extra);

        //クリアボタンを作成する
        $extra = "onclick=\"return ShowConfirm()\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタンを作成する
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        knjCreateHidden($objForm, "cmd");
        if ($Row3["TRANSFERCD"] == '1' || $Row3["TRANSFERCD"] == '2') {
            $trans = 1;
        } else {
            $trans = 0;
        }
        knjCreateHidden($objForm, "GRADUATE_FLG", $trans);
        knjCreateHidden($objForm, "TRANSFERCD", isset($Row3["TRANSFERCD"]) ? $Row3["TRANSFERCD"] : 0);
        knjCreateHidden($objForm, "UPDATED", '0');
        knjCreateHidden($objForm, "DISP2", isset($model->disp2) ? $model->disp2 : $model->disp);
        knjCreateHidden($objForm, "useSyojikou3", $model->Properties["useSyojikou3"]);
        knjCreateHidden($objForm, "Knje080UseAForm", $model->Properties["Knje080UseAForm"]);

        //発行番号手動
        knjCreateHidden($objForm, "certifNoSyudou", $model->Properties["certifNoSyudou"]);

        //調査書出欠備考文字数
        knjCreateHidden($objForm, "tyousasyoAttendrecRemarkFieldSize", $model->Properties["tyousasyoAttendrecRemarkFieldSize"]);

        //「３年用フォーム」と「６年用フォーム」
        knjCreateHidden($objForm, "3_or_6_nenYoForm", $model->Properties["3_or_6_nenYoForm"]);

        knjCreateHidden($objForm, "train_ref_1_2_3_field_size", $model->Properties["train_ref_1_2_3_field_size"]);

        knjCreateHidden($objForm, "tyousasyoSougouHyoukaNentani", $model->Properties["tyousasyoSougouHyoukaNentani"]);
        knjCreateHidden($objForm, "tyousasyoSyusyokuPrintGappeiTougou", $model->Properties["tyousasyoSyusyokuPrintGappeiTougou"]);

        knjCreateHidden($objForm, "TANIPRINT_SOUGOU", $model->Properties["tyousasyoTaniPrint"]);
        knjCreateHidden($objForm, "TANIPRINT_RYUGAKU", $model->Properties["tyousasyoTaniPrint"]);
        knjCreateHidden($objForm, "tyousasyoCheckCertifDate", $model->Properties["tyousasyoCheckCertifDate"]);
        knjCreateHidden($objForm, "tyousasyoPrintHomeRoomStaff", $model->Properties["tyousasyoPrintHomeRoomStaff"]);

        //学籍番号　UPDATE用
        knjCreateHidden($objForm, "GET_SCHREGNO");
        //証明書番号　UPDATE用
        knjCreateHidden($objForm, "GET_CERTIF_INDEX");

        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();

        switch ($model->cmd) {
            case "edit2":
            case "edit3":
                $arg["reload"]  = "reloadTop(" .$model->disp .");";
                break;
            case "edit":
            case "search":
                $arg["reload"]  = "bottm_frm_disable1();";
                break;
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjg010Form1.html", $arg);
    }
}

function createCheckOpt(&$objForm, &$model, $name, $text, $label)
{
    $opt = array();
    if ($model->field[$name] == "1") {
        $extra = "checked='checked' ";
    } else {
        $extra = "";
    }
    $extra .= " id='".$name."'";
    $opt["TEXT"] = $text;
    $opt["VAL"] = knjCreateCheckBox($objForm, $name, "1", $extra).'<LABEL for="'.$name.'">'.$label.'</LABEL>';
    return $opt;
}

function createRadioOpt(&$objForm, &$model, $name, $text, $optArray, $js = "")
{
    $extra = array();
    $optA = array();
    foreach ($optArray as $i => $str) {
        $optA[] = $i;
        $extra[] = "id=\"".$name.$i."\" {$js} ";
    }
    $opt = array();
    $radioArray = knjCreateRadio($objForm, $name, $model->field[$name], $extra, $optA, get_count($optA));
    $opt["TEXT"] = $text;
    $arr = array();
    foreach ($optArray as $i => $str) {
        $arr[] = $radioArray[$name.$i].'<LABEL for="'.$name.$i.'">'.$str.'</LABEL>';
    }
    $opt["VAL"] = implode("&nbsp;", $arr);
    return $opt;
}
