<?php

require_once('for_php7.php');

class knje360jShingaku_2
{
    public function main(&$model)
    {
        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("shingaku_2", "POST", "knje360jindex.php", "", "shingaku_2");

        //DB接続
        $db = Query::dbCheckOut();

        //進路情報取得
        if ($model->cmd == "replace2" && $model->cmd != "replace2_college") {
            if ($model->cmd == "replace2") {
                $model->replace["selectdata"] = $model->grade.$model->hr_class.$model->attendno."_".$model->schregno;
                $model->replace["ghr"] = $model->grade."-".$model->hr_class;
            }
            if (isset($model->schregno) && isset($model->seq) && !isset($model->warning)) {
                $Row = $db->getRow(knje360jQuery::getSubQuery2($model), DB_FETCHMODE_ASSOC);
            } else {
                $Row =& $model->replace["field"];
            }
        } else {
            $Row =& $model->replace["field"];
        }

        //編集項目選択
        for ($i=0; $i<9; $i++) {
            $extra  = ($model->replace["check"][$i] == "1") ? "checked" : "";
            if ($i==8) {
                $extra .= " onClick=\"return check_all(this, '8');\"";
            }

            $arg["data"]["RCHECK".$i] = knjCreateCheckBox($objForm, "RCHECK".$i, "1", $extra, "");
        }

        //学校コードテキストボックス
        $extra = "onblur=\"this.value=toInteger(this.value);\"";
        $arg["data"]["FINSCHOOLCD"] = knjCreateTextBox($objForm, $Row["FINSCHOOLCD"], "FINSCHOOLCD", $model->finschoolcdKeta, $model->finschoolcdKeta, $extra);

        //学校情報
        $finSchool = $db->getRow(knje360jQuery::getFinSchoolInfo($Row["FINSCHOOLCD"]), DB_FETCHMODE_ASSOC);

        //学校名
        $arg["data"]["SCHOOL_NAME"] = $finSchool["SCHOOL_NAME"];

        //学校立
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

        //学校検索後の処理
        if ($model->cmd == "replace2_college") {
            //学校名
            $response  = $finSchool["SCHOOL_NAME"];
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

        //本都道府県
        $mainpref = $db->getOne(knje360jQuery::getMainPref());

        //所在地
        $Row["PREF_CD"] = ($Row["PREF_CD"]) ? $Row["PREF_CD"] : '-';
        $query = knje360jQuery::getPrefList($mainpref);
        makeCmb($objForm, $arg, $db, $query, "PREF_CD", $Row["PREF_CD"], "", 1, 1);

        //登録日
        $Row["TOROKU_DATE"] = ($Row["TOROKU_DATE"] == "") ? str_replace("-", "/", CTRL_DATE) : str_replace("-", "/", $Row["TOROKU_DATE"]);
        $arg["data"]["TOROKU_DATE"] = View::popUpCalendar($objForm, "TOROKU_DATE", $Row["TOROKU_DATE"]);

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
        //調査書発行ラジオボタン
        $opt = array(1);
        $extra = " onclick=\"showMsg(this);\"";
        $radioArray = knjCreateRadio($objForm, "ISSUE", $Row["ISSUE"], $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            $arg["data"]["ISSUE"] = $val;
        }

        //進路状況
        $query = knje360jQuery::getNameMst('E006');
        makeCmb($objForm, $arg, $db, $query, "PLANSTAT", $Row["PLANSTAT"], "", 1, 1);

        //入試日
        $Row["STAT_DATE1"] = ($Row["STAT_DATE1"] == "") ? "" : str_replace("-", "/", $Row["STAT_DATE1"]);
        $arg["data"]["STAT_DATE1"] = View::popUpCalendar($objForm, "STAT_DATE1", $Row["STAT_DATE1"]);

        //合格発表日
        $Row["STAT_DATE3"] = ($Row["STAT_DATE3"] == "") ? "" : str_replace("-", "/", $Row["STAT_DATE3"]);
        $arg["data"]["STAT_DATE3"] = View::popUpCalendar($objForm, "STAT_DATE3", $Row["STAT_DATE3"]);

        //年度学期表示
        $arg["YEAR_SEM"] = CTRL_YEAR.'年度　　'.CTRL_SEMESTERNAME;

        //生徒リストToリスト作成
        makeStudentList($objForm, $arg, $db, $model);

        //生徒項目名切替
        $arg["SCH_LABEL"] = $model->sch_label;

        //ボタン作成
        makeBtn($objForm, $arg);

        //hidden作成
        makeHidden($objForm, $db, $model, $Row);

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"]  = $objForm->get_finish();

        //テンプレートのHTMLを読み込んでデータを$arg経由で渡す。
        View::toHTML($model, "knje360jShingaku_2.html", $arg);
    }
}

//生徒リストToリスト作成
function makeStudentList(&$objForm, &$arg, $db, $model)
{
    //置換処理選択時の生徒の情報
    $array = explode(",", $model->replace["selectdata"]);

    //年組コンボ
    $query = knje360jQuery::getGradeHrClass($model);
    $extra = "onchange=\"return btn_submit('replace2B');\"";
    makeCmb($objForm, $arg, $db, $query, "GRADE_HR_CLASS", $model->replace["ghr"], $extra, 1);

    //生徒一覧取得
    $opt_right = array();
    $result = $db->query(knje360jQuery::getStudent($model, "right", array()));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if (!in_array($row["VALUE"], $array)) {
            $opt_right[]  = array("label" => $row["LABEL"],
                                  "value" => $row["VALUE"]);
        }
    }
    $result->free();

