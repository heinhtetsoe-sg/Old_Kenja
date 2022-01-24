<?php

require_once('for_php7.php');

class knjd425Form1 {

    function main(&$model) {

        //オブジェクト作成
        $objForm = new form;

        //フォーム作成
        $arg["start"] = $objForm->get_start("edit", "POST", "knjd425index.php", "", "edit");

        //DB接続
        $db = Query::dbCheckOut();

        //タイトル
        $row = loadGuidanceKindName($db, $model, "01");
        /* Edit by HPA for PC-talker 読み start 2020/02/03 */
          $arg["SNAME"] = $row[1];
          $arg["SNAME"] .= "の情報画面";
          $arg["TITLE"] = $row[1];
        /* Edit by HPA for PC-talker 読み end 2020/02/20 */
        //生徒情報
        $arg["SCHREGNO"] = $model->schregno;
        $arg["NAME"]     = $model->name;


        //警告メッセージを表示しない場合
        if ((isset($model->schregno) && !isset($model->warning)) || !isset($model->schregno)) {
            $arg["NOT_WARNING"] = 1;
        }

        // BTN_SUBFORMCALL : ボタン名称
        // KINDCD          : データ種別(2:1,4,9 3:2,3,5,6 4:10 5:7,8 <- ※sqlにて指定)
        // SCHREGNO        : 生徒情報
        // GHR             : 法廷/実クラス
        // KIND_NO         : DB登録タイプ

        //生徒が選択されてから表示する処理
        if ($model->schregno) {
            //useKnjd425DispUpdDateプロパティが立っているときのみ、日付を利用。
            if ($model->Properties["useKnjd425DispUpdDate"] == "1") {
                $arg["dispseldate"] = "1";
                //日付選択
                $arg["data"]["UPDTITLE"] = "更新日:&nbsp;";
                $query = knjd425Query::getUpdatedDateList($model);
                $extra = "onchange=\"btn_submit('edit');\"";
                $opt = array();
                $opt[] = array("label"=>"新規", "value"=>"9999/99/99");
                makeDateCmb($objForm, $arg, $db, $query, "UPDDATE", $model->upddate, $extra, 1, $opt);

                if ($model->upddate == "9999/99/99") {
                    $arg["newdate"] = "1";
                    $model->selnewdate = $model->selnewdate == "" ? str_replace("-", "/", CTRL_DATE) : $model->selnewdate;

                    $param = "extra=btn_submit(\'edit\');";
                    $arg["data"]["SELNEWDATE"] = View::popUpCalendar($objForm, "SELNEWDATE", str_replace("-", "/", $model->selnewdate), $param);
                } else {
                    $model->selnewdate = "";
                }
            } else {
                //固定日付で処理
                $model->selnewdate = "9999/03/31";
                $model->upddate = $model->selnewdate;
            }

            //可変ボタン作成
            /* Edit by HPA for current_cursor start 2020/02/03 */
            $result = loadGuidanceKindName($db, $model);
            $id = 0;
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $prmdatestr = "";
                if ($model->selnewdate != "") {
                    $prmdatestr = "&UPDDATE={$model->selnewdate}";
                } else if ($model->upddate != "") {
                    $prmdatestr = "&UPDDATE={$model->upddate}";
                }
                $id += 1; 
                $link = REQUESTROOT."/D/KNJD425_{$row["KINDCD"]}/knjd425_{$row["KINDCD"]}index.php?mode=1&SEND_PRGID=KNJD425&SEND_AUTH={$model->auth}&SEND_selectSchoolKind={$model->selectSchoolKind}&KINDNO={$row["KIND_NO"]}&SCHREGNO={$model->schregno}&EXP_YEAR={$model->exp_year}&EXP_SEMESTER={$model->exp_semester}&GRADE={$model->grade}&NAME={$model->name}".$prmdatestr;
                $extra = "id = \"$id\"style=\"height:22px;width:250px;background:#00FFFF;color:#000080;font:bold\" onclick=\"current_cursor('$id');if (chksetdate()) {document.location.href='$link'}\"";
                $row["BTN_SUBFORMCALL"] = KnjCreateBtn($objForm, "btn_subform1", $row["BTN_SUBFORMCALL"], $extra);
                /* Edit by HPA for current_cursor end 2020/02/20 */

                $arg["list"][] = $row;
            }
            knjCreateHidden($objForm, "HID_SELKINDNO");

            //障害種別、作成日、作成者を取得
            $remark01arry = array();
            $extra = "";
            if ($model->upddate != "9999/99/99") {
                $query = knjd425Query::getRemarkDiv01Data($model);
                $result = $db->query($query);
                while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    if ($row["SEQ"] == 1) {
                        $remark01arry["EDUCATIONCOURSE"] = $row["REMARK"];
                    } else if ($row["SEQ"] == 2) {
                        $remark01arry["CREATEDATE"] = $row["REMARK"];
                    } else if ($row["SEQ"] == 3) {
                        $remark01arry["CREATEUSER"] = $row["REMARK"];
                    }
                }
            }

