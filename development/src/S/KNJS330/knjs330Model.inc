<?php

require_once('for_php7.php');

class knjs330Model extends Model {
    var $field      = array();
    var $cmd;
    var $control;        //コントロールマスタ
    var $weekArray = array();

    function knjs330Model()
    {
        //コントロールマスタより情報を取得
        common::GetControlMaster_Fnc($this->control);
    }

    function init()
    {
        $this->field = array("GRADE_HR_CLASS"   => VARS::post("GRADE_HR_CLASS"),    //対象クラス
                             "MONTH"            => VARS::post("MONTH"),             //対象月
                             "WEEK"             => VARS::post("WEEK"),              //週
                             "JISU"             => VARS::post("JISU"),              //教科名
                             "UNIT"             => VARS::post("UNIT"),              //単元名
                             "REMARK"           => VARS::post("REMARK"),            //備考欄
                             "FORM"             => VARS::post("FORM"),              //フォーム
                             "DBNAME"           => VARS::post("DBNAME")             //DB変更に備えて
                            );

        $this->cmd   = VARS::request("cmd");
        $this->setDispData();
        $this->getPropertiesAll();
    }

    //変数をクリアします
    function clean()
    {
    }

    //KNJS030からコピーしただけ
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
}
?>
