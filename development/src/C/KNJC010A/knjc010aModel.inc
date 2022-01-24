<?php

require_once('for_php7.php');
class knjc010aModel extends Model
{
    public $cmd;
    public $control_data = array();        //コントロールのデータを保持
    public $ctrl_year;     //処理年度
    public $ctrl_semester; //処理学期
    public $field;
    public $trCount;
    public $weekArray = array();
    public $updDateArray = array();
    public $bunkatuSu;
    public $bunKatuField = array();
    public $attndCntlDt;    //出力制御日付  (制御日付外は管理者のみ)

    public function knjc010aModel()
    {
        common::GetControlMaster_Fnc($this->control_data);
        $this->field = array();
    }

    public function init()
    {
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        $this->cmd = VARS::request("cmd");
        $this->attndCntlDt   = ATTEND_CTRL_DATE;

        // var_dump($_GET);
        //メニューからの呼出時、変数初期化
        if (VARS::get("PROGRAMID")) {
            unset($this->param);
        }

        if (VARS::get("SEND_PRG")) {
            $this->param["staffcd"] = VARS::get("staffcd");
            $this->param["disp_staff"] = VARS::get("disp_staff");
            $this->param["syoribi"] = VARS::get("syoribi");
            $this->param["periodcd"] = VARS::get("periodcd");
            $this->param["chaircd"] = VARS::get("chaircd");
            $this->param["grade"] = VARS::get("grade");
            $this->param["hrclass"] = VARS::get("hrclass");
            $this->param["tr_cd1"] = VARS::get("tr_cd1");
            $this->param["SEND_PRG"] = VARS::get("SEND_PRG");
            $this->param["SEND_AUTH"] = VARS::get("SEND_AUTH");
            $this->param["SEND_XMENU"] = VARS::get("SEND_XMENU");
            $this->field = array();
            unset($this->hiddenSchreg);
        }
        $this->field["TITLE_EXECUTEDIV"] = VARS::post("TITLE_EXECUTEDIV");
        $this->field["TITLE_DISP_SEIGYO"] = VARS::post("TITLE_DISP_SEIGYO");
        $this->field["HR_REMARK"] = VARS::post("HR_REMARK");  //クラス備考

        //詳細
        if (VARS::get("SYOUSAI_SCHREGNO")) {
            $this->field["SYOUSAI_SCHREGNO"] = VARS::get("SYOUSAI_SCHREGNO");
            $this->syousaiHiddenSchreg = VARS::get("HIDDEN_SCHREG");
            $this->sendRenzoku = VARS::get("SEND_RENZOKU");
            $this->renZoku = explode(",", $this->sendRenzoku);
        }

        //詳細
        if (VARS::get("JUGYOU_NAIYOU_SYORIBI")) {
            $this->jugyouNaiyouSyoribi = VARS::get("JUGYOU_NAIYOU_SYORIBI");
            $this->jugyouNaiyouSchregno = VARS::get("JUGYOU_NAIYOU_SCHREGNO");
            $this->jugyouNaiyouPeriodcd = VARS::get("JUGYOU_NAIYOU_PERIODCD");
        }

        //時間割講座一覧
        if (VARS::request("SCH_CHR_EXECUTEDATE")) {
            $this->schChrExecuteDate = str_replace("/", "-", VARS::request("SCH_CHR_EXECUTEDATE"));
            $this->schChrPeriodCd    = VARS::post("SCH_CHR_PERIODCD");
            $this->schChrChairCd     = VARS::post("SCH_CHR_CHAIRCD");
            // KNJC020A からのパラメタを設定
            $this->param["syoribi"] = str_replace("/", "-", VARS::post("SCH_CHR_EXECUTEDATE"));
            $this->param["periodcd"] = VARS::post("SCH_CHR_PERIODCD");
            $this->param["chaircd"] = VARS::post("SCH_CHR_CHAIRCD");
            $this->param["staffcd"] = STAFFCD;
            $this->param["SEND_AUTH"] = AUTHORITY;
        }

        $this->field["REMARK"] = VARS::post("REMARK");

        $postDefSchDi = VARS::post("defSchDi");
        $this->defSchDi = array();
        if ($postDefSchDi) {
            $getDefSchDi = preg_split("{:}", $postDefSchDi);
            foreach ($getDefSchDi as $key => $val) {
                list($schPeri, $di) = preg_split("{@}", $val);
                $this->defSchDi[$schPeri] = $di;
            }
        }

        $postRenzokuPeri = VARS::post("renzokuPeri");
        $this->renzokuPeri = array();
        if ($postRenzokuPeri) {
            $getRenzokuPeri = preg_split("{:}", $postRenzokuPeri);
            foreach ($getRenzokuPeri as $key => $val) {
                list($soeji, $peri) = preg_split("{@}", $val);
                $this->renzokuPeri[$soeji] = $peri;
            }
        }
        $renzokuChairPeri = VARS::post("renzkChairPriod");
        if ($renzokuChairPeri) {
            // 連続している講座の校時を再設定
            $this->renzokuPeri = array();
            $getRenzokuPeri = preg_split("{,}", $renzokuChairPeri);
            for ($i=0; $i < get_count($getRenzokuPeri); $i++) {
                $this->renzokuPeri[$i] = $getRenzokuPeri[$i];
            }
        }

        $this->hiddenSchreg = VARS::post("HIDDEN_SCHREG");
        $this->hiddenChairCd = VARS::post("HIDDEN_CHAIRCD");

        $this->lastUpdate = VARS::post("LASTUPDATE");

        $this->popupinfoPeriod=VARS::get("POPUPINFO_PERIOD");
        $this->popupinfoExecuteDate=VARS::get("POPUPINFO_EXECUTEDATE");
        $this->popupinfoChaircd=VARS::get("POPUPINFO_CHAIRCD");
    }

