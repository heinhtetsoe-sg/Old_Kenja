<?php

require_once('for_php7.php');

class knjz421Form2
{
    public function main(&$model)
    {
        //権限チェック
        if (AUTHORITY != DEF_UPDATABLE) {
            $arg["jscript"] = "OnAuthError();";
        }

        $objForm = new form();
        //フォーム作成
        //$arg["start"]   = $objForm->get_start("edit", "POST", "knjz421index.php", "", "edit");

        //警告メッセージを表示しない場合
        if (($model->senkou_no != "") && !isset($model->warning) && $model->cmd != 'chenge_cd' && $model->cmd != 'apply_jobtype' && $model->cmd != 'search') {
            $Row = knjz421Query::getRow($model->getyear, $model->senkou_no);
        } else {
            $Row =& $model->field;
        }
        $db = Query::dbCheckOut();

        // 会社情報
        if ($model->cmd == 'search') {
            $company = $db->getRow(knjz421Query::getCollegeOrCompanyMst(trim($Row["COMPANY_CD"])), DB_FETCHMODE_ASSOC);
            $Row["COMPANY_CD"]     = $company["COMPANY_CD"];
            $Row["COMPANY_NAME"]   = $company["COMPANY_NAME"];
            $Row["SHIHONKIN"]      = $company["SHIHONKIN"];
            $Row["SONINZU"]        = $company["SONINZU"];
            $Row["TONINZU"]        = $company["TONINZU"];
            $Row["COMPANY_ZIPCD"]  = $company["COMPANY_ZIPCD"];
            $Row["COMPANY_ADDR1"]  = $company["COMPANY_ADDR1"];
            $Row["COMPANY_ADDR2"]  = $company["COMPANY_ADDR2"];
            $Row["COMPANY_TELNO1"] = $company["COMPANY_TELNO1"];
        }

        //求人番号
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SENKOU_NO"] = knjCreateTextBox($objForm, $Row["SENKOU_NO"], "SENKOU_NO", 5, 5, $extra);

        //受付年月日
        $arg["data"]["ACCEPTANCE_DATE"] = View::popUpCalendarAlp($objForm, "ACCEPTANCE_DATE", str_replace("-", "/", $Row["ACCEPTANCE_DATE"]), $disabled, "");

        //管轄コンボボックス
        $query = knjz421Query::getKankatsu();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["KANKATSU"], "KANKATSU", $extra, 1, "BLANK");

        //会社コード
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["COMPANY_CD"] = knjCreateTextBox($objForm, $Row["COMPANY_CD"], "COMPANY_CD", 8, 8, $extra);

        //会社検索ボタン
        $extra = "onclick=\"wopen('" .REQUESTROOT."/X/KNJXSEARCH9/index.php?&PROGRAMID=KNJZ421&PATH=/Z/KNJZ421/knjz421index.php&cmd=&target=KNJZ421','search',0,0,790,470);\"";
        $arg["button"]["btn_company"] = knjCreateBtn($objForm, "btn_company", "会社検索", $extra);

        //会社名
        $extra = "";
        $arg["data"]["COMPANY_NAME"] = knjCreateTextBox($objForm, $Row["COMPANY_NAME"], "COMPANY_NAME", 80, 120, $extra);

        //会社名かな
        $extra = "";
        $arg["data"]["COMPANY_NAMEKANA"] = knjCreateTextBox($objForm, $Row["COMPANY_NAMEKANA"], "COMPANY_NAMEKANA", 80, 120, $extra);

        //事業内容
        $extra = "style=\"height:30px;\"";
        $arg["data"]["COMPANY_CONTENTS"] = KnjCreateTextArea($objForm, "COMPANY_CONTENTS", 2, 51, "soft", $extra, $Row["COMPANY_CONTENTS"]);

        //資本金
        $extra = "style=\"text-align: right\"";
        $arg["data"]["SHIHONKIN"] = knjCreateTextBox($objForm, $Row["SHIHONKIN"], "SHIHONKIN", 17, 17, $extra);

        //設立年度
        $extra = "onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["MAKECOMPANY_YEAR"] = knjCreateTextBox($objForm, $Row["MAKECOMPANY_YEAR"], "MAKECOMPANY_YEAR", 4, 4, $extra);

        //会社郵便番号
        $arg["data"]["COMPANY_ZIPCD"] = View::popUpZipCode($objForm, "COMPANY_ZIPCD", $Row["COMPANY_ZIPCD"], "COMPANY_ADDR1");

