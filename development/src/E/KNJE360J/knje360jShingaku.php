<?php

require_once('for_php7.php');

class knje360jShingaku
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("shingaku", "POST", "knje360jindex.php", "", "shingaku");

        //DB接続
        $db = Query::dbCheckOut();

        //生徒情報
        $info = $db->getRow(knje360jQuery::getSchInfo($model), DB_FETCHMODE_ASSOC);
        $ban = ($info["ATTENDNO"]) ? '番　' : '　';
        $arg["SCHINFO"] = $info["HR_NAME"].' '.$info["ATTENDNO"].$ban.$info["NAME_SHOW"];

        unset($model->replace);
        //警告メッセージを表示しない場合
        if (($model->cmd == "shingakuA" || $model->cmd == "shingaku_clear") && $model->cmd != "shingaku_college") {
            if (isset($model->schregno) && isset($model->seq) && !isset($model->warning)) {
                $query = knje360jQuery::getSubQuery2($model);
                $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $arg["NOT_WARNING"] = 1;
            } else {
                $Row =& $model->field;
            }
        } else {
            $Row =& $model->field;
        }

        //登録日
        $Row["TOROKU_DATE"] = ($Row["TOROKU_DATE"] == "") ? str_replace("-", "/", CTRL_DATE) : str_replace("-", "/", $Row["TOROKU_DATE"]);
        $arg["data"]["TOROKU_DATE"] = View::popUpCalendar($objForm, "TOROKU_DATE", $Row["TOROKU_DATE"]);

        //指導要録に表記する進路先（直接入力）
        $extra = "style=\"height:95px;\"";
        $arg["data"]["THINKEXAM"] = knjCreateTextArea($objForm, "THINKEXAM", 6, 51, "soft", $extra, $Row["THINKEXAM"]);

        //学校コードテキストボックス
        $extra = "onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["FINSCHOOLCD"] = knjCreateTextBox($objForm, $Row["FINSCHOOLCD"], "FINSCHOOLCD", $model->finschoolcdKeta, $model->finschoolcdKeta, $extra);

        //学校情報
        $query = knje360jQuery::getFinSchoolInfo($Row["FINSCHOOLCD"]);
        $finSchool = $db->getRow($query, DB_FETCHMODE_ASSOC);

        //学校名
        $arg["data"]["SCHOOL_NAME"] = $finSchool["SCHOOL_NAME"];

        //学部
        $arg["data"]["DISTDIV_NAME"] = $finSchool["DISTDIV_NAME"];

        //学科
        $extra = "";
        $arg["data"]["BUNAME"] = knjCreateTextBox($objForm, $Row["BUNAME"], "BUNAME", 60, 120, $extra);

        //郵便番号
        $arg["data"]["ZIPCD"] = $finSchool["ZIPCD"];

        //住所
        $arg["data"]["ADDR1"] = $finSchool["ADDR1"];
        $arg["data"]["ADDR2"] = $finSchool["ADDR2"];

        //電話番号
        $arg["data"]["TELNO"] = $finSchool["TELNO"];

        //本都道府県
        $mainpref = $db->getOne(knje360jQuery::getMainPref());

        //所在地
        $Row["PREF_CD"] = ($Row["PREF_CD"]) ? $Row["PREF_CD"] : '-';
        $query = knje360jQuery::getPrefList($mainpref);
        makeCmb($objForm, $arg, $db, $query, "PREF_CD", $Row["PREF_CD"], "", 1, 1);


        //学校検索後の処理
        if ($model->cmd == "shingaku_college") {
            $query = knje360jQuery::getLimit($model->field);
            $setLimit = $db->getRow($query, DB_FETCHMODE_ASSOC);

            //入試日
            $model->field["STAT_DATE1"] = makeDate($setLimit["EXAM_DATE"]);
            $response .= View::popUpCalendar($objForm, "STAT_DATE1", $model->field["STAT_DATE1"]);

            //合格発表日
            $model->field["STAT_DATE3"] = makeDate($setLimit["EXAM_PASS_DATE"]);
            $response .= "::".View::popUpCalendar($objForm, "STAT_DATE3", $model->field["STAT_DATE3"]);

            //学校名
            $response .= "::".$finSchool["SCHOOL_NAME"];
            //学部
            $response .= "::".$finSchool["DISTDIV_NAME"];
            //郵便番号
            $response .= "::".$finSchool["ZIPCD"];
            //住所
            $response .= "::".$finSchool["ADDR1"];
            $response .= "::".$finSchool["ADDR2"];
            //電話番号
            $response .= "::".$finSchool["TELNO"];

            echo $response;
            die();
        }

        //入試日
        $Row["STAT_DATE1"] = ($Row["STAT_DATE1"] == "") ? "" : str_replace("-", "/", $Row["STAT_DATE1"]);
        $arg["data"]["STAT_DATE1"] = View::popUpCalendar($objForm, "STAT_DATE1", $Row["STAT_DATE1"]);

        //合格発表日
        $Row["STAT_DATE3"] = ($Row["STAT_DATE3"] == "") ? "" : str_replace("-", "/", $Row["STAT_DATE3"]);
        $arg["data"]["STAT_DATE3"] = View::popUpCalendar($objForm, "STAT_DATE3", $Row["STAT_DATE3"]);

        //受験番号
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $Row["EXAMNO"], "EXAMNO", 10, 10, "");

        //備考1
        $arg["data"]["CONTENTEXAM"] = knjCreateTextBox($objForm, $Row["CONTENTEXAM"], "CONTENTEXAM", 80, 40, "");

        //備考2
        $extra = "style=\"height:35px;\"";
        $arg["data"]["REASONEXAM"] = knjCreateTextArea($objForm, "REASONEXAM", 2, 75, "soft", $extra, $Row["REASONEXAM"]);

        //受験方式
        $query = knje360jQuery::getNameMst('E002');
        makeCmb($objForm, $arg, $db, $query, "HOWTOEXAM", $Row["HOWTOEXAM"], "", 1, 1);

        //受験結果
        $query = knje360jQuery::getNameMst('E005');
        makeCmb($objForm, $arg, $db, $query, "DECISION", $Row["DECISION"], "", 1, 1);

        //証明書番号取得
        $certif_no = "";
        if (isset($model->seq)) {
            $query = knje360jQuery::getCertifNo($model, $model->seq);
            $certif_no = $db->getOne($query);
        }
        //証明書学校データ
        $query = knje360jQuery::getCertifSchoolDat($model);
        $certifSchool = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($certifSchool["CERTIF_NO"] == "0") {
            $arg["data"]["CERTIF_NO"] = $certif_no;
        }

        //調査書発行ラジオボタン
        $opt = array(1);
        $extra = ($certif_no != "") ? "disabled" : " onclick=\"showMsg(this);\"";
        $radioArray = knjCreateRadio($objForm, "ISSUE", $Row["ISSUE"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"]["ISSUE"] = $val;
        }

        //進路状況
        $query = knje360jQuery::getNameMst('E006');
        makeCmb($objForm, $arg, $db, $query, "PLANSTAT", $Row["PLANSTAT"], "", 1, 1);

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
        View::toHTML($model, "knje360jShingaku.html", $arg);
    }
}

