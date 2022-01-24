<?php

require_once('for_php7.php');

class knjh400Form1
{
    public function main(&$model)
    {
        $objForm      = new form();
        $arg["start"] = $objForm->get_start("edit", "POST", "knjh400index.php", "", "edit");
        //DB接続
        $db = Query::dbCheckOut();

        //ボタン表示用ラジオ
        if ($model->btnRadio != "") {
            $arg["btn"] = 1;
        } else {
            $arg["btn"] = "";
        }
        if ($model->Properties["un_BTN_MOCK"] == "1") {
            //外部模試情報 を非表示とする
            $opt = array(1, 2, 3);
            $extra = array("id=\"btnRadio1\" onclick=\"btn_submit('radio');\"", "id=\"btnRadio2\" onclick=\"btn_submit('radio');\"", "");
            $label = array("btnRadio1" => "基本情報", "btnRadio2" => "テスト･出欠情報", "btnRadio3" => "");
        } else {
            $opt = array(1, 2, 3);
            $extra = array("id=\"btnRadio1\" onclick=\"btn_submit('radio');\"", "id=\"btnRadio2\" onclick=\"btn_submit('radio');\"", "id=\"btnRadio3\" onclick=\"btn_submit('radio');\"");
            $label = array("btnRadio1" => "基本情報", "btnRadio2" => "テスト･出欠情報", "btnRadio3" => "外部模試情報");
        }
        if ($model->Properties["KNJH400_Pattern"] === '1') {
            $opt = array_merge($opt, array(4, 5, 6, 7));
            $extra = array_merge($extra, array("id=\"btnRadio4\" onclick=\"btn_submit('radio');\"", "id=\"btnRadio5\" onclick=\"btn_submit('radio');\"", "id=\"btnRadio6\" onclick=\"btn_submit('radio');\"", "id=\"btnRadio7\" onclick=\"btn_submit('radio');\""));
            $label = array_merge($label, array("btnRadio4" => "進路情報", "btnRadio5" => "調査書・要録", "btnRadio6" => "保健情報", "btnRadio7" => "入試情報"));
        }
        $radioArray = knjCreateRadio($objForm, "btnRadio", $model->btnRadio, $extra, $opt, get_count($opt));
        foreach ($radioArray as $key => $val) {
            if ($model->Properties["un_BTN_MOCK"] != "1" || $key != 'btnRadio3') {
                $arg["data3"][$key] = $val."<LABEL for=\"".$key."\">".$label[$key]."</LABEL>　";
            }
        }

        if ($model->Properties["KNJH400_Pattern"] == "1") {
            $arg['KNJH400_Pattern'] = 1;
        } else {
            $arg['NON_KNJH400_Pattern'] = 1;
        }

        $result = $db->query(knjh400Query::getZaisekiInfo($model->schregno));
        while ($zaisekirow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $arg['zaiseki'][] = $zaisekirow;
        }

        //生徒データ表示
        makeStudentInfo($arg, $db, $model);

        //ALLチェック
        $arg["data"]["CHECKALL"] = createCheckBox($objForm, "CHECKALL", "", "onClick=\"return check_all(this);\"", "");

        //分類絞り込みコンボ
        $query = knjh400Query::getNameMst($model, 'H307');
        $extra = "onChange=\"return btn_submit('radio');\"";
        makeCmb($objForm, $arg, $db, $query, "NARROWING", $model->narrowing, $extra, 1, "BLANK");

        //並び替え設定
        $sortQuery = makeSortLink($arg, $model);

        //行動の記録
        makeActionData($objForm, $arg, $db, $model, $sortQuery);

        //評定計算
        hyouteiCalc($model, $db, $arg);

        $cnt = 0;
        $result = $db->query(knjh400Query::selectQuery4($model));
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            foreach ($row as $key => $value) {
                if ($key != 'KAIKIN') {
                    $sumrow[$key] += $value;
                }
            }
            $row['SYUSSEKIRITU'] = round($row['SYUSSEKIRITU'], 3);
            $row['KANSANKESSEKISUU'] = round($row['KANSANKESSEKISUU'], 2);
            $arg['syukketu'][] = $row;
            $cnt++;
        }
        $sumrow['GRADE_CD'] = '計';
        if ($cnt != 0) {
            $sumrow['SYUSSEKIRITU'] = round($sumrow['SYUSSEKIRITU'] / $cnt, 3);
        }
        $sumrow['KANSANKESSEKISUU'] = round($sumrow['KANSANKESSEKISUU'], 2);
        $arg['syukketu'][] = $sumrow;
        //ボタン作成
        makeBtn($db, $objForm, $arg, $model);