        //会社住所
        $extra = "";
        $arg["data"]["COMPANY_ADDR1"] = knjCreateTextBox($objForm, $Row["COMPANY_ADDR1"], "COMPANY_ADDR1", 60, 90, $extra);
        if ($model->Properties["joblabel_requiredinput"] == "1") {
            //フラグが立っていれば必須チェックを有効とする。
            $arg["requiredinput1"] = 1;
        }
        $arg["data"]["COMPANY_ADDR2"] = knjCreateTextBox($objForm, $Row["COMPANY_ADDR2"], "COMPANY_ADDR2", 60, 90, $extra);

        //会社電話番号
        $extra = "";
        $arg["data"]["COMPANY_TELNO1"] = knjCreateTextBox($objForm, $Row["COMPANY_TELNO1"], "COMPANY_TELNO1", 16, 16, $extra);
        $arg["data"]["COMPANY_TELNO2"] = knjCreateTextBox($objForm, $Row["COMPANY_TELNO2"], "COMPANY_TELNO2", 16, 16, $extra);

        //会社FAX番号
        $extra = "";
        $arg["data"]["COMPANY_FAXNO"] = knjCreateTextBox($objForm, $Row["COMPANY_FAXNO"], "COMPANY_FAXNO", 16, 16, $extra);

        //産業分類
        $extra = " style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["INDUSTRY_SCD"] = knjCreateTextBox($objForm, $Row["INDUSTRY_SCD"], "INDUSTRY_SCD", 3, 3, $extra);

        //産業分類検索ボタン
        $extra = " onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_INDUSTRY_S/knjxindustrys_searchindex.php?cmd=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 320)\"";
        $arg["button"]["btn_industry"] = knjCreateBtn($objForm, "btn_industry", "産業分類検索", $extra);
        $industry_sname = $db->getOne(knjz421Query::getIndustrySName($Row["INDUSTRY_SCD"]));
        $arg["data"]["INDUSTRY_SNAME"] = $Row["INDUSTRY_SNAME"] ? $Row["INDUSTRY_SNAME"] : $industry_sname;

        //職業分類
        $extra = " style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["JOBTYPE_SCD"] = knjCreateTextBox($objForm, $Row["JOBTYPE_SCD"], "JOBTYPE_SCD", 3, 3, $extra);
        //hidden
        if ($model->cmd != 'apply_jobtype') {
            knjCreateHidden($objForm, "JOBTYPE_LCD", $Row["JOBTYPE_LCD"]);
            knjCreateHidden($objForm, "JOBTYPE_MCD", $Row["JOBTYPE_MCD"]);
        } else {
            $setLMcd = $db->getRow(knjz421Query::getjobTypeLMcd($Row["JOBTYPE_SCD"]), DB_FETCHMODE_ASSOC);
            knjCreateHidden($objForm, "JOBTYPE_LCD", $setLMcd["JOBTYPE_LCD"]);
            knjCreateHidden($objForm, "JOBTYPE_MCD", $setLMcd["JOBTYPE_MCD"]);
        }

        //職業分類検索ボタン
        $extra = " onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_JOBTYPE_S/knjxjobtypes_searchindex.php?cmd=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 320)\"";
        $arg["button"]["btn_jobType"] = knjCreateBtn($objForm, "btn_jobType", "職業分類検索", $extra);
        $jobType_sName = $db->getOne(knjz421Query::getjobTypeSName($model, $Row["JOBTYPE_LCD"], $Row["JOBTYPE_MCD"], $Row["JOBTYPE_SCD"]));
        $arg["data"]["JOBTYPE_SNAME"] = $Row["JOBTYPE_SNAME"] ? $Row["JOBTYPE_SNAME"] : $jobType_sName;

        //職業分類確定ボタン
        $extra = "onclick=\"return btn_submit('apply_jobtype');\"";
        $arg["button"]["btn_apply_jobtype"] = knjCreateBtn($objForm, "btn_apply_jobtype", "確定", $extra);
        if ($model->cmd == 'apply_jobtype' && $jobType_sName == "") {
            $arg["Jobtype"] = "RetJobtype();";
        }

        //全体人数
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SONINZU"] = knjCreateTextBox($objForm, $Row["SONINZU"], "SONINZU", 8, 8, $extra);