//ボタン作成
function makeBtn(&$objForm, &$arg, $model)
{
    //確定ボタン
    $extra = " onclick=\"collegeSelectEvent3();\"";
    $arg["button"]["btn_kakutei"] = knjCreateBtn($objForm, "btn_kakutei", "確 定", $extra);

    //学校検索ボタンを作成する
    if (SCHOOLKIND == "K") {
        $setschooltype = "2";
    } elseif (SCHOOLKIND == "P") {
        $setschooltype = "3";
    } elseif (SCHOOLKIND == "J") {
        $setschooltype = "4";
    }
    $extra = "style=\"width:70px\" onclick=\"loadwindow('" .REQUESTROOT ."/X/KNJXSEARCH_FINSCHOOL/knjwfin_searchindex.php?cmd=searchMain&fscdname=FINSCHOOLCD&fsname=label_name&fszip=ZIPCD&fsaddr1=ADDR1&fsaddr2=ADDR2&l015=RITSU_NAME_ID&setSchoolKind={$setschooltype}&tell=TELNO', event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 500, 380)\"";
    $arg["button"]["btn_search"] = knjCreateBtn($objForm, "btn_search", "学校検索", $extra);

    $disabled = ($model->mode == "grd") ? " disabled" : "";
    //追加ボタンを作成する
    $arg["button"]["btn_insert"] = knjCreateBtn($objForm, "btn_insert", "追 加", "onclick=\"return btn_submit('shingaku_insert');\"");
    //追加後前の生徒へを作成する
    $extra = " onclick=\"return updateNextStudent('".$model->schregno."', 1);\" style=\"width:130px\"";
    $arg["button"]["btn_up_pre"] = knjCreateBtn($objForm, "btn_up_pre", "追加後前の{$model->sch_label}へ", $extra.$disabled);
    //追加後次の生徒へを作成する
    $extra = " onclick=\"return updateNextStudent('".$model->schregno."', 0);\" style=\"width:130px\"";
    $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_next", "追加後次の{$model->sch_label}へ", $extra.$disabled);
    //更新ボタンを作成する
    $extra = ($model->seq == "") ? "disabled" : "onclick=\"return btn_submit('shingaku_update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //クリアボタンを作成する
    $extra = ($model->seq == "") ? "disabled" : "onclick=\"return btn_submit('shingaku_clear');\"";
    $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
    //戻るボタン
    $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "戻 る", "onclick=\"return btn_submit('edit');\"");

    //進路相談ボタン
    $extra = "style=\"height:30px;background:#FFE4E1;color:#FF0000;font:bold\" onclick=\"return btn_submit('shinroSoudan');\"";
    $arg["button"]["btn_shinroSoudan"] = KnjCreateBtn($objForm, "btn_shinroSoudan", "進路相談", $extra.$disabled);

    //一括更新ボタン
    $link = REQUESTROOT."/E/KNJE360J/knje360jindex.php?cmd=replace2&SCHREGNO=".$model->schregno."&SEQ=".$model->seq;
    $extra = "onclick=\"window.open('$link','_self');\"";
    $arg["button"]["btn_replace"] = KnjCreateBtn($objForm, "btn_replace", "一括更新", $extra.$disabled);
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "SEQ", $model->seq);
    knjCreateHidden($objForm, "ORIGINAL_ISSUE", $Row["ISSUE"]);
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "useFinschoolcdFieldSize", $model->Properties["useFinschoolcdFieldSize"]);

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

    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}
//コンボ作成
function makeCmb2(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $space="")
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

    if (get_count($opt) == 2) {
        $value = ($value != "" && $value_flg) ? $value : $opt[1]["value"];
    } else {
        $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    }

    return knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//日付チェック
function makeDate($monthDay)
{
    if ($monthDay == "") {
        return "";
    }
    if (strlen($monthDay) != 4) {
        return "";
    }
    $month = substr($monthDay, 0, 2);
    $day = substr($monthDay, 2);
    $year = ($month * 1) < 4 ? CTRL_YEAR + 1 : CTRL_YEAR;
    if (checkdate($month, $day, $year)) {
        return $year."/".$month."/".$day;
    } else {
        return "";
    }
}
