<?php

require_once('for_php7.php');

class knje360jSyuusyoku
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("syuusyoku", "POST", "knje360jindex.php", "", "syuusyoku");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $info = $db->getRow(knje360jQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];

        //警告メッセージを表示しない場合
        if ($model->cmd == "syuusyokuA" || $model->cmd == "syuusyoku_clear") {
            if (isset($model->schregno) && !isset($model->warning) && $model->seq) {
                $Row = $db->getRow(knje360jQuery::getSubQuery3($model), DB_FETCHMODE_ASSOC);
                $arg["NOT_WARNING"] = 1;
            } else {
                $Row =& $model->field;
            }
        } else {
            $Row =& $model->field;
        }

        // 会社情報
        $company = $db->getRow(knje360jQuery::getCollegeOrCompanyMst(trim($Row["STAT_CD"])), DB_FETCHMODE_ASSOC);

        //登録日
        $Row["TOROKU_DATE"] = ($Row["TOROKU_DATE"] == "") ? str_replace("-", "/", CTRL_DATE) : str_replace("-", "/", $Row["TOROKU_DATE"]);
        $arg["data"]["TOROKU_DATE"] = View::popUpCalendar($objForm, "TOROKU_DATE", $Row["TOROKU_DATE"]);

        //求人番号
        $arg["data"]["SENKOU_NO"] = knjCreateTextBox($objForm, $Row["SENKOU_NO"], "SENKOU_NO", 5, 5, "");

        if ($model->cmd === 'pdf' || $model->cmd === 'syuusyoku' && $model->field["SENKOU_NO"] != "") {
            $setcompany = $db->getRow(knje360jQuery::getJobOfferList($model), DB_FETCHMODE_ASSOC);
            $company = $setcompany;
            $Row["STAT_CD"] = $company["STAT_CD"];
        }

        //会社コード
        $arg["data"]["STAT_CD"] = $company["STAT_CD"];

        //会社名
        $arg["data"]["STAT_NAME"] = $company["STAT_NAME"];

        //住所
        $arg["data"]["ZIPCD"] = $company["ZIPCD"];
        $arg["data"]["ADDR1"] = $company["ADDR1"];
        $arg["data"]["ADDR2"] = $company["ADDR2"];

        //電話番号
        $arg["data"]["TELNO"] = $company["TELNO"];

        //産業種別
        $arg["data"]["INDUSTRY_MNAME"] = $company["INDUSTRY_MNAME"];

        //職業種別（大分類）
        $query = knje360jQuery::getJobtypeLList();
        $extra = "onChange=\"btn_submit('syuusyoku')\"";
        makeCmb($objForm, $arg, $db, $query, "JOBTYPE_LCD", $Row["JOBTYPE_LCD"], $extra, 1, 1);

        //職業種別（中分類）
        $query = knje360jQuery::getJobtypeMList($Row["JOBTYPE_LCD"]);
        $extra = "onChange=\"btn_submit('syuusyoku')\"";
        makeCmb($objForm, $arg, $db, $query, "JOBTYPE_MCD", $Row["JOBTYPE_MCD"], $extra, 1, 1);

        //職業種別（小分類）
        $query = knje360jQuery::getJobtypeSList($Row["JOBTYPE_LCD"], $Row["JOBTYPE_MCD"]);
        makeCmb($objForm, $arg, $db, $query, "JOBTYPE_SCD", $Row["JOBTYPE_SCD"], "", 1, 1);

        //本都道府県
        $mainpref = $db->getOne(knje360jQuery::getMainPref());

        //所在地
        $Row["PREF_CD"] = ($Row["CITY_CD"]) ? $Row["PREF_CD"].'-'.$Row["CITY_CD"] : $Row["PREF_CD"].'-';
        $query = knje360jQuery::getPrefList($mainpref);
        makeCmb($objForm, $arg, $db, $query, "PREF_CD", $Row["PREF_CD"], "", 1, 1);

        //紹介区分ラジオボタン 1:学校紹介 2:自己・縁故 3.公務員
        $opt_intro = array(1, 2, 3);
        $Row["INTRODUCTION_DIV"] = ($Row["INTRODUCTION_DIV"] == "") ? "1" : $Row["INTRODUCTION_DIV"];
        $extra = array("id=\"INTRODUCTION_DIV1\"", "id=\"INTRODUCTION_DIV2\"", "id=\"INTRODUCTION_DIV3\"");
        $radioArray = knjCreateRadio($objForm, "INTRODUCTION_DIV", $Row["INTRODUCTION_DIV"], $extra, $opt_intro, get_count($opt_intro));
        foreach ($radioArray as $key => $val) {
            $arg["data"][$key] = $val;
        }

        //受験結果
        $query = knje360jQuery::getNameMst('E005');
        makeCmb($objForm, $arg, $db, $query, "DECISION", $Row["DECISION"], "", 1, 1);

        //進路状況
        $query = knje360jQuery::getNameMst('E006');
        makeCmb($objForm, $arg, $db, $query, "PLANSTAT", $Row["PLANSTAT"], "", 1, 1);

        //指導要録に表記する進路先
        $extra = "style=\"height:90px;\"";
        $arg["data"]["JOB_THINK"] = knjCreateTextArea($objForm, "JOB_THINK", 4, 75, "soft", $extra, $Row["JOB_THINK"]);

        //備考1
        $arg["data"]["REMARK1"] = knjCreateTextBox($objForm, $Row["REMARK1"], "REMARK1", 80, 40, "");

        //備考2
        $extra = "style=\"height:35px;\"";
        $arg["data"]["REMARK2"] = knjCreateTextArea($objForm, "REMARK2", 2, 75, "soft", $extra, $Row["REMARK2"]);

        //ボタン作成
        makeBtn($objForm, $arg, $model);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        if (get_count($model->warning)== 0 && $model->cmd !="shinro_clear") {
            $arg["next"] = "NextStudent(0);";
        } elseif ($model->cmd =="shinro_clear") {
            $arg["next"] = "NextStudent(1);";
        }

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje360jSyuusyoku.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //会社検索ボタンを作成する
    $extra = "onclick=\"wopen('" .REQUESTROOT."/X/KNJXSEARCH9/index.php?PATH=/E/KNJE360J/knje360jindex.php&cmd=&target=KNJE360J','search',0,0,790,470);\"";
    $arg["button"]["btn_search"] = knjCreateBtn($objForm, "btn_search", "会社検索", $extra);

    $disabled = ($model->mode == "grd") ? " disabled" : "";
    //追加ボタンを作成する
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", "onclick=\"return btn_submit('syuusyoku_insert');\"");
    //追加後前の生徒へを作成する
    $extra = " onclick=\"return updateNextStudent('".$model->schregno."', 1);\" style=\"width:130px\"";
    $arg["button"]["btn_up_pre"] = knjCreateBtn($objForm, "btn_up_pre", "追加後前の{$model->sch_label}へ", $extra.$disabled);
    //追加後次の生徒へを作成する
    $extra = " onclick=\"return updateNextStudent('".$model->schregno."', 0);\" style=\"width:130px\"";
    $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_next", "追加後次の{$model->sch_label}へ", $extra.$disabled);
    //更新ボタンを作成する
    $extra = ($model->seq == "") ? "disabled" : "onclick=\"return btn_submit('syuusyoku_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //クリアボタンを作成する
    $extra = ($model->seq == "") ? "disabled" : "onclick=\"return btn_submit('syuusyoku_clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", "onclick=\"return btn_submit('edit');\"");

    //進路相談ボタン
    $extra = "style=\"height:30px;background:#FFE4E1;color:#FF0000;font:bold\" onclick=\"return btn_submit('shinroSoudan');\"";
    $arg["button"]["btn_shinroSoudan"] = KnjCreateBtn($objForm, "btn_shinroSoudan", "進路相談", $extra.$disabled);
    
    //求人検索ボタンを作成する
    $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_JOB_OFFER/knjxjoboffer_searchindex.php?cmd=', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 320)\"";
    $arg["button"]["btn_search_senkouno"] = knjCreateBtn($objForm, "btn_search_senkouno", "求人検索", $extra);
    
    //PDF参照ボタンを作成する
    $extra = "onclick=\"return btn_submit('pdf');\"";
    $arg["button"]["btn_pdf"] = knjCreateBtn($objForm, "btn_pdf", "PDF参照", $extra);
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "SEQ", $model->seq);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "STAT_CD", $Row["STAT_CD"]);

    $semes = $db->getRow(knje360jQuery::getSemesterMst(), DB_FETCHMODE_ASSOC);
    knjCreateHidden($objForm, "SDATE", str_replace("-", "/", $semes["SDATE"]));
    knjCreateHidden($objForm, "EDATE", str_replace("-", "/", $semes["EDATE"]));
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space="")
{
    $opt = array();
    if ($space) {
        $opt[] = array('label' => "", 'value' => "");
    }
    $value_flg = false;
    $result1 = $db->query($query);
    while ($row1 = $result1->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row1["LABEL"],
                       'value' => $row1["VALUE"]);
        if ($value == $row1["VALUE"]) {
            $value_flg = true;
        }
    }

    $value = ($value && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