        //hidden
        makeHidden($objForm, $model);

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        $arg["IFRAME"] = View::setIframeJs();
        View::toHTML($model, "knjh400Form1.html", $arg);
    }
}

//生徒データ表示
function makeStudentInfo(&$arg, $db, $model)
{
    $grad_Row = array();
    if (strlen($model->schregno)) {
        //校種取得
        $schoolkind = $db->getOne(knjh400Query::getSchoolKind($model));
        //生徒データ取得
        $stuData = $db->getRow(knjh400Query::getStudentData($model, $schoolkind), DB_FETCHMODE_ASSOC);
        //画像
        $grad_Row["FACE_IMG"] = REQUESTROOT."/".$model->control_data["LargePhotoPath"]."/P".$stuData["SCHREGNO"].".".$model->control_data["Extension"];
        $grad_Row["IMG_PATH"] = REQUESTROOT."/".$model->control_data["LargePhotoPath"]."/P".$stuData["SCHREGNO"].".".$model->control_data["Extension"];
        //表示項目
        $query = knjh400Query::getPortfolioHeadMst($model, $schoolkind);
        $chkData = $db->getOne($query);
        if (strlen($chkData)) {
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $grad_Row["HEADNAME".$row["SHOWORDER"]] = $row["HEADNAME"];
                $grad_Row["HEADDATA".$row["SHOWORDER"]] = $stuData[$row["FIELDNAME"]];
            }
        } else {
            //項目名（初期値）
            foreach ($model->head as $key => $val) {
                list($label, $field) = $val;
                $grad_Row["HEADNAME".$key] = $label;
                $grad_Row["HEADDATA".$key] = $stuData[$field];
            }
        }
    } else {
        //項目名（初期値）
        foreach ($model->head as $key => $val) {
            list($label, $field) = $val;
            $grad_Row["HEADNAME".$key] = $label;
        }
    }

    if ($model->Properties["useDispUnDispPicture"] === '1') {
        $arg["unDispPicture"] = "1";
    } else {
        $arg["dispPicture"] = "1";
    }

    $arg["data"] = $grad_Row;
}

