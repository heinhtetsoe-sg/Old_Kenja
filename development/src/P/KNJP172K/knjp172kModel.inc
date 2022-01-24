<?php

require_once('for_php7.php');

class knjp172kModel extends Model {

    var $cmd;
    var $schregno = "";
    var $year;
    
    function knjp172kModel()
    {
    }

    function init()
    {

        $this->cmd = VARS::request("cmd");

        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }
        $this->year = VARS::post("YEAR");

    }


    //入力項目チェック
    function getSetData($titleId)
    {
        $setTitle[0] = array("TITLE1"   => "授<br>業<br>料",
                             "TITLE2"   => "納入必要額");
        $setTitle[1] = array("TITLE3"   => "入金額");
        $setTitle[2] = array("TITLE4"   => "就<br>学<br>支<br>援<br>金",
                             "TITLE5"   => "基本額",
                             "TITLE6"   => "予定額");
        $setTitle[3] = array("TITLE7"   => "決定額");
        $setTitle[4] = array("TITLE8"   => "加算額",
                             "TITLE9"   => "予定額");
        $setTitle[5] = array("TITLE7"   => "決定額");
        $setTitle[6] = array("TITLE10"  => "補<br>助<br>金",
                             "TITLE11"  => "授業料",
                             "TITLE12"  => "予定額");
        $setTitle[7] = array("TITLE13"  => "交付決定額合計");

        $setData = array("DATA04" => "",
                         "DATA05" => "",
                         "DATA06" => "",
                         "DATA07" => "",
                         "DATA08" => "",
                         "DATA09" => "",
                         "DATA10" => "",
                         "DATA11" => "",
                         "DATA12" => "",
                         "DATA01" => "",
                         "DATA02" => "",
                         "DATA03" => "",
                         "KEI"    => "",
                         "ZAN"    => "",
                         "FUKEN"  => "",
                         "REMARK" => "");

        $setData = array_merge($setTitle[$titleId], $setData);

        return $setData;
    }

}
?>