        //就業場所人数
        $extra = "style=\"text-align:right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["TONINZU"] = knjCreateTextBox($objForm, $Row["TONINZU"], "TONINZU", 8, 8, $extra);

        //就業場所(事業所名)
        $extra = "style=\"height:30px;\"";
        $arg["data"]["SHUSHOKU_NAME"] = KnjCreateTextArea($objForm, "SHUSHOKU_NAME", 2, 51, "soft", $extra, $Row["SHUSHOKU_NAME"]);

        //checkbox(会社所在地と同じ)
        if (
            $Row["COMPANY_ADDR1"] == $Row["SHUSHOKU_ADDR1"] && $Row["COMPANY_ADDR2"]  == $Row["SHUSHOKU_ADDR2"] &&
            $Row["COMPANY_ZIPCD"] == $Row["SHUSHOKU_ZIPCD"] && $Row["COMPANY_TELNO1"] == $Row["SHUSHOKU_TELNO1"]
        ) {
            $checked = " checked";
        } else {
            $checked = "";
        }
        $extra = "id=\"SAMEADDR\" onclick=\"syugyouCopy(this)\"".$checked;
        $arg["data"]["SAMEADDR"] = knjCreateCheckBox($objForm, "SAMEADDR", "1", $extra);

        //就業場所郵便番号
        if ($model->Properties["joblabel_requiredinput"] == "1") {
            //フラグが立っていれば必須チェックを有効とする。
            $arg["requiredinput2"] = 1;
        }
        $arg["data"]["SHUSHOKU_ZIPCD"] = View::popUpZipCode($objForm, "SHUSHOKU_ZIPCD", $Row["SHUSHOKU_ZIPCD"], "SHUSHOKU_ADDR1");

        //就業場所住所
        $extra = "";
        if ($model->Properties["joblabel_requiredinput"] == "1") {
            //フラグが立っていれば必須チェックを有効とする。
            $arg["requiredinput3"] = 1;
            $arg["requiredinput4"] = 1;
        }
        $arg["data"]["SHUSHOKU_ADDR1"] = knjCreateTextBox($objForm, $Row["SHUSHOKU_ADDR1"], "SHUSHOKU_ADDR1", 60, 90, $extra);
        $arg["data"]["SHUSHOKU_ADDR2"] = knjCreateTextBox($objForm, $Row["SHUSHOKU_ADDR2"], "SHUSHOKU_ADDR2", 60, 90, $extra);

        //就業場所電話番号
        $extra = "";
        if ($model->Properties["joblabel_requiredinput"] == "1") {
            //フラグが立っていれば必須チェックを有効とする。
            $arg["requiredinput5"] = 1;
        }
        $arg["data"]["SHUSHOKU_TELNO1"] = knjCreateTextBox($objForm, $Row["SHUSHOKU_TELNO1"], "SHUSHOKU_TELNO1", 16, 16, $extra);
        $arg["data"]["SHUSHOKU_TELNO2"] = knjCreateTextBox($objForm, $Row["SHUSHOKU_TELNO2"], "SHUSHOKU_TELNO2", 16, 16, $extra);

        //就業場所FAX番号
        $extra = "";
        $arg["data"]["SHUSHOKU_FAXNO"] = knjCreateTextBox($objForm, $Row["SHUSHOKU_FAXNO"], "SHUSHOKU_FAXNO", 16, 16, $extra);