    /* 入力項目チェック */
    public function validateInput()
    {
        $db = Query::dbCheckOut();
        /* 表示生徒の最新の最終更新日取得 */
        $query = knjc010aQuery::getLastUpdate($db, $this);
        $lastUpdate = $db->getOne($query);
        Query::dbCheckIn($db);

        if ($this->lastUpdate != $lastUpdate) {
            $this->setMessage("MSG307", "他の職員がデータを変更しています。入力をやり直してください。");
            return false;
        }

        //出力制御日付前のデータ変更不可
        if (!IS_KANRISYA && $this->schChrExecuteDate <= $this->attndCntlDt) {
            $this->setWarning("MSG300", "（ 出力制御日付: ".$this->attndCntlDt."以前のデータ ）");
            return false;
        }

        return true;
    }

    /* 更新処理 */
    public function &getUpdateModel()
    {
        if ($this->validateInput()) {
            $db = Query::dbCheckOut();
            $db->autoCommit(false);

            $schregData = preg_split("{,}", VARS::post("HIDDEN_SCHREG"));
            $this->updRenzokuPeriChair = array();
            $schregNoSql = "('' ";
            foreach ($schregData as $key => $schPeri) {
                list($schregNo, $periodCd) = preg_split("{_}", str_replace("SCH", "", $schPeri));
                $schregNoSql .= ", '".$schregNo."' ";
            }
            $schregNoSql .= ") ";
            $query = knjc010aQuery::getSchChrInfo($this, $schregNoSql);
            $result = $db->query($query);
            while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
                $chair[$row["SCHREGNO"]][$row["PERIODCD"]] = $row["CHAIRCD"];
            }
            foreach ($schregData as $key => $schPeri) {
                list($schregNo, $periodCd) = preg_split("{_}", str_replace("SCH", "", $schPeri));
                $diCd = VARS::post(str_replace("SCH", "UPD", $schPeri));
                $chairCd = $chair[$schregNo][$periodCd];

                $this->update($db, $schregNo, $periodCd, $chairCd, $diCd);
                if (is_array($this->renzokuPeri) && in_array($periodCd, $this->renzokuPeri)) {
                    $this->updRenzokuPeriChair[$periodCd."_".$chairCd] = $periodCd."_".$chairCd;
                }

                if ($this->param["SEND_PRG"] == "KNJC030A") {
                    if ($this->Properties["knjc030aInputSelectOnly"] == "1") {
                        if ($this->defSchDi[$schregNo."_".$periodCd] != $diCd) {
                            $this->updRenzokuPeriChair[$periodCd."_".$chairCd] = $periodCd."_".$chairCd;
                        }
                    } else {
                        $this->updRenzokuPeriChair[$periodCd."_".$chairCd] = $periodCd."_".$chairCd;
                    }
                }
            }
            $this->delAttend($db);

            $db->commit();
            Query::dbCheckIn($db);

            foreach ($this->updRenzokuPeriChair as $key => $val) {
                list($peri, $chairCd) = preg_split("{_}", $val);
                $this->updateLastExe($peri, $chairCd);
            }
            $this->setMessage("MSG201");
        }
        return;
    }

    /* 編集対象データを更新 */
    public function &update($db, $schregNo, $periodCd, $chairCd, $diCd)
    {
        $query = knjc010aQuery::getUpdateAttend($db, $this, $schregNo, $periodCd, $diCd, $chairCd);
        $db->query($query);

        return $chairCd;
    }

    /* 編集対象データを更新 */
    public function &updateLastExe($periodCd, $chairCd)
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjc010aQuery::getdelSchChrHrate($this, $periodCd, $chairCd);
        $db->query($query);
        $query = knjc010aQuery::getInsSchChrHrate($this, $periodCd, $chairCd);
        $db->query($query);
        $query = knjc010aQuery::getUpdSchChrHrate($this, $periodCd, $chairCd);
        $db->query($query);
        knjc010aQuery::getUpdSchChrExecuted($db, $this, $periodCd);
        $query = knjc010aQuery::getUpdSchChrExecutediv($db, $this, $periodCd, $chairCd);
        $db->query($query);
        knjc010aQuery::getUpdSchChrCountflg($db, $this, $periodCd, $chairCd);

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    /* 出欠のごみデータを削除 */
    public function &delAttend($db)
    {
        $query = knjc010aQuery::getDelAttend($this);
        $db->query($query);
    }

    // 勤怠変更
    public function &getSendKintai()
    {
        $db = Query::dbCheckOut();

        $json = "";
        $json_array = array();
        $schSep = "";
        $schregData = preg_split("{,}", VARS::post("HIDDEN_SCHREG"));
        $schregNos = "('";
        foreach ($schregData as $key => $schPeri) {
            list($schregNo, $periodCd) = preg_split("{_}", str_replace("SCH", "", $schPeri));
            $schregNos .= "', '".$schregNo;
        }
        $schregNos .= "')";
        // 時間割データ
        $schChrInfo = array();
        $query = knjc010aQuery::getSchChrInfo($this, $schregNos);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $schChrInfo[$row["SCHREGNO"]][$row["PERIODCD"]]["EXECUTED"] = $row["EXECUTED"];
            $schChrInfo[$row["SCHREGNO"]][$row["PERIODCD"]]["SUBCLASSABBV"] = $row["SUBCLASSABBV"];
            $schChrInfo[$row["SCHREGNO"]][$row["PERIODCD"]]["DI_CD"] = $row["DI_CD"];
            $schChrInfo[$row["SCHREGNO"]][$row["PERIODCD"]]["CHAIRNAME"] = $row["CHAIRNAME"];
        }

        // 仮出欠データ
        $petition = array();
        $query = knjc010aQuery::getPetition($this, $schregNos);
        $result = $db->query($query);
        while ($row = $result->fetchRow(DB_FETCHMODE_ASSOC)) {
            $petition[$row["SCHREGNO"]][$row["PERIODCD"]]["DI_CD"] = $row["DI_CD"];
            $petition[$row["SCHREGNO"]][$row["PERIODCD"]]["DI_REMARK"] = $row["DI_REMARK"];
            $petition[$row["SCHREGNO"]][$row["PERIODCD"]]["DI_NAME1"] = $row["DI_NAME1"];
            $petition[$row["SCHREGNO"]][$row["PERIODCD"]]["DI_MARK"] = $row["DI_MARK"];
        }
        foreach ($schregData as $key => $schPeri) {
            list($schregNo, $periodCd) = preg_split("{_}", str_replace("SCH", "", $schPeri));
            if ($this->field["TITLE_DISP_SEIGYO"] == "1" || $this->field["TITLE_DISP_SEIGYO"] == "2") {
                $query = knjc010aQuery::getAttend($this, $schregNo, $periodCd);
            } elseif ($this->field["TITLE_DISP_SEIGYO"] == "3") {
                $query = knjc010aQuery::getRemark($this, $schregNo, $periodCd);
            } elseif ($this->field["TITLE_DISP_SEIGYO"] == "4") {
                $query = knjc010aQuery::getSubclassAbbv($this, $schregNo, $periodCd);
            } else {
                $query = knjc010aQuery::getChairInfo($this, $schregNo, $periodCd);
            }
            $setAttend = $db->getRow($query, DB_FETCHMODE_ASSOC);

            if ($schChrInfo[$row["SCHREGNO"]][$row["PERIODCD"]]["DI_CD"] == '' && $schChrInfo[$schregNo][$periodCd]["EXECUTED"] != '1') {
                // 仮出欠データ読み込み
                if (is_array($petition[$schregNo]) && array_key_exists($periodCd, $petition[$schregNo])) {
                    if ($this->field["TITLE_DISP_SEIGYO"] == "1") {
                        $setAttend["LABEL"] = $petition[$schregNo][$periodCd]["DI_MARK"];
                    } elseif ($this->field["TITLE_DISP_SEIGYO"] == "2") {
                        $setAttend["LABEL"] = $petition[$schregNo][$periodCd]["DI_NAME1"];
                    } elseif ($this->field["TITLE_DISP_SEIGYO"] == "3") {
                        $setAttend["LABEL"] = $petition[$schregNo][$periodCd]["DI_REMARK"];
                    } elseif ($this->field["TITLE_DISP_SEIGYO"] == "4") {
                        $setAttend["LABEL"] = $schChrInfo[$schregNo][$periodCd]["SUBCLASSABBV"];
                    } elseif ($this->field["TITLE_DISP_SEIGYO"] == "5") {
                        $setAttend["LABEL"] = $schChrInfo[$schregNo][$periodCd]["CHAIRNAME"];
                    }
                }
            }

            //備考が長い場合は、カットする。(全角6文字まで)
            if ($setAttend["LABEL"]) {
                $atLen = mb_strlen($setAttend["LABEL"]);
                $setAttendBef = $setAttend["LABEL"];
                $setAttendAft = "";
                $setAttend["LABEL"] = "";
                for ($atCnt = 0; $atCnt < $atLen; $atCnt++) {
                    $setAttendAft .= mb_substr($setAttendBef, $atCnt, 1, "UTF-8");
                    if (strlen($setAttendAft) > 18) {
                        break;
                    } else {
                        $setAttend["LABEL"] .= mb_substr($setAttendBef, $atCnt, 1, "UTF-8");
                    }
                }
            }
            $json_array[] = "'{$schPeri}':'{$setAttend["LABEL"]}'";
            $schSep = ",";
        }
        $json = implode(',', $json_array);
        if ($json) {
            $query = "\".$query.\"";
            echo "{ 'result':true,{$json} }";
        } else {
            echo "{ 'result':false }";
        }
        Query::dbCheckIn($db);
    }

    // 講座変更
    public function &getSendChair()
    {
        $db = Query::dbCheckOut();

        $json = "";
        $json_array = array();
        $schSep = "";
        $schregData = preg_split("{,}", VARS::post("HIDDEN_SCHREG"));
        $selectSchregNo = VARS::post("SELECT_SCHREGNO");
        $query = knjc010aQuery::getChairTitleDiv();
        $titleDiv = $db->getOne($query);
        foreach ($schregData as $key => $schPeri) {
            list($schregNo, $periodCd) = preg_split("{_}", str_replace("SCH", "", $schPeri));
            if ($schregNo == $selectSchregNo) {
                $query = knjc010aQuery::getChairTitleInfo($this, $schregNo, $periodCd, $titleDiv);
                $setChairName = $db->getRow($query, DB_FETCHMODE_ASSOC);
                if ($setChairName["LABEL"]) {
                    $setLabel = $setChairName["LABEL"];
                } else {
                    $setLabel = "-";
                }
                $titlePeri = "PERIOD".$periodCd;
                $json_array[] = "'{$titlePeri}':'{$setLabel}'";

                $query = knjc010aQuery::getCountSchZumi($this, $setChairName["CHAIRCD"], $periodCd);
                $countSchZumi = $db->getOne($query);
                $query = knjc010aQuery::getCountHrZumi($this, $setChairName["CHAIRCD"], $periodCd);
                $countHrZumi = $db->getOne($query);
                $setAttend = "";
                $attendPeri = "ATTEND".$periodCd;
                if ($setChairName["CHAIRCD"]) {
                    if ($countSchZumi > 0) {
                        $setAttend = "<font color = \"blue\">出欠済</font>";
                    } elseif ($countHrZumi > 0) {
                        $setAttend = "<font color = \"yellow\">出欠中</font>";
                    } else {
                        $setAttend = "<font color = \"red\">未出欠</font>";
                    }
                } else {
                    $setAttend = "-";
                }
                $json_array[] = "'{$attendPeri}':'{$setAttend}'";

                $schSep = ",";
            }
        }
        $json = implode(',', $json_array);

        if ($json) {
            echo "{ 'result':true,{$json} }";
        } else {
            echo "{ 'result':false }";
        }
        Query::dbCheckIn($db);
    }

    // 入力講座情報
    public function &getInputChair()
    {
        $db = Query::dbCheckOut();

        $json_array = array();
        $schregNO = VARS::post("SELECT_SCHREGNO");
        $date = VARS::post("SELECT_DATE");
        $periodcd = VARS::post("SELECT_PERIODCD");
        $query = knjc010aQuery::getInputCharInfo($schregNO, $date, $periodcd);
        $row = $db->getRow($query, DB_FETCHMODE_ASSOC);
        if (is_array($row)) {
            $json_array[] = " \"INPUT_CHAIR_NAME\"   : \"".$row["CHAIRNAME"]."\" ";
            $json_array[] = " \"INPUT_CHAIR_STAFF\"  : \"".$row["STAFFNAME"]."\" ";
            $json_array[] = " \"INPUT_CHAIR_CREDIT\" : \"".$row["CREDITS"]."\" ";
            $json_array[] = " \"INPUT_UPDATE_STAFF\" : \"".$row["UPDATE_STAFFNAME"]."\" ";
            $json_array[] = " \"INPUT_UPDATE_TIME\"  : \"".$row["UPDATE_TIME"]."\" ";
        }

        $json = implode(',', $json_array);

        if ($json) {
            echo "{ \"result\":true,{$json} }";
        } else {
            echo "{ \"result\":false }";
        }
        Query::dbCheckIn($db);
    }


    /* 詳細更新処理 */
    public function &getUpdateModelSyouSai()
    {
        $updArray = array();
        $schregs = preg_split("{,}", $this->syousaiHiddenSchreg);
        foreach ($schregs as $schPeri) {
            list($schregNo, $period) = preg_split("{_}", str_replace("SCH", "", $schPeri));
            if (!in_array($period, $this->renZoku)) {
                continue;
            }
            if ($this->field["SYOUSAI_SCHREGNO"] == $schregNo) {
                if (strlen(VARS::post("SYOUSAI_REMARK_{$period}")) > 60) {
                    $this->setWarning("MSG901", "( 備考 ) 60バイトまで");
                    return false;
                } else {
                    $updArray[] = array("SCHREGNO" => $schregNo,
                                        "PERI" => $period,
                                        "REMARKCD" => VARS::post("SYOUSAI_REMARK_CD_{$period}"),
                                        "REMARK" => VARS::post("SYOUSAI_REMARK_{$period}"));
                }
            }
        }

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        foreach ($updArray as $updData) {
            $query = knjc010aQuery::updSyouSai($db, $this, $updData);
            $db->query($query);
        }

        $this->setMessage("MSG201");

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }

    /* 詳細更新処理 */
    public function &getUpdateModelJugyouNaiyouAdd()
    {
        $updRemark = VARS::post("REMARK");

        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $query = knjc010aQuery::updJugyouNaiyou($db, $this, $updRemark);
        $db->query($query);

        $this->setMessage("MSG201");

        $db->commit();
        Query::dbCheckIn($db);
        return;
    }
}