            //障害情報を取得
            //B プロフィールデータ日付コンボ
            $extra = "";
            $query = knjd425Query::getTorikomiRecordDate($model, "B");
            makeCmb($objForm, $arg, $db, $query, "TORIKOMI_B_DATE", $torikomi_B_Date, $extra, 1);
            $diseasearry = array();
            $inspectarry = array();
            if ($torikomi_B_Date != "") {
                $query = knjd425Query::getSubQuery1($model, $torikomi_B_Date);
                $getR = $db->getRow($query, DB_FETCHMODE_ASSOC);
                $diseasearry["CHALLENGED_NAMES"] = $getR["CHALLENGED_NAMES"];
                $diseasearry["CHALLENGED_STATUS"] = $getR["CHALLENGED_STATUS"];

                $query = knjd425Query::getSubQuery2CheckRecordList($model, $torikomi_B_Date);
                $result = $db->query($query);
                $retCnt = 0;
                while ($rowlist = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                    $rowlist["RECORD_DIV_NAME"] = '検査機関'.($retCnt+1);
                    $rowlist["CHECK_DATE"] = str_replace("-", "/", $rowlist["CHECK_DATE"]);
                    $inspectarry[] = $rowlist;
                    $retCnt++;
                }
                $result->free();
            }

            //生徒情報設定
            $query = knjd425Query::getSchInfoShousai($model);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                //学部、年・組、氏名、性別
                $arg["SCHKIND_NAME"] = $row["GAKUBU_NAME"];
                $arg["GRADE_HR_NAME"] = ($row["GHR_NAME"] !== null && $row["GHR_NAME"] !== "") ? $row["GHR_NAME"] : $row["HR_NAME"] ;
                $arg["NAME_KANA"] = $row["NAME_KANA"];
                $arg["NAME"] = $row["NAME_SHOW"];
                $arg["SEX"] = $row["SEX_NAME"];

                //生年月日
                $dConvstr = explode("-", $row["BIRTHDAY"]);
                $dYear = Calc_SNameWareki($dConvstr[0], $dConvstr[1], $dConvstr[2]);
                $arg["BIRTHDAY"] = $dYear.".".$dConvstr[1].".".$dConvstr[2];

                //障害名、障害の概要
                $outbuf = str_replace(array("\n", "\r\n"), "<BR>", $diseasearry["CHALLENGED_NAMES"]);
                $arg["DISEASE_NAME"] = $outbuf;
                $outbuf = str_replace(array("\n", "\r\n"), "<BR>", $diseasearry["CHALLENGED_STATUS"]);
                $arg["DISEASE_OVERVIEW"] = $outbuf;

                //障害種別、作成日、作成者
                $arg["EDUCATIONCOURSE"] = $remark01arry["EDUCATIONCOURSE"];
                $arg["CREATEDATE"] = $remark01arry["CREATEDATE"];
                $arg["CREATEUSER"] = $remark01arry["CREATEUSER"];

                //検査日、検査機関、検査名
                if (get_count($inspectarry) > 0) {
                   $arg["CHECKUP_DATE1"] = $inspectarry[0]["CHECK_DATE"]; //$row[""];
                   $arg["CHECKUP_FACILITY1"] = $inspectarry[0]["CHECK_CENTER_TEXT"]; //$row[""];
                   $arg["CHECKUP_NAME1"] = $inspectarry[0]["CHECK_NAME"]; //$row[""];
                }
                if (get_count($inspectarry) > 1) {
                    $arg["CHECKUP_DATE2"] = $inspectarry[1]["CHECK_DATE"]; //$row[""];
                    $arg["CHECKUP_FACILITY2"] = $inspectarry[1]["CHECK_CENTER_TEXT"]; //$row[""];
                    $arg["CHECKUP_NAME2"] = $inspectarry[1]["CHECK_NAME"]; //$row[""];
                }
            }
        }

        //終了ボタン
        /* Edit by HPA for PC-talker 読み start 2020/02/03 */
        $extra = "aria-label = \"終了\" onclick=\"closeWin();\"";
        /* Edit by HPA for PC-talker 読み end 2020/02/20 */
        $arg["button"]["btn_end"] = KnjCreateBtn($objForm, "btn_end", "終 了", $extra);

        //hidden作成
        knjCreateHidden($objForm, "cmd");
        knjCreateHidden($objForm, "SCHREGNO", $model->schregno);
        knjCreateHidden($objForm, "PRGID", "KNJD425");
        knjCreateHidden($objForm, "DBNAME", DB_DATABASE);
        knjCreateHidden($objForm, "GRADE_HR_CLASS");
        knjCreateHidden($objForm, "USEKNJD425DISPUPDDATE", $model->Properties["useKnjd425DispUpdDate"]);

        $arg["IFRAME"] = VIEW::setIframeJs();

        //DB切断
        Query::dbCheckIn($db);

        //フォーム終わり
        $arg["finish"] = $objForm->get_finish();

        View::toHTML($model, "knjd425Form1.html", $arg);
    }
}

