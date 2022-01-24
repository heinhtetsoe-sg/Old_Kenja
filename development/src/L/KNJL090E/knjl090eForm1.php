<?php

require_once('for_php7.php');

class knjl090eForm1 {
    function main(&$model) {
        $objForm      = new form;

        //一覧表示
        if ((!isset($model->warning)) && (!is_array($existdata))) {
            //データを取得
            $Row = knjl090eQuery::get_edit_data($model);
            if ($model->cmd == 'back' || $model->cmd == 'next' || $model->cmd == 'back1' || $model->cmd == 'next1') {
                if (!is_array($Row)) {
                    if ($model->cmd == 'back' || $model->cmd == 'next') {
                        $model->setWarning("MSG303","更新しましたが、移動先のデータが存在しません。");
                    }
                    $model->cmd = "main";
                    $Row = knjl090eQuery::get_edit_data($model);
                }
                $model->examno  = $Row["EXAMNO"];
            }
            $disabled = "";
            if (!is_array($Row)) {
                $disabled = "disabled";
                if ($model->cmd == 'reference') {
                    $Row["APPLICANTDIV"] = $model->field["APPLICANTDIV"];
                    $Row["TESTDIV"] = $model->field["TESTDIV"];
                    $model->setWarning("MSG303");
                }
            }
        } else {
            $Row =& $model->field;
        }

        if ($model->cmd == 'changeTest' || $model->cmd == 'main') {
            $Row["APPLICANTDIV"] = $model->field["APPLICANTDIV"];
            $Row["TESTDIV"]      = $model->field["TESTDIV"];
        }

        if (isset($Row["EXAMNO"])) {
            $model->checkexam = $Row["EXAMNO"];
        }

        //DB接続
        $db = Query::dbCheckOut();

        //年度
        $arg["TOP"]["YEAR"] = $model->year;

        //入試制度
        $query = knjl090eQuery::get_name_cd($model->year, "L003");
        $extra = "onChange=\"change_flg(); return btn_submit('changeTest');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["APPLICANTDIV"], "APPLICANTDIV", $extra, 1, "");

        //入試区分
        $query = knjl090eQuery::get_name_cd($model->year, "L004");
        $extra = "onChange=\"change_flg(); return btn_submit('changeTest');\"";
        makeCmb($objForm, $arg, $db, $query, $Row["TESTDIV"], "TESTDIV", $extra, 1, "");

        //音楽フラグ
        $model->musicFlg = false;
        $nameCdL004Row = $db->getRow(knjl090eQuery::get_name_cd($model->year, "L004", $Row["TESTDIV"]), DB_FETCHMODE_ASSOC);
        if ($nameCdL004Row["NAMESPARE3"] == "2") $model->musicFlg  = true;
        if ($model->musicFlg) {
            $arg['MUSIC'] = '1';
        } else {
            $arg['MUSIC'] = '';
        }

        //受験番号
        $extra = " STYLE=\"ime-mode: inactive\" onChange=\"btn_disabled();\" onblur=\"this.value=toAlphaNumber(this.value);\"";
        $arg["data"]["EXAMNO"] = knjCreateTextBox($objForm, $model->examno, "EXAMNO", 7, 7, $extra);

        //名称セット
        $nameCd1In  = "('L004', 'L006', 'L013', 'L016', 'L045', 'L058', 'Z002')";
        $setNameArr = array();
        $query = knjl090eQuery::getNameMstList($model->year, $nameCd1In);
        $result = $db->query($query);
        while ($nameRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $setNameArr[$nameRow["NAMECD1"]][$nameRow["NAMECD2"]] = $nameRow["LABEL"];
        }

        //受験区分
        $arg["data"]["TESTDIV1"] = $setNameArr['L045'][$Row["TESTDIV1"]];
        knjCreateHidden($objForm, "TESTDIV1", $Row["TESTDIV1"]);

        //出願コース
        $arg["data"]["DESIREDIV"] = $setNameArr['L058'][$Row["DESIREDIV"]];
        knjCreateHidden($objForm, "DESIREDIV", $Row["DESIREDIV"]);

        //ログインID
        $arg["data"]["LOGIN_ID"] = $Row["LOGIN_ID"];
        knjCreateHidden($objForm, "LOGIN_ID", $Row["LOGIN_ID"]);

        //------------------------------志願者情報-------------------------------------
        //氏名(志願者)
        $arg["data"]["NAME"] = $Row["NAME"];
        knjCreateHidden($objForm, "NAME", $Row["NAME"]);

        //氏名カナ(志願者)
        $arg["data"]["NAME_KANA"] = $Row["NAME_KANA"];
        knjCreateHidden($objForm, "NAME_KANA", $Row["NAME_KANA"]);

        //性別(志願者)
        $arg["data"]["SEX"] = $setNameArr['Z002'][$Row["SEX"]];
        knjCreateHidden($objForm, "SEX", $Row["SEX"]);

        //生年月日
        $arg["data"]["BIRTHDAY"] = str_replace('-', '/', $Row["BIRTHDAY"]);
        knjCreateHidden($objForm, "BIRTHDAY", $Row["BIRTHDAY"]);

        //出身校
        $query = knjl090eQuery::getFinschoolName($Row["FINSCHOOLCD"]);
        $fsArray = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["FINSCHOOLNAME"] = $fsArray["FINSCHOOL_NAME"];
        knjCreateHidden($objForm, "FINSCHOOLCD", $Row["FINSCHOOLCD"]);

        //卒業年月
        $arg["data"]["FS_DAY"] = str_replace('-', '/', $Row["FS_DAY"]);
        knjCreateHidden($objForm, "FS_DAY", $Row["FS_DAY"]);

        //卒業区分
        $arg["data"]["FS_GRDDIV"] = $setNameArr['L016'][$Row["FS_GRDDIV"]];
        knjCreateHidden($objForm, "FS_GRDDIV", $Row["FS_GRDDIV"]);

        //得点情報セット
        $scoreInfo = array();
        $query = knjl090eQuery::getScoredata($model, $Row["LOGIN_ID"]);
        $result = $db->query($query);
        while ($scoreRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $scoreInfo[$scoreRow['RECEPTNO']][$scoreRow['TESTSUBCLASSCD']] = $scoreRow;
        }

        //得点情報セット
        $model->subClassArr = array();
        $query = knjl090eQuery::getSubclass($model->year, "L009");
        $result = $db->query($query);
        while ($subRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $model->subClassArr[$subRow["VALUE"]] = $subRow["LABEL"];

            //名称セット
            $arg['data']['TESTSUBCLASSCD'.$subRow["VALUE"]] = $subRow["LABEL"];
        }

        //併願状況
        $shCnt = '1';
        $query = knjl090eQuery::getHeiganData($model, $Row["LOGIN_ID"]);
        $result = $db->query($query);
        while ($shRow = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            //入試区分
            $arg["data"]["H_TESTDIV_".$shCnt] = $setNameArr['L004'][$shRow["TESTDIV"]];

            //受験番号
            $arg["data"]["H_EXAMNO_".$shCnt] = $shRow["EXAMNO"];

            //受験区分
            $arg["data"]["H_TESTDIV1_".$shCnt] = $setNameArr['L045'][$shRow["TESTDIV1"]];

            //出願コース
            $arg["data"]["H_DESIREDIV_".$shCnt] = $setNameArr['L058'][$shRow["DESIREDIV"]];

            //合格区分
            $arg["data"]["H_PASS_DIV_".$shCnt] = $setNameArr['L045'][$shRow["PASS_DIV"]];

            //合格コース
            $arg["data"]["H_PASS_COURSE_".$shCnt] = $setNameArr['L058'][$shRow["PASS_COURSE"]];

            //合否
            $arg["data"]["H_JUDGEMENT_".$shCnt] = $setNameArr['L013'][$shRow["JUDGEMENT"]];

            //入学コース
            if ($shRow["PASS_FLG"] == '1') { // 合格のデータだけ
                $query = knjl090eQuery::get_name_cdL062($model->year, $shRow["PASS_COURSE"]);
                $extra = "onChange=\"change_flg();\"";
                $shRow["ENT_COURSE"] = (isset($model->warning)) ? $model->field["H_ENT_COURSE_".$shCnt]: $shRow["ENT_COURSE"];
                makeCmb($objForm, $arg, $db, $query, $shRow["ENT_COURSE"], "H_ENT_COURSE_".$shCnt, $extra, 1, "BLANK");
            }

            //出願専攻
            if ($shRow["NAMESPARE3"] == '2') {
                $setHope = '';
                if ($shRow["HOPE"] == '1') {
                    $setHope = '1:'.$shRow["HOPE1"];
                } else if ($shRow["HOPE"] == '2') {
                    $setHope = '2:'.$shRow["HOPE2"];
                } else if ($shRow["HOPE"] == '9') {
                    $setHope = '9:声楽';
                }
                $arg["data"]["H_HOPE_".$shCnt] = $setHope;
            }

            //得点セット
            if ($scoreInfo[$shRow["EXAMNO"]] != '') {
                foreach ($model->subClassArr as $nameCd2 => $abbv) {
                    $arg['data']['SCORE'.$nameCd2.'_'.$shCnt] = $scoreInfo[$shRow["EXAMNO"]][$nameCd2]['SCORE'];
                    $arg['data']['TOTAL2'.'_'.$shCnt]         = $scoreInfo[$shRow["EXAMNO"]][$nameCd2]['TOTAL2'];
                    $arg['data']['TOTAL3'.'_'.$shCnt]         = $scoreInfo[$shRow["EXAMNO"]][$nameCd2]['TOTAL3'];
                    $arg['data']['TOTAL_RANK3'.'_'.$shCnt]    = $scoreInfo[$shRow["EXAMNO"]][$nameCd2]['TOTAL_RANK3'];
                }
            }

            $shCnt++;
        }

        //-------------------------------- ボタン作成 ------------------------------------
        global $sess;
        $gzip = $Row["GZIPCD"];
        $gadd = $Row["GADDRESS1"];

        //検索ボタン（受験番号）
        $extra = "onclick=\"return btn_submit('reference', '".$gzip."', '".$gadd."');\"";
        $arg["button"]["btn_reference"] = knjCreateBtn($objForm, "btn_reference", "検 索", $extra);

        //カナ検索ボタン
        $extra = "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL090E/search_name.php?cmd=search&year='+document.forms[0]['year'].value+'&applicantdiv='+document.forms[0]['APPLICANTDIV'].value+'&testdiv='+document.forms[0]['TESTDIV'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
        $arg["button"]["btn_kana_reference"] = knjCreateBtn($objForm, "btn_kana_reference", "カナ検索", $extra);

        //前の志願者検索ボタン
        $extra = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onClick=\"btn_submit('back1', '".$gzip."', '".$gadd."');\"";
        $arg["button"]["btn_back_next"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);

        //次の志願者検索ボタン
        $extra = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onClick=\"btn_submit('next1', '".$gzip."', '".$gadd."');\"";
        $arg["button"]["btn_back_next"] .= knjCreateBtn($objForm, "btn_next", " >> ", $extra);

        //更新ボタン
        $extra = "$disabled onclick=\"return btn_submit('update');\"";
        $arg["button"]["btn_udpate"] = knjCreateBtn($objForm, "btn_udpate", "更 新", $extra);
        //更新ボタン(更新後前の志願者)
        $extra = "$disabled style=\"width:150px\" onclick=\"return btn_submit('back');\"";
        $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_pre", "更新後前の志願者", $extra);
        //更新ボタン(更新後次の志願者)
        $extra = "$disabled style=\"width:150px\" onclick=\"return btn_submit('next');\"";
        $arg["button"]["btn_up_next"] .= knjCreateBtn($objForm, "btn_up_next", "更新後次の志願者", $extra);
        //取消ボタン
        $extra = "onclick=\"return btn_submit('reset');\"";
        $arg["button"]["btn_reset"] = knjCreateBtn($objForm, "btn_reset", "取 消", $extra);
        //終了ボタン
        $extra = "onclick=\"closeWin();\"";
        $arg["button"]["btn_end"] = knjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "year", $model->year);
        knjCreateHidden($objForm, "CTRL_YEAR", CTRL_YEAR);
        knjCreateHidden($objForm, "CTRL_SEMESTER", CTRL_SEMESTER);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        $arg["start"] = $objForm->get_start("main", "POST", "knjl090eindex.php", "", "main");
        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl090eForm1.html", $arg);
    }
}
//makeCmb
function makeCmb(&$objForm, &$arg, $db, $query, &$value, $name, $extra, $size, $blank = "") {
    $opt = array();
    if ($blank == "BLANK") {
        $opt[] = array("label" => "", "value" => "");
    }
    $result = $db->query($query);
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);

        if ($name == 'SEX') {
            if ($value == "" && $row["NAMESPARE2"] == '1') $value = $row["VALUE"];
        }
        if ($value === $row["VALUE"]) $value_flg = true;
    }
    $value = ($value != '' && $value_flg) ? $value : $opt[0]['value'];
    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);

    $result->free();
}
?>