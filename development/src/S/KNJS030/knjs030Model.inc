<?php

require_once('for_php7.php');

class knjs030Model extends Model {

    var $cmd;
    var $control_data = array();        //コントロールのデータを保持
    var $ctrl_year;     //処理年度
    var $ctrl_semester; //処理学期
    var $field;
    var $trCount;
    var $weekArray = array();
    var $updDateArray = array();
    var $bunkatuSu;
    var $bunKatuField = array();

    function knjs030Model()
    {
        common::GetControlMaster_Fnc($this->control_data);
        $this->field = array();
    }

    function init()
    {
        //プロパティファイルの読込み
        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("PROGRAMID")) {
            $this->field = array();
            unset($this->trCount);
        }
        $this->cmd = VARS::request("cmd");

        $this->field["HR_CLASS_DIV"] = "1";
        if ($this->cmd != "bunkatu" && $this->cmd != "subEnd" && $this->cmd != "bunUpd") {
            $this->field["HR_OR_STAFF"] = VARS::post("HR_OR_STAFF");
            $this->field["HR_CLASS"] = VARS::post("HR_CLASS");
            $this->field["STAFF"] = VARS::post("STAFF");
            $this->field["DISP_SHOW"] = VARS::post("DISP_SHOW");
            $this->field["JISU_SHOW"] = VARS::post("JISU_SHOW");
            $this->field["MONTH"] = VARS::post("MONTH");
            $this->field["WEEK_SHOW"] = VARS::post("WEEK_SHOW");
            $this->field["UNIT_SHOW"] = VARS::post("UNIT_SHOW");
            $this->field["REMARK_SHOW"] = VARS::post("REMARK_SHOW");
            $this->field["testday"] = VARS::post("testday");
            $this->field["testMode"] = VARS::post("testMode");
            $this->changeVal = VARS::post("changeVal");

            $this->testCalcMode = $this->field["testMode"] == "1" ? true : false;

            if ($this->testCalcMode) {
                $this->setDispData2();
            } else {
                $this->setDispData();
            }

            if (is_array($this->updDateArray)) {
                foreach ($this->updDateArray as $key => $val) {
                    $this->updEvent["EVENT_".$val["DAY"]] = VARS::post("EVENT_".$val["DAY"]);
                    $this->updEvent["HOLIDAY_FLG_".$val["DAY"]] = VARS::post("HOLIDAY_FLG_".$val["DAY"]);
                }
            }

            $this->updSchChr = array();
            $updCnt = 0;
            if (is_array($this->updDateArray)) {
                foreach ($this->updDateArray as $dateKey => $dateVal) {
                    foreach ($this->period as $pKey => $pVal) {
                        $setName = $dateVal["DAY"]."_".$pKey;
                        $this->updSchChr[$updCnt]["SEME"] = $dateVal["SEME"];
                        $this->updSchChr[$updCnt]["DAY"] = $dateVal["DAY"];
                        $this->updSchChr[$updCnt]["PERIODCD"] = $pKey;
                        $this->updSchChr[$updCnt]["CHAIRCD"] = VARS::post("CHAIR_".$setName);
                        $this->updSchChr[$updCnt]["REMARK"] = VARS::post("REMARK_".$setName);
                        $updCnt++;
                    }
                }
            }
        } else {
            $this->bunkatuSu = "9";
            $this->bunKatuField["BUNKATU_DATE"] = VARS::get("BUNKATU_DATE") ? VARS::get("BUNKATU_DATE") : $this->bunKatuField["BUNKATU_DATE"];
            $this->bunKatuField["BUNKATU_CHAIRCD"] = VARS::get("BUNKATU_CHAIRCD") ? VARS::get("BUNKATU_CHAIRCD") : $this->bunKatuField["BUNKATU_CHAIRCD"];
            $this->bunKatuField["BUNKATU_PERIOD"] = VARS::get("BUNKATU_PERIOD") ? VARS::get("BUNKATU_PERIOD") : $this->bunKatuField["BUNKATU_PERIOD"];
            $this->bunKatuField["BUNKATU_SEME"] = VARS::get("BUNKATU_SEME") ? VARS::get("BUNKATU_SEME") : $this->bunKatuField["BUNKATU_SEME"];
            for ($i = 1; $i <= $this->bunkatuSu; $i++) {
                $this->bunKatuField["B_CHAIRCD".$i] = VARS::post("B_CHAIRCD".$i);
                $this->bunKatuField["B_MINUTE".$i] = VARS::post("B_MINUTE".$i);
            }
        }

    }

    function setDispData() {

        //月初期値
        list($year, $month, $day) = preg_split("/-/", CTRL_DATE);
        $this->field["MONTH"] = $this->field["MONTH"] ? $this->field["MONTH"] : $month;

        //カレンダー配列 [曜日コード] : "WEEK" = 曜日
        //                              "DAY"[第〇週] = 日付
        $this->weekArray = array("1" => array("WEEK" => "月"),
                                 "2" => array("WEEK" => "火"),
                                 "3" => array("WEEK" => "水"),
                                 "4" => array("WEEK" => "木"),
                                 "5" => array("WEEK" => "金"),
                                 "6" => array("WEEK" => "土"),
                                 "0" => array("WEEK" => "日"));

        $year = $this->field["MONTH"] > 3 ? CTRL_YEAR : CTRL_YEAR + 1;
        //対象月の1日の曜日
        $daiItiYoubi = date("w", mktime( 0, 0, 0, $this->field["MONTH"], 1, $year));
        //カレンダーの最初の月曜日の日付(1日が月でなければ、前月の最終日曜の日付)
        $firstDate = date("Y-m-d", mktime( 0, 0, 0, $this->field["MONTH"], 1 - $daiItiYoubi, $year ));
        list($firstYear, $firstMonth, $firstDay) = preg_split("/-/", $firstDate);
        //対象月の最終日
        $last_day = date("t", mktime(0, 0, 0, $this->field["MONTH"], 1, $year));
        $last_date = date("Y-m-d", mktime(0, 0, 0, $this->field["MONTH"], $last_day, $year));
        //対象月の日数 + 前月表示分の日数
        $dayCnt = $last_day + $daiItiYoubi;
        //第〇週
        $weekArrayCnt = 1;
        //日付のカウントアップ
        $addCnt = 0;
        for ($i = 0; $i < $dayCnt; $i++) {
            //一週間
            for ($weekCnt = 1; $weekCnt <= 7; $weekCnt++) {
                $setDay = date("Y-m-d", mktime( 0, 0, 0, $firstMonth, $firstDay + $addCnt, $firstYear));
                $setDayWa = date("Y年m月d日", mktime( 0, 0, 0, $firstMonth, $firstDay + $addCnt, $firstYear));
                $setWeek = date("w", mktime( 0, 0, 0, $firstMonth, $firstDay + $addCnt, $firstYear));
                $this->weekArray[$setWeek]["DAY"][$weekArrayCnt] = $setDay;
                $this->weekArray[$setWeek]["WAREKI"][$weekArrayCnt] = $setDayWa;
                $addCnt++;
            }
            //最終日の週を処理し終わったら終了
            if ($setDay >= $last_date) {
                break;
            }
            $weekArrayCnt++;
        }
    }

    function setDispData2() {
        //月初期値
        $this->field["testday"] = $this->field["testday"] ? $this->field["testday"] : strtr(CTRL_DATE, "-", "/");;
        list($year, $month, $day) = preg_split("/\//", $this->field["testday"]);

        //カレンダー配列 [曜日コード] : "WEEK" = 曜日
        //                              "DAY"[第〇週] = 日付
        $this->weekArray = array("1" => array("WEEK" => "月"),
                                 "2" => array("WEEK" => "火"),
                                 "3" => array("WEEK" => "水"),
                                 "4" => array("WEEK" => "木"),
                                 "5" => array("WEEK" => "金"),
                                 "6" => array("WEEK" => "土"),
                                 "0" => array("WEEK" => "日"));

        //対象月の1日の曜日
        $daiItiYoubi = date("w", mktime( 0, 0, 0, $month, 1, $year));
        //カレンダーの最初の月曜日の日付(1日が月でなければ、前月の最終日曜の日付)
        $firstDate = date("Y-m-d", mktime( 0, 0, 0, $month, 1 - $daiItiYoubi, $year ));
        list($firstYear, $firstMonth, $firstDay) = preg_split("/-/", $firstDate);
        //対象月の最終日
        $last_day = date("t", mktime(0, 0, 0, $month, 1, $year));
        $last_date = date("Y-m-d", mktime(0, 0, 0, $month, $last_day, $year));
        //対象月の日数 + 前月表示分の日数
        $dayCnt = $last_day + $daiItiYoubi;
        //第〇週
        $weekArrayCnt = 1;
        //日付のカウントアップ
        $addCnt = 0;
        for ($i = 0; $i < $dayCnt; $i++) {
            //一週間
            for ($weekCnt = 1; $weekCnt <= 7; $weekCnt++) {
                $setDay = date("Y-m-d", mktime( 0, 0, 0, $firstMonth, $firstDay + $addCnt, $firstYear));
                $setDayWa = date("Y年m月d日", mktime( 0, 0, 0, $firstMonth, $firstDay + $addCnt, $firstYear));
                $setWeek = date("w", mktime( 0, 0, 0, $firstMonth, $firstDay + $addCnt, $firstYear));
                $this->weekArray[$setWeek]["DAY"][$weekArrayCnt] = $setDay;
                $this->weekArray[$setWeek]["WAREKI"][$weekArrayCnt] = $setDayWa;
                $addCnt++;
            }
            //最終日の週を処理し終わったら終了
            if ($setDay >= $last_date) {
                break;
            }
            $weekArrayCnt++;
        }
    }

    /* 入力項目チェック */
    function validateInput()
    {
        return true;
    }

    /* 更新処理 */
    function &getUpdateModel()
    {
        if ($this->validateInput()){
            $this->update();
            $this->changeVal = "";
            $this->setMessage("MSG201");
        }
        return;
    }

    /* 編集対象データを更新 */
    function &update()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($this->field["HR_OR_STAFF"] == "1") {
            knjs030Query::getUpdEvent($db, $this);
            knjs030Query::getUpdateSchChr($db, $this);
            knjs030Query::getUpdateUnitDat($db, $this);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }

    /* 分割更新処理 */
    function &getBunkatuUpdateModel()
    {
        if ($this->validateInput()){
            $this->bunkatuUpdate();
            $this->setMessage("MSG201");
        }
        return;
    }

    /* 編集対象データを更新 */
    function &bunkatuUpdate()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        if ($this->field["HR_OR_STAFF"] == "1") {
            knjs030Query::setBunkatuUpdate($db, $this);
        }

        $db->commit();
        Query::dbCheckIn($db);

        return;
    }
}
?>