//コンボ作成
function makeCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $opt=array())
{
    $result = $db->query($query);
    $defValue = '';
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        $opt[] = array('label' => $row["LABEL"],
                       'value' => $row["VALUE"]);
        if ($row["DEF_VALUE_FLG"] == '1') {
            $defValue = $row["VALUE"];
        }
    }

    $result->free();
    if ($name == "SEMESTER") {
        $value = ($value == "" && $defValue) ? $defValue : ($value ? $value : $opt[0]["value"]);
    } else if ($name == "TORIKOMI_B_DATE") {
        $value = ($value == "") ? $opt[0]["value"] : $value;
        //初期値設定のみで、オブジェクト作成は不要。
        return;
    } else {
        $value = ($value == "") ? $opt[0]["value"] : $value;
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//日付コンボ作成
function makeDateCmb(&$objForm, &$arg, $db, $query, $name, &$value, $extra, $size, $opt=array())
{
    $result = $db->query($query);
    $defValue = '';
    while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
        array_unshift($opt, array('label' => str_replace("-", "/", $row["LABEL"]),
                       'value' => str_replace("-", "/", $row["VALUE"])));
        if ($row["DEF_VALUE_FLG"] == '1') {
            $defValue = str_replace("-", "/", $row["VALUE"]);
        }
    }

    $result->free();
    if ($name == "SEMESTER") {
        $value = ($value == "" && $defValue) ? $defValue : ($value ? $value : $opt[0]["value"]);
    } else {
        $value = ($value == "") ? $opt[0]["value"] : $value;
    }

    $arg["data"][$name] = knjCreateCombo($objForm, $name, $value, $opt, $extra, $size);
}

//項目名称取得
function loadGuidanceKindName($db, $model, $kno="")
{
    //データの優先度として、個人>年組>年度となる。データが無ければ下位SQLでデータを取得していく。
    $specifyschregflg = "";
    $query = knjd425Query::getGuidanceKindName($model, $specifyschregflg, $kno);
    $result = $db->getRow($query);
    if (get_count($result) > 0) {
        //if文を抜けて最後の処理を実施。
    } else {
        //データが無い時は学籍番号抜きで取り直し。
        $specifyschregflg = "1";
        $query = knjd425Query::getGuidanceKindName($model, $specifyschregflg, $kno);
        $result = $db->getRow($query);
        if (get_count($result) > 0) {
            //if文を抜けて最後の処理を実施。
        } else {
            //データが無い時は年組、学籍番号抜きで取り直し。
            $specifyschregflg = "2";
            $query = knjd425Query::getGuidanceKindName($model, $specifyschregflg, $kno);
            $result = $db->getRow($query);
        }
    }

    if ($kno != "") {
        return $result;
    } else {
        $query = knjd425Query::getGuidanceKindName($model, $specifyschregflg, $kno);
        $result = $db->query($query);
        return $result;
    }
}

//西暦をH99.00.00形式で変換
function Calc_SNameWareki(&$year, $month, $day)
{
    $border = array();

    $warekiList = array();
    $warekiList = common::getWarekiList();

    for ($i = 0; $i < get_count($warekiList); $i++) {
        $warekiInfo = $warekiList[$i];
        $start = str_replace("/", "", $warekiInfo['Start']);
        $end = str_replace("/", "", $warekiInfo['End']);
        $border[] = array("開始日" =>  $start, "終了日" => $end, "元号" => $warekiInfo['SName']);
    }

    $target = sprintf("%04d%02d%02d", $year, $month, $day);
    for ($i = 0; $border[$i]; $i++){
        if ($border[$i]["開始日"] <= $target &&
            $target <= $border[$i]["終了日"] ){
            $year = ($year - substr($border[$i]["開始日"], 0, 4) + 1);
            return $border[$i]["元号"] .(sprintf("%02d", (int) $year));
        }

    }
    return false;
}
?>
