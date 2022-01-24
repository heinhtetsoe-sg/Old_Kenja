<?php
class knjl511iForm1
{
    public function main(&$model)
    {
        $objForm = new form();
        $arg["start"] = $objForm->get_start("edit", "POST", "knjl511iindex.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //データを取得
        $query = knjl511iQuery::getSelectQuery($model, $model->examno);
        $Row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if ($model->cmd == 'back1' || $model->cmd == 'next1') {
            if (!is_array($Row)) {
                if ($model->cmd == 'back1' || $model->cmd == 'next1') {
                    $model->setWarning("MSG303", "更新しましたが、移動先のデータが存在しません。");
                }
                $model->cmd = "main";
                $query = knjl511iQuery::getSelectQuery($model, $model->examno);
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

        $disabled = "disabled";

        //入試年度
        $arg["TOP"]["YEAR"] = $model->year;

        //入試制度
        $applicantRow = $db->getRow(knjl511iQuery::getNameMst($model, "L003", $model->applicantdiv), DB_FETCHMODE_ASSOC);
        $arg["data"]["APPLICANTDIV"] = $applicantRow["LABEL"];

        //学科
        $extra = "";
        $opt = array();
        $opt[] = array("label" => "", "value" => "");
        $opt[] = array("label" => "1:普通科", "value" => "1");
        $opt[] = array("label" => "2:工業科", "value" => "2");
        $arg["data"]["TESTDIV0"] = knjCreateCombo($objForm, "TESTDIV0", $Row["TESTDIV0"], $opt, $extra, $size);

        //入試区分
        $query = knjl511iQuery::getTestdivMst($model);
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
        $arg["data"]["NAME"] = $Row["NAME"];

        //氏名かな
        $arg["data"]["NAME_KANA"] = $Row["NAME_KANA"];

        //生年月日
        $arg["data"]["BIRTHDAY"] = View::popUpCalendarAlp($objForm, "BIRTHDAY", str_replace("-", "/", $Row["BIRTHDAY"]), $disabled, "");

        //性別
        $extra = $disabled;
        $query = knjl511iQuery::getNameMst($model, "Z002");
        makeCmb($objForm, $arg, $db, $query, $Row["SEX"], "SEX", $extra, 1, "BLANK");

        //過年度チェックボックス
        $Row["SEQ031_REMARK5"] = ($Row["SEQ031_REMARK5"] == "1") ? "有り" : "無";
        $arg["data"]["SEQ031_REMARK5"] = $Row["SEQ031_REMARK5"];

        //出身学校コード
        $arg["data"]["FINSCHOOLCD"] = $Row["FINSCHOOLCD"];

        //学校名
        $query = knjl511iQuery::getFinschoolName($Row["FINSCHOOLCD"]);
        $fsArray = $db->getRow($query, DB_FETCHMODE_ASSOC);
        $arg["data"]["FINSCHOOLNAME"] = $fsArray["FINSCHOOL_NAME"]."　".$fsArray["FINSCHOOL_DISTCD_NAME"];

        //卒業年度
        $arg["data"]["FS_GRDYEAR"] = $Row["FS_GRDYEAR"];

        //保護者氏名
        $arg["data"]["GNAME"] = $Row["GNAME"];

        //保護者氏名かな
        $arg["data"]["GKANA"] = $Row["GKANA"];

        //郵便番号
        global $sess;
        $arg["data"]["GZIPCD"] = $Row["GZIPCD"];

        //住所1
        $arg["data"]["GADDRESS1"] = $Row["GADDRESS1"];

        //住所2
        $arg["data"]["GADDRESS2"] = $Row["GADDRESS2"];

        //電話番号
        $arg["data"]["GTELNO"] = $Row["GTELNO"];

        //志望コース
        $extra = $disabled;
        $query = knjl511iQuery::getGeneralMst($model, "02");
        for ($idx = 1; $idx <= $model->maxHopeCourseNum; $idx++) {
            makeCmb($objForm, $arg, $db, $query, $Row["HOPE_COURSE".$idx], "HOPE_COURSE".$idx, $extra, 1, "BLANK");
        }

        //奨学希望チェックボックス
        $Row["DORMITORY_FLG"] = ($Row["DORMITORY_FLG"] == "1") ? "有り" : "無";
        $arg["data"]["DORMITORY_FLG"] = $Row["DORMITORY_FLG"];

        //入寮希望チェックボックス
        $Row["SEQ031_REMARK6"] = ($Row["SEQ031_REMARK6"] == "1") ? "有り" : "無";
        $arg["data"]["SEQ031_REMARK6"] = $Row["SEQ031_REMARK6"];

        //特待コード
        if ($model->cmd == "updEdit") {
            $Row["CON009_REMARK2"] = $model->field["CON009_REMARK2"];
        }
        $extra = "";
        $query = knjl511iQuery::getGeneralMst($model, "04");
        makeCmb($objForm, $arg, $db, $query, $Row["CON009_REMARK2"], "CON009_REMARK2", $extra, 1, "BLANK");

        //特待理由コード
        if ($model->cmd == "updEdit") {
            $Row["CON009_REMARK3"] = $model->field["CON009_REMARK3"];
        }
        $extra = "";
        $query = knjl511iQuery::getGeneralMst($model, "05");
        makeCmb($objForm, $arg, $db, $query, $Row["CON009_REMARK3"], "CON009_REMARK3", $extra, 1, "BLANK");

        //相談コース
        $extra = $disabled;
        $query = knjl511iQuery::getGeneralMst($model, "02");

        makeCmb($objForm, $arg, $db, $query, $Row["CON009_REMARK1"], "CON009_REMARK1", $extra, 1, "BLANK");

        //共通テスト
        $arg["data"]["CON004_REMARK1"] = $Row["CON004_REMARK1"];

        //欠席理由書No.
        $arg["data"]["CON004_REMARK2"] = $Row["CON004_REMARK2"];

        //部活動
        $extra = $disabled;
        $query = knjl511iQuery::getClubMst();
        makeCmb($objForm, $arg, $db, $query, $Row["CON005_REMARK5"], "CON005_REMARK5", $extra, 1, "BLANK");

        //ボタン作成
        makeBtn($objForm, $arg, $model, $sess);

        //hidden作成
        makeHidden($objForm, $model);

        //インラインフレーム用Javascriptタグ生成
        $arg["IFRAME"] = View::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        $arg["finish"]  = $objForm->get_finish();
        View::toHTML($model, "knjl511iForm1.html", $arg);
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

    //かな検索ボタン
    $extra = "style=\"width:80px\" onclick=\"loadwindow('" .REQUESTROOT ."/L/KNJL511I/search_name.php?cmd=search&year={$model->year}&applicantdiv={$model->applicantdiv}&testdiv0='+document.forms[0]['TESTDIV0'].value+'&testdiv='+document.forms[0]['TESTDIV'].value+'&examno='+document.forms[0]['EXAMNO'].value+'&NAME_SESSID={$sess->id}&frame='+getFrameName(self), event.clientX + function () {var scrollX = document.documentElement.scrollLeft || document.body.scrollLeft;return scrollX;}(), event.clientY + function () {var scrollY = document.documentElement.scrollTop || document.body.scrollTop;return scrollY;}(), 320, 260)\"";
    $arg["button"]["btn_kana_reference"] = knjCreateBtn($objForm, "btn_kana_reference", "かな検索", $extra);

    //前の志願者検索ボタン
    $extra = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onClick=\"btn_submit('back1');\"";
    $arg["button"]["btn_back_next"] = knjCreateBtn($objForm, "btn_back", " << ", $extra);

    //次の志願者検索ボタン
    $extra = "style=\"width:32px; padding-left:0px; padding-right:0px;\" onClick=\"btn_submit('next1');\"";
    $arg["button"]["btn_back_next"] .= knjCreateBtn($objForm, "btn_next", " >> ", $extra);

    //更新ボタン
    $extra = "onclick=\"return btn_submit('update');\"";
    $arg["button"]["btn_update"] = knjCreateBtn($objForm, "btn_update", "更 新", $extra);

    //更新ボタン(更新後前の志願者)
    $extra = "onclick=\"return btn_submit('back');\"";
    $arg["button"]["btn_up_next"] = knjCreateBtn($objForm, "btn_up_pre", "更新後前の志願者", $extra);

    //更新ボタン(更新後次の志願者)
    $extra = "onclick=\"return btn_submit('next');\"";
    $arg["button"]["btn_up_next"] .= knjCreateBtn($objForm, "btn_up_next", "更新後次の志願者", $extra);

    //終了ボタン
    $extra = "onclick=\"closeWin();\"";
    $arg["button"]["btn_back"] = knjCreateBtn($objForm, "btn_back", "終 了", $extra);
}

function makeHidden(&$objForm, $model)
{
    knjCreateHidden($objForm, "cmd");
}