    //生徒一覧リストボックス作成
    $extra = "multiple style=\"width:170px\" width=\"170px\" ondblclick=\"move1('left')\"";
    $arg["data"]["RIGHT_PART"] = knjCreateCombo($objForm, "right_select", "", $opt_right, $extra, 20);

    //対象者一覧取得
    $opt_left = array();
    $result = $db->query(knje360jQuery::getStudent($model, "left", $array));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt_left[] = array("label" => $row["LABEL"],
                            "value" => $row["VALUE"]);
    }
    $result->free();

    //対象者一覧リストボックス作成
    $extra = "multiple style=\"width:170px\" width=\"170px\" ondblclick=\"move1('right')\"";
    $arg["data"]["LEFT_PART"] = knjCreateCombo($objForm, "left_select", "", $opt_left, $extra, 20);

    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:30px\" onclick=\"return moves('left');\"";
    $arg["button"]["SEL_ADD_ALL"] = knjCreateBtn($objForm, "sel_add_all", "<<", $extra);
    //対象選択ボタンを作成する
    $extra = "style=\"height:20px;width:30px\" onclick=\"return move1('left');\"";
    $arg["button"]["SEL_ADD"] = knjCreateBtn($objForm, "sel_add", "＜", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:30px\" onclick=\"return move1('right');\"";
    $arg["button"]["SEL_DEL"] = knjCreateBtn($objForm, "sel_del", "＞", $extra);
    //対象取消ボタンを作成する
    $extra = "style=\"height:20px;width:30px\" onclick=\"return moves('right');\"";
    $arg["button"]["SEL_DEL_ALL"] = knjCreateBtn($objForm, "sel_del_all", ">>", $extra);
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

//ボタン作成
function makeBtn(&$objForm, &$arg)
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

    //更新ボタンを作成する
    $extra = "onclick=\"return doSubmit()\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);
    //戻るボタン
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "戻 る", "onclick=\"return btn_submit('shingakuA');\"");
}

//hidden作成
function makeHidden(&$objForm, $db, $model, $Row)
{
    knjCreateHidden($objForm, "cmd");
    knjCreateHidden($objForm, "selectdata");
    knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
    knjCreateHidden($objForm, "SEQ", $model->seq);
    knjCreateHidden($objForm, "useFinschoolcdFieldSize", $model->Properties["useFinschoolcdFieldSize"]);

    $semes = $db->getRow(knje360jQuery::getSemesterMst(), DB_FETCHMODE_ASSOC);
    knjCreateHidden($objForm, "SDATE", str_replace("-", "/", $semes["SDATE"]));
    knjCreateHidden($objForm, "EDATE", str_replace("-", "/", $semes["EDATE"]));
}
?>