//評定計算
function hyouteiCalc(&$model, &$db, &$arg)
{
    $datas = array();
    $annuals = array();
    $result2 = $db->query(knjh400Query::selectQuery($model));
    while ($row2 = $result2->fetchRow(DB_FETCHMODE_ASSOC)) {
        $annuals[] = $row2['ANNUAL'];
        $arg['gradename'][]['GRADE_NAME2'] = $row2['GRADE_NAME2'];
        $datas[$row2['ANNUAL']]= array();
    }
    $result2->free();

    $arg['averowspan'] = get_count($annuals) + 2;

    $idx = 0;
    $subdataTemplate = array();
    for ($i = 0; $i < get_count($annuals); $i++) {
        $subdataTemplate[] = '';
    }
    $sum = 0;
    $subdata = $subdataTemplate;
    $value = '';
    $result = $db->query(knjh400Query::selectQuery2($model));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        if ($value != $row['VALUE']) {
            if ($value == '' && get_count($annuals) > 1) {
            } else {
                if (isset($backupRow)) {
                    if ($className == $backupRow['CLASSNAME']) {
                        $backupRow['CLASSNAME'] = '';
                    } else {
                        $className = $backupRow['CLASSNAME'];
                        $now = $idx;
                    }
                    $backupRow['sum'] = $sum;
                    $backupRow['subdata'] = $subdata;
                    $arg['data'][] = $backupRow;
                    $arg['data'][$now]['avedata']+=$ave;
                    $arg['data'][$now]['avecnt']+=$cnt;
                    if ($cnt != 0) {
                        $arg['data'][$now]['ave'] =sprintf("%01.2f", round(($arg['data'][$now]['avedata'] / $arg['data'][$now]['avecnt']) * 100) / 100, 2);
                    }
                    $sum = 0;
                    $ave = 0;
                    $cnt = 0;
                    $subdata = $subdataTemplate;
                    $idx++;
                }
            }
            $value = $row['VALUE'];
        }
        $sum += intval($row['CREDIT']);
        $ave += intval($row['VALUATION']);
        if (isset($row['VALUATION'])) {
            $subdata[$annualsIdx[$row['ANNUAL']]] += $row['VALUATION'];
        }
        if (isset($row['VALUATION'])) {
            $datas[$row['ANNUAL']][] = $row['VALUATION'];
            $cnt++;
        }
        $backupRow = $row;
    }
    if ($idx != 0) {
        if ($value == '' && get_count($annuals) > 1) {
        } else {
            if ($className == $backupRow['CLASSNAME']) {
                $backupRow['CLASSNAME'] = '';
            } else {
                $className = $backupRow['CLASSNAME'];
                $now = $idx;
            }
            $backupRow['sum'] = $sum;
            $backupRow['subdata'] = $subdata;
            $arg['data'][] = $backupRow;
            $arg['data'][$now]['avedata']+=$ave;
            $arg['data'][$now]['avecnt']+=$cnt;
            if ($cnt != 0) {
                $arg['data'][$now]['ave'] =sprintf("%01.2f", round(($arg['data'][$now]['avedata'] / $arg['data'][$now]['avecnt']) * 100) / 100, 2);
            }
            $sum = 0;
            $ave = 0;
            $cnt = 0;
            $subdata = $subdataTemplate;
            $idx++;
        }
    }

    $cnt = 0;
    for ($i = 0; $i < get_count($annuals); $i++) {
        $sum = 0;
        for ($j = 0; $j < get_count($datas[$annuals[$i]]); $j++) {
            $sum+=intval($datas[$annuals[$i]][$j]);
            $cnt++;
        }
        $allsum += $sum;
        if (get_count($datas[$annuals[$i]]) == 0) {
            $arg['avedata'][]['disp'] = 0;
            continue;
        }
        $arg['avedata'][] =array('disp' => sprintf("%01.2f", round(($sum / get_count($datas[$annuals[$i]])) * 100) / 100, 2), 'name' => $arg['gradename'][$i]['GRADE_NAME2']);
    }
    if ($cnt != 0) {
        $arg['allave'] = sprintf("%01.2f", round(($allsum / $cnt) * 100) / 100, 2);
    } else {
        $arg['allave'] = 0;
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $blank = "")
{
    $opt = array();
    if ($blank) {
        $opt[] = array('label' => "", 'value' => "");
    }
    $value_flg = false;
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($value == $row["VALUE"]) {
            $value_flg = true;
        }
    }
    $value = ($value != "" && $value_flg) ? $value : $opt[0]["value"];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//並び替え設定
function makeSortLink(&$arg, $model)
{
    $sortQuery = $model->taitleSort[$model->sort]["ORDER".$model->taitleSort[$model->sort]["VALUE"]];
    foreach ($model->taitleSort as $key => $val) {
        $arg["data"][$key] = View::alink(
            "knjh400index.php",
            "<font color=\"white\">".$val["NAME".$val["VALUE"]]."</font>",
            "",
            array("cmd" => $key."CLICK", $key => $val["VALUE"], "sort" => $key)
        );
        if ($key != $model->sort) {
            $sortQuery .= $val["ORDER".$val["VALUE"]];
        }
    }
    return $sortQuery;
}

//行動の記録
function makeActionData(&$objForm, &$arg, $db, $model, $sortQuery)
{
    $time = array();
    $result = $db->query(knjh400Query::getActionDuc($model, $sortQuery));
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        //チェックボックス
        $disable = (AUTHORITY != DEF_UPDATABLE && $row["STAFFCD"] != STAFFCD) ? "disabled" : "";
        $checkVal = $row["SCHREGNO"].":".$row["ACTIONDATE"].":".$row["SEQ"];
        $row["DELCHK"] = createCheckBox($objForm, "DELCHK", $checkVal, $disable, "1");

        //リンク設定
        $subdata = "loadwindow('knjh400index.php?cmd=upd&cmdSub=upd&SCHREGNO={$row["SCHREGNO"]}&ACTIONDATE={$row["ACTIONDATE"]}&SEQ={$row["SEQ"]}',0,0,600,450)";
        $row["TITLE"] = View::alink("#", htmlspecialchars($row["TITLE"]), "onclick=\"$subdata\"");

        $time = preg_split("/:/", $row["ACTIONTIME"]);
        $row["ACTIONTIME"] = $time[0]."：".$time[1];

        $arg["data2"][] = $row;
    }
    $result->free();
}

