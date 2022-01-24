<?php

require_once('for_php7.php');
class knjp173kModel extends Model {

    var $cmd;
    var $schregno = "";
    
    function knjp173kModel()
    {
    }

    function init()
    {

        $this->cmd = VARS::request("cmd");

        $this->getPropertiesAll();
        if ($this->Properties["use_prg_schoolkind"] == "1") {
            $this->getSelectSchoolKind();
        }

        if (VARS::get("SCHREGNO") != "") {
            $this->schregno = VARS::get("SCHREGNO");
        }

        $this->decFlg = VARS::request("DEC_FLG");
    }

    //入力項目チェック
    function getSetData($titleId)
    {
        $setTitle[0] = array("TITLE1"   => "基本額",
                             "TITLE2"   => "予定額");
        $setTitle[1] = array("TITLE3"   => "決定額");
        $setTitle[2] = array("TITLE1"   => "加算額",
                             "TITLE2"   => "予定額");
        $setTitle[3] = array("TITLE3"   => "決定額");
        $setTitle[4] = array("TITLE1"   => "合計",
                             "TITLE2"   => "予定額");
        $setTitle[5] = array("TITLE3"   => "決定額");

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
                         "KEI"    => "");

        $setData = array_merge($setTitle[$titleId], $setData);

        return $setData;
    }


    function &getUpdateModel()
    {
        $db = Query::dbCheckOut();
        $db->autoCommit(false);

        $month = array("04", "05", "06", "07", "08", "09", "10", "11", "12", "01", "02", "03");

        foreach ($month as $key => $val) {
            //画面のコンテンツ
            $planYear = $val < "04" ? CTRL_YEAR + 1 : CTRL_YEAR;
            $this->field[$val]["PLAN_YEAR"] = $planYear;
            $this->field[$val]["PLAN_MONTH"] = $val;
            $this->field[$val]["PLAN_CANCEL_FLG"] = VARS::post("DATA".$val."DUE_FLG") ? "1" : NULL;
            $this->field[$val]["ADD_PLAN_CANCEL_FLG"] = VARS::post("DATA".$val."ADD_FLG") ? "1" : NULL;
        }

        //foreach
        foreach ($this->field as $key => $val) {
            $query = knjp173kQuery::getUpdatePlanQuery($db, $this, $val);
            $db->query($query);

        }

        $query = knjp173kQuery::getUpdateQuery($db, $this);
        $db->query($query);

        $db->commit();
        Query::dbCheckIn($db);

        $this->setMessage("MSG201");
    }
}
?>