        //雇用形態コンボボックス
        $query = knjz421Query::getEmploymentStatus();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["EMPLOYMENT_STATUS"], "EMPLOYMENT_STATUS", $extra, 1, "BLANK");

        $extra = "";
        $arg["data"]["APPLICATION_TARGET"] = knjCreateTextBox($objForm, $Row["APPLICATION_TARGET"], "APPLICATION_TARGET", 45, 20, $extra);

        if ($model->Properties["useCompany_Sort"] == "1") {
            //会社職種テキストフィールド
            $extra = "";
            $arg["data"]["COMPANY_SORT_REMARK"] = knjCreateTextBox($objForm, $Row["COMPANY_SORT_REMARK"], "COMPANY_SORT_REMARK", 45, 20, $extra);
        } else {
            //会社職種コンボボックス
            $query = knjz421Query::getCompanycd();
            $extra = "";
            makeCmb($objForm, $arg, $db, $query, $Row["COMPANY_SORT"], "COMPANY_SORT", $extra, 1, "BLANK");
        }

        //通勤
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["TSUKIN_NINZU"] = knjCreateTextBox($objForm, $Row["TSUKIN_NINZU"], "TSUKIN_NINZU", 3, 3, $extra);

        //住込
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SUMIKOMI_NINZU"] = knjCreateTextBox($objForm, $Row["SUMIKOMI_NINZU"], "SUMIKOMI_NINZU", 3, 3, $extra);

        //不問
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["FUMON_NINZU"] = knjCreateTextBox($objForm, $Row["FUMON_NINZU"], "FUMON_NINZU", 3, 3, $extra);

        //推薦
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SUISEN_NINZU"] = knjCreateTextBox($objForm, $Row["SUISEN_NINZU"], "SUISEN_NINZU", 3, 3, $extra);

        //二次募集チェックボックス
        $extra = " id=\"NIJI_BOSYU\" ";
        if ($Row["NIJI_BOSYU"] == "1") {
            $extra .= " checked='checked' ";
        }
        $arg["data"]["NIJI_BOSYU"] = knjCreateCheckBox($objForm, "NIJI_BOSYU", "1", $extra);

        //通勤賃金
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["TSUKIN_SALARY"] = knjCreateTextBox($objForm, $Row["TSUKIN_SALARY"], "TSUKIN_SALARY", 8, 8, $extra);

        //住込賃金
        $extra = "style=\"text-align: right\" onblur=\"this.value=toInteger(this.value)\"";
        $arg["data"]["SUMIKOMI_SALARY"] = knjCreateTextBox($objForm, $Row["SUMIKOMI_SALARY"], "SUMIKOMI_SALARY", 8, 8, $extra);

        //選考受付
        $arg["data"]["SELECT_RECEPT_DATE"] = View::popUpCalendar($objForm, "SELECT_RECEPT_DATE", str_replace("-", "/", $Row["SELECT_RECEPT_DATE"]));

        //選考日
        $arg["data"]["SELECT_DATE"] = View::popUpCalendar($objForm, "SELECT_DATE", str_replace("-", "/", $Row["SELECT_DATE"]));

        //選考方法コンボボックス1
        $query = knjz421Query::getJudgeing();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["JUDGING_MEANS1"], "JUDGING_MEANS1", $extra, 1, "BLANK");

        //選考方法コンボボックス2
        $query = knjz421Query::getJudgeing();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["JUDGING_MEANS2"], "JUDGING_MEANS2", $extra, 1, "BLANK");

        //選考方法コンボボックス3
        $query = knjz421Query::getJudgeing();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["JUDGING_MEANS3"], "JUDGING_MEANS3", $extra, 1, "BLANK");

        //選考方法コンボボックス4
        $query = knjz421Query::getJudgeing();
        $extra = "";
        makeCmb($objForm, $arg, $db, $query, $Row["JUDGING_MEANS4"], "JUDGING_MEANS4", $extra, 1, "BLANK");

        //担当者名
        $extra = "";
        $arg["data"]["PERSONNEL_MANAGER"] = knjCreateTextBox($objForm, $Row["PERSONNEL_MANAGER"], "PERSONNEL_MANAGER", 21, 30, $extra);

        //担当者課係
        $extra = "";
        $arg["data"]["DEPARTMENT_POSITION"] = knjCreateTextBox($objForm, $Row["DEPARTMENT_POSITION"], "DEPARTMENT_POSITION", 27, 40, $extra);

        //見学会チェックボックス
        $extra = " id=\"KENGAKU_KAI\" ";
        if ($Row["KENGAKU_KAI"] == "1") {
            $extra .= " checked='checked' ";
        }
        $arg["data"]["KENGAKU_KAI"] = knjCreateCheckBox($objForm, "KENGAKU_KAI", "1", $extra);

        //休日チェックボックス1
        if ($Row["HOLIDAY1"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["HOLIDAY1"] = knjCreateCheckBox($objForm, "HOLIDAY1", "1", $extra);

        //休日チェックボックス2
        if ($Row["HOLIDAY2"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["HOLIDAY2"] = knjCreateCheckBox($objForm, "HOLIDAY2", "1", $extra);

        //休日チェックボックス3
        if ($Row["HOLIDAY3"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["HOLIDAY3"] = knjCreateCheckBox($objForm, "HOLIDAY3", "1", $extra);

        //休日チェックボックス4
        if ($Row["HOLIDAY4"] == "1") {
            $extra = "checked='checked' ";
        } else {
            $extra = "";
        }
        $arg["data"]["HOLIDAY4"] = knjCreateCheckBox($objForm, "HOLIDAY4", "1", $extra);

        //休日その他
        $extra = "";
        $arg["data"]["HOLIDAY_REMARK"] = knjCreateTextBox($objForm, $Row["HOLIDAY_REMARK"], "HOLIDAY_REMARK", 20, 30, $extra);

        //休暇
        $extra = "";
        $arg["data"]["OTHER_HOLIDAY"] = knjCreateTextBox($objForm, $Row["OTHER_HOLIDAY"], "OTHER_HOLIDAY", 16, 30, $extra);

        //備考
        $extra = "";
        $arg["data"]["REMARK"] = knjCreateTextBox($objForm, $Row["REMARK"], "REMARK", 40, 40, $extra);

        if ($model->Properties["kyujinhyoPDFtorikomi"] != "1") {
            $arg["kyujinhyoPDFtorikomi"] = "1";
            //ファイルからの取り込み
            $arg["data"]["PDF_FILE"] = knjCreateFile($objForm, "PDF_FILE", "", 512000);
            //実行ボタン
            $extra = "onclick=\"return btn_submit('execute');\"";
            $arg["button"]["BTN_OK"] = knjCreateBtn($objForm, "btn_ok", "実 行", $extra);

            //PDF参照ボタンを作成する
            $extra = "onclick=\"return btn_submit('pdf');\"";
            $arg["button"]["btn_pdf"] = knjCreateBtn($objForm, "btn_pdf", "PDF参照", $extra);
        } else {
            $arg["kyujinhyoPDFtorikomi"] = "";
        }

        //CSVファイルアップロードコントロール
        makeCsv($objForm, $arg, $model);

        //追加ボタン
        $extra = "onclick=\"return btn_submit('add');\"";
        $arg["button"]["btn_add"] = knjCreateBtn($objForm, "btn_add", "追 加", $extra);

        //修正ボタン
        $extra = "onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

        //削除ボタン
        $extra = "onclick=\"return btn_submit('delete');\"";
        $arg["button"]["btn_del"] = knjCreateBtn($objForm, "btn_del", "削 除", $extra);

        //クリアボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);

        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");

        //フォーム作成
        $arg["start"]   = $objForm->get_start("edit", "POST", "knjz421index.php", "", "edit");
        $arg["finish"]  = $objForm->get_finish();
        Query::dbCheckIn($db);
        if (VARS::get("cmd") != "edit" && $model->cmd != 'chenge_cd' &&  $model->cmd != 'apply_jobtype' && !$model->warning) {
            $model->year = CTRL_YEAR;
            $arg["reload"]  = "parent.left_frame.location.href='knjz421index.php?cmd=list';";
        }

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knjz421Form2.html", $arg);
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

//CSV作成
function makeCsv(&$objForm, &$arg, $model)
{
    //出力取込種別ラジオボタン 1:取込 2:書出 3:エラー書出 4:見本
    $opt_shubetsu = array(1, 2, 3, 4);
    $model->field["OUTPUT"] = ($model->field["OUTPUT"]) ? $model->field["OUTPUT"] : "1";
    $click = " onclick=\"return changeRadio(this);\"";
    $extra = array("id=\"OUTPUT1\"".$click, "id=\"OUTPUT2\"".$click, "id=\"OUTPUT3\"".$click, "id=\"OUTPUT4\"".$click);
    $radioArray = knjCreateRadio($objForm, "OUTPUT", $model->field["OUTPUT"], $extra, $opt_shubetsu, get_count($opt_shubetsu));
    foreach ($radioArray as $key => $val) {
        $arg["data"][$key] = $val;
    }

    //ファイルからの取り込み
    $extra = ($model->field["OUTPUT"] == "1") ? "" : "disabled";
    $arg["FILE"] = knjCreateFile($objForm, "FILE", $extra, 1024000);

    //ヘッダ有チェックボックス
    $check_header  = "checked id=\"HEADER\"";
    $arg["data"]["HEADER"] = knjCreateCheckBox($objForm, "HEADER", "on", $check_header, "");

    //実行ボタン
    $extra = "onclick=\"return btn_submit('csv');\"";
    $arg["button"]["btn_exec"] = knjCreateBtn($objForm, "btn_exec", "実 行", $extra);
}