//ボタン作成
function makeBtn($db, &$objForm, &$arg, $model)
{
    $authAll  = "OFF";
    $authSome = "OFF";
    if ($model->auth["CHAIRFLG"] == "ON" || $model->auth["HRCLASSFLG"] == "ON" || $model->auth["COURSEFLG"] == "ON") {
        $authAll = "ON";
    }
    if ($model->auth["CLUBFLG"] == "ON") {
        $authSome = "ON";
    }

    if ($model->btnRadio == "1") {
        //学籍基礎情報
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        $extra = $disable." onClick=\" wopen('".REQUESTROOT."/X/KNJXSCHREG/knjxschregindex.php?SCHREGNO=".$model->schregno."&YEAR=".$model->year."&BUTTON_FLG=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["data"]["BTN_SCHREG"] = createBtn($objForm, "BTN_SCHREG", "学籍基礎情報", $extra);

        //住所情報
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        $extra = $disable." onClick=\" wopen('".REQUESTROOT."/A/KNJA110_2A/knja110_2aindex.php?SCHREGNO=".$model->schregno."&SEND_UN_UPDATE=1&YEAR=".$model->year."&BUTTON_FLG=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        //$extra = $disable;
        $arg["data"]["BTN_ZYUSYO"] = createBtn($objForm, "BTN_ZYUSYO", "住所情報", $extra);

        //通学情報
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        $extra = $disable." onClick=\" wopen('".REQUESTROOT."/X/KNJXTRAIN/knjxtrainindex.php?SCHREGNO=".$model->schregno."&YEAR=".$model->year."&BUTTON_FLG=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["data"]["BTN_TSUGAKU"] = createBtn($objForm, "BTN_TSUGAKU", "通学情報", $extra);

        //保健情報
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        $extra = $disable." onClick=\" wopen('".REQUESTROOT."/X/KNJXHOKEN/knjxhokenindex.php?cmd=&SCHREGNO=".$model->schregno."&YEAR=".$model->year."&BUTTON_FLG=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["data"]["BTN_HOKEN"] = createBtn($objForm, "BTN_HOKEN", "保健情報", $extra);

        //部活情報
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        $extra = $disable." onClick=\" wopen('".REQUESTROOT."/X/KNJXCLUB_COMMITTEE/index.php?SCHREGNO=".$model->schregno."&YEAR=".$model->year."&HYOUJI_FLG=1&BUTTON_FLG=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["data"]["BTN_CLUB"] = createBtn($objForm, "BTN_CLUB", "部活情報", $extra);

        //委員会情報
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        $extra = $disable." onClick=\" wopen('".REQUESTROOT."/X/KNJXCLUB_COMMITTEE/index.php?SCHREGNO=".$model->schregno."&YEAR=".$model->year."&BUTTON_FLG=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["data"]["BTN_COMMITTEE"] = createBtn($objForm, "BTN_COMMITTEE", "委員会情報", $extra);

        //資格情報
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        $extra = $disable." onClick=\" wopen('".REQUESTROOT."/X/KNJXCLUB_COMMITTEE/index.php?SCHREGNO=".$model->schregno."&YEAR=".$model->year."&HYOUJI_FLG=2&BUTTON_FLG=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["data"]["BTN_SHIKAKU"] = createBtn($objForm, "BTN_SHIKAKU", "資格情報", $extra);

        //指導情報
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        $extra = $disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH303/knjh303index.php?SCHREGNO=".$model->schregno."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["data"]["BTN_TRAIN"] = createBtn($objForm, "BTN_TRAIN", "指導情報", $extra);

        //賞罰情報
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        $extra = $disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH302/knjh302index.php?SCHREGNO=".$model->schregno."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["data"]["BTN_DETAIL"] = createBtn($objForm, "BTN_DETAIL", "賞罰情報", $extra);

        if ($model->Properties["useBTN_PASSWORD"] == "1") {
            //パスワード情報
            $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
            $extra = $disable." onClick=\" wopen('".REQUESTROOT."/X/KNJXOTHERSYSTEM/knjxothersystemindex.php?SCHREGNO=".$model->schregno."&YEAR=".$model->year."&BUTTON_FLG=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["data"]["BTN_PASSWORD"] = createBtn($objForm, "BTN_PASSWORD", "パスワード情報", $extra);
        }
        if ($model->Properties["KNJH400_Pattern"] === '1') {
            $red = '';
            $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
            $extra = $red.$disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH400_SEITOKANKYOU/knjh400_SeitoKankyouindex.php?SCHREGNO=".$model->schregno."&YEAR=".$model->year."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["appendButton"]["Pattern3"] = createBtn($objForm, "Pattern3", "生徒環境", $extra);

            $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
            $extra = $red.$disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH400_BUKATU/knjh400_bukatuindex.php?SCHREGNO=".$model->schregno."&YEAR=".$model->year."&SEMESTER=".$model->semester."&GRADE=".$model->grade."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["data"]["BTN_CLUB"] = createBtn($objForm, "Pattern4", "部活情報", $extra);

            $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
            $extra = $red.$disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH400_SIKAKU/knjh400_sikakuindex.php?SCHREGNO=".$model->schregno."&YEAR=".$model->year."&SEMESTER=".$model->semester."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["data"]["BTN_SHIKAKU"] = createBtn($objForm, "Pattern5", "資格情報", $extra);

            $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
            $extra = $red.$disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH400_ZYUKOUKAMOKU/knjh400_zyukoukamokuindex.php?SCHREGNO=".$model->schregno."&YEAR=".$model->year."&SEMESTER=".$model->semester."&GRADE=".$model->grade."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["appendButton"]["Pattern6"] = createBtn($objForm, "Pattern6", "受講科目", $extra);
        }
    } elseif ($model->btnRadio == "2") {
        //定期考査情報
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF") ? "disabled" : "";
        $extra = $disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH310/knjh310index.php?SCHREGNO=".$model->schregno."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["data"]["BTN_TEST"] = createBtn($objForm, "BTN_TEST", "定期考査情報", $extra);
        //出欠情報
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF") ? "disabled" : "";
        $extra = $disable." onClick=\" wopen('".REQUESTROOT."/X/KNJXATTEND2/knjxattendindex.php?SCHREGNO=".$model->schregno."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["data"]["BTN_APPEND"] = createBtn($objForm, "BTN_APPEND", "出欠情報", $extra);
        //実力テスト情報
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF") ? "disabled" : "";
        $extra = $disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH320/knjh320index.php?SCHREGNO=".$model->schregno."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["data"]["BTN_SHAM"] = createBtn($objForm, "BTN_SHAM", "実力テスト情報", $extra);
        if ($model->Properties["KNJH400_Pattern"] === '1') {
            $red = '';
            $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
            $extra = $red.$disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH400_HYOUTEIHEIKIN/knjh400_hyouteiheikinindex.php?SCHREGNO=".$model->schregno."&YEAR=".$model->year."&SEMESTER=".$model->semester."&GRADE=".$model->grade."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["appendButton"]["Pattern7"] = createBtn($objForm, "Pattern7", "評定平均", $extra);

            $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
            $extra = $red.$disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH400_KEKKAZISUU/knjh400_kekkazisuuindex.php?SCHREGNO=".$model->schregno."&YEAR=".$model->year."&SEMESTER=".$model->semester."&GRADE=".$model->grade."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["appendButton"]["Pattern8"] = createBtn($objForm, "Pattern8", "欠課時数", $extra);
        }
    } elseif ($model->btnRadio == "3") {
        //志望校推移
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        $extra = $disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH400_SIBOU/knjh400_sibouindex.php?&SCHREGNO=".$model->schregno."&YEAR=".$model->year."&BUTTON_FLG=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["data"]["BTN_SHIBOU"] = createBtn($objForm, "BTN_SCHREG", "志望校推移", $extra);
        //判定推移
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        $extra = $disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH400_HANTEI/knjh400_hanteiindex.php?&SCHREGNO=".$model->schregno."&YEAR=".$model->year."&BUTTON_FLG=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["data"]["BTN_HANTEI"] = createBtn($objForm, "BTN_SCHREG", "判定推移", $extra);
        //成績推移
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        $extra = $disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH400_SEISEKI/knjh400_seisekiindex.php?&SCHREGNO=".$model->schregno."&YEAR=".$model->year."&BUTTON_FLG=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["data"]["BTN_SEISEKI"] = createBtn($objForm, "BTN_SCHREG", "成績推移", $extra);
        //教科バランス
        $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
        $extra = $disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH400_KYOUKA/knjh400_kyoukaindex.php?&SCHREGNO=".$model->schregno."&YEAR=".$model->year."&BUTTON_FLG=1','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
        $arg["data"]["BTN_KYOUKA"] = createBtn($objForm, "BTN_SCHREG", "教科間バランス", $extra);
    }
    if ($model->Properties["KNJH400_Pattern"] === '1') {
        $red = '';
        $extra = $red;
        if ($model->btnRadio == '4') {
            $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
            $extra = $red.$disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH400_SINGAKU/knjh400_singakuindex.php?SCHREGNO=".$model->schregno."&YEAR=".$model->year."&SEMESTER=".$model->semester."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["appendButton"]["Pattern9"] = createBtn($objForm, "Pattern9", "進学", $extra);

            $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
            $extra = $red.$disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH400_SYUUSYOKU/knjh400_syuusyokuindex.php?SCHREGNO=".$model->schregno."&YEAR=".$model->year."&SEMESTER=".$model->semester."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["appendButton"]["Pattern10"] = createBtn($objForm, "Pattern10", "就職", $extra);
        }
        if ($model->btnRadio == '5') {
            $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
            $extra = $red.$disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH400_SIDOUYOUROKU/knjh400_Sidouyourokuindex.php?SCHREGNO=".$model->schregno."&YEAR=".$model->year."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["appendButton"]["Pattern11"] = createBtn($objForm, "Pattern11", "指導要録(所見)", $extra);

            $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
            $extra = $red.$disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH400_TYOUSASYO_SYOKEN/knjh400_TyousasyoSyokenindex.php?SCHREGNO=".$model->schregno."&YEAR=".$model->year."&SEMESTER=".$model->semester."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["appendButton"]["Pattern12"] = createBtn($objForm, "Pattern12", "調査書(所見)", $extra);

            $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
            $extra = $red.$disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH400_TYOUSASYO_SYUUSYOKU/knjh400_TyousasyoSyuusyokuindex.php?SCHREGNO=".$model->schregno."&YEAR=".$model->yearr."&SEMESTER=".$model->semester."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["appendButton"]["Pattern13"] = createBtn($objForm, "Pattern13", "調査書(就職)", $extra);

            $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
            $extra = $red.$disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH400_TYOUSASYO_SEISEKI/knjh400_TyousasyoSeisekiindex.php?SCHREGNO=".$model->schregno."&YEAR=".$model->year."&SEMESTER=".$model->semester."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["appendButton"]["Pattern14"] = createBtn($objForm, "Pattern14", "調査書(成績)", $extra);

            $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
            $extra = $red.$disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH400_TYOUSASYO_KYUU/knjh400_TyousasyoKyuuindex.php?SCHREGNO=".$model->schregno."&YEAR=".$model->year."&SEMESTER=".$model->semester."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["appendButton"]["Pattern15"] = createBtn($objForm, "Pattern15", "調査書(旧)", $extra);
        }
        if ($model->btnRadio == '6') {
            $flg = false;
            $opt = array(array('label' => '', 'value' => ''));
            $result = $db->query(knjh400Query::getAllergies($model));
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                if ($row['CARE_DIV'] == '01' && $row['CARE_FLG'] == '1') {
                    $opt[] = array('label' => '気管支ぜん息' ,'value' => 'form1');
                }
                if ($row['CARE_DIV'] == '02' && $row['CARE_FLG'] == '1') {
                    $opt[] = array('label' => 'アトピー性皮膚炎' ,'value' => 'form2');
                }
                if ($row['CARE_DIV'] == '03' && $row['CARE_FLG'] == '1') {
                    $opt[] = array('label' => 'アレルギー性結膜炎' ,'value' => 'form3');
                }
                if (($row['CARE_DIV'] == '04' && $row['CARE_FLG'] == '1')||($row['CARE_DIV'] == '05' && $row['CARE_FLG'] == '1')) {
                    if (!$flg) {
                        $opt[] = array('label' => '食物アレルギー/アナフィラキシー' ,'value' => 'form4');
                        $flg = true;
                    }
                }
                if ($row['CARE_DIV'] == '06' && $row['CARE_FLG'] == '1') {
                    $opt[] = array('label' => 'アレルギー性鼻炎' ,'value' => 'form6');
                }
            }
            $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
            $extra = $red.$disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH400_HOKENSITU/knjh400_hokensituindex.php?SCHREGNO=".$model->schregno."&YEAR=".$model->year."&SEMESTER=".$model->semester."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["appendButton"]["Pattern16"] = createBtn($objForm, "Pattern16", "保健室利用記録", $extra);

            $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
            $extra = $red.$disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH400_IPPANKENSIN/knjh400_ippankensinindex.php?SCHREGNO=".$model->schregno."&YEAR=".$model->year."&SEMESTER=".$model->semester."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["appendButton"]["Pattern17"] = createBtn($objForm, "Pattern17", "一般検診", $extra);

            $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
            $extra = $red.$disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH400_HAKOUKUU/knjh400_hakoukuuindex.php?SCHREGNO=".$model->schregno."&YEAR=".$model->year."&SEMESTER=".$model->semester."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["appendButton"]["Pattern18"] = createBtn($objForm, "Pattern18", "歯・口腔", $extra);

            $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
            $extra = $red.$disable." onClick=\"if(document.forms[0].Pattern20.value!='') wopen('".REQUESTROOT."/H/KNJH400_ALLERGIES/knjh400_allergiesindex.php?SCHREGNO=".$model->schregno."&YEAR=".$model->year."&SEMESTER=".$model->semester."&cmd='+document.forms[0].Pattern20.value,'SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["appendButton"]["Pattern19"] = createBtn($objForm, "Pattern19", "アレルギー疾患", $extra);
            $extra = $red;
            $arg["appendButton"]["Pattern20"] = knjCreateCombo($objForm, "Pattern20", $model->Pattern20, $opt, " onchange=\"btn_submit('radio');\"".$extra, '');

            $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
            $extra = $red.$disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH400_KIOUSYOU_UNDOU/knjh400_KiousyouUndouindex.php?SCHREGNO=".$model->schregno."&YEAR=".$model->year."&SEMESTER=".$model->semester."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["appendButton"]["Pattern21"] = createBtn($objForm, "Pattern21", "既往症・運動", $extra);
        }
        if ($model->btnRadio == '7') {
            $disable = (AUTHORITY != DEF_UPDATABLE && $authAll == "OFF" && $authSome == "OFF") ? "disabled" : "";
            $extra = $red.$disable." onClick=\" wopen('".REQUESTROOT."/H/KNJH400_NYUUSI/knjh400_nyuusiindex.php?SCHREGNO=".$model->schregno."&YEAR=".$model->year."&SEMESTER=".$model->semester."','SUBWIN2',0,0,screen.availWidth,screen.availHeight);\"";
            $arg["appendButton"]["Pattern22"] = createBtn($objForm, "Pattern22", "入試情報", $extra);
        }
    }

    //追加
    $extra = "onclick=\"return btn_submit('insert');\"";
    $arg["button"]["btn_insert"] = createBtn($objForm, "btn_insert", "追 加", $extra);
    //削除
    $extra = "onclick=\"return btn_submit('delete');\"";
    $arg["button"]["btn_del"] = createBtn($objForm, "btn_del", "削 除", $extra);
    //終了
    $arg["button"]["btn_end"] = createBtn($objForm, "btn_end", "終 了", "onclick=\"closeWin();\"");
}

//Hidden作成
function makeHidden(&$objForm, $model)
{
    $objForm->ae(createHiddenAe("cmd"));
    $objForm->ae(createHiddenAe("SCHREGNO", $model->schregno));
    $objForm->ae(createHiddenAe("sort", $model->sort));
}

//ボタン作成
function createBtn(&$objForm, $name, $value, $extra)
{
    $objForm->ae(array("type"      => "button",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra));
    return $objForm->ge($name);
}

//チェックボックス作成
function createCheckBox(&$objForm, $name, $value, $extra, $multi)
{
    $objForm->ae(array("type"      => "checkbox",
                        "name"      => $name,
                        "value"     => $value,
                        "extrahtml" => $extra,
                        "multiple"  => $multi));

    return $objForm->ge($name);
}

//Hidden作成ae
function createHiddenAe($name, $value = "")
{
    $opt_hidden = array();
    $opt_hidden = array("type"      => "hidden",
                        "name"      => $name,
                        "value"     => $value);
    return $opt_hidden;
}
